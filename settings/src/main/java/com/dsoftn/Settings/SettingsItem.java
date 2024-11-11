package com.dsoftn.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.dsoftn.utils.PyDict;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class SettingsItem {
    // Constants
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private DateTimeFormatter dateTimeFormatterForJson = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Properties
    private String key = "";
    private String value = "";
    private String defaultValue = "";
    private String min = "null";
    private String max = "null";
    private int settingType = 0;
    private DataType dataType = DataType.UNDEFINED;
    private String description = "";
    private String creationDate = LocalDateTime.now().format(dateTimeFormatterForJson);
    private String userData = "";
    
    public boolean canBeSavedInFile = false;

    public SettingsItem() {}

    public SettingsItem(String key, Object value, Object defaultValue, int settingType, String description) {
        this(key, value, defaultValue, "null", "null", settingType, DataType.UNDEFINED, description);
    }

    public SettingsItem(String key, Object value, Object defaultValue, Object min, Object max, Integer settingType, DataType dataType, String description) {
        this.key = key;
        this.value = getStringFromObject(value);
        this.defaultValue = getStringFromObject(defaultValue);
        this.min = getStringFromObject(min);
        this.max = getStringFromObject(max);
        this.settingType = settingType;

        if (dataType == null) {
            this.dataType = getRecommendedDataTypeForString(this.value);
        }
        else {
            this.dataType = dataType;
        }
        this.description = description;
    }

    // Public methods

    public SettingsItem duplicate() {
        SettingsItem result = new SettingsItem();

        result.setKey(this.key);
        result.setValue(this.value);
        result.setDefaultValue(this.defaultValue);
        result.setMin(this.min);
        result.setMax(this.max);
        result.setSettingType(this.settingType);
        result.setDataType(this.dataType);
        result.setDescription(this.description);
        result.setCreationDate(this.getCreationDate());
        result.setUserData(this.userData);

        return result;
    }

    // Checkers

    public boolean isValid() {
        if (key == null || key.isEmpty()) {
            return false;
        }
        
        if (! isValidDataType(value)) {
            return false;
        }
        
        if (! isValidDataType(defaultValue)) {
            return false;
        }

        if (! isValidDataType(min) && ! isNull(min)) {
            return false;
        }

        if (! isValidDataType(max) && ! isNull(max)) {
            return false;
        }

        if (isValueInRange(value) == false) {
            return false;
        }

        if (isValueInRange(defaultValue) == false) {
            return false;
        }

        return true;
    }

    public Boolean isValueInRange(String value) {
        // If value cannot be converted to double, return true because it is not a number
        if (getDouble(value) == null) {
            return true;
        }

        // If min or max cannot be converted to double and they are not null, return null to indicate error
        if (getDouble(min) == null && ! isNull(min)) {
            return null;
        }
        if (getDouble(max) == null && ! isNull(max)) {
            return null;
        }
    
        Double valueDouble = getDouble(value);

        // Case where min and max are not set
        if (isNull(min) && isNull(max)) {
            return true;
        }
    
        // Case where only max is set
        if (isNull(min)) {
            return valueDouble <= getDouble(max);
        }
    
        // Case where only min is set
        if (isNull(max)) {
            return valueDouble >= getDouble(min);
        }
    
        // Case where both min and max are set
        double minDouble = getDouble(min);
        double maxDouble = getDouble(max);

        return valueDouble >= minDouble && valueDouble <= maxDouble;
    }
    
    public boolean isValueNULL() {
        return isNull(value);
    }

    public boolean isDefaultValueNULL() {
        return isNull(defaultValue);
    }

    // Serialization / Deserialization methods

    public Map<String, Object> toMap() {
        Map<String, Object> result = new PyDict();
        result.put("key", key);
        result.put("value", value);
        result.put("defaultValue", defaultValue);
        result.put("min", min);
        result.put("max", max);
        result.put("settingType", settingType);
        result.put("dataType", dataType.getValue());
        result.put("description", description);
        result.put("creationDate", creationDate);
        result.put("userData", userData);
        
        return result;
    }
    
    public void fromMap(Map<String, Object> map) {
        // Key
        key = (String) map.get("key");
        
        // Data type
        Number numDataTypeValue = (Number) map.get("dataType");
        int intDataTypeValue = (numDataTypeValue != null) ? numDataTypeValue.intValue() : DataType.UNDEFINED.getValue();
        dataType = DataType.fromNumberValue(intDataTypeValue);
        if (dataType == null) {
            printError("Invalid data type: " + intDataTypeValue);
            dataType = DataType.UNDEFINED;
        }

        // Value, default value, min and max
        value = (String) map.get("value");
        if (value == null) {
            value = "null";
        }
        defaultValue = (String) map.get("defaultValue");
        if (defaultValue == null) {
            defaultValue = "null";
        }
        min = (String) map.get("min");
        if (min == null) {
            min = "null";
        }
        max = (String) map.get("max");
        if (max == null) {
            max = "null";
        }

        Number numSettingTypeValue = (Number) map.get("settingType");
        int intSettingTypeValue = (numSettingTypeValue != null) ? numSettingTypeValue.intValue() : 0;
        settingType = intSettingTypeValue;
        
        description = (String) map.get("description");
        if (description == null) {
            description = "";
        }

        // Creation date
        String creationDateString = (String) map.get("creationDate");
        if (creationDateString != null) {
            creationDate = creationDateString;
        }
        else {
            creationDate = LocalDateTime.now().format(dateTimeFormatterForJson);
        }

        userData = (String) map.get("userData");

    }

    // SettingsType operations

    public boolean hasSettingType(SettingType settingType) {
        return (this.settingType & settingType.getValue()) != 0;
    }

    public void addSettingsType(SettingType settingType) {
        if (hasSettingType(settingType)) {
            removeSettingsType(settingType);
        }
        
        this.settingType |= settingType.getValue();
    }

    public void removeSettingsType(SettingType settingType) {
        this.settingType &= ~settingType.getValue();
    }

    public void clearSettingsType() {
        this.settingType = 0;
    }

    // Getters and setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns value object according to data type set
     */
    public Object getValue() {
        return getCorrectObjectAccordingToDataType(value);
    }

    /**
     * Set value will also set correct data type
     * @param value - <b>Object</b> <i>value</i> - any Object that can be converted to String
     */
    public void setValue(Object value) {
        this.value = getStringFromObject(value);
        dataType = getRecommendedDataTypeForString(this.value);
    }

    /**
     * Tries to convert <b>value</b> to <b>Object</b> regardless of data type set
     */
    public Object getValueOBJECT() {
        return getObjectFromString(value);
    }

    public Character getValueCHAR() {
        return getChar(value);
    }

    public String getValueSTRING() {
        return value;
    }

    public Integer getValueINT() {
        return getInteger(value);
    }

    public Long getValueLONG() {
        return getLong(value);
    }

    public Double getValueDOUBLE() {
        return getDouble(value);
    }

    public Float getValueFLOAT() {
        return getFloat(value);
    }

    public Boolean getValueBOOLEAN() {
        return getBoolean(value);
    }

    public List<Object> getValueLIST() {
        return getList(value);
    }

    public Map<String, Object> getValueMAP() {
        return getMap(value);
    }

    public PyDict getValuePYDICT() {
        return getMap(value);
    }

    public LocalDate getValueDATE() {
        return getDate(value);
    }

    public LocalTime getValueTIME() {
        return getTime(value);
    }

    public LocalDateTime getValueDATETIME() {
        return getDateTime(value);
    }

    /**
     * Returns default value object according to data type set
     */
    public Object getDefaultValue() {
        return getCorrectObjectAccordingToDataType(defaultValue);
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = getStringFromObject(defaultValue);
    }

    /**
     * Tries to convert <b>defaultValue</b> to <b>Object</b> regardless of data type set
     */
    public Object getDefaultValueOBJECT() {
        return getObjectFromString(defaultValue);
    }

    public Character getDefaultValueCHAR() {
        return getChar(defaultValue);
    }

    public String getDefaultValueSTRING() {
        return defaultValue;
    }

    public Integer getDefaultValueINT() {
        return getInteger(defaultValue);
    }

    public Long getDefaultValueLONG() {
        return getLong(defaultValue);
    }

    public Double getDefaultValueDOUBLE() {
        return getDouble(defaultValue);
    }

    public Float getDefaultValueFLOAT() {
        return getFloat(defaultValue);
    }

    public Boolean getDefaultValueBOOLEAN() {
        return getBoolean(defaultValue);
    }

    public List<Object> getDefaultValueLIST() {
        return getList(defaultValue);
    }

    public Map<String, Object> getDefaultValueMAP() {
        return getMap(defaultValue);
    }

    public PyDict getDefaultValuePYDICT() {
        return getMap(defaultValue);
    }

    public LocalDate getDefaultValueDATE() {
        return getDate(defaultValue);
    }

    public LocalTime getDefaultValueTIME() {
        return getTime(defaultValue);
    }

    public LocalDateTime getDefaultValueDATETIME() {
        return getDateTime(defaultValue);
    }

    /**
     * Returns min value object according to data type set
     */
    public Object getMin() {
        return getCorrectObjectAccordingToDataType(min);
    }

    public void setMin(Object min) {
        this.min = getStringFromObject(min);
    }

    public Integer getMinINT() {
        return getInteger(min);
    }

    public Double getMinDOUBLE() {
        return getDouble(min);
    }

    public String getMinSTRING() {
        return min;
    }

    /**
     * Returns max value object according to data type set
     */
    public Object getMax() {
        return getCorrectObjectAccordingToDataType(max);
    }

    public void setMax(Object max) {
        this.max = getStringFromObject(max);
    }

    public Integer getMaxINT() {
        return getInteger(max);
    }

    public Double getMaxDOUBLE() {
        return getDouble(max);
    }

    public String getMaxSTRING() {
        return max;
    }
    
    public int getSettingType() {
        return settingType;
    }

    public void setSettingType(int settingType) {
        this.settingType = settingType;
    }

    public void setSettingType(SettingType settingType) {
        this.settingType = settingType.getValue();
    }

    public DataType getDataType() {
        return dataType;
    }

    /**
     * Sets data type based on <i>value</i> object if no argument is passed
     */
    public void setDataType() {
        this.dataType = DataType.getDataTypeForObject(value);
    }
    
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getCanBeSavedInFile() {
        return canBeSavedInFile;
    }

    public void setCanBeSavedInFile(boolean canBeSavedInFile) {
        this.canBeSavedInFile = canBeSavedInFile;
    }

    /**
     * Returns easy to sort date+time in format <i>yyyy-MM-dd HH:mm:ss</i>
     * @return - <i>String</i> easy to sort date+time
     */
    public String getCreationDateForJson() {
        return creationDate;
    }

    /**
     * Returns date+time in format <i>dd.MM.yyyy HH:mm:ss</i>
     * @return - <i>String</i> date+time
     */
    public String getCreationDate() {
        try {
            return LocalDateTime.parse(creationDate, dateTimeFormatterForJson).format(dateTimeFormatter);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets creation date+time
     * @param creationDate - <i>String</i> date+time in format <i>dd.MM.yyyy HH:mm:ss</i>
     */
    public void setCreationDate(String creationDate) {
        try {
            this.creationDate = LocalDateTime.parse(creationDate, dateTimeFormatter).format(dateTimeFormatterForJson);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets current date+time
     */
    public void setCreationDate() {
        this.creationDate = LocalDateTime.now().format(dateTimeFormatterForJson);
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }
    
    // Private methods

    private void printError(String message) {
        System.out.println("SettingsItem Message: " + message);
    }

    private String getStringFromObject(Object value) {
        if (value == null) {
            return "null";
        }
        else if (value instanceof Character) {
            return value.toString();
        }
        else if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof Number) {
            return value.toString();
        }
        else if (value instanceof Boolean) {
            if ((Boolean) value) {
                return "true";
            }
            else {
                return "false";
            }
        }
        else if (value instanceof List) {
            // Use GSon to convert List to String
            Gson gson = new Gson();
            return gson.toJson(value);
        }
        else if (value instanceof Map) {
            // Use GSon to convert Map to String
            Gson gson = new Gson();
            return gson.toJson(value);
        }
        else if (value instanceof PyDict) {
            // Use GSon to convert PyDict to String
            Gson gson = new Gson();
            return gson.toJson(value);
        }
        else if (value instanceof LocalDate) {
            return ((LocalDate) value).format(dateFormatter);
        }
        else if (value instanceof LocalTime) {
            return ((LocalTime) value).format(timeFormatter);
        }
        else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(dateTimeFormatter);
        }

        return value.toString();
    }

    private Object getObjectFromString(String value) {
        if (value == null) {
            return null;
        }

        switch (dataType) {
            case CHAR:
                return getChar(value);
            case STRING:
                return value;
            case INTEGER:
                return getInteger(value);
            case LONG:
                return getLong(value);
            case FLOAT:
                return getFloat(value);
            case DOUBLE:
                return getDouble(value);
            case BOOLEAN:
                return getBoolean(value);
            case LIST:
                return getList(value);
            case MAP:
                return getMap(value);
            case DATE:
                return getDate(value);
            case TIME:  
                return getTime(value);
            case DATETIME:
                return getDateTime(value);
            case NULL:
                return null;
            default:
                return null;
        }
    }

    private DataType getRecommendedDataTypeForString(String value) {
        if (value == null) {
            return DataType.UNDEFINED;
        }
        else if (isNull(value)) {
            return DataType.NULL;
        }
        else if (getBoolean(value) != null) {
            return DataType.BOOLEAN;
        }
        else if (getInteger(value) != null) {
            return DataType.INTEGER;
        }
        else if (getLong(value) != null) {
            return DataType.LONG;
        }
        else if (getDouble(value) != null) {
            return DataType.DOUBLE;
        }
        else if (getFloat(value) != null) {
            return DataType.FLOAT;
        }
        else if (getList(value) != null) {
            return DataType.LIST;
        }
        else if (getMap(value) != null) {
            return DataType.MAP;
        }
        else if (getDate(value) != null) {
            return DataType.DATE;
        }
        else if (getTime(value) != null) {
            return DataType.TIME;
        }
        else if (getDateTime(value) != null) {
            return DataType.DATETIME;
        }
        else {
            if (value instanceof String) {
                return DataType.STRING;
            }

            try {
                value.toString();
                return DataType.STRING;
            }
            catch (Exception e) {
                return DataType.UNDEFINED;
            }
        }
    }

    private Object getCorrectObjectAccordingToDataType(String stringValue) {
        return getCorrectObjectAccordingToDataType(stringValue, null);
    }

    private Object getCorrectObjectAccordingToDataType(String stringValue, DataType toDataType) {
        if (toDataType == null) {
            toDataType = dataType;
        }

        switch (toDataType) {
            case CHAR:
                return getChar(stringValue);
            case STRING:
                return stringValue;
            case INTEGER:
                return getInteger(stringValue);
            case LONG:
                return getLong(stringValue);
            case DOUBLE:
                return getDouble(stringValue);
            case FLOAT:
                return getFloat(stringValue);
            case BOOLEAN:
                return getBoolean(stringValue);
            case LIST:
                return getList(stringValue);
            case MAP:
                return getMap(stringValue);
            case DATE:
                return getDate(stringValue);
            case TIME:
                return getTime(stringValue);
            case DATETIME:
                return getDateTime(stringValue);
            case NULL:
                return isNull(stringValue);
            default:
                printError("Invalid data type: " + toDataType);
                return null;
        }
    }

    // Data Type resolving methods

    /**
     * Checks if string value can match with current data type
     * @param value - string to check
     * @return - true if string value can match with current data type
     */
    private boolean isValidDataType(String value) {
        switch (dataType) {
            case CHAR:
                return getChar(value) != null;
            case STRING:
                return true;
            case INTEGER:
                return getInteger(value) != null;
            case LONG:
                return getLong(value) != null;
            case FLOAT:
                return getFloat(value) != null;
            case DOUBLE:
                return getDouble(value) != null;
            case BOOLEAN:
                return getBoolean(value) != null;
            case LIST:
                return getList(value) != null;
            case MAP:
                return getMap(value) != null;
            case DATE:
                return getDate(value) != null;
            case TIME:
                return getTime(value) != null;
            case DATETIME:
                return getDateTime(value) != null;
            case NULL:
                return isNull(value);
            default:
                return false;
        }
    }

    /**
     * Checks if string is null
     * @param str - string to check
     * @return - true if string is null
     */
    private boolean isNull(String str) {
        if (str.toLowerCase().equals("null")
            || str.toLowerCase().equals("none")
            || str.toLowerCase().equals("nan")) {
            return true;
        }
        return false;
    }

    /**
     * If string can be converted to char, returns Char value, otherwise returns null
     * @param data - string to check
     * @return - char value or null
     */
    private Character getChar (String data) {
        if (data == null) {
            return null;
        }
        else if (data.length() == 1) {
            return (char) data.charAt(0);
        }
        else {
            return null;
        }
    }

    /**
     * If string can be converted to integer, returns Integer value, otherwise returns null
     * @param data - string to check
     * @return - integer value or null
     */
    private Integer getInteger (String data) {
        if (data == null) {
            return null;
        }
        else {
            try {
                return Integer.parseInt(data);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * If string can be converted to boolean, returns Boolean value, otherwise returns null
     * @param data - string to check
     * @return - boolean value or null
     */
    private Boolean getBoolean (String data) {
        if (data == null) {
            return null;
        }
        else if (data.toLowerCase().equals("true")) {
            return true;
        }
        else if (data.toLowerCase().equals("false")) {
            return false;
        }
        else {
            return null;
        }
    }

    /**
     * If string can be converted to long, returns Long value, otherwise returns null
     * @param data - string to check
     * @return - long value or null
     */
    private Long getLong (String data) {
        if (data == null) {
            return null;
        }
        else {
            try {
                return Long.parseLong(data);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * If string can be converted to float, returns Float value, otherwise returns null
     * @param data - string to check
     * @return - float value or null
     */
    private Float getFloat (String data) {
        if (data == null) {
            return null;
        }
        else {
            try {
                return Float.parseFloat(data);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * If string can be converted to double, returns Double value, otherwise returns null
     * @param data - string to check
     * @return - double value or null
     */
    private Double getDouble (String data) {
        if (data == null) {
            return null;
        }
        else {
            try {
                return Double.parseDouble(data);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    }    

    /**
     * If string can be converted to Map, returns PyDict (HashMap) value, otherwise returns null
     * @param data - string to check
     * @return - PyDict value or null
     */
    private PyDict getMap (String data) {
        Gson gson = new Gson();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(data, HashMap.class);
            if (map == null) {
                return null;
            }
            PyDict pyDict = new PyDict();
            pyDict.putAll(map);
            return pyDict;
        }
        catch (JsonSyntaxException e) {
            return null;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * If string can be converted to List, returns ArrayList value, otherwise returns null
     * @param data - string to check
     * @return - ArrayList value or null
     */
    private ArrayList<Object> getList (String data) {
        Gson gson = new Gson();
        try {
            @SuppressWarnings("unchecked")
            List<Object> list = gson.fromJson(data, ArrayList.class);
            if (list == null) {
                return null;
            }
            return (ArrayList<Object>) list;
        }
        catch (JsonSyntaxException e) {
            return null;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * If string can be converted to LocalDate, returns LocalDate value, otherwise returns null
     * @param data - string to check
     * @return - LocalDate value or null
     */
    private LocalDate getDate (String data) {
        if (data == null) {
            return null;
        }
        else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
                if (!data.endsWith(".")) {
                    data += ".";
                }
                return LocalDate.parse(data, formatter);
            }
            catch (DateTimeParseException e) {
                return null;
            }
            catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * If string can be converted to LocalTime, returns LocalTime value, otherwise returns null
     * @param data - string to check
     * @return - LocalTime value or null
     */
    private LocalTime getTime (String data) {
        if (data == null) {
            return null;
        }
        else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                return LocalTime.parse(data, formatter);
            }
            catch (DateTimeParseException e) {
                return null;
            }
            catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * If string can be converted to LocalDateTime, returns LocalDateTime value, otherwise returns null
     * @param data - string to check
     * @return - LocalDateTime value or null
     */
    private LocalDateTime getDateTime (String data) {
        if (data == null) {
            return null;
        }
        else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
                return LocalDateTime.parse(data, formatter);
            }
            catch (DateTimeParseException e) {
                return null;
            }
            catch (Exception e) {
                return null;
            }
        }
    }



    // Override methods

    /**
     * <p>Compares this SettingsItem with the specified object for equality.</p>
     * <table border="1">
     * <tr>
     *   <th>Compares values</th>
     *   <th>Does not compare</th>
     * </tr>
     * <tr>
     *   <td>Key</td>
     *   <td>UserData</td>
     * </tr>
     * <tr>
     *   <td>Value</td>
     *   <td>CreationDate</td>
     * </tr>
     * <tr>
     *   <td>SettingType</td>
     *   <td>CanBeSavedInFile</td>
     * </tr>
     * <tr>
     *   <td>DataType</td>
     *   <td></td>
     * </tr>
     * <tr>
     *   <td>DefaultValue</td>
     *   <td></td>
     * </tr>
     * <tr>
     *   <td>Minimum</td>
     *   <td></td>
     * </tr>
     * <tr>
     *   <td>Maximum</td>
     *   <td></td>
     * </tr>
     * <tr>
     *   <td>Description</td>
     *   <td></td>
     * </tr>
     * </table>
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SettingsItem other = (SettingsItem) obj;
        
        return  Objects.equals(this.getKey(), other.getKey()) &&
                Objects.equals(this.getValueSTRING(), other.getValueSTRING()) &&
                Objects.equals(this.getSettingType(), other.getSettingType()) &&
                Objects.equals(this.getDataType(), other.getDataType()) &&
                Objects.equals(this.getDefaultValueSTRING(), other.getDefaultValueSTRING()) &&
                Objects.equals(this.getMinDOUBLE(), other.getMinDOUBLE()) &&
                Objects.equals(this.getMaxDOUBLE(), other.getMaxDOUBLE()) &&
                Objects.equals(this.getDescription(), other.getDescription());
    }

    /**
     * <p>Values of this SettingsItem.</p>
     * <table border="1">
     * <tr>
     *   <th>Includes</th>
     *   <th>Does not include</th>
     * </tr>
     * <tr>
     *   <td>Key</td>
     *   <td>UserData</td>
     * </tr>
     * <tr>
     *   <td>Value</td>
     *   <td>CreationDate</td>
     * </tr>
     * <tr>
     *   <td>SettingType</td>
     *   <td>CanBeSavedInFile</td>
     * </tr>
     * <tr>
     *   <td>DataType</td>
     *   <td></td>
     * </tr>
     * <tr>
     *   <td>DefaultValue</td>
     *   <td></td>
     * </tr>
     * <tr>
     *   <td>Minimum</td>
     *   <td></td>
     * </tr>
     * <tr>
     *   <td>Maximum</td>
     *   <td></td>
     * </tr>
     * <tr>
     *   <td>Description</td>
     *   <td></td>
     * </tr>
     * </table>
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValueSTRING(), getSettingType(), getDataType(), getDefaultValueSTRING(), getMinDOUBLE(), getMaxDOUBLE(), getDescription());
    }


}
