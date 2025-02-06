package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Map;

@Data
public class TopicPropertyReq {
    @NotEmpty
    @Pattern(regexp = "^[%|a-zA-Z0-9_-]+$")
    @Size(max = 127)
    @ApiModelProperty("topic名字")
    private String topicName;
    @ApiModelProperty("topic属性配置")
    @NotEmpty
    private Map<String, String> configs;
}

