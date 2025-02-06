package com.wubing.kafka.agent.domain.response;

import com.wubing.kafka.agent.domain.entity.ProducerClientInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProducersResponse {

    @ApiModelProperty("生成者链接总数")
    private int totalCount;
    @ApiModelProperty("生产者链接列表信息")
    private List<ProducersInfo> item;

    @Data
    public static class ProducersInfo {
        private String ip;
        private int connectionCount;
        private Map<Integer, String> brokerIdMap;
        private List<ProducerClientInfo> producerClientInfoList;
    }
}
