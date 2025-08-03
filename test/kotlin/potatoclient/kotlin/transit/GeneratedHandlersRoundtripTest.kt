package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import com.cognitect.transit.Keyword
import com.cognitect.transit.TransitFactory
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import potatoclient.kotlin.transit.generated.GeneratedCommandHandlers
import potatoclient.kotlin.transit.generated.GeneratedStateHandlers

/**
 * Roundtrip tests for generated Transit handlers
 * Tests: Transit Map → Protobuf → Binary → Protobuf → Transit Map
 * 
 * Test data can be generated using the transit-test-generator tool:
 * cd tools/transit-test-generator
 * bb batch --output-dir test-data/
 */
class GeneratedHandlersRoundtripTest {
    
    private val jsonPrinter = JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames()
    
    /**
     * Convert protobuf message to JSON for debugging
     */
    private fun protoToJson(message: MessageOrBuilder): String {
        return jsonPrinter.print(message)
    }
    
    /**
     * Helper to create Transit keyword
     */
    private fun kw(name: String): Keyword = TransitFactory.keyword(name)
    
    // ============================================================================
    // Basic Command Tests
    // ============================================================================
    
    @Test
    fun `test ping command roundtrip`() {
        // Create Transit map for ping command
        val transitMap = mapOf(
            "ping" to emptyMap<String, Any>()
        )
        
        // Convert to protobuf using generated handlers
        val proto = GeneratedCommandHandlers.buildCommand(transitMap)
        
        // Verify it's a ping command
        assertTrue(proto.hasPing())
        assertEquals(JonSharedCmd.Root.PayloadCase.PING, proto.payloadCase)
        
        // Convert to binary and back
        val binary = proto.toByteArray()
        val deserializedProto = JonSharedCmd.Root.parseFrom(binary)
        
        // Convert back to Transit map
        val resultMap = GeneratedCommandHandlers.extractCommand(deserializedProto)
        
        // Verify roundtrip
        assertEquals("ping", resultMap.keys.first())
        assertTrue(resultMap["ping"] is Map<*, *>)
        
        // Debug output
        println("Ping command JSON: ${protoToJson(proto)}")
        println("Roundtrip result: $resultMap")
    }
    
    @Test
    fun `test noop command roundtrip`() {
        // Create Transit map for noop command
        val transitMap = mapOf(
            "noop" to emptyMap<String, Any>()
        )
        
        // Convert to protobuf
        val proto = GeneratedCommandHandlers.buildCommand(transitMap)
        
        // Verify it's a noop command
        assertTrue(proto.hasNoop())
        assertEquals(JonSharedCmd.Root.PayloadCase.NOOP, proto.payloadCase)
        
        // Roundtrip through binary
        val binary = proto.toByteArray()
        val deserializedProto = JonSharedCmd.Root.parseFrom(binary)
        val resultMap = GeneratedCommandHandlers.extractCommand(deserializedProto)
        
        // Verify roundtrip
        assertEquals("noop", resultMap.keys.first())
    }
    
    @Test
    fun `test frozen command roundtrip`() {
        // Create Transit map for frozen command
        val transitMap = mapOf(
            "frozen" to emptyMap<String, Any>()
        )
        
        // Convert to protobuf
        val proto = GeneratedCommandHandlers.buildCommand(transitMap)
        
        // Verify it's a frozen command
        assertTrue(proto.hasFrozen())
        assertEquals(JonSharedCmd.Root.PayloadCase.FROZEN, proto.payloadCase)
        
        // Roundtrip through binary
        val binary = proto.toByteArray()
        val deserializedProto = JonSharedCmd.Root.parseFrom(binary)
        val resultMap = GeneratedCommandHandlers.extractCommand(deserializedProto)
        
        // Verify roundtrip
        assertEquals("frozen", resultMap.keys.first())
    }
    
    // ============================================================================
    // Commands with Parameters
    // ============================================================================
    
