package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Glass Heater commands from jon_shared_cmd_glass_heater.proto
 */
public class GlassHeaterCommands {
    
    /**
     * Initialize and register all glass heater commands
     */
    public static void initialize() {
        // Basic control
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("glass-heater-start")
                .description("Start glass heater")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("glass-heater-stop")
                .description("Stop glass heater")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("glass-heater-turn-on")
                .description("Turn on glass heater")
                .build()
        );
        
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("glass-heater-turn-off")
                .description("Turn off glass heater")
                .build()
        );
        
        // Meteorological data
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("glass-heater-get-meteo")
                .description("Get meteorological data")
                .build()
        );
    }
}