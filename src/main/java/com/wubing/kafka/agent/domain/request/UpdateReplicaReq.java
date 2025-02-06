package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateReplicaReq {
    @NotEmpty
    @Pattern(regexp = "^[%|a-zA-Z0-9_-]+$")
    @Size(max = 127)
    @ApiModelProperty("topic名字")
    private String topicName;
    @ApiModelProperty("topic分区数")
    @Min(1)
    private int partitionNum;
    @ApiModelProperty("当前topic副本数")
    @Min(1)
    private int replicas;
    @ApiModelProperty("目标topic副本数")
    @Min(1)
    private int updateReplica;
}
