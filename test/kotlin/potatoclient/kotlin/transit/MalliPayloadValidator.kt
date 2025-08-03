package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory
import com.google.protobuf.util.JsonFormat
import potatoclient.kotlin.transit.generated.GeneratedCommandHandlers
import build.buf.protovalidate.Validator
import java.io.File
import com.google.gson.Gson

/**
 * Validates Malli-generated payloads through the full stack:
 * Transit → Protobuf → Validation → Binary → Protobuf → Transit
 * 
 * Used by malli-kotlin-validation-test.clj
 */
object MalliPayloadValidator {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("""
                {"success": false, "error": "No input file provided"}
            """.trimIndent())
            return
        }
        
        try {
            // Read Transit file
            val transitData = File(args[0]).readBytes()
            val reader = TransitFactory.reader(TransitFactory.Format.MSGPACK, transitData.inputStream())
            @Suppress("UNCHECKED_CAST")
            val commandMap = reader.read() as Map<String, Any>
            
            // Convert to protobuf
            val proto = GeneratedCommandHandlers.buildCommand(commandMap)
                ?: throw Exception("Failed to build command from Transit data")
            
            // Validate with buf.validate
            val validator = Validator.newBuilder().build()
            val validationResult = validator.validate(proto)
            
            if (!validationResult.isSuccess) {
                val violations = validationResult.violations.violationsList
                    .joinToString(", ") { "${it.fieldPath}: ${it.message}" }
                println("""
                    {"success": false, "error": "Validation failed: $violations"}
                """.trimIndent())
                return
            }
            
            // Binary roundtrip
            val binary = proto.toByteArray()
            val originalHash = proto.hashCode()
            
            val deserialized = JonSharedCmd.Root.parseFrom(binary)
            val roundtripHash = deserialized.hashCode()
            
            // Test Java equals
            val areEqual = proto == deserialized
            
            // Extract back to Transit
            val extracted = GeneratedCommandHandlers.extractCommand(deserialized)
            
            // Convert to JSON for output
            val gson = Gson()
            val jsonPrinter = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
            
            println("""
                {
                    "success": true,
                    "original-hash": $originalHash,
                    "roundtrip-hash": $roundtripHash,
                    "equals": $areEqual,
                    "proto": ${jsonPrinter.print(proto)},
                    "extracted": ${gson.toJson(extracted)},
                    "binary-size": ${binary.size}
                }
            """.trimIndent())
            
        } catch (e: Exception) {
            e.printStackTrace(System.err)
            println("""
                {"success": false, "error": "${e.message?.replace("\"", "\\\"")}"}
            """.trimIndent())
        }
    }
}