package com.sycha;

import java.util.EnumSet;

public class EventFilter {
    public enum EventType {
        MOUSE_CLICK,
        MOUSE_DOUBLE_CLICK,
        MOUSE_DRAG_START,
        MOUSE_DRAG,
        MOUSE_DRAG_END,
        MOUSE_MOVE,
        MOUSE_WHEEL,
        MOUSE_ENTER,
        MOUSE_EXIT,
        MOUSE_PRESS,
        MOUSE_RELEASE,
        WINDOW_OPENED,
        WINDOW_CLOSING,
        WINDOW_CLOSED,
        WINDOW_MINIMIZED,
        WINDOW_RESTORED,
        WINDOW_MAXIMIZED,
        WINDOW_UNMAXIMIZED,
        WINDOW_FOCUSED,
        WINDOW_UNFOCUSED,
        WINDOW_MOVED,
        WINDOW_RESIZED
    }
    
    // Default filtered events (configurable)
    private static final EnumSet<EventType> DEFAULT_FILTERED_EVENTS = EnumSet.of(
        EventType.MOUSE_MOVE,
        EventType.MOUSE_PRESS,
        EventType.MOUSE_RELEASE,
        EventType.WINDOW_MOVED,
        EventType.WINDOW_RESIZED,
        EventType.WINDOW_OPENED,
        EventType.WINDOW_MINIMIZED,
        EventType.WINDOW_RESTORED,
        EventType.WINDOW_MAXIMIZED,
        EventType.WINDOW_UNMAXIMIZED,
        EventType.WINDOW_FOCUSED,
        EventType.WINDOW_UNFOCUSED
    );
    
    private final EnumSet<EventType> filteredEvents;
    
    public EventFilter() {
        this.filteredEvents = EnumSet.copyOf(DEFAULT_FILTERED_EVENTS);
    }
    
    public EventFilter(EnumSet<EventType> customFilters) {
        this.filteredEvents = EnumSet.copyOf(customFilters);
    }
    
    public boolean isFiltered(EventType type) {
        return filteredEvents.contains(type);
    }
    
    public void addFilter(EventType type) {
        filteredEvents.add(type);
    }
    
    public void removeFilter(EventType type) {
        filteredEvents.remove(type);
    }
    
    public void setFilters(EnumSet<EventType> filters) {
        filteredEvents.clear();
        filteredEvents.addAll(filters);
    }
    
    public EnumSet<EventType> getFilters() {
        return EnumSet.copyOf(filteredEvents);
    }
}