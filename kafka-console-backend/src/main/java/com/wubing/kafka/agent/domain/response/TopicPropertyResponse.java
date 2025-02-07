package com.wubing.kafka.agent.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.kafka.clients.admin.Config;

import java.util.HashMap;
import java.util.Map;

@Data
public class TopicPropertyResponse {
    @ApiModelProperty("topic属性配置")
    private Map<String, String> topicProperty;

    public void setTopicPropertyByConfig(Config config){
        Map<String, String> thisTopicConfig = new HashMap<>();
        config.entries().forEach(key -> thisTopicConfig.put(key.name(),key.value()));
        this.setTopicProperty(thisTopicConfig);
    }
}
