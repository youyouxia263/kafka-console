package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.constants.KafkaCreateTopicConfigs;
import com.wubing.kafka.agent.constants.ResponeMsg;
import com.wubing.kafka.agent.domain.entity.TopicBeanForImportAndExport;
import com.wubing.kafka.agent.domain.entity.BatchTopicsBean;
import com.wubing.kafka.agent.domain.entity.ResourceInspectBean;
import com.wubing.kafka.agent.domain.response.GroupsInfoForTopicResponse;
import com.wubing.kafka.agent.domain.response.TopicDescribeResponse;
import com.wubing.kafka.agent.domain.response.TopicListResponse;
import com.wubing.kafka.agent.domain.response.TopicPropertyResponse;
import com.wubing.kafka.agent.exception.KafkaClientException;
import com.wubing.kafka.agent.service.TopicOpService;
import com.wubing.kafka.agent.utils.KafkaOpUtil;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.wubing.kafka.agent.domain.request.TopicConfigReq;
import com.wubing.kafka.agent.domain.request.TopicPropertyReq;
import com.wubing.kafka.agent.domain.request.UpdatePartitionReq;
import com.wubing.kafka.agent.domain.request.UpdateReplicaReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.*;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.ApiException;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class TopicOpServiceImpl implements TopicOpService {

    @Override
    public ResultData<String> createTopic(TopicConfigReq topicConfigReq)  {
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            if (adminClient.listTopics().names().get().contains(topicConfigReq.getTopicName())){
                return ResultData.fail(ReturnCode.RC003.getCode(), ReturnCode.RC003.getMessage());
            }
            NewTopic newTopic = new NewTopic(topicConfigReq.getTopicName(), topicConfigReq.getPartitions(),
                    (short) topicConfigReq.getReplicationFactor()).configs(topicConfigReq.getConfigs());
            Collection<NewTopic> newTopicList = new ArrayList<>();
            newTopicList.add(newTopic);
            CreateTopicsResult createTopicsResult = adminClient.createTopics(newTopicList);

            createTopicsResult.all().get();
            Thread.sleep(200);
        } catch (Exception e) {
            log.error("Create topic failed: ", e);
            return ResultData.fail(ReturnCode.RC001.getCode(),  e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.CREATE_TOPIC_SUCCESS);
    }

    @Override
    public ResultData<String> deleteTopic(String topicName) {
        try {
            if (ifAnyProducerOnlineInTopic(topicName) || ifAnyConsumerGroupOnlineInTopic(topicName)) {
                return ResultData.fail(ReturnCode.RC014.getCode(), ReturnCode.RC014.getMessage());
            }
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            DeleteTopicsResult topicsResult = adminClient.deleteTopics(Collections.singletonList(topicName));
            topicsResult.all().get();
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC002.getCode(),  e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.DELETE_TOPIC_SUCCESS);
    }

    @Override
    public ResultData<ResourceInspectBean> deleteTopicIfLegal(String topicName) {
        List<String> warnings = Lists.newArrayList();
        if (ifAnyProducerOnlineInTopic(topicName)) {
            warnings.add("实例内还存在向topic中生产消息的生产者，请停止生产后再删除topic");
        }
        if (ifAnyConsumerGroupOnlineInTopic(topicName)) {
            warnings.add("实例内还存在正在消费topic的消费者，请停止消费后再释放删除topic");
        }
        if (warnings.isEmpty()) {
            return ResultData.success(new ResourceInspectBean(true, "允许删除该Topic"));
        } else {
            return ResultData.success(new ResourceInspectBean(false, String.format("不允许删除该Topic: %s", String.join(";", warnings))));
        }
    }

    @Override
    public ResultData<TopicDescribeResponse> describeTopic(String topicName)  {
        TopicDescription topicInfo;
        TopicDescribeResponse topicDescribeResponse = new TopicDescribeResponse();
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            DescribeTopicsResult topicsResult = adminClient.describeTopics(Collections.singletonList(topicName));
            topicInfo = topicsResult.all().get().get(topicName);
            topicDescribeResponse.setTopicId(topicInfo.topicId().toString());
            topicDescribeResponse.setName(topicInfo.name());
            topicDescribeResponse.setInternal(topicInfo.isInternal());
            topicDescribeResponse.setPartitions(getPartitionFromTopicResult(topicInfo.partitions(),topicInfo.name(), adminClient));
            ConfigResource topicConfigResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
            DescribeConfigsResult describeConfigsResult =  adminClient.describeConfigs(Collections.singletonList(topicConfigResource));
            Map<ConfigResource, Config> topicConfigMap = describeConfigsResult.all().get();
            Config mapConfig = topicConfigMap.get(topicConfigResource);
            topicDescribeResponse.setTopicPropertiesByConfig(mapConfig);
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC005.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }

        return ResultData.success(topicDescribeResponse);
    }

    private List<GroupsInfoForTopicResponse> getGroupsForTopic(String topicName, Set<TopicPartition> topicPartitions, AdminClient adminClient) throws ExecutionException, InterruptedException, TimeoutException {
        KafkaConsumer<String, String> kafkaConsumer = null;
        List<GroupsInfoForTopicResponse> groupsInfoForTopicList = new ArrayList<>();
        List<GroupsInfoForTopicResponse> groupsInfoForTopicTempList;
        try {
            ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
            groupsInfoForTopicTempList = listConsumerGroupsResult.all().get().stream().map(x -> {
                GroupsInfoForTopicResponse groupsInfoForTopicResponse = new GroupsInfoForTopicResponse();
                groupsInfoForTopicResponse.setGroupId(x.groupId());
                groupsInfoForTopicResponse.setState(x.state().get().toString());
                return groupsInfoForTopicResponse;
                    }
            ).collect(Collectors.toList());
            List<String> consumerGroups =groupsInfoForTopicTempList.stream().map(GroupsInfoForTopicResponse::getGroupId).collect(Collectors.toList());
            // Get the consumer group description and its respective lag for each partition
            DescribeConsumerGroupsResult describeConsumerGroupsResult = adminClient.describeConsumerGroups(consumerGroups);
            kafkaConsumer = KafkaClientPool.createConsumer();
            kafkaConsumer.assign(topicPartitions);
            Map<TopicPartition, Long> topicPartitionOffsetMap = kafkaConsumer.endOffsets(topicPartitions);
            Map<String, ConsumerGroupDescription> consumerGroupDescriptionMap = describeConsumerGroupsResult.all().get();
            groupsInfoForTopicTempList.stream().parallel().forEach(groupsInfo -> {
                ConsumerGroupDescription consumerGroupDescription = consumerGroupDescriptionMap.get(groupsInfo.getGroupId());
                Map<TopicPartition, OffsetAndMetadata> groupPartitionOffsetAndMetadataMap;
                try {
                    groupPartitionOffsetAndMetadataMap = adminClient.listConsumerGroupOffsets(groupsInfo.getGroupId()).partitionsToOffsetAndMetadata().get();
                } catch (Exception e) {
                    log.error("exception: ", e);
                    throw new KafkaClientException(e.getMessage(), e.getCause());
                }
                groupsInfo.setMemberNum(consumerGroupDescription.members().size());
                Set<TopicPartition> partitions = groupPartitionOffsetAndMetadataMap.keySet().stream().filter(x -> x.topic().equals(topicName)).collect(Collectors.toSet());
                if (partitions.isEmpty()){
                    return;
                }
                long totalLag = 0;
                Map<TopicPartition, OffsetAndMetadata> finalGroupPartitionOffsetAndMetadataMap = groupPartitionOffsetAndMetadataMap;
                totalLag = topicPartitionOffsetMap.entrySet().stream()
                        .mapToLong(entry -> {
                            long currentConsumerOffset = 0;
                            if (finalGroupPartitionOffsetAndMetadataMap.get(entry.getKey()) != null) {
                                currentConsumerOffset = finalGroupPartitionOffsetAndMetadataMap.get(entry.getKey()).offset();
                            }
                            return entry.getValue() - currentConsumerOffset;
                        }).sum();
                groupsInfo.setLag(totalLag);
                groupsInfoForTopicList.add(groupsInfo);

            });

        }catch (Exception e) {
            log.error("exception: ", e);
            throw new KafkaClientException(e.getMessage(), e.getCause());
        }finally {
            if (kafkaConsumer != null) {
                kafkaConsumer.close();
            }
        }
        return groupsInfoForTopicList;

    }

    private List<TopicDescribeResponse.Partitions> getPartitionFromTopicResult(List<TopicPartitionInfo> partitions,
                                                                               String topicName, AdminClient adminClient) throws ExecutionException, InterruptedException{
        List<TopicDescribeResponse.Partitions> partitionsList = new ArrayList<>();

        Map<TopicPartition, OffsetSpec> topicPartitionLatestOffsets = new HashMap<>();
        Map<TopicPartition, OffsetSpec> topicPartitionEarliestOffsets = new HashMap<>();
        partitions.forEach(x->{
            topicPartitionLatestOffsets.put(new TopicPartition(topicName, x.partition()), OffsetSpec.latest());
            topicPartitionEarliestOffsets.put(new TopicPartition(topicName, x.partition()), OffsetSpec.earliest());
        } );
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsetsMap = adminClient.listOffsets(topicPartitionLatestOffsets).all().get();
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> earliestOffsetsMap = adminClient.listOffsets(topicPartitionEarliestOffsets).all().get();
        for (TopicPartitionInfo x : partitions) {
            TopicDescribeResponse.Partitions partitionsRes = new TopicDescribeResponse.Partitions();
            partitionsRes.setPartition(x.partition());
            partitionsRes.setLeader(x.leader().id());
            partitionsRes.setReplicas(x.replicas().stream().map(Node::id).collect(toList()));
            partitionsRes.setIsr(x.isr().stream().map(Node::id).collect(toList()));
            TopicPartition topicPartition = new TopicPartition(topicName, x.partition());

            partitionsRes.setOffsetMax(latestOffsetsMap.get(topicPartition).offset());
            partitionsRes.setOffsetMin(earliestOffsetsMap.get(topicPartition).offset());
            partitionsList.add(partitionsRes);
        }
        return partitionsList;
    }

    @Override
    public ResultData<TopicListResponse> listTopic(int pageIndex, int pageSize){
        TopicListResponse topicListResponse = new TopicListResponse();
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
            listTopicsOptions.listInternal(true);
            ListTopicsResult listTopicsResult = adminClient.listTopics(listTopicsOptions);
            Collection<TopicListing> topicListings = listTopicsResult.listings().get();
            Collection<String> topicNames = topicListings.stream().map(TopicListing::name).collect(toList());
            Map<String, TopicDescription> topicDescriptionMap = adminClient.describeTopics(topicNames).all().get();
            topicListResponse.setTotalCount(topicListings.size());
            List<TopicListResponse.TopicNameInfo> item = new ArrayList<>();
            topicListings.forEach(x -> {
                if (!x.isInternal()) {
                    TopicListResponse.TopicNameInfo topicNameInfo = new TopicListResponse.TopicNameInfo();
                    topicNameInfo.setName(x.name());
                    topicNameInfo.setInternal(x.isInternal());
                    topicNameInfo.setPartitionNum(topicDescriptionMap.get(x.name()).partitions().size());
                    topicNameInfo.setReplicasNum(topicDescriptionMap.get(x.name()).partitions().get(0).replicas().size());
                    item.add(topicNameInfo);
                }
            });
            topicListResponse.setTotalCount(item.size());
            if (item.size() <= pageSize || (pageIndex == -1 && pageSize == -1)) {
                topicListResponse.setItem(item);
            } else {
                topicListResponse.setItem(item.subList((pageIndex - 1) * pageSize, Math.min((pageIndex * pageSize), item.size())));
            }
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC004.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(topicListResponse);
    }

    @Override
    public ResultData<List<GroupsInfoForTopicResponse>> consumerGroupsByTopic(String topicName) {
        List<GroupsInfoForTopicResponse> groupsInfoForTopicResponseList;

        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            DescribeTopicsResult topicsResult = adminClient.describeTopics(Collections.singletonList(topicName));
            Set<TopicPartition> partitions = topicsResult.all().get().get(topicName).partitions().stream().map(x->new TopicPartition(topicName, x.partition())).collect(Collectors.toSet());
            groupsInfoForTopicResponseList = getGroupsForTopic(topicName, partitions, adminClient);

        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC010.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(groupsInfoForTopicResponseList);
    }

    @Override
    public ResultData<TopicPropertyResponse> getTopicProperty(String topicName) {
        TopicPropertyResponse topicPropertyResponse = new TopicPropertyResponse();
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            if (!adminClient.listTopics().names().get().contains(topicName)){
                return ResultData.fail(ReturnCode.RC009.getCode(), ReturnCode.RC009.getMessage());
            }
            ConfigResource topicConfigResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
            DescribeConfigsResult describeConfigsResult =  adminClient.describeConfigs(Collections.singletonList(topicConfigResource));
            Map<ConfigResource, Config> topicConfigMap = describeConfigsResult.all().get();
            Config mapConfig = topicConfigMap.get(topicConfigResource);
            topicPropertyResponse.setTopicPropertyByConfig(mapConfig);
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC006.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(topicPropertyResponse);

    }

    @Override
    public ResultData<String> updateTopicProperty(TopicPropertyReq topicPropertyReq){
        try {
            KafkaOpUtil.opKafkaConfig(ConfigResource.Type.TOPIC, topicPropertyReq.getTopicName(), topicPropertyReq.getConfigs());
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC008.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.UPDATE_TOPIC_CONFIG_SUCCESS);
    }

    /**
     * 增加kafka的topic分区数
     * @param updatePartitionReq
     * @return
     */
    @Override
    public ResultData<String> updateTopicPartitions(UpdatePartitionReq updatePartitionReq){
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);

            NewPartitions newPartitions = NewPartitions.increaseTo(updatePartitionReq.getUpdatePartition());
            Map<String, NewPartitions> partitionsMap = new HashMap<>();
            partitionsMap.put(updatePartitionReq.getTopicName(), newPartitions);
            adminClient.createPartitions(partitionsMap).all().get();
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC011.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.UPDATE_PARTITION_COUNT_SUCCESS);

    }

    @Override
    public ResultData<String> updateTopicReplica(UpdateReplicaReq updateReplicaReq) {
        if (updateReplicaReq.getUpdateReplica() <= updateReplicaReq.getReplicas()){
            return ResultData.fail(ReturnCode.RC012.getCode(), ResponeMsg.UPDATE_REPLICAS_COUNT_LITTLE_FAILED);
        }
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            List<Integer> brokerIds = adminClient.describeCluster().nodes().get().stream()
                    .map(Node::id).collect(Collectors.toList());
            if (brokerIds.size() < updateReplicaReq.getUpdateReplica()) {
                return ResultData.fail(ReturnCode.RC012.getCode(), ResponeMsg.NO_BROKERS_NUMS_FOR_REPLICAS_COUNT);
            }
            Map<TopicPartition, Optional<NewPartitionReassignment>> reassignments = new HashMap<>();
            IntStream.range(0, updateReplicaReq.getPartitionNum()).forEach(currentPartition -> {
                List<Integer> newReplicas = new ArrayList<>();
                Collections.shuffle(brokerIds);
                int brokerCount = 0;
                while (newReplicas.size() < updateReplicaReq.getUpdateReplica() ) {
                    int brokerId = brokerIds.get(brokerCount++);
                    if (!newReplicas.contains(brokerId)) {
                        newReplicas.add(brokerId);
                    }
                }
                reassignments.put(new TopicPartition(updateReplicaReq.getTopicName(), currentPartition), Optional.of(new NewPartitionReassignment(newReplicas)));
            });
            adminClient.alterPartitionReassignments(reassignments).all().get();
        }catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC012.getCode(),  e.getCause() == null ?e.getMessage(): e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.UPDATE_PARTITION_COUNT_SUCCESS);
    }

    @Override
    public ResultData<String> importTopics(String batchTopicJson) {
        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        BatchTopicsBean importedTopics;
        long tik = System.currentTimeMillis();
        try {
            importedTopics = new ObjectMapper().readValue(batchTopicJson, BatchTopicsBean.class);
        } catch (JsonProcessingException e) {
            System.out.println("导入的Topic json文件格式错误，解析失败: " + e);
            return ResultData.fail(ReturnCode.RC015.getCode(), "导入的Topic json文件格式错误，解析失败");
        }
        for (TopicBeanForImportAndExport topic : importedTopics.getTopics()) {
            if (topic.getPartitions() < 1) {
                topic.setPartitions(12);
            }
            if (topic.getReplicationFactor() < 1) {
                topic.setReplicationFactor(3);
            }
        }
        
        // 根据经验值限制导入的topic总量不能超过100个
        if (importedTopics.getTopics().size() > 100) {
            return ResultData.fail(ReturnCode.RC015.getCode(), "批量导入的topic总量不能超过100个");
        } else {
            int totalPartitions = importedTopics.getTopics().stream().mapToInt(topic -> topic.getPartitions() * topic.getReplicationFactor()).sum();
            if (totalPartitions > 3600) {
                return ResultData.fail(ReturnCode.RC015.getCode(), "批量导入的topic数量小于100，但总分区副本数（每个topic的分区副本数（分区数 * 副本因子数）的累加）不能超过3600");
            }
        }
        
        try {
            Set<String> existingTopics = adminClient.listTopics().names().get();
            List<String> topicNames = importedTopics.getTopics().stream().map(TopicBeanForImportAndExport::getTopic).collect(Collectors.toList());
            
            List<String> duplicateTopicList = topicNames.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .entrySet().stream().filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(toList());
            if (!duplicateTopicList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("导入的Topic中有重名Topic，请去重后重新导入。").append(System.lineSeparator()).append("重名Topic有: ").append(System.lineSeparator());
                duplicateTopicList.forEach(topicName -> sb.append(topicName).append(System.lineSeparator()));
                return ResultData.fail(ReturnCode.RC015.getCode(), sb.toString());
            }
            
            Set<String> diffTopics = new HashSet<>(existingTopics);
            boolean ifHaveConflictTopic = diffTopics.removeAll(new HashSet<>(topicNames));
            if (ifHaveConflictTopic) {
                existingTopics.removeAll(diffTopics);
                StringBuilder sb = new StringBuilder();
                sb.append("导入的Topic中有集群中已存在的Topic，导入失败").append(System.lineSeparator()).append("和集群中冲突的Topic有: ").append(System.lineSeparator());
                existingTopics.forEach(topicName -> sb.append(topicName).append(System.lineSeparator()));
                return ResultData.fail(ReturnCode.RC015.getCode(), sb.toString());
            }
            int nodeNum = adminClient.describeCluster().nodes().get().size();
            for (TopicBeanForImportAndExport topic : importedTopics.getTopics()) {
                if (topic.getReplicationFactor() > nodeNum) {
                    return ResultData.fail(ReturnCode.RC015.getCode(), "导入的Topic中 " + topic.getTopic() + " 的副本数大于集群节点数: " + nodeNum + "，导入失败");
                }
            }
            
            for (TopicBeanForImportAndExport topic : importedTopics.getTopics()) {
                Map<String, String> configs = topic.getConfigs();
                if (configs.containsKey(KafkaCreateTopicConfigs.CLEANUP_POLICY_CONFIG)) {
                    String cleanupPolicy = configs.get(KafkaCreateTopicConfigs.CLEANUP_POLICY_CONFIG);
                    if (!cleanupPolicy.equals("delete") && !cleanupPolicy.equals("compact")) {
                        return ResultData.fail(ReturnCode.RC015.getCode(), topic.getTopic() + "配置错误：Topic的清理策略只能是delete或compact，导入失败");
                    }
                }
                if (configs.containsKey(KafkaCreateTopicConfigs.MIN_INSYNC_REPLICAS_CONFIG)) {
                    int minInSyncReplicas = Integer.parseInt(configs.get(KafkaCreateTopicConfigs.MIN_INSYNC_REPLICAS_CONFIG));
                    if (minInSyncReplicas <= 0) {
                        return ResultData.fail(ReturnCode.RC015.getCode(), topic.getTopic() + "配置错误：Topic的最小同步副本数必须大于0，导入失败");
                    }
                    if (minInSyncReplicas > topic.getReplicationFactor()) {
                        return ResultData.fail(ReturnCode.RC015.getCode(), topic.getTopic() + "配置错误：Topic的最小同步副本数不能大于副本数" + topic.getReplicationFactor() + "，导入失败");
                    }
                }
                if (configs.containsKey(KafkaCreateTopicConfigs.RETENTION_MS_CONFIG)) {
                    long retentionMs = Long.parseLong(configs.get(KafkaCreateTopicConfigs.RETENTION_MS_CONFIG));
                    if (retentionMs < 24 * 60 * 60 * 1000L || retentionMs > 672 * 60 * 60 * 1000L) {
                        return ResultData.fail(ReturnCode.RC015.getCode(), topic.getTopic() + "配置错误：Topic的消息保留时长必须在24到672小时之间，导入失败");
                    }
                }
            }
            
            Collection<NewTopic> newTopics = importedTopics.getTopics().stream()
                .map(topic -> new NewTopic(topic.getTopic(), topic.getPartitions(), (short) topic.getReplicationFactor()).configs(topic.getConfigs()))
                .collect(Collectors.toList());
            CreateTopicsResult createTopicsResult = adminClient.createTopics(newTopics);
            
            // 检查是否有创建失败的topic
            StringBuilder sb = new StringBuilder();
            createTopicsResult.values().forEach((topicName, future) -> {
                try {
                    future.get();
                    log.info("Successfully created topic: " + topicName);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof ApiException) {
                        ApiException apiException = (ApiException) cause;
                        String errorMsg = apiException.getCause() == null ? apiException.getMessage() : apiException.getCause().getMessage();
                        sb.append("导出topic - " + topicName + " 失败，原因: " + errorMsg).append(System.lineSeparator());
                    } else {
                        sb.append("导出topic - " + topicName + " 失败，原因: " + cause).append(System.lineSeparator());
                    }
                } catch (InterruptedException e) {
                    sb.append("创建topic - : " + topicName + " 时线程中断").append(System.lineSeparator());
                }
            });
            System.out.println("batch import topics costs total time：" + (System.currentTimeMillis() - tik) + " ms");
            log.info("batch import topics costs total time：：" + (System.currentTimeMillis() - tik) + " ms");
            if (sb.length() == 0) {
                return ResultData.success(ResponeMsg.IMPORT_TOPICS_SUCCESS_CH);
            } else {
                return ResultData.fail(ReturnCode.RC015.getCode(), sb.toString());
            }
            
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC015.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
    }

    @Override
    public ResultData<String> exportTopics(List<String> exportedTopicList, boolean exportAll, HttpServletResponse response) {
        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        BatchTopicsBean batchTopicsBean = new BatchTopicsBean();
        batchTopicsBean.setTopics(new ArrayList<>());
        try {
            Map<String, TopicDescription> topicDescriptionMap;
            if (exportAll) {
                topicDescriptionMap = adminClient.describeTopics(adminClient.listTopics().names().get()).all().get();
            } else {
                topicDescriptionMap = adminClient.describeTopics(exportedTopicList).all().get();
            }

            for (String topic : topicDescriptionMap.keySet()) {
                TopicDescription topicDescription = topicDescriptionMap.get(topic);
                TopicBeanForImportAndExport topicBean = new TopicBeanForImportAndExport();
                // 导出topic名称，分区数，和副本数
                topicBean.setTopic(topic);
                topicBean.setPartitions(topicDescription.partitions().size());
                for (int i = 0; i < topicDescription.partitions().size(); i++) {
                    int replicationFactor = topicDescription.partitions().get(i).replicas().size();
                    if (i == 0) {
                        topicBean.setReplicationFactor(replicationFactor);
                    }
                    else if (replicationFactor != topicBean.getReplicationFactor()) {
                        topicBean.setCaution("注意：该Topic中每个分区的副本数不一致，导出时统一使用分区0的副本数：" + topicBean.getReplicationFactor()
                            + "，使用该文件向其他集群导入topic时将出现对应Topic的分区副本数与导出集群的Topic的分区副本数不一致的结果");
                    }
                }
                // 导出其他配置
                ConfigResource topicConfigResource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
                DescribeConfigsResult describeConfigsResult =  adminClient.describeConfigs(Collections.singletonList(topicConfigResource));
                Map<ConfigResource, Config> topicConfigMap = describeConfigsResult.all().get();
                Config mapConfig = topicConfigMap.get(topicConfigResource);
                topicBean.setTopicPropertiesByConfig(mapConfig);
                
                batchTopicsBean.getTopics().add(topicBean);
            }
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC016.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        
        // 将导出Topics的json string通过输出流方式返回给前端
        try {
            String exportedTopicsJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(batchTopicsBean);
            return ResultData.success(exportedTopicsJson);
        } catch (JsonProcessingException e) {
            return ResultData.fail(ReturnCode.RC016.getCode(), "导出json文件解析错误：" + (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
        }
    }

    @Override
    public boolean ifAnyConsumerGroupOnlineInTopic(String topicName) {
        List<GroupsInfoForTopicResponse> groupsInfoForTopic = null;
        try {
            groupsInfoForTopic = consumerGroupsByTopic(topicName).getData();
        } catch (Exception e) {
            log.error("exception: ", e);
        }
        if (groupsInfoForTopic != null) {
            for (GroupsInfoForTopicResponse groupInfo : groupsInfoForTopic) {
                if (groupInfo.getState().equals(ConsumerGroupState.STABLE.name())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean ifAnyProducerOnlineInTopic(String topicName) {
        long offsetBefore = 0L, offsetAfter = 0L;
        try {
            for (TopicDescribeResponse.Partitions partition : describeTopic(topicName).getData().getPartitions()) {
                offsetBefore += partition.getOffsetMax();
            }
            Thread.sleep(1000);
            for (TopicDescribeResponse.Partitions partition : describeTopic(topicName).getData().getPartitions()) {
                offsetAfter += partition.getOffsetMax();
            }
        } catch (Exception e) {
            log.error("exception: ", e);
        }
        return offsetBefore != offsetAfter;

//        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
//        DescribeTopicsResult topicsResult = adminClient.describeTopics(Collections.singletonList(topicName));
//        Collection<TopicPartition> partitions = topicsResult.allTopicNames().get().get(topicName).partitions().stream().map(x -> new TopicPartition(topicName, x.partition())).collect(Collectors.toSet());
//        DescribeProducersResult producersResult = adminClient.describeProducers(partitions);
//        log.info("The Producers' State：{}", JSON.toJSONString(producersResult.all().get()));
//        for (DescribeProducersResult.PartitionProducerState producerState : producersResult.all().get().values()) {
//            if (!producerState.activeProducers().isEmpty()) {
//                return true;
//            }
//        }
    }

}
