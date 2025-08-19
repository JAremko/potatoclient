package potatoclient.kotlin.ipc

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles JSON-based IPC communication with Clojure process
 */
class JsonCommunicator(
    private val inputStream: InputStream,
    private val outputStream: OutputStream
) {
    private val protocol = JsonMessageProtocol()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isRunning = AtomicBoolean(true)
    private val incomingMessages = Channel<Map<String, Any?>>(Channel.BUFFERED)
    
    /**
     * Start the communicator - begins reading messages
     */
    fun start() {
        scope.launch {
            while (isRunning.get()) {
                try {
                    val message = protocol.readMessage(inputStream)
                    if (message != null) {
                        incomingMessages.send(message)
                    }
                } catch (e: Exception) {
                    if (isRunning.get()) {
                        System.err.println("Error reading message: ${e.message}")
                    }
                    break
                }
            }
        }
    }
    
    /**
     * Send a message to the Clojure process
     */
    suspend fun sendMessage(message: Map<String, Any?>) {
        withContext(Dispatchers.IO) {
            try {
                protocol.writeMessage(outputStream, message)
            } catch (e: Exception) {
                System.err.println("Error sending message: ${e.message}")
            }
        }
    }
    
    /**
     * Send a message synchronously (blocking)
     */
    fun sendMessageBlocking(message: Map<String, Any?>) {
        runBlocking {
            sendMessage(message)
        }
    }
    
    /**
     * Get channel for receiving messages
     */
    fun getIncomingMessages(): ReceiveChannel<Map<String, Any?>> = incomingMessages
    
    /**
     * Stop the communicator
     */
    fun stop() {
        isRunning.set(false)
        scope.cancel()
        incomingMessages.close()
    }
    
    /**
     * Helper function to create a timestamped message
     */
    fun createMessage(msgType: String, vararg pairs: Pair<String, Any?>): Map<String, Any?> {
        val message = mutableMapOf<String, Any?>(
            MessageTypes.MSG_TYPE to msgType,
            MessageTypes.TIMESTAMP to System.currentTimeMillis()
        )
        pairs.forEach { (key, value) ->
            message[key] = value
        }
        return message
    }
}