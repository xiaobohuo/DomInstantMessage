package com.dom.ination.domforandroid.component.orm.extra;

import java.lang.reflect.Field;

/**
 * Created by 10174987 on 2016/8/29.
 */

public class TableColumn {
    private String dataType;
    private Field field;
    private String column;
    private String columnType;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }
}
