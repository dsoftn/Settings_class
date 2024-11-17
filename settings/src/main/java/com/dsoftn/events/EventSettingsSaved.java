package com.dsoftn.events;

import java.util.List;
import java.util.ArrayList;

import javafx.event.Event;
import javafx.event.EventType;


public class EventSettingsSaved extends Event {

    public static final EventType<EventSettingsSaved> EVENT_SETTINGS_SAVED_TYPE = new EventType<>(Event.ANY, "EVENT_SETTINGS_SAVED");

    private final List<String> settingsList;
    private final boolean allSettingsSaved;
    private final boolean isSettingsSaved;
    private final boolean isLanguageSaved;


    // Constructors

    public EventSettingsSaved(boolean isSettingsSaved, boolean isLanguageSaved) {
        super(EVENT_SETTINGS_SAVED_TYPE);
        this.settingsList = new ArrayList<>();
        this.allSettingsSaved = true;
        this.isSettingsSaved = isSettingsSaved;
        this.isLanguageSaved = isLanguageSaved;
    }

    public EventSettingsSaved(boolean isSettingsSaved, boolean isLanguageSaved, List<String> settingsList) {
        super(EVENT_SETTINGS_SAVED_TYPE);
        this.settingsList = settingsList;
        this.allSettingsSaved = false;
        this.isSettingsSaved = isSettingsSaved;
        this.isLanguageSaved = isLanguageSaved;
    }

    public EventSettingsSaved(boolean isSettingsSaved, boolean isLanguageSaved, List<String> settingsList, boolean saveAllSettings) {
        super(EVENT_SETTINGS_SAVED_TYPE);
        this.settingsList = settingsList;
        this.allSettingsSaved = saveAllSettings;
        this.isSettingsSaved = isSettingsSaved;
        this.isLanguageSaved = isLanguageSaved;
    }


    // Getters

    public List<String> getSettingsList() {
        return settingsList;
    }

    public boolean isAllSettingsSaved() {
        return allSettingsSaved;
    }

    public boolean isSettingsSaved() {
        return isSettingsSaved;
    }

    public boolean isLanguageSaved() {
        return isLanguageSaved;
    }

}
