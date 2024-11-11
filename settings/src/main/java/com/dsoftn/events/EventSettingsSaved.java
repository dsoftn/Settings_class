package com.dsoftn.events;

import java.util.List;
import java.util.ArrayList;

import javafx.event.Event;
import javafx.event.EventType;


public class EventSettingsSaved extends Event {

    public static final EventType<EventSettingsSaved> EVENT_SETTINGS_SAVED_TYPE = new EventType<>(Event.ANY, "EVENT_SETTINGS_SAVED");

    private final List<String> settingsList;
    private final boolean allSettingsSaved;


    // Constructors

    public EventSettingsSaved() {
        super(EVENT_SETTINGS_SAVED_TYPE);
        this.settingsList = new ArrayList<>();
        this.allSettingsSaved = true;
    }

    public EventSettingsSaved(List<String> settingsList) {
        super(EVENT_SETTINGS_SAVED_TYPE);
        this.settingsList = settingsList;
        this.allSettingsSaved = false;
    }

    public EventSettingsSaved(List<String> settingsList, boolean saveAllSettings) {
        super(EVENT_SETTINGS_SAVED_TYPE);
        this.settingsList = settingsList;
        this.allSettingsSaved = saveAllSettings;
    }


    // Getters

    public List<String> getSettingsList() {
        return settingsList;
    }

    public boolean isAllSettingsSaved() {
        return allSettingsSaved;
    }

}
