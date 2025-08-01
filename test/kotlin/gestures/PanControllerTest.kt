package potatoclient.kotlin.gestures

import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

class PanControllerTest {
    
    private class CommandCapture {
        data class RotaryCommand(
            val azSpeed: Double,
            val elSpeed: Double,
            val azDir: RotaryDirection,
            val elDir: RotaryDirection
        )
        
        val rotaryCommands = mutableListOf<RotaryCommand>()
        val haltCount = AtomicInteger(0)
        
        fun onRotaryCommand(azSpeed: Double, elSpeed: Double, azDir: RotaryDirection, elDir: RotaryDirection) {
            rotaryCommands.add(RotaryCommand(azSpeed, elSpeed, azDir, elDir))
        }
        
        fun onHaltCommand() {
            haltCount.incrementAndGet()
        }
        
        fun reset() {
            rotaryCommands.clear()
            haltCount.set(0)
        }
    }
    
    @Test
    fun testPanStartStop() {
        val capture = CommandCapture()
        val controller = PanController(
            onRotaryCommand = capture::onRotaryCommand,
            onHaltCommand = capture::onHaltCommand,
            streamType = StreamType.HEAT
        )
        
        try {
            // Start pan
            controller.startPan()
            
            // Should start sending commands
            Thread.sleep(150)
            assertTrue("Should have sent rotary commands", capture.rotaryCommands.isNotEmpty())
            
            // Stop pan
            controller.stopPan()
            Thread.sleep(50)
            
            // Should have sent halt
            assertTrue("Should have sent halt command", capture.haltCount.get() > 0)
        } finally {
            controller.shutdown()
        }
    }
    
    @Test
    fun testDeadZone() {
        val capture = CommandCapture()
        val controller = PanController(
            onRotaryCommand = capture::onRotaryCommand,
            onHaltCommand = capture::onHaltCommand,
            streamType = StreamType.HEAT
        )
        
        try {
            controller.startPan()
            
            // Update with values in dead zone
            controller.updatePan(0.01, 0.01, 0)
            
            // Wait for update interval
            Thread.sleep(150)
            
            // Should send halt commands for dead zone
            assertTrue("Should halt in dead zone", capture.haltCount.get() > 0)
            assertEquals("Should not send rotary commands in dead zone", 0, capture.rotaryCommands.size)
        } finally {
            controller.shutdown()
        }
    }
    
    @Test
    fun testSpeedCalculation() {
        val capture = CommandCapture()
        val controller = PanController(
            onRotaryCommand = capture::onRotaryCommand,
            onHaltCommand = capture::onHaltCommand,
            streamType = StreamType.HEAT
        )
        
        try {
            controller.startPan()
            
            // Update with moderate movement
            controller.updatePan(0.2, -0.1, 0)
            
            // Wait for command
            Thread.sleep(150)
            
            assertTrue("Should have sent commands", capture.rotaryCommands.isNotEmpty())
            
            val cmd = capture.rotaryCommands.last()
            
            // Check speed is within expected range
            assertTrue("Azimuth speed should be positive", cmd.azSpeed > 0)
            assertTrue("Elevation speed should be positive", cmd.elSpeed > 0)
            assertTrue("Azimuth speed should be reasonable", cmd.azSpeed <= 1.0)
            assertTrue("Elevation speed should be reasonable", cmd.elSpeed <= 1.0)
            
            // Check directions
            assertEquals("Azimuth should be clockwise for positive delta", 
                        RotaryDirection.CLOCKWISE, cmd.azDir)
            assertEquals("Elevation should be counter-clockwise for negative delta", 
                        RotaryDirection.COUNTER_CLOCKWISE, cmd.elDir)
        } finally {
            controller.shutdown()
        }
    }
    
