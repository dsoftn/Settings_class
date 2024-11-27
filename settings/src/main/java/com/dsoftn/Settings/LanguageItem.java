package com.dsoftn.Settings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import com.dsoftn.utils.PyDict;

public class LanguageItem {
    // Constants
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private DateTimeFormatter dateTimeFormatterForJson = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    // Properties
    private String key = "";
    private String value = "";
    private String languageCode = "";
    private String creationDate = LocalDateTime.now().format(dateTimeFormatterForJson);
    private String userData = "";

    
    // Constructors

    public LanguageItem() {}
    
    public LanguageItem(String key, String value, String languageCode) {
        this.key = key;
        this.value = value;
        this.languageCode = languageCode;
    }

    // Public methods

    public LanguageItem duplicate() {
        LanguageItem result = new LanguageItem();

        result.setKey(this.key);
        result.setValue(this.value);
        result.setLanguageCode(this.languageCode);
        result.setCreationDate(this.getCreationDate());
        result.setUserData(this.userData);

        return result;
    }

    // Serialization / Deserialization methods

    public Map<String, Object> toMap() {
        Map<String, Object> result = new PyDict();
        result.put("key", key);
        result.put("value", value);
        result.put("languageCode", languageCode);
        result.put("creationDate", creationDate);
        result.put("userData", userData);
        
        return result;
    }
    
    public void fromMap(Map<String, Object> map) {
        // Key
        key = (String) map.get("key");
        
        // Value, default value, min and max
        value = (String) map.get("value");

        // Language code
        languageCode = (String) map.get("languageCode");

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

    // Getters and Setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        if (key.isEmpty()) {
            printError("Key is empty");
            throw new RuntimeException("Key is empty");
        }
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
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
        System.out.println("LanguageItem Message: " + message);
    }

    // Override methods

    /**
     * <p>Compares this LanguageItem with the specified object for equality.</p>
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
     *   <td>LanguageCode</td>
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
        
        LanguageItem other = (LanguageItem) obj;

        return  Objects.equals(this.getKey(), other.getKey()) &&
                Objects.equals(this.getValue(), other.getValue()) &&
                Objects.equals(this.getLanguageCode(), other.getLanguageCode());
    }

    /**
     * <p>Values of this LanguageItem.</p>
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
     *   <td>LanguageCode</td>
     *   <td></td>
     * </tr>
     * </table>
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getValue(), getLanguageCode());
    }


}
