package com.taobao.yugong.cli;

import lombok.Getter;

/**
 * 参数定义
 * @author chaijunkun
 */
public enum ArgDef {

    config("c", "config", "config_file", "指定配置文件"),
    yaml("y", "yaml", "yaml_file","指定yaml配置文件"),
    help("h", "help", null, "获取帮助信息");

    @Getter
    private String shortName;

    @Getter
    private String longName;

    @Getter
    private String argVal;

    @Getter
    private String desc;

    ArgDef(String shortName, String longName, String argVal, String desc) {
        this.shortName = shortName;
        this.longName = longName;
        this.argVal = argVal;
        this.desc = desc;
    }

}