    @Test
    fun testZoomLevelAffectsSpeed() {
        val capture = CommandCapture()
        val controller = PanController(
            onRotaryCommand = capture::onRotaryCommand,
            onHaltCommand = capture::onHaltCommand,
            streamType = StreamType.HEAT
        )
        
        try {
            controller.startPan()
            
            // Test zoom level 0
            capture.reset()
            controller.updatePan(0.3, 0.0, 0)
            Thread.sleep(150)
            
            val speedZoom0 = capture.rotaryCommands.lastOrNull()?.azSpeed ?: 0.0
            
            // Test zoom level 3
            capture.reset()
            controller.updatePan(0.3, 0.0, 3)
            Thread.sleep(150)
            
            val speedZoom3 = capture.rotaryCommands.lastOrNull()?.azSpeed ?: 0.0
            
            // Higher zoom should have higher max speed
            assertTrue("Zoom 3 should have higher speed than zoom 0", 
                      speedZoom3 > speedZoom0)
        } finally {
            controller.shutdown()
        }
    }
    
    @Test
    fun testPeriodicUpdates() {
        val capture = CommandCapture()
        val latch = CountDownLatch(3) // Wait for 3 updates
        
        val controller = PanController(
            onRotaryCommand = { az, el, azDir, elDir ->
                capture.onRotaryCommand(az, el, azDir, elDir)
                latch.countDown()
            },
            onHaltCommand = capture::onHaltCommand,
            streamType = StreamType.DAY
        )
        
        try {
            controller.startPan()
            controller.updatePan(0.2, 0.2, 0)
            
            // Should receive periodic updates
            assertTrue("Should receive multiple updates", 
                      latch.await(500, TimeUnit.MILLISECONDS))
            
            // Verify consistent updates
            assertTrue("Should have multiple commands", capture.rotaryCommands.size >= 3)
            
            // All commands should be similar since input didn't change
            val speeds = capture.rotaryCommands.map { it.azSpeed }
            val variance = speeds.maxOrNull()!! - speeds.minOrNull()!!
            assertTrue("Speeds should be consistent", variance < 0.01)
        } finally {
            controller.shutdown()
        }
    }
    
    @Test
    fun testStreamTypeConfiguration() {
        val captureHeat = CommandCapture()
        val captureDay = CommandCapture()
        
        val controllerHeat = PanController(
            onRotaryCommand = captureHeat::onRotaryCommand,
            onHaltCommand = captureHeat::onHaltCommand,
            streamType = StreamType.HEAT
        )
        
        val controllerDay = PanController(
            onRotaryCommand = captureDay::onRotaryCommand,
            onHaltCommand = captureDay::onHaltCommand,
            streamType = StreamType.DAY
        )
        
        try {
            // Test with same input, zoom 0
            controllerHeat.startPan()
            controllerHeat.updatePan(0.3, 0.0, 0)
            
            controllerDay.startPan()
            controllerDay.updatePan(0.3, 0.0, 0)
            
            Thread.sleep(150)
            
            val heatSpeed = captureHeat.rotaryCommands.lastOrNull()?.azSpeed ?: 0.0
            val daySpeed = captureDay.rotaryCommands.lastOrNull()?.azSpeed ?: 0.0
            
            // Day camera should have lower speed at zoom 0
            assertTrue("Day camera should have lower speed than heat at zoom 0", 
                      daySpeed < heatSpeed)
        } finally {
            controllerHeat.shutdown()
            controllerDay.shutdown()
        }
    }
    
    @Test
    fun testShutdown() {
        val capture = CommandCapture()
        val controller = PanController(
            onRotaryCommand = capture::onRotaryCommand,
            onHaltCommand = capture::onHaltCommand,
            streamType = StreamType.HEAT
        )
        
        controller.startPan()
        controller.updatePan(0.2, 0.2, 0)
        
        // Shutdown should stop updates
        controller.shutdown()
        
        val countBefore = capture.rotaryCommands.size
        Thread.sleep(200)
        val countAfter = capture.rotaryCommands.size
        
        assertEquals("Should not send commands after shutdown", 
                    countBefore, countAfter)
    }
}