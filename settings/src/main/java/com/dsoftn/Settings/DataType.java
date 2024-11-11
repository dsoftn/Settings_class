package com.dsoftn.Settings;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


public enum DataType {

    UNDEFINED(1),
    CHAR(2),
    STRING(4),
    INTEGER(8),
    LONG(16),
    FLOAT(32),
    DOUBLE(64),
    BOOLEAN(128),
    LIST(256),
    MAP(512),
    DATE(1024),
    TIME(2048),
    DATETIME(4096),
    NULL(8192);

    private final int value;
    
    DataType(int value) {
        this.value = value;
    }

    /**
     * Get integer value of this DataType
     * @return <b>int</b> <i>value</i> - integer value
     */
    public int getValue() {
        return value;
    }

    public static DataType getDataTypeForObject(Object object) {
        if (object instanceof Character) {
            return DataType.CHAR;
        } else if (object instanceof String) {
            return DataType.STRING;
        } else if (object instanceof Integer) {
            return DataType.INTEGER;
        } else if (object instanceof Long) {
            return DataType.LONG;
        } else if (object instanceof Float) {
            return DataType.FLOAT;
        } else if (object instanceof Double) {
            return DataType.DOUBLE;
        } else if (object instanceof Boolean) {
            return DataType.BOOLEAN;
        } else if (object instanceof List) {
            return DataType.LIST;
        } else if (object instanceof Map) {
            return DataType.MAP;
        } else if (object instanceof LocalDate) {
            return DataType.DATE;
        } else if (object instanceof LocalTime) {
            return DataType.TIME;
        } else if (object instanceof LocalDateTime) {
            return DataType.DATETIME;
        } else if (object == null) {
            return DataType.NULL;
        } else {
            return DataType.UNDEFINED;
        }
    }

    /**
     * Get DataType object from integer value
     * <p>If value is not found, null is returned</p>
     * @param value - <b>Number</b> <i>value</i> - any number
     * @return <b>DataType</b> <i>type</i> - DataType object
     * @see DataType#toInteger(DataType)
     */
    public static DataType fromNumberValue(Number value) {
        int intValue = value.intValue();
        for (DataType type : DataType.values()) {
            if (type.getValue() == intValue) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get integer value from DataType object
     * @param type - <b>DataType</b> <i>type</i> - DataType object
     * @return <b>int</b> <i>value</i> - integer value
     * @see DataType#fromNumberValue(int)
     */
    public static int toInteger(DataType type) {
        return type.getValue();
    }

    /**
     * Get DataType object from name
     * <p>If name is not found, null is returned</p>
     * @param name - <b>String</b> <i>name</i> - name
     * @return <b>DataType</b> <i>type</i> - DataType object
     */
    public static DataType fromName(String name) {
        for (DataType type : DataType.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    

}
