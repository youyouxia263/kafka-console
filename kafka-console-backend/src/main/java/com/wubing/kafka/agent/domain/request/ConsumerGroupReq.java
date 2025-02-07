package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class ConsumerGroupReq {
    @NotEmpty
    @Pattern(regexp = "^[%|a-zA-Z0-9_-]+$")
    @Size(max = 127)
    @ApiModelProperty("消费组id")
    private String groupId;
}
