package potatoclient.kotlin.transit

import build.buf.protovalidate.Validator
import cmd.JonSharedCmd
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import potatoclient.kotlin.transit.generated.GeneratedCommandHandlers

/**
 * Sanity checks to ensure our validation pipeline actually validates at each stage
 */
class ValidatorSanityTest {
    
    private lateinit var validator: Validator
    
    @BeforeEach
    fun setUp() {
        validator = Validator.newBuilder().build()
    }
    
    @Test
    fun `test validator rejects constraint violations`() {
        // Create a command that violates constraints
        val command = mapOf(
            "rotary" to mapOf(
                "goto" to mapOf(
                    "azimuth" to 400.0,  // > 360 - should fail
                    "elevation" to 45.0
                )
            )
        )
        
        // Build protobuf
        val proto = GeneratedCommandHandlers.buildCommand(command)
        assertNotNull(proto, "Should build protobuf even with invalid values")
        
        // Validate - should fail
        val result = validator.validate(proto!!)
        assertFalse(result.isSuccess, "Validation should fail for azimuth > 360")
        
        // Check we get meaningful error
        val violations = result.violations.violationsList
        assertTrue(violations.isNotEmpty(), "Should have violations")
        
        val azimuthViolation = violations.find { it.fieldPath.contains("azimuth") }
        assertNotNull(azimuthViolation, "Should have azimuth violation")
        
        // Verify error mentions the constraint
        val errorMessage = azimuthViolation!!.message
        assertTrue(
            errorMessage.contains("360") || errorMessage.contains("less than or equal"),
            "Error should mention the constraint: $errorMessage"
        )
    }
    
    @Test
    fun `test validator accepts valid values`() {
        // Valid command
        val command = mapOf(
            "rotary" to mapOf(
                "goto" to mapOf(
                    "azimuth" to 180.0,
                    "elevation" to 45.0
                )
            )
        )
        
        val proto = GeneratedCommandHandlers.buildCommand(command)
        assertNotNull(proto)
        
        val result = validator.validate(proto!!)
        assertTrue(result.isSuccess, "Valid command should pass validation")
        assertTrue(result.violations.violationsList.isEmpty(), "Should have no violations")
    }
    
    @Test
    fun `test binary roundtrip preserves data`() {
        val command = mapOf(
            "cv" to mapOf(
                "start-track-ndc" to mapOf(
                    "channel" to "heat",
                    "x" to 0.5,
                    "y" to -0.5,
                    "frame-time" to 123456789L
                )
            )
        )
        
        // Build proto
        val original = GeneratedCommandHandlers.buildCommand(command)
        assertNotNull(original)
        
        // Binary roundtrip
        val binary = original!!.toByteArray()
        assertTrue(binary.isNotEmpty(), "Should have binary data")
        
        val deserialized = JonSharedCmd.Root.parseFrom(binary)
        assertNotNull(deserialized)
        
        // Verify equality
        assertEquals(original, deserialized, "Protobuf should equal after roundtrip")
        assertEquals(original.hashCode(), deserialized.hashCode(), "Hash codes should match")
        
        // Verify data preserved
        assertEquals(original.cv.startTrackNdc.x, deserialized.cv.startTrackNdc.x)
        assertEquals(original.cv.startTrackNdc.y, deserialized.cv.startTrackNdc.y)
        assertEquals(original.cv.startTrackNdc.frameTime, deserialized.cv.startTrackNdc.frameTime)
    }
    
    @Test
    fun `test equals detects differences`() {
        val proto1 = GeneratedCommandHandlers.buildCommand(
            mapOf("ping" to emptyMap<String, Any>())
        )
        
        val proto2 = GeneratedCommandHandlers.buildCommand(
            mapOf("noop" to emptyMap<String, Any>())
        )
        
        assertNotNull(proto1)
        assertNotNull(proto2)
        assertNotEquals(proto1, proto2, "Different commands should not be equal")
        assertNotEquals(proto1.hashCode(), proto2.hashCode(), "Hash codes should differ")
    }
    
    @Test
    fun `test GeneratedCommandHandlers rejects invalid structure`() {
        // Test various invalid structures
        val invalidCommands = listOf(
            // Wrong type for nested value
            mapOf("rotary" to "not-a-map"),
            
            // Unknown command
            mapOf("unknown-command" to mapOf("foo" to "bar")),
            
            // Empty map (no command specified)
            emptyMap<String, Any>(),
            
            // Missing nested command
            mapOf("cv" to emptyMap<String, Any>())
        )
        
        for (invalidCommand in invalidCommands) {
            val proto = GeneratedCommandHandlers.buildCommand(invalidCommand)
            // Should either return null or create empty proto
            if (proto != null) {
                // If it creates a proto, it should be essentially empty
                assertEquals(
                    JonSharedCmd.Root.PayloadCase.PAYLOAD_NOT_SET,
                    proto.payloadCase,
                    "Invalid command should result in unset payload: $invalidCommand"
                )
            }
        }
    }
    
    @Test
    fun `test multiple constraint violations reported`() {
        // Command with multiple violations
        val command = mapOf(
            "rotary" to mapOf(
                "goto" to mapOf(
                    "azimuth" to 400.0,   // > 360
                    "elevation" to -50.0  // < -30
                )
            )
        )
        
        val proto = GeneratedCommandHandlers.buildCommand(command)
        assertNotNull(proto)
        
        val result = validator.validate(proto!!)
        assertFalse(result.isSuccess)
        
        val violations = result.violations.violationsList
        assertTrue(violations.size >= 2, "Should have at least 2 violations")
        
        // Check both violations are reported
        val hasAzimuthViolation = violations.any { it.fieldPath.contains("azimuth") }
        val hasElevationViolation = violations.any { it.fieldPath.contains("elevation") }
        
        assertTrue(hasAzimuthViolation, "Should report azimuth violation")
        assertTrue(hasElevationViolation, "Should report elevation violation")
    }
}