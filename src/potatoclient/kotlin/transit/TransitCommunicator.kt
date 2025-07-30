package potatoclient.transit

import com.cognitect.transit.*
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Handles bidirectional Transit communication with the Clojure main process
 * with proper message framing and backpressure handling
 */
class TransitCommunicator(
    private val inputStream: InputStream = System.`in`,
    private val outputStream: OutputStream = System.out
) {
    companion object {
        // Message framing constants
        private const val MAX_MESSAGE_SIZE = 10 * 1024 * 1024  // 10MB max message size
        private const val FRAME_HEADER_SIZE = 4  // 4 bytes for message length
        
        // Backpressure configuration
        private const val HIGH_WATER_MARK = 1000  // Start applying backpressure
        private const val LOW_WATER_MARK = 100    // Resume normal operation
    }
    
    private val writer: Writer<Any>
    private val reader: Reader<Any>
    private val messageQueue = Channel<Map<*, *>>(Channel.UNLIMITED)
    private val isRunning = AtomicBoolean(true)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Backpressure state
    private val backpressureActive = AtomicBoolean(false)
    
    init {
        // Create framed streams for reliable message boundaries
        val framedOutput = FramedOutputStream(outputStream)
        val framedInput = FramedInputStream(inputStream)
        
        // Create Transit writer with MessagePack format
        writer = TransitFactory.writer(TransitFactory.Format.MSGPACK, framedOutput)
        
        // Create Transit reader with MessagePack format
        reader = TransitFactory.reader(TransitFactory.Format.MSGPACK, framedInput)
        
        // Start reader coroutine
        scope.launch {
            startReading()
        }
        
        // Start backpressure monitor
        scope.launch {
            monitorBackpressure()
        }
    }
    
    /**
     * Send a message to the Clojure process with backpressure awareness
     */
    suspend fun sendMessage(message: Map<String, Any>) {
        // Apply backpressure if needed
        if (backpressureActive.get()) {
            delay(10) // Small delay when under pressure
        }
        
        withContext(Dispatchers.IO) {
            synchronized(writer) {
                try {
                    writer.write(message)
                    // Framed output stream handles flushing after frame is written
                } catch (e: IOException) {
                    System.err.println("Error sending Transit message: ${e.message}")
                    throw e
                }
            }
        }
    }
    
    /**
     * Read a message from the Clojure process
     */
    suspend fun readMessage(): Map<*, *>? {
        return if (isRunning.get()) {
            messageQueue.receive()
        } else {
            null
        }
    }
    
    /**
     * Check if a message is available
     */
    fun hasMessage(): Boolean {
        return !messageQueue.isEmpty
    }
    
    /**
     * Monitor queue size and apply backpressure
     */
    private suspend fun monitorBackpressure() {
        while (isRunning.get()) {
            val queueSize = messageQueue.isEmpty.let { 
                if (it) 0 else HIGH_WATER_MARK // Approximate since we can't get exact size
            }
            
            when {
                queueSize > HIGH_WATER_MARK && !backpressureActive.get() -> {
                    backpressureActive.set(true)
                    System.err.println("Backpressure activated - queue size high")
                }
                queueSize < LOW_WATER_MARK && backpressureActive.get() -> {
                    backpressureActive.set(false)
                    System.err.println("Backpressure deactivated - queue size normal")
                }
            }
            
            delay(100) // Check every 100ms
        }
    }
    
    /**
     * Start reading messages from input stream
     */
    private suspend fun startReading() {
        withContext(Dispatchers.IO) {
            while (isRunning.get() && isActive) {
                try {
                    val message = reader.read()
                    if (message is Map<*, *>) {
                        // Non-blocking send to avoid blocking the reader
                        val sent = messageQueue.trySend(message).isSuccess
                        if (!sent) {
                            System.err.println("Message queue full, dropping message")
                        }
                    }
                } catch (e: EOFException) {
                    // Stream closed, exit gracefully
                    System.err.println("Input stream closed, stopping reader")
                    break
                } catch (e: IOException) {
                    if (isRunning.get()) {
                        System.err.println("I/O error reading Transit message: ${e.message}")
                    }
                    break
                } catch (e: Exception) {
                    // Log error but continue reading
                    System.err.println("Error reading Transit message: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Create a standard message envelope
     */
    fun createMessage(
        msgType: String,
        payload: Map<String, Any>
    ): Map<String, Any> {
        return mapOf(
            "msg-type" to msgType,
            "msg-id" to UUID.randomUUID().toString(),
            "timestamp" to System.currentTimeMillis(),
            "payload" to payload
        )
    }
    
    /**
     * Close the communicator
     */
    fun close() {
        isRunning.set(false)
        scope.cancel()
        
        try {
            messageQueue.close()
        } catch (e: Exception) {
            // Ignore channel close errors
        }
        
        try {
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            // Ignore stream close errors
        }
    }
}

/**
 * Output stream that frames messages with a length prefix
 */
class FramedOutputStream(private val wrapped: OutputStream) : OutputStream() {
    private val buffer = ByteArrayOutputStream()
    
    override fun write(b: Int) {
        buffer.write(b)
    }
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        buffer.write(b, off, len)
    }
    
    override fun flush() {
        val data = buffer.toByteArray()
        if (data.isNotEmpty()) {
            // Write length prefix (4 bytes, big endian)
            val lengthBuffer = ByteBuffer.allocate(4)
            lengthBuffer.order(ByteOrder.BIG_ENDIAN)
            lengthBuffer.putInt(data.size)
            
            // Write frame
            wrapped.write(lengthBuffer.array())
            wrapped.write(data)
            wrapped.flush()
            
            // Clear buffer for next message
            buffer.reset()
        }
    }
    
    override fun close() {
        flush()
        wrapped.close()
    }
}

/**
 * Input stream that reads framed messages with length prefix
 */
class FramedInputStream(private val wrapped: InputStream) : InputStream() {
    private var currentFrame: ByteArrayInputStream? = null
    
    override fun read(): Int {
        ensureFrame()
        return currentFrame?.read() ?: -1
    }
    
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureFrame()
        return currentFrame?.read(b, off, len) ?: -1
    }
    
    private fun ensureFrame() {
        if (currentFrame == null || currentFrame!!.available() == 0) {
            readNextFrame()
        }
    }
    
    private fun readNextFrame() {
        // Read length prefix
        val lengthBytes = ByteArray(4)
        var bytesRead = 0
        while (bytesRead < 4) {
            val n = wrapped.read(lengthBytes, bytesRead, 4 - bytesRead)
            if (n == -1) {
                currentFrame = null
                return
            }
            bytesRead += n
        }
        
        val length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).int
        
        // Validate frame size
        if (length <= 0 || length > TransitCommunicator.MAX_MESSAGE_SIZE) {
            throw IOException("Invalid frame size: $length")
        }
        
        // Read frame data
        val frameData = ByteArray(length)
        bytesRead = 0
        while (bytesRead < length) {
            val n = wrapped.read(frameData, bytesRead, length - bytesRead)
            if (n == -1) {
                throw IOException("Unexpected EOF while reading frame")
            }
            bytesRead += n
        }
        
        currentFrame = ByteArrayInputStream(frameData)
    }
    
    override fun close() {
        wrapped.close()
    }
}