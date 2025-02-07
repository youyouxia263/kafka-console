package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.constants.ResponeMsg;
import com.wubing.kafka.agent.domain.entity.ConsumerMessage;
import com.wubing.kafka.agent.domain.request.SendMessageReq;
import com.wubing.kafka.agent.domain.response.ConsumerMessageResponse;
import com.wubing.kafka.agent.service.MessageService;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Override
    public ResultData<ConsumerMessageResponse> listMessagesByOffset(String topicName, int partition, long offset, int pageNo, int pageSize) {
        //按照位点查询
        Pair<Long, List<ConsumerMessage>> pair = listHistoryMessagesWithOffset(topicName, partition, offset, pageNo, pageSize);
        return ResultData.success(new ConsumerMessageResponse(pair.getKey(), pageNo, pageSize, pair.getValue()));
    }

    @Override
    public ResultData<ConsumerMessageResponse> listMessagesByTime(String topicName, int partition, long beginTime, long endTime, int pageNo, int pageSize) {
        // 按照时间查询
        Pair<Long, List<ConsumerMessage>> pair = listHistoryMessagesWithTimeRange(topicName, partition, beginTime, endTime, pageNo, pageSize);
        return ResultData.success(new ConsumerMessageResponse(pair.getKey(), pageNo, pageSize, pair.getValue()));
    }

    @Override
    public ResultData<String> sendMessage(SendMessageReq sendMessageReq) {
        try (Producer<String, String> producer = KafkaClientPool.createProducer()) {
            producer.send(new ProducerRecord<>(sendMessageReq.getTopicName(), sendMessageReq.getPartition(), sendMessageReq.getKey(), sendMessageReq.getContent())).get();
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC013.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.SEND_MESSAGE_SUCCESS);
    }

    private Pair<Long, List<ConsumerMessage>> listHistoryMessagesWithTimeRange(String topicName, int partition, long beginTime, long endTime, int pageNo, int pageSize) {
        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClientPool.createConsumer()) {
            // 如果partition 是 -1 获取全部partition的数据
            if (partition == -1) {
                AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
                KafkaFuture<TopicDescription> topicDescriptionFuture = adminClient.describeTopics(Lists.newArrayList(topicName)).values().get(topicName);
                List<TopicPartitionInfo> partitions = topicDescriptionFuture.get().partitions();
                List<ConsumerMessage> consumerMessageList = new ArrayList<>();
                long totalCount = 0;
                int fixLimit = 20;
                for (TopicPartitionInfo partitionInfo : partitions) {
                    Pair<Long, List<ConsumerMessage>> messagePair = getConsumerMessagePairPage(topicName, partitionInfo.partition(), beginTime, endTime, 1, fixLimit, kafkaConsumer);
                    totalCount += messagePair.getKey();
                    consumerMessageList.addAll(messagePair.getValue());
                }
                List<ConsumerMessage> collect = consumerMessageList.stream().sorted(Comparator.comparingLong(ConsumerMessage::getTimestamp)).limit(fixLimit).collect(Collectors.toList());
                return Pair.of(totalCount, collect);
            }
            return getConsumerMessagePairPage(topicName, partition, beginTime, endTime, pageNo, pageSize, kafkaConsumer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Pair.of(0L, new ArrayList<>());
        }
    }

    private Pair<Long, List<ConsumerMessage>> getConsumerMessagePairPage(String topicName, int partition, long beginTime, long endTime, int pageNo, int pageSize, KafkaConsumer<String, String> kafkaConsumer) {
        TopicPartition topicPartition = new TopicPartition(topicName, partition);
        Map<TopicPartition, Long> beginTimeToSearch = new HashMap<>();
        beginTimeToSearch.put(topicPartition, beginTime);
        OffsetAndTimestamp offsetBeginTime = kafkaConsumer.offsetsForTimes(beginTimeToSearch).get(topicPartition);
        if (offsetBeginTime == null) {
            log.info("OffsetAndTimestamp is null, there is no message in the timestamp: {}", beginTime);
            // 返回空消息记录
            return Pair.of(0L, new ArrayList<>());
        }
        long startOffset = offsetBeginTime.offset();
        Map<TopicPartition, Long> endTimeToSearch = new HashMap<>();
        endTimeToSearch.put(topicPartition, endTime);
        OffsetAndTimestamp offsetEndTime = kafkaConsumer.offsetsForTimes(endTimeToSearch).get(topicPartition);
        long limit = pageSize;
        long endOffset = 0L;
        if (offsetEndTime != null) {
            endOffset = offsetEndTime.offset();
        }
        if(endOffset == 0L){
            //如果结束位点为零，使用最大位点
            endOffset = kafkaConsumer.endOffsets(Collections.singleton(topicPartition)).get(topicPartition);
        }
        log.info("-----jmc---test xxx beginTime: {}, endTime: {} , startOffset: {} , endOffset: {}", beginTime, endTime, startOffset, endOffset);
        long totalCount = endOffset - startOffset;
        long realOffset = startOffset + (long) (pageNo - 1) * pageSize;
        long realCount = endOffset - realOffset;
        if (limit > realCount) {
            limit = realCount;
        }
        if (limit < 1) {
            // 返回空消息记录
            return Pair.of(0L, new ArrayList<>());
        }
        List<ConsumerMessage> consumerMessages = getConsumerMessageFromOffset(topicName, partition, realOffset, limit, kafkaConsumer, topicPartition);
        return Pair.of(totalCount, consumerMessages);
    }


    private Pair<Long, List<ConsumerMessage>> listHistoryMessagesWithOffset(String topicName, int partition, long offset, int pageNo, int pageSize) {
        if (partition == -1) {
            return Pair.of(0L, new ArrayList<>());//基于点位查询，不支持选择all的情况
        }
        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClientPool.createConsumer()) {
            TopicPartition topicPartition = new TopicPartition(topicName, partition);
            long maxOffset = kafkaConsumer.endOffsets(Collections.singleton(topicPartition)).get(topicPartition);
            long minOffset = kafkaConsumer.beginningOffsets(Collections.singleton(topicPartition)).get(topicPartition);
            long effectiveOffset = Math.max(minOffset, offset);
            log.info("------jmc ---test effectiveOffset: {}", effectiveOffset);

            if (maxOffset < effectiveOffset) {
                log.info("OffsetAndTimestamp is null, there is no message in the offset: {}", effectiveOffset);
                // 返回空消息记录
                return Pair.of(0L, new ArrayList<>());
            }
            long totalCount = maxOffset - effectiveOffset;
            long realOffset = effectiveOffset + (long) (pageNo - 1) * pageSize;
            long overCount = maxOffset - realOffset;
            long limit = pageSize < overCount ? pageSize : overCount;//取小的
            log.info("---------- jmc test offset: {}, limit: {}", realOffset, limit);
            List<ConsumerMessage> consumerMessageList = getConsumerMessageFromOffset(topicName, partition, realOffset, limit, kafkaConsumer, topicPartition);
            return Pair.of(totalCount, consumerMessageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private List<ConsumerMessage> getConsumerMessageFromOffset(String topicName, int partition, long offset, long limit, KafkaConsumer<String, String> kafkaConsumer, TopicPartition topicPartition) {
        kafkaConsumer.assign(Collections.singleton(topicPartition));
        kafkaConsumer.seek(topicPartition, offset);
        List<ConsumerRecord<String, String>> getRecordList = new ArrayList<>();
        long requestBeginTime = System.currentTimeMillis();
        while (true) {
            List<ConsumerRecord<String, String>> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100)).records(topicPartition);
            if (!consumerRecords.isEmpty()) {
                getRecordList.addAll(consumerRecords);
            }
            long requestEndTime = System.currentTimeMillis();
            //循环获取数据两个退出点，1、数据达到数量 2、请求时间超过5秒
            if (getRecordList.size() >= limit || requestEndTime - requestBeginTime > 5000) {
                break;
            }
        }
        return convertConsumerRecords2ConsumerMessages(topicName, partition, limit, getRecordList);
    }

    // 将ConsumerRecords转为ConsumerMessageResponse list
    private List<ConsumerMessage> convertConsumerRecords2ConsumerMessages(String topicName, int partition, long limit, List<ConsumerRecord<String, String>> consumerRecords) {
        if (consumerRecords.size() > limit) {
            consumerRecords = consumerRecords.subList(0, (int) limit);
        }
        List<ConsumerMessage> consumerMessageList = new ArrayList<>();
        consumerRecords.forEach(x -> {
            ConsumerMessage consumerMessage = new ConsumerMessage();
            consumerMessage.setTopicName(topicName);
            consumerMessage.setPartition(partition);
            consumerMessage.setKey(x.key());
            consumerMessage.setValue(x.value());
            consumerMessage.setOffset(x.offset());
            consumerMessage.setTimestamp(x.timestamp());
            consumerMessageList.add(consumerMessage);

        });
        return consumerMessageList;
    }
}
