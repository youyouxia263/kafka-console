package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.domain.request.OffsetResetReq;
import com.wubing.kafka.agent.service.OffsetService;
import com.wubing.kafka.agent.utils.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
@RequestMapping("/offset")
@Api(tags = "offset操作接口")
public class OffsetController {

    @Resource
    private OffsetService offsetService;

    @PostMapping("/reset")
    @ApiOperation("重置消费位点")
    public ResultData<String> resetOffset(@Valid @RequestBody OffsetResetReq offsetResetReq) throws ExecutionException, InterruptedException {
        return offsetService.resetOffset(offsetResetReq);
    }
}
