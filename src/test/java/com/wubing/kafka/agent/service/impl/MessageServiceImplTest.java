//package com.cecloud.paas.agent.service.impl;
//
//import com.cecloud.paas.agent.config.KafkaClientPool;
//import com.cecloud.paas.agent.domain.response.ConsumerMessageResponse;
//import com.cecloud.paas.agent.utils.ResultData;
//import com.cecloud.paas.agent.utils.ReturnCode;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
//import org.apache.kafka.common.TopicPartition;
//import org.assertj.core.util.Lists;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import java.time.Duration;
//import java.util.*;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(KafkaClientPool.class)
//@PowerMockIgnore({"javax.management.*"})
//public class MessageServiceImplTest {
//
//    @InjectMocks
//    MessageServiceImpl messageService;
//
//    @Before
//    public void setUp(){
//        PowerMockito.mockStatic(KafkaClientPool.class);
//    }
//
//    @Test
//    public void listMessageTest(){
//        String topic = "topic1";
//        int partition = 0;
//        boolean timeQuery = true;
//        boolean offsetQuery = false;
//
//        long timestamp = 0;
//        long offset = 0;
//        long limit = 1;
//        long endOffset = 1;
//
//        KafkaConsumer<String, String> kafkaConsumer = Mockito.mock(KafkaConsumer.class);
//        PowerMockito.when(KafkaClientPool.createConsumer()).thenReturn(kafkaConsumer);
//
//        TopicPartition topicPartition = new TopicPartition(topic, partition);
//        Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
//        timestampsToSearch.put(new TopicPartition(topic, partition), timestamp);
//
//        Map<TopicPartition, OffsetAndTimestamp> offsetAndTimestampMap = new HashMap<>();
//        OffsetAndTimestamp offsetAndTimestamp = new OffsetAndTimestamp(offset, timestamp);
//        offsetAndTimestampMap.put(topicPartition, offsetAndTimestamp);
//        PowerMockito.when(kafkaConsumer.offsetsForTimes(timestampsToSearch)).thenReturn(offsetAndTimestampMap);
//
//        Map<TopicPartition, Long> topicPartitionLongMap = new HashMap<>();
//        topicPartitionLongMap.put(topicPartition, offset);
//
//        PowerMockito.when(kafkaConsumer.endOffsets(Collections.singleton(topicPartition))).thenReturn(topicPartitionLongMap);
//
//        // message records is less than 1
//        ResultData<ConsumerMessageResponse> successTime = messageService.listMessages(topic, partition, timeQuery, timestamp,0, offset, limit);
//        Assert.assertEquals(ReturnCode.RC200.getCode(), successTime.getCode());
//
//        // message records is large than 1
//        Map<TopicPartition, Long> topicPartitionLongMap1 = new HashMap<>();
//        topicPartitionLongMap1.put(topicPartition, endOffset);
//
//        PowerMockito.when(kafkaConsumer.endOffsets(Collections.singleton(topicPartition))).thenReturn(topicPartitionLongMap1);
//
//        Map<TopicPartition, List<ConsumerRecord<String, String>>> records  = new HashMap<>();
//        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<String,String>(topic, partition, offset, "1", "1");
//        records.put(topicPartition, Lists.newArrayList(consumerRecord));
//        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(records);
//        PowerMockito.when(kafkaConsumer.poll(Duration.ofMillis(100))).thenReturn(consumerRecords);
//
//        ResultData<ConsumerMessageResponse> successTime1 = messageService.listMessages(topic, partition, timeQuery, timestamp,0, offset, limit);
//        Assert.assertEquals(ReturnCode.RC200.getCode(), successTime1.getCode());
//
//
//        ResultData<ConsumerMessageResponse> successOffset = messageService.listMessages(topic, partition, offsetQuery, timestamp,0, offset, limit);
//        Assert.assertEquals(ReturnCode.RC200.getCode(), successOffset.getCode());
//    }
//}
