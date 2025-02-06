package com.wubing.kafka.agent.domain.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TopicPartitionConsumerInfo {

    @ApiModelProperty("topic分区")
    private int partition;
    @ApiModelProperty("消费成员id")
    private String memberId;
    @ApiModelProperty("消费成员客户端id")
    private String clientId;
    @ApiModelProperty("消费成员host地址")
    private String host;
    @ApiModelProperty("topic分区当前消费位移")
    private long currentOffset;
    @ApiModelProperty("topic分区最大消费位移")
    private long endOffset;
    @ApiModelProperty("topic分区消息堆积量")
    private long lag;
    public TopicPartitionConsumerInfo (int partition, String memberId, String clientId, String host, long currentOffset,
                                       long endOffset, long lag){
        this.partition = partition;
        this.memberId = memberId;
        this.clientId = clientId;
        this.host = host;
        this.currentOffset = currentOffset;
        this.endOffset = endOffset;
        this.lag = lag;

    }
}
