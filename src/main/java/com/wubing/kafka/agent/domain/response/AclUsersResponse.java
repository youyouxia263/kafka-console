package com.wubing.kafka.agent.domain.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.List;
import java.util.Properties;

@Data
public class AclUsersResponse {

    @ApiModelProperty("acl用户总数")
    private int totalCount;
    @ApiModelProperty("acl用户列表信息")
    private List<AclUsersResponse.AclUserInfo> item;

    @Data
    @AllArgsConstructor
    public static class AclUserInfo {
        @ApiModelProperty("用户名")
        private String aclUser;
        @ApiModelProperty("用户密码")
        private String aclUserPassword;
        @ApiModelProperty("认证类型")
        private String certType;
        @ApiModelProperty("acl用户属性")
        private Properties aclUserConfig;
    }

}
