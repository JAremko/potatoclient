package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Laser Range Finder Alignment commands from jon_shared_cmd_lrf_align.proto
 */
public class LRFAlignCommands {
    
    /**
     * Initialize and register all LRF alignment commands
     */
    public static void initialize() {
        // Basic control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-align-start")
                .description("Start LRF alignment")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-align-stop")
                .description("Stop LRF alignment")
                .build()
        );
        
        // Alignment operations
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-align-save")
                .description("Save alignment")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-align-reset")
                .description("Reset alignment")
                .build()
        );
        
        // Offset adjustments
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-align-set-offset-x")
                .description("Set X offset")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-align-set-offset-y")
                .description("Set Y offset")
                .required("value")
                .build()
        );
    }
}