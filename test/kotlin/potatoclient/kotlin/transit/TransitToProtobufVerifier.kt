package potatoclient.kotlin.transit

import com.cognitect.transit.TransitFactory
import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream

/**
 * Verifies that Transit messages are correctly converted to Protobuf
 * by comparing field-by-field
 */
class TransitToProtobufVerifier {
    
    private lateinit var commandBuilder: ProtobufCommandBuilder
    private val jsonPrinter = JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames()
    
    @BeforeEach
    fun setUp() {
        commandBuilder = ProtobufCommandBuilder.getInstance()
    }
    
    /**
     * Helper to create Transit maps with keyword keys
     */
    private fun transitMap(vararg pairs: Pair<String, Any>): Map<*, *> {
        return pairs.associate { (k, v) -> 
            TransitFactory.keyword(k) to v
        }
    }
    
    /**
     * Verify protobuf message contains expected fields
     */
    private fun verifyProtobufFields(
        message: Message,
        expectedFields: Map<String, Any>
    ) {
        val descriptor = message.descriptorForType
        
        expectedFields.forEach { (fieldName, expectedValue) ->
            val field = descriptor.findFieldByName(fieldName.replace("-", "_"))
            assertNotNull(field, "Field $fieldName not found in protobuf")
            
            val actualValue = message.getField(field)
            
            when (expectedValue) {
                is Number -> {
                    when (actualValue) {
                        is Float -> assertEquals(expectedValue.toFloat(), actualValue, 0.001f)
                        is Double -> assertEquals(expectedValue.toDouble(), actualValue, 0.001)
                        is Int -> assertEquals(expectedValue.toInt(), actualValue)
                        is Long -> assertEquals(expectedValue.toLong(), actualValue)
                        else -> fail("Unexpected number type: ${actualValue::class}")
                    }
                }
                is Boolean -> assertEquals(expectedValue, actualValue)
                is String -> {
                    // Handle enum conversion
                    if (field.type == Descriptors.FieldDescriptor.Type.ENUM) {
                        val enumValue = actualValue as Descriptors.EnumValueDescriptor
                        assertEquals(expectedValue.uppercase(), enumValue.name)
                    } else {
                        assertEquals(expectedValue, actualValue)
                    }
                }
                else -> assertEquals(expectedValue, actualValue)
            }
        }
    }
    
    /**
     * Test helper that builds command and verifies protobuf
     */
    private fun testCommandConversion(
        action: String,
        transitParams: Map<*, *>?,
        verifyRoot: (Message) -> Unit
    ) {
        // Build protobuf
        val result = commandBuilder.buildCommand(action, transitParams)
        assertTrue(result.isSuccess, "Failed to build command: ${result.exceptionOrNull()}")
        
        val protoBytes = result.getOrThrow().toByteArray()
        
        // Parse based on action to get correct message type
        val rootMessage = when {
            action.startsWith("rotaryplatform-") -> 
                cmd.JonSharedCmdRotaryPlatform.Root.parseFrom(protoBytes)
            action.startsWith("heatcamera-") -> 
                cmd.JonSharedCmdHeatCamera.Root.parseFrom(protoBytes)
            action.startsWith("daycamera-") -> 
                cmd.JonSharedCmdDayCamera.Root.parseFrom(protoBytes)
            action.startsWith("cv-") -> 
                cmd.JonSharedCmdCv.Root.parseFrom(protoBytes)
            action == "set-gps-manual" || action == "set-recording" -> 
                cmd.JonSharedCmd.Root.parseFrom(protoBytes)
            else -> 
                cmd.JonSharedCmd.Root.parseFrom(protoBytes)
        }
        
        // Log JSON for debugging
        println("$action as JSON:")
        println(jsonPrinter.print(rootMessage))
        
        // Verify
        verifyRoot(rootMessage as Message)
    }
    
    @Test
    fun `verify rotary goto Transit to Protobuf conversion`() {
        val params = transitMap(
            "azimuth" to 123.45,
            "elevation" to -15.5
        )
        
        testCommandConversion("rotaryplatform-goto", params) { root ->
            val typedRoot = root as cmd.JonSharedCmdRotaryPlatform.Root
            assertTrue(typedRoot.hasGoto())
            
            verifyProtobufFields(typedRoot.goto, mapOf(
                "azimuth" to 123.45,
                "elevation" to -15.5
            ))
        }
    }
    
