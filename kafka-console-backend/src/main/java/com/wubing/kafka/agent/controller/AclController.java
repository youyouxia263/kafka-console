package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.domain.entity.AclInfo;
import com.wubing.kafka.agent.domain.request.AclConfigReq;
import com.wubing.kafka.agent.domain.response.AclsResponse;
import com.wubing.kafka.agent.service.AclOpService;
import com.wubing.kafka.agent.utils.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/acl")
@Api(tags = "Acl权限操作接口")
public class AclController {

    @Resource
    private AclOpService aclOpService;

    @PostMapping("/create")
    @ApiOperation("创建ACL权限")
    public ResultData<String> createAcl(@RequestBody @Valid AclConfigReq aclConfigReq) {
        return aclOpService.createAcl(aclConfigReq);
    }

    @GetMapping("/list")
    @ApiOperation("获取Acl权限列表")
    public ResultData<AclsResponse> listAclLimits(@RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        return aclOpService.listAclLimits(pageIndex, pageSize);
    }

    @PostMapping("/delete")
    @ApiOperation("删除Acl权限")
    public ResultData<String> deleteAclLimits(@RequestBody @Valid AclInfo aclInfo) {
        return aclOpService.deleteAclLimits(aclInfo);
    }
}
