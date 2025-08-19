package potatoclient.kotlin.ipc

import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * Tests for MessageBuilders.
 */
class MessageBuildersTest {
    
    // Gesture tests removed - gestures no longer supported
    
    @Test
    fun testWindowEventWithAllParams() {
        val message = MessageBuilders.windowEvent(
            IpcKeys.RESIZE,
            width = 1920,
            height = 1080,
            x = 100,
            y = 200,
            deltaX = 10,
            deltaY = 20
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.WINDOW, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.RESIZE, message[IpcKeys.ACTION])
        assertEquals(1920, message[IpcKeys.WIDTH])
        assertEquals(1080, message[IpcKeys.HEIGHT])
        assertEquals(100, message[IpcKeys.X])
        assertEquals(200, message[IpcKeys.Y])
        assertEquals(10, message[IpcKeys.DELTA_X])
        assertEquals(20, message[IpcKeys.DELTA_Y])
    }
    
    @Test
    fun testWindowEventWithPartialParams() {
        val message = MessageBuilders.windowEvent(
            IpcKeys.MINIMIZE
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.WINDOW, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.MINIMIZE, message[IpcKeys.ACTION])
        
        // Optional params should not be present
        assertFalse(message.containsKey(IpcKeys.WIDTH))
        assertFalse(message.containsKey(IpcKeys.HEIGHT))
        assertFalse(message.containsKey(IpcKeys.X))
        assertFalse(message.containsKey(IpcKeys.Y))
        assertFalse(message.containsKey(IpcKeys.DELTA_X))
        assertFalse(message.containsKey(IpcKeys.DELTA_Y))
    }
    
    @Test
    fun testConnectionEvent() {
        val details = mapOf<Any, Any>("reason" to "timeout", "retry_count" to 3)
        val message = MessageBuilders.connectionEvent(
            IpcKeys.RECONNECTING,
            details
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.CONNECTION, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.RECONNECTING, message[IpcKeys.ACTION])
        assertEquals(details, message[IpcKeys.DETAILS])
    }
    
