package com.wubing.kafka.agent.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.kafka.clients.admin.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TopicDescribeResponse {
    @ApiModelProperty("topic名字")
    private String name;
    @ApiModelProperty("topic是否为内部topic")
    private boolean internal;
    @ApiModelProperty("topic分区信息")
    private List<Partitions> partitions;
    @ApiModelProperty("topic id")
    private String topicId;
    @ApiModelProperty("topic 属性配置")
    private Map<String, String> topicProperties;

    public void setTopicPropertiesByConfig(Config config){
        Map<String, String> thisTopicConfig = new HashMap<>();
        config.entries().forEach(key -> thisTopicConfig.put(key.name(),key.value()));
        this.setTopicProperties(thisTopicConfig);
    }

    @Data
    public  static class Partitions {
        @ApiModelProperty("topic分区")
        private Integer partition;
        @ApiModelProperty("topic leader id")
        private Integer leader;
        @ApiModelProperty("topic分区副本id")
        private List<Integer> replicas ;
        @ApiModelProperty("topic分区isr集合")
        private List<Integer> isr;
        @ApiModelProperty("topic分区消息最大位移")
        private Long offsetMax;
        @ApiModelProperty("topic分区消息最小位移")
        private Long offsetMin;
    }

}
