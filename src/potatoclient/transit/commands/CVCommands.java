package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Computer Vision tracking commands from jon_shared_cmd_cv.proto
 */
public class CVCommands {
    
    /**
     * Initialize and register all CV commands
     */
    public static void initialize() {
        // Auto-focus
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-set-auto-focus")
                .description("Set auto focus")
                .required("channel", "value")
                .build()
        );
        
        // Tracking - IMPORTANT!
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-start-track-ndc")
                .description("Start NDC tracking")
                .required("channel", "x", "y")
                .optional("frame-time")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-stop-track")
                .description("Stop tracking")
                .build()
        );
        
        // Vampire mode
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-vampire-mode-enable")
                .description("Enable vampire mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-vampire-mode-disable")
                .description("Disable vampire mode")
                .build()
        );
        
        // Stabilization
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-stabilization-mode-enable")
                .description("Enable stabilization")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-stabilization-mode-disable")
                .description("Disable stabilization")
                .build()
        );
        
        // Dump
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-dump-start")
                .description("Start dump")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("cv-dump-stop")
                .description("Stop dump")
                .build()
        );
    }
}