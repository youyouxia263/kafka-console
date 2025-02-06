package com.wubing.kafka.agent.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.kafka.clients.admin.Config;

import java.util.HashMap;
import java.util.Map;

import static com.wubing.kafka.agent.utils.KafkaOpUtil.parseConfig2Map;

@Data
public class BrokerConfigResponse {
    @ApiModelProperty("broker属性配置")
    private Map<String, String> brokerConfig;

    public void setBrokerConfigByConfig(Config config){
        Map<String, String> brokerConfig = new HashMap<>();
        config.entries().parallelStream().forEach(key -> brokerConfig.put(key.name(),key.value()));
        this.setBrokerConfig(parseConfig2Map(config));
    }

}
