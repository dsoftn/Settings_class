package com.dsoftn.Settings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import com.dsoftn.utils.PyDict;

public class LanguageItem {
    // Constants
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private DateTimeFormatter dateTimeFormatterForJson = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    // Properties
    private String key = "";
    private String value = "";
    private String creationDate = LocalDateTime.now().format(dateTimeFormatterForJson);
    private String userData = "";

    
    // Constructors

    public LanguageItem() {}
    
    public LanguageItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Public methods

    public LanguageItem duplicate() {
        LanguageItem result = new LanguageItem();

        result.setKey(this.key);
        result.setValue(this.value);
        result.setCreationDate(this.getCreationDate());
        result.setUserData(this.userData);

        return result;
    }

    // Serialization / Deserialization methods

    public Map<String, Object> toMap() {
        Map<String, Object> result = new PyDict();
        result.put("key", key);
        result.put("value", value);
        result.put("creationDate", creationDate);
        result.put("userData", userData);
        
        return result;
    }
    
    public void fromMap(Map<String, Object> map) {
        // Key
        key = (String) map.get("key");
        
        // Value, default value, min and max
        value = (String) map.get("value");

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



}
