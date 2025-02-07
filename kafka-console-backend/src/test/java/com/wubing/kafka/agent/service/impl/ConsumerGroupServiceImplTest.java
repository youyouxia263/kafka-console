package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.response.ConsumerGroupDesResponse;
import com.wubing.kafka.agent.domain.response.ConsumerGroupListResponse;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import org.apache.kafka.clients.admin.*;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaClientPool.class)
@PowerMockIgnore({"javax.management.*"})
public class ConsumerGroupServiceImplTest {

    @InjectMocks
    ConsumerGroupServiceImpl consumerGroupService;

    @Before
    public void setUp(){
        PowerMockito.mockStatic(KafkaClientPool.class);
    }

    @Test
    public void deleteConsumerGroupFailedTest(){
        String groupId = "testGroupId";
        AdminClient adminClient = Mockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<String> test = consumerGroupService.deleteConsumerGroup(groupId);
        Assert.assertEquals(test.getCode(), ReturnCode.RC101.getCode());

    }

    @Test
    public void deleteConsumerGroupSuccessTest() throws ExecutionException, InterruptedException {
        String groupId = "testGroupId";
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);

        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DeleteConsumerGroupsResult deleteConsumerGroupsResult = PowerMockito.mock(DeleteConsumerGroupsResult.class);
        KafkaFuture<Void> deleteConsumer =PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(adminClient.deleteConsumerGroups(any())).thenReturn(deleteConsumerGroupsResult);
        PowerMockito.when(deleteConsumerGroupsResult.all()).thenReturn(deleteConsumer);
        PowerMockito.when(deleteConsumer.get()).thenReturn(null);
        ResultData<String> success = consumerGroupService.deleteConsumerGroup(groupId);
        Assert.assertEquals(ReturnCode.RC101.getCode(), success.getCode());

    }

    @Test
    public void describeConsumerGroupFailedTest(){
        String groupId = "testGroupId";
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<ConsumerGroupDesResponse> test = consumerGroupService.describeConsumerGroup(groupId);
        Assert.assertEquals(test.getCode(), ReturnCode.RC101.getCode());
    }

    @Test
    public void describeConsumerGroupSuccessTest() throws ExecutionException, InterruptedException {
        String groupId = "testGroupId";
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DescribeConsumerGroupsResult describeConsumerGroupsResult = PowerMockito.mock(DescribeConsumerGroupsResult.class);
        PowerMockito.when(adminClient.describeConsumerGroups(any())).thenReturn(describeConsumerGroupsResult);

        // mock ConsumerGroupDescription
        Map describeGroups = PowerMockito.mock(Map.class);
        PowerMockito.when(describeConsumerGroupsResult.describedGroups()).thenReturn(describeGroups);
        KafkaFuture<ConsumerGroupDescription> consumerGroupDescriptionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeGroups.get(groupId)).thenReturn(consumerGroupDescriptionKafkaFuture);
        TopicPartition topicPartition = new TopicPartition("topic0", 0);
        MemberAssignment memberAssignment = new MemberAssignment(Collections.singleton(topicPartition));
        MemberDescription memberDescription = new MemberDescription("memId", "clientId", "localhost", memberAssignment);
        ConsumerGroupDescription consumerGroupDescription = new ConsumerGroupDescription(groupId, false,
                Collections.singletonList(memberDescription) , "0", ConsumerGroupState.STABLE, Node.noNode());
        PowerMockito.when(consumerGroupDescriptionKafkaFuture.get()).thenReturn(consumerGroupDescription);

        ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = PowerMockito.mock(ListConsumerGroupOffsetsResult.class);
        PowerMockito.when(adminClient.listConsumerGroupOffsets(groupId)).thenReturn(listConsumerGroupOffsetsResult);

        KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> mapKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata()).thenReturn(mapKafkaFuture);


        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = new HashMap<>();
        OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(0);
        topicPartitionOffsetAndMetadataMap.put(topicPartition, offsetAndMetadata);
        PowerMockito.when(mapKafkaFuture.get()).thenReturn(topicPartitionOffsetAndMetadataMap);

        ListOffsetsResult listOffsetsResult = PowerMockito.mock(ListOffsetsResult.class);
        PowerMockito.when(adminClient.listOffsets(any())).thenReturn(listOffsetsResult);

        KafkaFuture<Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo>> listOffsetsFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listOffsetsResult.all()).thenReturn(listOffsetsFuture);
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> topicPartitionListOffsetsResultInfoMap = new HashMap<>();
        ListOffsetsResult.ListOffsetsResultInfo listOffsetsResultInfo = new ListOffsetsResult.ListOffsetsResultInfo(0, 0, Optional.of(0));
        topicPartitionListOffsetsResultInfoMap.put(topicPartition, listOffsetsResultInfo);
        PowerMockito.when(listOffsetsFuture.get()).thenReturn(topicPartitionListOffsetsResultInfoMap);

        ResultData<ConsumerGroupDesResponse> test = consumerGroupService.describeConsumerGroup(groupId);

        ConsumerGroupDescription consumerGroupDescription1 = new ConsumerGroupDescription(groupId, false,
                new ArrayList<>(), "0", ConsumerGroupState.STABLE, Node.noNode());
        PowerMockito.when(consumerGroupDescriptionKafkaFuture.get()).thenReturn(consumerGroupDescription1);
        ResultData<ConsumerGroupDesResponse> test1 = consumerGroupService.describeConsumerGroup(groupId);
        Assert.assertEquals(test1.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void listConsumerGroupFailedTest(){
        String groupId = "testGroupId";
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<ConsumerGroupListResponse> test = consumerGroupService.listConsumerGroup(1,1);
        Assert.assertEquals(test.getCode(), ReturnCode.RC102.getCode());
    }

    @Test
    public void listConsumerGroupSuccessTest() throws ExecutionException, InterruptedException, TimeoutException {
        String groupId = "testGroupId";
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ListConsumerGroupsResult listConsumerGroupsResult = PowerMockito.mock(ListConsumerGroupsResult.class);

        PowerMockito.when(adminClient.listConsumerGroups()).thenReturn(listConsumerGroupsResult);

        KafkaFuture<Collection<ConsumerGroupListing>> collectionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listConsumerGroupsResult.valid()).thenReturn(collectionKafkaFuture);

        // mock ConsumerGroupDescription
        DescribeConsumerGroupsResult describeConsumerGroupsResult = PowerMockito.mock(DescribeConsumerGroupsResult.class);
        PowerMockito.when(adminClient.describeConsumerGroups(any())).thenReturn(describeConsumerGroupsResult);
        KafkaFuture<Map<String, ConsumerGroupDescription>> kafkaFutureConsumer = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeConsumerGroupsResult.all()).thenReturn(kafkaFutureConsumer);

        TopicPartition topicPartition = new TopicPartition("topic0", 0);
        MemberAssignment memberAssignment = new MemberAssignment(Collections.singleton(topicPartition));
        MemberDescription memberDescription = new MemberDescription("memId", "clientId", "localhost", memberAssignment);
        ConsumerGroupDescription consumerGroupDescription = new ConsumerGroupDescription(groupId, false,
                Collections.singletonList(memberDescription) , "0", ConsumerGroupState.STABLE, Node.noNode());
        Map<String, ConsumerGroupDescription> consumerGroupDescriptionMap = new HashMap<>();
        consumerGroupDescriptionMap.put(groupId, consumerGroupDescription);
        PowerMockito.when(kafkaFutureConsumer.get()).thenReturn(consumerGroupDescriptionMap);

        Collection<ConsumerGroupListing> consumerGroupListings = new ArrayList<>();
        PowerMockito.when(collectionKafkaFuture.get(10, TimeUnit.SECONDS)).thenReturn(consumerGroupListings);
        ResultData<ConsumerGroupListResponse> test = consumerGroupService.listConsumerGroup(1,1);
        Assert.assertEquals(test.getCode(), ReturnCode.RC200.getCode());

        consumerGroupDescriptionMap.put("2", consumerGroupDescription);
        ConsumerGroupListing consumerGroupListing1 = new ConsumerGroupListing(groupId, true, Optional.of(ConsumerGroupState.STABLE));
        ConsumerGroupListing consumerGroupListing2 = new ConsumerGroupListing("2", true, Optional.of(ConsumerGroupState.STABLE));
        consumerGroupListings.add(consumerGroupListing1);
        consumerGroupListings.add(consumerGroupListing2);
        PowerMockito.when(collectionKafkaFuture.get(10, TimeUnit.SECONDS)).thenReturn(consumerGroupListings);

        //mock listConsumerGroupOffsets
        PowerMockito.when(KafkaClientPool.createAdminClient()).thenReturn(adminClient);
        ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = PowerMockito.mock(ListConsumerGroupOffsetsResult.class);
        PowerMockito.when(adminClient.listConsumerGroupOffsets(anyString())).thenReturn(listConsumerGroupOffsetsResult);
        KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> kafkaFutureOffset = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata()).thenReturn(kafkaFutureOffset);
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = new HashMap<>();
        topicPartitionOffsetAndMetadataMap.put(topicPartition, new OffsetAndMetadata(0, "localhost"));
        PowerMockito.when(kafkaFutureOffset.get()).thenReturn(topicPartitionOffsetAndMetadataMap);

        // mock listOffsets
        ListOffsetsResult listOffsetsResult = PowerMockito.mock(ListOffsetsResult.class);
        PowerMockito.when(adminClient.listOffsets(any())).thenReturn(listOffsetsResult);

        KafkaFuture<Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo>> listOffsetsFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listOffsetsResult.all()).thenReturn(listOffsetsFuture);
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> topicPartitionListOffsetsResultInfoMap = new HashMap<>();
        ListOffsetsResult.ListOffsetsResultInfo listOffsetsResultInfo = new ListOffsetsResult.ListOffsetsResultInfo(0, 0, Optional.of(0));
        topicPartitionListOffsetsResultInfoMap.put(topicPartition, listOffsetsResultInfo);
        PowerMockito.when(listOffsetsFuture.get()).thenReturn(topicPartitionListOffsetsResultInfoMap);

        ResultData<ConsumerGroupListResponse> test1 = consumerGroupService.listConsumerGroup(1,1);
        Assert.assertEquals(test1.getCode(), ReturnCode.RC200.getCode());
    }
}