    @Test
    fun `verify GPS manual Transit to Protobuf conversion`() {
        val params = transitMap(
            "use-manual" to true,
            "latitude" to 40.7128,
            "longitude" to -74.0060,
            "altitude" to 10.5
        )
        
        testCommandConversion("set-gps-manual", params) { root ->
            val typedRoot = root as cmd.JonSharedCmd.Root
            assertTrue(typedRoot.hasSetGpsManual())
            
            verifyProtobufFields(typedRoot.setGpsManual, mapOf(
                "use_manual" to true,
                "latitude" to 40.7128,
                "longitude" to -74.0060,
                "altitude" to 10.5
            ))
        }
    }
    
    @Test
    fun `verify heat camera palette conversion with enum`() {
        val params = transitMap(
            "palette" to "white-hot"
        )
        
        testCommandConversion("heatcamera-palette", params) { root ->
            val typedRoot = root as cmd.JonSharedCmdHeatCamera.Root
            assertTrue(typedRoot.hasPalette())
            
            // Enum should be converted to uppercase with underscores
            assertEquals("WHITE_HOT", typedRoot.palette.palette.name)
        }
    }
    
    @Test
    fun `verify CV track command with all fields`() {
        val params = transitMap(
            "channel" to "heat",
            "x" to 0.5,
            "y" to -0.25,
            "frame-timestamp" to 1234567890L
        )
        
        testCommandConversion("cv-start-track-ndc", params) { root ->
            val typedRoot = root as cmd.JonSharedCmdCv.Root
            assertTrue(typedRoot.hasStartTrackNdc())
            
            val cmd = typedRoot.startTrackNdc
            assertEquals("heat", cmd.channel)
            assertEquals(0.5, cmd.x, 0.001)
            assertEquals(-0.25, cmd.y, 0.001)
            assertEquals(1234567890L, cmd.frameTimestamp)
        }
    }
    
    @Test
    fun `verify optional parameters are handled correctly`() {
        // Day camera focus with optional distance
        val paramsWithDistance = transitMap(
            "mode" to "manual",
            "distance" to 10.0
        )
        
        testCommandConversion("daycamera-focus", paramsWithDistance) { root ->
            val typedRoot = root as cmd.JonSharedCmdDayCamera.Root
            assertTrue(typedRoot.hasFocus())
            
            val focus = typedRoot.focus
            assertEquals("MANUAL", focus.mode.name)
            assertTrue(focus.hasDistance())
            assertEquals(10.0, focus.distance, 0.001)
        }
        
        // Same command without optional distance
        val paramsWithoutDistance = transitMap(
            "mode" to "auto"
        )
        
        testCommandConversion("daycamera-focus", paramsWithoutDistance) { root ->
            val typedRoot = root as cmd.JonSharedCmdDayCamera.Root
            val focus = typedRoot.focus
            assertEquals("AUTO", focus.mode.name)
            assertFalse(focus.hasDistance())
        }
    }
    
    @Test
    fun `generate test report for all commands`() {
        // This test generates a report showing Transit input vs Protobuf output
        val testCases = listOf(
            Triple("ping", null, "Basic ping command"),
            Triple("rotaryplatform-goto", transitMap("azimuth" to 180.0, "elevation" to 45.0), "Rotary goto"),
            Triple("heatcamera-zoom", transitMap("zoom" to 4.0), "Heat camera zoom"),
            Triple("set-recording", transitMap("enabled" to true), "Enable recording")
        )
        
        val report = StringBuilder()
        report.appendLine("=== Transit to Protobuf Conversion Report ===")
        report.appendLine()
        
        testCases.forEach { (action, params, description) ->
            report.appendLine("Test: $description")
            report.appendLine("Action: $action")
            report.appendLine("Transit params: $params")
            
            val result = commandBuilder.buildCommand(action, params)
            if (result.isSuccess) {
                val protoBytes = result.getOrThrow().toByteArray()
                report.appendLine("Protobuf size: ${protoBytes.size} bytes")
                report.appendLine("Success: ✓")
            } else {
                report.appendLine("Failed: ${result.exceptionOrNull()?.message}")
            }
            report.appendLine()
        }
        
        println(report.toString())
    }
}

/**
 * Extension to make assertions more readable
 */
fun Message.hasField(fieldName: String): Boolean {
    val field = this.descriptorForType.findFieldByName(fieldName.replace("-", "_"))
    return field != null && this.hasField(field)
}