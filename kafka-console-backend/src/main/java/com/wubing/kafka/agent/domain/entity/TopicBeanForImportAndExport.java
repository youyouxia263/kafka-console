package com.wubing.kafka.agent.domain.entity;

import com.wubing.kafka.agent.constants.KafkaCreateTopicConfigs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.admin.Config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicBeanForImportAndExport {
    
    @NotEmpty
    @Pattern(regexp = "^[%|a-zA-Z0-9_-]+$")
    @Size(max = 127)
    private String topic;
    
    @NotEmpty
    @Min(1)
    private int partitions;
    
    @NotEmpty
    @Min(1)
    private int replicationFactor;
    
    private Map<String, String> configs = new HashMap<>();
    
    private String caution;
    
    public void setTopicPropertiesByConfig(Config config){
        Map<String, String> thisTopicConfig = new HashMap<>();
        config.entries().forEach(configEntry -> {
            if (KafkaCreateTopicConfigs.listAllConfigs().contains(configEntry.name())) {
            thisTopicConfig.put(configEntry.name(),configEntry.value());
            }
        });
        this.setConfigs(thisTopicConfig);
    }
}