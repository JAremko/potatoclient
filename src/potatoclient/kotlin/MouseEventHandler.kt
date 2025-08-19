package potatoclient.kotlin

import potatoclient.kotlin.gestures.FrameDataProvider
import potatoclient.kotlin.gestures.GestureConfig
import potatoclient.kotlin.gestures.GestureEvent
import potatoclient.kotlin.gestures.GestureRecognizer
import potatoclient.kotlin.ipc.IpcManager
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseWheelListener

/**
 * Simplified mouse event handler that sends raw gesture events to Clojure.
 * All gesture interpretation and command generation happens on the Clojure side.
 */
class MouseEventHandler(
    private val videoComponent: Component,
    private val ipcManager: IpcManager,
    private val frameDataProvider: FrameDataProvider
) {
    // Gesture recognition components
    private val gestureConfig = GestureConfig(
        moveThreshold = 20,
        tapLongPressThreshold = 300,
        doubleTapThreshold = 300,
        panUpdateInterval = 120,
        doubleTapTolerance = 10
    )

    private val gestureRecognizer = GestureRecognizer(
        config = gestureConfig,
        onGesture = { gesture ->
            // Send gesture directly to Clojure via IPC
            ipcManager.sendGestureEvent(gesture)
        },
        frameDataProvider = frameDataProvider
    )

    fun attachListeners() {
        // Mouse button events
        videoComponent.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                gestureRecognizer.processMousePressed(
                    e.x, e.y, e.button, System.currentTimeMillis()
                )
            }

            override fun mouseReleased(e: MouseEvent) {
                gestureRecognizer.processMouseReleased(
                    e.x, e.y, e.button, System.currentTimeMillis()
                )
            }
        })

        // Mouse motion events
        videoComponent.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                gestureRecognizer.processMouseDragged(
                    e.x, e.y, System.currentTimeMillis()
                )
            }
        })

        // Mouse wheel events
        videoComponent.addMouseWheelListener(MouseWheelListener { e ->
            gestureRecognizer.processMouseWheel(
                e.x, e.y, e.wheelRotation, System.currentTimeMillis()
            )
        })
    }

    fun cleanup() {
        gestureRecognizer.reset()
    }

    /**
     * Companion object containing NDC conversion utilities.
     * These may be used by other components but are not used here
     * since all coordinate conversion happens on the Clojure side.
     */
    companion object {
        data class NDCPoint(val x: Double, val y: Double)
        data class PixelPoint(val x: Int, val y: Int)

        /**
         * Convert pixel coordinates to NDC coordinates.
         * 
         * @param x Pixel X coordinate (0 to width-1)
         * @param y Pixel Y coordinate (0 to height-1)
         * @param width Canvas width in pixels
         * @param height Canvas height in pixels
         * @return NDC coordinates where x,y are in range [-1, 1]
         */
        @JvmStatic
        fun pixelToNDC(x: Int, y: Int, width: Int, height: Int): NDCPoint {
            require(width > 0 && height > 0) { "Width and height must be positive" }
            
            var ndcX = (x.toDouble() / width) * 2.0 - 1.0
            var ndcY = -((y.toDouble() / height) * 2.0 - 1.0) // Invert Y
            
            // Clamp to valid NDC range to handle edge cases
            ndcX = ndcX.coerceIn(-1.0, 1.0)
            ndcY = ndcY.coerceIn(-1.0, 1.0)
            
            return NDCPoint(ndcX, ndcY)
        }

        /**
         * Convert NDC coordinates to pixel coordinates.
         * 
         * @param ndcX NDC X coordinate (-1 to 1)
         * @param ndcY NDC Y coordinate (-1 to 1)
         * @param width Canvas width in pixels
         * @param height Canvas height in pixels
         * @return Pixel coordinates
         */
        @JvmStatic
        fun ndcToPixel(ndcX: Double, ndcY: Double, width: Int, height: Int): PixelPoint {
            require(width > 0 && height > 0) { "Width and height must be positive" }
            
            // Clamp NDC coordinates to valid range
            val clampedX = ndcX.coerceIn(-1.0, 1.0)
            val clampedY = ndcY.coerceIn(-1.0, 1.0)
            
            var x = Math.round((clampedX + 1.0) / 2.0 * width).toInt()
            var y = Math.round((-clampedY + 1.0) / 2.0 * height).toInt() // Invert Y back
            
            // Clamp to valid pixel range
            x = x.coerceIn(0, width - 1)
            y = y.coerceIn(0, height - 1)
            
            return PixelPoint(x, y)
        }

        /**
         * Convert pixel delta to NDC delta (for pan gestures).
         * Does not invert Y axis as this is a relative movement.
         * 
         * @param deltaX Pixel delta X
         * @param deltaY Pixel delta Y
         * @param width Canvas width in pixels
         * @param height Canvas height in pixels
         * @return NDC delta values
         */
        @JvmStatic
        fun pixelDeltaToNDC(deltaX: Int, deltaY: Int, width: Int, height: Int): NDCPoint {
            require(width > 0 && height > 0) { "Width and height must be positive" }
            
            val ndcDeltaX = (deltaX.toDouble() / width) * 2.0
            val ndcDeltaY = -(deltaY.toDouble() / height) * 2.0 // Still invert for consistency
            
            return NDCPoint(ndcDeltaX, ndcDeltaY)
        }

        /**
         * Apply aspect ratio correction to NDC X coordinate.
         * This is useful for maintaining circular motion in non-square viewports.
         * 
         * @param ndcX NDC X coordinate
         * @param aspectRatio Width/Height ratio
         * @return Aspect-corrected NDC X
         */
        @JvmStatic
        fun applyAspectRatio(ndcX: Double, aspectRatio: Double): Double {
            return ndcX * aspectRatio
        }

        /**
         * Check if pixel coordinates are within canvas bounds.
         * 
         * @param x Pixel X coordinate
         * @param y Pixel Y coordinate
         * @param width Canvas width
         * @param height Canvas height
         * @return true if coordinates are within bounds
         */
        @JvmStatic
        fun isInBounds(x: Int, y: Int, width: Int, height: Int): Boolean {
            return x >= 0 && x < width && y >= 0 && y < height
        }
    }
}