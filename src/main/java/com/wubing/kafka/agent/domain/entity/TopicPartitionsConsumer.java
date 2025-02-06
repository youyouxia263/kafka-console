package com.wubing.kafka.agent.domain.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TopicPartitionsConsumer {
    @ApiModelProperty("topic名字")
    private String topicName;
    @ApiModelProperty("topic分区订阅信息列表")
    List<TopicPartitionConsumerInfo> topicPartitions;
}
