package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.entity.ConsumerMessage;
import com.wubing.kafka.agent.domain.response.ConsumerMessageResponse;
import com.wubing.kafka.agent.service.MessageService;
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

import java.util.Collections;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaClientPool.class)
@PowerMockIgnore({"javax.management.*"})
public class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @Test
    public void listMessagesTest()  {
        List<ConsumerMessage> consumerMessageList = Collections.singletonList(new ConsumerMessage());
        ResultData<ConsumerMessageResponse> consumerGroupDesResponse = ResultData.success( new ConsumerMessageResponse(1, 1,10,consumerMessageList));
        Mockito.when(messageService.listMessagesByOffset("topic1", 0, 0, 1, 10)).thenReturn(consumerGroupDesResponse);

        ResultData<ConsumerMessageResponse> testResult = messageController.listMessagesByOffset("topic1", 0, 0, 1, 10);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

}