    @Test
    fun `test CV start track NDC command with parameters`() {
        // Create Transit map for cv start-track-ndc command
        val transitMap = mapOf(
            "cv" to mapOf(
                "start-track-ndc" to mapOf(
                    "channel" to "heat",
                    "x" to 0.5f,
                    "y" to -0.25f,
                    "frame-time" to 1234567890L
                )
            )
        )
        
        // Convert to protobuf
        val proto = GeneratedCommandHandlers.buildCommand(transitMap)
        
        // Verify structure
        assertTrue(proto.hasCv())
        assertTrue(proto.cv.hasStartTrackNdc())
        
        val trackCmd = proto.cv.startTrackNdc
        assertEquals(0.5f, trackCmd.x)
        assertEquals(-0.25f, trackCmd.y)
        assertEquals(1234567890L, trackCmd.frameTime)
        
        // Roundtrip through binary
        val binary = proto.toByteArray()
        val deserializedProto = JonSharedCmd.Root.parseFrom(binary)
        val resultMap = GeneratedCommandHandlers.extractCommand(deserializedProto)
        
        // Verify roundtrip
        val cvMap = resultMap["cv"] as Map<*, *>
        val trackMap = cvMap["start-track-ndc"] as Map<*, *>
        assertEquals(0.5, trackMap["x"])
        assertEquals(-0.25, trackMap["y"])
        assertEquals(1234567890L, trackMap["frame-time"])
        
        // Channel should be converted back to string from enum
        assertEquals("heat", trackMap["channel"])
        
        println("CV command JSON: ${protoToJson(proto)}")
        println("Roundtrip result: $resultMap")
    }
    
    @Test
    fun `test rotary goto command with azimuth and elevation`() {
        // Create Transit map for rotary goto command
        val transitMap = mapOf(
            "rotary" to mapOf(
                "goto" to mapOf(
                    "azimuth" to 45.5,
                    "elevation" to 30.0
                )
            )
        )
        
        // Convert to protobuf
        val proto = GeneratedCommandHandlers.buildCommand(transitMap)
        
        // Verify structure
        assertTrue(proto.hasRotary())
        assertTrue(proto.rotary.hasGoto())
        
        val gotoCmd = proto.rotary.goto
        assertEquals(45.5, gotoCmd.azimuth)
        assertEquals(30.0, gotoCmd.elevation)
        
        // Roundtrip through binary
        val binary = proto.toByteArray()
        val deserializedProto = JonSharedCmd.Root.parseFrom(binary)
        val resultMap = GeneratedCommandHandlers.extractCommand(deserializedProto)
        
        // Verify roundtrip
        val rotaryMap = resultMap["rotary"] as Map<*, *>
        val gotoMap = rotaryMap["goto"] as Map<*, *>
        assertEquals(45.5, gotoMap["azimuth"])
        assertEquals(30.0, gotoMap["elevation"])
    }
    
    // ============================================================================
    // Complex Nested Commands
    // ============================================================================
    
    @Test
    fun `test rotary scan add node with camelCase fields`() {
        // Create Transit map with camelCase fields
        val transitMap = mapOf(
            "rotary" to mapOf(
                "scan-add-node" to mapOf(
                    "index" to 5,
                    "DayZoomTableValue" to 10,
                    "HeatZoomTableValue" to 15,
                    "azimuth" to 180.0,
                    "elevation" to 45.0,
                    "linger" to 2.5,
                    "speed" to 100.0
                )
            )
        )
        
        // Convert to protobuf
        val proto = GeneratedCommandHandlers.buildCommand(transitMap)
        
        // Verify structure
        assertTrue(proto.hasRotary())
        assertTrue(proto.rotary.hasScanAddNode())
        
        val scanNode = proto.rotary.scanAddNode
        assertEquals(5, scanNode.index)
        assertEquals(10, scanNode.dayZoomTableValue)
        assertEquals(15, scanNode.heatZoomTableValue)
        assertEquals(180.0, scanNode.azimuth)
        assertEquals(45.0, scanNode.elevation)
        assertEquals(2.5, scanNode.linger)
        assertEquals(100.0, scanNode.speed)
        
        // Roundtrip through binary
        val binary = proto.toByteArray()
        val deserializedProto = JonSharedCmd.Root.parseFrom(binary)
        val resultMap = GeneratedCommandHandlers.extractCommand(deserializedProto)
        
        // Verify roundtrip preserves camelCase field names
        val rotaryMap = resultMap["rotary"] as Map<*, *>
        val scanMap = rotaryMap["scan-add-node"] as Map<*, *>
        assertEquals(10, scanMap["DayZoomTableValue"])
        assertEquals(15, scanMap["HeatZoomTableValue"])
        
        println("Scan node JSON: ${protoToJson(proto)}")
        println("Roundtrip result: $resultMap")
    }
    
