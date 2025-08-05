package potatoclient.kotlin

import cmd.CV.JonSharedCmdCv
import cmd.JonSharedCmd
import cmd.RotaryPlatform.JonSharedCmdRotary
import com.google.protobuf.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import potatoclient.java.transit.MessageType
import potatoclient.kotlin.transit.TransitCommunicator
import potatoclient.kotlin.transit.TransitKeys
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test mode stub for WebSocket operations.
 * Provides mock responses for testing without actual WebSocket connection.
 */
class TestModeWebSocketStub(
    private val transitComm: TransitCommunicator,
) {
    private val commandCounter = AtomicInteger(0)

    /**
     * Simulate sending a command and receiving acknowledgment
     */
    suspend fun handleCommand(command: JonSharedCmd.Root) {
        val commandId = commandCounter.incrementAndGet()

        // Simulate processing delay
        kotlinx.coroutines.delay(10)

        // Send acknowledgment via Transit
        val response =
            when {
                command.hasPing() -> createPongResponse()
                command.hasNoop() -> createAckResponse("noop", commandId)
                command.hasRotary() -> handleRotaryCommand(command.rotary, commandId)
                command.hasCv() -> handleCvCommand(command.cv, commandId)
                command.hasHeatCamera() -> createAckResponse("heat-camera", commandId)
                command.hasDayCamera() -> createAckResponse("day-camera", commandId)
                else -> createErrorResponse("Unknown command type")
            }

        transitComm.sendMessage(response)
    }

    private fun createPongResponse(): Map<Any, Any> =
        mapOf(
            TransitKeys.MSG_TYPE to MessageType.RESPONSE.key,
            TransitKeys.MSG_ID to
                java.util.UUID
                    .randomUUID()
                    .toString(),
            TransitKeys.TIMESTAMP to System.currentTimeMillis(),
            TransitKeys.PAYLOAD to
                mapOf(
                    TransitKeys.TYPE to TransitKeys.PONG,
                    TransitKeys.TIMESTAMP to System.currentTimeMillis(),
                ),
        )

    private fun createAckResponse(
        commandType: String,
        commandId: Int,
    ): Map<Any, Any> =
        mapOf(
            TransitKeys.MSG_TYPE to MessageType.RESPONSE.key,
            TransitKeys.MSG_ID to
                java.util.UUID
                    .randomUUID()
                    .toString(),
            TransitKeys.TIMESTAMP to System.currentTimeMillis(),
            TransitKeys.PAYLOAD to
                mapOf(
                    TransitKeys.TYPE to TransitKeys.ACK,
                    TransitKeys.ACTION to commandType,
                    TransitKeys.MSG_ID to commandId,
                ),
        )

    private fun createErrorResponse(message: String): Map<Any, Any> =
        mapOf(
            TransitKeys.MSG_TYPE to MessageType.ERROR.key,
            TransitKeys.MSG_ID to
                java.util.UUID
                    .randomUUID()
                    .toString(),
            TransitKeys.TIMESTAMP to System.currentTimeMillis(),
            TransitKeys.PAYLOAD to
                mapOf(
                    TransitKeys.TYPE to TransitKeys.ERROR,
                    TransitKeys.MESSAGE to message,
                ),
        )

    private suspend fun handleRotaryCommand(
        rotary: JonSharedCmdRotary.Root,
        commandId: Int,
    ): Map<Any, Any> {
        // Simulate validation based on actual commands
        return when {
            rotary.hasRotateToNdc() -> {
                val rotate = rotary.rotateToNdc
                if (rotate.x < -1 || rotate.x > 1 || rotate.y < -1 || rotate.y > 1) {
                    createErrorResponse("NDC coordinates out of range")
                } else {
                    createAckResponse("rotary-rotate-to-ndc", commandId)
                }
            }
            rotary.hasStop() -> createAckResponse("rotary-stop", commandId)
            rotary.hasHalt() -> createAckResponse("rotary-halt", commandId)
            rotary.hasStart() -> createAckResponse("rotary-start", commandId)
            else -> createAckResponse("rotary", commandId)
        }
    }

    private fun handleCvCommand(
        cv: JonSharedCmdCv.Root,
        commandId: Int,
    ): Map<Any, Any> =
        when {
            cv.hasStartTrackNdc() -> {
                val track = cv.startTrackNdc
                if (track.x < -1 || track.x > 1 || track.y < -1 || track.y > 1) {
                    createErrorResponse("NDC coordinates out of range")
                } else {
                    createAckResponse("cv-start-track-ndc", commandId)
                }
            }
            cv.hasStopTrack() -> createAckResponse("cv-stop-track", commandId)
            else -> createAckResponse("cv", commandId)
        }

    /**
     * Simulate receiving state updates
     */
    fun simulateStateUpdates(): Flow<Message> =
        flow {
            // Could emit mock state updates if needed for testing
            // For now, just complete without emitting
        }
}
