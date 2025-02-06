package com.wubing.kafka.agent.constants;

public class ResponeMsg {
    public static final String CREATE_TOPIC_SUCCESS = "Create topic success!";
    public static final String DELETE_TOPIC_SUCCESS = "Delete topic success!";
    public static final String DELETE_CONSUMER_GROUP_SUCCESS = "Delete consumer group success!";
    public static final String UPDATE_TOPIC_CONFIG_SUCCESS = "Update topic config success!";
    public static final String UPDATE_PARTITION_COUNT_SUCCESS = "Update topic partition count success!";
    public static final String UPDATE_REPLICAS_COUNT_LITTLE_FAILED = "Update topic replicas is less than current replicas";
    public static final String NO_BROKERS_NUMS_FOR_REPLICAS_COUNT = "New replication factor cannot exceed the number of brokers in the cluster";
    public static final String UPDATE_BROKER_CONFIG_SUCCESS = "Update broker config success";
    public static final String NO_NEED_TO_UPDATE_BROKER_CONFIG = "Broker configs are the same as the previous ones, therefore no need to change";

    public static final String START_OFFSET_GREATER_THAN_OFFSET = "The start offset startOffset is larger than the offset setOffset, please larger than it!";
    public static final String NO_MESSAGE_BY_OFFSET = "The max offset is less than the offset: ";
    public static final String NO_SUPPORT_RESET_TYPE = "Current reset type is not supported";

    public static final String UPSERT_ACL_USER_SUCCESS = "Upsert acl user success!";
    public static final String DELETE_ACL_USER_SUCCESS = "Delete acl user success!";
    public static final String UPSERT_ACL_LIMITS_SUCCESS = "Create acl limits success!";
    public static final String DELETE_ACL_LIMITS_SUCCESS = "Delete acl limits success!";

    public static final String SEND_MESSAGE_SUCCESS = "Send message success!";
    public static final String IMPORT_TOPICS_SUCCESS = "Import topics success!";
    public static final String IMPORT_TOPICS_SUCCESS_CH = "导入Topic成功!";
}
