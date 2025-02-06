package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.constants.ResponeMsg;
import com.wubing.kafka.agent.domain.entity.ConsumerGroupInfo;
import com.wubing.kafka.agent.domain.entity.TopicPartitionConsumerInfo;
import com.wubing.kafka.agent.domain.response.ConsumerGroupDesResponse;
import com.wubing.kafka.agent.domain.response.ConsumerGroupListResponse;
import com.wubing.kafka.agent.exception.KafkaClientException;
import com.wubing.kafka.agent.service.ConsumerGroupService;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;
import org.eclipse.jetty.util.Promise;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsumerGroupServiceImpl implements ConsumerGroupService {

    @Override
    public ResultData<String> deleteConsumerGroup(String groupId) {
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            // 在线状态下的消费组不允许删除
            ConsumerGroupDescription groupDescription = adminClient.describeConsumerGroups(Collections.singletonList(groupId))
                    .describedGroups().get(groupId).get();
            if (groupDescription.state() != ConsumerGroupState.EMPTY) {
                return ResultData.fail(ReturnCode.RC103.getCode(), ReturnCode.RC103.getMessage());
            }

            adminClient.deleteConsumerGroups(Collections.singletonList(groupId)).all().get();
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC101.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.DELETE_CONSUMER_GROUP_SUCCESS);
    }

    @Override
    public ResultData<ConsumerGroupDesResponse> describeConsumerGroup(String groupId) {
        try {
            return ResultData.success(getConsumerGroupDesByGroupId(groupId));
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC101.getCode(), e.getMessage());
        }

    }



    public ConsumerGroupDesResponse getConsumerGroupDesByGroupId(String groupId) throws ExecutionException, InterruptedException {
        AtomicLong totalLag = new AtomicLong(0L);
        ConsumerGroupDesResponse groupDesResponse = new ConsumerGroupDesResponse();
        groupDesResponse.setGroupId(groupId);

        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        ConsumerGroupDescription groupDescription = adminClient.describeConsumerGroups(Collections.singletonList(groupId))
                .describedGroups().get(groupId).get();

        // get consumerGroup offset and topic offset
        Map<TopicPartition, OffsetAndMetadata> groupPartitionOffsetAndMetadataMap = adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();

        Map<TopicPartition, OffsetSpec> topicPartitionOffsets = new HashMap<>();
        groupDescription.members().forEach(memberDescription -> memberDescription.assignment().topicPartitions().forEach(tp -> topicPartitionOffsets.put(tp, OffsetSpec.latest())));
        groupPartitionOffsetAndMetadataMap.keySet().forEach(tp -> topicPartitionOffsets.put(tp, OffsetSpec.latest()));
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> topicListOffsetsResult = adminClient.listOffsets(topicPartitionOffsets).all().get();

        groupDesResponse.setState(groupDescription.state().toString());
        groupDesResponse.setPartitionAssignor(groupDescription.partitionAssignor());
        groupDesResponse.setMemberNum(groupDescription.members().size());
        Map<String, List<TopicPartitionConsumerInfo>> topicPartitionConsumerMap = new HashMap<>();
        Map<TopicPartition, TopicPartitionConsumerInfo> topicPartitionClientMap = new HashMap<>();
//         计算 消费组的消费位移
        if (!groupDescription.members().isEmpty()) {
            groupDescription.members().forEach(memberDescription -> memberDescription.assignment().topicPartitions().forEach(tp -> {
                        TopicPartitionConsumerInfo topicPartitionConsumerInfo = new TopicPartitionConsumerInfo(
                                tp.partition(), memberDescription.consumerId(), memberDescription.clientId(), memberDescription.host(), 0, 0, 0);
                        topicPartitionClientMap.put(new TopicPartition(tp.topic(), tp.partition()), topicPartitionConsumerInfo);
                    })
            );
        }
        topicPartitionOffsets.keySet().forEach(tp -> {
            List<TopicPartitionConsumerInfo> topicPartitionConsumerInfoList = new ArrayList<>();
            if (topicPartitionConsumerMap.containsKey(tp.topic())) {
                topicPartitionConsumerInfoList = topicPartitionConsumerMap.get(tp.topic());
            }
            long topicCurrentOffset1 = 0L;
            if (groupPartitionOffsetAndMetadataMap.get(tp) != null) {
                topicCurrentOffset1 = groupPartitionOffsetAndMetadataMap.get(tp).offset();
            }
            String memberId = "";
            String clientId = "";
            String host = "";
            if (topicPartitionClientMap.get(tp) != null) {
                memberId = topicPartitionClientMap.get(tp).getMemberId();
                clientId = topicPartitionClientMap.get(tp).getClientId();
                host = topicPartitionClientMap.get(tp).getHost().replace("/", "");
            }
            TopicPartitionConsumerInfo topicPartitionConsumerInfo = new TopicPartitionConsumerInfo(
                    tp.partition(), memberId, clientId, host, topicCurrentOffset1,
                    topicListOffsetsResult.get(tp).offset(), topicListOffsetsResult.get(tp).offset() - topicCurrentOffset1
            );
            totalLag.addAndGet(topicPartitionConsumerInfo.getLag());
            topicPartitionConsumerInfoList.add(topicPartitionConsumerInfo);
            topicPartitionConsumerMap.put(tp.topic(), topicPartitionConsumerInfoList);
        });
        groupDesResponse.setLag(totalLag.longValue());
        groupDesResponse.setTopicConsumerInfoMap(topicPartitionConsumerMap);
        return groupDesResponse;
    }

    @Override
    public ResultData<ConsumerGroupListResponse> listConsumerGroup(int pageIndex, int pageSize) {
        ConsumerGroupListResponse groupListResponse = new ConsumerGroupListResponse();
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);

            List<String> groupList;

            List<ConsumerGroupInfo> allGroups = adminClient.listConsumerGroups()
                    .valid()
                    .get(10, TimeUnit.SECONDS)
                    .stream().filter(x -> x.state().isPresent())
                    .map(consumer -> new ConsumerGroupInfo(consumer.groupId(), consumer.state().get().toString()))
                    .collect(Collectors.toList());

            groupListResponse.setTotalCount(allGroups.size());
            List<ConsumerGroupInfo> returnGroupList;
            if (allGroups.size() <= pageSize) {
                returnGroupList = allGroups;

            } else {
                int fromIndex = (pageIndex - 1) * pageSize;
                int toIndex = Math.min((pageIndex * pageSize), allGroups.size());
                returnGroupList = allGroups.subList(fromIndex, toIndex);
            }
            groupList = returnGroupList.stream().map(ConsumerGroupInfo::getGroupId).collect(Collectors.toList());
            Map<String, ConsumerGroupDescription> groupDescriptionMap = adminClient.describeConsumerGroups(groupList).all().get();
            returnGroupList.stream().parallel().map(x -> {
                int memberNum = 0;
                ConsumerGroupDescription consumerGroupDescription = groupDescriptionMap.get(x.getGroupId());
                if (consumerGroupDescription.members() != null) {
                    memberNum = consumerGroupDescription.members().size();
                }
                x.setMemberNum(memberNum);
                x.setLag(getLagByGroupId(consumerGroupDescription, x.getGroupId()));
                return x;
            }).collect(Collectors.toList());
            groupListResponse.setItem(returnGroupList);
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC102.getCode(), ReturnCode.RC102.getMessage());
        }
        return ResultData.success(groupListResponse);
    }


    private Long getLagByGroupId(ConsumerGroupDescription groupDescription, String groupId) {

        AtomicLong totalLag = new AtomicLong(0L);
        AdminClient adminClient = null;
        try {
            adminClient = KafkaClientPool.createAdminClient();

            // get consumerGroup offset and topic offset
            Map<TopicPartition, OffsetAndMetadata> groupPartitionOffsetAndMetadataMap = adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();

            Map<TopicPartition, OffsetSpec> topicPartitionOffsets = new HashMap<>();
            groupDescription.members().forEach(memberDescription -> memberDescription.assignment().topicPartitions().forEach(tp -> topicPartitionOffsets.put(tp, OffsetSpec.latest())));
            groupPartitionOffsetAndMetadataMap.keySet().forEach(tp -> topicPartitionOffsets.put(tp, OffsetSpec.latest()));
            Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> topicListOffsetsResult = adminClient.listOffsets(topicPartitionOffsets).all().get();

            groupPartitionOffsetAndMetadataMap.keySet().forEach(tp -> {
                long topicCurrentOffset1 = 0L;
                if (groupPartitionOffsetAndMetadataMap.get(tp) != null) {
                    topicCurrentOffset1 = groupPartitionOffsetAndMetadataMap.get(tp).offset();
                }
                totalLag.addAndGet(topicListOffsetsResult.get(tp).offset() - topicCurrentOffset1);
            });
        } catch (Exception e) {
            log.error("exception: ", e);
            throw new KafkaClientException(e.getMessage(), e.getCause());
        } finally {
            if (adminClient != null) {
                adminClient.close();
            }
        }
        return totalLag.longValue();
    }
}
