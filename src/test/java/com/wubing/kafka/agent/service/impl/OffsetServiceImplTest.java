package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.request.OffsetResetReq;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaClientPool.class)
@PowerMockIgnore({"javax.management.*"})
public class OffsetServiceImplTest {
    @InjectMocks
    OffsetServiceImpl offsetService;

    OffsetResetReq offsetResetReq = new OffsetResetReq();

    @Before
    public void setUp(){
        PowerMockito.mockStatic(KafkaClientPool.class);
        offsetResetReq.setTopicName("topic1");
        offsetResetReq.setPartition(0);
        offsetResetReq.setGroupId("1");
        offsetResetReq.setResetOffset(0);
        offsetResetReq.setResetTime(0);
        offsetResetReq.setResetType(0);
    }

    @Test
    public void resetOffsetEmptyConsumerTest() throws ExecutionException, InterruptedException {
        AdminClient adminClient = Mockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DescribeConsumerGroupsResult describeConsumerGroupsResult = PowerMockito.mock(DescribeConsumerGroupsResult.class);
        PowerMockito.when(adminClient.describeConsumerGroups(any())).thenReturn(describeConsumerGroupsResult);
        // mock ConsumerGroupDescription
        Map describeGroups = PowerMockito.mock(Map.class);
        PowerMockito.when(describeConsumerGroupsResult.describedGroups()).thenReturn(describeGroups);
        KafkaFuture<ConsumerGroupDescription> consumerGroupDescriptionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeGroups.get(offsetResetReq.getGroupId())).thenReturn(consumerGroupDescriptionKafkaFuture);
        TopicPartition topicPartition = new TopicPartition(offsetResetReq.getTopicName(), offsetResetReq.getPartition());
        MemberAssignment memberAssignment = new MemberAssignment(Collections.singleton(topicPartition));
        MemberDescription memberDescription = new MemberDescription("memId", "clientId", "localhost", memberAssignment);
        ConsumerGroupDescription consumerGroupDescription = new ConsumerGroupDescription(offsetResetReq.getGroupId(), false,
                Collections.singletonList(memberDescription) , "0", ConsumerGroupState.STABLE, Node.noNode());
        PowerMockito.when(consumerGroupDescriptionKafkaFuture.get()).thenReturn(consumerGroupDescription);

