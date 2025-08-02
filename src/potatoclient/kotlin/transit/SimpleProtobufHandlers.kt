package potatoclient.kotlin.transit

import com.cognitect.transit.WriteHandler
import com.cognitect.transit.TransitFactory
import ser.JonSharedData
import ser.JonSharedDataSystem
import ser.JonSharedDataRotary
import ser.JonSharedDataGps
import ser.JonSharedDataCompass
import ser.JonSharedDataLrf
import ser.JonSharedDataTime
import ser.JonSharedDataCameraDay
import ser.JonSharedDataCameraHeat

/**
 * Simplified Transit write handlers for protobuf messages.
 * These handlers convert protobuf objects to Transit-compatible maps.
 * 
 * Note: We always include all fields without checking for presence,
 * as the protobuf classes don't have has methods.
 */
object SimpleProtobufHandlers {
    /**
     * Create all protobuf write handlers
     */
    fun createWriteHandlers(): Map<Class<*>, WriteHandler<*, *>> {
        val handlers = mutableMapOf<Class<*>, WriteHandler<*, *>>()
        
        // Main state handler
        handlers[JonSharedData.JonGUIState::class.java] = createStateWriteHandler()
        
        // Component handlers
        handlers[JonSharedDataSystem.JonGuiDataSystem::class.java] = createSystemWriteHandler()
        handlers[JonSharedDataRotary.JonGuiDataRotary::class.java] = createRotaryWriteHandler()
        handlers[JonSharedDataGps.JonGuiDataGps::class.java] = createGpsWriteHandler()
        handlers[JonSharedDataCompass.JonGuiDataCompass::class.java] = createCompassWriteHandler()
        handlers[JonSharedDataLrf.JonGuiDataLrf::class.java] = createLrfWriteHandler()
        handlers[JonSharedDataTime.JonGuiDataTime::class.java] = createTimeWriteHandler()
        handlers[JonSharedDataCameraDay.JonGuiDataCameraDay::class.java] = createDayCameraWriteHandler()
        handlers[JonSharedDataCameraHeat.JonGuiDataCameraHeat::class.java] = createHeatCameraWriteHandler()
        
        return handlers
    }
    
    // Simple write handler implementation with proper verbose method
    private abstract class SimpleWriteHandler<T> : WriteHandler<T, Map<String, Any>> {
        override fun stringRep(o: T): String? = null
        override fun <V> getVerboseHandler(): WriteHandler<T, V>? = null
    }
    
