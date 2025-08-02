package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Basic system commands from jon_shared_cmd.proto
 * These are the root-level commands with no parameters
 */
public class BasicCommands {
    
    /**
     * Initialize and register all basic commands
     */
    public static void initialize() {
        // Ping - Heartbeat/keepalive command
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("ping")
                .description("Heartbeat/keepalive command")
                .implemented()  // Mark as implemented since it's basic
                .build()
        );
        
        // Noop - No operation command
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("noop")
                .description("No operation command")
                .implemented()
                .build()
        );
        
        // Frozen - Mark important state
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("frozen")
                .description("Mark important state")
                .implemented()
                .build()
        );
    }
}