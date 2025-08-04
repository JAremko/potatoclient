package potatoclient.kotlin.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import potatoclient.kotlin.MouseEventHandler
import potatoclient.kotlin.VideoStreamEventCallback
import potatoclient.kotlin.gestures.StreamType
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.JPanel

class VideoStreamCommandIntegrationTest {
    
    private lateinit var mockComponent: Component
    private lateinit var capturedCommands: MutableList<Map<String, Any>>
    private lateinit var capturedEvents: MutableList<Map<String, Any>>
    private lateinit var mouseHandler: MouseEventHandler
    
    private val mockCallback = object : VideoStreamEventCallback {
        override fun sendCommand(command: Map<String, Any>) {
            capturedCommands.add(command)
        }
        
        override fun sendEvent(event: Map<String, Any>) {
            capturedEvents.add(event)
        }
        
        override fun sendRequest(request: Map<String, Any>) {
            // Not used in new architecture
            fail("sendRequest should not be called - use sendCommand instead")
        }
    }
    
    @BeforeEach
    fun setup() {
        mockComponent = JPanel().apply {
            setSize(800, 600)
        }
        capturedCommands = mutableListOf()
        capturedEvents = mutableListOf()
        mouseHandler = MouseEventHandler(mockComponent, mockCallback, StreamType.HEAT)
    }
    
    @Test
    fun `single click generates tap gesture and goto command with keywords`() {
        // Simulate mouse click
        val downEvent = MouseEvent(
            mockComponent, MouseEvent.MOUSE_PRESSED, 
            System.currentTimeMillis(), 0,
            400, 300, 1, false, MouseEvent.BUTTON1
        )
        val upEvent = MouseEvent(
            mockComponent, MouseEvent.MOUSE_RELEASED,
            System.currentTimeMillis() + 50, 0,
            400, 300, 1, false, MouseEvent.BUTTON1
        )
        
        mockComponent.dispatchEvent(downEvent)
        mockComponent.dispatchEvent(upEvent)
        
        // Verify command was sent
        assertEquals(1, capturedCommands.size)
        val command = capturedCommands[0]
        
        // Check structure
        assertTrue(command.containsKey("rotary"))
        val rotary = command["rotary"] as Map<*, *>
        assertTrue(rotary.containsKey("goto-ndc"))
        
        val gotoNdc = rotary["goto-ndc"] as Map<*, *>
        assertEquals("heat", gotoNdc["channel"])  // keyword!
        assertEquals(0.0, gotoNdc["x"] as Double, 0.01)  // center
        assertEquals(0.0, gotoNdc["y"] as Double, 0.01)  // center
        
        // Verify event was sent
        assertTrue(capturedEvents.any { event ->
            val gestureType = event["gesture-type"] as? String
            gestureType == "tap"
        })
    }
    
