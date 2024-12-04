package com.dsoftn.utils;

import java.util.Map;

import com.dsoftn.Settings.SettingsItem;
import com.dsoftn.Settings.LanguageItem;
import com.dsoftn.Settings.LanguageItemGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UJson {

    /**
     * Converts Map to JSON string
     * If map contains SettingsItem, LanguageItem or LanguageItemGroup it will be converted to Map
     * @param data - Map
     * @return - String
     */
    public static String getPrettyJSON(Map<String, Object> data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> translatedData = new PyDict();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Map<String, Object> map = new PyDict();
            if (entry.getValue() instanceof SettingsItem) {
                SettingsItem item = (SettingsItem) entry.getValue();
                map = item.toMap();
                translatedData.put(entry.getKey(), map);
            }
            else if (entry.getValue() instanceof LanguageItem) {
                LanguageItem item = (LanguageItem) entry.getValue();
                map = item.toMap();
                translatedData.put(entry.getKey(), map);
            }
            else if (entry.getValue() instanceof LanguageItemGroup) {
                LanguageItemGroup item = (LanguageItemGroup) entry.getValue();
                map = item.toMap();
                translatedData.put(entry.getKey(), map);
            }
            else {
                translatedData.put(entry.getKey(), entry.getValue());
            }

        }
        String json = gson.toJson(translatedData);
        return json;
    }


}
