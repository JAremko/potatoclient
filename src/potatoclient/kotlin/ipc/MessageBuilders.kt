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
     * Build a command message (to be forwarded to server).
     */
    @JvmStatic
    fun command(
        action: Keyword,
        streamType: Keyword,
        params: Map<Any, Any> = emptyMap()
    ): Map<Any, Any> = mapOf(
        IpcKeys.MSG_TYPE to IpcKeys.COMMAND,
        IpcKeys.MSG_ID to UUID.randomUUID().toString(),
        IpcKeys.TIMESTAMP to System.currentTimeMillis(),
        IpcKeys.ACTION to action,
        IpcKeys.STREAM_TYPE to streamType
    ) + params
    
    /**
     * Build a rotary goto command.
     */
    @JvmStatic
    fun rotaryGotoNdc(
        streamType: Keyword,
        ndcX: Double,
        ndcY: Double
    ): Map<Any, Any> = command(
        IpcKeys.ROTARY_GOTO_NDC,
        streamType,
        mapOf(
            IpcKeys.NDC_X to ndcX,
            IpcKeys.NDC_Y to ndcY
        )
    )
    
    /**
     * Build a CV track command.
     */
    @JvmStatic
    fun cvStartTrackNdc(
        streamType: Keyword,
        ndcX: Double,
        ndcY: Double
    ): Map<Any, Any> = command(
        IpcKeys.CV_START_TRACK_NDC,
        streamType,
        mapOf(
            IpcKeys.NDC_X to ndcX,
            IpcKeys.NDC_Y to ndcY
        )
    )
    
    /**
     * Build a rotary velocity command.
     */
    @JvmStatic
    fun rotarySetVelocity(
        streamType: Keyword,
        azimuthSpeed: Double,
        elevationSpeed: Double,
        azimuthDirection: Keyword
    ): Map<Any, Any> = command(
        IpcKeys.ROTARY_SET_VELOCITY,
        streamType,
        mapOf(
            IpcKeys.AZIMUTH_SPEED to azimuthSpeed,
            IpcKeys.ELEVATION_SPEED to elevationSpeed,
            IpcKeys.AZIMUTH_DIRECTION to azimuthDirection
        )
    )
    
    /**
     * Build a rotary halt command.
     */
    @JvmStatic
    fun rotaryHalt(streamType: Keyword): Map<Any, Any> = command(
        IpcKeys.ROTARY_HALT,
        streamType
    )
    
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