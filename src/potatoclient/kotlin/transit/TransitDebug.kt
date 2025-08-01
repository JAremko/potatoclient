package potatoclient.kotlin.transit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

/**
 * Debug utilities for Transit messages
 */
object TransitDebug {
    private val objectMapper =
        ObjectMapper().apply {
            enable(SerializationFeature.INDENT_OUTPUT)
        }

    /**
     * Convert Transit data to JSON string for debugging
     */
    fun transitToJson(data: Any): String =
        try {
            // For now, just convert directly to JSON without Transit round-trip
            // This is sufficient for debugging purposes
            objectMapper.writeValueAsString(data)
        } catch (e: Exception) {
            "Error converting to JSON: ${e.message}"
        }

    /**
     * Convert any object directly to JSON (without Transit encoding)
     */
    fun toJson(data: Any): String =
        try {
            objectMapper.writeValueAsString(data)
        } catch (e: Exception) {
            "Error converting to JSON: ${e.message}"
        }

    /**
     * Log an unknown or malformed Transit message
     */
    fun logUnknownMessage(
        context: String,
        message: Any?,
        error: String,
        logger: org.slf4j.Logger,
    ) {
        val messageStr =
            when (message) {
                is Map<*, *> -> {
                    val keys = message.keys.joinToString(", ")
                    "Map with keys: $keys\n${toJson(message)}"
                }
                null -> "null"
                else -> "${message.javaClass.simpleName}: $message"
            }

        logger.warn(
            "Unknown/malformed message in $context: $error\n" +
                "Message type: ${message?.javaClass?.name}\n" +
                "Message: $messageStr",
        )
    }

    /**
     * Validate Transit envelope structure
     */
    fun validateEnvelope(message: Any?): Pair<Boolean, String?> {
        if (message !is Map<*, *>) {
            return false to "Message is not a Map"
        }

        val msgType = message["msg-type"] ?: message["msgType"]
        if (msgType == null) {
            return false to "Missing required msg-type field"
        }

        val msgId = message["msg-id"] ?: message["msgId"]
        if (msgId == null) {
            return false to "Missing required msg-id field"
        }

        val timestamp = message["timestamp"]
        if (timestamp == null) {
            return false to "Missing required timestamp field"
        }

        val payload = message["payload"]
        if (payload == null) {
            return false to "Missing required payload field"
        }

        return true to null
    }

    /**
     * Create a debug message for testing
     */
    fun createDebugMessage(
        originalMsgId: String?,
        error: String,
    ): Map<String, Any> =
        mapOf(
            "msg-type" to "debug",
            "msg-id" to
                java.util.UUID
                    .randomUUID()
                    .toString(),
            "timestamp" to System.currentTimeMillis(),
            "payload" to
                mapOf(
                    "original-msg-id" to (originalMsgId ?: "unknown"),
                    "error" to error,
                ),
        )

    /**
     * Pretty print a Transit message for logging
     */
    fun prettyPrint(message: Any?): String =
        when (message) {
            is Map<*, *> -> {
                buildString {
                    appendLine("Transit Message:")
                    appendLine("  Type: ${message["msg-type"] ?: "unknown"}")
                    appendLine("  ID: ${message["msg-id"] ?: "none"}")
                    appendLine("  Timestamp: ${message["timestamp"] ?: "none"}")
                    appendLine("  Payload: ${toJson(message["payload"] ?: mapOf<String, Any>())}")
                }
            }
            else -> "Not a valid Transit message: $message"
        }
}
