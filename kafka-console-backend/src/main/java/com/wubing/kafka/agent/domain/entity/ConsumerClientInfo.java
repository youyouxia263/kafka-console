package com.wubing.kafka.agent.domain.entity;

import lombok.Data;

import java.util.List;

@Data
public class ConsumerClientInfo {
    private String clientId;
    private String connectionId;
    private String userName;
    private String securityProtocol;
    private String clientHost;
    private int brokerId;
    private int maxWaitMs;
    private int minBytes;
    private int maxBytes;
    private List<ClientTopicInfo> topics;
}
