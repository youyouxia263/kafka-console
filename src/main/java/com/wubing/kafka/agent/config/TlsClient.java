package com.wubing.kafka.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.tls-client")
@Data
public class TlsClient {
    private String keystore;
    private String truststore;
}