    @Test
    fun `test day camera zoom commands with nested structure`() {
        // Create Transit map for day camera zoom move
        val transitMap = mapOf(
            "day-camera" to mapOf(
                "zoom" to mapOf(
                    "move" to mapOf(
                        "target-value" to 50.0f,
                        "speed" to 2.5f
                    )
                )
            )
        )
        
        // Convert to protobuf
        val proto = GeneratedCommandHandlers.buildCommand(transitMap)
        
        // Verify structure
        assertTrue(proto.hasDayCamera())
        assertTrue(proto.dayCamera.hasZoom())
        assertTrue(proto.dayCamera.zoom.hasMove())
        
        val moveCmd = proto.dayCamera.zoom.move
        assertEquals(50.0f, moveCmd.targetValue)
        assertEquals(2.5f, moveCmd.speed)
        
        // Roundtrip through binary
        val binary = proto.toByteArray()
        val deserializedProto = JonSharedCmd.Root.parseFrom(binary)
        val resultMap = GeneratedCommandHandlers.extractCommand(deserializedProto)
        
        // Verify roundtrip
        val dayCamMap = resultMap["day-camera"] as Map<*, *>
        val zoomMap = dayCamMap["zoom"] as Map<*, *>
        val moveMap = zoomMap["move"] as Map<*, *>
        assertEquals(50.0, moveMap["target-value"])
        assertEquals(2.5, moveMap["speed"])
    }
    
    // ============================================================================
    // Enum Handling Tests
    // ============================================================================
    
    @Test
    fun `test system localization command with enum`() {
        // Create Transit map with enum value
        val transitMap = mapOf(
            "system" to mapOf(
                "localization" to mapOf(
                    "loc" to "uk"  // Ukrainian locale
                )
            )
        )
        
        // Convert to protobuf
        val proto = GeneratedCommandHandlers.buildCommand(transitMap)
        
        // Verify structure
        assertTrue(proto.hasSystem())
        assertTrue(proto.system.hasLocalization())
        
        // Roundtrip through binary
        val binary = proto.toByteArray()
        val deserializedProto = JonSharedCmd.Root.parseFrom(binary)
        val resultMap = GeneratedCommandHandlers.extractCommand(deserializedProto)
        
        // Verify enum converts back to lowercase string
        val systemMap = resultMap["system"] as Map<*, *>
        val locMap = systemMap["localization"] as Map<*, *>
        assertEquals("uk", locMap["loc"])
    }
    
    // ============================================================================
    // Common Command Disambiguation Tests
    // ============================================================================
    
    @Test
    fun `test different start commands are properly disambiguated`() {
        // Test GPS start
        val gpsStart = mapOf("gps" to mapOf("start" to emptyMap<String, Any>()))
        val gpsProto = GeneratedCommandHandlers.buildCommand(gpsStart)
        assertTrue(gpsProto.hasGps())
        assertTrue(gpsProto.gps.hasStart())
        
        // Test LRF start
        val lrfStart = mapOf("lrf" to mapOf("start" to emptyMap<String, Any>()))
        val lrfProto = GeneratedCommandHandlers.buildCommand(lrfStart)
        assertTrue(lrfProto.hasLrf())
        assertTrue(lrfProto.lrf.hasStart())
        
        // Test Day Camera start
        val dayStart = mapOf("day-camera" to mapOf("start" to emptyMap<String, Any>()))
        val dayProto = GeneratedCommandHandlers.buildCommand(dayStart)
        assertTrue(dayProto.hasDayCamera())
        assertTrue(dayProto.dayCamera.hasStart())
        
        // Verify each produces different protobuf messages
        assertNotEquals(gpsProto.toByteArray().contentToString(), 
                       lrfProto.toByteArray().contentToString())
        assertNotEquals(lrfProto.toByteArray().contentToString(), 
                       dayProto.toByteArray().contentToString())
    }
    
    // ============================================================================
    // Example: Using Generated Test Data
    // ============================================================================
    
    /*
    // Example test using transit-test-generator output
    @Test
    fun `test with generated Transit data`() {
        // Assuming test data was generated:
        // cd tools/transit-test-generator
        // bb batch --output-dir ../../test/resources/transit-test-data/
        
        val testDataDir = File("src/test/resources/transit-test-data")
        if (testDataDir.exists()) {
            testDataDir.listFiles { file -> 
                file.name.endsWith(".transit") 
            }?.forEach { transitFile ->
                println("Testing with: ${transitFile.name}")
                
                // Read Transit data
                val transitData = transitFile.readBytes()
                val reader = Transit.reader(transitData.inputStream(), Transit.Format.MSGPACK)
                val transitMap = reader.read<Map<String, Any>>()
                
                // Convert to protobuf
                val proto = GeneratedCommandHandlers.buildCommand(transitMap)
                
                // Roundtrip test
                val binary = proto.toByteArray()
                val deserialized = JonSharedCmd.Root.parseFrom(binary)
                val resultMap = GeneratedCommandHandlers.extractCommand(deserialized)
                
                // Basic structure should match
                assertEquals(transitMap.keys, resultMap.keys)
            }
        }
    }
    */
}