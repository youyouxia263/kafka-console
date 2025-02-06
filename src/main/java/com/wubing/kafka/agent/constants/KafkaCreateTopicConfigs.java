package com.wubing.kafka.agent.constants;

import java.util.HashSet;
import java.util.Set;

public class KafkaCreateTopicConfigs {
    public static final String CLEANUP_POLICY_CONFIG = "cleanup.policy";
    public static final String MIN_INSYNC_REPLICAS_CONFIG = "min.insync.replicas";
    public static final String RETENTION_MS_CONFIG = "retention.ms";
    
    public static Set<String> listAllConfigs() {
       Set<String> configs = new HashSet<>();
       configs.add(CLEANUP_POLICY_CONFIG);
       configs.add(MIN_INSYNC_REPLICAS_CONFIG);
       configs.add(RETENTION_MS_CONFIG);
       return configs;
    }
}
