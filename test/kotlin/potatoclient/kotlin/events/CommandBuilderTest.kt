package potatoclient.kotlin.events

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import potatoclient.kotlin.gestures.StreamType
import potatoclient.kotlin.gestures.RotaryDirection

class CommandBuilderTest {
    
    @Test
    fun `rotaryGotoNDC creates correct nested structure with keywords`() {
        val command = CommandBuilder.rotaryGotoNDC(StreamType.HEAT, 0.5, -0.3)
        
        assertEquals(1, command.size)
        assertTrue(command.containsKey("rotary"))
        
        val rotary = command["rotary"] as Map<*, *>
        assertTrue(rotary.containsKey("goto-ndc"))
        
        val gotoNdc = rotary["goto-ndc"] as Map<*, *>
        assertEquals("heat", gotoNdc["channel"])  // keyword, not "HEAT"
        assertEquals(0.5, gotoNdc["x"])
        assertEquals(-0.3, gotoNdc["y"])
    }
    
    @Test
    fun `cvStartTrackNDC creates correct structure with keywords`() {
        val command = CommandBuilder.cvStartTrackNDC(StreamType.DAY, -0.8, 0.2, 12345L)
        
        val cv = command["cv"] as Map<*, *>
        val startTrack = cv["start-track-ndc"] as Map<*, *>
        
        assertEquals("day", startTrack["channel"])  // keyword, not "DAY"
        assertEquals(-0.8, startTrack["x"])
        assertEquals(0.2, startTrack["y"])
        assertEquals(12345L, startTrack["frame-time"])
    }
    
    @Test
    fun `cvStartTrackNDC without frame time omits the field`() {
        val command = CommandBuilder.cvStartTrackNDC(StreamType.HEAT, 0.0, 0.0, null)
        
        val cv = command["cv"] as Map<*, *>
        val startTrack = cv["start-track-ndc"] as Map<*, *>
        
        assertFalse(startTrack.containsKey("frame-time"))
    }
    
    @Test
    fun `rotarySetVelocity uses keyword directions`() {
        val command = CommandBuilder.rotarySetVelocity(
            0.5, 0.25,
            RotaryDirection.CLOCKWISE,
            RotaryDirection.COUNTER_CLOCKWISE
        )
        
        val rotary = command["rotary"] as Map<*, *>
        val velocity = rotary["set-velocity"] as Map<*, *>
        
        assertEquals(0.5, velocity["azimuth-speed"])
        assertEquals(0.25, velocity["elevation-speed"])
        assertEquals("clockwise", velocity["azimuth-direction"])  // keyword
        assertEquals("counter-clockwise", velocity["elevation-direction"])  // keyword
    }
    
    @Test
    fun `rotaryHalt creates minimal command`() {
        val command = CommandBuilder.rotaryHalt()
        
        val rotary = command["rotary"] as Map<*, *>
        val halt = rotary["halt"] as Map<*, *>
        
        assertTrue(halt.isEmpty())
    }
    
    @Test
    fun `camera zoom commands use correct structure`() {
        val heatZoomIn = CommandBuilder.heatCameraNextZoom()
        val heatZoomOut = CommandBuilder.heatCameraPrevZoom()
        val dayZoomIn = CommandBuilder.dayCameraNextZoom()
        val dayZoomOut = CommandBuilder.dayCameraPrevZoom()
        
        // Heat camera
        assertTrue(heatZoomIn.containsKey("heat-camera"))
        val heatIn = heatZoomIn["heat-camera"] as Map<*, *>
        assertTrue(heatIn.containsKey("next-zoom-table-pos"))
        
        val heatOut = heatZoomOut["heat-camera"] as Map<*, *>
        assertTrue(heatOut.containsKey("prev-zoom-table-pos"))
        
        // Day camera
        assertTrue(dayZoomIn.containsKey("day-camera"))
        val dayIn = dayZoomIn["day-camera"] as Map<*, *>
        assertTrue(dayIn.containsKey("next-zoom-table-pos"))
        
        val dayOut = dayZoomOut["day-camera"] as Map<*, *>
        assertTrue(dayOut.containsKey("prev-zoom-table-pos"))
    }
    
    @Test
    fun `enum to keyword conversions are correct`() {
        // Stream types
        assertEquals("heat", StreamType.HEAT.toKeyword())
        assertEquals("day", StreamType.DAY.toKeyword())
        
        // Rotary directions
        assertEquals("clockwise", RotaryDirection.CLOCKWISE.toKeyword())
        assertEquals("counter-clockwise", RotaryDirection.COUNTER_CLOCKWISE.toKeyword())
    }
    
    @Test
    fun `command keys use kebab-case not camelCase`() {
        val command = CommandBuilder.rotaryGotoNDC(StreamType.HEAT, 0.0, 0.0)
        
        // Check all keys are kebab-case
        val rotary = command["rotary"] as Map<*, *>
        assertTrue(rotary.containsKey("goto-ndc"))  // not "gotoNdc"
        
        val velocity = CommandBuilder.rotarySetVelocity(
            0.1, 0.1,
            RotaryDirection.CLOCKWISE,
            RotaryDirection.CLOCKWISE
        )
        val velMap = (velocity["rotary"] as Map<*, *>)["set-velocity"] as Map<*, *>
        assertTrue(velMap.containsKey("azimuth-speed"))  // not "azimuthSpeed"
        assertTrue(velMap.containsKey("elevation-speed"))  // not "elevationSpeed"
        assertTrue(velMap.containsKey("azimuth-direction"))  // not "azimuthDirection"
        assertTrue(velMap.containsKey("elevation-direction"))  // not "elevationDirection"
    }
}