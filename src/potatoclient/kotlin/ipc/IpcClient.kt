package potatoclient.kotlin.ipc

import com.cognitect.transit.Keyword
import com.cognitect.transit.Reader
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Writer
import potatoclient.java.ipc.SocketFactory
import potatoclient.java.ipc.UnixSocketCommunicator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

/**
 * IPC Client for video stream subprocesses.
 * Sends events, commands, logs, and metrics to the server.
 * Can only receive CLOSE_REQUEST from server.
 */
class IpcClient(
    private val socketPath: Path,
    private val streamName: String  // e.g., "heat" or "day"
) {
    private lateinit var socketComm: UnixSocketCommunicator
    private val isRunning = AtomicBoolean(false)
    private val closeRequestReceived = AtomicBoolean(false)

    @Volatile
    private var listenerThread: Thread? = null

    // Callback for close request
    private var onCloseRequest: (() -> Unit)? = null
    
    // Message handlers
    private val messageHandlers = mutableListOf<(Map<*, *>) -> Unit>()

    // Transit handlers for custom types
    private val readHandlers = mapOf(
        "kw" to com.cognitect.transit.ReadHandler<Any, Any> { rep ->
            TransitFactory.keyword(rep.toString())
        }
    )

    /**
     * Initialize the IPC client and establish connection.
     * Alias for connect() to match IpcManager API.
     */
    fun initialize() {
        connect()
    }
    
    /**
     * Connect to the server and start listening for close requests.
     * @param retryOnFailure If true, retries connection if initial attempt fails
     * @param maxRetries Maximum number of connection attempts
     */
    fun connect(retryOnFailure: Boolean = true, maxRetries: Int = 5) {
        if (isRunning.getAndSet(true)) {
            throw IllegalStateException("Client already connected")
        }

        var lastException: Exception? = null
        var attempts = 0

        while (attempts < maxRetries) {
            try {
                // Check if socket file exists before attempting connection
                if (!java.nio.file.Files.exists(socketPath)) {
                    if (!retryOnFailure || attempts >= maxRetries - 1) {
                        isRunning.set(false)
                        throw java.net.SocketException("Socket file does not exist: $socketPath")
                    }
                    // Wait before retry
                    Thread.sleep(100L * (attempts + 1))
                    attempts++
                    continue
                }

                socketComm = SocketFactory.createClient(socketPath)
                socketComm.start()

                // Connection successful
                break
            } catch (e: Exception) {
                lastException = e
                if (!retryOnFailure || attempts >= maxRetries - 1) {
                    isRunning.set(false)
                    throw e
                }
                // Wait before retry with exponential backoff
                Thread.sleep(100L * (attempts + 1))
                attempts++
            }
        }

        if (!::socketComm.isInitialized || !socketComm.isRunning) {
            isRunning.set(false)
            throw lastException ?: IllegalStateException("Failed to connect after $maxRetries attempts")
        }

        // Start listening for close requests
        listenerThread = Thread {
            listenForCloseRequest()
        }.apply {
            name = "$streamName-client-listener"
            isDaemon = true
            start()
        }
    }

    /**
     * Send an event message to the server.
     */
    fun sendEvent(eventType: Keyword, eventData: Map<Any, Any>) {
        val message = buildMap {
            put(IpcKeys.MSG_TYPE, IpcKeys.EVENT)
            put(IpcKeys.TYPE, eventType)
            put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
            put(IpcKeys.STREAM_TYPE, IpcKeys.streamType(streamName))
            putAll(eventData)
        }
        sendMessage(message)
    }

    /**
     * Send a command message to the server.
     */
    fun sendCommand(action: Keyword, commandData: Map<Any, Any>) {
        val message = buildMap {
            put(IpcKeys.MSG_TYPE, IpcKeys.COMMAND)
            put(IpcKeys.ACTION, action)
            put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
            put(IpcKeys.STREAM_TYPE, IpcKeys.streamType(streamName))
            putAll(commandData)
        }
        sendMessage(message)
    }

    /**
     * Send a log message to the server.
     */
    fun sendLog(level: Keyword, message: String, data: Map<Any, Any>? = null) {
        val logMessage = buildMap<Any, Any> {
            put(IpcKeys.MSG_TYPE, IpcKeys.LOG)
            put(IpcKeys.LEVEL, level)
            put(IpcKeys.MESSAGE, message)
            put(IpcKeys.PROCESS, "$streamName-stream")
            put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
            if (data != null) {
                put(IpcKeys.DATA, data)
            }
        }
        sendMessage(logMessage)
    }

    /**
     * Send a metric message to the server.
     */
    fun sendMetric(metricName: String, value: Any, tags: Map<String, String>? = null) {
        val message = buildMap<Any, Any> {
            put(IpcKeys.MSG_TYPE, IpcKeys.METRIC)
            put(IpcKeys.keyword("name"), metricName)
            put(IpcKeys.keyword("value"), value)
            put(IpcKeys.PROCESS, "$streamName-stream")
            put(IpcKeys.TIMESTAMP, System.currentTimeMillis())
            if (tags != null) {
                put(IpcKeys.keyword("tags"), tags)
            }
        }
        sendMessage(message)
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
        val message = MessageBuilders.windowEvent(
            IpcKeys.keyword(action),
            width, height, x, y, deltaX, deltaY
        )
        sendMessage(message)
    }

    /**
     * Send a connection event.
     */
    fun sendConnectionEvent(action: Keyword, details: Map<Any, Any>? = null) {
        val message = MessageBuilders.connectionEvent(action, details)
        sendMessage(message)
    }

    /**
     * Send a gesture event.
     */
    fun sendGestureEvent(
        gestureType: Keyword,
        x: Int,
        y: Int,
        frameTimestamp: Long,
        ndcX: Double? = null,
        ndcY: Double? = null,
        deltaX: Int? = null,
        deltaY: Int? = null,
        scrollAmount: Int? = null
    ) {
        val message = MessageBuilders.gestureEvent(
            gestureType,
            IpcKeys.streamType(streamName),
            x, y, frameTimestamp,
            ndcX, ndcY,
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
                    deltaX = event.deltaX, deltaY = event.deltaY)
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
     * Send a gesture event with NDC coordinates using GestureEvent object.
     */
    fun sendGestureEventWithNDC(event: potatoclient.kotlin.gestures.GestureEvent, ndcX: Double, ndcY: Double) {
        when (event) {
            is potatoclient.kotlin.gestures.GestureEvent.Tap -> 
                sendGestureEvent(IpcKeys.TAP, event.x, event.y, event.frameTimestamp, ndcX, ndcY)
            is potatoclient.kotlin.gestures.GestureEvent.DoubleTap -> 
                sendGestureEvent(IpcKeys.DOUBLE_TAP, event.x, event.y, event.frameTimestamp, ndcX, ndcY)
            is potatoclient.kotlin.gestures.GestureEvent.PanStart -> 
                sendGestureEvent(IpcKeys.PAN_START, event.x, event.y, event.frameTimestamp, ndcX, ndcY)
            is potatoclient.kotlin.gestures.GestureEvent.PanMove -> 
                sendGestureEvent(IpcKeys.PAN_MOVE, event.x, event.y, event.frameTimestamp, 
                    ndcX, ndcY, event.deltaX, event.deltaY)
            is potatoclient.kotlin.gestures.GestureEvent.PanStop -> 
                sendGestureEvent(IpcKeys.PAN_STOP, event.x, event.y, event.frameTimestamp, ndcX, ndcY)
            is potatoclient.kotlin.gestures.GestureEvent.WheelUp -> 
                sendGestureEvent(IpcKeys.WHEEL_UP, event.x, event.y, event.frameTimestamp,
                    ndcX, ndcY, scrollAmount = event.scrollAmount)
            is potatoclient.kotlin.gestures.GestureEvent.WheelDown -> 
                sendGestureEvent(IpcKeys.WHEEL_DOWN, event.x, event.y, event.frameTimestamp,
                    ndcX, ndcY, scrollAmount = event.scrollAmount)
        }
    }

    /**
     * Send a message directly without coroutine overhead (for critical paths).
     */
    fun sendMessageDirect(message: Map<Any, Any>) {
        if (!isRunning.get()) {
            throw IllegalStateException("Client not connected")
        }

        val baos = ByteArrayOutputStream()
        val writer = createWriter(baos)
        writer.write(message)

        val messageBytes = baos.toByteArray()
        socketComm.send(messageBytes)
    }

    /**
     * Set callback for close request from server.
     */
    fun setOnCloseRequest(callback: () -> Unit) {
        onCloseRequest = callback
    }
    
    /**
     * Register a handler for incoming messages.
     */
    fun onMessage(handler: (Map<*, *>) -> Unit) {
        messageHandlers.add(handler)
    }

    /**
     * Check if a close request was received.
     */
    fun hasCloseRequest(): Boolean = closeRequestReceived.get()

    /**
     * Shutdown the IPC client.
     * Alias for disconnect() to match IpcManager API.
     */
    fun shutdown() {
        disconnect()
    }
    
    /**
     * Disconnect from the server and clean up resources.
     */
    fun disconnect() {
        if (!isRunning.getAndSet(false)) {
            return
        }

        // Stop listener thread
        listenerThread?.interrupt()

        // Stop socket
        if (::socketComm.isInitialized) {
            socketComm.stop()
        }
    }

    /**
     * Check if the client is connected and running.
     */
    fun isConnected(): Boolean = isRunning.get() && ::socketComm.isInitialized && socketComm.isRunning

    private fun sendMessage(message: Map<Any, Any>) {
        if (!isRunning.get()) {
            throw IllegalStateException("Client not connected")
        }

        try {
            val baos = ByteArrayOutputStream()
            val writer = createWriter(baos)
            writer.write(message)

            val messageBytes = baos.toByteArray()
            socketComm.send(messageBytes)
        } catch (e: Exception) {
            System.err.println("[$streamName-client] Failed to send message: ${e.message}")
            throw e
        }
    }

    private fun listenForCloseRequest() {
        while (isRunning.get() && !Thread.currentThread().isInterrupted) {
            try {
                val messageBytes = socketComm.receive()
                if (messageBytes != null) {
                    val bais = ByteArrayInputStream(messageBytes)
                    val reader = createReader(bais)
                    val message = reader.read<Any>()

                    if (message is Map<*, *>) {
                        // Dispatch to all registered handlers
                        messageHandlers.forEach { handler ->
                            try {
                                handler(message)
                            } catch (e: Exception) {
                                System.err.println("[$streamName-client] Handler error: ${e.message}")
                            }
                        }
                        
                        // Check for close request
                        val msgType = message[IpcKeys.MSG_TYPE]
                        val action = message[IpcKeys.ACTION]

                        if (msgType == IpcKeys.COMMAND && action == IpcKeys.CLOSE_REQUEST) {
                            closeRequestReceived.set(true)
                            onCloseRequest?.invoke()
                            break
                        }
                    }
                }
            } catch (_: InterruptedException) {
                // Normal shutdown
                Thread.currentThread().interrupt()
                break
            } catch (e: Exception) {
                if (isRunning.get()) {
                    System.err.println("[$streamName-client] Error reading message: ${e.message}")
                }
                break
            }
        }
    }

    private fun createWriter(out: ByteArrayOutputStream): Writer<Any> =
        TransitFactory.writer(TransitFactory.Format.MSGPACK, out)

    private fun createReader(input: ByteArrayInputStream): Reader =
        TransitFactory.reader(
            TransitFactory.Format.MSGPACK,
            input,
            TransitFactory.readHandlerMap(readHandlers)
        )

    companion object {
        @Volatile
        private var instance: IpcClient? = null
        
        /**
         * Generate a socket path with server PID and stream name.
         */
        @JvmStatic
        fun generateSocketPath(serverPid: Long, streamName: String): Path {
            val socketName = "ipc-$serverPid-$streamName"
            return SocketFactory.generateSocketPath(socketName)
        }

        /**
         * Get the singleton instance for the video stream process.
         * Uses parent process PID to connect to the main process.
         */
        @JvmStatic
        fun getInstance(streamName: String): IpcClient {
            return instance ?: synchronized(this) {
                instance ?: run {
                    // Get parent process PID (the main process that launched this subprocess)
                    val parentPid = getParentProcessPid()
                    create(parentPid, streamName).also {
                        instance = it
                    }
                }
            }
        }
        
        /**
         * Create and connect a client.
         * @param retryOnFailure If true, retries connection if initial attempt fails
         * @param maxRetries Maximum number of connection attempts
         */
        @JvmStatic
        fun create(
            serverPid: Long,
            streamName: String,
            retryOnFailure: Boolean = true,
            maxRetries: Int = 5
        ): IpcClient {
            val socketPath = generateSocketPath(serverPid, streamName)
            val client = IpcClient(socketPath, streamName)
            client.connect(retryOnFailure, maxRetries)
            return client
        }
        
        /**
         * Get the parent process PID.
         * This is used by subprocesses to find the main process socket.
         */
        @JvmStatic
        private fun getParentProcessPid(): Long {
            return try {
                // Try to get parent PID from environment variable first
                System.getenv("POTATOCLIENT_PARENT_PID")?.toLongOrNull()
                    ?: ProcessHandle.current().parent().orElse(null)?.pid()
                    ?: throw IllegalStateException("Cannot determine parent process PID")
            } catch (e: Exception) {
                throw IllegalStateException("Failed to get parent process PID", e)
            }
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
