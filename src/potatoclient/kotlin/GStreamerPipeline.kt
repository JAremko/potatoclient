package potatoclient.kotlin

import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.Pointer
import org.freedesktop.gstreamer.*
import org.freedesktop.gstreamer.elements.AppSrc
import org.freedesktop.gstreamer.interfaces.VideoOverlay
import java.awt.Component
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock

class GStreamerPipeline(private val callback: EventCallback) {
    interface EventCallback {
        fun onLog(level: String, message: String)
        fun onPipelineError(message: String)
        fun isRunning(): Boolean
    }

    private val pipelineLock = ReentrantLock()

    @Volatile private var pipeline: Pipeline? = null
    @Volatile private var appsrc: AppSrc? = null
    @Volatile private var videosink: Element? = null
    @Volatile private var videoOverlay: VideoOverlay? = null
    @Volatile private var selectedDecoder: String? = null
    @Volatile private var isAppImage = false
    @Volatile private var gstreamerVersion: String? = null

    private val frameCount = AtomicLong(0)
    private val startTime = AtomicLong(0)
    @Volatile private var hasReceivedKeyframe = false
    @Volatile private var pendingVideoComponent: Component? = null
    @Volatile private var overlaySet = false

    // Buffer pool for zero-allocation streaming
    private val bufferPool = ConcurrentLinkedQueue<Buffer>()
    private val poolHits = AtomicLong(0)
    private val poolMisses = AtomicLong(0)

