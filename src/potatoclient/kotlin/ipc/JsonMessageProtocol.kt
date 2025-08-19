package potatoclient.kotlin.ipc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * JSON-based message protocol for IPC communication between Kotlin and Clojure.
 * Uses length-prefixed JSON messages for framing.
 */
class JsonMessageProtocol {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    
    /**
     * Write a message to the output stream as length-prefixed JSON
     */
    fun writeMessage(output: OutputStream, message: Map<String, Any?>) {
        val json = objectMapper.writeValueAsBytes(message)
        val lengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN)
        lengthBuffer.putInt(json.size)
        
        output.write(lengthBuffer.array())
        output.write(json)
        output.flush()
    }
    
    /**
     * Read a length-prefixed JSON message from the input stream
     */
    fun readMessage(input: InputStream): Map<String, Any?>? {
        // Read 4-byte length header
        val lengthBytes = ByteArray(4)
        val bytesRead = input.read(lengthBytes)
        if (bytesRead != 4) return null
        
        val length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).int
        if (length <= 0) return null
        
        // Read JSON payload
        val jsonBytes = ByteArray(length)
        var totalRead = 0
        while (totalRead < length) {
            val read = input.read(jsonBytes, totalRead, length - totalRead)
            if (read == -1) return null
            totalRead += read
        }
        
        return objectMapper.readValue<Map<String, Any?>>(jsonBytes)
    }
    
    /**
     * Convert any object to a Map for JSON serialization
     */
    fun toMap(obj: Any): Map<String, Any?> {
        return objectMapper.convertValue(obj, Map::class.java) as Map<String, Any?>
    }
}