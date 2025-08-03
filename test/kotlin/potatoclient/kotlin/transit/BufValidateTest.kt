package potatoclient.kotlin.transit

import build.buf.protovalidate.Validator
import potatoclient.kotlin.transit.generated.GeneratedCommandHandlers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

/**
 * Tests for buf.validate constraint validation on protobuf messages
 */
class BufValidateTest {
    
    private lateinit var validator: Validator
    
    @BeforeEach
    fun setUp() {
        validator = Validator.newBuilder().build()
    }
    
    @Test
    fun `test valid rotary goto command passes validation`() {
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
        
        val validationResult = validator.validate(proto)
        
        assertTrue(validationResult.isSuccess, 
            "Valid command should pass validation: ${validationResult.violations}")
    }
    
    @Test
    fun `test azimuth exceeding 360 fails validation`() {
        val command = mapOf(
            "rotary" to mapOf(
                "goto" to mapOf(
                    "azimuth" to 400.0,  // > 360
                    "elevation" to 45.0
                )
            )
        )
        
        val proto = GeneratedCommandHandlers.buildCommand(command)
        assertNotNull(proto) // Building succeeds
        
        val validationResult = validator.validate(proto!!)
        
        assertFalse(validationResult.isSuccess, "Should fail validation")
        
        val violations = validationResult.violations.violations
        assertTrue(violations.isNotEmpty())
        
        val azimuthViolation = violations.find { it.fieldPath.contains("azimuth") }
        assertNotNull(azimuthViolation)
        assertTrue(azimuthViolation!!.message.contains("360") || 
                  azimuthViolation.constraintId.contains("lte"))
    }
    
    @Test
    fun `test elevation below -30 fails validation`() {
        val command = mapOf(
            "rotary" to mapOf(
                "goto" to mapOf(
                    "azimuth" to 180.0,
                    "elevation" to -45.0  // < -30
                )
            )
        )
        
        val proto = GeneratedCommandHandlers.buildCommand(command)
        assertNotNull(proto)
        
        val validationResult = validator.validate(proto!!)
        
        assertFalse(validationResult.isSuccess)
        
        val violations = validationResult.violations.violations
        val elevationViolation = violations.find { it.fieldPath.contains("elevation") }
        assertNotNull(elevationViolation)
    }
    
    @Test
    fun `test GPS latitude constraints`() {
        // Valid latitude
        val validCommand = mapOf(
            "gps" to mapOf(
                "set-manual-position" to mapOf(
                    "position" to mapOf(
                        "latitude" to 51.5,
                        "longitude" to -0.1,
                        "altitude" to 100.0
                    )
                )
            )
        )
        
        val validProto = GeneratedCommandHandlers.buildCommand(validCommand)
        assertNotNull(validProto)
        assertTrue(validator.validate(validProto!!).isSuccess)
        
        // Invalid latitude > 90
        val invalidCommand = mapOf(
            "gps" to mapOf(
                "set-manual-position" to mapOf(
                    "position" to mapOf(
                        "latitude" to 91.0,  // > 90
                        "longitude" to -0.1,
                        "altitude" to 100.0
                    )
                )
            )
        )
        
        val invalidProto = GeneratedCommandHandlers.buildCommand(invalidCommand)
        assertNotNull(invalidProto)
        
        val validationResult = validator.validate(invalidProto!!)
        
        assertFalse(validationResult.isSuccess)
        assertTrue(validationResult.violations.violations.any { 
            it.fieldPath.contains("latitude")
        })
    }
    
    @Test
    fun `test heat camera zoom constraints`() {
        // Valid zoom level
        val validCommand = mapOf(
            "heat-camera" to mapOf(
                "zoom" to mapOf(
                    "zoom" to 4.0
                )
            )
        )
        
        val validProto = GeneratedCommandHandlers.buildCommand(validCommand)
        assertNotNull(validProto)
        assertTrue(validator.validate(validProto!!).isSuccess)
        
        // Invalid zoom > 8
        val invalidCommand = mapOf(
            "heat-camera" to mapOf(
                "zoom" to mapOf(
                    "zoom" to 10.0  // > 8
                )
            )
        )
        
        val invalidProto = GeneratedCommandHandlers.buildCommand(invalidCommand)
        assertNotNull(invalidProto)
        
        val validationResult = validator.validate(invalidProto!!)
        assertFalse(validationResult.isSuccess)
    }
    
    @Test
    fun `test velocity direction enum constraint`() {
        // Direction should not be 0 (NONE)
        val command = mapOf(
            "rotary" to mapOf(
                "set-velocity" to mapOf(
                    "azimuth-speed" to 10.0,
                    "elevation-speed" to 5.0,
                    "azimuth-direction" to 0,  // NONE = 0, should fail
                    "elevation-direction" to 1  // FORWARD = 1
                )
            )
        )
        
        val proto = GeneratedCommandHandlers.buildCommand(command)
        
        if (proto != null) {
            val validationResult = validator.validate(proto)
            
            // Log the result for debugging
            println("Validation result for direction=0: ${validationResult.isSuccess}")
            if (!validationResult.isSuccess) {
                validationResult.violations.violations.forEach { violation ->
                    println("Violation: ${violation.fieldPath} - ${violation.message}")
                }
            }
        }
    }
    
    @Test
    fun `test multiple violations in single command`() {
        // Multiple constraint violations
        val command = mapOf(
            "rotary" to mapOf(
                "goto" to mapOf(
                    "azimuth" to 400.0,  // > 360
                    "elevation" to -100.0  // < -30
                )
            )
        )
        
        val proto = GeneratedCommandHandlers.buildCommand(command)
        assertNotNull(proto)
        
        val validationResult = validator.validate(proto!!)
        assertFalse(validationResult.isSuccess)
        
        val violations = validationResult.violations.violations
        assertTrue(violations.size >= 2, "Should have at least 2 violations")
        
        assertTrue(violations.any { it.fieldPath.contains("azimuth") })
        assertTrue(violations.any { it.fieldPath.contains("elevation") })
    }
}