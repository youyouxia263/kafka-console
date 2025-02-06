package com.wubing.kafka.agent.service;

import com.wubing.kafka.agent.domain.request.OffsetResetReq;
import com.wubing.kafka.agent.utils.ResultData;

import java.util.concurrent.ExecutionException;

public interface OffsetService {
    ResultData<String> resetOffset(OffsetResetReq offsetResetReq) throws ExecutionException, InterruptedException;
}
