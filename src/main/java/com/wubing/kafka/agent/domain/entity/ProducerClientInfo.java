package com.wubing.kafka.agent.domain.entity;

import lombok.Data;

import java.util.List;

@Data
public class ProducerClientInfo {
    private String clientId;
    private String connectionId;
    private String userName;
    private String securityProtocol;
    private String clientHost;
    private List<ClientTopicInfo> topics;
    private int brokerId;
    private String acks;
    private int timeoutMs;
}
