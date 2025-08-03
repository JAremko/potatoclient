package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import cmd.JonSharedCmdRotaryPlatform
import com.cognitect.transit.TransitFactory
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

/**
 * Tests that verify Transit messages are correctly converted to Protobuf
 * by deserializing the protobuf and comparing values
 */
class ProtobufRoundtripTest {
    
    private lateinit var commandBuilder: ProtobufCommandBuilder
    private val jsonPrinter = JsonFormat.printer()
        .includingDefaultValueFields()
        .preservingProtoFieldNames()
    
    @BeforeEach
    fun setUp() {
        commandBuilder = ProtobufCommandBuilder.getInstance()
    }
    
    /**
     * Convert protobuf message to JSON for comparison
     */
    private fun protoToJson(message: MessageOrBuilder): String {
        return jsonPrinter.print(message)
    }
    
    /**
     * Parse JSON and extract nested values
     */
    private fun parseJson(json: String): Map<String, Any> {
        // Simple JSON parser for our test needs
        return jacksonObjectMapper().readValue(json, Map::class.java) as Map<String, Any>
    }
    
    @Test
    fun `test ping command roundtrip`() {
        // Transit message
        val transitAction = "ping"
        val transitParams: Map<*, *>? = null
        
        // Build protobuf
        val result = commandBuilder.buildCommand(transitAction, transitParams)
        assertTrue(result.isSuccess)
        
        val protoBytes = result.getOrThrow().toByteArray()
        
        // Deserialize back
        val deserialized = JonSharedCmd.Root.parseFrom(protoBytes)
        
        // Verify it's a ping command
        assertTrue(deserialized.hasPing())
        assertEquals(JonSharedCmd.Root.DataCase.PING, deserialized.dataCase)
        
        // Convert to JSON for inspection
        val json = protoToJson(deserialized)
        println("Ping command as JSON: $json")
        
        // Verify JSON structure
        val jsonMap = parseJson(json)
        assertNotNull(jsonMap["ping"])
    }
    
    @Test
    fun `test rotary goto command with parameters`() {
        // Transit message
        val transitAction = "rotaryplatform-goto"
        val transitParams = mapOf(
            TransitFactory.keyword("azimuth") to 45.5,
            TransitFactory.keyword("elevation") to 30.0
        )
        
        // Build protobuf
        val result = commandBuilder.buildCommand(transitAction, transitParams)
        assertTrue(result.isSuccess)
        
        val protoBytes = result.getOrThrow().toByteArray()
        
        // Deserialize back
        val deserialized = JonSharedCmdRotaryPlatform.Root.parseFrom(protoBytes)
        
        // Verify it's a goto command
        assertTrue(deserialized.hasGoto())
        assertEquals(JonSharedCmdRotaryPlatform.Root.DataCase.GOTO, deserialized.dataCase)
        
        // Verify values match
        val gotoCmd = deserialized.goto
        assertEquals(45.5, gotoCmd.azimuth, 0.001)
        assertEquals(30.0, gotoCmd.elevation, 0.001)
        
        // Convert to JSON for comparison
        val json = protoToJson(deserialized)
        println("Goto command as JSON: $json")
        
        // Parse JSON and verify structure
        val jsonMap = parseJson(json)
        val gotoJson = jsonMap["goto"] as Map<*, *>
        assertEquals(45.5, (gotoJson["azimuth"] as Number).toDouble(), 0.001)
        assertEquals(30.0, (gotoJson["elevation"] as Number).toDouble(), 0.001)
    }
    
    @Test
    fun `test complex command with nested data`() {
        // Test set-gps-manual with all parameters
        val transitAction = "set-gps-manual"
        val transitParams = mapOf(
            TransitFactory.keyword("use-manual") to true,
            TransitFactory.keyword("latitude") to 51.5074,
            TransitFactory.keyword("longitude") to -0.1278,
            TransitFactory.keyword("altitude") to 100.5
        )
        
        // Build protobuf
        val result = commandBuilder.buildCommand(transitAction, transitParams)
        assertTrue(result.isSuccess)
        
        val protoBytes = result.getOrThrow().toByteArray()
        
        // Deserialize and verify
        val deserialized = JonSharedCmd.Root.parseFrom(protoBytes)
        assertTrue(deserialized.hasSetGpsManual())
        
        val gpsCmd = deserialized.setGpsManual
        assertTrue(gpsCmd.useManual)
        assertEquals(51.5074, gpsCmd.latitude, 0.00001)
        assertEquals(-0.1278, gpsCmd.longitude, 0.00001) 
        assertEquals(100.5, gpsCmd.altitude, 0.1)
        
        // JSON comparison
        val json = protoToJson(deserialized)
        println("GPS command as JSON: $json")
        
        val jsonMap = parseJson(json)
        val gpsJson = jsonMap["set_gps_manual"] as Map<*, *>
        assertEquals(true, gpsJson["use_manual"])
        assertEquals(51.5074, (gpsJson["latitude"] as Number).toDouble(), 0.00001)
    }
    
    @Test
    fun `test command with enum values`() {
        // Test day-camera-focus with enum mode
        val transitAction = "daycamera-focus"
        val transitParams = mapOf(
            TransitFactory.keyword("mode") to "manual",
            TransitFactory.keyword("distance") to 5.0
        )
        
        // Build protobuf
        val result = commandBuilder.buildCommand(transitAction, transitParams)
        assertTrue(result.isSuccess)
        
        val protoBytes = result.getOrThrow().toByteArray()
        
        // Deserialize and check JSON
        val deserialized = JonSharedCmdDayCamera.Root.parseFrom(protoBytes)
        val json = protoToJson(deserialized)
        println("Focus command as JSON: $json")
        
        // Verify enum was correctly set
        val jsonMap = parseJson(json)
        val focusJson = jsonMap["focus"] as Map<*, *>
        assertEquals("MANUAL", focusJson["mode"]) // Protobuf enums are uppercase
        assertEquals(5.0, (focusJson["distance"] as Number).toDouble(), 0.001)
    }
    
    @Test
    fun `test missing required parameters`() {
        // Try to build goto without required params
        val transitAction = "rotaryplatform-goto"
        val transitParams = mapOf(
            TransitFactory.keyword("azimuth") to 45.5
            // Missing elevation!
        )
        
        // Should fail
        val result = commandBuilder.buildCommand(transitAction, transitParams)
        assertTrue(result.isFailure)
        
        val error = result.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error!!.message!!.contains("elevation"))
    }
    
    @Test
    fun `test protobuf validation constraints`() {
        // Try to build goto with out-of-range azimuth
        val transitAction = "rotaryplatform-goto"
        val transitParams = mapOf(
            TransitFactory.keyword("azimuth") to 400.0, // > 360!
            TransitFactory.keyword("elevation") to 30.0
        )
        
        // Build should succeed (no validation at build time)
        val result = commandBuilder.buildCommand(transitAction, transitParams)
        assertTrue(result.isSuccess)
        
        // But the protobuf should contain the invalid value
        val protoBytes = result.getOrThrow().toByteArray()
        val deserialized = JonSharedCmdRotaryPlatform.Root.parseFrom(protoBytes)
        
        // Value is stored even though it's invalid
        assertEquals(400.0, deserialized.goto.azimuth, 0.001)
        
        // Note: buf.validate would catch this when actually sending,
        // but that requires the protovalidate-java library
    }
}

// Simple Jackson import for JSON parsing
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper