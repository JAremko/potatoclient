package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * System-level commands from jon_shared_cmd_system.proto
 */
public class SystemCommands {
    
    /**
     * Initialize and register all system commands
     */
    public static void initialize() {
        // System control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-start-all")
                .description("Start all systems")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-stop-all")
                .description("Stop all systems")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-reboot")
                .description("Reboot system")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-power-off")
                .description("Power off system")
                .build()
        );
        
        // Configuration
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-set-localization")
                .description("Set localization")
                .required("loc")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-reset-configs")
                .description("Reset configurations")
                .build()
        );
        
        // Recording
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-start-rec")
                .description("Start recording")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-stop-rec")
                .description("Stop recording")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-mark-rec-important")
                .description("Mark recording as important")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-unmark-rec-important")
                .description("Unmark recording as important")
                .build()
        );
        
        // Modes
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-enter-transport")
                .description("Enter transport mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-enable-geodesic-mode")
                .description("Enable geodesic mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("system-disable-geodesic-mode")
                .description("Disable geodesic mode")
                .build()
        );
    }
}