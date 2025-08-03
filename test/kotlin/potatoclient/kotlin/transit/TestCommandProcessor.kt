package potatoclient.kotlin.transit

import build.buf.protovalidate.Validator
import build.buf.protovalidate.ValidationResult
import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import kotlinx.coroutines.runBlocking
import potatoclient.kotlin.transit.generated.GeneratedCommandHandlers
import java.io.PrintStream

/**
 * Test command processor that returns JSON representation of protobuf
 * instead of sending to WebSocket. Used for roundtrip testing.
 */
class TestCommandProcessor {
    
    private val jsonPrinter = JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames()
        .sortingMapKeys() // For consistent output
    
    /**
     * Process a command and return JSON representation
     * Now expects commands in nested format: {:cv {:start-track-ndc {:x 0.5 :y 0.5}}}
     */
    fun processCommand(commandMap: Map<*, *>): String {
        return try {
            val proto = GeneratedCommandHandlers.buildCommand(commandMap)
            
            if (proto != null) {
                // Validate with buf.validate
                val validator = Validator.newBuilder().build()
                val validationResult = validator.validate(proto)
                
                if (!validationResult.isSuccess) {
                    val violations = validationResult.violations.violationsList
                        .joinToString(", ") { "${it.fieldPath}: ${it.message}" }
                    return """{"success": false, "error": "Validation failed: $violations"}"""
                }
                
                // Test binary roundtrip
                val binary = proto.toByteArray()
                val deserialized = cmd.JonSharedCmd.Root.parseFrom(binary)
                
                // Check equality
                val protoEquals = proto == deserialized
                
                // Convert to JSON with sorted keys
                val json = jsonPrinter.print(proto)
                
                // Wrap in success envelope with additional info
                """{"success": true, "proto": $json, "binary-size": ${binary.size}, "proto-equals": $protoEquals}"""
            } else {
                """{"success": false, "error": "Failed to build command from map"}"""
            }
        } catch (e: Exception) {
            """{"success": false, "error": "${e.message?.replace("\"", "\\\"")}"}"""
        }
    }
    
    /**
     * Process with buf.validate validation
     */
    fun processCommandWithValidation(commandMap: Map<*, *>): String {
        return try {
            val proto = GeneratedCommandHandlers.buildCommand(commandMap)
            
            if (proto != null) {
                // Validate using buf.validate
                try {
                    val validator = build.buf.protovalidate.Validator.newBuilder().build()
                    val validationResult = validator.validate(proto)
                    
                    if (!validationResult.isSuccess) {
                        val violations = validationResult.violations.violations.map { violation ->
                            mapOf(
                                "field" to violation.fieldPath,
                                "message" to violation.message,
                                "constraint" to violation.constraintId
                            )
                        }
                        return """{"success": false, "validation_errors": ${toJson(violations)}}"""
                    }
                    
                    val json = jsonPrinter.print(proto)
                    """{"success": true, "proto": $json, "validated": true}"""
                } catch (e: Exception) {
                    // If validation fails, still return the proto but note validation couldn't run
                    val json = jsonPrinter.print(proto)
                    """{"success": true, "proto": $json, "validated": false, "validation_error": "${e.message}"}"""
                }
            } else {
                """{"success": false, "error": "Failed to build command from map"}"""
            }
        } catch (e: Exception) {
            """{"success": false, "error": "${e.message}"}"""
        }
    }
    
    /**
     * Get detailed field mapping for a command
     * Now works with nested command structure
     */
    fun getFieldMapping(commandMap: Map<*, *>): String {
        val mapping = mutableMapOf<String, Any>()
        
        // Process nested command structure
        fun processMap(map: Map<*, *>, prefix: String = "") {
            map.forEach { (key, value) ->
                val transitKey = when (key) {
                    is com.cognitect.transit.Keyword -> key.toString()
                    else -> key.toString()
                }
                
                val fullKey = if (prefix.isEmpty()) transitKey else "$prefix.$transitKey"
                
                when (value) {
                    is Map<*, *> -> processMap(value, fullKey)
                    else -> {
                        mapping[fullKey] = mapOf(
                            "transit_value" to value,
                            "value_type" to value?.javaClass?.simpleName
                        )
                    }
                }
            }
        }
        
        processMap(commandMap)
        
        return toJson(mapOf("field_mapping" to mapping))
    }
    
    private fun toJson(obj: Any): String {
        // Simple JSON serialization for our needs
        return when (obj) {
            is Map<*, *> -> {
                val entries = obj.entries.joinToString(",") { (k, v) ->
                    "\"$k\": ${toJson(v)}"
                }
                "{$entries}"
            }
            is String -> "\"$obj\""
            is Number -> obj.toString()
            is Boolean -> obj.toString()
            else -> "\"${obj.toString()}\""
        }
    }
}

/**
 * Standalone test processor that reads Transit from stdin and writes JSON to stdout
 */
fun main(args: Array<String>) {
    val processor = TestCommandProcessor()
    val transitComm = TransitCommunicator(System.`in`, System.out)
    
    try {
        runBlocking {
            while (true) {
                val msg = transitComm.readMessage() ?: break
                
                val msgType = msg[TransitKeys.MSG_TYPE] as? String
                if (msgType == "command") {
                    val payload = msg[TransitKeys.PAYLOAD] as? Map<*, *>
                    
                    // New format: payload is the command map directly
                    // e.g., {:cv {:start-track-ndc {:x 0.5 :y 0.5}}}
                    if (payload != null) {
                        val jsonResult = if (args.contains("--validate")) {
                            processor.processCommandWithValidation(payload)
                        } else {
                            processor.processCommand(payload)
                        }
                        
                        // Send back as Transit message
                        val response = mapOf(
                            TransitKeys.MSG_TYPE to "response",
                            TransitKeys.PAYLOAD to mapOf(
                                "json" to jsonResult,
                                "command" to payload
                            )
                        )
                        
                        transitComm.sendMessage(response)
                    } else {
                        val response = mapOf(
                            TransitKeys.MSG_TYPE to "error",
                            TransitKeys.PAYLOAD to mapOf(
                                "error" to "No payload found in command message"
                            )
                        )
                        transitComm.sendMessage(response)
                    }
                } else if (msgType == "control" && 
                          (msg[TransitKeys.PAYLOAD] as? Map<*, *>)?.get("action") == "shutdown") {
                    break
                }
            }
        }
    } catch (e: Exception) {
        System.err.println("Error in test processor: ${e.message}")
        e.printStackTrace()
    }
}