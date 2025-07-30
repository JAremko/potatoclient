package potatoclient.transit;

/**
 * Common message payload keys used in Transit communication.
 * These constants ensure consistency between Clojure and Kotlin code.
 */
public final class MessageKeys {
    // Core message structure
    public static final String MSG_TYPE = "msg-type";
    public static final String MSG_ID = "msg-id";
    public static final String TIMESTAMP = "timestamp";
    public static final String PAYLOAD = "payload";
    
    // Common payload keys
    public static final String ACTION = "action";
    public static final String PROCESS = "process";
    public static final String STREAM_ID = "streamId";
    public static final String LEVEL = "level";
    public static final String MESSAGE = "message";
    public static final String CONTEXT = "context";
    public static final String STACK_TRACE = "stackTrace";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String ERROR = "error";
    public static final String DETAILS = "details";
    public static final String STATUS = "status";
    public static final String CLASS = "class";
    
    // Event-specific keys
    public static final String X = "x";
    public static final String Y = "y";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String CANVAS_WIDTH = "canvasWidth";
    public static final String CANVAS_HEIGHT = "canvasHeight";
    public static final String NDC_X = "ndcX";
    public static final String NDC_Y = "ndcY";
    public static final String FRAME_TIMESTAMP = "frameTimestamp";
    public static final String FRAME_DURATION = "frameDuration";
    public static final String WINDOW_STATE = "windowState";
    public static final String NAV_TYPE = "navType";
    
    // Private constructor to prevent instantiation
    private MessageKeys() {}
}