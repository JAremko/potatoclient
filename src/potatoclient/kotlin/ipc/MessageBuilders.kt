package potatoclient.kotlin.ipc

import com.cognitect.transit.Keyword
import java.util.UUID

/**
 * Helper functions for building IPC messages according to the protocol.
 */
object MessageBuilders {
    
    /**
     * Build a window event message.
     */
    @JvmStatic
    fun windowEvent(
        action: Keyword,
        width: Int? = null,
        height: Int? = null,
        x: Int? = null,
        y: Int? = null,
        deltaX: Int? = null,
        deltaY: Int? = null
    ): Map<Any, Any> = buildMap {
        put(IpcKeys.MSG_TYPE, IpcKeys.EVENT)
        put(IpcKeys.MSG_ID, UUID.randomUUID().toString())
        put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
        put(IpcKeys.TYPE, IpcKeys.WINDOW)
        put(IpcKeys.ACTION, action)
        
        // Add optional parameters
        width?.let { put(IpcKeys.WIDTH, it) }
        height?.let { put(IpcKeys.HEIGHT, it) }
        x?.let { put(IpcKeys.X, it) }
        y?.let { put(IpcKeys.Y, it) }
        deltaX?.let { put(IpcKeys.DELTA_X, it) }
        deltaY?.let { put(IpcKeys.DELTA_Y, it) }
    }
    
    /**
     * Build a connection event message.
     */
    @JvmStatic
    fun connectionEvent(
        action: Keyword,
        details: Map<Any, Any>? = null
    ): Map<Any, Any> = buildMap {
        put(IpcKeys.MSG_TYPE, IpcKeys.EVENT)
        put(IpcKeys.MSG_ID, UUID.randomUUID().toString())
        put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
        put(IpcKeys.TYPE, IpcKeys.CONNECTION)
        put(IpcKeys.ACTION, action)
        
        details?.let { put(IpcKeys.DETAILS, it) }
    }
    
    /**
     * Build a gesture event message.
     */
    @JvmStatic
    fun gestureEvent(
        gestureType: Keyword,
        streamType: Keyword,
        x: Int,
        y: Int,
        frameTimestamp: Long,
        deltaX: Int? = null,
        deltaY: Int? = null,
        scrollAmount: Int? = null
    ): Map<Any, Any> = buildMap {
        put(IpcKeys.MSG_TYPE, IpcKeys.EVENT)
        put(IpcKeys.MSG_ID, UUID.randomUUID().toString())
        put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
        put(IpcKeys.TYPE, IpcKeys.GESTURE)
        put(IpcKeys.GESTURE_TYPE, gestureType)
        put(IpcKeys.STREAM_TYPE, streamType)
        put(IpcKeys.X, x)
        put(IpcKeys.Y, y)
        put(IpcKeys.FRAME_TIMESTAMP, frameTimestamp)
        
        // Optional parameters for specific gesture types
        deltaX?.let { put(IpcKeys.DELTA_X, it) }
        deltaY?.let { put(IpcKeys.DELTA_Y, it) }
        scrollAmount?.let { put(IpcKeys.SCROLL_AMOUNT, it) }
    }
    
    /**
     * Build a log message.
     */
    @JvmStatic
    fun log(
        level: Keyword,
        message: String,
        process: String,
        data: Map<Any, Any>? = null
    ): Map<Any, Any> = buildMap {
        put(IpcKeys.MSG_TYPE, IpcKeys.LOG)
        put(IpcKeys.MSG_ID, UUID.randomUUID().toString())
        put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
        put(IpcKeys.LEVEL, level)
        put(IpcKeys.MESSAGE, message)
        put(IpcKeys.PROCESS, process)
        
        data?.let { put(IpcKeys.DATA, it) }
    }
    
    /**
     * Build a metric message.
     */
    @JvmStatic
    fun metric(
        name: String,
        value: Any,
        tags: Map<String, String>? = null
    ): Map<Any, Any> = buildMap {
        put(IpcKeys.MSG_TYPE, IpcKeys.METRIC)
        put(IpcKeys.MSG_ID, UUID.randomUUID().toString())
        put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
        put(IpcKeys.PAYLOAD, buildMap {
            put("name", name)
            put("value", value)
            tags?.let { put("tags", it) }
        })
    }
}