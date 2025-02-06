package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * acl权限参数对应表
 * AclOperation:
 * 0-UNKNOWN 1-ANY 2-ALL 3-READ 4-WRITE 5-CREATE 6-DELETE 7-ALTER 8-DESCRIBE 9-CLUSTER_ACTION 10-DESCRIBE_CONFIGS 11-ALTER_CONFIGS 12-IDEMPOTENT_WRITE
 * AclPermissionType:AD
 * 0-UNKNOWN 1-ANY 2-DENY 3-ALLOW
 * ResourceType:
 * 0-UNKNOWN 1-ANY 2-TOPIC 3-GROUP 4-CLUSTER 5-TRANSACTIONAL_ID 6-DELEGATION_TOKEN
 * PatternType:
 * 0-UNKNOWN 1-ANY 2-MATCH 3-LITERAL 4-PREFIXED
 */
@Data
public class AclConfigReq {
    @NotNull
    @Min(0)
    @Max(2)
    @ApiModelProperty("用途：0-生产权限，1-消费权限，2-自定义权限")
    private int useType;
    @NotEmpty
    @ApiModelProperty("acl用户-principal")
    private List<String> principals;
    @NotNull
    @ApiModelProperty("主机/ip")
    private String aclClientHost;
    @NotNull
    @Min(0)
    @Max(12)
    @ApiModelProperty("操作")
    private int aclOperation;
    @NotNull
    @Min(0)
    @Max(3)
    @ApiModelProperty("策略")
    private int aclPermissionType;

    @Min(0)
    @Max(6)
    @ApiModelProperty("资源类型")
    private int resourceType;
    @ApiModelProperty("topic名")
    private List<String> topicNames;
    @ApiModelProperty("消费组名")
    private List<String> consumerGroupNames;
    @ApiModelProperty("资源名称列表")
    private List<String> resourceNames;
    @Min(0)
    @Max(4)
    @ApiModelProperty("自定义匹配类型")
    private int resourcePatternType;
    @Min(0)
    @Max(4)
    @ApiModelProperty("topic匹配类型")
    private int topicPatternType;
    @Min(0)
    @Max(4)
    @ApiModelProperty("消费组匹配类型")
    private int groupPatternType;

}
