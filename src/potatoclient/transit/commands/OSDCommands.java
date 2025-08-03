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
        // Screen display commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-show-default-screen")
                .description("Show default OSD screen")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-show-lrf-measure-screen")
                .description("Show LRF measure screen")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-show-lrf-result-screen")
                .description("Show LRF result screen")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-show-lrf-result-simplified-screen")
                .description("Show LRF result simplified screen")
                .build()
        );
        
        // Heat OSD commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-enable-heat-osd")
                .description("Enable heat camera OSD")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-disable-heat-osd")
                .description("Disable heat camera OSD")
                .build()
        );
        
        // Day OSD commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-enable-day-osd")
                .description("Enable day camera OSD")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("osd-disable-day-osd")
                .description("Disable day camera OSD")
                .build()
        );
    }
}