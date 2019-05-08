package com.taobao.yugong.common.utils.thread;

import org.slf4j.Logger;

/**
 * yugong捕捉异常处理器
 *
 * @author agapple
 */
public class YuGongUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Logger logger;

    public YuGongUncaughtExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("uncaught exception", e);
    }
}
