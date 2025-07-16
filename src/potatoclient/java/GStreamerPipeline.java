package potatoclient.java;

import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.*;
import org.freedesktop.gstreamer.message.*;
import org.freedesktop.gstreamer.interfaces.VideoOverlay;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import java.awt.Component;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import static potatoclient.java.Constants.*;

public class GStreamerPipeline {
    public interface EventCallback {
        void onLog(String level, String message);
        void onPipelineError(String message);
        boolean isRunning();
    }
    
    private final String streamId;
    private final EventCallback callback;
    private final ReentrantLock pipelineLock = new ReentrantLock();
    
    private volatile Pipeline pipeline;
    private volatile AppSrc appsrc;
    private volatile Element videosink;
    private volatile VideoOverlay videoOverlay;
    private volatile String selectedDecoder;
    private volatile boolean isAppImage = false;
    private volatile String gstreamerVersion = null;
    
    private final AtomicLong frameCount = new AtomicLong(0);
    private final AtomicLong startTime = new AtomicLong(0);
    private volatile boolean hasReceivedKeyframe = false;
    private volatile Component pendingVideoComponent = null;
    private volatile boolean overlaySet = false;
    
    // Buffer pool for zero-allocation streaming
    private final Queue<Buffer> bufferPool = new ConcurrentLinkedQueue<>();
    private final AtomicLong poolHits = new AtomicLong(0);
    private final AtomicLong poolMisses = new AtomicLong(0);
    
    public GStreamerPipeline(String streamId, EventCallback callback) {
        this.streamId = streamId;
        this.callback = callback;
    }
    
