package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.utils.LanguagesEnum;


public class EventAddLanguageToFile extends Event {

    public static final EventType<EventAddLanguageToFile> EVENT_ADD_LANGUAGE_TO_FILE_TYPE = new EventType<>(Event.ANY, "EVENT_ADD_LANGUAGE_TO_FILE");

    private final LanguagesEnum languageAdded;

    // Constructors

    public EventAddLanguageToFile(LanguagesEnum languageAdded) {
        super(EVENT_ADD_LANGUAGE_TO_FILE_TYPE);
        this.languageAdded = languageAdded;
    }

    public EventAddLanguageToFile(String languageName) {
        super(EVENT_ADD_LANGUAGE_TO_FILE_TYPE);
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
