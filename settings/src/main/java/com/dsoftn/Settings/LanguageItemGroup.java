package com.dsoftn.Settings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dsoftn.utils.PyDict;


public class LanguageItemGroup {
    // Properties
    private String groupName = ""; // Group name
    private String groupKey = ""; // Group key will be determined automatically based on LanguageItems in group
    private List<LanguageItem> languageItems = new ArrayList<LanguageItem>(); // List of LanguageItems for different languages with same key
    private String userData = "";

    // Constructors
    public LanguageItemGroup(String groupName) {
        this.groupName = groupName;
    }

    public LanguageItemGroup(String groupName, List<LanguageItem> languageItems) {
        this.groupName = groupName;
        this.languageItems = languageItems;
        this.groupKey = checkItemsAndGetGroupKey(languageItems);
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
    public void addToGroup(LanguageItem item) {
        this.groupKey = checkItemsAndGetGroupKey(languageItems);
        this.languageItems.add(item);
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

        for (LanguageItem item : this.languageItems) {
            result.addToGroup(item.duplicate());
        }

        if (this.groupKey != result.getGroupKey()) {
            throw new RuntimeException("Internal error: group keys are different. This GroupKey: " + this.groupKey + " ResultGroupKey: " + result.getGroupKey());
        }

        return result;
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

    // Private methods

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

    private void errorInvalidGroupItems() {
        throw new RuntimeException("Invalid group items, either item key is different from group key or item with same key and language code but different value already exists in group.");
    }


}
