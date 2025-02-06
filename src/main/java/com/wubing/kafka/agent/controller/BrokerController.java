package com.wubing.kafka.agent.controller;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.entity.ResourceInspectBean;
import com.wubing.kafka.agent.domain.request.BrokerConfigReq;
import com.wubing.kafka.agent.domain.response.BrokerConfigResponse;
import com.wubing.kafka.agent.domain.response.BrokerListResponse;
import com.wubing.kafka.agent.domain.response.ConsumersResponse;
import com.wubing.kafka.agent.domain.response.ProducersResponse;
import com.wubing.kafka.agent.service.BrokerOpService;
import com.wubing.kafka.agent.utils.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.ConsumerGroupState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/broker")
@Api(tags = "broker操作接口")
public class BrokerController {

    @Value("${kafka.tls-client.keystore}")
    private String tlsKeyStorePath;

    @Value("${kafka.tls-client.truststore}")
    private String tlsTrustStorePath;

    @Value("${kafka.ssl-enable}")
    private String sslEnable;

    @Value("${kafka.tls-client.keystore-password}")
    private String keyStorePassword;

    @Value("${kafka.tls-client.truststore-password}")
    private String trustStorePassword;

    @Resource
    private BrokerOpService brokerOpService;

    @GetMapping("/detail")
    @ApiOperation("获取broker配置详情")
    public ResultData<BrokerConfigResponse> describeBroker(@RequestParam @NotBlank int brokerId) {
        return brokerOpService.describeBroker(brokerId);
    }

    @GetMapping("/list")
    @ApiOperation("获取broker列表")
    public ResultData<BrokerListResponse> listBrokers(@RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
        return brokerOpService.listBrokers(pageIndex, pageSize);
    }

    @PostMapping("/update_config")
    @ApiOperation("更新broker配置")
    public ResultData<String> updateBrokerConfig(@RequestBody @Valid BrokerConfigReq brokerConfigReq) {
        return brokerOpService.updateBrokerConfig(brokerConfigReq);
    }

    @GetMapping("/download/tls/keystore")
    @ApiOperation("下载tls keystore")
    public void downloadTlsKey(HttpServletResponse response) {
        log.info("tls keystore download path: {}", tlsKeyStorePath);
        brokerOpService.downloadTLS(response, tlsKeyStorePath, "kafka.keystore.jks");
    }

    @GetMapping("/download/tls/truststore")
    @ApiOperation("下载tls truststore 证书")
    public void downloadCACrt(HttpServletResponse response) {
        log.info("tls truststore download path: {}", tlsTrustStorePath);
        brokerOpService.downloadTLS(response, tlsTrustStorePath, "kafka.truststore.jks");
    }

    @GetMapping("/tls/keystore/password")
    @ApiOperation("返回keystore密码")
    public ResultData<String> getKeyStorePassword() {
        return ResultData.success(keyStorePassword);
    }

    @GetMapping("/tls/truststore/password")
    @ApiOperation("返回truststore密码")
    public ResultData<String> getTrustStorePassword() {
        return ResultData.success(trustStorePassword);
    }


    @GetMapping("/resource/inspect")
    @ApiOperation("释放实例前对实例内的topic和消费组进行检查")
    public ResultData<ResourceInspectBean> inspectResourceBeforeDeletion() {
        List<String> warnings = new ArrayList<>();
        try {
            if (IfExistAnyTopic()) {
                warnings.add("实例内还存在topic，请删除全部topic后再释放实例");
            }
            if (ifExistAnyAvailableConsumerGroup()) {
                warnings.add("实例内还存在在线的消费组，请删除或下线全部消费组再释放实例");
            }
        } catch (Exception e) {
            log.error("exception: ", e);
        }
        if (warnings.isEmpty()) {
            return ResultData.success(new ResourceInspectBean(true, "允许释放该实例"));
        } else {
            return ResultData.success(new ResourceInspectBean(false, String.format("%s: %s", "不允许释放该实例，未通过资源检测", String.join("; ", warnings))));
        }
    }

    private boolean IfExistAnyTopic() throws Exception {
        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
        listTopicsOptions.listInternal(false);
        ListTopicsResult listTopicsResult = adminClient.listTopics(listTopicsOptions);
        return !listTopicsResult.listings().get().isEmpty();
    }

    private boolean ifExistAnyAvailableConsumerGroup() throws Exception {
        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
        Collection<ConsumerGroupListing> consumerGroupListings = listConsumerGroupsResult.all().get();
        for (ConsumerGroupListing consumerGroup : consumerGroupListings) {
            assert consumerGroup.state().isPresent();
            if (consumerGroup.state().get() == ConsumerGroupState.STABLE) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/list/producers")
    @ApiOperation("列出所有的生产者信息")
    public ResultData<ProducersResponse> listProducers(@RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "20") int pageSize, @RequestParam(defaultValue = "") String ip) {
        return brokerOpService.listProducers(pageIndex, pageSize, ip);
    }

    @GetMapping("/list/consumers")
    @ApiOperation("列出所有的消费者信息")
    public ResultData<ConsumersResponse> listConsumers(@RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "20") int pageSize, @RequestParam(defaultValue = "") String ip) {
        return brokerOpService.listConsumers(pageIndex, pageSize, ip);
    }
}
