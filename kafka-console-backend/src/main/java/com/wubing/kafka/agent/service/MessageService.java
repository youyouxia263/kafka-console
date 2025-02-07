package com.wubing.kafka.agent.service;

import com.wubing.kafka.agent.domain.request.SendMessageReq;
import com.wubing.kafka.agent.domain.response.ConsumerMessageResponse;
import com.wubing.kafka.agent.utils.ResultData;

public interface MessageService {
    ResultData<ConsumerMessageResponse> listMessagesByOffset(String topicName, int partition, long offset, int pageNo,int pageSize);

    ResultData<ConsumerMessageResponse> listMessagesByTime(String topicName, int partition, long beginTime, long endTime, int pageNo,int pageSize);

    ResultData<String> sendMessage(SendMessageReq sendMessageReq);
}