        ResultData<String> test1 = offsetService.resetOffset(offsetResetReq);
        Assert.assertEquals(test1.getCode(), ReturnCode.RC502.getCode());
    }

    @Test
    public void noResetOffsetTest() throws ExecutionException, InterruptedException {
        AdminClient adminClient = Mockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DescribeConsumerGroupsResult describeConsumerGroupsResult = PowerMockito.mock(DescribeConsumerGroupsResult.class);
        PowerMockito.when(adminClient.describeConsumerGroups(any())).thenReturn(describeConsumerGroupsResult);

        offsetResetReq.setResetType(4);
        // mock ConsumerGroupDescription
        Map describeGroups = PowerMockito.mock(Map.class);
        PowerMockito.when(describeConsumerGroupsResult.describedGroups()).thenReturn(describeGroups);
        KafkaFuture<ConsumerGroupDescription> consumerGroupDescriptionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeGroups.get(offsetResetReq.getGroupId())).thenReturn(consumerGroupDescriptionKafkaFuture);
        TopicPartition topicPartition = new TopicPartition(offsetResetReq.getTopicName(), offsetResetReq.getPartition());
        MemberAssignment memberAssignment = new MemberAssignment(Collections.singleton(topicPartition));
        MemberDescription memberDescription = new MemberDescription("memId", "clientId", "localhost", memberAssignment);
        ConsumerGroupDescription consumerGroupDescription = new ConsumerGroupDescription(offsetResetReq.getGroupId(), false,
                Collections.singletonList(memberDescription) , "0", ConsumerGroupState.EMPTY, Node.noNode());
        PowerMockito.when(consumerGroupDescriptionKafkaFuture.get()).thenReturn(consumerGroupDescription);

        ResultData<String> test = offsetService.resetOffset(offsetResetReq);
        Assert.assertEquals(test.getCode(), ReturnCode.RC501.getCode());
    }

    @Test
    public void resetOffsetTest() throws ExecutionException, InterruptedException {
        AdminClient adminClient = Mockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DescribeConsumerGroupsResult describeConsumerGroupsResult = PowerMockito.mock(DescribeConsumerGroupsResult.class);
        PowerMockito.when(adminClient.describeConsumerGroups(any())).thenReturn(describeConsumerGroupsResult);
        // mock ConsumerGroupDescription
        Map describeGroups = PowerMockito.mock(Map.class);
        PowerMockito.when(describeConsumerGroupsResult.describedGroups()).thenReturn(describeGroups);
        KafkaFuture<ConsumerGroupDescription> consumerGroupDescriptionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeGroups.get(offsetResetReq.getGroupId())).thenReturn(consumerGroupDescriptionKafkaFuture);
        TopicPartition topicPartition = new TopicPartition(offsetResetReq.getTopicName(), offsetResetReq.getPartition());
        MemberAssignment memberAssignment = new MemberAssignment(Collections.singleton(topicPartition));
        MemberDescription memberDescription = new MemberDescription("memId", "clientId", "localhost", memberAssignment);
        ConsumerGroupDescription consumerGroupDescription = new ConsumerGroupDescription(offsetResetReq.getGroupId(), false,
                Collections.singletonList(memberDescription) , "0", ConsumerGroupState.EMPTY, Node.noNode());
        PowerMockito.when(consumerGroupDescriptionKafkaFuture.get()).thenReturn(consumerGroupDescription);

        KafkaConsumer<String, String> kafkaConsumer = Mockito.mock(KafkaConsumer.class);
        PowerMockito.when(KafkaClientPool.createConsumerByGroupId(offsetResetReq.getGroupId())).thenReturn(kafkaConsumer);

        Map<TopicPartition, Long> topicPartitionLongMap1 = new HashMap<>();
        topicPartitionLongMap1.put(topicPartition, 1L);

        PowerMockito.when(kafkaConsumer.endOffsets(any())).thenReturn(topicPartitionLongMap1);

        PowerMockito.when(kafkaConsumer.beginningOffsets(any())).thenReturn(topicPartitionLongMap1);
        ResultData<String> test = offsetService.resetOffset(offsetResetReq);
        Assert.assertEquals(test.getCode(), ReturnCode.RC501.getCode());

        // reset by timestamp
        offsetResetReq.setResetType(1);
        ResultData<String> test1 = offsetService.resetOffset(offsetResetReq);
        Assert.assertEquals(test1.getCode(), ReturnCode.RC200.getCode());

        Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
        timestampsToSearch.put(topicPartition, offsetResetReq.getResetTime());
        Map<TopicPartition, OffsetAndTimestamp> offsetAndTimestampMap = new HashMap<>();
        OffsetAndTimestamp offsetAndTimestamp = new OffsetAndTimestamp(offsetResetReq.getResetOffset(), offsetResetReq.getResetTime());
        offsetAndTimestampMap.put(topicPartition, offsetAndTimestamp);
        PowerMockito.when(kafkaConsumer.offsetsForTimes(timestampsToSearch)).thenReturn(offsetAndTimestampMap);

        offsetResetReq.setResetType(1);
        ResultData<String> test2 = offsetService.resetOffset(offsetResetReq);
        Assert.assertEquals(test2.getCode(), ReturnCode.RC200.getCode());

        // reset by early offset
        offsetResetReq.setResetType(2);
        PowerMockito.when(kafkaConsumer.beginningOffsets(Collections.singleton(topicPartition))).thenReturn(timestampsToSearch);
        ResultData<String> test3 = offsetService.resetOffset(offsetResetReq);
        Assert.assertEquals(test3.getCode(), ReturnCode.RC200.getCode());

        // reset by latest offset
        offsetResetReq.setResetType(3);
        PowerMockito.when(kafkaConsumer.endOffsets(Collections.singleton(topicPartition))).thenReturn(timestampsToSearch);
        ResultData<String> test4 = offsetService.resetOffset(offsetResetReq);
        Assert.assertEquals(test4.getCode(), ReturnCode.RC200.getCode());
    }
}
