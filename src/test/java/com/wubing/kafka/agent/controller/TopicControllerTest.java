package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.request.TopicConfigReq;
import com.wubing.kafka.agent.domain.request.TopicPropertyReq;
import com.wubing.kafka.agent.domain.response.GroupsInfoForTopicResponse;
import com.wubing.kafka.agent.domain.response.TopicDescribeResponse;
import com.wubing.kafka.agent.domain.response.TopicListResponse;
import com.wubing.kafka.agent.domain.response.TopicPropertyResponse;
import com.wubing.kafka.agent.service.TopicOpService;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaClientPool.class)
@PowerMockIgnore({"javax.management.*"})
public class TopicControllerTest {

    @Mock
    private TopicOpService topicOpService;

    @InjectMocks
    private TopicController topicController;


    @Test
    public void createTopicTest(){
        TopicConfigReq topicConfigReq = new TopicConfigReq();
        Mockito.when(topicOpService.createTopic(topicConfigReq)).thenReturn(ResultData.success("success"));
        ResultData<String> testResult = topicController.createTopic(topicConfigReq);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void deleteTopicTest(){
        TopicConfigReq topicConfigReq = new TopicConfigReq();
        topicConfigReq.setTopicName("topic1");
        Mockito.when(topicOpService.deleteTopic(topicConfigReq.getTopicName())).thenReturn(ResultData.success("success"));
        ResultData<String> testResult = topicController.deleteTopic(topicConfigReq);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void describeTopicTest(){
        String topicName = "topic1";
        TopicDescribeResponse topicDescribeResponse = new TopicDescribeResponse();
        Mockito.when(topicOpService.describeTopic(topicName)).thenReturn(ResultData.success(topicDescribeResponse));
        ResultData<TopicDescribeResponse> testResult = topicController.describeTopic(topicName);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void listTopicTest(){
        TopicListResponse topicListResponse = new TopicListResponse();
        Mockito.when(topicOpService.listTopic(1,1)).thenReturn(ResultData.success(topicListResponse));
        ResultData<TopicListResponse> testResult = topicController.listTopic(1,1);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void consumerGroupsByTopicTest(){
        String topicName = "topic1";
        List<GroupsInfoForTopicResponse> groupsInfoForTopicResponseList = new ArrayList<>();
        Mockito.when(topicOpService.consumerGroupsByTopic(topicName)).thenReturn(ResultData.success(groupsInfoForTopicResponseList));
        ResultData<List<GroupsInfoForTopicResponse>> testResult = topicController.consumerGroupsByTopic(topicName);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void getTopicPropertyTest(){
        String topicName = "topic1";
        TopicPropertyResponse topicPropertyResponse = new TopicPropertyResponse();
        Mockito.when(topicOpService.getTopicProperty(topicName)).thenReturn(ResultData.success(topicPropertyResponse));
        ResultData<TopicPropertyResponse> testResult = topicController.getTopicProperty(topicName);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void updateTopicPropertyTest(){
        TopicPropertyReq topicPropertyReq = new TopicPropertyReq();
        String ok = "ok";
        Mockito.when(topicOpService.updateTopicProperty(topicPropertyReq)).thenReturn(ResultData.success(ok));
        ResultData<String> testResult = topicController.updateTopicProperty(topicPropertyReq);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }
}
