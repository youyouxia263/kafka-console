package com.wubing.kafka.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka")
@Data
public class KafkaServerConfig {
    private String server;
    private boolean aclEnable;
    private String superUser;
    private String superUserPass;

    private boolean sslEnable;
    private TlsClient tlsClient;
}
