package com.wubing.kafka.agent.domain.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExportTopicsReq {
    private boolean exportAll;
    
    private List<String> exportedTopicList;
}
