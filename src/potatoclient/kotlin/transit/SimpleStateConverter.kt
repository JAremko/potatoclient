package potatoclient.kotlin.transit

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
 * Converts protobuf JonGUIState to Transit-compatible maps
 */
class SimpleStateConverter {
    fun convert(proto: JonSharedData.JonGUIState): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        // Add timestamp
        result["timestamp"] = System.currentTimeMillis()
        
        // Add protocol version
        if (proto.hasProtocolVersion()) {
            result["protocol-version"] = proto.protocolVersion
        }

        // Convert system data
        if (proto.hasSystem()) {
            result["system"] = convertSystem(proto.system)
        }

        // Convert rotary data
        if (proto.hasRotary()) {
            result["rotary"] = convertRotary(proto.rotary)
        }

        // Convert GPS data
        if (proto.hasGps()) {
            result["gps"] = convertGps(proto.gps)
        }

        // Convert compass data
        if (proto.hasCompass()) {
            result["compass"] = convertCompass(proto.compass)
        }

        // Convert LRF data
        if (proto.hasLrf()) {
            result["lrf"] = convertLrf(proto.lrf)
        }

        // Convert time data
        if (proto.hasTime()) {
            result["time"] = convertTime(proto.time)
        }

        // Convert day camera data
        if (proto.hasCameraDay()) {
            result["camera-day"] = convertCameraDay(proto.cameraDay)
        }

        // Convert heat camera data
        if (proto.hasCameraHeat()) {
            result["camera-heat"] = convertCameraHeat(proto.cameraHeat)
        }

        // Basic conversion flag for testing
        result["proto-received"] = true

        return result
    }

    private fun convertSystem(system: JonSharedDataSystem.JonGuiDataSystem): Map<String, Any> {
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
        
        // Localization
        if (system.hasLoc()) {
            result["localization"] = system.loc.name.lowercase()
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

    private fun convertRotary(rotary: JonSharedDataRotary.JonGuiDataRotary): Map<String, Any> {
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
        
        // Mode
        if (rotary.hasMode()) {
            result["mode"] = rotary.mode.name.lowercase()
        }
        
        return result
    }

    private fun convertGps(gps: JonSharedDataGps.JonGuiDataGps): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Position
        if (gps.hasLatitude()) result["latitude"] = gps.latitude
        if (gps.hasLongitude()) result["longitude"] = gps.longitude
        if (gps.hasAltitude()) result["altitude"] = gps.altitude
        
        // Accuracy
        if (gps.hasHorizontalAccuracy()) result["horizontal-accuracy"] = gps.horizontalAccuracy
        if (gps.hasVerticalAccuracy()) result["vertical-accuracy"] = gps.verticalAccuracy
        
        // Speed and heading
        if (gps.hasSpeed()) result["speed"] = gps.speed
        if (gps.hasHeading()) result["heading"] = gps.heading
        
        // Status
        if (gps.hasSatellitesVisible()) result["satellites-visible"] = gps.satellitesVisible
        if (gps.hasSatellitesUsed()) result["satellites-used"] = gps.satellitesUsed
        if (gps.hasFixType()) result["fix-type"] = gps.fixType.name.lowercase()
        
        return result
    }

    private fun convertCompass(compass: JonSharedDataCompass.JonGuiDataCompass): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Heading
        if (compass.hasHeading()) result["heading"] = compass.heading
        if (compass.hasPitch()) result["pitch"] = compass.pitch
        if (compass.hasRoll()) result["roll"] = compass.roll
        
        // Magnetic field
        if (compass.hasMagneticFieldX()) result["magnetic-field-x"] = compass.magneticFieldX
        if (compass.hasMagneticFieldY()) result["magnetic-field-y"] = compass.magneticFieldY
        if (compass.hasMagneticFieldZ()) result["magnetic-field-z"] = compass.magneticFieldZ
        
        // Calibration status
        if (compass.hasCalibrationStatus()) {
            result["calibration-status"] = compass.calibrationStatus.name.lowercase()
        }
        
        return result
    }

    private fun convertLrf(lrf: JonSharedDataLrf.JonGuiDataLrf): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // Range
        if (lrf.hasRange()) result["range"] = lrf.range
        if (lrf.hasRangeMin()) result["range-min"] = lrf.rangeMin
        if (lrf.hasRangeMax()) result["range-max"] = lrf.rangeMax
        
        // Status
        if (lrf.hasIsActive()) result["is-active"] = lrf.isActive
        if (lrf.hasLastMeasurementTime()) result["last-measurement-time"] = lrf.lastMeasurementTime
        
        // Error info
        if (lrf.hasErrorCode()) result["error-code"] = lrf.errorCode
        if (lrf.hasErrorMessage()) result["error-message"] = lrf.errorMessage
        
        return result
    }

    private fun convertTime(time: JonSharedDataTime.JonGuiDataTime): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // System time
        if (time.hasSystemTime()) result["system-time"] = time.systemTime
        if (time.hasUptime()) result["uptime"] = time.uptime
        
        // Time sync
        if (time.hasTimeSyncStatus()) {
            result["time-sync-status"] = time.timeSyncStatus.name.lowercase()
        }
        if (time.hasTimeSyncOffset()) result["time-sync-offset"] = time.timeSyncOffset
        
        return result
    }

    private fun convertCameraDay(camera: JonSharedDataCameraDay.JonGuiDataCameraDay): Map<String, Any> {
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

    private fun convertCameraHeat(camera: JonSharedDataCameraHeat.JonGuiDataCameraHeat): Map<String, Any> {
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
        
        // Palette
        if (camera.hasPalette()) {
            result["palette"] = camera.palette.name.lowercase()
        }
        
        // Status
        if (camera.hasIsActive()) result["is-active"] = camera.isActive
        if (camera.hasIsRecording()) result["is-recording"] = camera.isRecording
        
        return result
    }
}
