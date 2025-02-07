package com.wubing.kafka.agent.domain.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@AllArgsConstructor
public class AclInfo {
    @NotEmpty
    @ApiModelProperty("acl用户-principal")
    private String principal;
    @ApiModelProperty("主机/ip")
    private String aclClientHost;

    @Max(12)
    @NotNull
    @ApiModelProperty("操作")
    private int aclOperation;

    @Max(3)
    @NotNull
    @ApiModelProperty("策略")
    private int aclPermissionType;


    @Max(6)
    @NotNull
    @ApiModelProperty("资源类型")
    private int resourceType;
    @ApiModelProperty("资源名字")
    private String resourceName;

    @Max(4)
    @ApiModelProperty("匹配类型")
    private int resourcePatternType;

}
