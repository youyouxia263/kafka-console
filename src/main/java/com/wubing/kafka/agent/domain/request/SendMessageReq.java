package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class SendMessageReq {
    @NotEmpty
    @Pattern(regexp = "^[%|a-zA-Z0-9_-]+$")
    @Size(max = 127)
    @ApiModelProperty("topic名字")
    private String topicName;
    @ApiModelProperty("topic分区, optional")
    private int partition;
    @ApiModelProperty("消息的key值, optional")
    private String key;
    @ApiModelProperty("消息内容")
    private String content;
}
