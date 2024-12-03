package com.dsoftn;

import com.dsoftn.Settings.SettingsItem;
import com.dsoftn.Settings.gui.GuiMain;
import com.dsoftn.utils.PyDict;

import java.util.Map;

import com.google.gson.*;


public class Main {
    public static void main(String[] args) {
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