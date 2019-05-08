package com.taobao.yugong.common.db.meta;

import com.google.common.collect.Lists;
import com.taobao.yugong.common.utils.YuGongToStringStyle;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 代表一张数据表
 *
 * @author agapple 2013-9-3 下午2:51:56
 * @since 3.0.0
 */
@Data
@EqualsAndHashCode(exclude = {"primaryKeys", "columns", "extKey"})
public class Table implements Serializable {

    private final String type;
    private final String schema;
    private String name;

    private List<ColumnMeta> primaryKeys;
    private List<ColumnMeta> columns;

    /**
     * 增量必带的扩展字段,比如DRDS模式下的拆分键
     */
    private String extKey;

    public Table(String type, String schema, String name) {
        this.type = type;
        this.schema = schema;
        this.name = name;
        this.primaryKeys = Lists.newArrayList();
        this.columns = Lists.newArrayList();
    }

    public Table(String type, String schema, String name, List<ColumnMeta> primaryKeys, List<ColumnMeta> columns) {
        this.type = type;
        this.schema = schema;
        this.name = name;
        this.primaryKeys = primaryKeys;
        this.columns = columns;
    }

    public void addPrimaryKey(ColumnMeta primaryKey) {
        this.primaryKeys.add(primaryKey);
    }

    public void addColumn(ColumnMeta column) {
        this.columns.add(column);
    }

    /**
     * 返回所有字段信息，包括主键
     */
    public List<ColumnMeta> getColumnsWithPrimary() {
        List<ColumnMeta> result = Lists.newArrayList(primaryKeys);
        result.addAll(columns);
        return result;
    }

    public boolean isPrimaryKey(String columnName) {
        for (ColumnMeta col : primaryKeys) {
            if (StringUtils.equalsIgnoreCase(col.getName(), columnName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 返回schema.name
     */
    public String getFullName() {
        return schema + "." + name;
    }

}
