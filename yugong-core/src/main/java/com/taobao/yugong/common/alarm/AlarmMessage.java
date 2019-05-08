package com.taobao.yugong.common.alarm;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author agapple 2014年2月25日 下午11:38:06
 * @since 1.0.0
 */
@Data
@NoArgsConstructor(access =  AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class AlarmMessage implements Serializable {

  private static final long serialVersionUID = 6110474591366995515L;

  private String message;

  private String receiveKey;

}
