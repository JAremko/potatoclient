package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Rotary platform commands from jon_shared_cmd_rotary.proto
 * This is one of the largest command sets with ~30+ commands
 */
public class RotaryCommands {
    
    /**
     * Initialize and register all rotary commands
     */
    public static void initialize() {
        // Basic control commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-start")
                .description("Start rotary platform")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-stop")
                .description("Stop rotary platform")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-halt")
                .description("Halt rotary platform")
                .build()
        );
        
        // Platform position commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-set-platform-azimuth")
                .description("Set platform azimuth")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-set-platform-elevation")
                .description("Set platform elevation")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-set-platform-bank")
                .description("Set platform bank")
                .required("value")
                .build()
        );
        
        // Mode and configuration
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-set-mode")
                .description("Set rotary mode")
                .required("mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-set-use-rotary-as-compass")
                .description("Use rotary as compass")
                .required("flag")
                .build()
        );
        
        // GPS-based rotation
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-rotate-to-gps")
                .description("Rotate to GPS coordinates")
                .required("latitude", "longitude", "altitude")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-set-origin-gps")
                .description("Set GPS origin")
                .required("latitude", "longitude", "altitude")
                .build()
        );
        
        // NDC (Normalized Device Coordinates) rotation - IMPORTANT!
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-rotate-to-ndc")
                .description("Rotate to NDC coordinates")
                .required("channel", "x", "y")
                .build()
        );
        
        // Scanning commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-start")
                .description("Start scanning")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-stop")
                .description("Stop scanning")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-pause")
                .description("Pause scanning")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-unpause")
                .description("Unpause scanning")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-prev")
                .description("Previous scan position")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-next")
                .description("Next scan position")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-refresh-node-list")
                .description("Refresh scan node list")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-select-node")
                .description("Select scan node")
                .required("index")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-delete-node")
                .description("Delete scan node")
                .required("index")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-update-node")
                .description("Update scan node")
                .required("index", "day-zoom-table-value", "heat-zoom-table-value", 
                          "azimuth", "elevation", "linger", "speed")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-scan-add-node")
                .description("Add scan node")
                .required("index", "day-zoom-table-value", "heat-zoom-table-value",
                          "azimuth", "elevation", "linger", "speed")
                .build()
        );
        
        // Meteorological data
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-get-meteo")
                .description("Get meteorological data")
                .build()
        );
        
        // Azimuth axis commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-azimuth-set-value")
                .description("Set azimuth value")
                .required("value", "direction")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-azimuth-rotate-to")
                .description("Rotate azimuth to target")
                .required("target-value", "speed", "direction")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-azimuth-rotate")
                .description("Rotate azimuth")
                .required("speed", "direction")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-azimuth-relative")
                .description("Rotate azimuth relative")
                .required("value", "speed", "direction")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-azimuth-relative-set")
                .description("Set azimuth relative")
                .required("value", "direction")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-azimuth-halt")
                .description("Halt azimuth")
                .build()
        );
        
        // Elevation axis commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-elevation-set-value")
                .description("Set elevation value")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-elevation-rotate-to")
                .description("Rotate elevation to target")
                .required("target-value", "speed")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-elevation-rotate")
                .description("Rotate elevation")
                .required("speed", "direction")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-elevation-relative")
                .description("Rotate elevation relative")
                .required("value", "speed", "direction")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-elevation-relative-set")
                .description("Set elevation relative")
                .required("value", "direction")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("rotary-axis-elevation-halt")
                .description("Halt elevation")
                .build()
        );
    }
}