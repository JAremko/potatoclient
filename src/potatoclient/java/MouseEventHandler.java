package potatoclient.java;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import static potatoclient.java.Constants.*;

public class MouseEventHandler {
    // Click caching for double-click detection
    private record ClickInfo(int x, int y, int button, int modifiers, long timestamp) {
        boolean isDoubleClick(int x, int y, int button, long timestamp) {
            // Check if it's a double click (same button, close position, within time window)
            return this.button == button &&
                   Math.abs(this.x - x) <= DOUBLE_CLICK_MAX_DISTANCE &&
                   Math.abs(this.y - y) <= DOUBLE_CLICK_MAX_DISTANCE &&
                   (timestamp - this.timestamp) <= DOUBLE_CLICK_WINDOW_MS;
        }
    }
    
    public interface EventCallback {
        void onNavigationEvent(EventFilter.EventType type, String eventName, int x, int y, Map<String, Object> details);
    }
    
    // Constants are imported from Constants class
    
    private final EventCallback callback;
    private final EventFilter eventFilter;
    private final ScheduledExecutorService eventThrottleExecutor;
    private final Component videoComponent;
    
    // State tracking
    private final AtomicReference<ClickInfo> lastClick = new AtomicReference<>();
    private volatile ScheduledFuture<?> pendingClickTask;
    private volatile ScheduledFuture<?> pendingMouseMoveTask;
    private volatile ScheduledFuture<?> pendingMouseDragTask;
    private volatile MouseEvent lastMouseMoveEvent;
    private volatile MouseEvent lastMouseDragEvent;
    private final AtomicLong lastMoveTime = new AtomicLong(0);
    private final AtomicLong lastDragTime = new AtomicLong(0);
    private final AtomicBoolean isDragging = new AtomicBoolean(false);
    private volatile Point dragOrigin;
    
    public MouseEventHandler(Component videoComponent, EventCallback callback, 
                           EventFilter eventFilter, ScheduledExecutorService eventThrottleExecutor) {
        this.videoComponent = videoComponent;
        this.callback = callback;
        this.eventFilter = eventFilter;
        this.eventThrottleExecutor = eventThrottleExecutor;
    }
    
    public void attachListeners() {
        videoComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Skip processing - we handle clicks on mouse release
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                // Internal tracking only - filtered from output
                Map<String, Object> details = new HashMap<>();
                details.put("button", e.getButton());
                details.put("modifiers", e.getModifiersEx());
                sendFilteredEvent(EventFilter.EventType.MOUSE_PRESS, "mouse-press", e.getX(), e.getY(), details);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                // Handle drag end first
                if (isDragging.compareAndSet(true, false)) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("button", e.getButton());
                    details.put("modifiers", e.getModifiersEx());
                    sendFilteredEvent(EventFilter.EventType.MOUSE_DRAG_END, "mouse-drag-end", e.getX(), e.getY(), details);
                    
                    // Clear drag origin
                    dragOrigin = null;
                } else {
                    // Process potential click/double-click
                    processClick(e);
                }
                
