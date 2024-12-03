package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.utils.LanguagesEnum;


public class EventEditLanguageChanged extends Event {

    public static final EventType<EventEditLanguageChanged> EVENT_EDIT_LANGUAGE_CHANGED_TYPE = new EventType<EventEditLanguageChanged>("EVENT_EDIT_LANGUAGE_CHANGED");

    private final LanguagesEnum languageChanged;

    // Constructors

    public EventEditLanguageChanged(LanguagesEnum languageChanged) {
        super(EVENT_EDIT_LANGUAGE_CHANGED_TYPE);
        this.languageChanged = languageChanged;
    }

    public EventEditLanguageChanged(String languageName) {
        super(EVENT_EDIT_LANGUAGE_CHANGED_TYPE);
        LanguagesEnum value = LanguagesEnum.fromName(languageName);
        if (value == null) {
            throw new IllegalArgumentException("Language not found: " + languageName);
        }
        this.languageChanged = value;

    }

    // Getters

    public LanguagesEnum getLanguageChanged() {
        return languageChanged;
    }

}
