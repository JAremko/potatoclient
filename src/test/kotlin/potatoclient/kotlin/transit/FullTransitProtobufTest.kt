package potatoclient.kotlin.transit

import com.cognitect.transit.Reader
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Writer
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Full end-to-end test of Transit → Protobuf conversion
 * Simulates actual message flow from Clojure
 */
class FullTransitProtobufTest {
    
    private lateinit var commandBuilder: ProtobufCommandBuilder
    private lateinit var transitWriter: Writer<ByteArrayOutputStream>
    private lateinit var transitReader: Reader
    
    @BeforeEach
    fun setUp() {
        commandBuilder = ProtobufCommandBuilder.getInstance()
    }
    
    /**
     * Create a Transit message like Clojure would send
     */
    private fun createTransitMessage(action: String, params: Map<String, Any>?): ByteArray {
        val out = ByteArrayOutputStream()
        transitWriter = TransitFactory.writer(TransitFactory.Format.MSGPACK, out)
        
        // Create message envelope like Clojure does
        val message = mutableMapOf<Any, Any>(
            TransitFactory.keyword("msg-type") to "command",
            TransitFactory.keyword("msg-id") to "test-${System.currentTimeMillis()}",
            TransitFactory.keyword("timestamp") to System.currentTimeMillis(),
            TransitFactory.keyword("payload") to mutableMapOf<Any, Any>(
                TransitFactory.keyword("action") to action
            ).apply {
                if (params != null) {
                    this[TransitFactory.keyword("params")] = params.mapKeys { 
                        TransitFactory.keyword(it.key) 
                    }
                }
            }
        )
        
        transitWriter.write(message)
        return out.toByteArray()
    }
    
    /**
     * Read Transit message and extract payload
     */
    private fun readTransitMessage(data: ByteArray): Map<*, *> {
        val input = ByteArrayInputStream(data)
        transitReader = TransitFactory.reader(TransitFactory.Format.MSGPACK, input)
        return transitReader.read() as Map<*, *>
    }
    
    /**
     * Full test: Transit bytes → Parse → Build Proto → Verify
     */
    private fun testFullTransitFlow(
        action: String,
        params: Map<String, Any>?,
        verify: (Message, Map<*, *>) -> Unit
    ) {
        // 1. Create Transit message as Clojure would
        val transitBytes = createTransitMessage(action, params)
        println("Transit message size: ${transitBytes.size} bytes")
        
        // 2. Parse Transit message
        val transitMsg = readTransitMessage(transitBytes)
        val payload = transitMsg[TransitFactory.keyword("payload")] as Map<*, *>
        val extractedAction = payload[TransitFactory.keyword("action")] as String
        val extractedParams = payload[TransitFactory.keyword("params")] as? Map<*, *>
        
        assertEquals(action, extractedAction)
        
        // 3. Build protobuf command
        val result = commandBuilder.buildCommand(extractedAction, extractedParams)
        assertTrue(result.isSuccess, "Failed to build: ${result.exceptionOrNull()}")
        
        val protoBytes = result.getOrThrow().toByteArray()
        println("Protobuf size: ${protoBytes.size} bytes")
        
        // 4. Parse protobuf based on action type
        val protoMessage = when {
            action.startsWith("rotaryplatform-") -> 
                cmd.JonSharedCmdRotaryPlatform.Root.parseFrom(protoBytes)
            action.startsWith("heatcamera-") -> 
                cmd.JonSharedCmdHeatCamera.Root.parseFrom(protoBytes)
            else -> 
                cmd.JonSharedCmd.Root.parseFrom(protoBytes)
        }
        
        // 5. Convert to JSON for comparison
        val jsonPrinter = JsonFormat.printer()
            .includingDefaultValueFields()
            .preservingProtoFieldNames()
        val protoJson = jsonPrinter.print(protoMessage)
        println("Protobuf as JSON: $protoJson")
        
        // 6. Verify
        verify(protoMessage as Message, extractedParams ?: emptyMap<Any, Any>())
    }
    
