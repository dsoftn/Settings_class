package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;


public class EventWriteLog extends Event {

    public static final EventType<EventWriteLog> EVENT_WRITE_LOG_TYPE = new EventType<>(Event.ANY, "EVENT_WRITE_LOG");

    // Properties
    private final String message;
    private final int indentLevel;

    // Constructors
    public EventWriteLog(String message) {
        super(EVENT_WRITE_LOG_TYPE);
        this.message = message;
        this.indentLevel = 0;
    }

    public EventWriteLog(String message, int indentLevel) {
        super(EVENT_WRITE_LOG_TYPE);
        this.message = message;
        this.indentLevel = indentLevel;
    }

    public String getMessage() {
        return message;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

}
