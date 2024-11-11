package com.dsoftn;

import com.dsoftn.Settings.SettingType;
import com.dsoftn.Settings.Settings;
import com.dsoftn.Settings.SettingsItem;
import com.dsoftn.Settings.gui.GuiMain;
import com.dsoftn.utils.PyDict;

import java.util.Map;

import com.google.gson.*;


public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Settings settings = new Settings();

        settings.userSettingsFilePath = "user_settings.json";
        settings.languagesFilePath = "languages.json";
        settings.defaultSettingsFilePath = "default_settings.json";
        settings.appDataFilePath = "app_data.json";

        settings.load(true, false, false);

        System.out.println(settings.getUserSettingsItem("my_key").isValid());
        settings.setv("my_key", 1);
        System.out.println(settings.getUserSettingsItem("my_key").isValid());
        settings.getUserSettingsItem("my_key").addSettingsType(SettingType.DEFAULT);
        settings.save(true, false, false);
        settings.load(true, false, false);

        printJSON(settings.getAllUserSettingsData());

        settings.save(true, false, false);

        GuiMain.launch(GuiMain.class, args);


        
    }

    private static void printJSON(Map<String, Object> data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> translatedData = new PyDict();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Map<String, Object> map = new PyDict();
            if (entry.getValue() instanceof SettingsItem) {
                SettingsItem item = (SettingsItem) entry.getValue();
                map = item.toMap();
                translatedData.put(entry.getKey(), map);
            } else {
                translatedData.put(entry.getKey(), entry.getValue());
            }

        }
        String json = gson.toJson(translatedData);
        System.out.println(json);
    }
}