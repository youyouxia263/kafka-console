package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.entity.BatchTopicsBean;
import com.wubing.kafka.agent.domain.entity.TopicBeanForImportAndExport;
import com.wubing.kafka.agent.domain.request.TopicConfigReq;
import com.wubing.kafka.agent.domain.request.TopicPropertyReq;
import com.wubing.kafka.agent.domain.response.GroupsInfoForTopicResponse;
import com.wubing.kafka.agent.domain.response.TopicDescribeResponse;
import com.wubing.kafka.agent.domain.response.TopicListResponse;
import com.wubing.kafka.agent.domain.response.TopicPropertyResponse;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.*;
import org.apache.kafka.common.config.ConfigResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaClientPool.class)
@PowerMockIgnore({"javax.management.*"})
public class TopicOpServiceImplTest {
    @InjectMocks
    TopicOpServiceImpl topicOpService;

    TopicConfigReq topicConfigReq = new TopicConfigReq();

    @Before
    public void setUp(){
        PowerMockito.mockStatic(KafkaClientPool.class);
        topicConfigReq.setTopicName("topic1");
        topicConfigReq.setPartitions(1);
        topicConfigReq.setReplicationFactor(1);
    }

    @Test
    public void createTopicFailedTest(){
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<String> test = topicOpService.createTopic(topicConfigReq);
        Assert.assertEquals(test.getCode(), ReturnCode.RC001.getCode());
    }

    @Test
    public void createTopicTest() throws ExecutionException, InterruptedException {
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ListTopicsResult listTopicsResult = PowerMockito.mock(ListTopicsResult.class);
        PowerMockito.when(adminClient.listTopics()).thenReturn(listTopicsResult);
        KafkaFuture<Set<String>> kafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listTopicsResult.names()).thenReturn(kafkaFuture);
        PowerMockito.when(kafkaFuture.get()).thenReturn(new HashSet<>());

