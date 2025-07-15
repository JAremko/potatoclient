package potatoclient.java;

/**
 * Central location for all constants used across the video streaming system.
 */
public final class Constants {
    // Prevent instantiation
    private Constants() {}
    
    // Timing constants (milliseconds)
    public static final int RECONNECT_DELAY_MS = 1000;
    public static final int MOUSE_EVENT_THROTTLE_MS = 50;
    public static final int WINDOW_EVENT_THROTTLE_MS = 100;
    public static final int DOUBLE_CLICK_WINDOW_MS = 200;
    public static final long COMMAND_THREAD_SLEEP_MS = 100;
    public static final long WEBSOCKET_CLOSE_WAIT_MS = 100;
    public static final long RECONNECT_CLOSE_WAIT_MS = 50;
    
    // GStreamer constants
    public static final int QUEUE_MAX_BUFFERS = 5;
    public static final long QUEUE_MAX_TIME_NS = 1_000_000_000L; // 1 second
    public static final int FRAME_LOG_INTERVAL = 300;
    
    // Thread pool sizes
    public static final int EVENT_THROTTLE_POOL_SIZE = 2;
    
    // Executor shutdown timeouts
    public static final long EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 1;
    
    // UI constants
    public static final int DOUBLE_CLICK_MAX_DISTANCE = 3; // pixels
    
    // Stream configurations
    public static final class StreamConfig {
        public static final String HEAT_STREAM_ID = "heat";
        public static final String HEAT_STREAM_TITLE = "Heat Stream (900x720)";
        public static final int HEAT_STREAM_WIDTH = 900;
        public static final int HEAT_STREAM_HEIGHT = 720;
        
        public static final String DAY_STREAM_TITLE = "Day Stream (1920x1080)";
        public static final int DAY_STREAM_DISPLAY_WIDTH = 960;
        public static final int DAY_STREAM_DISPLAY_HEIGHT = 540;
    }
    
    // WebSocket headers
    public static final String WS_USER_AGENT = "VideoStreamManager/1.0";
    public static final String WS_CACHE_CONTROL = "no-cache";
    public static final String WS_PRAGMA = "no-cache";
    
    // GStreamer pipeline
    public static final String H264_CAPS = "video/x-h264,stream-format=byte-stream,alignment=nal";
    public static final String GSTREAMER_APP_NAME = "VideoStreamManager";
    
    // Buffer sizes
    public static final int STRING_BUILDER_INITIAL_SIZE = 256;
    public static final int STRING_WRITER_INITIAL_SIZE = 512;
    public static final int MAP_INITIAL_CAPACITY = 8;
}