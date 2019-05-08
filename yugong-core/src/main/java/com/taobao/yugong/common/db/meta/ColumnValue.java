package com.taobao.yugong.common.db.meta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 代表一个具体字段的value
 *
 * @author agapple 2013-9-3 下午2:47:33
 * @since 1.0.0
 */
@EqualsAndHashCode(exclude = {"check"})
public class ColumnValue implements Cloneable {

    @Getter
    @Setter
    private ColumnMeta column;

    @Getter
    @Setter
    private Object value;

    /**
     * 是否需要做数据对比
     */
    @Getter
    @Setter
    private boolean check = true;

    public ColumnValue() {
    }

    public ColumnValue(ColumnMeta column, Object value) {
        this(column, value, true);
    }

    public ColumnValue(ColumnMeta column, Object value, boolean check) {
        this.value = value;
        this.column = column;
        this.check = check;
    }

    @Override
    public ColumnValue clone() {
        ColumnValue column = new ColumnValue();
        column.setValue(this.value);
        column.setColumn(this.column.clone());
        return column;
    }

}