        NewTopic newTopic = new NewTopic(topicConfigReq.getTopicName(), topicConfigReq.getPartitions(),
                (short) topicConfigReq.getReplicationFactor()).configs(topicConfigReq.getConfigs());
        Collection<NewTopic> newTopicList = new ArrayList<>();
        newTopicList.add(newTopic);
        CreateTopicsResult createTopicsResult = PowerMockito.mock(CreateTopicsResult.class);
        PowerMockito.when(adminClient.createTopics(newTopicList)).thenReturn(createTopicsResult);
        KafkaFuture<Void> kafkaFuture1 = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(createTopicsResult.all()).thenReturn(kafkaFuture1);
        ResultData<String> test = topicOpService.createTopic(topicConfigReq);
        Assert.assertEquals(test.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void deleteTopicFailedTest(){
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<String> test = topicOpService.deleteTopic(topicConfigReq.getTopicName());
        Assert.assertEquals(test.getCode(), ReturnCode.RC002.getCode());
    }

    @Test
    public void deleteTopicTest(){
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DeleteTopicsResult deleteTopics = PowerMockito.mock(DeleteTopicsResult.class);
        PowerMockito.when(adminClient.deleteTopics(Collections.singletonList(topicConfigReq.getTopicName()))).thenReturn(deleteTopics);
        KafkaFuture<Void> kafkaFuture1 = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(deleteTopics.all()).thenReturn(kafkaFuture1);
        ResultData<String> test = topicOpService.deleteTopic(topicConfigReq.getTopicName());
        Assert.assertEquals(test.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void describeTopicFailedTest(){
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<TopicDescribeResponse> test = topicOpService.describeTopic(topicConfigReq.getTopicName());
        Assert.assertEquals(test.getCode(), ReturnCode.RC005.getCode());
    }

    @Test
    public void describeTopicTest() throws ExecutionException, InterruptedException, TimeoutException {
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DescribeTopicsResult describeTopicsResult = PowerMockito.mock(DescribeTopicsResult.class);
        PowerMockito.when(adminClient.describeTopics(Collections.singletonList(topicConfigReq.getTopicName()))).thenReturn(describeTopicsResult);
        KafkaFuture<Map<String, TopicDescription>> topicDescriptionFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeTopicsResult.all()).thenReturn(topicDescriptionFuture);

        Map<String, TopicDescription> topicDescriptionMap = new HashMap<>();
        List<TopicPartitionInfo> partitions = new ArrayList<>();
        TopicPartitionInfo topicPartitionInfo = new TopicPartitionInfo(topicConfigReq.getPartitions(), Node.noNode(), Collections.singletonList(Node.noNode()), Collections.singletonList(Node.noNode()));
        partitions.add(topicPartitionInfo);
        TopicDescription topicDescription = new TopicDescription(topicConfigReq.getTopicName(),false, partitions);
        topicDescriptionMap.put(topicConfigReq.getTopicName(), topicDescription);
        PowerMockito.when(topicDescriptionFuture.get()).thenReturn(topicDescriptionMap);

        ListOffsetsResult listOffsets = PowerMockito.mock(ListOffsetsResult.class);
        PowerMockito.when(adminClient.listOffsets(any())).thenReturn(listOffsets);
        KafkaFuture<Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo>> listOffsetsKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listOffsets.all()).thenReturn(listOffsetsKafkaFuture);
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> listOffsetsResultInfoMap = new HashMap<>();
        ListOffsetsResult.ListOffsetsResultInfo listOffsetsResultInfo = new ListOffsetsResult.ListOffsetsResultInfo(0, 0, Optional.of(0));
        TopicPartition topicPartition = new TopicPartition(topicConfigReq.getTopicName(), topicConfigReq.getPartitions());
        listOffsetsResultInfoMap.put(topicPartition, listOffsetsResultInfo);
        PowerMockito.when(listOffsetsKafkaFuture.get()).thenReturn(listOffsetsResultInfoMap);

        ListConsumerGroupsResult listConsumerGroupsResult = PowerMockito.mock(ListConsumerGroupsResult.class);
        PowerMockito.when(adminClient.listConsumerGroups()).thenReturn(listConsumerGroupsResult);

        KafkaFuture<Collection<ConsumerGroupListing>> collectionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listConsumerGroupsResult.valid()).thenReturn(collectionKafkaFuture);
        Collection<ConsumerGroupListing> consumerGroupListings = new ArrayList<>();
        PowerMockito.when(collectionKafkaFuture.get(10, TimeUnit.SECONDS)).thenReturn(consumerGroupListings);

        DescribeConsumerGroupsResult describeConsumerGroupsResult = PowerMockito.mock(DescribeConsumerGroupsResult.class);
        PowerMockito.when(adminClient.describeConsumerGroups(any())).thenReturn(describeConsumerGroupsResult);
        // mock ConsumerGroupDescription
        KafkaFuture<Map<String, ConsumerGroupDescription>> consumerGroupDescriptionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeConsumerGroupsResult.all()).thenReturn(consumerGroupDescriptionKafkaFuture);
        MemberAssignment memberAssignment = new MemberAssignment(Collections.singleton(topicPartition));
        MemberDescription memberDescription = new MemberDescription("memId", "clientId", "localhost", memberAssignment);
        ConsumerGroupDescription consumerGroupDescription = new ConsumerGroupDescription("groupId", false,
                Collections.singletonList(memberDescription) , "0", ConsumerGroupState.STABLE, Node.noNode());
        Map<String, ConsumerGroupDescription> consumerGroupDescriptionMap = new HashMap<>();
        consumerGroupDescriptionMap.put("groupId", consumerGroupDescription);
        PowerMockito.when(consumerGroupDescriptionKafkaFuture.get(10, TimeUnit.SECONDS)).thenReturn(consumerGroupDescriptionMap);

        // mock describeConfig
        ConfigResource topicConfigResource = new ConfigResource(ConfigResource.Type.TOPIC, topicConfigReq.getTopicName());
        DescribeConfigsResult describeConfigsResult = PowerMockito.mock(DescribeConfigsResult.class);
        PowerMockito.when(adminClient.describeConfigs(Collections.singletonList(topicConfigResource))).thenReturn(describeConfigsResult);


