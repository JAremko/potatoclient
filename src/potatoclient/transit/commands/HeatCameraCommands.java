package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Thermal camera control commands from jon_shared_cmd_heat_camera.proto
 */
public class HeatCameraCommands {
    
    /**
     * Initialize and register all heat camera commands
     */
    public static void initialize() {
        // Basic control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-start")
                .description("Start heat camera")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-stop")
                .description("Stop heat camera")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-photo")
                .description("Take photo")
                .build()
        );
        
        // AGC and filters
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-set-agc")
                .description("Set AGC mode")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-set-filters")
                .description("Set filters")
                .required("value")
                .build()
        );
        
        // Zoom control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-zoom-in")
                .description("Zoom in")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-zoom-out")
                .description("Zoom out")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-zoom-stop")
                .description("Stop zoom")
                .build()
        );
        
        // Focus control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-focus-in")
                .description("Focus in")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-focus-out")
                .description("Focus out")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-focus-stop")
                .description("Stop focus")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-focus-step-plus")
                .description("Focus step forward")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-focus-step-minus")
                .description("Focus step backward")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-set-auto-focus")
                .description("Set auto focus")
                .required("value")
                .build()
        );
        
        // Calibration
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-calibrate")
                .description("Calibrate camera")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-set-calib-mode")
                .description("Set calibration mode")
                .build()
        );
        
        // DDE (Digital Detail Enhancement)
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-set-dde-level")
                .description("Set DDE level")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-shift-dde")
                .description("Shift DDE")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-enable-dde")
                .description("Enable DDE")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-disable-dde")
                .description("Disable DDE")
                .build()
        );
        
        // FX modes
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-set-fx-mode")
                .description("Set FX mode")
                .required("mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-next-fx-mode")
                .description("Next FX mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-prev-fx-mode")
                .description("Previous FX mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-refresh-fx-mode")
                .description("Refresh FX mode")
                .build()
        );
        
        // Digital zoom and enhancement
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-set-digital-zoom-level")
                .description("Set digital zoom level")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-set-clahe-level")
                .description("Set CLAHE level")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-shift-clahe-level")
                .description("Shift CLAHE level")
                .required("value")
                .build()
        );
        
        // Zoom table commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-zoom-set-table-value")
                .description("Set zoom table value")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-zoom-next-table-pos")
                .description("Next zoom table position")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-zoom-prev-table-pos")
                .description("Previous zoom table position")
                .build()
        );
        
        // Utility commands
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-get-meteo")
                .description("Get meteorological data")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-reset-zoom")
                .description("Reset zoom")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("heat-camera-save-to-table")
                .description("Save to table")
                .build()
        );
    }
}