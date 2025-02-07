package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.request.ConsumerGroupReq;
import com.wubing.kafka.agent.domain.response.ConsumerGroupDesResponse;
import com.wubing.kafka.agent.domain.response.ConsumerGroupListResponse;
import com.wubing.kafka.agent.service.ConsumerGroupService;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaClientPool.class)
@PowerMockIgnore({"javax.management.*"})
public class ConsumerGroupControllerTest {


    @Mock
    private ConsumerGroupService consumerGroupService;

    @InjectMocks
    private ConsumerGroupController consumerGroupController;

    @Test
    public void deleteConsumerGroupTest() throws Exception {
        String groupId = "groutId";
        ResultData<String> deleteResult = ResultData.success("success");
        Mockito.when(consumerGroupService.deleteConsumerGroup(groupId)).thenReturn(deleteResult);

        ConsumerGroupReq consumerGroupReq = new ConsumerGroupReq();
        consumerGroupReq.setGroupId(groupId);
        ResultData<String> testResult = consumerGroupController.deleteConsumerGroup(consumerGroupReq);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void describeConsumerGroupTest() throws Exception {
        String groupId = "groutId";
        ConsumerGroupDesResponse consumerGroupDesResponse = new ConsumerGroupDesResponse();
        ResultData<ConsumerGroupDesResponse> consumerGroupDesResponseResultData = ResultData.success(consumerGroupDesResponse);
        Mockito.when(consumerGroupService.describeConsumerGroup(groupId)).thenReturn(consumerGroupDesResponseResultData);

        ResultData<ConsumerGroupDesResponse> testResult = consumerGroupController.describeConsumerGroup(groupId);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

    @Test
    public void listConsumerGroupTest() throws Exception {

        ResultData<ConsumerGroupListResponse> consumerGroupDesResponse = ResultData.success( new ConsumerGroupListResponse());
        Mockito.when(consumerGroupService.listConsumerGroup(1,1)).thenReturn(consumerGroupDesResponse);
        ResultData<ConsumerGroupListResponse> testResult = consumerGroupController.listConsumerGroup(1,1);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }
}
