package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import java.util.List;


public class EventEditLanguageContentChanged extends Event {

    public static final EventType<EventEditLanguageContentChanged> EVENT_EDIT_LANGUAGE_CONTENT_CHANGED_TYPE = new EventType<EventEditLanguageContentChanged>("EVENT_EDIT_LANGUAGE_CONTENT_CHANGED");

    private final boolean isChanged;
    private final List<String> changedLangCodes;

    // Constructors

    public EventEditLanguageContentChanged(List<String> changedLangCodes) {
        super(EVENT_EDIT_LANGUAGE_CONTENT_CHANGED_TYPE);
        this.changedLangCodes = changedLangCodes;
        this.isChanged = changedLangCodes.size() > 0;
    }

    // Getters

    public boolean isChanged() {
        return isChanged;
    }

    public List<String> getChangedLangCodes() {
        return changedLangCodes;
    }

}
