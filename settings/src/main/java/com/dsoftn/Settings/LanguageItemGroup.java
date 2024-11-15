package com.dsoftn.Settings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dsoftn.utils.PyDict;


public class LanguageItemGroup {
    // Constants
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private DateTimeFormatter dateTimeFormatterForJson = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    // Properties
    private String groupName = ""; // Group name
    private String groupKey = ""; // Group key will be determined automatically based on LanguageItems in group
    private List<LanguageItem> languageItems = new ArrayList<LanguageItem>(); // List of LanguageItems for different languages with same key
    private String userData = "";
    private String creationDate = LocalDateTime.now().format(dateTimeFormatterForJson);

    // Constructors
    public LanguageItemGroup(String groupName) {
        this.groupName = groupName;
    }

    public LanguageItemGroup(String groupName, List<LanguageItem> languageItems) {
        this.groupName = groupName;
        addToGroup(languageItems);
        if (this.groupKey == null) {
            errorInvalidGroupItems();
        }
    }

    public LanguageItemGroup() {}

    // Checkers
    public boolean isInGroup(LanguageItem item) { return isInGroup(item, languageItems); }

    private boolean isInGroup(LanguageItem item, List<LanguageItem> languageItems) {
        for (LanguageItem langItem : languageItems) {
            if (langItem.equals(item)) {
                return true;
            }
        }

        return false;
    }

    public boolean canBeAddedToGroup(LanguageItem item) { return canBeAddedToGroup(item, languageItems, groupKey); }

    private boolean canBeAddedToGroup(LanguageItem item, List<LanguageItem> languageItems, String groupKey) {
        boolean result = true;
        if (!item.getKey().equals(groupKey) && !groupKey.isEmpty()) return false;

        for (LanguageItem langItem : languageItems) {
            if (!item.getKey().equals(langItem.getKey())) {
                result = false;
                break;
            }

            if (!isInGroup(item, languageItems) && item.getKey().equals(langItem.getKey()) && item.getLanguageCode().equals(langItem.getLanguageCode())) {
                result = false;
                break;
            }
        }

        return result;
    }

    // Serialization / Deserialization methods

    public Map<String, Object> toMap() {
        Map<String, Object> result = new PyDict();
        result.put("groupName", groupName);
        result.put("groupKey", groupKey);
        result.put("userData", userData);
        result.put("creationDate", creationDate);
        
        List<Map<String, Object>> items = new ArrayList<>();

        for (LanguageItem item : languageItems) {
            items.add(item.toMap());
        }
        result.put("languageItems", items);

        return result;
    }
    
    public void fromMap(Map<String, Object> map) {
        // Name
        groupName = (String) map.get("groupName");

        // Key
        groupKey = (String) map.get("groupKey");

        // User Data
        userData = (String) map.get("userData");

        // Creation Date
        creationDate = (String) map.get("creationDate");
        
        // Language Items
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("languageItems");
        languageItems = new ArrayList<LanguageItem>();
        for (Map<String, Object> itemMap : items) {
            LanguageItem item = new LanguageItem();
            item.fromMap(itemMap);
            languageItems.add(item);
        }
    }

    // Public methods

    /**
     * Add list of LanguageItems to group
     * Every LanguageItem will be duplicated
     * @param items
     */
    public void addToGroup(List<LanguageItem> items) {
        for (LanguageItem item : items) {
            this.addToGroup(item);
        }
    }

    /**
     * Add duplicate of LanguageItem to group
     * @param item LanguageItem
     */
    public void addToGroup(LanguageItem item) {
        this.groupKey = checkItemsAndGetGroupKey(item);
        if (this.groupKey == null) {
            errorInvalidGroupItems();
        }
        this.languageItems.add(item.duplicate());
        setupCreationDate(item);
    }

    public boolean removeFromGroup(List<LanguageItem> items) {
        Boolean result = null;
        for (LanguageItem item : items) {
            boolean removed = removeFromGroup(item);

            if (result == null && removed) {
                result = true;
            }

            if (!removed) {
                result = false;
            }
        }

        if (result == null) {
            result = false;
        }

        return result;
    }

    public boolean removeFromGroup(LanguageItem item) {
        boolean result = false;
        for (LanguageItem langItem : languageItems) {
            if (langItem.equals(item)) {
                languageItems.remove(langItem);
                result = true;
                break;
            }
        }

        if (languageItems.isEmpty()) {
            groupKey = "";
        }

        return result;
    }

    public LanguageItemGroup duplicate() {
        LanguageItemGroup result = new LanguageItemGroup();

        result.setGroupName(this.groupName);

        result.setUserData(this.userData);

        result.setCreationDate(this.getCreationDate());

        for (LanguageItem item : this.languageItems) {
            result.addToGroup(item.duplicate());
        }

        if (this.groupKey != result.getGroupKey()) {
            throw new RuntimeException("Internal error: group keys are different. This GroupKey: " + this.groupKey + " ResultGroupKey: " + result.getGroupKey());
        }

        return result;
    }

    public LanguageItem getLanguageItemByLanguageCode(String languageCode) {
        for (LanguageItem item : languageItems) {
            if (item.getLanguageCode().equals(languageCode)) {
                return item;
            }
        }

        return null;
    }

    public List<String> getListOfLanguageCodes() {
        List<String> result = new ArrayList<>();
        for (LanguageItem item : languageItems) {
            result.add(item.getLanguageCode());
        }
        return result;
    }

    // Static methods

    public static boolean isLanguageMapObjectValid(Map<String, Object> langMap) {
        boolean result = true;

        if (!langMap.containsKey("available_languages")) {
            result = false;
        }
        if (!langMap.containsKey("active_language")) {
            result = false;
        }
        if (!langMap.containsKey("data")) {
            result = false;
        }

        return result;
    }

