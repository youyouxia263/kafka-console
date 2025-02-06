package com.wubing.kafka.agent.service;

import com.wubing.kafka.agent.domain.response.ConsumerGroupDesResponse;
import com.wubing.kafka.agent.domain.response.ConsumerGroupListResponse;
import com.wubing.kafka.agent.utils.ResultData;

public interface ConsumerGroupService {
    ResultData<String> deleteConsumerGroup(String groupId);
    ResultData<ConsumerGroupDesResponse> describeConsumerGroup(String groupId);
    ResultData<ConsumerGroupListResponse> listConsumerGroup(int pageIndex, int pageSize);
}