        KafkaFuture<Map<ConfigResource, Config>> configMap = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeConfigsResult.all()).thenReturn(configMap);
        Map<ConfigResource, Config> configResourceConfigMap = new HashMap<>();

        configResourceConfigMap.put(new ConfigResource(ConfigResource.Type.TOPIC, topicConfigReq.getTopicName()), new Config(Collections.singletonList( new ConfigEntry("test", "test"))));
        PowerMockito.when(configMap.get()).thenReturn(configResourceConfigMap);

        ResultData<TopicDescribeResponse> test = topicOpService.describeTopic(topicConfigReq.getTopicName());
        Assert.assertEquals(test.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void listTopicFailedTest(){
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<TopicListResponse> test = topicOpService.listTopic(1,1);
        Assert.assertEquals(test.getCode(), ReturnCode.RC004.getCode());
    }

    @Test
    public void listTopicTest() throws ExecutionException, InterruptedException {
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ListTopicsResult listTopicsResult = PowerMockito.mock(ListTopicsResult.class);
        ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
        listTopicsOptions.listInternal(true);
        PowerMockito.when(adminClient.listTopics(any())).thenReturn(listTopicsResult);

        KafkaFuture<Collection<TopicListing>> topicLists = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listTopicsResult.listings()).thenReturn(topicLists);
        TopicListing topicListing = new TopicListing(topicConfigReq.getTopicName(), false);
        PowerMockito.when(topicLists.get()).thenReturn(Collections.singletonList(topicListing));
        DescribeTopicsResult describeTopicsResult = PowerMockito.mock(DescribeTopicsResult.class);
        PowerMockito.when(adminClient.describeTopics(Collections.singletonList(topicConfigReq.getTopicName()))).thenReturn(describeTopicsResult);

        KafkaFuture<Map<String, TopicDescription>> mapKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeTopicsResult.all()).thenReturn(mapKafkaFuture);

        Map<String, TopicDescription> topicDescriptionMap = new HashMap<>();
        List<TopicPartitionInfo> partitions = new ArrayList<>();
        TopicPartitionInfo topicPartitionInfo = new TopicPartitionInfo(0, Node.noNode(), Collections.singletonList(Node.noNode()), Collections.singletonList(Node.noNode()));
        partitions.add(topicPartitionInfo);
        TopicDescription topicDescription = new TopicDescription(topicConfigReq.getTopicName(),false, partitions);
        topicDescriptionMap.put(topicConfigReq.getTopicName(), topicDescription);
        PowerMockito.when(mapKafkaFuture.get()).thenReturn(topicDescriptionMap);

        ResultData<TopicListResponse> test = topicOpService.listTopic(1,1);
        Assert.assertEquals(test.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void consumerGroupsByTopicFailedTest(){
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<List<GroupsInfoForTopicResponse>> test = topicOpService.consumerGroupsByTopic(topicConfigReq.getTopicName());
        Assert.assertEquals(test.getCode(), ReturnCode.RC010.getCode());
    }

    @Test
    public void consumerGroupsByTopicTest() throws ExecutionException, InterruptedException, TimeoutException {
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        TopicPartition topicPartition = new TopicPartition(topicConfigReq.getTopicName(), topicConfigReq.getPartitions());
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DescribeTopicsResult describeTopicsResult = PowerMockito.mock(DescribeTopicsResult.class);
        PowerMockito.when(adminClient.describeTopics(Collections.singletonList(topicConfigReq.getTopicName()))).thenReturn(describeTopicsResult);
        KafkaFuture<Map<String, TopicDescription>> topicDescriptionFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeTopicsResult.all()).thenReturn(topicDescriptionFuture);
        // mock topicDescribe
        Map<String, TopicDescription> topicDescriptionMap = new HashMap<>();
        List<TopicPartitionInfo> partitions = new ArrayList<>();
        TopicPartitionInfo topicPartitionInfo = new TopicPartitionInfo(topicConfigReq.getPartitions(), Node.noNode(), Collections.singletonList(Node.noNode()), Collections.singletonList(Node.noNode()));
        partitions.add(topicPartitionInfo);
        TopicDescription topicDescription = new TopicDescription(topicConfigReq.getTopicName(),false, partitions);
        topicDescriptionMap.put(topicConfigReq.getTopicName(), topicDescription);
        PowerMockito.when(topicDescriptionFuture.get()).thenReturn(topicDescriptionMap);
        // mock consumer group list
        ListConsumerGroupsResult listConsumerGroupsResult = PowerMockito.mock(ListConsumerGroupsResult.class);
        PowerMockito.when(adminClient.listConsumerGroups()).thenReturn(listConsumerGroupsResult);
        KafkaFuture<Collection<ConsumerGroupListing>> collectionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listConsumerGroupsResult.all()).thenReturn(collectionKafkaFuture);
        Collection<ConsumerGroupListing> consumerGroupListings = new ArrayList<>();
        ConsumerGroupListing consumerGroupListing1 = new ConsumerGroupListing("groupId", true, Optional.of(ConsumerGroupState.STABLE));
        consumerGroupListings.add(consumerGroupListing1);
        PowerMockito.when(collectionKafkaFuture.get()).thenReturn(consumerGroupListings);

        // mock ConsumerGroupDescription
        DescribeConsumerGroupsResult describeConsumerGroupsResult = PowerMockito.mock(DescribeConsumerGroupsResult.class);
        PowerMockito.when(adminClient.describeConsumerGroups(any())).thenReturn(describeConsumerGroupsResult);
        KafkaFuture<Map<String, ConsumerGroupDescription>> consumerGroupDescriptionKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeConsumerGroupsResult.all()).thenReturn(consumerGroupDescriptionKafkaFuture);
        MemberAssignment memberAssignment = new MemberAssignment(Collections.singleton(topicPartition));
        MemberDescription memberDescription = new MemberDescription("memId", "clientId", "localhost", memberAssignment);
        ConsumerGroupDescription consumerGroupDescription = new ConsumerGroupDescription("groupId", false,
                Collections.singletonList(memberDescription) , "0", ConsumerGroupState.STABLE, Node.noNode());
        Map<String, ConsumerGroupDescription> consumerGroupDescriptionMap = new HashMap<>();
        consumerGroupDescriptionMap.put("groupId", consumerGroupDescription);

        PowerMockito.when(consumerGroupDescriptionKafkaFuture.get()).thenReturn(consumerGroupDescriptionMap);
