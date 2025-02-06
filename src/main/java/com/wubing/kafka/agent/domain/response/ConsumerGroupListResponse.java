package com.wubing.kafka.agent.domain.response;

import com.wubing.kafka.agent.domain.entity.ConsumerGroupInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ConsumerGroupListResponse {

    @ApiModelProperty("消费组总数")
    private int totalCount;
    @ApiModelProperty("消费组列表信息")
    private List<ConsumerGroupInfo> item;

}
