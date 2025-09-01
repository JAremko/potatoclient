package potatoclient.kotlin.gestures

/**
 * Interface for providing frame timing data to gesture handlers
 */
interface FrameDataProvider {
    fun getFrameData(): FrameData?

    fun getCurrentFrameTimestamp(): Long = getFrameData()?.timestamp ?: System.currentTimeMillis()

    fun getCurrentZoomLevel(): Int
}

data class FrameData(
    val timestamp: Long,
    val duration: Long,
)
