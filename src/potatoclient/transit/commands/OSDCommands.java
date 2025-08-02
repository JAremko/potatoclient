package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * On-Screen Display commands from jon_shared_cmd_osd.proto
 */
public class OSDCommands {
    
    /**
     * Initialize and register all OSD commands
     */
    public static void initialize() {
        // Visibility commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-show")
                .description("Show OSD")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-hide")
                .description("Hide OSD")
                .build()
        );
        
        // Brightness control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-set-brightness")
                .description("Set OSD brightness")
                .required("value")
                .build()
        );
        
        // Navigation
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-next-page")
                .description("Go to next OSD page")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-prev-page")
                .description("Go to previous OSD page")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-set-page")
                .description("Set OSD page")
                .required("value")
                .build()
        );
    }
}