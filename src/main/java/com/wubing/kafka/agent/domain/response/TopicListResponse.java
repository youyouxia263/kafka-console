package com.wubing.kafka.agent.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
public class TopicListResponse {
    @ApiModelProperty("topic总数")
    private int totalCount;
    @ApiModelProperty("topic列表信息")
    private List<TopicNameInfo> item;

    @Data
    public static class TopicNameInfo {
        @ApiModelProperty("topic名")
        private String name;
        @ApiModelProperty("topic是否为内部topic")
        private boolean internal;
        @ApiModelProperty("topic分区数")
        private int partitionNum;
        @ApiModelProperty("topic副本数")
        private int replicasNum;
    }
}