    @Test
    fun `test full Transit to Protobuf flow for rotary goto`() {
        val params = mapOf(
            "azimuth" to 270.0,
            "elevation" to 15.0
        )
        
        testFullTransitFlow("rotaryplatform-goto", params) { proto, transitParams ->
            val rotaryRoot = proto as cmd.JonSharedCmdRotaryPlatform.Root
            assertTrue(rotaryRoot.hasGoto())
            
            val goto = rotaryRoot.goto
            val transitAzimuth = transitParams[TransitFactory.keyword("azimuth")] as Double
            val transitElevation = transitParams[TransitFactory.keyword("elevation")] as Double
            
            assertEquals(transitAzimuth, goto.azimuth, 0.001)
            assertEquals(transitElevation, goto.elevation, 0.001)
        }
    }
    
    @Test
    fun `test Transit preserves numeric precision`() {
        val preciseValue = 123.456789012345
        val params = mapOf(
            "azimuth" to preciseValue,
            "elevation" to 30.0
        )
        
        testFullTransitFlow("rotaryplatform-goto", params) { proto, transitParams ->
            val rotaryRoot = proto as cmd.JonSharedCmdRotaryPlatform.Root
            val goto = rotaryRoot.goto
            
            // Transit should preserve the precision
            val transitAzimuth = transitParams[TransitFactory.keyword("azimuth")] as Double
            assertEquals(preciseValue, transitAzimuth, 0.000000000001)
            
            // Protobuf double should also preserve it
            assertEquals(preciseValue, goto.azimuth, 0.000000000001)
        }
    }
    
    @Test
    fun `test complex nested Transit message`() {
        // Simulate a complex command with nested data
        val params = mapOf(
            "use-manual" to true,
            "latitude" to 51.5074,
            "longitude" to -0.1278,
            "altitude" to 100.0
        )
        
        testFullTransitFlow("set-gps-manual", params) { proto, transitParams ->
            val root = proto as cmd.JonSharedCmd.Root
            assertTrue(root.hasSetGpsManual())
            
            val gps = root.setGpsManual
            
            // Verify all fields match Transit input
            assertEquals(
                transitParams[TransitFactory.keyword("use-manual")], 
                gps.useManual
            )
            assertEquals(
                transitParams[TransitFactory.keyword("latitude")] as Double,
                gps.latitude,
                0.0001
            )
            assertEquals(
                transitParams[TransitFactory.keyword("longitude")] as Double,
                gps.longitude,
                0.0001
            )
            assertEquals(
                transitParams[TransitFactory.keyword("altitude")] as Double,
                gps.altitude,
                0.1
            )
        }
    }
    
    @Test
    fun `compare Transit and Protobuf sizes`() {
        // Test various commands and compare message sizes
        val testCases = listOf(
            "ping" to null,
            "rotaryplatform-goto" to mapOf("azimuth" to 180.0, "elevation" to 45.0),
            "heatcamera-zoom" to mapOf("zoom" to 4.0),
            "set-gps-manual" to mapOf(
                "use-manual" to true,
                "latitude" to 51.5074,
                "longitude" to -0.1278,
                "altitude" to 100.0
            )
        )
        
        println("\n=== Size Comparison: Transit vs Protobuf ===")
        testCases.forEach { (action, params) ->
            val transitBytes = createTransitMessage(action, params)
            
            val transitMsg = readTransitMessage(transitBytes)
            val payload = transitMsg[TransitFactory.keyword("payload")] as Map<*, *>
            val extractedParams = payload[TransitFactory.keyword("params")] as? Map<*, *>
            
            val result = commandBuilder.buildCommand(action, extractedParams)
            if (result.isSuccess) {
                val protoBytes = result.getOrThrow().toByteArray()
                
                println("$action:")
                println("  Transit: ${transitBytes.size} bytes")
                println("  Protobuf: ${protoBytes.size} bytes")
                println("  Ratio: ${String.format("%.2f", protoBytes.size.toDouble() / transitBytes.size)}")
            }
        }
    }
}

/**
 * Helper to create keyword-based maps for testing
 */
fun transitParams(vararg pairs: Pair<String, Any>): Map<Any, Any> {
    return pairs.associate { (k, v) -> 
        TransitFactory.keyword(k) to v
    }
}