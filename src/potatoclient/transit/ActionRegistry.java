package potatoclient.transit;

import com.cognitect.transit.Keyword;
import com.cognitect.transit.TransitFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central registry for all command actions in the system.
 * Provides lookup, validation metadata, and handler registration.
 * NO protobuf dependencies or validation logic - just metadata!
 */
public class ActionRegistry {
    // Static storage for all actions
    private static final Map<String, ActionDefinition> actions = new ConcurrentHashMap<>();
    private static final Map<Keyword, ActionDefinition> actionsByKeyword = new ConcurrentHashMap<>();
    
    // Handler storage (for tracking implementation)
    private static final Map<String, Object> handlers = new ConcurrentHashMap<>();
    
    // Static initialization of all command modules
    static {
        initializeRegistry();
    }
    
    /**
     * Initialize the registry with all command modules
     */
    private static void initializeRegistry() {
        // Initialize each command module
        // Each module will call registerAction() for its commands
        potatoclient.transit.commands.BasicCommands.initialize();
        potatoclient.transit.commands.GPSCommands.initialize();
        potatoclient.transit.commands.RotaryCommands.initialize();
        potatoclient.transit.commands.SystemCommands.initialize();
        potatoclient.transit.commands.CVCommands.initialize();
        potatoclient.transit.commands.CompassCommands.initialize();
        potatoclient.transit.commands.DayCameraCommands.initialize();
        potatoclient.transit.commands.HeatCameraCommands.initialize();
        potatoclient.transit.commands.LRFCommands.initialize();
        potatoclient.transit.commands.OSDCommands.initialize();
        potatoclient.transit.commands.LRFAlignCommands.initialize();
        potatoclient.transit.commands.GlassHeaterCommands.initialize();
        potatoclient.transit.commands.LIRACommands.initialize();
    }
    
    /**
     * Register an action definition
     */
    public static void registerAction(ActionDefinition action) {
        actions.put(action.getName(), action);
        actionsByKeyword.put(action.getKeyword(), action);
    }
    
    /**
     * Check if an action is known (by string name)
     */
    public static boolean isKnownAction(String actionName) {
        return actions.containsKey(actionName);
    }
    
    /**
     * Check if an action is known (by keyword)
     */
    public static boolean isKnownAction(Keyword actionKeyword) {
        return actionsByKeyword.containsKey(actionKeyword);
    }
    
    /**
     * Get action definition by name
     */
    public static ActionDefinition getAction(String actionName) {
        return actions.get(actionName);
    }
    
    /**
     * Get action definition by keyword
     */
    public static ActionDefinition getAction(Keyword actionKeyword) {
        return actionsByKeyword.get(actionKeyword);
    }
    
    /**
     * Get all registered action names
     */
    public static Set<String> getAllActionNames() {
        return Collections.unmodifiableSet(actions.keySet());
    }
    
    /**
     * Get all registered action keywords
     */
    public static List<Keyword> getAllActionKeywords() {
        return new ArrayList<>(actionsByKeyword.keySet());
    }
    
    /**
     * Get required parameters for an action
     */
    public static Set<String> getRequiredParams(String actionName) {
        ActionDefinition action = actions.get(actionName);
        if (action == null) return Collections.emptySet();
        
        return action.getRequiredParams().stream()
                .map(Keyword::getName)
                .collect(Collectors.toSet());
    }
    
    /**
     * Get optional parameters for an action
     */
    public static Set<String> getOptionalParams(String actionName) {
        ActionDefinition action = actions.get(actionName);
        if (action == null) return Collections.emptySet();
        
        return action.getOptionalParams().stream()
                .map(Keyword::getName)
                .collect(Collectors.toSet());
    }
    
    /**
     * Check if all required parameters are present (for metadata only)
     * This does NOT validate values - just checks presence
     */
    public static boolean hasRequiredParams(String actionName, Map<?, ?> params) {
        ActionDefinition action = actions.get(actionName);
        if (action == null) return false;
        
        for (Keyword required : action.getRequiredParams()) {
            boolean found = false;
            // Check for both string and keyword keys
            if (params.containsKey(required.getName()) || params.containsKey(required)) {
                found = true;
            }
            if (!found) return false;
        }
        return true;
    }
    
    /**
     * Get all implemented actions
     */
    public static List<String> getImplementedActions() {
        return actions.values().stream()
                .filter(ActionDefinition::isImplemented)
                .map(ActionDefinition::getName)
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Get all unimplemented actions
     */
    public static List<String> getUnimplementedActions() {
        return actions.values().stream()
                .filter(a -> !a.isImplemented())
                .map(ActionDefinition::getName)
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Register a handler for an action (marks it as implemented)
     */
    public static void registerHandler(String actionName, Object handler) {
        handlers.put(actionName, handler);
        ActionDefinition action = actions.get(actionName);
        if (action != null) {
            // Mark as implemented by replacing with new definition
            ActionDefinition implemented = new ActionDefinition.Builder(actionName)
                    .description(action.getDescription())
                    .required(action.getRequiredParams().stream()
                            .map(Keyword::getName)
                            .toArray(String[]::new))
                    .optional(action.getOptionalParams().stream()
                            .map(Keyword::getName)
                            .toArray(String[]::new))
                    .implemented(true)
                    .build();
            registerAction(implemented);
        }
    }
    
    /**
     * Get handler for an action
     */
    public static Object getHandler(String actionName) {
        return handlers.get(actionName);
    }
    
    /**
     * Get registry statistics
     */
    public static Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", actions.size());
        stats.put("implemented", getImplementedActions().size());
        stats.put("unimplemented", getUnimplementedActions().size());
        stats.put("handlers", handlers.size());
        return stats;
    }
}