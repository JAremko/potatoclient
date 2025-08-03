package potatoclient.java.transit;

import com.cognitect.transit.Keyword;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for working with Transit enums in Clojure.
 * Provides convenient methods for enum-to-keyword conversions.
 */
public final class EnumUtils {
    
    /**
     * Get all MessageType values as a map of keywords to enum values
     * Useful for Clojure interop
     */
    public static Map<Keyword, MessageType> getMessageTypeKeywordMap() {
        Map<Keyword, MessageType> map = new HashMap<>();
        for (MessageType type : MessageType.values()) {
            map.put(type.getKeyword(), type);
        }
        return map;
    }
    
    /**
     * Get all EventType values as a map of keywords to enum values
     * Useful for Clojure interop
     */
    public static Map<Keyword, EventType> getEventTypeKeywordMap() {
        Map<Keyword, EventType> map = new HashMap<>();
        for (EventType type : EventType.values()) {
            map.put(type.getKeyword(), type);
        }
        return map;
    }
    
    /**
     * Check if a string is a valid MessageType key
     */
    public static boolean isValidMessageType(String key) {
        return MessageType.fromKey(key) != null;
    }
    
    /**
     * Check if a string is a valid EventType key
     */
    public static boolean isValidEventType(String key) {
        return EventType.fromKey(key) != null;
    }
    
    // Private constructor to prevent instantiation
    private EnumUtils() {}
}