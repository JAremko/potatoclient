package potatoclient.kotlin

import com.fasterxml.jackson.databind.ObjectMapper
import java.awt.Component
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles all communication protocol with the parent Clojure process.
 * Encapsulates message formatting and JSON serialization.
 */
class MessageProtocol(private val streamId: String) {
    private val mapper = ObjectMapper()
    
    // Pre-allocated message templates to reduce garbage
    private val responseTemplate = ConcurrentHashMap<String, Any>(Constants.MAP_INITIAL_CAPACITY).apply {
        put("type", "response")
        put("streamId", streamId)
    }
    
    private val logTemplate = ConcurrentHashMap<String, Any>(Constants.MAP_INITIAL_CAPACITY).apply {
        put("type", "log")
        put("streamId", streamId)
    }
    
    private val navTemplate = ConcurrentHashMap<String, Any>(Constants.MAP_INITIAL_CAPACITY).apply {
        put("type", "navigation")
        put("streamId", streamId)
    }
    
    private val windowTemplate = ConcurrentHashMap<String, Any>(Constants.MAP_INITIAL_CAPACITY).apply {
        put("type", "window")
        put("streamId", streamId)
    }
    
    fun sendResponse(status: String, data: String) {
        try {
            val resp = HashMap(responseTemplate).apply {
                put("status", status)
                put("data", data)
                put("timestamp", System.currentTimeMillis())
            }
            sendMessage(resp)
        } catch (e: Exception) {
            // Can't use sendException here as it might cause infinite recursion
            System.err.println("[MessageProtocol] Error sending response: ${e.javaClass.name}: ${e.message}")
        }
    }
    
    fun sendLog(level: String, message: String) {
        try {
            val log = HashMap(logTemplate).apply {
                put("level", level)
                put("message", message)
                put("timestamp", System.currentTimeMillis())
            }
            sendMessage(log)
        } catch (e: Exception) {
            // Can't use sendException here as it might cause infinite recursion
            System.err.println("[MessageProtocol] Error sending log: ${e.javaClass.name}: ${e.message}")
        }
    }
    
    fun sendException(context: String, ex: Exception) {
        try {
            val sw = StringWriter(Constants.STRING_WRITER_INITIAL_SIZE)
            val pw = PrintWriter(sw)
            ex.printStackTrace(pw)
            
            val log = HashMap(logTemplate).apply {
                put("level", "ERROR")
                put("message", "$context: ${ex.message}")
                put("stackTrace", sw.toString())
                put("timestamp", System.currentTimeMillis())
            }
            sendMessage(log)
        } catch (e: Exception) {
            // Last resort - print to stderr
            System.err.println("[MessageProtocol] Error sending exception: ${e.javaClass.name}: ${e.message}")
        }
    }
    
    // Pre-allocated constants for event keys
    private companion object {
        const val KEY_TYPE = "type"
        const val KEY_X = "x"
        const val KEY_Y = "y"
        const val KEY_NDC_X = "ndcX"
        const val KEY_NDC_Y = "ndcY"
        const val KEY_CANVAS_WIDTH = "canvasWidth"
        const val KEY_CANVAS_HEIGHT = "canvasHeight"
        const val KEY_EVENT = "event"
        const val KEY_TIMESTAMP = "timestamp"
    }
    
    // Thread-local event map to avoid allocations
    private val threadLocalEventMap = ThreadLocal.withInitial { 
        HashMap<String, Any>(Constants.MAP_INITIAL_CAPACITY * 2)
    }
    
    fun sendNavigationEvent(
        eventType: String,
        x: Int,
        y: Int,
        videoComponent: Component,
        details: Map<String, Any>? = null
    ) {
        try {
            // Get canvas dimensions
            val canvasWidth = videoComponent.width
            val canvasHeight = videoComponent.height
            
            // Avoid division by zero
            if (canvasWidth == 0 || canvasHeight == 0) return
            
            // Pre-calculate reciprocals to avoid division
            val widthReciprocal = 2.0 / canvasWidth
            val heightReciprocal = 2.0 / canvasHeight
            
            // Convert to NDC coordinates (-1 to 1 range)
            val ndcX = x * widthReciprocal - 1.0
            val ndcY = y * heightReciprocal - 1.0
            
            // Reuse thread-local map to avoid allocation
            val event = threadLocalEventMap.get().apply {
                clear()
                put(KEY_TYPE, eventType)
                put(KEY_X, x)
                put(KEY_Y, y)
                put(KEY_NDC_X, ndcX)
                put(KEY_NDC_Y, ndcY)
                put(KEY_CANVAS_WIDTH, canvasWidth)
                put(KEY_CANVAS_HEIGHT, canvasHeight)
                details?.let { putAll(it) }
            }
            
            val nav = HashMap(navTemplate).apply {
                put(KEY_EVENT, HashMap(event)) // Copy event map since it's reused
                put(KEY_TIMESTAMP, System.currentTimeMillis())
            }
            
            sendMessage(nav)
        } catch (e: Exception) {
            sendException("Navigation event error", e)
        }
    }

    fun sendWindowEvent(eventType: String, details: Map<String, Any>? = null) {
        try {
            val event = HashMap<String, Any>(2 + (details?.size ?: 0)).apply {
                put(KEY_TYPE, eventType)
                details?.let { putAll(it) }
            }
            
            val window = HashMap(windowTemplate).apply {
                put(KEY_EVENT, event)
                put(KEY_TIMESTAMP, System.currentTimeMillis())
            }
            
            sendMessage(window)
        } catch (e: Exception) {
            sendException("Window event error", e)
        }
    }
    
    @Suppress("NOTHING_TO_INLINE")
    private inline fun sendMessage(message: Map<String, Any>) {
        val json = mapper.writeValueAsString(message)
        println(json)
        System.out.flush()
    }
}