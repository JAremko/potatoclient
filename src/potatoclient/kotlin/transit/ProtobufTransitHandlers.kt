package potatoclient.kotlin.transit

import com.cognitect.transit.ReadHandler
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
import ser.JonSharedDataTypes

/**
 * Transit handlers for all message types in the system
 * 
 * This provides automatic conversion between various message types and Transit representations:
 * - Protobuf state messages
 * - Gesture events
 * - Control messages
 * - Navigation events
 * - Window events
 * - Error messages
 * - Log messages
 */
object ProtobufTransitHandlers {
    
    /**
     * Create write handlers for all message types
     */
    fun createWriteHandlers(): Map<Class<*>, WriteHandler<*, *>> {
        val handlers = mutableMapOf<Class<*>, WriteHandler<*, *>>()
        
        // Protobuf state handlers
        handlers[JonSharedData.JonGUIState::class.java] = createJonGUIStateWriteHandler()
        handlers[JonSharedDataSystem.JonGuiDataSystem::class.java] = createSystemWriteHandler()
        handlers[JonSharedDataRotary.JonGuiDataRotary::class.java] = createRotaryWriteHandler()
        handlers[JonSharedDataGps.JonGuiDataGps::class.java] = createGpsWriteHandler()
        handlers[JonSharedDataCompass.JonGuiDataCompass::class.java] = createCompassWriteHandler()
        handlers[JonSharedDataLrf.JonGuiDataLrf::class.java] = createLrfWriteHandler()
        handlers[JonSharedDataTime.JonGuiDataTime::class.java] = createTimeWriteHandler()
        handlers[JonSharedDataCameraDay.JonGuiDataCameraDay::class.java] = createCameraDayWriteHandler()
        handlers[JonSharedDataCameraHeat.JonGuiDataCameraHeat::class.java] = createCameraHeatWriteHandler()
        
        // Event and control message handlers
        handlers[GestureEvent::class.java] = createGestureEventWriteHandler()
        handlers[NavigationEvent::class.java] = createNavigationEventWriteHandler()
        handlers[WindowEvent::class.java] = createWindowEventWriteHandler()
        handlers[ControlMessage::class.java] = createControlMessageWriteHandler()
        handlers[ErrorMessage::class.java] = createErrorMessageWriteHandler()
        handlers[LogMessage::class.java] = createLogMessageWriteHandler()
        
        return handlers
    }
    
    /**
     * Create read handlers for all protobuf types
     */
    fun createReadHandlers(): Map<String, ReadHandler<*, *>> {
        val handlers = mutableMapOf<String, ReadHandler<*, *>>()
        
        // Command handlers would go here
        // handlers["rotary-command"] = createRotaryCommandReadHandler()
        
        return handlers
    }
    
