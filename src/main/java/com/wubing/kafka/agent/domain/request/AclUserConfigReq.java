package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AclUserConfigReq {
    @NotEmpty
    @ApiModelProperty("acl用户")
    private String aclUser;
    @ApiModelProperty("acl用户密码")
    private String aclUserPassword;
    @ApiModelProperty("认证方式")
    private String mechanism = "SCRAM";

}
