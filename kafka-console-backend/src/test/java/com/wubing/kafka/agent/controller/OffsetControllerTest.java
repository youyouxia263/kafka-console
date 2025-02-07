package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.request.OffsetResetReq;
import com.wubing.kafka.agent.service.OffsetService;
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

import java.util.concurrent.ExecutionException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaClientPool.class)
@PowerMockIgnore({"javax.management.*"})
public class OffsetControllerTest {

    @Mock
    private OffsetService offsetService;

    @InjectMocks
    private OffsetController offsetController;

    @Test
    public void resetOffsetTest() throws ExecutionException, InterruptedException {
        OffsetResetReq offsetResetReq = new OffsetResetReq();
        Mockito.when(offsetService.resetOffset(offsetResetReq)).thenReturn(ResultData.success("success"));
        ResultData<String> testResult = offsetController.resetOffset(offsetResetReq);
        Assert.assertEquals(testResult.getCode(), ReturnCode.RC200.getCode());
    }

}
