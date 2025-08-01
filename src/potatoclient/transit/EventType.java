package potatoclient.transit;

import com.cognitect.transit.Keyword;
import com.cognitect.transit.TransitFactory;

/**
 * Event type constants for all events in the system.
 * Uses Transit Keywords for automatic conversion in Clojure.
 */
public enum EventType {
    // Navigation events
    NAVIGATION("navigation"),
    
    // Window events
    WINDOW("window"),
    
    // Frame events
    FRAME("frame"),
    
    // Error events
    ERROR("error"),
    
    // Gesture events
    GESTURE("gesture"),
    
    // Gesture subtypes (for gesture-type field)
    TAP("tap"),
    DOUBLE_TAP("doubletap"),
    PAN_START("panstart"),
    PAN_MOVE("panmove"),
    PAN_STOP("panstop"),
    SWIPE("swipe"),
    
    // Navigation subtypes (for nav-type field)
    MOUSE_MOVE("mouse-move"),
    MOUSE_CLICK("mouse-click"),
    MOUSE_DRAG("mouse-drag"),
    MOUSE_WHEEL("mouse-wheel"),
    
    // Window subtypes (for window-type field)
    RESIZE("resize"),
    MOVE("move"),
    FOCUS("focus"),
    MINIMIZE("minimize"),
    MAXIMIZE("maximize"),
    RESTORE("restore"),
    CLOSE("close");
    
    private final String key;
    private final Keyword keyword;
    
    EventType(String key) {
        this.key = key;
        this.keyword = TransitFactory.keyword(key);
    }
    
    public String getKey() {
        return key;
    }
    
    public Keyword getKeyword() {
        return keyword;
    }
    
    public static EventType fromKey(String key) {
        for (EventType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return null;
    }
    
    public static EventType fromKeyword(Keyword keyword) {
        return fromKey(keyword.toString());
    }
    
    /**
     * Check if this is a gesture event type
     */
    public boolean isGestureType() {
        return this == TAP || this == DOUBLE_TAP || 
               this == PAN_START || this == PAN_MOVE || this == PAN_STOP || 
               this == SWIPE;
    }
    
    /**
     * Check if this is a navigation event type
     */
    public boolean isNavigationType() {
        return this == MOUSE_MOVE || this == MOUSE_CLICK || 
               this == MOUSE_DRAG || this == MOUSE_WHEEL;
    }
    
    /**
     * Check if this is a window event type
     */
    public boolean isWindowType() {
        return this == RESIZE || this == MOVE || this == FOCUS ||
               this == MINIMIZE || this == MAXIMIZE || this == RESTORE || 
               this == CLOSE;
    }
}