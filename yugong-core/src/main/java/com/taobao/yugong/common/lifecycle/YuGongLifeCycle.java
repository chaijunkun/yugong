package com.taobao.yugong.common.lifecycle;

/**
 * 对应的lifecycle控制接口
 * @author agapple 2013-9-12 下午2:19:56
 */
public interface YuGongLifeCycle {

    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 异常stop的机制
     * @param why
     * @param e
     */
    void abort(String why, Throwable e);

    /**
     * 判断是否正在运行
     * @return
     */
    boolean isStart();

    /**
     * 判断是否已停止
     * @return
     */
    boolean isStop();

}