    @Test
    fun testConnectionEventWithoutDetails() {
        val message = MessageBuilders.connectionEvent(IpcKeys.CONNECTED)
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.CONNECTION, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.CONNECTED, message[IpcKeys.ACTION])
        assertFalse(message.containsKey(IpcKeys.DETAILS))
    }
    
    @Test
    fun testCommand() {
        val params = mapOf<Any, Any>(
            IpcKeys.NDC_X to 0.5,
            IpcKeys.NDC_Y to -0.5
        )
        
        val message = MessageBuilders.command(
            IpcKeys.ROTARY_GOTO_NDC,
            IpcKeys.DAY,
            params
        )
        
        assertEquals(IpcKeys.COMMAND, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_GOTO_NDC, message[IpcKeys.ACTION])
        assertEquals(IpcKeys.DAY, message[IpcKeys.STREAM_TYPE])
        assertEquals(0.5, message[IpcKeys.NDC_X])
        assertEquals(-0.5, message[IpcKeys.NDC_Y])
    }
    
    @Test
    fun testRotaryGotoNdc() {
        val message = MessageBuilders.rotaryGotoNdc(
            IpcKeys.HEAT,
            0.75,
            -0.25
        )
        
        assertEquals(IpcKeys.COMMAND, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_GOTO_NDC, message[IpcKeys.ACTION])
        assertEquals(IpcKeys.HEAT, message[IpcKeys.STREAM_TYPE])
        assertEquals(0.75, message[IpcKeys.NDC_X])
        assertEquals(-0.25, message[IpcKeys.NDC_Y])
    }
    
    @Test
    fun testCvStartTrackNdc() {
        val message = MessageBuilders.cvStartTrackNdc(
            IpcKeys.DAY,
            0.1,
            0.2
        )
        
        assertEquals(IpcKeys.COMMAND, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.CV_START_TRACK_NDC, message[IpcKeys.ACTION])
        assertEquals(IpcKeys.DAY, message[IpcKeys.STREAM_TYPE])
        assertEquals(0.1, message[IpcKeys.NDC_X])
        assertEquals(0.2, message[IpcKeys.NDC_Y])
    }
    
    @Test
    fun testRotarySetVelocity() {
        val message = MessageBuilders.rotarySetVelocity(
            IpcKeys.HEAT,
            1.5,
            0.5,
            IpcKeys.CLOCKWISE,
            IpcKeys.COUNTER_CLOCKWISE
        )
        
        assertEquals(IpcKeys.COMMAND, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_SET_VELOCITY, message[IpcKeys.ACTION])
        assertEquals(IpcKeys.HEAT, message[IpcKeys.STREAM_TYPE])
        assertEquals(1.5, message[IpcKeys.AZIMUTH_SPEED])
        assertEquals(0.5, message[IpcKeys.ELEVATION_SPEED])
        assertEquals(IpcKeys.CLOCKWISE, message[IpcKeys.AZIMUTH_DIRECTION])
        assertEquals(IpcKeys.COUNTER_CLOCKWISE, message[IpcKeys.ELEVATION_DIRECTION])
    }
    
    @Test
    fun testRotaryHalt() {
        val message = MessageBuilders.rotaryHalt(IpcKeys.DAY)
        
        assertEquals(IpcKeys.COMMAND, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_HALT, message[IpcKeys.ACTION])
        assertEquals(IpcKeys.DAY, message[IpcKeys.STREAM_TYPE])
    }
    
    @Test
    fun testLogMessage() {
        val data = mapOf<Any, Any>("file" to "test.kt", "line" to 42)
        val message = MessageBuilders.log(
            IpcKeys.DEBUG,
            "Test debug message",
            "test-process",
            data
        )
        
        assertEquals(IpcKeys.LOG, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.DEBUG, message[IpcKeys.LEVEL])
        assertEquals("Test debug message", message[IpcKeys.MESSAGE])
        assertEquals("test-process", message[IpcKeys.PROCESS])
        assertEquals(data, message[IpcKeys.DATA])
    }
    
    @Test
    fun testLogMessageWithoutData() {
        val message = MessageBuilders.log(
            IpcKeys.INFO,
            "Simple log",
            "my-process"
        )
        
        assertEquals(IpcKeys.LOG, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.INFO, message[IpcKeys.LEVEL])
        assertEquals("Simple log", message[IpcKeys.MESSAGE])
        assertEquals("my-process", message[IpcKeys.PROCESS])
        assertFalse(message.containsKey(IpcKeys.DATA))
    }
    
    @Test
    fun testMetric() {
        val tags = mapOf("host" to "server1", "region" to "us-east")
        val message = MessageBuilders.metric(
            "cpu.usage",
            75.5,
            tags
        )
        
        assertEquals(IpcKeys.METRIC, message[IpcKeys.MSG_TYPE])
        assertNotNull(message[IpcKeys.MSG_ID])
        assertNotNull(message[IpcKeys.TIMESTAMP])
        
        val payload = message[IpcKeys.PAYLOAD] as Map<*, *>
        assertEquals("cpu.usage", payload["name"])
        assertEquals(75.5, payload["value"])
        assertEquals(tags, payload["tags"])
    }
    
    @Test
    fun testMetricWithoutTags() {
        val message = MessageBuilders.metric(
            "memory.used",
            1024
        )
        
        assertEquals(IpcKeys.METRIC, message[IpcKeys.MSG_TYPE])
        
        val payload = message[IpcKeys.PAYLOAD] as Map<*, *>
        assertEquals("memory.used", payload["name"])
        assertEquals(1024, payload["value"])
        assertFalse(payload.containsKey("tags"))
    }
    
    @Test
    fun testMessageIdUniqueness() {
        val message1 = MessageBuilders.log(IpcKeys.INFO, "msg1", "proc")
        val message2 = MessageBuilders.log(IpcKeys.INFO, "msg2", "proc")
        
        assertNotEquals(message1[IpcKeys.MSG_ID], message2[IpcKeys.MSG_ID])
    }
    
    @Test
    fun testTimestampPresence() {
        // Use windowEvent instead of removed gestureEvent
        val message = MessageBuilders.windowEvent(
            IpcKeys.RESIZE
        )
        
        val timestamp = message[IpcKeys.TIMESTAMP] as Long
        assertTrue(timestamp > 0)
        
        // Should be recent (within last minute)
        val now = System.currentTimeMillis()
        assertTrue(timestamp <= now)
        assertTrue(timestamp > now - 60000)
    }
}