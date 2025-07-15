package potatoclient.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import static potatoclient.java.Constants.*;

public class WindowEventHandler {
    public interface EventCallback {
        void onWindowEvent(EventFilter.EventType type, String eventName, Map<String, Object> details);
        void onWindowClosing();
    }
    
    private final JFrame frame;
    private final EventCallback callback;
    private final EventFilter eventFilter;
    private final ScheduledExecutorService eventThrottleExecutor;
    
    // Window state tracking
    private volatile Point lastWindowLocation;
    private volatile Dimension lastWindowSize;
    private volatile int lastWindowState = JFrame.NORMAL;
    private final AtomicLong lastWindowEventTime = new AtomicLong(0);
    
    // Throttled event tasks
    private volatile ScheduledFuture<?> pendingWindowResizeTask;
    private volatile ScheduledFuture<?> pendingWindowMoveTask;
    private volatile ComponentEvent lastResizeEvent;
    private volatile ComponentEvent lastMoveEvent;
    
    public WindowEventHandler(JFrame frame, EventCallback callback, 
                            EventFilter eventFilter, ScheduledExecutorService eventThrottleExecutor) {
        this.frame = frame;
        this.callback = callback;
        this.eventFilter = eventFilter;
        this.eventThrottleExecutor = eventThrottleExecutor;
        
        // Initialize tracking variables
        this.lastWindowLocation = frame.getLocation();
        this.lastWindowSize = frame.getSize();
    }
    
    public void attachListeners() {
        // Add comprehensive window listeners
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendFilteredWindowEvent(EventFilter.EventType.WINDOW_CLOSING, "closing", null);
                callback.onWindowClosing();
            }
            
            @Override
            public void windowOpened(WindowEvent e) {
                sendFilteredWindowEvent(EventFilter.EventType.WINDOW_OPENED, "opened", null);
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
                sendFilteredWindowEvent(EventFilter.EventType.WINDOW_MINIMIZED, "minimized", null);
            }
            
            @Override
            public void windowDeiconified(WindowEvent e) {
                sendFilteredWindowEvent(EventFilter.EventType.WINDOW_RESTORED, "restored", null);
            }
            
            @Override
            public void windowActivated(WindowEvent e) {
                sendFilteredWindowEvent(EventFilter.EventType.WINDOW_FOCUSED, "focused", null);
            }
            
            @Override
            public void windowDeactivated(WindowEvent e) {
                sendFilteredWindowEvent(EventFilter.EventType.WINDOW_UNFOCUSED, "unfocused", null);
            }
        });
        
        // Window state listener for maximize/normal
        frame.addWindowStateListener(e -> {
            int oldState = e.getOldState();
            int newState = e.getNewState();
            
            if (oldState != newState) {
                Map<String, Object> details = new HashMap<>();
                details.put("oldState", getStateString(oldState));
                details.put("newState", getStateString(newState));
                
                if ((newState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_MAXIMIZED, "maximized", details);
                } else if ((oldState & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH && 
                           (newState & Frame.MAXIMIZED_BOTH) == 0) {
                    sendFilteredWindowEvent(EventFilter.EventType.WINDOW_UNMAXIMIZED, "unmaximized", details);
                }
                
                lastWindowState = newState;
            }
        });
        
        // Component listener for resize and move
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                lastResizeEvent = e;
                scheduleWindowEvent("resized", () -> {
                    if (lastResizeEvent != null && frame != null) {
                        Dimension newSize = frame.getSize();
                        if (lastWindowSize == null || !newSize.equals(lastWindowSize)) {
                            Map<String, Object> details = new HashMap<>();
                            details.put("width", newSize.width);
                            details.put("height", newSize.height);
                            if (lastWindowSize != null) {
                                details.put("oldWidth", lastWindowSize.width);
                                details.put("oldHeight", lastWindowSize.height);
                            }
                            sendFilteredWindowEvent(EventFilter.EventType.WINDOW_RESIZED, "resized", details);
                            lastWindowSize = newSize;
                        }
                    }
                }, pendingWindowResizeTask, task -> pendingWindowResizeTask = task);
            }
            
            @Override
            public void componentMoved(ComponentEvent e) {
                lastMoveEvent = e;
                scheduleWindowEvent("moved", () -> {
                    if (lastMoveEvent != null && frame != null) {
                        Point newLocation = frame.getLocation();
                        if (lastWindowLocation == null || !newLocation.equals(lastWindowLocation)) {
                            Map<String, Object> details = new HashMap<>();
                            details.put("x", newLocation.x);
                            details.put("y", newLocation.y);
                            if (lastWindowLocation != null) {
                                details.put("oldX", lastWindowLocation.x);
                                details.put("oldY", lastWindowLocation.y);
                            }
                            sendFilteredWindowEvent(EventFilter.EventType.WINDOW_MOVED, "moved", details);
                            lastWindowLocation = newLocation;
                        }
                    }
                }, pendingWindowMoveTask, task -> pendingWindowMoveTask = task);
            }
        });
    }
    
    private void scheduleWindowEvent(String eventType, Runnable task, ScheduledFuture<?> currentTask, 
                                   Consumer<ScheduledFuture<?>> taskSetter) {
        long now = System.currentTimeMillis();
        long lastTime = lastWindowEventTime.get();
        
        if (now - lastTime >= WINDOW_EVENT_THROTTLE_MS) {
            if (lastWindowEventTime.compareAndSet(lastTime, now)) {
                task.run();
                // Cancel any pending task
                if (currentTask != null) {
                    currentTask.cancel(false);
                }
            }
        } else {
            // Cancel existing task and schedule new one
            if (currentTask != null) {
                currentTask.cancel(false);
            }
            
            long delay = WINDOW_EVENT_THROTTLE_MS - (now - lastTime);
            var newTask = eventThrottleExecutor.schedule(() -> {
                task.run();
                lastWindowEventTime.set(System.currentTimeMillis());
            }, delay, TimeUnit.MILLISECONDS);
            taskSetter.accept(newTask);
        }
    }
    
    private String getStateString(int state) {
        var sb = new StringBuilder(32);
        if ((state & Frame.NORMAL) == Frame.NORMAL || state == 0) sb.append("normal,");
        if ((state & Frame.ICONIFIED) == Frame.ICONIFIED) sb.append("minimized,");
        if ((state & Frame.MAXIMIZED_HORIZ) == Frame.MAXIMIZED_HORIZ) sb.append("maximized_horizontal,");
        if ((state & Frame.MAXIMIZED_VERT) == Frame.MAXIMIZED_VERT) sb.append("maximized_vertical,");
        if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) sb.append("maximized,");
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }
    
    private void sendFilteredWindowEvent(EventFilter.EventType type, String eventName, Map<String, Object> details) {
        if (!eventFilter.isFiltered(type)) {
            callback.onWindowEvent(type, eventName, details);
        }
    }
    
    public void cleanup() {
        if (pendingWindowResizeTask != null) {
            pendingWindowResizeTask.cancel(false);
        }
        if (pendingWindowMoveTask != null) {
            pendingWindowMoveTask.cancel(false);
        }
    }
    
    public Point getLastWindowLocation() {
        return lastWindowLocation;
    }
    
    public Dimension getLastWindowSize() {
        return lastWindowSize;
    }
    
    public int getLastWindowState() {
        return lastWindowState;
    }
}