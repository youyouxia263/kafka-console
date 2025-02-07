package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class OffsetResetReq {
    @NotEmpty
    @Pattern(regexp = "^[%|a-zA-Z0-9_-]+$")
    @Size(max = 127)
    @ApiModelProperty("topic名字")
    private String topicName;
    @NotNull
    @ApiModelProperty("topic分区")
    private int partition;
    @NotNull
    @ApiModelProperty("消费组")
    private String groupId;
    @NotNull
    @ApiModelProperty("重置类型:0-位移,1-时间戳；2-最初位移；3-最新位移")
    private int resetType;
    @ApiModelProperty("重置位移的13位时间戳")
    private long resetTime;
    @ApiModelProperty("重置位移")
    private long resetOffset;
}
