package potatoclient.transit

import ser.JonSharedData

/**
 * Simplified state converter to test basic functionality
 */
class SimpleStateConverter {
    fun convert(proto: JonSharedData.JonGUIState): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Add timestamp
        result["timestamp"] = System.currentTimeMillis()
        
        // Add system info if present
        if (proto.hasSystem()) {
            val system = proto.system
            result["system"] = mapOf(
                "battery-level" to 0, // TODO: Get actual battery level
                "has-data" to true
            )
        }
        
        // Basic conversion for testing
        result["proto-received"] = true
        
        return result
    }
}