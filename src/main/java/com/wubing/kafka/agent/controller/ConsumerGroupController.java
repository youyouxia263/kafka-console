package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.domain.request.ConsumerGroupReq;
import com.wubing.kafka.agent.domain.response.ConsumerGroupDesResponse;
import com.wubing.kafka.agent.domain.response.ConsumerGroupListResponse;
import com.wubing.kafka.agent.service.ConsumerGroupService;
import com.wubing.kafka.agent.utils.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * @author wubing
 */

@RestController
@Slf4j
@RequestMapping("/consumerguoup")
@Api(tags = "consumerguoup操作接口")
public class ConsumerGroupController {

    @Resource
    private ConsumerGroupService consumerGroupService;

    @PostMapping("/delete")
    @ApiOperation("删除consumer group")
    public ResultData<String> deleteConsumerGroup(@RequestBody @Valid ConsumerGroupReq consumerGroupReq){
        return consumerGroupService.deleteConsumerGroup(consumerGroupReq.getGroupId());
    }

    @GetMapping("/detail")
    @ApiOperation("获取消费组详情")
    public ResultData<ConsumerGroupDesResponse> describeConsumerGroup(@RequestParam @NotBlank String groupId){
        return consumerGroupService.describeConsumerGroup(groupId);
    }

    @GetMapping("/list")
    @ApiOperation("获取消费组列表")
    public ResultData<ConsumerGroupListResponse> listConsumerGroup(@RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "10") int pageSize){
        return consumerGroupService.listConsumerGroup(pageIndex, pageSize);
    }
}
