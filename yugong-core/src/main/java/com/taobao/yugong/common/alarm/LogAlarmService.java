package com.taobao.yugong.common.alarm;

import com.taobao.yugong.common.lifecycle.AbstractYuGongLifeCycle;

import lombok.extern.slf4j.Slf4j;

/**
 * @author agapple 2014年2月25日 下午11:38:06
 * @since 1.0.0
 */
@Slf4j
public class LogAlarmService extends AbstractYuGongLifeCycle implements AlarmService {

    @Override
    public void sendAlarm(AlarmMessage data) {
        log.error("Alarm:{} , Receiver:{}", new Object[]{data.getMessage(), data.getReceiveKey()});
    }

}
