package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * GPS commands from jon_shared_cmd_gps.proto
 */
public class GPSCommands {
    
    /**
     * Initialize and register all GPS commands
     */
    public static void initialize() {
        // Start - Start GPS
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("gps-start")
                .description("Start GPS")
                .build()
        );
        
        // Stop - Stop GPS
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("gps-stop")
                .description("Stop GPS")
                .build()
        );
        
        // SetManualPosition - Set manual GPS position
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("gps-set-manual-position")
                .description("Set manual GPS position")
                .required("latitude", "longitude", "altitude")
                .build()
        );
        
        // SetUseManualPosition - Use manual position
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("gps-set-use-manual-position")
                .description("Use manual position")
                .required("flag")
                .build()
        );
        
        // GetMeteo - Get meteorological data
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("gps-get-meteo")
                .description("Get meteorological data")
                .build()
        );
    }
}