    // Main state handler
    private fun createStateWriteHandler(): WriteHandler<JonSharedData.JonGUIState, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedData.JonGUIState>() {
            override fun tag(state: JonSharedData.JonGUIState): String = "jon-gui-state"
            
            override fun rep(state: JonSharedData.JonGUIState): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Always include protocol version
                result["protocol-version"] = state.protocolVersion
                
                // Add components if not null
                state.system?.let { result["system"] = it }
                state.rotary?.let { result["rotary"] = it }
                state.gps?.let { result["gps"] = it }
                state.compass?.let { result["compass"] = it }
                state.lrf?.let { result["lrf"] = it }
                state.time?.let { result["time"] = it }
                state.cameraDay?.let { result["camera-day"] = it }
                state.cameraHeat?.let { result["camera-heat"] = it }
                
                return result
            }
        }
    }
    
    // System write handler
    private fun createSystemWriteHandler(): WriteHandler<JonSharedDataSystem.JonGuiDataSystem, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedDataSystem.JonGuiDataSystem>() {
            override fun tag(system: JonSharedDataSystem.JonGuiDataSystem): String = "system-data"
            
            override fun rep(system: JonSharedDataSystem.JonGuiDataSystem): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Always include all fields
                result["cpu-temperature"] = system.cpuTemperature
                result["gpu-temperature"] = system.gpuTemperature
                result["cpu-load"] = system.cpuLoad
                result["gpu-load"] = system.gpuLoad
                result["power-consumption"] = system.powerConsumption
                result["rec-enabled"] = system.recEnabled
                result["important-rec-enabled"] = system.importantRecEnabled
                result["low-disk-space"] = system.lowDiskSpace
                result["no-disk-space"] = system.noDiskSpace
                result["disk-space"] = system.diskSpace
                result["tracking"] = system.tracking
                result["vampire-mode"] = system.vampireMode
                result["stabilization-mode"] = system.stabilizationMode
                result["geodesic-mode"] = system.geodesicMode
                result["cv-dumping"] = system.cvDumping
                
                // Localization - convert enum to keyword
                result["localization"] = TransitFactory.keyword(system.loc.name.lowercase().replace("jon_gui_data_system_localization_", ""))
                
                // Recording directory timestamp
                val recDirParts = mutableMapOf<String, Any>()
                recDirParts["year"] = system.curVideoRecDirYear
                recDirParts["month"] = system.curVideoRecDirMonth
                recDirParts["day"] = system.curVideoRecDirDay
                recDirParts["hour"] = system.curVideoRecDirHour
                recDirParts["minute"] = system.curVideoRecDirMinute
                recDirParts["second"] = system.curVideoRecDirSecond
                result["rec-dir-timestamp"] = recDirParts
                
                return result
            }
        }
    }
    
    // Rotary write handler
    private fun createRotaryWriteHandler(): WriteHandler<JonSharedDataRotary.JonGuiDataRotary, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedDataRotary.JonGuiDataRotary>() {
            override fun tag(rotary: JonSharedDataRotary.JonGuiDataRotary): String = "rotary-data"
            
            override fun rep(rotary: JonSharedDataRotary.JonGuiDataRotary): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Position - always include
                result["azimuth"] = rotary.azimuth
                result["elevation"] = rotary.elevation
                
                // Speed
                result["azimuth-speed"] = rotary.azimuthSpeed
                result["elevation-speed"] = rotary.elevationSpeed
                
                // Platform position
                result["platform-azimuth"] = rotary.platformAzimuth
                result["platform-elevation"] = rotary.platformElevation
                result["platform-bank"] = rotary.platformBank
                
                // Status
                result["is-moving"] = rotary.isMoving
                result["is-scanning"] = rotary.isScanning
                
                // Mode - use Transit keyword for enum
                result["mode"] = TransitFactory.keyword(rotary.mode.name.lowercase().replace("jon_gui_data_rotary_mode_", ""))
                
                // Add scanning info
                result["is-scanning-paused"] = rotary.isScanningPaused
                result["use-rotary-as-compass"] = rotary.useRotaryAsCompass
                result["scan-target"] = rotary.scanTarget
                result["scan-target-max"] = rotary.scanTargetMax
                result["sun-azimuth"] = rotary.sunAzimuth
                result["sun-elevation"] = rotary.sunElevation
                
                // Handle scan node - check if it's not null
                rotary.currentScanNode?.let { node ->
                    val nodeMap = mutableMapOf<String, Any>()
                    nodeMap["index"] = node.index
                    nodeMap["day-zoom-table-value"] = node.dayZoomTableValue
                    nodeMap["heat-zoom-table-value"] = node.heatZoomTableValue
                    nodeMap["azimuth"] = node.azimuth
                    nodeMap["elevation"] = node.elevation
                    nodeMap["linger"] = node.linger
                    nodeMap["speed"] = node.speed
                    result["current-scan-node"] = nodeMap
                }
                
                return result
            }
        }
    }
    
    // GPS write handler
    private fun createGpsWriteHandler(): WriteHandler<JonSharedDataGps.JonGuiDataGps, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedDataGps.JonGuiDataGps>() {
            override fun tag(gps: JonSharedDataGps.JonGuiDataGps): String = "gps-data"
            
            override fun rep(gps: JonSharedDataGps.JonGuiDataGps): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Position
                result["latitude"] = gps.latitude
                result["longitude"] = gps.longitude
                result["altitude"] = gps.altitude
                
                // Manual position
                result["manual-latitude"] = gps.manualLatitude
                result["manual-longitude"] = gps.manualLongitude
                result["manual-altitude"] = gps.manualAltitude
                
                // Fix type - use Transit keyword for enum
                result["fix-type"] = TransitFactory.keyword(gps.fixType.name.lowercase().replace("jon_gui_data_gps_fix_type_", ""))
                
                // Manual mode
                result["use-manual"] = gps.useManual
                
                return result
            }
        }
    }
    
    // Compass write handler
    private fun createCompassWriteHandler(): WriteHandler<JonSharedDataCompass.JonGuiDataCompass, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedDataCompass.JonGuiDataCompass>() {
            override fun tag(compass: JonSharedDataCompass.JonGuiDataCompass): String = "compass-data"
            
            override fun rep(compass: JonSharedDataCompass.JonGuiDataCompass): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Compass orientation
                result["azimuth"] = compass.azimuth
                result["elevation"] = compass.elevation
                result["bank"] = compass.bank
                
                // Offsets and calibration
                result["offset-azimuth"] = compass.offsetAzimuth
                result["offset-elevation"] = compass.offsetElevation
                result["magnetic-declination"] = compass.magneticDeclination
                result["calibrating"] = compass.calibrating
                
                return result
            }
        }
    }
    
    // LRF write handler
    private fun createLrfWriteHandler(): WriteHandler<JonSharedDataLrf.JonGuiDataLrf, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedDataLrf.JonGuiDataLrf>() {
            override fun tag(lrf: JonSharedDataLrf.JonGuiDataLrf): String = "lrf-data"
            
            override fun rep(lrf: JonSharedDataLrf.JonGuiDataLrf): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // LRF status
                result["is-scanning"] = lrf.isScanning
                result["is-measuring"] = lrf.isMeasuring
                result["measure-id"] = lrf.measureId
                result["pointer-mode"] = TransitFactory.keyword(lrf.pointerMode.name.lowercase().replace("jon_gui_datat_lrf_laser_pointer_modes_", ""))
                result["fog-mode-enabled"] = lrf.fogModeEnabled
                result["is-refining"] = lrf.isRefining
                
                // Target if present
                if (lrf.hasTarget()) {
                    val target = lrf.target
                    // TODO: Add target fields
                    result["has-target"] = true
                } else {
                    result["has-target"] = false
                }
                
                return result
            }
        }
    }
    
    // Time write handler
    private fun createTimeWriteHandler(): WriteHandler<JonSharedDataTime.JonGuiDataTime, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedDataTime.JonGuiDataTime>() {
            override fun tag(time: JonSharedDataTime.JonGuiDataTime): String = "time-data"
            
            override fun rep(time: JonSharedDataTime.JonGuiDataTime): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["timestamp"] = time.timestamp
                result["manual-timestamp"] = time.manualTimestamp
                result["zone-id"] = time.zoneId
                result["use-manual-time"] = time.useManualTime
                
                return result
            }
        }
    }
    
    // Day camera write handler
    private fun createDayCameraWriteHandler(): WriteHandler<JonSharedDataCameraDay.JonGuiDataCameraDay, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedDataCameraDay.JonGuiDataCameraDay>() {
            override fun tag(camera: JonSharedDataCameraDay.JonGuiDataCameraDay): String = "camera-day-data"
            
            override fun rep(camera: JonSharedDataCameraDay.JonGuiDataCameraDay): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["focus-pos"] = camera.focusPos
                result["zoom-pos"] = camera.zoomPos
                result["iris-pos"] = camera.irisPos
                result["infrared-filter"] = camera.infraredFilter
                result["zoom-table-pos"] = camera.zoomTablePos
                result["zoom-table-pos-max"] = camera.zoomTablePosMax
                result["fx-mode"] = TransitFactory.keyword(camera.fxMode.name.lowercase().replace("jon_gui_data_fx_mode_day_", ""))
                result["auto-focus"] = camera.autoFocus
                result["auto-iris"] = camera.autoIris
                result["digital-zoom-level"] = camera.digitalZoomLevel
                result["clahe-level"] = camera.claheLevel
                
                return result
            }
        }
    }
    
    // Heat camera write handler
    private fun createHeatCameraWriteHandler(): WriteHandler<JonSharedDataCameraHeat.JonGuiDataCameraHeat, Map<String, Any>> {
        return object : SimpleWriteHandler<JonSharedDataCameraHeat.JonGuiDataCameraHeat>() {
            override fun tag(camera: JonSharedDataCameraHeat.JonGuiDataCameraHeat): String = "camera-heat-data"
            
            override fun rep(camera: JonSharedDataCameraHeat.JonGuiDataCameraHeat): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["zoom-pos"] = camera.zoomPos
                result["agc-mode"] = TransitFactory.keyword(camera.agcMode.name.lowercase().replace("jon_gui_data_video_channel_heat_agc_modes_", ""))
                result["filter"] = TransitFactory.keyword(camera.filter.name.lowercase().replace("jon_gui_data_video_channel_heat_filters_", ""))
                result["auto-focus"] = camera.autoFocus
                result["zoom-table-pos"] = camera.zoomTablePos
                result["zoom-table-pos-max"] = camera.zoomTablePosMax
                result["dde-level"] = camera.ddeLevel
                result["dde-enabled"] = camera.ddeEnabled
                result["fx-mode"] = TransitFactory.keyword(camera.fxMode.name.lowercase().replace("jon_gui_data_fx_mode_heat_", ""))
                result["digital-zoom-level"] = camera.digitalZoomLevel
                result["clahe-level"] = camera.claheLevel
                
                return result
            }
        }
    }
}