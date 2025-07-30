package potatoclient.transit

import cmd.JonSharedCmd

/**
 * Simplified command builder to test basic functionality
 */
class SimpleCommandBuilder {
    fun buildPing(): JonSharedCmd.Root {
        // Build a simple ping command
        val pingCmd = JonSharedCmd.Ping.newBuilder().build()
        
        return JonSharedCmd.Root.newBuilder()
            .setPing(pingCmd)
            .build()
    }
    
    fun buildCommand(action: String): JonSharedCmd.Root {
        return when (action) {
            "ping" -> buildPing()
            else -> buildPing() // Default to ping for now
        }
    }
}