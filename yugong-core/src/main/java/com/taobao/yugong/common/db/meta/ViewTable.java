package com.taobao.yugong.common.db.meta;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 视图表信息
 *
 * @author agapple 2013-9-3 下午3:02:15
 * @since 3.0.0
 */
public class ViewTable extends Table {

    /**
     * 视图表名
     */
    @Getter
    @Setter
    private String viewName;
    /**
     * 视图字段
     */
    @Getter
    @Setter
    private List<ColumnMeta> viewColumns;
    /**
     * 原始表主键的index信息
     */
    @Getter
    @Setter
    private String primaryKeyIndex;

    public ViewTable(String type, String schema, String name) {
        super(type, schema, name);
    }

    public ViewTable(String type, String schema, String name, List<ColumnMeta> primaryKeys, List<ColumnMeta> columns) {
        super(type, schema, name, primaryKeys, columns);
    }

}
