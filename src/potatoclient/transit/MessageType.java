package potatoclient.transit;

/**
 * Transit message types used for communication between Clojure and Kotlin processes.
 * Using a Java enum ensures both sides stay in sync and can use the same constants.
 */
public enum MessageType {
    // Core message types
    COMMAND("command"),
    RESPONSE("response"),
    LOG("log"),
    ERROR("error"),
    STATUS("status"),
    METRIC("metric"),
    EVENT("event"),
    
    // Specific event types
    EVENT_NAVIGATION("navigation"),
    EVENT_WINDOW("window"),
    EVENT_FRAME("frame"),
    EVENT_ERROR("error"),
    
    // State-specific types
    STATE_UPDATE("state-update"),
    STATE_PARTIAL("state-partial"),
    
    // Video stream specific
    STREAM_READY("stream-ready"),
    STREAM_ERROR("stream-error"),
    STREAM_CLOSED("stream-closed");
    
    private final String key;
    
    MessageType(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
    
    public static MessageType fromKey(String key) {
        for (MessageType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return null;
    }
}