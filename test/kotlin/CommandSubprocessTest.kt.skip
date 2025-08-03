package potatoclient.transit

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import cmd.JonSharedCmd

class CommandSubprocessTest {

    @Test
    fun testSimpleCommandBuilderPing() {
        val builder = SimpleCommandBuilder()
        val command = builder.buildCommand(mapOf("action" to "ping"))
        
        assertNotNull(command)
        assertTrue(command.hasPing())
        assertEquals(cmd.JonSharedCmd.Ping.getDefaultInstance(), command.ping)
    }

    @Test
    fun testSimpleCommandBuilderConnect() {
        val command = SimpleCommandBuilder.buildCommand(mapOf(
            "action" to "connect",
            "url" to "wss://example.com"
        ))
        
        assertNotNull(command)
        assertTrue(command.hasConnect())
        assertEquals("wss://example.com", command.connect.url)
    }

    @Test
    fun testSimpleCommandBuilderWithInvalidAction() {
        try {
            SimpleCommandBuilder.buildCommand(mapOf("action" to "invalid-action"))
            fail("Should throw exception for invalid action")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Unknown command action") ?: false)
        }
    }

    @Test
    fun testSimpleCommandBuilderWithMissingAction() {
        try {
            SimpleCommandBuilder.buildCommand(mapOf("url" to "wss://example.com"))
            fail("Should throw exception for missing action")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Missing 'action' field") ?: false)
        }
    }

    @Test
    fun testCommandSubprocessMessageHandling() = runBlocking {
        // Create pipes for communication
        val toSubprocess = PipedOutputStream()
        val fromSubprocess = PipedOutputStream()
        val subprocessIn = PipedInputStream(toSubprocess)
        val subprocessOut = fromSubprocess
        val testIn = PipedInputStream(fromSubprocess)
        
        // Mock WebSocket channel
        val mockWebSocketChannel = Channel<ByteArray>(10)
        
        // Create communicator
        val communicator = TransitCommunicator(subprocessIn, subprocessOut)
        
        // Launch subprocess message handler in coroutine
        val job = launch {
            try {
                // Simulate reading a message
                val message = mapOf(
                    "msg-type" to "command",
                    "msg-id" to "test-123",
                    "timestamp" to System.currentTimeMillis(),
                    "payload" to mapOf("action" to "ping")
                )
                
                // Process the message
                val command = SimpleCommandBuilder.buildCommand(message["payload"] as Map<String, Any>)
                mockWebSocketChannel.send(command.toByteArray())
                
                // Send response
                communicator.sendMessage("response", mapOf(
                    "msg-id" to message["msg-id"],
                    "status" to "sent"
                ))
            } catch (e: Exception) {
                fail("Unexpected exception: ${e.message}")
            }
        }
        
        // Wait a bit for coroutine to process
        delay(100)
        
        // Verify command was sent to WebSocket
        val sentData = mockWebSocketChannel.tryReceive().getOrNull()
        assertNotNull("Command should be sent to WebSocket", sentData)
        
        // Parse the sent protobuf
        val sentCommand = JonSharedCmd.Root.parseFrom(sentData)
        assertTrue("Should be ping command", sentCommand.hasPing())
        
        job.cancel()
    }

    @Test
    fun testRotaryCommands() {
        // Test azimuth rotation
        val azimuthCommand = SimpleCommandBuilder.buildCommand(mapOf(
            "action" to "rotary-azimuth-rotate",
            "angle" to 45.0,
            "direction" to "clockwise"
        ))
        
        assertTrue(azimuthCommand.hasRotaryPlatform())
        assertTrue(azimuthCommand.rotaryPlatform.hasAzimuth())
        assertTrue(azimuthCommand.rotaryPlatform.azimuth.hasRotate())
        assertEquals(45.0, azimuthCommand.rotaryPlatform.azimuth.rotate.angle, 0.01)
        
        // Test scan start
        val scanCommand = SimpleCommandBuilder.buildCommand(mapOf(
            "action" to "rotary-scan-start"
        ))
        
        assertTrue(scanCommand.hasRotaryPlatform())
        assertTrue(scanCommand.rotaryPlatform.hasScan())
        assertTrue(scanCommand.rotaryPlatform.scan.hasStart())
    }

    @Test
    fun testDayCameraCommands() {
        // Test zoom
        val zoomCommand = SimpleCommandBuilder.buildCommand(mapOf(
            "action" to "day-camera-zoom-in"
        ))
        
        assertTrue(zoomCommand.hasDayCamera())
        assertTrue(zoomCommand.dayCamera.hasZoom())
        assertTrue(zoomCommand.dayCamera.zoom.hasDirection())
        assertEquals(
            cmd.DayCamera.JonSharedCmdDayCamera.Direction.DIRECTION_IN,
            zoomCommand.dayCamera.zoom.direction
        )
        
        // Test power
        val powerCommand = SimpleCommandBuilder.buildCommand(mapOf(
            "action" to "day-camera-power-on"
        ))
        
        assertTrue(powerCommand.hasDayCamera())
        assertTrue(powerCommand.dayCamera.hasPower())
        assertTrue(powerCommand.dayCamera.power.hasOn())
    }

    @Test
    fun testComplexRotaryCommand() {
        val command = SimpleCommandBuilder.buildCommand(mapOf(
            "action" to "rotary-rotate-both-to",
            "azimuthAngle" to 90.0,
            "azimuthSpeed" to 5.0,
            "azimuthDirection" to "counterclockwise",
            "elevationAngle" to 45.0,
            "elevationSpeed" to 3.0
        ))
        
        assertTrue(command.hasRotaryPlatform())
        val rotary = command.rotaryPlatform
        
        // Check azimuth
        assertTrue(rotary.hasAzimuth())
        assertTrue(rotary.azimuth.hasRotateTo())
        assertEquals(90.0, rotary.azimuth.rotateTo.angle, 0.01)
        assertEquals(5.0, rotary.azimuth.rotateTo.speed, 0.01)
        
        // Check elevation
        assertTrue(rotary.hasElevation())
        assertTrue(rotary.elevation.hasRotateTo())
        assertEquals(45.0, rotary.elevation.rotateTo.angle, 0.01)
        assertEquals(3.0, rotary.elevation.rotateTo.speed, 0.01)
    }
}