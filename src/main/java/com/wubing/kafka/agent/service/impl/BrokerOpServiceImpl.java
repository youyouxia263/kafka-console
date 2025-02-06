package com.wubing.kafka.agent.service.impl;

import com.wubing.kafka.agent.config.KafkaClientPool;
import com.wubing.kafka.agent.constants.KafkaOpConstants;
import com.wubing.kafka.agent.constants.ResponeMsg;
import com.wubing.kafka.agent.domain.entity.ClientTopicInfo;
import com.wubing.kafka.agent.domain.entity.ConsumerClientInfo;
import com.wubing.kafka.agent.domain.entity.ProducerClientInfo;
import com.wubing.kafka.agent.domain.request.BrokerConfigReq;
import com.wubing.kafka.agent.domain.response.BrokerConfigResponse;
import com.wubing.kafka.agent.domain.response.BrokerListResponse;
import com.wubing.kafka.agent.domain.response.ConsumersResponse;
import com.wubing.kafka.agent.domain.response.ProducersResponse;
import com.wubing.kafka.agent.service.BrokerOpService;
import com.wubing.kafka.agent.utils.KafkaOpUtil;
import com.wubing.kafka.agent.utils.ResultData;
import com.wubing.kafka.agent.utils.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ListConsumersResult;
import org.apache.kafka.clients.admin.ListProducersResult;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BrokerOpServiceImpl implements BrokerOpService {

    @Override
    public ResultData<BrokerListResponse> listBrokers(int pageIndex, int pageSize) {
        BrokerListResponse brokerListResponse = new BrokerListResponse();
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            Collection<Node> nodes = adminClient.describeCluster().nodes().get();
            Node controllerNode = adminClient.describeCluster().controller().get();
            brokerListResponse.setTotalCount(nodes.size());
            List<BrokerListResponse.BrokerInfo> brokerInfos = nodes.stream().map(node ->
                    new BrokerListResponse.BrokerInfo(node.id(), node.host(), 9094, controllerNode.id() == node.id())
            ).collect(Collectors.toList());
            if (brokerInfos.size() <= pageSize) {
                brokerListResponse.setItem(brokerInfos);
            } else {
                brokerListResponse.setItem(brokerInfos.subList((pageIndex - 1) * pageSize, Math.min((pageIndex * pageSize), brokerInfos.size())));
            }
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC201.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(brokerListResponse);
    }
     @Override
    public ResultData<BrokerConfigResponse> describeBroker(int brokerId) {
        BrokerConfigResponse brokerConfigResponse = new BrokerConfigResponse();
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            ConfigResource brokerResource = new ConfigResource(ConfigResource.Type.BROKER, String.valueOf(brokerId));
            Config brokerConfig = adminClient.describeConfigs(Collections.singletonList(brokerResource)).values().get(brokerResource).get();
            brokerConfigResponse.setBrokerConfigByConfig(brokerConfig);


            Node controllerNode = adminClient.describeCluster().controller().get();
            Map<String, String> detailConfig = brokerConfigResponse.getBrokerConfig();
            if (controllerNode.id() == brokerId) {
                detailConfig.put("controller", "true");
            } else {
                detailConfig.put("controller", "false");
            }
            if (!detailConfig.containsKey(KafkaOpConstants.LOG_RETENTION_DISK_USAGE) || detailConfig.get(KafkaOpConstants.LOG_RETENTION_DISK_USAGE) == null) {

                long logDiskTotalSize = Long.parseLong(detailConfig.get(KafkaOpConstants.LOG_DISK_TOTAL_BYTES));
                long logRetentionTotalSize = Long.parseLong(detailConfig.get(KafkaOpConstants.LOG_RETENTION_TOTAL_BYTES));
                int logRetentionDiskUsage = 100;
                if (logDiskTotalSize > logRetentionTotalSize) {
                    logRetentionDiskUsage = (int) Math.round((double) logRetentionTotalSize * 100 / logDiskTotalSize);
                }
                detailConfig.put(KafkaOpConstants.LOG_RETENTION_DISK_USAGE, String.valueOf(logRetentionDiskUsage));
            }
            brokerConfigResponse.setBrokerConfig(detailConfig);
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC202.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(brokerConfigResponse);
    }

    @Override
    public ResultData<String> updateBrokerConfig(BrokerConfigReq brokerConfigReq) {
        try {
            // todo:增加config判断，如果value值未改变则不变更
//            BrokerConfigResponse oldBrokerConfig = describeBroker(brokerConfigReq.getBrokerId()).getData();
//            if (compareBrokerConfigIsTheSame(oldBrokerConfig.getBrokerConfig(), brokerConfigReq.getConfigs())) {
//                return ResultData.success(ResponeMsg.NO_NEED_TO_UPDATE_BROKER_CONFIG);
//            }
            // 将滚动分片时长从小时转换成毫秒
            if (brokerConfigReq.getConfigs().containsKey("log.roll.hours")) {
                int segmentRollHours = Integer.parseInt(brokerConfigReq.getConfigs().get("log.roll.hours"));
                brokerConfigReq.getConfigs().put("log.roll.ms", String.valueOf(segmentRollHours * 3600 * 1000));
                brokerConfigReq.getConfigs().remove("log.roll.hours");
            }
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);

            if (brokerConfigReq.getConfigs().containsKey(KafkaOpConstants.LOG_RETENTION_DISK_USAGE)) {
                ConfigResource brokerResource = new ConfigResource(ConfigResource.Type.BROKER, KafkaOpConstants.BROKER_ID_1000);
                Config brokerConfig = adminClient.describeConfigs(Collections.singletonList(brokerResource)).values().get(brokerResource).get();
                Map<String, String> configMap = KafkaOpUtil.parseConfig2Map(brokerConfig);
                long logDiskTotalBytes = Long.parseLong(configMap.get(KafkaOpConstants.LOG_DISK_TOTAL_BYTES));
                int logDiskUsage = Integer.parseInt(brokerConfigReq.getConfigs().get(KafkaOpConstants.LOG_RETENTION_DISK_USAGE));
                long logRetentionTotalBytes = logDiskTotalBytes * logDiskUsage / 100;
                brokerConfigReq.getConfigs().put(KafkaOpConstants.LOG_RETENTION_TOTAL_BYTES, String.valueOf(logRetentionTotalBytes));
//                brokerConfigReq.getConfigs().remove(KafkaOpConstants.LOG_RETENTION_DISK_USAGE);
           }
            if (Boolean.TRUE.equals(brokerConfigReq.getIsAll())) {
                Collection<Node> nodes = adminClient.describeCluster().nodes().get();
                List<String> brokerIdList = nodes.stream().map(Node::idString).collect(Collectors.toList());
                KafkaOpUtil.opKafkaConfigForNameList(ConfigResource.Type.BROKER, brokerIdList, brokerConfigReq.getConfigs());
            } else {
                KafkaOpUtil.opKafkaConfig(ConfigResource.Type.BROKER, String.valueOf(brokerConfigReq.getBrokerId()), brokerConfigReq.getConfigs());
            }
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC203.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(ResponeMsg.UPDATE_BROKER_CONFIG_SUCCESS);
    }

    /**
     * 统一文件的下载处理逻辑
     *
     * @param response  响应
     * @param actualPath    实际文件路径
     * @param fileName  下载后的文件名
     */
    @Override
    public void downloadTLS(HttpServletResponse response, String actualPath, String fileName) {
        // 设置响应头，指定文件名
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        File canonicalFile;
        try {
            canonicalFile = new File(actualPath + fileName).getCanonicalFile();
            if (!canonicalFile.toPath().startsWith(actualPath.substring(0, actualPath.indexOf(fileName)))){
                log.warn("存在路径穿透风险: {}", actualPath);
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path tlsPath = Paths.get(canonicalFile.getPath().substring(0, canonicalFile.getPath().indexOf(fileName) + fileName.length()));
        try (InputStream inputStream = Files.newInputStream(tlsPath)) {
            // 创建StreamingResponseBody对象，将文件内容写入响应输出流
            byte[] bytes = getTlsEncodedStream(inputStream);
            // 返回StreamingResponseBody对象
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
        } catch (Exception e) {
            log.error("download tls file error");
            throw new RuntimeException(e);
        }
    }

    private static byte[] getTlsEncodedStream(InputStream inputStream) throws IOException {
        byte[] allBuffer = new byte[40960];
        byte[] buffer = new byte[4096];
        int size = 0;
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            System.arraycopy(buffer,0, allBuffer, size, bytesRead);
            size += bytesRead;
        }
        byte[] newBuffer = new byte[size];
        System.arraycopy(allBuffer,0, newBuffer,0, size);
        String iso88591String = new String(newBuffer, StandardCharsets.ISO_8859_1);
        String base64String = Base64.getEncoder().encodeToString(iso88591String.getBytes(StandardCharsets.UTF_8));
        return base64String.getBytes(StandardCharsets.UTF_8);
    }

    private boolean compareBrokerConfigIsTheSame(Map<String, String> oldBrokerConfig, Map<String, String> brokerConfig) {
        for (String key : brokerConfig.keySet()) {
            if (!oldBrokerConfig.containsKey(key) || !(oldBrokerConfig.get(key).equals(brokerConfig.get(key)))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ResultData<ProducersResponse> listProducers(int pageIndex, int pageSize, String ip) {
        ProducersResponse response = new ProducersResponse();
        List<ProducersResponse.ProducersInfo> item;
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            List<ListProducersResult.ProducerInfo> listProducersResult = adminClient.listProducers().all().get();

            Map<String, ProducersResponse.ProducersInfo> producersInfoResponseMap = new HashMap<>();

            listProducersResult.parallelStream().forEach(producerInfo -> {
                if (ip == null || producerInfo.clientHost().contains(ip)) {

                    String clientHost = producerInfo.clientHost();
                    ProducersResponse.ProducersInfo producersInfoResponse = producersInfoResponseMap.getOrDefault(clientHost, new ProducersResponse.ProducersInfo());
                    Map<Integer, String> brokerIdMap = producersInfoResponse.getBrokerIdMap() == null ? new HashMap<>() : producersInfoResponse.getBrokerIdMap();
                    List<String> ipPortPair = Arrays.asList(producerInfo.connectionId().split("-")[0].split(":"));
                    brokerIdMap.put(producerInfo.brokerId(), ipPortPair.get(0));
                    producersInfoResponse.setIp(clientHost);
                    producersInfoResponse.setBrokerIdMap(brokerIdMap);
                    ProducerClientInfo producerClientInfo = new ProducerClientInfo();
                    producerClientInfo.setClientId(producerInfo.clientId());
                    producerClientInfo.setConnectionId(producerInfo.connectionId());
                    producerClientInfo.setClientHost(clientHost);
                    producerClientInfo.setAcks(producerInfo.acks());
                    producerClientInfo.setUserName(producerInfo.userName());
                    producerClientInfo.setSecurityProtocol(producerInfo.securityProtocol());
                    producerClientInfo.setTopics(producerInfo.topics().stream()
                            .map(topicPartition -> new ClientTopicInfo(topicPartition.topic(), topicPartition.partition(), topicPartition.brokerId()))
                            .collect(Collectors.toList()));
                    producerClientInfo.setBrokerId(producerInfo.brokerId());
                    producerClientInfo.setTimeoutMs(producerInfo.timeoutMs());

                    List<ProducerClientInfo> producerClientInfoList = producersInfoResponse.getProducerClientInfoList();
                    if (producerClientInfoList == null) {
                        producerClientInfoList = new ArrayList<>();
                        producersInfoResponse.setProducerClientInfoList(producerClientInfoList);
                    }
                    producerClientInfoList.add(producerClientInfo);
                    producersInfoResponse.setConnectionCount(producerClientInfoList.size());
                    producersInfoResponseMap.put(clientHost, producersInfoResponse);
            }
            });

            item = new ArrayList<>(producersInfoResponseMap.values());
            response.setTotalCount(item.size());
            if (item.size() <= pageSize)
            {
                response.setItem(item);
            }else {
                response.setItem(item.subList((pageIndex - 1) * pageSize, Math.min((pageIndex * pageSize), item.size())));
            }
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC204.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(response);
    }

    @Override
    public ResultData<ConsumersResponse> listConsumers(int pageIndex, int pageSize, String ip){
        ConsumersResponse response = new ConsumersResponse();
        List<ConsumersResponse.ConsumersInfo> item;
        try {
            AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
            List<ListConsumersResult.ConsumerInfo> listConsumersResult = adminClient.listConsumers().all().get();

            Map<String, ConsumersResponse.ConsumersInfo> consumersResponseMap = new HashMap<>();

            listConsumersResult.parallelStream().forEach(consumerInfo -> {
                if (ip == null || consumerInfo.clientHost().contains(ip)) {
                    String consumerId = consumerInfo.clientId(); // Assuming clientId is used as consumerId
                    ConsumersResponse.ConsumersInfo consumersInfo = consumersResponseMap.getOrDefault(consumerId, new ConsumersResponse.ConsumersInfo());
                    consumersInfo.setConsumerId(consumerId);
                    consumersInfo.setIp(consumerInfo.clientHost());

                    Map<Integer, String> brokerIdMap = consumersInfo.getBrokerIdMap() == null ? new HashMap<>() : consumersInfo.getBrokerIdMap();
                    List<String> ipPortPair = Arrays.asList(consumerInfo.connectionId().split("-")[0].split(":"));
                    brokerIdMap.put(consumerInfo.brokerId(), ipPortPair.get(0));
                    consumersInfo.setBrokerIdMap(brokerIdMap);

                    ConsumerClientInfo consumerClientInfo = new ConsumerClientInfo();
                    consumerClientInfo.setBrokerId(consumerInfo.brokerId());
                    consumerClientInfo.setClientId(consumerInfo.clientId());
                    consumerClientInfo.setConnectionId(consumerInfo.connectionId());
                    consumerClientInfo.setUserName(consumerInfo.userName());
                    consumerClientInfo.setClientHost(consumerInfo.clientHost());
                    consumerClientInfo.setSecurityProtocol(consumerInfo.securityProtocol());
                    consumerClientInfo.setTopics(consumerInfo.topics().stream()
                            .map(topicPartition -> new ClientTopicInfo(topicPartition.topic(), topicPartition.partition(), topicPartition.brokerId()))
                            .collect(Collectors.toList()));
                    consumerClientInfo.setMaxWaitMs(consumerInfo.maxWaitMs());
                    consumerClientInfo.setMinBytes(consumerInfo.minBytes());
                    consumerClientInfo.setMaxBytes(consumerInfo.maxBytes());

                    List<ConsumerClientInfo> consumerClientInfoList = consumersInfo.getConsumerClientInfoList();
                    if (consumerClientInfoList == null) {
                        consumerClientInfoList = new ArrayList<>();
                        consumersInfo.setConsumerClientInfoList(consumerClientInfoList);
                    }
                    consumerClientInfoList.add(consumerClientInfo);
                    consumersInfo.setConsumerClientInfoList(consumerClientInfoList);
                    consumersInfo.setConnectionCount(consumerClientInfoList.size());
                    consumersResponseMap.put(consumerId, consumersInfo);
                }
            });

            item = new ArrayList<>(consumersResponseMap.values());
            response.setTotalCount(item.size());
            if (item.size() <= pageSize)
            {
                response.setItem(item);
            }else {
                response.setItem(item.subList((pageIndex - 1) * pageSize, Math.min((pageIndex * pageSize), item.size())));
            }
        } catch (Exception e) {
            log.error("exception: ", e);
            return ResultData.fail(ReturnCode.RC205.getCode(), e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }
        return ResultData.success(response);
    }

}
