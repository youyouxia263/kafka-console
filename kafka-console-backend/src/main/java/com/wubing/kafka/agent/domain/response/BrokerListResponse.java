package com.wubing.kafka.agent.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class BrokerListResponse {
    @ApiModelProperty("broker总数")
    private int totalCount;
    @ApiModelProperty("broker列表信息")
    private List<BrokerListResponse.BrokerInfo> item;

    @Data
    @AllArgsConstructor
    public static class BrokerInfo {
        @ApiModelProperty("broker id")
        private int brokerId;
        @ApiModelProperty("broker的host或ip")
        private String host;
        @ApiModelProperty("端口")
        private int port;
        @ApiModelProperty("controller节点")
        private boolean controller;
    }
}