    public void initialize(Component videoComponent) {
        pipelineLock.lock();
        try {
            // Configure Windows paths if needed
            if (Platform.isWindows()) {
                GStreamerUtils.configureGStreamerPaths(new GStreamerUtils.EventCallback() {
                    public void onLog(String level, String message) {
                        callback.onLog(level, message);
                    }
                });
            }
            
            // Initialize GStreamer if needed
            if (!Gst.isInitialized()) {
                callback.onLog("INFO", "Initializing GStreamer...");
                
                // Check for plugin path from environment or system property
                String gstPluginPath = System.getenv("GST_PLUGIN_PATH_1_0");
                if (gstPluginPath == null || gstPluginPath.isEmpty()) {
                    gstPluginPath = System.getenv("GST_PLUGIN_PATH");
                }
                if (gstPluginPath == null || gstPluginPath.isEmpty()) {
                    gstPluginPath = System.getProperty("gstreamer.plugin.path");
                }
                
                if (gstPluginPath != null && !gstPluginPath.isEmpty()) {
                    callback.onLog("DEBUG", "Found GST plugin path: " + gstPluginPath);
                }
                
                try {
                    // Initialize with plugin path if available
                    if (gstPluginPath != null && !gstPluginPath.isEmpty()) {
                        String[] args = new String[] {
                            "--gst-plugin-path=" + gstPluginPath
                        };
                        Gst.init(GSTREAMER_APP_NAME, args);
                    } else {
                        Gst.init(GSTREAMER_APP_NAME, new String[]{});
                    }
                    gstreamerVersion = Gst.getVersionString();
                    callback.onLog("INFO", "GStreamer initialized successfully. Version: " + gstreamerVersion);
                    
                    // Check if running in AppImage
                    String appDir = System.getenv("APPDIR");
                    isAppImage = appDir != null && !appDir.isEmpty();
                    if (isAppImage) {
                        callback.onLog("INFO", "Running in AppImage environment");
                    }
                    
                    // Force plugin registry update
                    Registry registry = Registry.get();
                    if (registry != null && gstPluginPath != null) {
                        callback.onLog("DEBUG", "Scanning plugin path: " + gstPluginPath);
                        try {
                            registry.scanPath(gstPluginPath);
                        } catch (Exception e) {
                            callback.onLog("DEBUG", "Plugin path scan warning: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    callback.onLog("ERROR", "Failed to initialize GStreamer: " + e.getMessage());
                    callback.onPipelineError("GStreamer init error: " + e.getMessage());
                    return;
                }
            }
            
            // Log GStreamer environment
            callback.onLog("DEBUG", "GST_PLUGIN_PATH: " + System.getenv("GST_PLUGIN_PATH"));
            callback.onLog("DEBUG", "GST_PLUGIN_PATH_1_0: " + System.getenv("GST_PLUGIN_PATH_1_0"));
            callback.onLog("DEBUG", "GST_PLUGIN_SYSTEM_PATH_1_0: " + System.getenv("GST_PLUGIN_SYSTEM_PATH_1_0"));
            callback.onLog("DEBUG", "GST_PLUGIN_SCANNER: " + System.getenv("GST_PLUGIN_SCANNER"));
            callback.onLog("DEBUG", "GST_PLUGIN_SCANNER_1_0: " + System.getenv("GST_PLUGIN_SCANNER_1_0"));
            callback.onLog("DEBUG", "LD_LIBRARY_PATH: " + System.getenv("LD_LIBRARY_PATH"));
            callback.onLog("DEBUG", "APPDIR: " + System.getenv("APPDIR"));
            callback.onLog("DEBUG", "Java library path: " + System.getProperty("java.library.path"));
            
            // Create pipeline elements
            pipeline = new Pipeline("video-pipeline");
            
            // AppSrc for receiving video data
            appsrc = new AppSrc("appsrc");
            appsrc.set("is-live", true);
            appsrc.set("format", Format.TIME);
            appsrc.set("caps", Caps.fromString(H264_CAPS));
            appsrc.set("max-bytes", 0L);
            appsrc.set("block", false);
            appsrc.set("emit-signals", false);
            appsrc.set("min-latency", 0L);
            appsrc.set("max-latency", 0L);
            
            // H264 parser
            Element h264parse = ElementFactory.make("h264parse", "h264parse");
            if (h264parse == null) {
                callback.onLog("ERROR", "Failed to create h264parse element - check if gstreamer1.0-plugins-bad is installed");
                return;
            }
            h264parse.set("config-interval", 1);
            
            // H264 decoder - try multiple options in order of preference
            // Priority: Hardware decoders > Software decoders
            Element decoder = null;
            String[] decoderOptions;
            
            if (isAppImage) {
                // In AppImage, prefer software decoders for better compatibility
                decoderOptions = new String[] {
                    "avdec_h264",      // FFmpeg/libav software decoder (most reliable in AppImage)
                    "openh264dec",     // OpenH264 software decoder
                    "decodebin"        // Auto-negotiating decoder (fallback)
                };
            } else {
                // Normal priority: hardware first
                decoderOptions = new String[] {
                    "nvh264dec",       // NVIDIA hardware decoder (NVDEC)
                    "nvdec",           // Newer NVIDIA decoder
                    "d3d11h264dec",    // Windows Direct3D 11 hardware decoder
                    "msdkh264dec",     // Intel Media SDK hardware decoder
                    "vaapih264dec",    // VA-API hardware decoder (Linux)
                    "vtdec_h264",      // macOS VideoToolbox hardware decoder
                    "avdec_h264",      // FFmpeg/libav software decoder (most common)
                    "openh264dec",     // OpenH264 software decoder
                    "decodebin"        // Auto-negotiating decoder (fallback)
                };
            }
            
            for (String decoderName : decoderOptions) {
                try {
                    decoder = ElementFactory.make(decoderName, "decoder");
                    if (decoder != null) {
                        selectedDecoder = decoderName;
                        callback.onLog("INFO", "Using H264 decoder: " + decoderName);
                        
                        // Configure decoder-specific settings
                        switch (decoderName) {
                            case "avdec_h264":
                                decoder.set("lowres", 0);
                                decoder.set("skip-frame", 0);
                                decoder.set("max-threads", Runtime.getRuntime().availableProcessors());
                                break;
                            case "nvh264dec":
                            case "nvdec":
                                // NVIDIA decoders usually work with default settings
                                callback.onLog("INFO", "Hardware acceleration enabled (NVIDIA)");
                                break;
                            case "d3d11h264dec":
                                callback.onLog("INFO", "Hardware acceleration enabled (Direct3D 11)");
                                break;
                            case "msdkh264dec":
                                callback.onLog("INFO", "Hardware acceleration enabled (Intel Quick Sync)");
                                break;
                            case "decodebin":
                                // decodebin will auto-negotiate the best decoder
                                callback.onLog("INFO", "Using automatic decoder selection");
                                break;
                        }
                        break;
                    }
                } catch (Exception e) {
                    // Log and continue to next decoder option
                    callback.onLog("DEBUG", "Failed to create " + decoderName + ": " + e.getMessage());
                }
            }
            
            if (decoder == null) {
                callback.onLog("ERROR", "Failed to create any H264 decoder. Please ensure GStreamer is properly installed with at least one of: gstreamer1.0-libav (for avdec_h264), gstreamer1.0-plugins-bad (for hardware decoders), or gstreamer1.0-plugins-good (for decodebin)");
                return;
            }
            
            // Queue for buffering with optimized settings
            Element queue = ElementFactory.make("queue", "queue");
            if (queue == null) {
                callback.onLog("ERROR", "Failed to create queue element - check GStreamer installation");
                return;
            }
            queue.set("leaky", 2); // Drop old buffers
            queue.set("max-size-buffers", QUEUE_MAX_BUFFERS);
            queue.set("max-size-time", QUEUE_MAX_TIME_NS);
            queue.set("max-size-bytes", 0L);
            
            
            // Skip video converter/scaler - test direct pipeline
            callback.onLog("INFO", "Using direct pipeline without color conversion");
            
            // Video sink - platform specific
            if (Platform.isLinux()) {
                callback.onLog("DEBUG", "Creating Linux video sink...");
                videosink = ElementFactory.make("xvimagesink", "videosink");
                if (videosink == null) {
                    callback.onLog("DEBUG", "xvimagesink not available, trying ximagesink...");
                    videosink = ElementFactory.make("ximagesink", "videosink");
                }
            } else if (Platform.isWindows()) {
                videosink = ElementFactory.make("d3dvideosink", "videosink");
                if (videosink == null) {
                    videosink = ElementFactory.make("directdrawsink", "videosink");
                }
            } else if (Platform.isMac()) {
                videosink = ElementFactory.make("osxvideosink", "videosink");
            }
            
            // Fallback to autovideosink
            if (videosink == null) {
                callback.onLog("WARN", "Platform-specific video sink not available, using autovideosink");
                videosink = ElementFactory.make("autovideosink", "videosink");
            }
            
            if (videosink == null) {
                callback.onLog("ERROR", "Failed to create any video sink - check GStreamer plugins installation");
                return;
            }
            
            videosink.set("sync", false);
            videosink.set("async", false);
            // For xvimagesink/ximagesink on Linux, prevent initial centering issues
            if (Platform.isLinux() && videosink.getName().contains("imagesink")) {
                videosink.set("force-aspect-ratio", true);
            }
            
            // Add elements to pipeline and link based on decoder type
            if ("decodebin".equals(selectedDecoder)) {
                // decodebin handles parsing internally
                pipeline.addMany(appsrc, decoder, queue, videosink);
                
                // Link appsrc to decoder
                appsrc.link(decoder);
                
                // decodebin uses dynamic pads, so we need to handle pad-added signal
                decoder.connect(new Element.PAD_ADDED() {
                    @Override
                    public void padAdded(Element element, Pad pad) {
                        if (pad.getName().startsWith("src")) {
                            Pad sinkPad = queue.getStaticPad("sink");
                            if (!sinkPad.isLinked()) {
                                pad.link(sinkPad);
                                callback.onLog("DEBUG", "Linked decoder to queue");
                            }
                        }
                    }
                });
                
                // Link remaining elements
                queue.link(videosink);
            } else {
                // Standard pipeline with h264parse
                pipeline.addMany(appsrc, h264parse, decoder, queue, videosink);
                Element.linkMany(appsrc, h264parse, decoder, queue, videosink);
            }
            
            // Set up bus for error handling
            Bus bus = pipeline.getBus();
            bus.connect(new Bus.ERROR() {
                public void errorMessage(GstObject source, int code, String message) {
                    callback.onLog("ERROR", "Pipeline error: " + message);
                }
            });
            
            bus.connect(new Bus.WARNING() {
                public void warningMessage(GstObject source, int code, String message) {
                    callback.onLog("WARN", "Pipeline warning: " + message);
                }
            });
            
            // Store video component for later overlay setup
            if (videoComponent != null && videosink != null) {
                pendingVideoComponent = videoComponent;
                callback.onLog("DEBUG", "Deferring video overlay setup until first frame");
            }
            
            // Configure pipeline for low latency
            pipeline.set("latency", 0L);
            pipeline.set("delay", 0L);
            
            // Start pipeline
            StateChangeReturn ret = pipeline.play();
            if (ret == StateChangeReturn.FAILURE) {
                callback.onLog("ERROR", "Failed to start GStreamer pipeline");
                pipeline.setState(State.NULL);
                pipeline.dispose();
                pipeline = null;
                appsrc = null;
                videosink = null;
                return;
            } else if (ret == StateChangeReturn.NO_PREROLL) {
                callback.onLog("INFO", "GStreamer pipeline started (live source, no preroll)");
            } else {
                callback.onLog("INFO", "GStreamer pipeline started successfully");
            }
        } finally {
            pipelineLock.unlock();
        }
    }
    
    private void setupVideoOverlay() {
        if (overlaySet || pendingVideoComponent == null || videosink == null) {
            return;
        }
        
        try {
            // Ensure component is realized
            if (!pendingVideoComponent.isDisplayable()) {
                callback.onLog("WARN", "Video component not yet displayable");
            }
            
            videoOverlay = VideoOverlay.wrap(videosink);
            
            // Get native window handle
            long windowHandle = 0;
            if (Platform.isLinux()) {
                windowHandle = Native.getComponentID(pendingVideoComponent);
            } else if (Platform.isWindows()) {
                Pointer p = Native.getComponentPointer(pendingVideoComponent);
                windowHandle = Pointer.nativeValue(p);
            } else if (Platform.isMac()) {
                windowHandle = Native.getComponentID(pendingVideoComponent);
            }
            
            if (windowHandle != 0) {
                videoOverlay.setWindowHandle(windowHandle);
                overlaySet = true;
                callback.onLog("DEBUG", "Set video overlay window handle: " + windowHandle);
            } else {
                callback.onLog("WARN", "Could not get native window handle for video component");
            }
        } catch (Exception e) {
            callback.onLog("ERROR", "Failed to setup video overlay: " + e.getMessage());
        }
    }
    
    private Buffer acquireBuffer(int size) {
        // Try to get a buffer from the pool
        Buffer buffer = bufferPool.poll();
        
        if (buffer != null) {
            // GStreamer buffers are mutable, we can reuse any buffer
            poolHits.incrementAndGet();
            return buffer;
        }
        
        // Need to allocate a new buffer
        poolMisses.incrementAndGet();
        
        return new Buffer(Math.min(size, MAX_BUFFER_SIZE));
    }
    
    private void releaseBuffer(Buffer buffer) {
        if (buffer != null && bufferPool.size() < BUFFER_POOL_SIZE) {
            bufferPool.offer(buffer);
        }
    }
    
    public void pushVideoData(ByteBuffer data) {
        // Fast path - check without lock
        if (pipeline == null || appsrc == null || !callback.isRunning()) {
            return;
        }
        
        int dataSize = data.remaining();
        Buffer buffer = null;
        
        try {
            // Acquire buffer before taking lock
            buffer = acquireBuffer(dataSize);
            
            // Map and copy data outside of lock
            ByteBuffer bb = buffer.map(false);
            bb.put(data);
            buffer.unmap();
            
            // Only lock for the actual push operation
            pipelineLock.lock();
            try {
                if (pipeline == null || appsrc == null) {
                    return;
                }
                
                startTime.compareAndSet(0, System.nanoTime());
                
                // Push buffer to pipeline
                FlowReturn ret = appsrc.pushBuffer(buffer);
                if (ret != FlowReturn.OK && ret != FlowReturn.FLUSHING) {
                    if (callback.isRunning()) {
                        callback.onLog("ERROR", "Error pushing buffer: " + ret);
                    }
                    return;
                }
                
                // Buffer successfully pushed, don't release it
                buffer = null;
                
                long frames = frameCount.incrementAndGet();
                
                // Mark that we've received the first keyframe
                if (!hasReceivedKeyframe) {
                    hasReceivedKeyframe = true;
                    callback.onLog("DEBUG", "Received first keyframe");
                    
                    // Setup video overlay after first frame
                    setupVideoOverlay();
                }
                
                if (pendingVideoComponent != null && !overlaySet) {
                    setupVideoOverlay();
                }
                
                if (frames % FRAME_LOG_INTERVAL == 0 && callback.isRunning()) {
                    long elapsedNs = System.nanoTime() - startTime.get();
                    double elapsedSec = elapsedNs / 1e9;
                    double fps = frames / elapsedSec;
                    
                    // Log buffer pool stats
                    long hits = poolHits.get();
                    long misses = poolMisses.get();
                    double hitRate = hits + misses > 0 ? (hits * 100.0) / (hits + misses) : 0;
                    
                    callback.onLog("DEBUG", String.format("%d frames, %.1f fps, pool hit rate: %.1f%%", 
                                                        frames, fps, hitRate));
                }
            } finally {
                pipelineLock.unlock();
            }
        } finally {
            // Release buffer if not consumed
            if (buffer != null) {
                releaseBuffer(buffer);
            }
        }
    }
    
    public void stop() {
        pipelineLock.lock();
        try {
            if (pipeline != null) {
                pipeline.stop();
                pipeline.dispose();
                pipeline = null;
                appsrc = null;
                videosink = null;
                videoOverlay = null;
                selectedDecoder = null;
            }
            frameCount.set(0);
            startTime.set(0);
            hasReceivedKeyframe = false;
            pendingVideoComponent = null;
            overlaySet = false;
            
            // Clear buffer pool
            bufferPool.clear();
            
            // Log final buffer pool statistics
            long hits = poolHits.get();
            long misses = poolMisses.get();
            if (hits + misses > 0) {
                double hitRate = (hits * 100.0) / (hits + misses);
                callback.onLog("INFO", String.format("Buffer pool final stats: %.1f%% hit rate (%d hits, %d misses)", 
                                                   hitRate, hits, misses));
            }
        } finally {
            pipelineLock.unlock();
        }
    }
    
    public boolean isActive() {
        return pipeline != null;
    }
    
    public long getFrameCount() {
        return frameCount.get();
    }
    
    public boolean hasReceivedFirstKeyframe() {
        return hasReceivedKeyframe;
    }
}