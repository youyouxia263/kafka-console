package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.domain.request.SendMessageReq;
import com.wubing.kafka.agent.domain.response.ConsumerMessageResponse;
import com.wubing.kafka.agent.service.MessageService;
import com.wubing.kafka.agent.utils.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@Slf4j
@RequestMapping("/message")
@Api(tags = "消息操作接口")
public class MessageController {

    @Resource
    private MessageService messageService;

    @GetMapping("/list/offset")
    @ApiOperation("列出某个topic分区的消息")
    @Valid
    public ResultData<ConsumerMessageResponse> listMessagesByOffset(@RequestParam @NotBlank String topicName,
                                                                    @RequestParam @NotBlank int partition,
                                                                    @RequestParam(required = false, defaultValue = "0") long offset,
                                                                    @RequestParam @NotBlank int pageNo,
                                                                    @RequestParam(defaultValue = "20") int pageSize) {
        return messageService.listMessagesByOffset(topicName, partition, offset, pageNo, pageSize);
    }

    @GetMapping("/list/time")
    @ApiOperation("列出某个topic分区的消息")
    @Valid
    public ResultData<ConsumerMessageResponse> listMessagesByTime(@RequestParam @NotBlank String topicName,
                                                                  @RequestParam @NotBlank int partition,
                                                                  @RequestParam(required = false, defaultValue = "0") long beginTime,
                                                                  @RequestParam(required = false, defaultValue = "0") long endTime,
                                                                  @RequestParam @NotBlank int pageNo,
                                                                  @RequestParam(defaultValue = "20") int pageSize) {
        return messageService.listMessagesByTime(topicName, partition, beginTime, endTime, pageNo, pageSize);
    }

    @PostMapping("/send")
    @ApiOperation("发送消息")
    public ResultData<String> sendMessage(@RequestBody @Valid SendMessageReq sendMessageReq) {
        return messageService.sendMessage(sendMessageReq);
    }

}
