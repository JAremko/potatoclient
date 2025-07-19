package potatoclient.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.awt.Component;
import static potatoclient.java.Constants.*;

/**
 * Handles all communication protocol with the parent Clojure process.
 * Encapsulates message formatting and JSON serialization.
 */
public class MessageProtocol {
    private final ObjectMapper mapper;
    
    // Pre-allocated message templates to reduce garbage
    private final Map<String, Object> responseTemplate;
    private final Map<String, Object> logTemplate;
    private final Map<String, Object> navTemplate;
    private final Map<String, Object> windowTemplate;
    
    public MessageProtocol(String streamId) {
        this.mapper = new ObjectMapper();
        
        // Initialize templates
        this.responseTemplate = new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);
        this.logTemplate = new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);
        this.navTemplate = new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);
        this.windowTemplate = new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);
        
        responseTemplate.put("type", "response");
        responseTemplate.put("streamId", streamId);
        
        logTemplate.put("type", "log");
        logTemplate.put("streamId", streamId);
        
        navTemplate.put("type", "navigation");
        navTemplate.put("streamId", streamId);
        
        windowTemplate.put("type", "window");
        windowTemplate.put("streamId", streamId);
    }
    
    public void sendResponse(String status, String data) {
        try {
            Map<String, Object> resp = new HashMap<>(responseTemplate);
            resp.put("status", status);
            resp.put("data", data);
            resp.put("timestamp", System.currentTimeMillis());
            
            sendMessage(resp);
        } catch (Exception e) {
            // Can't use sendException here as it might cause infinite recursion
            e.printStackTrace();
        }
    }
    
    public void sendLog(String level, String message) {
        try {
            Map<String, Object> log = new HashMap<>(logTemplate);
            log.put("level", level);
            log.put("message", message);
            log.put("timestamp", System.currentTimeMillis());
            
            sendMessage(log);
        } catch (Exception e) {
            // Can't use sendException here as it might cause infinite recursion
            e.printStackTrace();
        }
    }
    
    public void sendException(String context, Exception ex) {
        try {
            StringWriter sw = new StringWriter(STRING_WRITER_INITIAL_SIZE);
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            
            Map<String, Object> log = new HashMap<>(logTemplate);
            log.put("level", "ERROR");
            log.put("message", context + ": " + ex.getMessage());
            log.put("stackTrace", sw.toString());
            log.put("timestamp", System.currentTimeMillis());
            
            sendMessage(log);
        } catch (Exception e) {
            // Last resort - print to stderr
            e.printStackTrace();
        }
    }
    
    public void sendNavigationEvent(String eventType, int x, int y, 
                                   Component videoComponent, Map<String, Object> details) {
        try {
            // Get canvas dimensions
            int canvasWidth = videoComponent.getWidth();
            int canvasHeight = videoComponent.getHeight();
            
            // Avoid division by zero
            if (canvasWidth == 0 || canvasHeight == 0) return;
            
            // Convert to NDC coordinates (-1 to 1 range)
            double ndcX = (x / (double)canvasWidth) * 2.0 - 1.0;
            double ndcY = (y / (double)canvasHeight) * 2.0 - 1.0;
            
            Map<String, Object> event = new HashMap<>(MAP_INITIAL_CAPACITY + (details != null ? details.size() : 0));
            event.put("type", eventType);
            event.put("x", x);
            event.put("y", y);
            event.put("ndcX", ndcX);
            event.put("ndcY", ndcY);
            event.put("canvasWidth", canvasWidth);
            event.put("canvasHeight", canvasHeight);
            if (details != null) {
                event.putAll(details);
            }
            
            Map<String, Object> nav = new HashMap<>(navTemplate);
            nav.put("event", event);
            nav.put("timestamp", System.currentTimeMillis());
            
            sendMessage(nav);
        } catch (Exception e) {
            sendException("Navigation event error", e);
        }
    }
    
    public void sendWindowEvent(String eventType, Map<String, Object> details) {
        try {
            Map<String, Object> event = new HashMap<>(2 + (details != null ? details.size() : 0));
            event.put("type", eventType);
            if (details != null) {
                event.putAll(details);
            }
            
            Map<String, Object> windowEvent = new HashMap<>(windowTemplate);
            windowEvent.put("event", event);
            windowEvent.put("timestamp", System.currentTimeMillis());
            
            sendMessage(windowEvent);
        } catch (Exception e) {
            sendException("Window event error", e);
        }
    }
    
    private void sendMessage(Map<String, Object> message) throws Exception {
        String json = mapper.writeValueAsString(message);
        System.out.println(json);
        System.out.flush();
    }
}