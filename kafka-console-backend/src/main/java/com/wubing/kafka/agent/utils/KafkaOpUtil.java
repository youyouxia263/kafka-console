package com.wubing.kafka.agent.utils;

import com.wubing.kafka.agent.config.KafkaClientPool;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class KafkaOpUtil {
    public static void opKafkaConfig(ConfigResource.Type opType, String opName, Map<String, String> configMap) throws ExecutionException, InterruptedException {

        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        ConfigResource resource = new ConfigResource(opType, opName);
        Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>();
        List<AlterConfigOp> alterConfigOpList = new ArrayList<>();
        configMap.forEach((key, value) -> {
            ConfigEntry configEntry = new ConfigEntry(key, value);
            AlterConfigOp alterConfigOp = new AlterConfigOp(configEntry, AlterConfigOp.OpType.SET);
            alterConfigOpList.add(alterConfigOp);
        });
        configs.put(resource, alterConfigOpList);
        adminClient.incrementalAlterConfigs(configs).all().get();

    }

    public static void opKafkaConfigForNameList(ConfigResource.Type opType, List<String> opNameList, Map<String, String> configMap) throws ExecutionException, InterruptedException {

        AdminClient adminClient = KafkaClientPool.getAdminClient(KafkaClientPool.ADMIN_KEY);
        Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>();
        List<AlterConfigOp> alterConfigOpList = new ArrayList<>();
        configMap.forEach((key, value) -> {
            ConfigEntry configEntry = new ConfigEntry(key, value);
            AlterConfigOp alterConfigOp = new AlterConfigOp(configEntry, AlterConfigOp.OpType.SET);
            alterConfigOpList.add(alterConfigOp);
        });
        opNameList.parallelStream().forEach(opName -> {
            ConfigResource resource = new ConfigResource(opType, opName);
            configs.put(resource, alterConfigOpList);
        });
        adminClient.incrementalAlterConfigs(configs).all().get();

    }

    public static AclPermissionType getKafkaAclPermissionType (int aclPermissionType) {
        return AclPermissionType.fromCode((byte) aclPermissionType);
    }

    public static AclOperation getKafkaAclOperation (int aclPermissionType) {
        return AclOperation.fromCode((byte) aclPermissionType);
    }

    public static ResourceType getKafkaResourceType (int resourceType) {
        return ResourceType.fromCode((byte) resourceType);
    }

    public static PatternType getKafkaResourcePatternType (int resourcePatternType) {
        return PatternType.fromCode((byte) resourcePatternType);
    }

    public static Map<String, String> parseConfig2Map(Config config) {
        Map<String, String> configMap = new HashMap<>();
        config.entries().parallelStream().forEach(key -> configMap.put(key.name(),key.value()));
        return configMap;
    }
}
