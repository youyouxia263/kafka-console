package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Map;

@Data
public class TopicConfigReq {
    @NotEmpty
    @Pattern(regexp = "^[%|a-zA-Z0-9_-]+$")
    @Size(max = 127)
    @ApiModelProperty("topic名字")
    private String topicName;
    @ApiModelProperty("topic分区数")
    private int partitions;
    @ApiModelProperty("topic分区副本数")
    private int replicationFactor;
    @ApiModelProperty("topic配置")
    private Map<String, String> configs;
}
