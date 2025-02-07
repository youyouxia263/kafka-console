package com.wubing.kafka.agent.service;

import com.wubing.kafka.agent.domain.request.BrokerConfigReq;
import com.wubing.kafka.agent.domain.response.BrokerConfigResponse;
import com.wubing.kafka.agent.domain.response.BrokerListResponse;
import com.wubing.kafka.agent.domain.response.ConsumersResponse;
import com.wubing.kafka.agent.domain.response.ProducersResponse;
import com.wubing.kafka.agent.utils.ResultData;

import javax.servlet.http.HttpServletResponse;

public interface BrokerOpService {
    ResultData<BrokerListResponse> listBrokers(int pageIndex, int pageSize);

    ResultData<BrokerConfigResponse> describeBroker(int brokerId);

    ResultData<String> updateBrokerConfig(BrokerConfigReq brokerConfigReq);

    void downloadTLS(HttpServletResponse response, String actualPath, String fileName);

    ResultData<ProducersResponse> listProducers(int pageIndex, int pageSize, String ip);

    ResultData<ConsumersResponse> listConsumers(int pageIndex, int pageSize, String ip);
}
