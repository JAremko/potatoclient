package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Compass commands from jon_shared_cmd_compass.proto
 */
public class CompassCommands {
    
    /**
     * Initialize and register all compass commands
     */
    public static void initialize() {
        // Basic control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-start")
                .description("Start compass")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-stop")
                .description("Stop compass")
                .build()
        );
        
        // Configuration
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-set-magnetic-declination")
                .description("Set magnetic declination")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-set-offset-angle-azimuth")
                .description("Set azimuth offset")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-set-offset-angle-elevation")
                .description("Set elevation offset")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-set-use-rotary-position")
                .description("Use rotary position")
                .required("flag")
                .build()
        );
        
        // Calibration
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-calibrate-start-long")
                .description("Start long calibration")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-calibrate-start-short")
                .description("Start short calibration")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-calibrate-next")
                .description("Next calibration step")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-calibrate-cencel")
                .description("Cancel calibration")
                .build()
        );
        
        // Meteorological data
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("compass-get-meteo")
                .description("Get meteorological data")
                .build()
        );
    }
}