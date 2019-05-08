package com.taobao.yugong.common.db.meta;

import com.taobao.yugong.common.utils.YuGongToStringStyle;

import lombok.Data;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.sql.JDBCType;

/**
 * 代表一个字段的信息
 *
 * @author agapple 2013-9-3 下午2:46:32
 * @since 3.0.0
 */
public class ColumnMeta implements Cloneable {

    @Getter
    private final String name;

    @Getter
    private final int type;

    public ColumnMeta(String columnName, int columnType) {
        this.name = columnName;
        this.type = columnType;
    }


    @Override
    public ColumnMeta clone() {
        return new ColumnMeta(this.name, this.type);
    }

}
