package com.wubing.kafka.agent.domain.response;

import com.wubing.kafka.agent.domain.entity.AclInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class AclsResponse {
    @ApiModelProperty("acl权限总数")
    private int totalCount;
    @ApiModelProperty("acl权限列表信息")
    private List<AclInfo> item;
}
