package com.wubing.kafka.agent.domain.entity;

import lombok.Data;

@Data
public class OffsetResult {
    private boolean isOk;
    private String errMsg;

}
