package com.wubing.kafka.agent.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchTopicsBean {
    
    private List<TopicBeanForImportAndExport> topics;
    
}
