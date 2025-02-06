package com.wubing.kafka.agent.domain.entity;

import lombok.Data;

@Data
public class ConsumerGroupInfo {
    private String groupId;
    private String state;
    private int memberNum;
    private long lag;

    public ConsumerGroupInfo(String groupId, String state, int memberNum, long lag){
        this.groupId = groupId;
        this.state = state;
        this.memberNum = memberNum;
        this.lag = lag;
    }
    public  ConsumerGroupInfo(String groupId, String state){
        this.groupId = groupId;
        this.state = state;
    }
}
