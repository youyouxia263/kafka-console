package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.domain.entity.ResourceInspectBean;
import com.wubing.kafka.agent.domain.request.*;
import com.wubing.kafka.agent.domain.response.GroupsInfoForTopicResponse;
import com.wubing.kafka.agent.domain.response.TopicDescribeResponse;
import com.wubing.kafka.agent.domain.response.TopicListResponse;
import com.wubing.kafka.agent.domain.response.TopicPropertyResponse;
import com.wubing.kafka.agent.service.TopicOpService;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.domain.request.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/topic")
@Api(tags = "topic操作接口")
public class TopicController {

    @Resource
    private TopicOpService topicOpService;

    @PostMapping("/create")
    @ApiOperation("创建topic")
    public ResultData<String> createTopic(@RequestBody @Valid TopicConfigReq topicConfigReq) {
        return topicOpService.createTopic(topicConfigReq);
    }

    @PostMapping("/delete")
    @ApiOperation("删除topic")
    public ResultData<String> deleteTopic(@RequestBody @Valid TopicConfigReq topicConfigReq) {
        return topicOpService.deleteTopic(topicConfigReq.getTopicName());
    }

    @GetMapping("/delete/if-legal")
    @ApiOperation("删除topic操作是否合法")
    public ResultData<ResourceInspectBean> deleteTopicIfLegal(@RequestParam("topicName") String topicName) {
        return topicOpService.deleteTopicIfLegal(topicName);
    }

    @GetMapping("/detail")
    @ApiOperation("获取topic详情")
    public ResultData<TopicDescribeResponse> describeTopic(@RequestParam @NotBlank String topicName) {
        return topicOpService.describeTopic(topicName);
    }

    @GetMapping("/list")
    @ApiOperation("获取topic列表")
    public ResultData<TopicListResponse> listTopic(@RequestParam int pageIndex, @RequestParam int pageSize) {
        return topicOpService.listTopic(pageIndex, pageSize);
    }

    @GetMapping("/groups-info")
    @ApiOperation("获取订阅某topic的消费组信息")
    public ResultData<List<GroupsInfoForTopicResponse>> consumerGroupsByTopic(@RequestParam @NotBlank String topicName) {
        return topicOpService.consumerGroupsByTopic(topicName);
    }

    @GetMapping("/property")
    @ApiOperation("获取topic属性配置")
    public ResultData<TopicPropertyResponse> getTopicProperty(@RequestParam @NotBlank String topicName) {
        return topicOpService.getTopicProperty(topicName);
    }

    @PostMapping("/property")
    @ApiOperation("更新topic属性配置")
    public ResultData<String> updateTopicProperty(@RequestBody @Valid TopicPropertyReq topicPropertyReq) {
        return topicOpService.updateTopicProperty(topicPropertyReq);
    }

    @PostMapping("/addpartitions")
    @ApiOperation("增加topic分区")
    public ResultData<String> updateTopicPartitions(@RequestBody @Valid UpdatePartitionReq updatePartitionReq) {
        return topicOpService.updateTopicPartitions(updatePartitionReq);
    }

    @PostMapping("/addreplica")
    @ApiOperation("增加topic副本数")
    public ResultData<String> updateTopicReplica(@RequestBody @Valid UpdateReplicaReq updateReplica ) {
        return topicOpService.updateTopicReplica(updateReplica);
    }

    @PostMapping("/import")
    @ApiOperation("批量导入topic")
    public ResultData<String> importTopics(@RequestBody ImportTopicsReq importTopicsReq) {
        String batchTopicJson = new String(importTopicsReq.getImportTopicBytes());
        return topicOpService.importTopics(batchTopicJson);
    }

    @PostMapping("/export")
    @ApiOperation("批量导出topic")
    public ResultData<String> exportTopics(@RequestBody ExportTopicsReq exportedTopicsRequest,
                                           HttpServletResponse response) {
        boolean exportAll = exportedTopicsRequest.isExportAll();
        List<String> exportedTopicList = exportedTopicsRequest.getExportedTopicList();
        return topicOpService.exportTopics(exportedTopicList, exportAll, response);
    }

//    @PostMapping("/receiveMessage")
//    @ApiOperation("接收消息")
//    public ResultData<String> receiveMessage(@RequestBody @Valid UpdateReplicaReq updateReplica ) {
//
//    }

}