    @Test
    fun `double click generates cv tracking command with keywords`() {
        val clickX = 200
        val clickY = 150
        val clickTime = System.currentTimeMillis()
        
        // First click
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_PRESSED,
            clickTime, 0, clickX, clickY, 1, false, MouseEvent.BUTTON1
        ))
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_RELEASED,
            clickTime + 50, 0, clickX, clickY, 1, false, MouseEvent.BUTTON1
        ))
        
        // Second click (double-click)
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_PRESSED,
            clickTime + 200, 0, clickX, clickY, 1, false, MouseEvent.BUTTON1
        ))
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_RELEASED,
            clickTime + 250, 0, clickX, clickY, 1, false, MouseEvent.BUTTON1
        ))
        
        // Should have both tap and double-tap commands
        assertTrue(capturedCommands.size >= 1)
        
        // Find the CV command
        val cvCommand = capturedCommands.find { it.containsKey("cv") }
        assertNotNull(cvCommand)
        
        val cv = cvCommand!!["cv"] as Map<*, *>
        val startTrack = cv["start-track-ndc"] as Map<*, *>
        
        assertEquals("heat", startTrack["channel"])  // keyword!
        assertEquals(-0.5, startTrack["x"] as Double, 0.01)
        assertEquals(0.5, startTrack["y"] as Double, 0.01)
    }
    
    @Test
    fun `mouse wheel generates zoom commands for correct camera`() {
        // Test heat camera zoom in
        var handler = MouseEventHandler(mockComponent, mockCallback, StreamType.HEAT)
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_WHEEL,
            System.currentTimeMillis(), 0,
            400, 300, 0, false, MouseEvent.NOBUTTON,
            0, false, -1  // negative = zoom in
        ))
        
        assertEquals(1, capturedCommands.size)
        assertTrue(capturedCommands[0].containsKey("heat-camera"))
        val heatCmd = capturedCommands[0]["heat-camera"] as Map<*, *>
        assertTrue(heatCmd.containsKey("next-zoom-table-pos"))
        
        // Test day camera zoom out
        capturedCommands.clear()
        handler.cleanup()
        handler = MouseEventHandler(mockComponent, mockCallback, StreamType.DAY)
        
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_WHEEL,
            System.currentTimeMillis(), 0,
            400, 300, 0, false, MouseEvent.NOBUTTON,
            0, false, 1  // positive = zoom out
        ))
        
        assertEquals(1, capturedCommands.size)
        assertTrue(capturedCommands[0].containsKey("day-camera"))
        val dayCmd = capturedCommands[0]["day-camera"] as Map<*, *>
        assertTrue(dayCmd.containsKey("prev-zoom-table-pos"))
        
        handler.cleanup()
    }
    
    @Test
    fun `pan gesture generates velocity commands with keyword directions`() {
        // Start pan
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(), 0,
            400, 300, 1, false, MouseEvent.BUTTON1
        ))
        
        // Drag right and up
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_DRAGGED,
            System.currentTimeMillis() + 100, 0,
            440, 260, 1, false, MouseEvent.BUTTON1
        ))
        
        // Should have velocity command
        val velocityCmd = capturedCommands.find { cmd ->
            val rotary = cmd["rotary"] as? Map<*, *>
            rotary?.containsKey("set-velocity") == true
        }
        assertNotNull(velocityCmd)
        
        val velocity = (velocityCmd!!["rotary"] as Map<*, *>)["set-velocity"] as Map<*, *>
        
        // Check directions are keywords
        assertEquals("clockwise", velocity["azimuth-direction"])
        assertEquals("clockwise", velocity["elevation-direction"])
        assertTrue(velocity["azimuth-speed"] as Double > 0.0)
        assertTrue(velocity["elevation-speed"] as Double > 0.0)
        
        // Release to stop pan
        mockComponent.dispatchEvent(MouseEvent(
            mockComponent, MouseEvent.MOUSE_RELEASED,
            System.currentTimeMillis() + 200, 0,
            440, 260, 1, false, MouseEvent.BUTTON1
        ))
        
        // Should have halt command
        val haltCmd = capturedCommands.find { cmd ->
            val rotary = cmd["rotary"] as? Map<*, *>
            rotary?.containsKey("halt") == true
        }
        assertNotNull(haltCmd)
    }
    
    @Test
    fun `all commands use kebab-case keys`() {
        // Generate various commands
        val events = listOf(
            // Click for goto
            MouseEvent(mockComponent, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 100, 100, 1, false, MouseEvent.BUTTON1),
            // Wheel for zoom
            MouseEvent(mockComponent, MouseEvent.MOUSE_WHEEL,
                System.currentTimeMillis(), 0, 200, 200, 0, false, 
                MouseEvent.NOBUTTON, 0, false, -1)
        )
        
        events.forEach { mockComponent.dispatchEvent(it) }
        
        // Check all captured commands
        capturedCommands.forEach { command ->
            checkKeysAreKebabCase(command)
        }
    }
    
    private fun checkKeysAreKebabCase(map: Map<*, *>) {
        map.forEach { (key, value) ->
            if (key is String) {
                assertFalse(key.contains('_'), "Key '$key' contains underscore")
                assertFalse(key.matches(Regex(".*[A-Z].*")), "Key '$key' contains uppercase")
            }
            if (value is Map<*, *>) {
                checkKeysAreKebabCase(value)
            }
        }
    }
}