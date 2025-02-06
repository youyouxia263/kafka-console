package com.wubing.kafka.agent.config;

import com.wubing.kafka.agent.constants.KafkaOpConstants;
import com.wubing.kafka.agent.exception.KafkaClientException;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class KafkaClientPool {

    public static KafkaServerConfig kafkaServerConfig;

    @Resource
    private KafkaServerConfig tempKafkaServerConfig;

    @PostConstruct
    public void setKafkaServerConfig(){
        kafkaServerConfig = this.tempKafkaServerConfig;
    }

    /**
     * AdminClient
     */
    private static final Map<String, AdminClient> ADMIN_CLIENT_MAP = new ConcurrentHashMap<>();
    public static final String ADMIN_KEY = "adminClient";
    private static final ReentrantLock LOCK = new ReentrantLock();

    public static AdminClient getAdminClient(String key) {
        AdminClient adminClient = ADMIN_CLIENT_MAP.get(key);
        if (adminClient != null && checkAdminClientAlive(adminClient)) {
            return adminClient;
        }
        LOCK.lock();
        try {
            adminClient = ADMIN_CLIENT_MAP.get(key);
            if (adminClient != null && checkAdminClientAlive(adminClient)) {
                return adminClient;
            }
            ADMIN_CLIENT_MAP.putIfAbsent(key, createAdminClient());

        }catch (Exception e){
            log.error("create kafka admin client failed: ",e);
            throw new KafkaClientException(e.getMessage(), e.getCause());
        }finally {
            LOCK.unlock();
        }
        return ADMIN_CLIENT_MAP.get(key);
    }

    public static void closeAdminClient(String key){
        if (ADMIN_CLIENT_MAP.containsKey(key)) {
            ADMIN_CLIENT_MAP.get(key).close();
        }
    }


    public static boolean checkAdminClientAlive(AdminClient adminclient) {
        boolean result = true;
        try {
            adminclient.describeCluster().clusterId().get();
        } catch (Exception e) {
            result = false;
            log.warn("AdminClient is not alive, remove it in map");
            adminclient.close();
            ADMIN_CLIENT_MAP.remove(ADMIN_KEY);
        }
        return result;
    }

    public static KafkaConsumer<String, String> createConsumer(){
        Properties props = getCommonProperties();
        return new KafkaConsumer<>(props);
    }

    public static KafkaConsumer<String, String>  createConsumerByGroupId(String groupId){
        Properties props = getCommonProperties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return new KafkaConsumer<>(props);
    }

    public static Producer<String, String> createProducer(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerConfig.getServer());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafkaAgentProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        setSaslConfig(props);
        return new KafkaProducer<>(props);
    }

    private static Properties getCommonProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-agent-" + UUID.randomUUID());
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerConfig.getServer());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
        setSaslConfig(props);
        return props;
    }

    public static AdminClient createAdminClient(){
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerConfig.getServer());
        properties.put(AdminClientConfig.RETRIES_CONFIG, KafkaOpConstants.ADMIN_CLIENT_RETIES);
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, KafkaOpConstants.ADMIN_CLIENT_TIMEOUT_MS);
        setSaslConfig(properties);
        return AdminClient.create(properties);
    }

    public static void setSaslConfig(Properties properties){
        if (kafkaServerConfig.isAclEnable()) {
            properties.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            String jaasConfig = "org.apache.kafka.common.security.scram.ScramLoginModule required username='testUser' password='testPassWord';";
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig.replace("testUser",kafkaServerConfig.getSuperUser()).replace("testPassWord", kafkaServerConfig.getSuperUserPass()));
        } else {
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
        }
//        if (kafkaServerConfig.isSslEnable()) {
//            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaServerConfig.getTlsClient().getCaPath());
//            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaServerConfig.getTlsClient().getPassword());
//            properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, kafkaServerConfig.getTlsClient().getKeypairPath());
//            properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaServerConfig.getTlsClient().getPassword());
//            properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, kafkaServerConfig.getTlsClient().getPassword());
//        }
    }
}
