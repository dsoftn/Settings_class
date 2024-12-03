package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.utils.LanguagesEnum;


public class EventEditLanguageAdded extends Event {

    public static final EventType<EventEditLanguageAdded> EVENT_EDIT_LANGUAGE_ADDED_TYPE = new EventType<>(Event.ANY, "EVENT_EDIT_LANGUAGE_ADDED");

    private final LanguagesEnum languageAdded;

    // Constructors

    public EventEditLanguageAdded(LanguagesEnum languageAdded) {
        super(EVENT_EDIT_LANGUAGE_ADDED_TYPE);
        this.languageAdded = languageAdded;
    }

    public EventEditLanguageAdded(String languageName) {
        super(EVENT_EDIT_LANGUAGE_ADDED_TYPE);
        LanguagesEnum value = LanguagesEnum.fromName(languageName);
        if (value == null) {
            throw new IllegalArgumentException("Language not found: " + languageName);
        }
        this.languageAdded = value;

    }

    // Getters

    public LanguagesEnum getLanguageAdded() {
        return languageAdded;
    }

}
