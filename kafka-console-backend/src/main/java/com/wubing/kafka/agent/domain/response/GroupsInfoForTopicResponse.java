package com.wubing.kafka.agent.domain.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GroupsInfoForTopicResponse {

    @ApiModelProperty("消费组名字")
    private String groupId;

    @ApiModelProperty("消费者数量")
    private int memberNum;

    @ApiModelProperty("消费组状态")
    private String state;

    @ApiModelProperty("消费组堆积消息数")
    private long lag;

}
