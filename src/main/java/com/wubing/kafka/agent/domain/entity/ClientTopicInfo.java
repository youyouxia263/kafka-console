package com.wubing.kafka.agent.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientTopicInfo {
    private String topic;
    private int partition;
    private int brokerId;
}