    public static List<LanguageItemGroup> getListOfGroupLanguageObjectsFromLanguageMapObject(Map<String, Object> langMap) {
        if (!isLanguageMapObjectValid(langMap)) {
            errorInvalidLanguageMapObject();
        }

       if (langMap.get("data") instanceof Map) {
            Map<String, Object> data = PyDict.castToMap(langMap.get("data"));
            if (data.isEmpty()) {
                return new ArrayList<LanguageItemGroup>();
            }

            List<String> langCodeKeys = new ArrayList<>(data.keySet());
            if (langCodeKeys.isEmpty()) {
                return new ArrayList<LanguageItemGroup>();
            }

            List<String> keys = new ArrayList<String>();

            Map<String, LanguageItem> langCodeFirst = PyDict.castToMap(data.get(langCodeKeys.get(0)));

            keys.addAll(langCodeFirst.keySet());

            List<LanguageItemGroup> result = new ArrayList<LanguageItemGroup>();

            for (String key : keys) {
                LanguageItemGroup group = getGroupFromLanguageMapObject(langMap, key);
                if (group != null) {
                    result.add(group);
                }
            }

            return result;
       }
       else {
           errorInvalidLanguageMapObject();
           return null;
       }
    }

    public static LanguageItemGroup getGroupFromLanguageMapObject(Map<String, Object> langMap, String langKeyToExtract) {
        if (!isLanguageMapObjectValid(langMap)) {
            errorInvalidLanguageMapObject();
        }

       if (langMap.get("data") instanceof Map) {
            Map<String, Object> data = PyDict.castToMap(langMap.get("data"));
            if (data.isEmpty()) {
                return null;
            }

            List<String> langCodeKeys = new ArrayList<>(data.keySet());
            if (langCodeKeys.isEmpty()) {
                return null;
            }

            Map<String, LanguageItem> langCodeFirst = PyDict.castToMap(data.get(langCodeKeys.get(0)));

            List<String> keys = new ArrayList<>(langCodeFirst.keySet());
            if (keys.isEmpty()) {
                return null;
            }

            LanguageItemGroup group = new LanguageItemGroup();

            for (String langCodeKey : langCodeKeys) {
                Map<String, LanguageItem> langCodeItem = PyDict.castToMap(data.get(langCodeKey));
                LanguageItem item = langCodeItem.get(langKeyToExtract);
                if (item != null) {
                    group.addToGroup(item);
                }
            }

            return group;
       }
       else {
           errorInvalidLanguageMapObject();
           return null;
       }
    }

    public static void insertIntoLanguageMapObject(Map<String, Object> langMap, LanguageItemGroup groupObject) {
        if (!isLanguageMapObjectValid(langMap)) {
            errorInvalidLanguageMapObject();
        }

       if (langMap.get("data") instanceof Map) {
            Map<String, Object> data = PyDict.castToMap(langMap.get("data"));
            if (data.isEmpty()) {
                return;
            }

            List<String> langCodeKeys = new ArrayList<>(data.keySet());
            if (langCodeKeys.isEmpty()) {
                return;
            }

            for (LanguageItem item : groupObject.getLanguageItems()) {
                if (!langCodeKeys.contains(item.getLanguageCode())) {
                    continue;
                }

                Map<String, LanguageItem> langCodeItem = PyDict.castToMap(data.get(item.getLanguageCode()));
                langCodeItem.put(item.getKey(), item.duplicate());
            }
       }
       else {
           errorInvalidLanguageMapObject();
       }
    }

    // Getters and Setters

    public String getGroupName() { return groupName; }

    public void setGroupName(String groupName) { this.groupName = groupName; }

    public List<LanguageItem> getLanguageItems() { return languageItems; }

    public void setLanguageItems(List<LanguageItem> languageItems) {
        this.groupKey = checkItemsAndGetGroupKey(languageItems);
        if (this.groupKey == null) {
            errorInvalidGroupItems();
        }
        this.languageItems = languageItems;
    }

    public String getGroupKey() { return groupKey; }

    public String getUserData() { return userData; }

    public void setUserData(String userData) { this.userData = userData; }

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

    // Private methods

    private void setupCreationDate(List<LanguageItem> langItems) {
        for (LanguageItem item : langItems) {
            setupCreationDate(item);
        }
    }

    private void setupCreationDate(LanguageItem item) {
        if (item.getCreationDateForJson() != null && !item.getCreationDateForJson().isEmpty()) {
            this.creationDate = item.getCreationDateForJson();
        }
    }

    private String checkItemsAndGetGroupKey(LanguageItem langItem) {
        List<LanguageItem> langItems = new ArrayList<LanguageItem>();
        langItems.add(langItem);
        return checkItemsAndGetGroupKey(langItems);
    }

    private String checkItemsAndGetGroupKey(List<LanguageItem> langItems) {
        // Create virtual group key and items list
        String vGroupKey = "";
        List<LanguageItem> vLanguageItems = new ArrayList<LanguageItem>();

        for (LanguageItem item : languageItems) {
            vLanguageItems.add(item);
        }

        for (LanguageItem item : langItems) {
            if (vGroupKey.isEmpty()) {
                vGroupKey = item.getKey();
            }
            
            if (canBeAddedToGroup(item, vLanguageItems, vGroupKey)) {
                vLanguageItems.add(item);
            }
            else {
                return null;
            }
        }

        return vGroupKey;
    }

    private static void errorInvalidGroupItems() {
        throw new RuntimeException("Invalid group items, either item key is different from group key or item with same key and language code but different value already exists in group.");
    }

    private static void errorInvalidLanguageMapObject() {
        throw new RuntimeException("Invalid language map object.");
    }

}
