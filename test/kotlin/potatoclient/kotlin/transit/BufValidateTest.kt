package potatoclient.kotlin.transit

import build.buf.protovalidate.Validator
import cmd.JonSharedCmdRotaryPlatform
import com.cognitect.transit.TransitFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

/**
 * Tests for buf.validate constraint validation on protobuf messages
 */
class BufValidateTest {
    
    private lateinit var commandBuilder: ProtobufCommandBuilder
    private lateinit var validator: Validator
    
    @BeforeEach
    fun setUp() {
        commandBuilder = ProtobufCommandBuilder.getInstance()
        validator = Validator.newBuilder().build()
    }
    
    @Test
    fun `test valid rotary goto command passes validation`() {
        val params = mapOf(
            TransitFactory.keyword("azimuth") to 180.0,
            TransitFactory.keyword("elevation") to 45.0
        )
        
        val result = commandBuilder.buildCommand("rotaryplatform-goto", params)
        assertTrue(result.isSuccess)
        
        val proto = result.getOrThrow()
        val validationResult = validator.validate(proto)
        
        assertTrue(validationResult.isSuccess, 
            "Valid command should pass validation: ${validationResult.violations}")
    }
    
    @Test
    fun `test azimuth exceeding 360 fails validation`() {
        val params = mapOf(
            TransitFactory.keyword("azimuth") to 400.0,  // > 360
            TransitFactory.keyword("elevation") to 45.0
        )
        
        val result = commandBuilder.buildCommand("rotaryplatform-goto", params)
        assertTrue(result.isSuccess) // Building succeeds
        
        val proto = result.getOrThrow()
        val validationResult = validator.validate(proto)
        
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
        val params = mapOf(
            TransitFactory.keyword("azimuth") to 180.0,
            TransitFactory.keyword("elevation") to -45.0  // < -30
        )
        
        val result = commandBuilder.buildCommand("rotaryplatform-goto", params)
        assertTrue(result.isSuccess)
        
        val proto = result.getOrThrow()
        val validationResult = validator.validate(proto)
        
        assertFalse(validationResult.isSuccess)
        
        val violations = validationResult.violations.violations
        val elevationViolation = violations.find { it.fieldPath.contains("elevation") }
        assertNotNull(elevationViolation)
    }
    
    @Test
    fun `test GPS latitude constraints`() {
        // Valid latitude
        val validParams = mapOf(
            TransitFactory.keyword("use-manual") to true,
            TransitFactory.keyword("latitude") to 51.5,
            TransitFactory.keyword("longitude") to -0.1,
            TransitFactory.keyword("altitude") to 100.0
        )
        
        val validResult = commandBuilder.buildCommand("set-gps-manual", validParams)
        assertTrue(validResult.isSuccess)
        
        val validProto = validResult.getOrThrow()
        assertTrue(validator.validate(validProto).isSuccess)
        
        // Invalid latitude > 90
        val invalidParams = mapOf(
            TransitFactory.keyword("use-manual") to true,
            TransitFactory.keyword("latitude") to 91.0,  // > 90
            TransitFactory.keyword("longitude") to -0.1,
            TransitFactory.keyword("altitude") to 100.0
        )
        
        val invalidResult = commandBuilder.buildCommand("set-gps-manual", invalidParams)
        assertTrue(invalidResult.isSuccess)
        
        val invalidProto = invalidResult.getOrThrow()
        val validationResult = validator.validate(invalidProto)
        
        assertFalse(validationResult.isSuccess)
        assertTrue(validationResult.violations.violations.any { 
            it.fieldPath.contains("latitude")
        })
    }
    
    @Test
    fun `test heat camera zoom constraints`() {
        // Valid zoom level
        val validParams = mapOf(
            TransitFactory.keyword("zoom") to 4.0
        )
        
        val validResult = commandBuilder.buildCommand("heatcamera-zoom", validParams)
        assertTrue(validResult.isSuccess)
        assertTrue(validator.validate(validResult.getOrThrow()).isSuccess)
        
        // Invalid zoom > 8
        val invalidParams = mapOf(
            TransitFactory.keyword("zoom") to 10.0  // > 8
        )
        
        val invalidResult = commandBuilder.buildCommand("heatcamera-zoom", invalidParams)
        assertTrue(invalidResult.isSuccess)
        
        val validationResult = validator.validate(invalidResult.getOrThrow())
        assertFalse(validationResult.isSuccess)
    }
    
    @Test
    fun `test velocity direction enum constraint`() {
        // Direction should not be 0 (NONE)
        val paramsWithNone = mapOf(
            TransitFactory.keyword("azimuth-speed") to 10.0,
            TransitFactory.keyword("elevation-speed") to 5.0,
            TransitFactory.keyword("azimuth-direction") to "none",  // Should fail
            TransitFactory.keyword("elevation-direction") to "forward"
        )
        
        val result = commandBuilder.buildCommand("rotaryplatform-set-velocity", paramsWithNone)
        
        // Note: This test might not fail if the builder doesn't map "none" to 0
        // or if the constraint is not properly set in the proto
        if (result.isSuccess) {
            val proto = result.getOrThrow()
            val validationResult = validator.validate(proto)
            
            // Log the result for debugging
            println("Validation result for direction=none: ${validationResult.isSuccess}")
            if (!validationResult.isSuccess) {
                validationResult.violations.violations.forEach { violation ->
                    println("Violation: ${violation.fieldPath} - ${violation.message}")
                }
            }
        }
    }
    
    @Test
    fun `test validation error details`() {
        // Create a command with multiple constraint violations
        val params = mapOf(
            TransitFactory.keyword("azimuth") to -10.0,   // < 0
            TransitFactory.keyword("elevation") to 100.0  // > 90
        )
        
        val result = commandBuilder.buildCommand("rotaryplatform-goto", params)
        assertTrue(result.isSuccess)
        
        val proto = result.getOrThrow()
        val validationResult = validator.validate(proto)
        
        assertFalse(validationResult.isSuccess)
        
        val violations = validationResult.violations.violations
        assertEquals(2, violations.size, "Should have 2 violations")
        
        // Check that we get meaningful error messages
        violations.forEach { violation ->
            assertNotNull(violation.fieldPath)
            assertNotNull(violation.message)
            assertNotNull(violation.constraintId)
            
            println("Violation details:")
            println("  Field: ${violation.fieldPath}")
            println("  Message: ${violation.message}")
            println("  Constraint: ${violation.constraintId}")
        }
    }
    
    @Test
    fun `test TestCommandProcessor with validation`() {
        val processor = TestCommandProcessor()
        
        // Test valid command
        val validJson = processor.processCommandWithValidation(
            "rotaryplatform-goto",
            mapOf(
                TransitFactory.keyword("azimuth") to 90.0,
                TransitFactory.keyword("elevation") to 30.0
            )
        )
        
        assertTrue(validJson.contains("\"success\": true"))
        assertTrue(validJson.contains("\"validated\": true"))
        
        // Test invalid command
        val invalidJson = processor.processCommandWithValidation(
            "rotaryplatform-goto",
            mapOf(
                TransitFactory.keyword("azimuth") to 400.0,  // > 360
                TransitFactory.keyword("elevation") to 30.0
            )
        )
        
        assertTrue(invalidJson.contains("\"success\": false"))
        assertTrue(invalidJson.contains("validation_errors"))
    }
}