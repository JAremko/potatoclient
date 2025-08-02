package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Day camera control commands from jon_shared_cmd_day_camera.proto
 */
public class DayCameraCommands {
    
    /**
     * Initialize and register all day camera commands
     */
    public static void initialize() {
        // Basic control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-start")
                .description("Start day camera")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-stop")
                .description("Stop day camera")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-photo")
                .description("Take photo")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-halt-all")
                .description("Halt all operations")
                .build()
        );
        
        // Iris control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-set-iris")
                .description("Set iris value")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-set-auto-iris")
                .description("Set auto iris")
                .required("value")
                .build()
        );
        
        // Filters
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-set-infra-red-filter")
                .description("Set IR filter")
                .required("value")
                .build()
        );
        
        // FX modes
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-set-fx-mode")
                .description("Set FX mode")
                .required("mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-next-fx-mode")
                .description("Next FX mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-prev-fx-mode")
                .description("Previous FX mode")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-refresh-fx-mode")
                .description("Refresh FX mode")
                .build()
        );
        
        // Digital zoom and enhancement
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-set-digital-zoom-level")
                .description("Set digital zoom level")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-set-clahe-level")
                .description("Set CLAHE level")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-shift-clahe-level")
                .description("Shift CLAHE level")
                .required("value")
                .build()
        );
        
        // Meteorological data
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-get-meteo")
                .description("Get meteorological data")
                .build()
        );
        
        // Focus commands (nested structure)
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-focus-set-value")
                .description("Set focus value")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-focus-move")
                .description("Move focus")
                .required("target-value", "speed")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-focus-halt")
                .description("Halt focus")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-focus-offset")
                .description("Offset focus")
                .required("offset-value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-focus-reset")
                .description("Reset focus")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-focus-save-to-table")
                .description("Save focus to table")
                .build()
        );
        
        // Zoom commands (nested structure)
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-set-value")
                .description("Set zoom value")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-move")
                .description("Move zoom")
                .required("target-value", "speed")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-halt")
                .description("Halt zoom")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-set-table-value")
                .description("Set zoom table value")
                .required("value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-next-table-pos")
                .description("Next zoom table position")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-prev-table-pos")
                .description("Previous zoom table position")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-offset")
                .description("Offset zoom")
                .required("offset-value")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-reset")
                .description("Reset zoom")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("day-camera-zoom-save-to-table")
                .description("Save zoom to table")
                .build()
        );
    }
}