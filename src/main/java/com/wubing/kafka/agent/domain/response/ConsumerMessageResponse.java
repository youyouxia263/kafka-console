package com.wubing.kafka.agent.domain.response;

import com.wubing.kafka.agent.domain.entity.ConsumerMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ConsumerMessageResponse {
    @ApiModelProperty("页码")
    private long pageNo;
    @ApiModelProperty("条数")
    private long pageSize;
    @ApiModelProperty("消息总数")
    private long total;
    @ApiModelProperty("消息列表")
    private List<ConsumerMessage> consumerMessageList;

    public ConsumerMessageResponse(long total,long pageNo,long pageSize, List<ConsumerMessage> consumerMessageList){
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.consumerMessageList = consumerMessageList;
    }
}
