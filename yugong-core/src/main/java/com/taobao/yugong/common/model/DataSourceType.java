package com.taobao.yugong.common.model;

import lombok.Getter;

/**
 * 数据源类型，同步时需要指定来源和目标，因此用枚举类型规范化配置
 * @author chaijunkun
 */
public enum DataSourceType {
    source("source"),
    target("target");

    @Getter
    private String configKey;

    DataSourceType(String configKey) {
        this.configKey = configKey;
    }

}