    fun initialize(videoComponent: Component) {
        pipelineLock.lock()
        try {
            // Configure Windows paths if needed
            if (Platform.isWindows()) {
                GStreamerUtils.configureGStreamerPaths(object : GStreamerUtils.EventCallback {
                    override fun onLog(level: String, message: String) {
                        callback.onLog(level, message)
                    }
                })
            }

            // Initialize GStreamer if needed
            if (!Gst.isInitialized()) {
                callback.onLog("INFO", "Initializing GStreamer...")

                // Check for plugin path from environment or system property
                var gstPluginPath = System.getenv("GST_PLUGIN_PATH_1_0")
                if (gstPluginPath.isNullOrEmpty()) {
                    gstPluginPath = System.getenv("GST_PLUGIN_PATH")
                }
                if (gstPluginPath.isNullOrEmpty()) {
                    gstPluginPath = System.getProperty("gstreamer.plugin.path")
                }

                if (!gstPluginPath.isNullOrEmpty()) {
                    callback.onLog("DEBUG", "Found GST plugin path: $gstPluginPath")
                }

                try {
                    // Initialize with plugin path if available
                    if (!gstPluginPath.isNullOrEmpty()) {
                        val args = arrayOf("--gst-plugin-path=$gstPluginPath")
                        Gst.init(Constants.GSTREAMER_APP_NAME, *args)
                    } else {
                        Gst.init(Constants.GSTREAMER_APP_NAME)
                    }
                    gstreamerVersion = Gst.getVersionString()
                    callback.onLog("INFO", "GStreamer initialized successfully. Version: $gstreamerVersion")

                    // Check if running in AppImage
                    val appDir = System.getenv("APPDIR")
                    isAppImage = !appDir.isNullOrEmpty()
                    if (isAppImage) {
                        callback.onLog("INFO", "Running in AppImage environment")
                    }

                    // Force plugin registry update
                    val registry = Registry.get()
                    if (registry != null && !gstPluginPath.isNullOrEmpty()) {
                        callback.onLog("DEBUG", "Scanning plugin path: $gstPluginPath")
                        try {
                            registry.scanPath(gstPluginPath)
                        } catch (e: Exception) {
                            callback.onLog("DEBUG", "Plugin path scan warning: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    callback.onLog("ERROR", "Failed to initialize GStreamer: ${e.message}")
                    callback.onPipelineError("GStreamer init error: ${e.message}")
                    return
                }
            }

            // Log GStreamer environment
            callback.onLog("DEBUG", "GST_PLUGIN_PATH: ${System.getenv("GST_PLUGIN_PATH")}")
            callback.onLog("DEBUG", "GST_PLUGIN_PATH_1_0: ${System.getenv("GST_PLUGIN_PATH_1_0")}")
            callback.onLog("DEBUG", "GST_PLUGIN_SYSTEM_PATH_1_0: ${System.getenv("GST_PLUGIN_SYSTEM_PATH_1_0")}")
            callback.onLog("DEBUG", "GST_PLUGIN_SCANNER: ${System.getenv("GST_PLUGIN_SCANNER")}")
            callback.onLog("DEBUG", "GST_PLUGIN_SCANNER_1_0: ${System.getenv("GST_PLUGIN_SCANNER_1_0")}")
            callback.onLog("DEBUG", "LD_LIBRARY_PATH: ${System.getenv("LD_LIBRARY_PATH")}")
            callback.onLog("DEBUG", "APPDIR: ${System.getenv("APPDIR")}")
            callback.onLog("DEBUG", "Java library path: ${System.getProperty("java.library.path")}")

            // Create pipeline elements
            pipeline = Pipeline("video-pipeline")

            // AppSrc for receiving video data
            appsrc = AppSrc("appsrc").apply {
                set("is-live", true)
                set("format", Format.TIME)
                set("caps", Caps.fromString(Constants.H264_CAPS))
                set("max-bytes", 0L)
                set("block", false)
                set("emit-signals", false)
                set("min-latency", 0L)
                set("max-latency", 0L)
            }

            // H264 parser
            val h264parse = ElementFactory.make("h264parse", "h264parse")
            if (h264parse == null) {
                callback.onLog("ERROR", "Failed to create h264parse element - check if gstreamer1.0-plugins-bad is installed")
                return
            }
            h264parse.set("config-interval", 1)

            // H264 decoder - try multiple options in order of preference
            var decoder: Element? = null
            val decoderOptions = getDecoderOptions()

            for (decoderName in decoderOptions) {
                try {
                    decoder = ElementFactory.make(decoderName, "decoder")
                    if (decoder != null) {
                        selectedDecoder = decoderName
                        callback.onLog("INFO", "Using H264 decoder: $decoderName")

                        // Configure decoder-specific settings
                        when (decoderName) {
                            "avdec_h264" -> {
                                decoder.set("lowres", 0)
                                decoder.set("skip-frame", 0)
                                decoder.set("max-threads", Runtime.getRuntime().availableProcessors())
                            }
                            "nvh264dec", "nvdec" -> {
                                // NVIDIA decoders usually work with default settings
                                callback.onLog("INFO", "Hardware acceleration enabled (NVIDIA)")
                            }
                            "d3d11h264dec" -> {
                                callback.onLog("INFO", "Hardware acceleration enabled (Direct3D 11)")
                            }
                            "msdkh264dec" -> {
                                callback.onLog("INFO", "Hardware acceleration enabled (Intel Quick Sync)")
                            }
                            "decodebin" -> {
                                // decodebin will auto-negotiate the best decoder
                                callback.onLog("INFO", "Using automatic decoder selection")
                            }
                        }
                        break
                    }
                } catch (e: Exception) {
                    // Log and continue to next decoder option
                    callback.onLog("DEBUG", "Failed to create $decoderName: ${e.message}")
                }
            }

            if (decoder == null) {
                callback.onLog("ERROR", "Failed to create any H264 decoder. Please ensure GStreamer is properly installed with at least one of: gstreamer1.0-libav (for avdec_h264), gstreamer1.0-plugins-bad (for hardware decoders), or gstreamer1.0-plugins-good (for decodebin)")
                return
            }

            // Queue for buffering with optimized settings
            val queue = ElementFactory.make("queue", "queue")
            if (queue == null) {
                callback.onLog("ERROR", "Failed to create queue element - check GStreamer installation")
                return
            }
            queue.apply {
                set("leaky", 2) // Drop old buffers
                set("max-size-buffers", Constants.QUEUE_MAX_BUFFERS)
                set("max-size-time", Constants.QUEUE_MAX_TIME_NS)
                set("max-size-bytes", 0L)
            }

            // Skip video converter/scaler - test direct pipeline
            callback.onLog("INFO", "Using direct pipeline without color conversion")

            // Video sink - platform specific
            videosink = when {
                Platform.isLinux() -> {
                    callback.onLog("DEBUG", "Creating Linux video sink...")
                    ElementFactory.make("xvimagesink", "videosink") ?: run {
                        callback.onLog("DEBUG", "xvimagesink not available, trying ximagesink...")
                        ElementFactory.make("ximagesink", "videosink")
                    }
                }
                Platform.isWindows() -> {
                    ElementFactory.make("d3dvideosink", "videosink")
                        ?: ElementFactory.make("directdrawsink", "videosink")
                }
                Platform.isMac() -> {
                    ElementFactory.make("osxvideosink", "videosink")
                }
                else -> null
            }

            // Fallback to autovideosink
            if (videosink == null) {
                callback.onLog("WARN", "Platform-specific video sink not available, using autovideosink")
                videosink = ElementFactory.make("autovideosink", "videosink")
            }

            if (videosink == null) {
                callback.onLog("ERROR", "Failed to create any video sink - check GStreamer plugins installation")
                return
            }

            videosink?.apply {
                set("sync", false)
                set("async", false)
                // For xvimagesink/ximagesink on Linux, prevent initial centering issues
                if (Platform.isLinux() && name.contains("imagesink")) {
                    set("force-aspect-ratio", true)
                }
            }

            // Add elements to pipeline and link based on decoder type
            if (selectedDecoder == "decodebin") {
                // decodebin handles parsing internally
                pipeline?.addMany(appsrc, decoder, queue, videosink)

                // Link appsrc to decoder
                appsrc?.link(decoder)

                // decodebin uses dynamic pads, so we need to handle pad-added signal
                decoder.connect(Element.PAD_ADDED { element, pad ->
                    if (pad.name.startsWith("src")) {
                        val sinkPad = queue.getStaticPad("sink")
                        if (!sinkPad.isLinked) {
                            pad.link(sinkPad)
                            callback.onLog("DEBUG", "Linked decoder to queue")
                        }
                    }
                })

                // Link remaining elements
                queue.link(videosink)
            } else {
                // Standard pipeline with h264parse
                pipeline?.addMany(appsrc, h264parse, decoder, queue, videosink)
                Element.linkMany(appsrc, h264parse, decoder, queue, videosink)
            }

            // Set up bus for error handling
            val bus = pipeline?.bus
            bus?.connect(Bus.ERROR { _, source, message ->
                callback.onLog("ERROR", "Pipeline error: $message")
            })

            bus?.connect(Bus.WARNING { _, source, message ->
                callback.onLog("WARN", "Pipeline warning: $message")
            })

            // Store video component for later overlay setup
            if (videoComponent != null && videosink != null) {
                pendingVideoComponent = videoComponent
                callback.onLog("DEBUG", "Deferring video overlay setup until first frame")
            }

            // Configure pipeline for low latency
            pipeline?.apply {
                set("latency", 0L)
                set("delay", 0L)
            }

            // Start pipeline
            val ret = pipeline?.play()
            when (ret) {
                StateChangeReturn.FAILURE -> {
                    callback.onLog("ERROR", "Failed to start GStreamer pipeline")
                    pipeline?.state = State.NULL
                    pipeline?.dispose()
                    pipeline = null
                    appsrc = null
                    videosink = null
                }
                StateChangeReturn.NO_PREROLL -> {
                    callback.onLog("INFO", "GStreamer pipeline started (live source, no preroll)")
                }
                else -> {
                    callback.onLog("INFO", "GStreamer pipeline started successfully")
                }
            }
        } finally {
            pipelineLock.unlock()
        }
    }

    private fun getDecoderOptions(): Array<String> {
        return if (isAppImage) {
            // In AppImage, prefer software decoders for better compatibility
            arrayOf(
                "avdec_h264",      // FFmpeg/libav software decoder (most reliable in AppImage)
                "openh264dec",     // OpenH264 software decoder
                "decodebin"        // Auto-negotiating decoder (fallback)
            )
        } else {
            // Normal priority: hardware first
            arrayOf(
                "nvh264dec",       // NVIDIA hardware decoder (NVDEC)
                "nvdec",           // Newer NVIDIA decoder
                "d3d11h264dec",    // Windows Direct3D 11 hardware decoder
                "msdkh264dec",     // Intel Media SDK hardware decoder
                "vaapih264dec",    // VA-API hardware decoder (Linux)
                "vtdec_h264",      // macOS VideoToolbox hardware decoder
                "avdec_h264",      // FFmpeg/libav software decoder (most common)
                "openh264dec",     // OpenH264 software decoder
                "decodebin"        // Auto-negotiating decoder (fallback)
            )
        }
    }

    private fun setupVideoOverlay() {
        if (overlaySet || pendingVideoComponent == null || videosink == null) {
            return
        }

        try {
            // Ensure component is realized
            if (!pendingVideoComponent!!.isDisplayable) {
                callback.onLog("WARN", "Video component not yet displayable")
            }

            videoOverlay = VideoOverlay.wrap(videosink)

            // Get native window handle
            val windowHandle = when {
                Platform.isLinux() -> Native.getComponentID(pendingVideoComponent).toLong()
                Platform.isWindows() -> {
                    val p = Native.getComponentPointer(pendingVideoComponent)
                    Pointer.nativeValue(p)
                }
                Platform.isMac() -> Native.getComponentID(pendingVideoComponent).toLong()
                else -> 0L
            }

            if (windowHandle != 0L) {
                videoOverlay?.setWindowHandle(windowHandle)
                overlaySet = true
                callback.onLog("DEBUG", "Set video overlay window handle: $windowHandle")
            } else {
                callback.onLog("WARN", "Could not get native window handle for video component")
            }
        } catch (e: Exception) {
            callback.onLog("ERROR", "Failed to setup video overlay: ${e.message}")
        }
    }

    private fun acquireBuffer(size: Int): Buffer {
        // Try to get a buffer from the pool
        val buffer = bufferPool.poll()

        return if (buffer != null) {
            // GStreamer buffers are mutable, we can reuse any buffer
            poolHits.incrementAndGet()
            buffer
        } else {
            // Need to allocate a new buffer
            poolMisses.incrementAndGet()
            Buffer(minOf(size, Constants.MAX_BUFFER_SIZE))
        }
    }

    private fun releaseBuffer(buffer: Buffer?) {
        if (buffer != null && bufferPool.size < Constants.BUFFER_POOL_SIZE) {
            bufferPool.offer(buffer)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isActive(): Boolean {
        // Volatile read, no lock needed
        return pipeline != null && appsrc != null
    }

    fun pushVideoData(data: ByteBuffer) {
        // Fast path - volatile reads only, no lock
        if (!isActive() || !callback.isRunning()) {
            return
        }

        val dataSize = data.remaining()

        // Try lock with timeout to avoid blocking on the hot path
        if (!pipelineLock.tryLock()) {
            // Pipeline is busy, skip this frame rather than blocking
            return
        }

        var buffer: Buffer? = null
        try {
            // Double-check after acquiring lock
            val currentAppsrc = appsrc
            if (pipeline == null || currentAppsrc == null) {
                return
            }

            // Acquire and fill buffer while holding lock to ensure atomicity
            buffer = acquireBuffer(dataSize)

            // Map buffer directly - avoid intermediate ByteBuffer reference
            buffer.map(false).put(data)
            buffer.unmap()

            // Set start time if needed
            startTime.compareAndSet(0, System.nanoTime())

            // Push buffer to pipeline
            val ret = currentAppsrc.pushBuffer(buffer)
            when (ret) {
                FlowReturn.OK -> {
                    // Buffer successfully pushed, don't release it
                    buffer = null

                    val frames = frameCount.incrementAndGet()

                    // First keyframe handling - do minimal work
                    if (!hasReceivedKeyframe) {
                        hasReceivedKeyframe = true
                        if (pendingVideoComponent != null) {
                            setupVideoOverlay()
                        }
                    }

                    // Periodic logging - only if actually needed
                    if (frames % Constants.FRAME_LOG_INTERVAL == 0L) {
                        logFrameStats(frames)
                    }
                }
                FlowReturn.FLUSHING -> {
                    // Pipeline is flushing, normal during shutdown
                }
                else -> {
                    // Error case - but only log if still running
                    if (callback.isRunning()) {
                        callback.onLog("ERROR", "Error pushing buffer: $ret")
                    }
                }
            }
        } finally {
            pipelineLock.unlock()

            // Release buffer if not consumed (outside of lock)
            buffer?.let { releaseBuffer(it) }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun logFrameStats(frames: Long) {
        if (!callback.isRunning()) return

        val elapsedNs = System.nanoTime() - startTime.get()
        val elapsedSec = elapsedNs / 1e9
        val fps = frames / elapsedSec

        // Log buffer pool stats
        val hits = poolHits.get()
        val misses = poolMisses.get()
        val hitRate = if (hits + misses > 0) (hits * 100.0) / (hits + misses) else 0.0

        callback.onLog("DEBUG", String.format("%d frames, %.1f fps, pool hit rate: %.1f%%",
                                            frames, fps, hitRate))
    }

    fun stop() {
        pipelineLock.lock()
        try {
            pipeline?.let {
                it.stop()
                it.dispose()
                pipeline = null
                appsrc = null
                videosink = null
                videoOverlay = null
                selectedDecoder = null
            }
            frameCount.set(0)
            startTime.set(0)
            hasReceivedKeyframe = false
            pendingVideoComponent = null
            overlaySet = false

            // Clear buffer pool
            bufferPool.clear()

            // Log final buffer pool statistics
            val hits = poolHits.get()
            val misses = poolMisses.get()
            if (hits + misses > 0) {
                val hitRate = (hits * 100.0) / (hits + misses)
                callback.onLog("INFO", String.format("Buffer pool final stats: %.1f%% hit rate (%d hits, %d misses)",
                                                   hitRate, hits, misses))
            }
        } finally {
            pipelineLock.unlock()
        }
    }
}