//        PowerMockito.when(consumerGroupDescriptionMap.get("groupId")).thenReturn(consumerGroupDescription);

        // mock consumer
        KafkaConsumer<String, String> kafkaConsumer = Mockito.mock(KafkaConsumer.class);
        PowerMockito.when(KafkaClientPool.createConsumer()).thenReturn(kafkaConsumer);
        Map<TopicPartition, Long> topicPartitionLongMap = new HashMap<>();
        topicPartitionLongMap.put(topicPartition, 0L);
        PowerMockito.when(kafkaConsumer.endOffsets(Collections.singleton(topicPartition))).thenReturn(topicPartitionLongMap);
        PowerMockito.when(kafkaConsumer.position(topicPartition)).thenReturn(0L);

        // mock listConsumerGroupOffsets
        ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = PowerMockito.mock(ListConsumerGroupOffsetsResult.class);
        PowerMockito.when(adminClient.listConsumerGroupOffsets("groupId")).thenReturn(listConsumerGroupOffsetsResult);
        KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> mapKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata()).thenReturn(mapKafkaFuture);
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = new HashMap<>();
        OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(0);
        topicPartitionOffsetAndMetadataMap.put(topicPartition, offsetAndMetadata);
        PowerMockito.when(mapKafkaFuture.get()).thenReturn(topicPartitionOffsetAndMetadataMap);

        ResultData<List<GroupsInfoForTopicResponse>> test = topicOpService.consumerGroupsByTopic(topicConfigReq.getTopicName());
        Assert.assertEquals(test.getCode(), ReturnCode.RC200.getCode());

    }


    @Test
    public void getTopicPropertyFailedTest(){
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<TopicPropertyResponse> test = topicOpService.getTopicProperty(topicConfigReq.getTopicName());
        Assert.assertEquals(test.getCode(), ReturnCode.RC006.getCode());
    }

    @Test
    public void getTopicPropertyTest() throws ExecutionException, InterruptedException {
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ListTopicsResult listTopicsResult = PowerMockito.mock(ListTopicsResult.class);
        ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
        listTopicsOptions.listInternal(true);
        PowerMockito.when(adminClient.listTopics()).thenReturn(listTopicsResult);

        Set<String> topicLists = new HashSet<>();
        topicLists.add(topicConfigReq.getTopicName());
        KafkaFuture<Set<String>> setKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(listTopicsResult.names()).thenReturn(setKafkaFuture);
        PowerMockito.when(setKafkaFuture.get()).thenReturn(topicLists);

        // mock describeConfig
        ConfigResource topicConfigResource = new ConfigResource(ConfigResource.Type.TOPIC, topicConfigReq.getTopicName());
        DescribeConfigsResult describeConfigsResult = PowerMockito.mock(DescribeConfigsResult.class);
        PowerMockito.when(adminClient.describeConfigs(Collections.singletonList(topicConfigResource))).thenReturn(describeConfigsResult);


        KafkaFuture<Map<ConfigResource, Config>> configMap = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeConfigsResult.all()).thenReturn(configMap);
        Map<ConfigResource, Config> configResourceConfigMap = new HashMap<>();

        configResourceConfigMap.put(new ConfigResource(ConfigResource.Type.TOPIC, topicConfigReq.getTopicName()), new Config(Collections.singletonList( new ConfigEntry("test", "test"))));
        PowerMockito.when(configMap.get()).thenReturn(configResourceConfigMap);

        ResultData<TopicPropertyResponse> test = topicOpService.getTopicProperty(topicConfigReq.getTopicName());
        Assert.assertEquals(test.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void updateTopicPropertyFailedTest(){
        TopicPropertyReq topicPropertyReq = new TopicPropertyReq();
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<String> test = topicOpService.updateTopicProperty(topicPropertyReq);
        Assert.assertEquals(test.getCode(), ReturnCode.RC008.getCode());
    }

    @Test
    public void updateTopicPropertyTest(){
        TopicPropertyReq topicPropertyReq = new TopicPropertyReq();
        topicPropertyReq.setTopicName(topicConfigReq.getTopicName());
        Map<String, String> config = new HashMap<>();
        config.put("test", "test");
        topicPropertyReq.setConfigs(config);
        AdminClient adminClient = PowerMockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        AlterConfigsResult alterConfigsResult = PowerMockito.mock(AlterConfigsResult.class);
        PowerMockito.when(adminClient.incrementalAlterConfigs(any())).thenReturn(alterConfigsResult);
        KafkaFuture<Void> voidKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(alterConfigsResult.all()).thenReturn(voidKafkaFuture);
        ResultData<String> test = topicOpService.updateTopicProperty(topicPropertyReq);
        Assert.assertEquals(test.getCode(), ReturnCode.RC200.getCode());
    }
    
    @Test
    public void importTopicsTest() {
        String batchTopicJson = "{\n" +
            "  \"topics\": [\n" +
            "    {\n" +
            "      \"topic\": \"TOPIC_SEAN_0\",\n" +
            "      \"partitions\": 10,\n" +
            "      \"replicationFactor\": 1,\n" +
            "      \"configs\": {},\n" +
            "      \"caution\": null\n" +
            "    },\n" +
            "    {\n" +
            "      \"topic\": \"TOPIC_SEAN_1\",\n" +
            "      \"partitions\": 10,\n" +
            "      \"replicationFactor\": 1,\n" +
            "      \"configs\": {},\n" +
            "      \"caution\": null\n" +
            "    },\n" +
            "    {\n" +
            "      \"topic\": \"TOPIC_SEAN_2\",\n" +
            "      \"partitions\": 10,\n" +
            "      \"replicationFactor\": 1,\n" +
            "      \"configs\": {},\n" +
            "      \"caution\": null\n" +
            "    },\n" +
            "    {\n" +
            "      \"topic\": \"TOPIC_SEAN_3\",\n" +
            "      \"partitions\": 10,\n" +
            "      \"replicationFactor\": 1,\n" +
            "      \"configs\": {},\n" +
            "      \"caution\": null\n" +
            "    },\n" +
            "    {\n" +
            "      \"topic\": \"TOPIC_SEAN_4\",\n" +
            "      \"partitions\": 10,\n" +
            "      \"replicationFactor\": 1,\n" +
            "      \"configs\": {},\n" +
            "      \"caution\": null\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        ResultData<String> resultData = topicOpService.importTopics(batchTopicJson);
        if (resultData.getCode() == ReturnCode.RC200.getCode()) {
             System.out.println("导入topic成功，返回结果：" + resultData.getData());
        } else {
            System.out.println("导入topic失败，返回结果：" + resultData.getMsg());
        }
    }
    
    @Test
    public void exportTopicsTest() {
        ResultData<String> stringResultData = topicOpService.exportTopics(Arrays.asList("topic111", "topic222"), true, null);
        if (stringResultData.getCode() == ReturnCode.RC200.getCode()) {
            System.out.println("导出topic成功，返回结果：" + stringResultData.getData());
        } else {
            System.out.println("导出topic失败，返回结果：" + stringResultData.getMsg());
        }
    }
    
    @Test
    public void generateImportTopicsJsonFileTest() {
        int topicNum = 30;
        int partitionNum = 12;
        int replicationFactor = 5;
        BatchTopicsBean importedTopics = new BatchTopicsBean();
        importedTopics.setTopics(new ArrayList<>());
        for (int i = 1; i <= topicNum; i++) {
            TopicBeanForImportAndExport newTopic = new TopicBeanForImportAndExport();
            newTopic.setTopic("sean_topic_" + i);
            newTopic.setPartitions(partitionNum);
            newTopic.setReplicationFactor(replicationFactor);
            importedTopics.getTopics().add(newTopic);
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("./import_topics.json"), importedTopics);
            System.out.println("json文件生成成功");
        } catch (IOException e) {
            System.out.println("json文件生成失败");
            throw new RuntimeException(e);
        }
    }
    
}
