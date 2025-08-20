package potatoclient.kotlin.ipc

import com.cognitect.transit.Keyword
import com.cognitect.transit.Reader
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Writer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import potatoclient.java.ipc.SocketFactory
import potatoclient.java.ipc.UnixSocketCommunicator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.UUID
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
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val closeRequestReceived = AtomicBoolean(false)
    
    // Callback for close request
    private var onCloseRequest: (() -> Unit)? = null
    
    // Transit handlers for custom types
    private val readHandlers = mapOf(
        "kw" to com.cognitect.transit.ReadHandler<Any, Any> { rep ->
            TransitFactory.keyword(rep.toString())
        }
    )
    
    /**
     * Connect to the server and start listening for close requests.
     */
    suspend fun connect() {
        if (isRunning.getAndSet(true)) {
            throw IllegalStateException("Client already connected")
        }
        
        withContext(Dispatchers.IO) {
            socketComm = SocketFactory.createClient(socketPath)
            socketComm.start()
        }
        
        // Start listening for close requests
        scope.launch {
            listenForCloseRequest()
        }
    }
    
    /**
     * Send an event message to the server.
     */
    suspend fun sendEvent(eventType: Keyword, eventData: Map<Any, Any>) {
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
    suspend fun sendCommand(action: Keyword, commandData: Map<Any, Any>) {
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
    suspend fun sendLog(level: Keyword, message: String, data: Map<Any, Any>? = null) {
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
    suspend fun sendMetric(metricName: String, value: Any, tags: Map<String, String>? = null) {
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
     * Check if a close request was received.
     */
    fun hasCloseRequest(): Boolean = closeRequestReceived.get()
    
    /**
     * Disconnect from the server and clean up resources.
     */
    fun disconnect() {
        isRunning.set(false)
        scope.cancel()
        
        if (::socketComm.isInitialized) {
            socketComm.stop()
        }
    }
    
    /**
     * Check if the client is connected and running.
     */
    fun isConnected(): Boolean = isRunning.get() && ::socketComm.isInitialized && socketComm.isRunning()
    
    private suspend fun sendMessage(message: Map<Any, Any>) {
        if (!isRunning.get()) {
            throw IllegalStateException("Client not connected")
        }
        
        withContext(Dispatchers.IO) {
            val baos = ByteArrayOutputStream()
            val writer = createWriter(baos)
            writer.write(message)
            
            val messageBytes = baos.toByteArray()
            socketComm.send(messageBytes)
        }
    }
    
    private suspend fun listenForCloseRequest() {
        withContext(Dispatchers.IO) {
            while (isRunning.get() && isActive) {
                try {
                    val messageBytes = socketComm.receive()
                    if (messageBytes != null) {
                        val bais = ByteArrayInputStream(messageBytes)
                        val reader = createReader(bais)
                        val message = reader.read<Any>()
                        
                        if (message is Map<*, *>) {
                            val msgType = message[IpcKeys.MSG_TYPE]
                            val action = message[IpcKeys.ACTION]
                            
                            if (msgType == IpcKeys.COMMAND && action == IpcKeys.CLOSE_REQUEST) {
                                closeRequestReceived.set(true)
                                onCloseRequest?.invoke()
                                break
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    // Normal shutdown
                    break
                } catch (e: Exception) {
                    if (isRunning.get()) {
                        System.err.println("[$streamName] Error reading message: ${e.message}")
                    }
                    break
                }
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
        /**
         * Generate a socket path with server PID and stream name.
         */
        @JvmStatic
        fun generateSocketPath(serverPid: Long, streamName: String): Path {
            val socketName = "ipc-$serverPid-$streamName"
            return SocketFactory.generateSocketPath(socketName)
        }
        
        /**
         * Create and connect a client.
         */
        @JvmStatic
        suspend fun create(serverPid: Long, streamName: String): IpcClient {
            val socketPath = generateSocketPath(serverPid, streamName)
            val client = IpcClient(socketPath, streamName)
            client.connect()
            return client
        }
    }
}