package potatoclient.kotlin.transit

import build.buf.protovalidate.Validator
import build.buf.protovalidate.ValidationResult
import com.cognitect.transit.TransitFactory
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import kotlinx.coroutines.runBlocking
import java.io.PrintStream

/**
 * Test command processor that returns JSON representation of protobuf
 * instead of sending to WebSocket. Used for roundtrip testing.
 */
class TestCommandProcessor {
    
    private val commandBuilder = ProtobufCommandBuilder.getInstance()
    private val jsonPrinter = JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames()
        .sortingMapKeys() // For consistent output
    
    /**
     * Process a command and return JSON representation
     */
    fun processCommand(action: String, params: Map<*, *>?): String {
        return try {
            val result = commandBuilder.buildCommand(action, params)
            
            when {
                result.isSuccess -> {
                    val proto = result.getOrThrow()
                    // Convert to JSON with sorted keys
                    val json = jsonPrinter.print(proto)
                    // Wrap in success envelope
                    """{"success": true, "proto": $json}"""
                }
                else -> {
                    val error = result.exceptionOrNull()
                    """{"success": false, "error": "${error?.message?.replace("\"", "\\\"")}"}"""
                }
            }
        } catch (e: Exception) {
            """{"success": false, "error": "${e.message?.replace("\"", "\\\"")}"}"""
        }
    }
    
    /**
     * Process with buf.validate validation
     */
    fun processCommandWithValidation(action: String, params: Map<*, *>?): String {
        return try {
            val result = commandBuilder.buildCommand(action, params)
            
            when {
                result.isSuccess -> {
                    val proto = result.getOrThrow()
                    
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
                }
                else -> {
                    val error = result.exceptionOrNull()
                    """{"success": false, "error": "${error?.message}"}"""
                }
            }
        } catch (e: Exception) {
            """{"success": false, "error": "${e.message}"}"""
        }
    }
    
    /**
     * Get detailed field mapping for a command
     */
    fun getFieldMapping(action: String, params: Map<*, *>?): String {
        val mapping = mutableMapOf<String, Any>()
        
        // Track which Transit keys map to which proto fields
        params?.forEach { (key, value) ->
            val transitKey = when (key) {
                is com.cognitect.transit.Keyword -> key.toString()
                else -> key.toString()
            }
            
            // Convert kebab-case to snake_case
            val protoField = transitKey.replace("-", "_")
            
            mapping[transitKey] = mapOf(
                "proto_field" to protoField,
                "transit_value" to value,
                "value_type" to value?.javaClass?.simpleName
            )
        }
        
        return JsonFormat.printer().print(
            com.google.protobuf.util.JsonFormat.parser().parse(
                """{"action": "$action", "field_mapping": ${toJson(mapping)}}""",
                com.google.protobuf.Empty.getDefaultInstance()
            )
        )
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
                    val action = payload?.get(TransitKeys.ACTION) as? String ?: "unknown"
                    val params = payload?.get(TransitKeys.PARAMS) as? Map<*, *>
                    
                    val jsonResult = if (args.contains("--validate")) {
                        processor.processCommandWithValidation(action, params)
                    } else {
                        processor.processCommand(action, params)
                    }
                    
                    // Send back as Transit message
                    val response = mapOf(
                        TransitKeys.MSG_TYPE to "response",
                        TransitKeys.PAYLOAD to mapOf(
                            "json" to jsonResult,
                            "action" to action
                        )
                    )
                    
                    transitComm.sendMessage(response)
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