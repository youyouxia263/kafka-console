package com.wubing.kafka.agent.constants;

public class KafkaOpConstants {
    // adminClient
    public static final int ADMIN_CLIENT_RETIES = 3;
    public static final int ADMIN_CLIENT_TIMEOUT_MS = 20000;

    // offset 操作
    public static final int RESET_OFFSET_BY_OFFSET = 0;
    public static final int RESET_OFFSET_BY_TIMESTAMP = 1;
    public static final int RESET_OFFSET_TO_EARLY = 2;
    public static final int RESET_OFFSET_TO_END = 3;
    // scram iterations
    public static final int SCRAM_ITERATIONS = 4096;
    // acl 权限分配类型
    public static final int ACL_PRODUCER_TYPE = 0;
    public static final int ACL_CONSUMER_TYPE = 1;
    public static final int ACL_USER_DEFINE_TYPE = 2;

    public static final int ACL_RESOURCE_TYPE_TOPIC = 2;
    public static final int ACL_RESOURCE_TYPE_GROUP = 3;
    public static final int ACL_RESOURCE_TYPE_CLUSTER = 4;
    public static final String ACL_RESOURCE_CLUSTER_NAME = "kafka-cluster";
    public static final String LOG_RETENTION_DISK_USAGE = "log.retention.disk.usage";
    public static final String LOG_DISK_TOTAL_BYTES = "log.disk.total.bytes";
    public static final String LOG_RETENTION_TOTAL_BYTES = "log.retention.total.bytes";
    public static final String BROKER_ID_1000 = "1000";
}
