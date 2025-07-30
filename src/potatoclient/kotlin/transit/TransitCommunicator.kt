package potatoclient.transit

import com.cognitect.transit.*
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * Handles bidirectional Transit communication with the Clojure main process
 */
class TransitCommunicator(
    private val inputStream: InputStream = System.`in`,
    private val outputStream: OutputStream = System.out
) {
    private val writer: Writer<Any>
    private val reader: Reader<Any>
    private val messageQueue = Channel<Map<*, *>>(Channel.UNLIMITED)
    
    init {
        // Create Transit writer with MessagePack format
        writer = TransitFactory.writer(TransitFactory.Format.MSGPACK, outputStream)
        
        // Create Transit reader with MessagePack format
        reader = TransitFactory.reader(TransitFactory.Format.MSGPACK, inputStream)
        
        // Start reader coroutine
        GlobalScope.launch {
            startReading()
        }
    }
    
    /**
     * Send a message to the Clojure process
     */
    suspend fun sendMessage(message: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            synchronized(writer) {
                writer.write(message)
                outputStream.flush()
            }
        }
    }
    
    /**
     * Read a message from the Clojure process
     */
    suspend fun readMessage(): Map<*, *>? {
        return messageQueue.receive()
    }
    
    /**
     * Check if a message is available
     */
    fun hasMessage(): Boolean {
        return !messageQueue.isEmpty
    }
    
    /**
     * Start reading messages from input stream
     */
    private suspend fun startReading() {
        withContext(Dispatchers.IO) {
            while (isActive) {
                try {
                    val message = reader.read()
                    if (message is Map<*, *>) {
                        messageQueue.send(message)
                    }
                } catch (e: EOFException) {
                    // Stream closed, exit gracefully
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
        try {
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            // Ignore close errors
        }
    }
}