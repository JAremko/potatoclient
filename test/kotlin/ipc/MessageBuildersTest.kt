package potatoclient.kotlin.ipc

import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

/**
 * Tests for MessageBuilders focusing on gesture and window events.
 */
class MessageBuildersTest {
    
    @Test
    fun testGestureEventTap() {
        val frameTimestamp = System.currentTimeMillis()
        val message = MessageBuilders.gestureEvent(
            IpcKeys.TAP,
            IpcKeys.HEAT,
            x = 100,
            y = 200,
            frameTimestamp = frameTimestamp
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.TAP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(IpcKeys.HEAT, message[IpcKeys.STREAM_TYPE])
        assertEquals(100, message[IpcKeys.X])
        assertEquals(200, message[IpcKeys.Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
        assertNotNull(message[IpcKeys.MSG_ID])
        assertNotNull(message[IpcKeys.TIMESTAMP])
    }
    
    @Test
    fun testGestureEventDoubleTap() {
        val frameTimestamp = System.currentTimeMillis()
        val message = MessageBuilders.gestureEvent(
            IpcKeys.DOUBLE_TAP,
            IpcKeys.DAY,
            x = 150,
            y = 250,
            frameTimestamp = frameTimestamp
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.DOUBLE_TAP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(IpcKeys.DAY, message[IpcKeys.STREAM_TYPE])
        assertEquals(150, message[IpcKeys.X])
        assertEquals(250, message[IpcKeys.Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testGestureEventPanMove() {
        val frameTimestamp = System.currentTimeMillis()
        val message = MessageBuilders.gestureEvent(
            IpcKeys.PAN_MOVE,
            IpcKeys.HEAT,
            x = 300,
            y = 400,
            frameTimestamp = frameTimestamp,
            deltaX = 20,
            deltaY = -30
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.PAN_MOVE, message[IpcKeys.GESTURE_TYPE])
        assertEquals(300, message[IpcKeys.X])
        assertEquals(400, message[IpcKeys.Y])
        assertEquals(20, message[IpcKeys.DELTA_X])
        assertEquals(-30, message[IpcKeys.DELTA_Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testGestureEventWheelUp() {
        val frameTimestamp = System.currentTimeMillis()
        val message = MessageBuilders.gestureEvent(
            IpcKeys.WHEEL_UP,
            IpcKeys.DAY,
            x = 200,
            y = 300,
            frameTimestamp = frameTimestamp,
            scrollAmount = 3
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.WHEEL_UP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(200, message[IpcKeys.X])
        assertEquals(300, message[IpcKeys.Y])
        assertEquals(3, message["scroll-amount"])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testGestureEventWheelDown() {
        val frameTimestamp = System.currentTimeMillis()
        val message = MessageBuilders.gestureEvent(
            IpcKeys.WHEEL_DOWN,
            IpcKeys.HEAT,
            x = 250,
            y = 350,
            frameTimestamp = frameTimestamp,
            scrollAmount = 2
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.WHEEL_DOWN, message[IpcKeys.GESTURE_TYPE])
        assertEquals(250, message[IpcKeys.X])
        assertEquals(350, message[IpcKeys.Y])
        assertEquals(2, message["scroll-amount"])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testGestureEventWithoutOptionalParams() {
        val frameTimestamp = System.currentTimeMillis()
        val message = MessageBuilders.gestureEvent(
            IpcKeys.PAN_START,
            IpcKeys.HEAT,
            x = 100,
            y = 100,
            frameTimestamp = frameTimestamp
        )
        
        // Required fields present
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.PAN_START, message[IpcKeys.GESTURE_TYPE])
        assertEquals(100, message[IpcKeys.X])
        assertEquals(100, message[IpcKeys.Y])
        
        // Optional fields not present
        assertFalse(message.containsKey(IpcKeys.DELTA_X))
        assertFalse(message.containsKey(IpcKeys.DELTA_Y))
        assertFalse(message.containsKey("scroll-amount"))
    }
    
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
    fun testWindowMoveEvent() {
        val message = MessageBuilders.windowEvent(
            IpcKeys.WINDOW_MOVE,
            x = 500,
            y = 300,
            deltaX = 50,
            deltaY = -20
        )
        
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.WINDOW, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.WINDOW_MOVE, message[IpcKeys.ACTION])
        assertEquals(500, message[IpcKeys.X])
        assertEquals(300, message[IpcKeys.Y])
        assertEquals(50, message[IpcKeys.DELTA_X])
        assertEquals(-20, message[IpcKeys.DELTA_Y])
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
        val message = MessageBuilders.gestureEvent(
            IpcKeys.TAP,
            IpcKeys.HEAT,
            x = 100,
            y = 200,
            frameTimestamp = System.currentTimeMillis()
        )
        
        val timestamp = message[IpcKeys.TIMESTAMP] as Long
        assertTrue(timestamp > 0)
        
        // Should be recent (within last minute)
        val now = System.currentTimeMillis()
        assertTrue(timestamp <= now)
        assertTrue(timestamp > now - 60000)
    }
    
    @Test
    fun testGestureEventIncludesFrameTimestamp() {
        val frameTimestamp = 1234567890L
        val message = MessageBuilders.gestureEvent(
            IpcKeys.TAP,
            IpcKeys.HEAT,
            x = 100,
            y = 200,
            frameTimestamp = frameTimestamp
        )
        
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
        // Should also have a separate system timestamp
        assertNotNull(message[IpcKeys.TIMESTAMP])
        assertNotEquals(frameTimestamp, message[IpcKeys.TIMESTAMP])
    }
}