package com.wubing.kafka.agent.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResourceInspectBean {
    /**
     * 资源是否可以被释放
     */
    boolean ok;

    /**
     * 资源检测的返回通知
     */
    String message;
}
