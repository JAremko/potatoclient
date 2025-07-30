package potatoclient.transit

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

class TransitCommunicatorTest {

    @Test
    fun testMessageEnvelopeCreation() {
        val comm = TransitCommunicator(
            ByteArrayInputStream(ByteArray(0)),
            ByteArrayOutputStream()
        )
        
        val message = comm.createMessage("test", mapOf("data" to "value"))
        
        assertEquals("test", message["msg-type"])
        assertNotNull(message["msg-id"])
        assertNotNull(message["timestamp"])
        assertEquals(mapOf("data" to "value"), message["payload"])
    }

    @Test
    fun testFramedStreamWriteRead() {
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
    fun testLargeMessageFraming() {
        val outputStream = ByteArrayOutputStream()
        val framedOut = FramedOutputStream(outputStream)
        
        // Create large message
        val largeData = ByteArray(1024 * 1024) { it.toByte() } // 1MB
        framedOut.write(largeData)
        framedOut.flush()
        
        // Read it back
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val framedIn = FramedInputStream(inputStream)
        
        val buffer = ByteArray(largeData.size)
        var totalRead = 0
        while (totalRead < buffer.size) {
            val n = framedIn.read(buffer, totalRead, buffer.size - totalRead)
            if (n == -1) break
            totalRead += n
        }
        
        assertEquals(largeData.size, totalRead)
        assertArrayEquals(largeData, buffer)
    }

    @Test(expected = java.io.IOException::class)
    fun testInvalidFrameSize() {
        // Create invalid frame with negative size
        val invalidFrame = ByteArray(4)
        invalidFrame[0] = 0xFF.toByte() // Negative size when interpreted as int
        invalidFrame[1] = 0xFF.toByte()
        invalidFrame[2] = 0xFF.toByte()
        invalidFrame[3] = 0xFF.toByte()
        
        val inputStream = ByteArrayInputStream(invalidFrame)
        val framedIn = FramedInputStream(inputStream)
        
        framedIn.read() // Should throw IOException
    }

    @Test
    fun testRateLimiter() = runBlocking {
        val limiter = RateLimiter(10) // 10 Hz
        
        // Should allow first 10 immediately
        var allowed = 0
        repeat(15) {
            if (limiter.tryAcquire()) {
                allowed++
            }
        }
        
        // Should have allowed exactly 10
        assertEquals(10, allowed)
        
        // Wait a bit and try again
        delay(150) // Wait 150ms, should allow ~1-2 more
        
        var additionalAllowed = 0
        repeat(5) {
            if (limiter.tryAcquire()) {
                additionalAllowed++
            }
        }
        
        assertTrue("Should allow at least 1 more after delay", additionalAllowed >= 1)
        
        limiter.shutdown()
    }

    @Test
    fun testProtobufToTransitConversion() {
        val converter = ProtobufToTransitConverter()
        
        // This test would need actual protobuf classes to work
        // For now, just test that the converter is instantiable
        assertNotNull(converter)
    }
}

class TransitToProtobufBuilderTest {

    @Test
    fun testBuildCommandValidation() {
        val builder = TransitToProtobufBuilder()
        
        // Test unknown action throws exception
        try {
            builder.buildCommand(mapOf("action" to "unknown-action"))
            fail("Should throw exception for unknown action")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Unknown command action") ?: false)
        }
        
        // Test missing action throws exception
        try {
            builder.buildCommand(mapOf("params" to mapOf<String, Any>()))
            fail("Should throw exception for missing action")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Missing 'action' field") ?: false)
        }
    }
}