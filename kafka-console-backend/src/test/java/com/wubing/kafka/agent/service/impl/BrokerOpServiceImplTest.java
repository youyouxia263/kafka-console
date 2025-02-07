package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.domain.request.BrokerConfigReq;
import com.wubing.kafka.agent.domain.response.BrokerConfigResponse;
import com.wubing.kafka.agent.domain.response.BrokerListResponse;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KafkaClientPool.class)
@PowerMockIgnore({"javax.management.*"})
public class BrokerOpServiceImplTest {

    @InjectMocks
    BrokerOpServiceImpl brokerOpService;

    @Before
    public void setUp(){
        PowerMockito.mockStatic(KafkaClientPool.class);
    }

    @Test
    public void testListBrokers() throws ExecutionException, InterruptedException {
        // 模拟AdminClient的响应
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1, "host1", 1000));
        nodes.add(new Node(2, "host2", 1001));

        AdminClient adminClient = Mockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        ResultData<BrokerListResponse> result1 = brokerOpService.listBrokers(1, 10);
        Assert.assertEquals(ReturnCode.RC201.getCode(), result1.getCode());
        DescribeClusterResult describeClusterResult = PowerMockito.mock(DescribeClusterResult.class);
        PowerMockito.when(adminClient.describeCluster()).thenReturn(describeClusterResult);
        KafkaFuture<Collection<Node>> kafkaFutureNodes =PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeClusterResult.nodes()).thenReturn(kafkaFutureNodes);
        PowerMockito.when(kafkaFutureNodes.get()).thenReturn(nodes);
        KafkaFuture<Node> kafkaFutureController =PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeClusterResult.controller()).thenReturn(kafkaFutureController);

        PowerMockito.when(kafkaFutureController.get()).thenReturn(new Node(1, "host1", 1000));

        // 调用
        ResultData<BrokerListResponse> result = brokerOpService.listBrokers(1, 10);
        Assert.assertEquals(ReturnCode.RC200.getCode(), result.getCode());
        // 验证
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().getTotalCount());
        assertEquals(2, result.getData().getItem().size());
        // 验证节点信息
        BrokerListResponse.BrokerInfo broker = result.getData().getItem().get(0);
        assertEquals(1, broker.getBrokerId());
        assertEquals("host1", broker.getHost());
        assertEquals(1000, broker.getPort());
    }

//     describeBroker方法测试
    @Test
    public void testDescribeBroker() {
        int brokerId = 1000;
        AdminClient adminClient = Mockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);

        ResultData<BrokerConfigResponse> success = brokerOpService.describeBroker(brokerId);
        Assert.assertEquals(ReturnCode.RC202.getCode(), success.getCode());


    }

    @Test
    public void testupdateBrokerConfig() throws ExecutionException, InterruptedException {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(1, "host1", 1000));
        nodes.add(new Node(2, "host2", 1001));
        BrokerConfigReq brokerConfigReq = new BrokerConfigReq();
        brokerConfigReq.setIsAll(true);
        AdminClient adminClient = Mockito.mock(AdminClient.class);
        PowerMockito.when(KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY)).thenReturn(adminClient);
        DescribeClusterResult describeClusterResult = PowerMockito.mock(DescribeClusterResult.class);
        PowerMockito.when(adminClient.describeCluster()).thenReturn(describeClusterResult);
        KafkaFuture<Collection<Node>> kafkaFutureNodes =PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(describeClusterResult.nodes()).thenReturn(kafkaFutureNodes);
        PowerMockito.when(kafkaFutureNodes.get()).thenReturn(nodes);

        ResultData<String> success1 = brokerOpService.updateBrokerConfig(brokerConfigReq);
        Assert.assertEquals(ReturnCode.RC203.getCode(), success1.getCode());

        Map<String, String> configs = new HashMap<>();
        configs.put("key1", "value1");
        brokerConfigReq.setConfigs(configs);
        AlterConfigsResult alterConfigsResult = PowerMockito.mock(AlterConfigsResult.class);
        PowerMockito.when(adminClient.incrementalAlterConfigs(any())).thenReturn(alterConfigsResult);
        KafkaFuture<Void> voidKafkaFuture = PowerMockito.mock(KafkaFuture.class);
        PowerMockito.when(alterConfigsResult.all()).thenReturn(voidKafkaFuture);

        ResultData<String> success = brokerOpService.updateBrokerConfig(brokerConfigReq);
        Assert.assertEquals(ReturnCode.RC200.getCode(), success.getCode());
    }
}