                // Internal tracking only - filtered from output
                Map<String, Object> details2 = new HashMap<>();
                details2.put("button", e.getButton());
                details2.put("modifiers", e.getModifiersEx());
                sendFilteredEvent(EventFilter.EventType.MOUSE_RELEASE, "mouse-release", e.getX(), e.getY(), details2);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                sendFilteredEvent(EventFilter.EventType.MOUSE_ENTER, "mouse-enter", e.getX(), e.getY(), null);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                sendFilteredEvent(EventFilter.EventType.MOUSE_EXIT, "mouse-exit", e.getX(), e.getY(), null);
            }
        });
        
        videoComponent.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                lastMouseMoveEvent = e;
                long now = System.currentTimeMillis();
                long lastTime = lastMoveTime.get();
                
                if (now - lastTime >= MOUSE_EVENT_THROTTLE_MS) {
                    if (lastMoveTime.compareAndSet(lastTime, now)) {
                        Map<String, Object> details = new HashMap<>();
                        details.put("modifiers", e.getModifiersEx());
                        sendFilteredEvent(EventFilter.EventType.MOUSE_MOVE, "mouse-move", e.getX(), e.getY(), details);
                        
                        // Cancel any pending task
                        if (pendingMouseMoveTask != null) {
                            pendingMouseMoveTask.cancel(false);
                        }
                    }
                } else {
                    // Schedule the event to be sent after throttle period
                    if (pendingMouseMoveTask != null) {
                        pendingMouseMoveTask.cancel(false);
                    }
                    
                    long delay = MOUSE_EVENT_THROTTLE_MS - (now - lastTime);
                    pendingMouseMoveTask = eventThrottleExecutor.schedule(() -> {
                        MouseEvent evt = lastMouseMoveEvent;
                        if (evt != null) {
                            Map<String, Object> details = new HashMap<>();
                            details.put("modifiers", evt.getModifiersEx());
                            sendFilteredEvent(EventFilter.EventType.MOUSE_MOVE, "mouse-move", evt.getX(), evt.getY(), details);
                            lastMoveTime.set(System.currentTimeMillis());
                        }
                    }, delay, TimeUnit.MILLISECONDS);
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging.compareAndSet(false, true)) {
                    // Store drag origin
                    dragOrigin = new Point(e.getX(), e.getY());
                    
                    Map<String, Object> details = new HashMap<>(4);
                    int modifiers = e.getModifiersEx();
                    details.put("button", getButtonFromModifiers(modifiers));
                    details.put("modifiers", modifiers);
                    sendFilteredEvent(EventFilter.EventType.MOUSE_DRAG_START, "mouse-drag-start", e.getX(), e.getY(), details);
                }
                
                lastMouseDragEvent = e;
                long now = System.currentTimeMillis();
                long lastTime = lastDragTime.get();
                
                if (now - lastTime >= MOUSE_EVENT_THROTTLE_MS) {
                    if (lastDragTime.compareAndSet(lastTime, now)) {
                        sendDragEvent(e);
                        
                        // Cancel any pending task
                        if (pendingMouseDragTask != null) {
                            pendingMouseDragTask.cancel(false);
                        }
                    }
                } else {
                    // Schedule the event to be sent after throttle period
                    if (pendingMouseDragTask != null) {
                        pendingMouseDragTask.cancel(false);
                    }
                    
                    long delay = MOUSE_EVENT_THROTTLE_MS - (now - lastTime);
                    pendingMouseDragTask = eventThrottleExecutor.schedule(() -> {
                        MouseEvent evt = lastMouseDragEvent;
                        if (evt != null) {
                            sendDragEvent(evt);
                            lastDragTime.set(System.currentTimeMillis());
                        }
                    }, delay, TimeUnit.MILLISECONDS);
                }
            }
            
            private int getButtonFromModifiers(int modifiers) {
                return switch (modifiers & (MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) {
                    case MouseEvent.BUTTON1_DOWN_MASK -> 1;
                    case MouseEvent.BUTTON2_DOWN_MASK -> 2;
                    case MouseEvent.BUTTON3_DOWN_MASK -> 3;
                    default -> 0;
                };
            }
        });
        
        videoComponent.addMouseWheelListener(e -> {
            Map<String, Object> details = new HashMap<>(5);
            details.put("wheelRotation", e.getWheelRotation());
            details.put("scrollAmount", e.getScrollAmount());
            details.put("scrollType", e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL ? "unit" : "block");
            details.put("modifiers", e.getModifiersEx());
            sendFilteredEvent(EventFilter.EventType.MOUSE_WHEEL, "mouse-wheel", e.getX(), e.getY(), details);
        });
    }
    
    private void processClick(MouseEvent e) {
        long now = System.currentTimeMillis();
        int x = e.getX();
        int y = e.getY();
        int button = e.getButton();
        int modifiers = e.getModifiersEx();
        
        var previous = lastClick.get();
        
        if (previous != null && previous.isDoubleClick(x, y, button, now)) {
            // Cancel pending single click
            if (pendingClickTask != null) {
                pendingClickTask.cancel(false);
                pendingClickTask = null;
            }
            
            // Send double-click immediately
            Map<String, Object> details = new HashMap<>(4);
            details.put("button", button);
            details.put("clickCount", 2);
            details.put("modifiers", modifiers);
            sendFilteredEvent(EventFilter.EventType.MOUSE_DOUBLE_CLICK, "mouse-double-click", x, y, details);
            
            // Clear last click
            lastClick.set(null);
        } else {
            // Store this click
            var current = new ClickInfo(x, y, button, modifiers, now);
            lastClick.set(current);
            
            // Schedule single click
            pendingClickTask = eventThrottleExecutor.schedule(() -> {
                // If this click is still current, send it
                if (lastClick.compareAndSet(current, null)) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("button", button);
                    details.put("clickCount", 1);
                    details.put("modifiers", modifiers);
                    sendFilteredEvent(EventFilter.EventType.MOUSE_CLICK, "mouse-click", x, y, details);
                }
            }, DOUBLE_CLICK_WINDOW_MS, TimeUnit.MILLISECONDS);
        }
    }
    
    private void sendDragEvent(MouseEvent e) {
        Map<String, Object> details = new HashMap<>(10);
        details.put("modifiers", e.getModifiersEx());
        
        if (dragOrigin != null) {
            // Add origin point
            details.put("originX", dragOrigin.x);
            details.put("originY", dragOrigin.y);
            
            // Calculate distance in pixels
            int deltaX = e.getX() - dragOrigin.x;
            int deltaY = e.getY() - dragOrigin.y;
            double distancePixels = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            details.put("deltaX", deltaX);
            details.put("deltaY", deltaY);
            details.put("distancePixels", distancePixels);
            
            // Calculate NDC origin and distance
            int canvasWidth = videoComponent.getWidth();
            int canvasHeight = videoComponent.getHeight();
            if (canvasWidth > 0 && canvasHeight > 0) {
                double originNdcX = (dragOrigin.x / (double)canvasWidth) * 2.0 - 1.0;
                double originNdcY = (dragOrigin.y / (double)canvasHeight) * 2.0 - 1.0;
                double deltaNdcX = (deltaX / (double)canvasWidth) * 2.0;
                double deltaNdcY = (deltaY / (double)canvasHeight) * 2.0;
                double distanceNdc = Math.sqrt(deltaNdcX * deltaNdcX + deltaNdcY * deltaNdcY);
                
                details.put("originNdcX", originNdcX);
                details.put("originNdcY", originNdcY);
                details.put("deltaNdcX", deltaNdcX);
                details.put("deltaNdcY", deltaNdcY);
                details.put("distanceNdc", distanceNdc);
            }
        }
        
        sendFilteredEvent(EventFilter.EventType.MOUSE_DRAG, "mouse-drag", e.getX(), e.getY(), details);
    }
    
    private void sendFilteredEvent(EventFilter.EventType type, String eventName, int x, int y, Map<String, Object> details) {
        if (eventFilter.isUnfiltered(type)) {
            callback.onNavigationEvent(type, eventName, x, y, details);
        }
    }
    
    public void cleanup() {
        if (pendingClickTask != null) {
            pendingClickTask.cancel(false);
        }
        if (pendingMouseMoveTask != null) {
            pendingMouseMoveTask.cancel(false);
        }
        if (pendingMouseDragTask != null) {
            pendingMouseDragTask.cancel(false);
        }
    }
}