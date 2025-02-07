package com.wubing.kafka.agent.domain.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ConsumerMessage {
    @ApiModelProperty("topic名")
    private String topicName;
    @ApiModelProperty("分区")
    private int partition;
    @ApiModelProperty("消息位移")
    private long offset;
    @ApiModelProperty("消息key")
    private String key;
    @ApiModelProperty("消息value")
    private String value;
    @ApiModelProperty("消息创建时间-13位时间戳")
    private long timestamp;
}
