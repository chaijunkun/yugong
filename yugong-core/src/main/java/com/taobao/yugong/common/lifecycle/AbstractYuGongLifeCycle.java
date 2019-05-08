package com.taobao.yugong.common.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基本实现
 *
 * @author agapple 2014年2月25日 下午11:38:06
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractYuGongLifeCycle implements YuGongLifeCycle {

  /** 是否处于运行中 */
  protected volatile boolean running = false;

  @Override
  public boolean isStart() {
    return running;
  }

  @Override
  public void start() {
    if (running) {
      return;
    }

    running = true;
  }

  @Override
  public void stop() {
    if (!running) {
      return;
    }

    running = false;
  }

  @Override
  public void abort(String why, Throwable e) {
    log.error("abort caused by " + why, e);
    stop();
  }

  @Override
  public boolean isStop() {
    return !isStart();
  }

}
