package potatoclient.transit.commands;

import potatoclient.transit.ActionDefinition;
import potatoclient.transit.ActionRegistry;

/**
 * Laser Illuminator (LIRA) commands from jon_shared_cmd_lira.proto
 */
public class LIRACommands {
    
    /**
     * Initialize and register all LIRA commands
     */
    public static void initialize() {
        // Target refinement
        ActionRegistry.registerAction(
            new ActionDefinition.Builder("lira-refine-target")
                .description("Refine target position")
                .build()
        );
    }
}