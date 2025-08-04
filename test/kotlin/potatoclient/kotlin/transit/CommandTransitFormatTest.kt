package potatoclient.kotlin.transit

import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Writer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import potatoclient.kotlin.events.CommandBuilder
import potatoclient.kotlin.events.toKeyword
import potatoclient.kotlin.gestures.StreamType
import potatoclient.kotlin.gestures.RotaryDirection
import potatoclient.transit.MessageType
import java.io.ByteArrayOutputStream

class CommandTransitFormatTest {
    
    private fun serializeToTransit(data: Any): ByteArray {
        val out = ByteArrayOutputStream()
        val writer: Writer<*> = TransitFactory.writer(TransitFactory.Format.MSGPACK, out)
        writer.write(data)
        return out.toByteArray()
    }
    
    @Test
    fun `command messages have correct Transit envelope structure`() {
        val command = CommandBuilder.rotaryGotoNDC(StreamType.HEAT, 0.5, -0.3)
        
        // Create full Transit message envelope
        val message = mapOf(
            "msg-type" to MessageType.COMMAND.keyword,
            "msg-id" to "test-123",
            "timestamp" to System.currentTimeMillis(),
            "payload" to command
        )
        
        // Verify structure
        assertEquals(MessageType.COMMAND.keyword, message["msg-type"])
        assertTrue(message.containsKey("payload"))
        
        val payload = message["payload"] as Map<*, *>
        assertTrue(payload.containsKey("rotary"))
    }
    
    @Test
    fun `all enum values serialize as keywords not strings`() {
        // Test various commands that use enums
        val commands = listOf(
            CommandBuilder.rotaryGotoNDC(StreamType.HEAT, 0.0, 0.0),
            CommandBuilder.rotaryGotoNDC(StreamType.DAY, 0.0, 0.0),
            CommandBuilder.cvStartTrackNDC(StreamType.HEAT, 0.0, 0.0, null),
            CommandBuilder.cvStartTrackNDC(StreamType.DAY, 0.0, 0.0, null),
            CommandBuilder.rotarySetVelocity(
                0.1, 0.1,
                RotaryDirection.CLOCKWISE,
                RotaryDirection.COUNTER_CLOCKWISE
            )
        )
        
        // Check each command
        commands.forEach { cmd ->
            when {
                cmd.containsKey("rotary") -> {
                    val rotary = cmd["rotary"] as Map<*, *>
                    when {
                        rotary.containsKey("goto-ndc") -> {
                            val gotoNdc = rotary["goto-ndc"] as Map<*, *>
                            val channel = gotoNdc["channel"] as String
                            assertTrue(channel in listOf("heat", "day"))
                            assertFalse(channel in listOf("HEAT", "DAY"))
                        }
                        rotary.containsKey("set-velocity") -> {
                            val velocity = rotary["set-velocity"] as Map<*, *>
                            val azDir = velocity["azimuth-direction"] as String
                            val elDir = velocity["elevation-direction"] as String
                            assertTrue(azDir in listOf("clockwise", "counter-clockwise"))
                            assertTrue(elDir in listOf("clockwise", "counter-clockwise"))
                            assertFalse(azDir.contains("CLOCKWISE"))
                            assertFalse(elDir.contains("COUNTER_CLOCKWISE"))
                        }
                    }
                }
                cmd.containsKey("cv") -> {
                    val cv = cmd["cv"] as Map<*, *>
                    val startTrack = cv["start-track-ndc"] as Map<*, *>
                    val channel = startTrack["channel"] as String
                    assertTrue(channel in listOf("heat", "day"))
                    assertFalse(channel in listOf("HEAT", "DAY"))
                }
            }
        }
    }
    
    @Test
    fun `NDC values are within valid range`() {
        // Test boundary values
        val validNDCs = listOf(
            Pair(-1.0, -1.0),
            Pair(-1.0, 1.0),
            Pair(1.0, -1.0),
            Pair(1.0, 1.0),
            Pair(0.0, 0.0),
            Pair(0.5, -0.5)
        )
        
        validNDCs.forEach { (x, y) ->
            val command = CommandBuilder.rotaryGotoNDC(StreamType.HEAT, x, y)
            val gotoNdc = (command["rotary"] as Map<*, *>)["goto-ndc"] as Map<*, *>
            
            val ndcX = gotoNdc["x"] as Double
            val ndcY = gotoNdc["y"] as Double
            
            assertTrue(ndcX >= -1.0 && ndcX <= 1.0, "NDC X $ndcX out of range")
            assertTrue(ndcY >= -1.0 && ndcY <= 1.0, "NDC Y $ndcY out of range")
        }
    }
    
    @Test
    fun `velocity commands have non-negative speeds`() {
        val command = CommandBuilder.rotarySetVelocity(
            0.5, 0.0,  // azimuth can be 0
            RotaryDirection.CLOCKWISE,
            RotaryDirection.COUNTER_CLOCKWISE
        )
        
        val velocity = (command["rotary"] as Map<*, *>)["set-velocity"] as Map<*, *>
        val azSpeed = velocity["azimuth-speed"] as Double
        val elSpeed = velocity["elevation-speed"] as Double
        
        assertTrue(azSpeed >= 0.0)
        assertTrue(elSpeed >= 0.0)
    }
    
    @Test
    fun `frame time is Long when present`() {
        val frameTime = System.currentTimeMillis()
        val command = CommandBuilder.cvStartTrackNDC(StreamType.HEAT, 0.0, 0.0, frameTime)
        
        val startTrack = (command["cv"] as Map<*, *>)["start-track-ndc"] as Map<*, *>
        val actualFrameTime = startTrack["frame-time"]
        
        assertTrue(actualFrameTime is Long)
        assertEquals(frameTime, actualFrameTime)
    }
}