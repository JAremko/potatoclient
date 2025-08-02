package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Laser Range Finder commands from jon_shared_cmd_lrf.proto
 */
public class LRFCommands {
    
    /**
     * Initialize and register all LRF commands
     */
    public static void initialize() {
        // Basic control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-start")
                .description("Start LRF")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-stop")
                .description("Stop LRF")
                .build()
        );
        
        // Measurement
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-measure")
                .description("Measure range")
                .build()
        );
        
        // Scanning
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-scan-on")
                .description("Turn on scanning")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-scan-off")
                .description("Turn off scanning")
                .build()
        );
        
        // Refinement
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-refine-on")
                .description("Turn on refinement")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-refine-off")
                .description("Turn off refinement")
                .build()
        );
        
        // Target designator
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-target-designator-off")
                .description("Turn off target designator")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-target-designator-on-mode-a")
                .description("Target designator mode A")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-target-designator-on-mode-b")
                .description("Target designator mode B")
                .build()
        );
        
        // Modes
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-enable-fog-mode")
                .description("Enable fog mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-disable-fog-mode")
                .description("Disable fog mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-set-scan-mode")
                .description("Set scan mode")
                .required("mode")
                .build()
        );
        
        // Session management
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-new-session")
                .description("Start new session")
                .build()
        );
        
        // Meteorological data
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lrf-get-meteo")
                .description("Get meteorological data")
                .build()
        );
    }
}