package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.domain.request.AclUserConfigReq;
import com.wubing.kafka.agent.domain.response.AclUsersResponse;
import com.wubing.kafka.agent.service.AclUserService;
import com.wubing.kafka.agent.utils.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/acl_user")
@Api(tags = "Acl权限操作接口")
public class AclUserController {

    @Resource
    private AclUserService aclUserService;

    @PostMapping("/upsert")
    @ApiOperation("创建或更新ACL用户")
    public ResultData<String> upsertAclUser(@RequestBody @Valid AclUserConfigReq aclUserConfigReq) {
        return aclUserService.upsertAclUser(aclUserConfigReq);
    }

    @GetMapping("/list")
    @ApiOperation("获取Acl列表")
    public ResultData<AclUsersResponse> getAclUsers(@RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        return aclUserService.getAclUsers(pageIndex, pageSize);
    }

    @PostMapping("/delete")
    @ApiOperation("删除Acl用户")
    public ResultData<String> deleteAclUser(@RequestBody @Valid AclUserConfigReq aclUserConfigReq) {
        return aclUserService.deleteAclUser(aclUserConfigReq.getAclUser());
    }
}
