package com.wubing.kafka.agent.domain.response;

import com.wubing.kafka.agent.domain.entity.ConsumerClientInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ConsumersResponse {

    @ApiModelProperty("消费者链接总数")
    private int totalCount;
    @ApiModelProperty("消费者链接列表信息")
    private List<ConsumersInfo> item;

    @Data
    public static class ConsumersInfo {
        private String ip;
        private int connectionCount;
        private String consumerId;
        private Map<Integer, String> brokerIdMap;
        private List<ConsumerClientInfo> consumerClientInfoList;
    }
}
