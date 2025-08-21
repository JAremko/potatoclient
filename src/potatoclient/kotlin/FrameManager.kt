package potatoclient.kotlin

import com.sun.jna.Platform
import potatoclient.kotlin.ipc.IpcClient
import potatoclient.kotlin.ipc.IpcKeys
import java.awt.Canvas
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.SwingUtilities

/**
 * Manages the creation and lifecycle of the video frame and component.
 */
class FrameManager(
    private val streamId: String,
    private val domain: String,
    private val listener: FrameEventListener?,
    private val ipcClient: IpcClient,
) {
    @Volatile private var frame: JFrame? = null

    @Volatile private var videoComponent: Component? = null

    interface FrameEventListener {
        fun onFrameCreated(
            frame: JFrame,
            videoComponent: Component,
        )

        fun onFrameClosing()
    }

    fun createFrame() {
        SwingUtilities.invokeLater {
            frame = createJFrame()
            videoComponent = createVideoComponent()

            frame?.apply {
                add(videoComponent)
                pack()
                
                // Position windows side by side
                val screenSize = java.awt.Toolkit.getDefaultToolkit().screenSize
                val frameWidth = preferredSize.width
                val x = if (streamId == Constants.StreamConfig.HEAT_STREAM_ID) {
                    // Heat stream on the left
                    (screenSize.width / 2 - frameWidth - 50).coerceAtLeast(0)
                } else {
                    // Day stream on the right
                    (screenSize.width / 2 + 50).coerceAtMost(screenSize.width - frameWidth)
                }
                val y = (screenSize.height - preferredSize.height) / 2
                setLocation(x, y)
                
                // Show the frame immediately
                isVisible = true
            }

            frame?.let { f ->
                videoComponent?.let { vc ->
                    listener?.onFrameCreated(f, vc)
                }
            }
        }
    }

    private fun createJFrame(): JFrame {
        val baseTitle =
            if (streamId == Constants.StreamConfig.HEAT_STREAM_ID) {
                "Heat Stream"
            } else {
                "Day Stream"
            }

        // Include domain in title to help distinguish instances
        val title = "$baseTitle - $domain"

        return JFrame(title).apply {
            defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE

            // Set window icon based on stream type
            try {
                val iconResource =
                    if (streamId == Constants.StreamConfig.HEAT_STREAM_ID) {
                        "/heat.png"
                    } else {
                        "/day.png"
                    }
                javaClass.getResource(iconResource)?.let { iconURL ->
                    iconImage = ImageIcon(iconURL).image
                }
            } catch (e: IOException) {
                ipcClient.sendLog(IpcKeys.ERROR, "Failed to load window icon: ${e.message}")
            }

            // Add window close listener
            addWindowListener(
                object : WindowAdapter() {
                    override fun windowClosing(e: WindowEvent) {
                        listener?.onFrameClosing()
                    }
                },
            )
        }
    }

    private fun createVideoComponent(): Component {
        // Create video component based on platform
        val component =
            when {
                Platform.isLinux() -> Canvas() // Use Canvas for X11
                Platform.isWindows() -> Canvas() // Use heavyweight component for Windows
                Platform.isMac() -> {
                    // Use Canvas for macOS (more reliable than JComponent)
                    System.setProperty("apple.awt.graphics.UseQuartz", "true")
                    Canvas()
                }
                else -> Canvas() // Default fallback
            }

        // Set preferred size based on stream
        component.preferredSize =
            if (streamId == Constants.StreamConfig.HEAT_STREAM_ID) {
                Dimension(
                    Constants.StreamConfig.HEAT_STREAM_WIDTH,
                    Constants.StreamConfig.HEAT_STREAM_HEIGHT,
                )
            } else {
                Dimension(
                    Constants.StreamConfig.DAY_STREAM_DISPLAY_WIDTH,
                    Constants.StreamConfig.DAY_STREAM_DISPLAY_HEIGHT,
                )
            }

        component.background = Color.BLACK

        return component
    }

    fun showFrame() {
        SwingUtilities.invokeLater {
            frame?.isVisible = true
        }
    }

    fun hideFrame() {
        SwingUtilities.invokeLater {
            frame?.isVisible = false
        }
    }

    fun disposeFrame() {
        SwingUtilities.invokeLater {
            frame?.dispose()
            frame = null
            videoComponent = null
        }
    }

    fun getFrame(): JFrame? = frame

    fun getVideoComponent(): Component? = videoComponent
}
