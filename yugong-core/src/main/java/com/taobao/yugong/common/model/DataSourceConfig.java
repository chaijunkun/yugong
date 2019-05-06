package com.taobao.yugong.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Properties;

/**
 * 数据介质源信息描述
 *
 * @author agapple 2011-9-2 上午11:28:21
 */
@Data
public class DataSourceConfig implements Serializable {

  private static final long serialVersionUID = -7653632703273608373L;

  private String username;
  private String password;
  private String url;
  private DbType type;
  private String encode;
  private Properties properties = new Properties();

}
