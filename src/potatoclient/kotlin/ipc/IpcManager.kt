package potatoclient.kotlin.ipc

import kotlinx.coroutines.*
import java.nio.file.Path

/**
 * High-level IPC manager that coordinates Transit communication over Unix Domain Sockets.
 * This replaces the old stdin/stdout-based communication.
 */
class IpcManager private constructor(
    private val streamId: String,
    private val socketPath: Path? = null
) {
    private lateinit var communicator: TransitSocketCommunicator
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val incomingHandlers = mutableListOf<(Map<*, *>) -> Unit>()

    @Volatile
    private var isInitialized = false

    /**
     * Initialize the IPC manager and establish connection.
     */
    fun initialize() {
        if (isInitialized) {
            return
        }

        synchronized(this) {
            if (isInitialized) {
                return
            }

            // Create communicator based on configuration
            communicator = if (socketPath != null) {
                // Use specific socket path (for testing or custom setup)
                TransitSocketCommunicator.createWithPath(socketPath, streamId, false)
            } else {
                // Use singleton instance with auto-generated path
                TransitSocketCommunicator.getInstance(streamId)
            }

            // Start the communicator
            runBlocking {
                communicator.start()
            }

            // Start message processing loop
            scope.launch {
                processIncomingMessages()
            }

            isInitialized = true

            // Send initial connection event
            sendConnectionEvent(IpcKeys.CONNECTED)
        }
    }

    /**
     * Register a handler for incoming messages.
     */
    fun onMessage(handler: (Map<*, *>) -> Unit) {
        incomingHandlers.add(handler)
    }

    /**
     * Send a window event.
     */
    fun sendWindowEvent(
        action: String,
        width: Int? = null,
        height: Int? = null,
        x: Int? = null,
        y: Int? = null,
        deltaX: Int? = null,
        deltaY: Int? = null
    ) {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        val message = MessageBuilders.windowEvent(
            IpcKeys.keyword(action),
            width, height, x, y, deltaX, deltaY
        )

        sendMessage(message)
    }

    /**
     * Send a connection event.
     */
    fun sendConnectionEvent(action: com.cognitect.transit.Keyword, details: Map<Any, Any>? = null) {
        if (!isInitialized && action != IpcKeys.CONNECTED) {
            System.err.println("IpcManager not initialized")
            return
        }

        val message = MessageBuilders.connectionEvent(action, details)
        sendMessage(message)
    }

    /**
     * Send a log message.
     */
    fun sendLog(level: String, message: String, data: Map<Any, Any>? = null) {
        if (!isInitialized) {
            // Allow logging even before initialization
            System.err.println("[$streamId] $level: $message")
            return
        }

        val logMessage = MessageBuilders.log(
            IpcKeys.logLevel(level),
            message,
            streamId,
            data
        )

        sendMessage(logMessage)
    }

    /**
     * Send a gesture event.
     * @param gestureType The type of gesture (tap, double-tap, pan-start, etc.)
     * @param x X coordinate in pixels
     * @param y Y coordinate in pixels
     * @param frameTimestamp Timestamp of the video frame when gesture occurred
     * @param deltaX Optional delta X for pan-move events
     * @param deltaY Optional delta Y for pan-move events
     * @param scrollAmount Optional scroll amount for wheel events
     */
    fun sendGestureEvent(
        gestureType: com.cognitect.transit.Keyword,
        x: Int,
        y: Int,
        frameTimestamp: Long,
        deltaX: Int? = null,
        deltaY: Int? = null,
        scrollAmount: Int? = null
    ) {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        val message = MessageBuilders.gestureEvent(
            gestureType,
            IpcKeys.streamType(streamId),
            x, y, frameTimestamp,
            deltaX, deltaY, scrollAmount
        )

        sendMessage(message)
    }
    
    /**
     * Send a gesture event using GestureEvent object.
     */
    fun sendGestureEvent(event: potatoclient.kotlin.gestures.GestureEvent) {
        when (event) {
            is potatoclient.kotlin.gestures.GestureEvent.Tap -> 
                sendGestureEvent(IpcKeys.TAP, event.x, event.y, event.frameTimestamp)
            is potatoclient.kotlin.gestures.GestureEvent.DoubleTap -> 
                sendGestureEvent(IpcKeys.DOUBLE_TAP, event.x, event.y, event.frameTimestamp)
            is potatoclient.kotlin.gestures.GestureEvent.PanStart -> 
                sendGestureEvent(IpcKeys.PAN_START, event.x, event.y, event.frameTimestamp)
            is potatoclient.kotlin.gestures.GestureEvent.PanMove -> 
                sendGestureEvent(IpcKeys.PAN_MOVE, event.x, event.y, event.frameTimestamp, 
                    event.deltaX, event.deltaY)
            is potatoclient.kotlin.gestures.GestureEvent.PanStop -> 
                sendGestureEvent(IpcKeys.PAN_STOP, event.x, event.y, event.frameTimestamp)
            is potatoclient.kotlin.gestures.GestureEvent.WheelUp -> 
                sendGestureEvent(IpcKeys.WHEEL_UP, event.x, event.y, event.frameTimestamp,
                    scrollAmount = event.scrollAmount)
            is potatoclient.kotlin.gestures.GestureEvent.WheelDown -> 
                sendGestureEvent(IpcKeys.WHEEL_DOWN, event.x, event.y, event.frameTimestamp,
                    scrollAmount = event.scrollAmount)
        }
    }

    /**
     * Send a raw message.
     */
    fun sendMessage(message: Map<Any, Any>) {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        scope.launch {
            try {
                communicator.sendMessage(message)
            } catch (e: Exception) {
                System.err.println("Failed to send message: ${e.message}")
            }
        }
    }

    /**
     * Send a message synchronously (blocking).
     */
    fun sendMessageSync(message: Map<Any, Any>) {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        try {
            communicator.sendMessageDirect(message)
        } catch (e: Exception) {
            System.err.println("Failed to send message: ${e.message}")
        }
    }

    /**
     * Process incoming messages and dispatch to handlers.
     */
    private suspend fun processIncomingMessages() {
        while (communicator.isRunning()) {
            try {
                val message = communicator.readMessage()
                if (message != null) {
                    // Dispatch to all registered handlers
                    incomingHandlers.forEach { handler ->
                        try {
                            handler(message)
                        } catch (e: Exception) {
                            System.err.println("Handler error: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                if (communicator.isRunning()) {
                    System.err.println("Error processing message: ${e.message}")
                }
            }
        }
    }

    /**
     * Shutdown the IPC manager.
     */
    fun shutdown() {
        if (!isInitialized) {
            return
        }

        // Send disconnection event
        sendConnectionEvent(IpcKeys.DISCONNECTED)

        // Stop communicator
        communicator.stop()

        // Clear handlers
        incomingHandlers.clear()

        isInitialized = false
    }

    /**
     * Check if the manager is running.
     */
    fun isRunning(): Boolean = isInitialized && communicator.isRunning()

    companion object {
        @Volatile
        private var instance: IpcManager? = null

        /**
         * Get the singleton instance for the video stream process.
         */
        @JvmStatic
        fun getInstance(streamId: String): IpcManager {
            return instance ?: synchronized(this) {
                instance ?: IpcManager(streamId).also {
                    instance = it
                }
            }
        }

        /**
         * Create an instance with a specific socket path (for testing).
         */
        @JvmStatic
        fun createWithPath(streamId: String, socketPath: Path): IpcManager {
            return IpcManager(streamId, socketPath)
        }

        /**
         * Reset the singleton (mainly for testing).
         */
        @JvmStatic
        fun reset() {
            instance?.shutdown()
            instance = null
        }
    }
}
