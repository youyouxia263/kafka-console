package com.wubing.kafka.agent.domain.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class BrokerConfigReq {
    @NotNull
    @ApiModelProperty("broker id")
    private int brokerId;
    @ApiModelProperty("是否应用所有broker")
    private Boolean isAll;
    @ApiModelProperty("broker属性配置")
    @NotEmpty
    private Map<String, String> configs;
}
//    以下是 Kafka Broker 允许动态更改的常见配置项列表：
//        1. log.flush.interval.messages
//        2. log.flush.interval.ms
//        3. log.retention.bytes
//        4. num.recovery.threads.per.data.dir
//        5. unclean.leader.election.enable
//        6. num.io.threads：用于控制I/O线程的数量，可以通过Kafka API在运行时更改。
//        7. num.network.threads：用于控制网络线程的数量，可以通过Kafka API在运行时更改。
//        8. advertised.listeners：用于指定Kafka broker公开给客户端连接的地址和端口号，可以在运行时动态更改。
//        9. log.retention.ms：用于指定日志保留时间的长度（以毫秒为单位），可以在运行时动态更改。
//        10. log.segment.bytes：用于指定单个日志段的大小（以字节为单位），可以在运行时动态更改。
//        11. log.roll.ms: 用于指定日志滚动的时间间隔（以毫秒为单位）

//    以下是不允许动态更改，需要停掉broker修改的配置项列表：
//        1. log.roll.hours：用于指定日志滚动的时间间隔（以小时为单位），Kafka将在指定时间间隔后滚动生成新segment，即使segment大小还未达到log.segment.bytes。