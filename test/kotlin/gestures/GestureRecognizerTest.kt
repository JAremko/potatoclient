package potatoclient.kotlin.gestures

import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class GestureRecognizerTest {
    
    private class GestureCapture {
        val gestures = mutableListOf<GestureEvent>()
        
        fun capture(gesture: GestureEvent) {
            gestures.add(gesture)
        }
        
        fun reset() {
            gestures.clear()
        }
    }
    
    @Test
    fun testSingleTap() {
        val capture = GestureCapture()
        val recognizer = GestureRecognizer { capture.capture(it) }
        
        // Simulate tap
        val time = System.currentTimeMillis()
        recognizer.processMousePressed(100, 100, 1, time)
        recognizer.processMouseReleased(100, 100, 1, time + 50)
        
        assertEquals(1, capture.gestures.size)
        val tap = capture.gestures[0] as? GestureEvent.Tap
        assertNotNull(tap)
        assertEquals(100, tap?.x)
        assertEquals(100, tap?.y)
    }
    
    @Test
    fun testDoubleTap() {
        val capture = GestureCapture()
        val recognizer = GestureRecognizer { capture.capture(it) }
        
        val time = System.currentTimeMillis()
        
        // First tap
        recognizer.processMousePressed(100, 100, 1, time)
        recognizer.processMouseReleased(100, 100, 1, time + 50)
        
        // Second tap within threshold
        recognizer.processMousePressed(105, 105, 1, time + 150)
        recognizer.processMouseReleased(105, 105, 1, time + 200)
        
        assertEquals(2, capture.gestures.size)
        assertTrue(capture.gestures[0] is GestureEvent.Tap)
        
        val doubleTap = capture.gestures[1] as? GestureEvent.DoubleTap
        assertNotNull(doubleTap)
        assertEquals(105, doubleTap?.x)
        assertEquals(105, doubleTap?.y)
    }
    
    @Test
    fun testDoubleTapOutsideTolerance() {
        val capture = GestureCapture()
        val config = GestureConfig(doubleTapTolerance = 5)
        val recognizer = GestureRecognizer(config) { capture.capture(it) }
        
        val time = System.currentTimeMillis()
        
        // First tap
        recognizer.processMousePressed(100, 100, 1, time)
        recognizer.processMouseReleased(100, 100, 1, time + 50)
        
        // Second tap too far away
        recognizer.processMousePressed(110, 110, 1, time + 150)
        recognizer.processMouseReleased(110, 110, 1, time + 200)
        
        assertEquals(2, capture.gestures.size)
        assertTrue(capture.gestures[0] is GestureEvent.Tap)
        assertTrue(capture.gestures[1] is GestureEvent.Tap) // Not a double tap
    }
    
    @Test
    fun testPanGesture() {
        val capture = GestureCapture()
        val config = GestureConfig(moveThreshold = 10)
        val recognizer = GestureRecognizer(config) { capture.capture(it) }
        
        val time = System.currentTimeMillis()
        
        // Start drag
        recognizer.processMousePressed(100, 100, 1, time)
        
        // Move below threshold - no pan start yet
        recognizer.processMouseDragged(105, 105, time + 50)
        assertEquals(0, capture.gestures.size)
        
        // Move beyond threshold - pan starts
        recognizer.processMouseDragged(115, 115, time + 100)
        assertEquals(1, capture.gestures.size)
        assertTrue(capture.gestures[0] is GestureEvent.PanStart)
        
        // Continue dragging
        recognizer.processMouseDragged(120, 120, time + 150)
        
        // Should have pan move event
        val panMoves = capture.gestures.filterIsInstance<GestureEvent.PanMove>()
        assertTrue(panMoves.isNotEmpty())
        
        val panMove = panMoves.first()
        assertEquals(20, panMove.deltaX) // 120 - 100
        assertEquals(20, panMove.deltaY) // 120 - 100
        
        // Release
        recognizer.processMouseReleased(120, 120, 1, time + 200)
        
        val panStop = capture.gestures.lastOrNull() as? GestureEvent.PanStop
        assertNotNull(panStop)
        assertEquals(120, panStop?.x)
        assertEquals(120, panStop?.y)
    }
    
    @Test
    fun testPanThrottling() {
        val capture = GestureCapture()
        val config = GestureConfig(moveThreshold = 10, panUpdateInterval = 100)
        val recognizer = GestureRecognizer(config) { capture.capture(it) }
        
        val time = System.currentTimeMillis()
        
        // Start pan
        recognizer.processMousePressed(100, 100, 1, time)
        recognizer.processMouseDragged(120, 120, time + 10)
        
        val initialCount = capture.gestures.size
        
        // Rapid moves within throttle interval
        recognizer.processMouseDragged(125, 125, time + 20)
        recognizer.processMouseDragged(130, 130, time + 30)
        recognizer.processMouseDragged(135, 135, time + 40)
        
        // Should not have additional pan moves due to throttling
        assertEquals(initialCount, capture.gestures.size)
        
        // Move after throttle interval
        recognizer.processMouseDragged(140, 140, time + 150)
        
        // Should have new pan move
        assertTrue(capture.gestures.size > initialCount)
    }
    
    @Test
    fun testSwipeGesture() {
        val capture = GestureCapture()
        val config = GestureConfig(swipeThreshold = 50, tapLongPressThreshold = 200)
        val recognizer = GestureRecognizer(config) { capture.capture(it) }
        
        val time = System.currentTimeMillis()
        
        // Quick swipe right
        recognizer.processMousePressed(100, 100, 1, time)
        recognizer.processMouseReleased(160, 100, 1, time + 100)
        
        assertEquals(1, capture.gestures.size)
        val swipe = capture.gestures[0] as? GestureEvent.Swipe
        assertNotNull(swipe)
        assertEquals(SwipeDirection.RIGHT, swipe?.direction)
        assertEquals(60, swipe?.distance)
    }
    
    @Test
    fun testSwipeDirections() {
        val capture = GestureCapture()
        val config = GestureConfig(swipeThreshold = 50)
        val recognizer = GestureRecognizer(config) { capture.capture(it) }
        
        data class SwipeTest(val dx: Int, val dy: Int, val expected: SwipeDirection)
        
        val tests = listOf(
            SwipeTest(60, 0, SwipeDirection.RIGHT),
            SwipeTest(-60, 0, SwipeDirection.LEFT),
            SwipeTest(0, 60, SwipeDirection.DOWN),
            SwipeTest(0, -60, SwipeDirection.UP),
            SwipeTest(60, 30, SwipeDirection.RIGHT), // Diagonal right-down
            SwipeTest(-60, -30, SwipeDirection.LEFT)  // Diagonal left-up
        )
        
        for ((index, test) in tests.withIndex()) {
            capture.reset()
            recognizer.reset()
            
            val time = System.currentTimeMillis() + index * 1000
            recognizer.processMousePressed(100, 100, 1, time)
            recognizer.processMouseReleased(100 + test.dx, 100 + test.dy, 1, time + 100)
            
            assertEquals("Test $index failed", 1, capture.gestures.size)
            val swipe = capture.gestures[0] as? GestureEvent.Swipe
            assertNotNull("Test $index: swipe is null", swipe)
            assertEquals("Test $index: wrong direction", test.expected, swipe?.direction)
        }
    }
    
    @Test
    fun testRightButtonIgnored() {
        val capture = GestureCapture()
        val recognizer = GestureRecognizer { capture.capture(it) }
        
        val time = System.currentTimeMillis()
        
        // Right button click - should be ignored
        recognizer.processMousePressed(100, 100, 3, time)
        recognizer.processMouseReleased(100, 100, 3, time + 50)
        
        assertEquals(0, capture.gestures.size)
    }
    
    @Test
    fun testReset() {
        val capture = GestureCapture()
        val recognizer = GestureRecognizer { capture.capture(it) }
        
        val time = System.currentTimeMillis()
        
        // Start a pan
        recognizer.processMousePressed(100, 100, 1, time)
        recognizer.processMouseDragged(150, 150, time + 50)
        
        // Reset
        recognizer.reset()
        
        // Release should not generate pan stop
        capture.reset()
        recognizer.processMouseReleased(150, 150, 1, time + 100)
        
        assertEquals(0, capture.gestures.size)
    }
    
    @Test
    fun testLongPressNotRecognizedAsTap() {
        val capture = GestureCapture()
        val config = GestureConfig(tapLongPressThreshold = 200)
        val recognizer = GestureRecognizer(config) { capture.capture(it) }
        
        val time = System.currentTimeMillis()
        
        // Press and hold for too long
        recognizer.processMousePressed(100, 100, 1, time)
        recognizer.processMouseReleased(100, 100, 1, time + 300)
        
        // Should not generate tap
        assertEquals(0, capture.gestures.size)
    }
}