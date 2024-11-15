package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.utils.UTranslate.LanguagesEnum;


public class EventEditLanguageRemoved extends Event {

    public static final EventType<EventEditLanguageRemoved> EVENT_EDIT_LANGUAGE_REMOVED_TYPE = new EventType<>(Event.ANY, "EVENT_EDIT_LANGUAGE_REMOVED");

    private final LanguagesEnum languageRemoved;

    // Constructors

    public EventEditLanguageRemoved(LanguagesEnum languageRemoved) {
        super(EVENT_EDIT_LANGUAGE_REMOVED_TYPE);
        this.languageRemoved = languageRemoved;
    }

    public EventEditLanguageRemoved(String languageName) {
        super(EVENT_EDIT_LANGUAGE_REMOVED_TYPE);
        LanguagesEnum value = LanguagesEnum.fromName(languageName);
        if (value == null) {
            throw new IllegalArgumentException("Language not found: " + languageName);
        }
        this.languageRemoved = value;

    }

    // Getters

    public LanguagesEnum getLanguageRemoved() {
        return languageRemoved;
    }

}
