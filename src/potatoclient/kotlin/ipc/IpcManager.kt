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
     * Send a command message.
     */
    fun sendCommand(action: String, params: Map<Any, Any> = emptyMap()) {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        val message = MessageBuilders.command(
            IpcKeys.keyword(action),
            IpcKeys.streamType(streamId),
            params
        )

        sendMessage(message)
    }

    // ============================================================================
    // Command Helper Functions - RPC-style interface for Clojure commands
    // ============================================================================

    /**
     * Send rotary platform goto NDC command.
     * Rotates platform to point at normalized device coordinates.
     * @param channel Video channel (DAY or HEAT)
     * @param x NDC x coordinate (-1.0 to 1.0)
     * @param y NDC y coordinate (-1.0 to 1.0)
     */
    fun sendRotaryGotoNdc(channel: com.cognitect.transit.Keyword, x: Double, y: Double) {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        val params: Map<Any, Any> = mapOf(
            IpcKeys.CHANNEL to channel,
            IpcKeys.NDC_X to x,
            IpcKeys.NDC_Y to y
        )

        val message = MessageBuilders.command(
            IpcKeys.ROTARY_GOTO_NDC,
            IpcKeys.streamType(streamId),
            params
        )

        sendMessage(message)
    }

    /**
     * Send CV start track NDC command.
     * Starts tracking at normalized device coordinates.
     * @param channel Video channel (DAY or HEAT)
     * @param x NDC x coordinate (-1.0 to 1.0)
     * @param y NDC y coordinate (-1.0 to 1.0)
     * @param frameTime Timestamp of the frame being tracked
     */
    fun sendCvStartTrackNdc(channel: com.cognitect.transit.Keyword, x: Double, y: Double, frameTime: Long) {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        val params: Map<Any, Any> = mapOf(
            IpcKeys.CHANNEL to channel,
            IpcKeys.NDC_X to x,
            IpcKeys.NDC_Y to y,
            IpcKeys.FRAME_TIME to frameTime
        )

        val message = MessageBuilders.command(
            IpcKeys.CV_START_TRACK_NDC,
            IpcKeys.streamType(streamId),
            params
        )

        sendMessage(message)
    }

    /**
     * Send rotary set velocity command.
     * Sets continuous rotation velocities for both axes.
     * @param azimuthSpeed Speed for azimuth axis (0.0 to 1.0)
     * @param azimuthDirection Direction for azimuth (CLOCKWISE or COUNTER_CLOCKWISE)
     * @param elevationSpeed Speed for elevation axis (0.0 to 1.0)
     * @param elevationDirection Direction for elevation (CLOCKWISE or COUNTER_CLOCKWISE)
     */
    fun sendRotarySetVelocity(
        azimuthSpeed: Double,
        azimuthDirection: com.cognitect.transit.Keyword,
        elevationSpeed: Double,
        elevationDirection: com.cognitect.transit.Keyword
    ) {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        val params: Map<Any, Any> = mapOf(
            IpcKeys.AZIMUTH_SPEED to azimuthSpeed,
            IpcKeys.AZIMUTH_DIRECTION to azimuthDirection,
            IpcKeys.ELEVATION_SPEED to elevationSpeed,
            IpcKeys.ELEVATION_DIRECTION to elevationDirection
        )

        val message = MessageBuilders.command(
            IpcKeys.ROTARY_SET_VELOCITY,
            IpcKeys.streamType(streamId),
            params
        )

        sendMessage(message)
    }

    /**
     * Send rotary halt command.
     * Immediately stops all rotary platform movement.
     */
    fun sendRotaryHalt() {
        if (!isInitialized) {
            System.err.println("IpcManager not initialized")
            return
        }

        val message = MessageBuilders.command(
            IpcKeys.ROTARY_HALT,
            IpcKeys.streamType(streamId),
            emptyMap()
        )

        sendMessage(message)
    }

    // ============================================================================
    // Convenience overloads with string parameters
    // ============================================================================

    /**
     * Convenience overload for sendRotaryGotoNdc with string channel.
     * @param channel "day" or "heat"
     */
    fun sendRotaryGotoNdc(channel: String, x: Double, y: Double) {
        val channelKeyword = when (channel.lowercase()) {
            "day" -> IpcKeys.DAY
            "heat" -> IpcKeys.HEAT
            else -> IpcKeys.keyword(channel)
        }
        sendRotaryGotoNdc(channelKeyword, x, y)
    }

    /**
     * Convenience overload for sendCvStartTrackNdc with string channel.
     * @param channel "day" or "heat"
     */
    fun sendCvStartTrackNdc(channel: String, x: Double, y: Double, frameTime: Long) {
        val channelKeyword = when (channel.lowercase()) {
            "day" -> IpcKeys.DAY
            "heat" -> IpcKeys.HEAT
            else -> IpcKeys.keyword(channel)
        }
        sendCvStartTrackNdc(channelKeyword, x, y, frameTime)
    }

    /**
     * Convenience overload for sendRotarySetVelocity with string directions.
     * @param azimuthDirection "clockwise" or "counter-clockwise"
     * @param elevationDirection "clockwise" or "counter-clockwise"
     */
    fun sendRotarySetVelocity(
        azimuthSpeed: Double,
        azimuthDirection: String,
        elevationSpeed: Double,
        elevationDirection: String
    ) {
        val azimuthDirKeyword = when (azimuthDirection.lowercase()) {
            "clockwise" -> IpcKeys.CLOCKWISE
            "counter-clockwise", "counterclockwise" -> IpcKeys.COUNTER_CLOCKWISE
            else -> IpcKeys.keyword(azimuthDirection)
        }
        val elevationDirKeyword = when (elevationDirection.lowercase()) {
            "clockwise" -> IpcKeys.CLOCKWISE
            "counter-clockwise", "counterclockwise" -> IpcKeys.COUNTER_CLOCKWISE
            else -> IpcKeys.keyword(elevationDirection)
        }
        sendRotarySetVelocity(azimuthSpeed, azimuthDirKeyword, elevationSpeed, elevationDirKeyword)
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
