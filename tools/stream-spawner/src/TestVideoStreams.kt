package streamspawner

import potatoclient.kotlin.ipc.IpcServer
import potatoclient.kotlin.ipc.IpcClient
import potatoclient.kotlin.ipc.IpcKeys
import org.freedesktop.gstreamer.Gst
import org.freedesktop.gstreamer.Pipeline
import org.freedesktop.gstreamer.Element
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

/**
 * Test Video Streams - Creates test pattern video windows
 * This demonstrates the stream spawner can create video windows
 */
class TestVideoStreams(
    private val host: String = "sych.local"
) {
    private val shutdownLatch = CountDownLatch(1)
    private val ipcServers = mutableMapOf<String, IpcServer>()
    private val frames = mutableMapOf<String, JFrame>()
    
    /**
     * Initialize GStreamer
     */
    private fun initGStreamer() {
        try {
            Gst.init("TestVideoStreams")
            println("GStreamer initialized successfully")
        } catch (e: Exception) {
            println("Failed to initialize GStreamer: ${e.message}")
            throw e
        }
    }
    
    /**
     * Create a test video window with GStreamer test pattern
     */
    private fun createTestVideoWindow(streamId: String): JFrame {
        val frame = JFrame("Video Stream - $streamId (Test Pattern)")
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.setSize(640, 480)
        
        // Create a panel for the video
        val videoPanel = JPanel()
        videoPanel.preferredSize = Dimension(640, 480)
        videoPanel.background = java.awt.Color.BLACK
        
        frame.contentPane.add(videoPanel, BorderLayout.CENTER)
        
        // Position windows side by side
        val xOffset = if (streamId == "heat") 100 else 750
        frame.setLocation(xOffset, 100)
        
        // Create GStreamer pipeline with test pattern
        thread(name = "$streamId-gstreamer") {
            try {
                val pipeline = Pipeline.launch(
                    "videotestsrc pattern=smpte ! " +
                    "video/x-raw,width=640,height=480 ! " +
                    "videoconvert ! " +
                    "autovideosink"
                )
                
                pipeline.play()
                println("[$streamId] GStreamer pipeline started with test pattern")
                
                // Keep pipeline running
                Thread.sleep(Long.MAX_VALUE)
            } catch (e: Exception) {
                println("[$streamId] GStreamer error: ${e.message}")
            }
        }
        
        frame.isVisible = true
        return frame
    }
    
    /**
     * Create IPC server for a stream
     */
    private fun createIpcServer(streamId: String): IpcServer {
        println("Creating IPC server for $streamId...")
        val server = IpcServer.create(streamId, true)
        
        server.setOnMessage { message ->
            val msgType = message[IpcKeys.MSG_TYPE]
            println("[$streamId] IPC received: $msgType")
        }
        
        return server
    }
    
    /**
     * Start test video streams
     */
    fun start() {
        println("========================================")
        println("Test Video Streams - GStreamer Patterns")
        println("========================================")
        println("Host: $host (simulation mode)")
        println()
        
        // Initialize GStreamer
        try {
            initGStreamer()
        } catch (e: Exception) {
            System.err.println("Cannot start without GStreamer")
            return
        }
        
        // Create IPC servers
        try {
            ipcServers["heat"] = createIpcServer("heat")
            ipcServers["day"] = createIpcServer("day")
            println("IPC servers ready!")
        } catch (e: Exception) {
            System.err.println("Failed to create IPC servers: ${e.message}")
            return
        }
        
        // Create video windows
        SwingUtilities.invokeLater {
            try {
                frames["heat"] = createTestVideoWindow("heat")
                frames["day"] = createTestVideoWindow("day")
                
                println()
                println("Test video windows created!")
                println("Showing SMPTE test patterns")
                println("Press Ctrl+C to stop")
                println()
            } catch (e: Exception) {
                System.err.println("Failed to create video windows: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            shutdown()
        })
        
        // Wait for shutdown
        shutdownLatch.await()
    }
    
    /**
     * Shutdown everything
     */
    fun shutdown() {
        println("\n[TestVideoStreams] Shutting down...")
        
        // Close windows
        SwingUtilities.invokeLater {
            frames.values.forEach { it.dispose() }
        }
        
        // Stop IPC servers
        ipcServers.forEach { (streamId, server) ->
            println("  Stopping IPC server for $streamId...")
            server.stop()
        }
        
        // Quit GStreamer
        Gst.quit()
        
        shutdownLatch.countDown()
        println("[TestVideoStreams] Shutdown complete")
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val host = if (args.isNotEmpty()) args[0] else "sych.local"
            val streams = TestVideoStreams(host)
            streams.start()
        }
    }
}