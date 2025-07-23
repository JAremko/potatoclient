package potatoclient.kotlin

/**
 * Central location for all constants used across the video streaming system.
 */
object Constants {
    // Timing constants (milliseconds)
    const val MOUSE_EVENT_THROTTLE_MS = 50
    const val WINDOW_EVENT_THROTTLE_MS = 100
    const val DOUBLE_CLICK_WINDOW_MS = 200

    // GStreamer constants
    const val QUEUE_MAX_BUFFERS = 5
    const val QUEUE_MAX_TIME_NS = 1_000_000_000L // 1 second
    const val FRAME_LOG_INTERVAL = 300
    // Thread pool sizes
    const val EVENT_THROTTLE_POOL_SIZE = 2
    // Executor shutdown timeouts
    const val EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 1L
    // UI constants
    const val DOUBLE_CLICK_MAX_DISTANCE = 3 // pixels
    // Stream configurations
    object StreamConfig {
        const val HEAT_STREAM_ID = "heat"
        const val HEAT_STREAM_WIDTH = 900
        const val HEAT_STREAM_HEIGHT = 720
        
        const val DAY_STREAM_DISPLAY_WIDTH = 960
        const val DAY_STREAM_DISPLAY_HEIGHT = 540
    }
    
    // WebSocket headers
    const val WS_USER_AGENT = "VideoStreamManager/1.0"
    const val WS_CACHE_CONTROL = "no-cache"
    const val WS_PRAGMA = "no-cache"
    
    // GStreamer pipeline
    const val H264_CAPS = "video/x-h264,stream-format=byte-stream,alignment=nal"
    const val GSTREAMER_APP_NAME = "VideoStreamManager"
    
    // Buffer sizes
    const val STRING_BUILDER_INITIAL_SIZE = 256
    const val STRING_WRITER_INITIAL_SIZE = 512
    const val MAP_INITIAL_CAPACITY = 8
    
    // Buffer pooling
    const val BUFFER_POOL_SIZE = 10
    const val MAX_BUFFER_SIZE = 2 * 1024 * 1024 // 2MB max frame size
}