package com.dsoftn.Settings;

import java.util.Arrays;

/**
 * <b>SettingType</b> - enum for setting types
 * <p>SettingType can be combined with other SettingType values using <i>combineValues</i> method</p>
 * <p>SettingType can be checked for specific type using <i>hasType</i> method</p>
 * <p>SettingType can be converted to integer value using <i>getValue</i> method</p>
 */
public enum SettingType {
    NONE(1),
    APP(2),
    USER(4),
    DEFAULT(8);

    
    private final int value;

    // Constructor

    SettingType(int value) {
        this.value = value;
    }

    // Get integer value

    /**
     * Get integer value of this SettingType
     * @return <b>int</b> <i>value</i> - integer value
     */
    public int getValue() {
        return value;
    }

    // Methods

    /**
     * Combine multiple SettingType values into one
     * <p>Example: combineValues(SettingType.APP, SettingType.USER) = 6</p>
     * @param types - <b>SettingType[]</b> <i>types</i> - array of SettingType values
     * @return <b>int</b> <i>value</i> - combined integer value
     */
    public static int combineValues(SettingType ... types) {
        // Convert types to integer values and use combineValues method that takes integer values
        int[] integerValues = new int[types.length];

        for (int i = 0; i < types.length; i++) {
            integerValues[i] = types[i].getValue();
        }

        return combineValues(integerValues);
    }

    /**
     * Combine multiple integer values into one
     * <p>Example: combineValues(1, 2, 4) = 7</p>
     * @param values - <b>int[]</b> <i>values</i> - array of integer values
     * @return <b>int</b> <i>value</i> - combined integer value
     */
    public static int combineValues(int ... values) {
        int combinedValue = 0;

        for (int value : values) {
            combinedValue |= value;
        }

        return combinedValue;
    }

    /**
     * Get SettingType array from integer value
     * <p>Example: getContent(6) = {SettingType.APP, SettingType.USER}</p>
     * @param value - <b>int</b> <i>value</i> - integer value
     * @return <b>SettingType[]</b> <i>types</i> - array of SettingType values
     */
    public static SettingType[] getContent (int value) {
        return Arrays.stream(values())
                .filter(type -> (value & type.getValue()) != 0)
                .toArray(SettingType[]::new);
    }

    /**
     * Get SettingType object from integer value
     * <p>If value is not found, returns null</p>
     * @param value - <b>int</b> <i>value</i> - integer value
     * @return <b>SettingType</b> <i>type</i> - SettingType object
     * @see toInteger
     */
    public static SettingType fromInteger(int value) {
        for (SettingType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get integer value from SettingType object
     * @param type - <b>SettingType</b> <i>type</i> - SettingType object
     * @return <b>int</b> <i>value</i> - integer value
     * @see fromInteger
     */
    public static int toInteger(SettingType type) {
        return type.getValue();
    }

    /**
     * Get SettingType object from name
     * <p>If name is not found, returns null</p>
     * @param name - <b>String</b> <i>name</i> - name of SettingType
     * @return <b>SettingType</b> <i>type</i> - SettingType object
     */
    public static SettingType fromName(String name) {
        for (SettingType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    // Checkers
    
    /**
     * Check if this SettingType contains specified type
     * <p>Example: hasType(SettingType.APP, SettingType.USER) = false</p>
     * @param typeToExamine - <b>SettingType</b> or <b>int</b> <i>typeToExamine</i> - type to examine
     * @param requiredType - <b>SettingType</b> or <b>int</b> <i>requiredType</i> - type to check for
     * @return <b>boolean</b> <i>true</i> if this SettingType has specified type, <i>false</i> otherwise
     */
    public static boolean hasType(SettingType typeToExamine, SettingType requiredType) {
        return hasType(typeToExamine.getValue(), requiredType.getValue());
    }

    /**
     * Check if this SettingType contains specified type
     * <p>Example: hasType(SettingType.APP, SettingType.USER) = false</p>
     * @param typeToExamine - <b>SettingType</b> or <b>int</b> <i>typeToExamine</i> - type to examine
     * @param requiredType - <b>SettingType</b> or <b>int</b> <i>requiredType</i> - type to check for
     * @return <b>boolean</b> <i>true</i> if this SettingType has specified type, <i>false</i> otherwise
     */
    public static boolean hasType(SettingType typeToExamine, int requiredType) {
        return hasType(typeToExamine.getValue(), requiredType);
    }

    /**
     * Check if this SettingType contains specified type
     * <p>Example: hasType(SettingType.APP, SettingType.USER) = false</p>
     * @param typeToExamine - <b>SettingType</b> or <b>int</b> <i>typeToExamine</i> - type to examine
     * @param requiredType - <b>SettingType</b> or <b>int</b> <i>requiredType</i> - type to check for
     * @return <b>boolean</b> <i>true</i> if this SettingType has specified type, <i>false</i> otherwise
     */
    public static boolean hasType(int typeToExamine, SettingType requiredType) {
        return hasType(typeToExamine, requiredType.getValue());
    }

    /**
     * Check if this SettingType contains specified type
     * <p>Example: hasType(SettingType.APP, SettingType.USER) = false</p>
     * @param typeToExamine - <b>SettingType</b> or <b>int</b> <i>typeToExamine</i> - type to examine
     * @param requiredType - <b>SettingType</b> or <b>int</b> <i>requiredType</i> - type to check for
     * @return <b>boolean</b> <i>true</i> if this SettingType has specified type, <i>false</i> otherwise
     */
    public static boolean hasType(int typeToExamine, int requiredType) {
        return (typeToExamine & requiredType) == requiredType;
    }

}
