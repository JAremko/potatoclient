package potatoclient.java;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.PrintStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import static potatoclient.java.Constants.*;

public class VideoStreamManager implements MouseEventHandler.EventCallback, 
                                         WindowEventHandler.EventCallback,
                                         WebSocketManager.EventCallback,
                                         GStreamerPipeline.EventCallback,
                                         FrameManager.FrameEventListener {
    
    // Immutable fields
    private final String streamId;
    private final String streamUrl;
    private final Scanner scanner;
    private final ObjectMapper mapper = new ObjectMapper(); // For command parsing
    
    // Thread-safe primitives
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    // Threading and synchronization
    private Thread commandThread;
    private Thread shutdownHook;
    
    // Executor services
    private final ScheduledExecutorService reconnectExecutor;
    private final ScheduledExecutorService eventThrottleExecutor;
    
    // Module instances
    private final EventFilter eventFilter;
    private final MessageProtocol messageProtocol;
    private final FrameManager frameManager;
    private MouseEventHandler mouseEventHandler;
    private WindowEventHandler windowEventHandler;
    private WebSocketManager webSocketManager;
    private GStreamerPipeline gstreamerPipeline;
    
    public VideoStreamManager(String streamId, String streamUrl) {
        this.streamId = streamId;
        this.streamUrl = streamUrl;
        this.scanner = new Scanner(System.in);
        this.eventFilter = new EventFilter();
        this.messageProtocol = new MessageProtocol(streamId);
        this.frameManager = new FrameManager(streamId, this);
        
        this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "VideoStream-Reconnect-" + streamId);
            t.setDaemon(true);
            return t;
        });
        this.eventThrottleExecutor = Executors.newScheduledThreadPool(EVENT_THROTTLE_POOL_SIZE, r -> {
            Thread t = new Thread(r, "VideoStream-EventThrottle-" + streamId);
            t.setDaemon(true);
            return t;
        });
        
        // Initialize modules
        this.webSocketManager = new WebSocketManager(streamUrl, this, reconnectExecutor);
        this.gstreamerPipeline = new GStreamerPipeline(streamId, this);
    }
    
    private void redirectSystemErr() {
        // Keep reference to original err for fallback
        final PrintStream originalErr = System.err;
        
        System.setErr(new PrintStream(System.err) {
            private final StringBuilder buffer = new StringBuilder(STRING_BUILDER_INITIAL_SIZE);
            
            @Override
            public void println(String x) {
                if (x != null && !x.trim().isEmpty()) {
                    String trimmed = x.trim();
                    
                    // Filter out known GStreamer warnings
                    if (trimmed.contains("gst_video_center_rect: assertion 'src->h != 0' failed") ||
                        (trimmed.contains("g_object_unref: assertion") && trimmed.contains("G_IS_OBJECT"))) {
                        originalErr.println(x);
                        return;
                    }
                    
                    messageProtocol.sendLog("STDERR", trimmed);
                    originalErr.println(x);
                }
            }
            
            @Override
            public void println(Object x) {
                if (x != null) {
                    String str = String.valueOf(x).trim();
                    
                    // Skip empty lines
                    if (str.isEmpty()) {
                        originalErr.println(x);
                        return;
                    }
                    
                    // Filter out known GStreamer warnings
                    if (str.contains("gst_video_center_rect: assertion 'src->h != 0' failed") ||
                        (str.contains("g_object_unref: assertion") && str.contains("G_IS_OBJECT"))) {
                        originalErr.println(x);
                        return;
                    }
                    
                    messageProtocol.sendLog("STDERR", str);
                    originalErr.println(x);
                }
            }
            
            @Override
            public void write(byte[] buf, int off, int len) {
                String msg = new String(buf, off, len);
                buffer.append(msg);
                
                // Process complete lines
                int lastNewline = buffer.lastIndexOf("\n");
                if (lastNewline >= 0) {
                    String lines = buffer.substring(0, lastNewline);
                    buffer.delete(0, lastNewline + 1);
                    
                    for (String line : lines.split("\n")) {
                        String trimmedLine = line.trim();
                        
                        // Skip empty lines or lines with only whitespace
                        if (trimmedLine.isEmpty()) {
                            continue;
                        }
                        
                        // Filter out known GStreamer warnings that aren't useful
                        if (trimmedLine.contains("gst_video_center_rect: assertion 'src->h != 0' failed")) {
                            // This warning happens when window is resized/moved, not useful
                            continue;
                        }
                        
                        // Filter out other common GStreamer noise
                        if (trimmedLine.contains("g_object_unref: assertion") && 
                            trimmedLine.contains("G_IS_OBJECT")) {
                            // Common during pipeline shutdown
                            continue;
                        }
                        
                        messageProtocol.sendLog("STDERR", trimmedLine);
                    }
                }
                
                originalErr.write(buf, off, len);
            }
        });
    }
    
    public void run() {
        // Redirect System.err to capture all exceptions
        redirectSystemErr();
        
        // Add minimal shutdown hook
        shutdownHook = new Thread(() -> {
            running.set(false);
            try {
                messageProtocol.sendResponse("shutdown", null);
            } catch (Exception ignored) {}
        }, "VideoStream-Shutdown-" + streamId);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        
        // Start command reader thread
        commandThread = new Thread(this::readCommands, "VideoStream-Commands-" + streamId);
        commandThread.setDaemon(true);
        commandThread.start();
        
        // Main event loop
        while (running.get()) {
            try {
                Thread.sleep(COMMAND_THREAD_SLEEP_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                messageProtocol.sendException("Main thread interrupted", e);
                break;
            }
        }
        
        // Exit when main loop ends
        System.exit(0);
    }
    
    private void readCommands() {
        try {
            while (running.get() && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                
                try {
                    Map<String, String> cmd = mapper.readValue(line, mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
                    handleCommand(cmd);
                } catch (Exception e) {
                    if (running.get()) {
                        messageProtocol.sendException("Failed to parse command: " + line, e);
                    }
                }
            }
            
            // If we exit the loop because stdin was closed (parent process died)
            if (running.get()) {
                System.err.println("Parent process stdin closed, shutting down");
                shutdown();
            }
        } catch (Exception e) {
            if (running.get()) {
                messageProtocol.sendException("Command reader error", e);
            }
        }
    }
    
    private void handleCommand(Map<String, String> cmd) {
        var action = cmd.get("action");
        switch (action) {
            case "show" -> show();
            case "shutdown" -> shutdown();
            default -> messageProtocol.sendLog("WARN", "Unknown command: " + action);
        }
    }
    
    private void show() {
        // Create and show frame
        frameManager.createFrame();
        
        // Wait for frame creation to complete
        try {
            SwingUtilities.invokeAndWait(() -> {
                frameManager.showFrame();
            });
        } catch (Exception e) {
            messageProtocol.sendException("Failed to create/show frame", e);
            return;
        }
        
        // Initialize pipeline and connect WebSocket
        SwingUtilities.invokeLater(() -> {
            if (frameManager.getVideoComponent() != null) {
                gstreamerPipeline.initialize(frameManager.getVideoComponent());
                webSocketManager.connect();
            }
        });
        
        messageProtocol.sendResponse("shown", null);
    }
    
    
    private void shutdown() {
        if (!running.compareAndSet(true, false)) {
            return; // Already shutting down
        }
        
        messageProtocol.sendLog("INFO", "Shutting down stream " + streamId);
        
        // Careful cleanup to avoid hardware glitches
        try {
            // 1. Stop accepting new frames first
            webSocketManager.disconnect();
            
            // 2. Let pipeline finish processing current frames
            Thread.sleep(100);
            
            // 3. Stop the pipeline properly
            gstreamerPipeline.stop();
            
            // 4. Hide and dispose frame
            SwingUtilities.invokeAndWait(() -> {
                frameManager.hideFrame();
                frameManager.disposeFrame();
            });
            
            // 5. Cleanup event handlers
            if (mouseEventHandler != null) {
                mouseEventHandler.cleanup();
            }
            if (windowEventHandler != null) {
                windowEventHandler.cleanup();
            }
            
            // 6. Shutdown executors
            shutdownExecutors();
            
            // 7. Send shutdown response
            messageProtocol.sendResponse("shutdown", null);
            
            // 8. Give time for response to be sent
            Thread.sleep(50);
            
        } catch (Exception e) {
            messageProtocol.sendLog("WARN", "Error during shutdown: " + e.getMessage());
        }
        
        // Exit cleanly
        System.exit(0);
    }
    
    private void shutdownExecutors() {
        reconnectExecutor.shutdown();
        eventThrottleExecutor.shutdown();
        try {
            if (!reconnectExecutor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                reconnectExecutor.shutdownNow();
            }
            if (!eventThrottleExecutor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                eventThrottleExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            reconnectExecutor.shutdownNow();
            eventThrottleExecutor.shutdownNow();
        }
    }
    
    // FrameManager.FrameEventListener implementation
    @Override
    public void onFrameCreated(JFrame frame, Component videoComponent) {
        // Initialize event handlers
        mouseEventHandler = new MouseEventHandler(videoComponent, this, eventFilter, eventThrottleExecutor);
        mouseEventHandler.attachListeners();
        
        windowEventHandler = new WindowEventHandler(frame, this, eventFilter, eventThrottleExecutor);
        windowEventHandler.attachListeners();
    }
    
    @Override
    public void onFrameClosing() {
        try {
            messageProtocol.sendResponse("window-closed", null);
            // Don't call shutdown() here - let the Clojure side handle it
            // This ensures consistent behavior with button clicks
        } catch (Exception e) {
            // If we can't send the message (parent process is dead), shutdown directly
            System.err.println("Parent process appears to be dead, shutting down directly");
            shutdown();
        }
    }
    
    private void cleanup() {
        shutdown();
    }
    
    // MouseEventHandler.EventCallback implementation
    @Override
    public void onNavigationEvent(EventFilter.EventType type, String eventName, int x, int y, Map<String, Object> details) {
        if (frameManager.getVideoComponent() != null && running.get()) {
            messageProtocol.sendNavigationEvent(eventName, x, y, frameManager.getVideoComponent(), details);
        }
    }
    
    // WindowEventHandler.EventCallback implementation
    @Override
    public void onWindowEvent(EventFilter.EventType type, String eventName, Map<String, Object> details) {
        if (running.get()) {
            messageProtocol.sendWindowEvent(eventName, details);
        }
    }
    
    @Override
    public void onWindowClosing() {
        try {
            messageProtocol.sendResponse("window-closed", null);
            // Don't call shutdown() here - let the Clojure side handle it
            // This ensures consistent behavior with button clicks
        } catch (Exception e) {
            // If we can't send the message (parent process is dead), shutdown directly
            System.err.println("Parent process appears to be dead, shutting down directly");
            shutdown();
        }
    }
    
    // WebSocketManager.EventCallback implementation
    @Override
    public void onConnected(String url) {
        messageProtocol.sendLog("INFO", "WebSocket connected to " + url);
    }
    
    @Override
    public void onDisconnected(int code, String reason, boolean remote) {
        messageProtocol.sendLog("INFO", String.format("WebSocket closed: %s (code: %d, remote: %s)", 
                reason, code, remote));
    }
    
    @Override
    public void onError(String message) {
        messageProtocol.sendLog("ERROR", "WebSocket error: " + message);
    }
    
    @Override
    public void onVideoData(ByteBuffer data) {
        gstreamerPipeline.pushVideoData(data);
    }
    
    // GStreamerPipeline.EventCallback implementation
    @Override
    public void onLog(String level, String message) {
        messageProtocol.sendLog(level, message);
    }
    
    @Override
    public void onPipelineError(String message) {
        messageProtocol.sendException("Pipeline error", new Exception(message));
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: VideoStreamManager <streamId> <url>");
            System.exit(1);
        }
        
        // Configure GStreamer paths on Windows before creating the manager
        GStreamerUtils.configureGStreamerPaths((level, message) -> {
            System.err.println("[" + level + "] " + message);
        });
        
        VideoStreamManager manager = new VideoStreamManager(args[0], args[1]);
        manager.run();
    }
}