    // Main state write handler
    private fun createJonGUIStateWriteHandler(): WriteHandler<JonSharedData.JonGUIState, Map<String, Any>> {
        return object : WriteHandler<JonSharedData.JonGUIState, Map<String, Any>> {
            override fun tag(state: JonSharedData.JonGUIState): String = "jon-state"
            
            override fun rep(state: JonSharedData.JonGUIState): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["timestamp"] = System.currentTimeMillis()
                
                if (state.hasProtocolVersion()) {
                    result["protocol-version"] = state.protocolVersion
                }
                
                // Let individual handlers handle subsystems
                if (state.hasSystem()) {
                    result["system"] = state.system
                }
                if (state.hasRotary()) {
                    result["rotary"] = state.rotary
                }
                if (state.hasGps()) {
                    result["gps"] = state.gps
                }
                if (state.hasCompass()) {
                    result["compass"] = state.compass
                }
                if (state.hasLrf()) {
                    result["lrf"] = state.lrf
                }
                if (state.hasTime()) {
                    result["time"] = state.time
                }
                if (state.hasCameraDay()) {
                    result["camera-day"] = state.cameraDay
                }
                if (state.hasCameraHeat()) {
                    result["camera-heat"] = state.cameraHeat
                }
                
                return result
            }
            
            override fun stringRep(state: JonSharedData.JonGUIState): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // System write handler
    private fun createSystemWriteHandler(): WriteHandler<JonSharedDataSystem.JonGuiDataSystem, Map<String, Any>> {
        return object : WriteHandler<JonSharedDataSystem.JonGuiDataSystem, Map<String, Any>> {
            override fun tag(system: JonSharedDataSystem.JonGuiDataSystem): String = "system-data"
            
            override fun rep(system: JonSharedDataSystem.JonGuiDataSystem): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Temperatures
                if (system.hasCpuTemperature()) result["cpu-temperature"] = system.cpuTemperature
                if (system.hasGpuTemperature()) result["gpu-temperature"] = system.gpuTemperature
                
                // Load percentages
                if (system.hasCpuLoad()) result["cpu-load"] = system.cpuLoad
                if (system.hasGpuLoad()) result["gpu-load"] = system.gpuLoad
                
                // Power
                if (system.hasPowerConsumption()) result["power-consumption"] = system.powerConsumption
                
                // Recording info
                if (system.hasRecEnabled()) result["rec-enabled"] = system.recEnabled
                if (system.hasImportantRecEnabled()) result["important-rec-enabled"] = system.importantRecEnabled
                if (system.hasLowDiskSpace()) result["low-disk-space"] = system.lowDiskSpace
                if (system.hasNoDiskSpace()) result["no-disk-space"] = system.noDiskSpace
                if (system.hasDiskSpace()) result["disk-space"] = system.diskSpace
                
                // Modes
                if (system.hasTracking()) result["tracking"] = system.tracking
                if (system.hasVampireMode()) result["vampire-mode"] = system.vampireMode
                if (system.hasStabilizationMode()) result["stabilization-mode"] = system.stabilizationMode
                if (system.hasGeodesicMode()) result["geodesic-mode"] = system.geodesicMode
                if (system.hasCvDumping()) result["cv-dumping"] = system.cvDumping
                
                // Localization - use Transit keyword for enum
                if (system.hasLoc()) {
                    result["localization"] = TransitFactory.keyword(system.loc.name.lowercase())
                }
                
                // Recording directory timestamp
                val recDirParts = mutableMapOf<String, Any>()
                if (system.hasCurVideoRecDirYear()) recDirParts["year"] = system.curVideoRecDirYear
                if (system.hasCurVideoRecDirMonth()) recDirParts["month"] = system.curVideoRecDirMonth
                if (system.hasCurVideoRecDirDay()) recDirParts["day"] = system.curVideoRecDirDay
                if (system.hasCurVideoRecDirHour()) recDirParts["hour"] = system.curVideoRecDirHour
                if (system.hasCurVideoRecDirMinute()) recDirParts["minute"] = system.curVideoRecDirMinute
                if (system.hasCurVideoRecDirSecond()) recDirParts["second"] = system.curVideoRecDirSecond
                if (recDirParts.isNotEmpty()) {
                    result["rec-dir-timestamp"] = recDirParts
                }
                
                return result
            }
            
            override fun stringRep(system: JonSharedDataSystem.JonGuiDataSystem): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Rotary write handler
    private fun createRotaryWriteHandler(): WriteHandler<JonSharedDataRotary.JonGuiDataRotary, Map<String, Any>> {
        return object : WriteHandler<JonSharedDataRotary.JonGuiDataRotary, Map<String, Any>> {
            override fun tag(rotary: JonSharedDataRotary.JonGuiDataRotary): String = "rotary-data"
            
            override fun rep(rotary: JonSharedDataRotary.JonGuiDataRotary): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Position
                if (rotary.hasAzimuth()) result["azimuth"] = rotary.azimuth
                if (rotary.hasElevation()) result["elevation"] = rotary.elevation
                
                // Speed
                if (rotary.hasAzimuthSpeed()) result["azimuth-speed"] = rotary.azimuthSpeed
                if (rotary.hasElevationSpeed()) result["elevation-speed"] = rotary.elevationSpeed
                
                // Platform position
                if (rotary.hasPlatformAzimuth()) result["platform-azimuth"] = rotary.platformAzimuth
                if (rotary.hasPlatformElevation()) result["platform-elevation"] = rotary.platformElevation
                if (rotary.hasPlatformBank()) result["platform-bank"] = rotary.platformBank
                
                // Status
                if (rotary.hasIsMoving()) result["is-moving"] = rotary.isMoving
                if (rotary.hasIsScanning()) result["is-scanning"] = rotary.isScanning
                
                // Mode - use Transit keyword for enum
                if (rotary.hasMode()) {
                    result["mode"] = TransitFactory.keyword(rotary.mode.name.lowercase())
                }
                
                // Add scanning info
                if (rotary.hasIsScanningPaused()) result["is-scanning-paused"] = rotary.isScanningPaused
                if (rotary.hasUseRotaryAsCompass()) result["use-rotary-as-compass"] = rotary.useRotaryAsCompass
                if (rotary.hasScanTarget()) result["scan-target"] = rotary.scanTarget
                if (rotary.hasScanTargetMax()) result["scan-target-max"] = rotary.scanTargetMax
                if (rotary.hasSunAzimuth()) result["sun-azimuth"] = rotary.sunAzimuth
                if (rotary.hasSunElevation()) result["sun-elevation"] = rotary.sunElevation
                
                // Handle scan node
                if (rotary.hasCurrentScanNode()) {
                    val node = rotary.currentScanNode
                    val nodeMap = mutableMapOf<String, Any>()
                    if (node.hasIndex()) nodeMap["index"] = node.index
                    if (node.hasDayZoomTableValue()) nodeMap["day-zoom-table-value"] = node.dayZoomTableValue
                    if (node.hasHeatZoomTableValue()) nodeMap["heat-zoom-table-value"] = node.heatZoomTableValue
                    if (node.hasAzimuth()) nodeMap["azimuth"] = node.azimuth
                    if (node.hasElevation()) nodeMap["elevation"] = node.elevation
                    if (node.hasLinger()) nodeMap["linger"] = node.linger
                    if (node.hasSpeed()) nodeMap["speed"] = node.speed
                    result["current-scan-node"] = nodeMap
                }
                
                return result
            }
            
            override fun stringRep(rotary: JonSharedDataRotary.JonGuiDataRotary): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // GPS write handler
    private fun createGpsWriteHandler(): WriteHandler<JonSharedDataGps.JonGuiDataGps, Map<String, Any>> {
        return object : WriteHandler<JonSharedDataGps.JonGuiDataGps, Map<String, Any>> {
            override fun tag(gps: JonSharedDataGps.JonGuiDataGps): String = "gps-data"
            
            override fun rep(gps: JonSharedDataGps.JonGuiDataGps): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Position
                if (gps.hasLatitude()) result["latitude"] = gps.latitude
                if (gps.hasLongitude()) result["longitude"] = gps.longitude
                if (gps.hasAltitude()) result["altitude"] = gps.altitude
                
                // Manual position
                if (gps.hasManualLatitude()) result["manual-latitude"] = gps.manualLatitude
                if (gps.hasManualLongitude()) result["manual-longitude"] = gps.manualLongitude
                if (gps.hasManualAltitude()) result["manual-altitude"] = gps.manualAltitude
                
                // Fix type - use Transit keyword for enum
                if (gps.hasFixType()) {
                    result["fix-type"] = TransitFactory.keyword(gps.fixType.name.lowercase())
                }
                
                // Manual mode
                if (gps.hasUseManual()) result["use-manual"] = gps.useManual
                
                return result
            }
            
            override fun stringRep(gps: JonSharedDataGps.JonGuiDataGps): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Compass write handler
    private fun createCompassWriteHandler(): WriteHandler<JonSharedDataCompass.JonGuiDataCompass, Map<String, Any>> {
        return object : WriteHandler<JonSharedDataCompass.JonGuiDataCompass, Map<String, Any>> {
            override fun tag(compass: JonSharedDataCompass.JonGuiDataCompass): String = "compass-data"
            
            override fun rep(compass: JonSharedDataCompass.JonGuiDataCompass): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Heading
                if (compass.hasHeading()) result["heading"] = compass.heading
                if (compass.hasPitch()) result["pitch"] = compass.pitch
                if (compass.hasRoll()) result["roll"] = compass.roll
                
                // Magnetic field
                if (compass.hasMagneticFieldX()) result["magnetic-field-x"] = compass.magneticFieldX
                if (compass.hasMagneticFieldY()) result["magnetic-field-y"] = compass.magneticFieldY
                if (compass.hasMagneticFieldZ()) result["magnetic-field-z"] = compass.magneticFieldZ
                
                // Calibration status - use Transit keyword for enum
                if (compass.hasCalibrationStatus()) {
                    result["calibration-status"] = TransitFactory.keyword(compass.calibrationStatus.name.lowercase())
                }
                
                return result
            }
            
            override fun stringRep(compass: JonSharedDataCompass.JonGuiDataCompass): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // LRF write handler
    private fun createLrfWriteHandler(): WriteHandler<JonSharedDataLrf.JonGuiDataLrf, Map<String, Any>> {
        return object : WriteHandler<JonSharedDataLrf.JonGuiDataLrf, Map<String, Any>> {
            override fun tag(lrf: JonSharedDataLrf.JonGuiDataLrf): String = "lrf-data"
            
            override fun rep(lrf: JonSharedDataLrf.JonGuiDataLrf): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Range
                if (lrf.hasRange()) result["range"] = lrf.range
                if (lrf.hasRangeMin()) result["range-min"] = lrf.rangeMin
                if (lrf.hasRangeMax()) result["range-max"] = lrf.rangeMax
                
                // Status
                if (lrf.hasIsActive()) result["is-active"] = lrf.isActive
                if (lrf.hasLastMeasurementTime()) result["last-measurement-time"] = lrf.lastMeasurementTime
                
                // Error info - Note: error message stays as string, not keyword
                if (lrf.hasErrorCode()) result["error-code"] = lrf.errorCode
                if (lrf.hasErrorMessage()) result["error-message"] = lrf.errorMessage
                
                return result
            }
            
            override fun stringRep(lrf: JonSharedDataLrf.JonGuiDataLrf): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Time write handler
    private fun createTimeWriteHandler(): WriteHandler<JonSharedDataTime.JonGuiDataTime, Map<String, Any>> {
        return object : WriteHandler<JonSharedDataTime.JonGuiDataTime, Map<String, Any>> {
            override fun tag(time: JonSharedDataTime.JonGuiDataTime): String = "time-data"
            
            override fun rep(time: JonSharedDataTime.JonGuiDataTime): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // System time
                if (time.hasSystemTime()) result["system-time"] = time.systemTime
                if (time.hasUptime()) result["uptime"] = time.uptime
                
                // Time sync - use Transit keyword for enum
                if (time.hasTimeSyncStatus()) {
                    result["time-sync-status"] = TransitFactory.keyword(time.timeSyncStatus.name.lowercase())
                }
                if (time.hasTimeSyncOffset()) result["time-sync-offset"] = time.timeSyncOffset
                
                return result
            }
            
            override fun stringRep(time: JonSharedDataTime.JonGuiDataTime): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Day camera write handler
    private fun createCameraDayWriteHandler(): WriteHandler<JonSharedDataCameraDay.JonGuiDataCameraDay, Map<String, Any>> {
        return object : WriteHandler<JonSharedDataCameraDay.JonGuiDataCameraDay, Map<String, Any>> {
            override fun tag(camera: JonSharedDataCameraDay.JonGuiDataCameraDay): String = "camera-day-data"
            
            override fun rep(camera: JonSharedDataCameraDay.JonGuiDataCameraDay): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Zoom
                if (camera.hasZoom()) result["zoom"] = camera.zoom
                if (camera.hasZoomMin()) result["zoom-min"] = camera.zoomMin
                if (camera.hasZoomMax()) result["zoom-max"] = camera.zoomMax
                
                // Focus
                if (camera.hasFocus()) result["focus"] = camera.focus
                if (camera.hasAutoFocus()) result["auto-focus"] = camera.autoFocus
                
                // Exposure
                if (camera.hasExposure()) result["exposure"] = camera.exposure
                if (camera.hasAutoExposure()) result["auto-exposure"] = camera.autoExposure
                
                // Status
                if (camera.hasIsActive()) result["is-active"] = camera.isActive
                if (camera.hasIsRecording()) result["is-recording"] = camera.isRecording
                
                return result
            }
            
            override fun stringRep(camera: JonSharedDataCameraDay.JonGuiDataCameraDay): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Heat camera write handler
    private fun createCameraHeatWriteHandler(): WriteHandler<JonSharedDataCameraHeat.JonGuiDataCameraHeat, Map<String, Any>> {
        return object : WriteHandler<JonSharedDataCameraHeat.JonGuiDataCameraHeat, Map<String, Any>> {
            override fun tag(camera: JonSharedDataCameraHeat.JonGuiDataCameraHeat): String = "camera-heat-data"
            
            override fun rep(camera: JonSharedDataCameraHeat.JonGuiDataCameraHeat): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Zoom
                if (camera.hasZoom()) result["zoom"] = camera.zoom
                if (camera.hasZoomMin()) result["zoom-min"] = camera.zoomMin
                if (camera.hasZoomMax()) result["zoom-max"] = camera.zoomMax
                
                // Thermal settings
                if (camera.hasGain()) result["gain"] = camera.gain
                if (camera.hasLevel()) result["level"] = camera.level
                if (camera.hasSpan()) result["span"] = camera.span
                
                // NUC (Non-Uniformity Correction)
                if (camera.hasNucEnabled()) result["nuc-enabled"] = camera.nucEnabled
                if (camera.hasLastNucTime()) result["last-nuc-time"] = camera.lastNucTime
                
                // Palette - use Transit keyword for enum
                if (camera.hasPalette()) {
                    result["palette"] = TransitFactory.keyword(camera.palette.name.lowercase())
                }
                
                // Status
                if (camera.hasIsActive()) result["is-active"] = camera.isActive
                if (camera.hasIsRecording()) result["is-recording"] = camera.isRecording
                
                return result
            }
            
            override fun stringRep(camera: JonSharedDataCameraHeat.JonGuiDataCameraHeat): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Event and control message classes
    data class GestureEvent(
        val gestureType: String,
        val timestamp: Long,
        val x: Float? = null,
        val y: Float? = null,
        val velocity: Float? = null,
        val direction: String? = null,
        val streamType: String? = null
    )
    
    data class NavigationEvent(
        val type: String,  // move, click, drag, wheel
        val x: Int,
        val y: Int,
        val button: Int? = null,
        val wheelDelta: Int? = null
    )
    
    data class WindowEvent(
        val type: String,  // resize, focus, minimize, close
        val width: Int? = null,
        val height: Int? = null,
        val focused: Boolean? = null
    )
    
    data class ControlMessage(
        val type: String,  // rate-limit, enable-debounce, etc.
        val params: Map<String, Any>
    )
    
    data class ErrorMessage(
        val error: String,
        val message: String,
        val stackTrace: String? = null,
        val context: Map<String, Any>? = null
    )
    
    data class LogMessage(
        val level: String,  // debug, info, warn, error
        val message: String,
        val logger: String? = null,
        val context: Map<String, Any>? = null
    )
    
    // Gesture event handler
    private fun createGestureEventWriteHandler(): WriteHandler<GestureEvent, Map<String, Any>> {
        return object : WriteHandler<GestureEvent, Map<String, Any>> {
            override fun tag(event: GestureEvent): String = "gesture-event"
            
            override fun rep(event: GestureEvent): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                // Gesture type as keyword
                result["gesture-type"] = TransitFactory.keyword(event.gestureType)
                result["timestamp"] = event.timestamp
                
                // Optional fields
                event.x?.let { result["x"] = it }
                event.y?.let { result["y"] = it }
                event.velocity?.let { result["velocity"] = it }
                event.direction?.let { result["direction"] = TransitFactory.keyword(it) }
                event.streamType?.let { result["stream-type"] = TransitFactory.keyword(it) }
                
                return result
            }
            
            override fun stringRep(event: GestureEvent): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Navigation event handler
    private fun createNavigationEventWriteHandler(): WriteHandler<NavigationEvent, Map<String, Any>> {
        return object : WriteHandler<NavigationEvent, Map<String, Any>> {
            override fun tag(event: NavigationEvent): String = "nav-event"
            
            override fun rep(event: NavigationEvent): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["type"] = TransitFactory.keyword(event.type)
                result["x"] = event.x
                result["y"] = event.y
                
                event.button?.let { result["button"] = it }
                event.wheelDelta?.let { result["wheel-delta"] = it }
                
                return result
            }
            
            override fun stringRep(event: NavigationEvent): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Window event handler
    private fun createWindowEventWriteHandler(): WriteHandler<WindowEvent, Map<String, Any>> {
        return object : WriteHandler<WindowEvent, Map<String, Any>> {
            override fun tag(event: WindowEvent): String = "window-event"
            
            override fun rep(event: WindowEvent): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["type"] = TransitFactory.keyword(event.type)
                
                event.width?.let { result["width"] = it }
                event.height?.let { result["height"] = it }
                event.focused?.let { result["focused"] = it }
                
                return result
            }
            
            override fun stringRep(event: WindowEvent): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Control message handler
    private fun createControlMessageWriteHandler(): WriteHandler<ControlMessage, Map<String, Any>> {
        return object : WriteHandler<ControlMessage, Map<String, Any>> {
            override fun tag(msg: ControlMessage): String = "ctl-message"
            
            override fun rep(msg: ControlMessage): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["type"] = TransitFactory.keyword(msg.type)
                result["params"] = msg.params
                
                return result
            }
            
            override fun stringRep(msg: ControlMessage): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Error message handler
    private fun createErrorMessageWriteHandler(): WriteHandler<ErrorMessage, Map<String, Any>> {
        return object : WriteHandler<ErrorMessage, Map<String, Any>> {
            override fun tag(msg: ErrorMessage): String = "error-message"
            
            override fun rep(msg: ErrorMessage): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["error"] = msg.error  // Keep as string (not keyword)
                result["message"] = msg.message  // Keep as string
                
                msg.stackTrace?.let { result["stack-trace"] = it }
                msg.context?.let { result["context"] = it }
                
                return result
            }
            
            override fun stringRep(msg: ErrorMessage): String? = null
            override fun verbose(): Boolean = false
        }
    }
    
    // Log message handler
    private fun createLogMessageWriteHandler(): WriteHandler<LogMessage, Map<String, Any>> {
        return object : WriteHandler<LogMessage, Map<String, Any>> {
            override fun tag(msg: LogMessage): String = "log-message"
            
            override fun rep(msg: LogMessage): Map<String, Any> {
                val result = mutableMapOf<String, Any>()
                
                result["level"] = TransitFactory.keyword(msg.level)
                result["message"] = msg.message  // Keep as string (log text)
                
                msg.logger?.let { result["logger"] = it }
                msg.context?.let { result["context"] = it }
                
                return result
            }
            
            override fun stringRep(msg: LogMessage): String? = null
            override fun verbose(): Boolean = false
        }
    }
}