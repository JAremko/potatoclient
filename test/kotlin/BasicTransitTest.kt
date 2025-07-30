package potatoclient.transit

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BasicTransitTest {

    @Test
    fun testFramedStreamBasic() {
        val outputStream = ByteArrayOutputStream()
        val framedOut = FramedOutputStream(outputStream)
        
        // Write test data
        val testData = "Hello Transit!".toByteArray()
        framedOut.write(testData)
        framedOut.flush()
        
        // Read it back
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val framedIn = FramedInputStream(inputStream)
        
        val buffer = ByteArray(testData.size)
        val bytesRead = framedIn.read(buffer)
        
        assertEquals(testData.size, bytesRead)
        assertArrayEquals(testData, buffer)
    }

    @Test
    fun testTransitCommunicatorEnvelope() {
        val out = ByteArrayOutputStream()
        val comm = TransitCommunicator(ByteArrayInputStream(ByteArray(0)), out)
        
        val message = comm.createMessage("test-type", mapOf("key" to "value"))
        
        assertEquals("test-type", message["msg-type"])
        assertNotNull(message["msg-id"])
        assertNotNull(message["timestamp"])
        assertEquals(mapOf("key" to "value"), message["payload"])
    }

    @Test
    fun testRateLimiterBasic() {
        val limiter = RateLimiter(10) // 10 Hz
        
        // Should allow first acquisition
        assertTrue(limiter.tryAcquire())
        
        // Shutdown
        limiter.shutdown()
    }
}