package com.wubing.kafka.agent.domain.response;

import com.wubing.kafka.agent.domain.entity.TopicPartitionConsumerInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ConsumerGroupDesResponse {

    @ApiModelProperty("消费组id")
    private String groupId;
    @ApiModelProperty("订阅topic列表信息")
    private Map<String, List<TopicPartitionConsumerInfo>> topicConsumerInfoMap;
    @ApiModelProperty("消费分区方法")
    private String partitionAssignor;
    @ApiModelProperty("消费组状态")
    private String state;
    @ApiModelProperty("消费组堆积消息数")
    private long lag;
    @ApiModelProperty("消费者数量")
    private int memberNum;

}
