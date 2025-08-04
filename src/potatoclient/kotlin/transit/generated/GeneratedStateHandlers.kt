package potatoclient.kotlin.transit.generated

import com.cognitect.transit.WriteHandler
import ser.JonSharedData
import ser.JonSharedDataActualSpaceTime
import ser.JonSharedDataCameraDay
import ser.JonSharedDataCameraHeat
import ser.JonSharedDataCompass
import ser.JonSharedDataCompassCalibration
import ser.JonSharedDataDayCamGlassHeater
import ser.JonSharedDataGps
import ser.JonSharedDataLrf
import ser.JonSharedDataRecOsd
import ser.JonSharedDataRotary
import ser.JonSharedDataSystem
import ser.JonSharedDataTime
import ser.JonSharedDataTypes

/**
 * Generated Transit handlers for state messages.
 *
 * This file is auto-generated from protobuf definitions.
 * DO NOT EDIT - regenerate with: bb generate-kotlin-handlers.clj
 *
 * Generated on: Mon Aug 04 08:09:39 CEST 2025
 */
object GeneratedStateHandlers {
    /**
     * Extract Transit data from state message
     */
    fun extractState(state: JonSharedData.JonGUIState): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (state.hasMeteoInternal()) {
            result["meteo-internal"] = extractMeteoInternal(state.meteoInternal)
        }
        if (state.hasSystem()) {
            result["system"] = extractSystem(state.system)
        }
        if (state.hasGps()) {
            result["gps"] = extractGps(state.gps)
        }
        if (state.hasTime()) {
            result["time"] = extractTime(state.time)
        }
        if (state.hasLrf()) {
            result["lrf"] = extractLrf(state.lrf)
        }
        if (state.hasDayCamGlassHeater()) {
            result["day-cam-glass-heater"] = extractDayCamGlassHeater(state.dayCamGlassHeater)
        }
        if (state.hasRotary()) {
            result["rotary"] = extractRotary(state.rotary)
        }
        if (state.hasCameraDay()) {
            result["camera-day"] = extractCameraDay(state.cameraDay)
        }
        if (state.hasRecOsd()) {
            result["rec-osd"] = extractRecOsd(state.recOsd)
        }
        if (state.hasCompass()) {
            result["compass"] = extractCompass(state.compass)
        }
        if (state.hasCompassCalibration()) {
            result["compass-calibration"] = extractCompassCalibration(state.compassCalibration)
        }
        if (state.hasCameraHeat()) {
            result["camera-heat"] = extractCameraHeat(state.cameraHeat)
        }
        if (state.hasActualSpaceTime()) {
            result["actual-space-time"] = extractActualSpaceTime(state.actualSpaceTime)
        }

        return result
    }

    /**
     * Extract Transit data from JonGuiDataMeteo
     */
    private fun extractMeteoInternal(msg: JonSharedDataTypes.JonGuiDataMeteo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["temperature"] = msg.getTemperature()
        result["humidity"] = msg.getHumidity()
        result["pressure"] = msg.getPressure()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataSystem
     */
    private fun extractSystem(msg: JonSharedDataSystem.JonGuiDataSystem): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["cur-video-rec-dir-month"] = msg.getCurVideoRecDirMonth()
        result["cur-video-rec-dir-hour"] = msg.getCurVideoRecDirHour()
        result["cur-video-rec-dir-year"] = msg.getCurVideoRecDirYear()
        result["cpu-temperature"] = msg.getCpuTemperature()
        result["low-disk-space"] = msg.getLowDiskSpace()
        result["disk-space"] = msg.getDiskSpace()
        result["power-consumption"] = msg.getPowerConsumption()
        result["stabilization-mode"] = msg.getStabilizationMode()
        result["cv-dumping"] = msg.getCvDumping()
        result["vampire-mode"] = msg.getVampireMode()
        result["gpu-load"] = msg.getGpuLoad()
        result["important-rec-enabled"] = msg.getImportantRecEnabled()
        result["cur-video-rec-dir-second"] = msg.getCurVideoRecDirSecond()
        result["cur-video-rec-dir-day"] = msg.getCurVideoRecDirDay()
        result["tracking"] = msg.getTracking()
        result["geodesic-mode"] = msg.getGeodesicMode()
        result["cur-video-rec-dir-minute"] = msg.getCurVideoRecDirMinute()
        result["loc"] =
            msg
                .getLoc()
                .name
                .lowercase()
                .replace("_", "-")
        result["rec-enabled"] = msg.getRecEnabled()
        result["gpu-temperature"] = msg.getGpuTemperature()
        result["cpu-load"] = msg.getCpuLoad()
        result["no-disk-space"] = msg.getNoDiskSpace()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataGps
     */
    private fun extractGps(msg: JonSharedDataGps.JonGuiDataGps): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["longitude"] = msg.getLongitude()
        result["latitude"] = msg.getLatitude()
        result["altitude"] = msg.getAltitude()
        result["manual-longitude"] = msg.getManualLongitude()
        result["manual-latitude"] = msg.getManualLatitude()
        result["manual-altitude"] = msg.getManualAltitude()
        result["fix-type"] =
            msg
                .getFixType()
                .name
                .lowercase()
                .replace("_", "-")
        result["use-manual"] = msg.getUseManual()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataTime
     */
    private fun extractTime(msg: JonSharedDataTime.JonGuiDataTime): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["timestamp"] = msg.getTimestamp()
        result["manual-timestamp"] = msg.getManualTimestamp()
        result["zone-id"] = msg.getZoneId()
        result["use-manual-time"] = msg.getUseManualTime()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataLrf
     */
    private fun extractLrf(msg: JonSharedDataLrf.JonGuiDataLrf): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["is-scanning"] = msg.getIsScanning()
        result["is-measuring"] = msg.getIsMeasuring()
        result["measure-id"] = msg.getMeasureId()
        if (msg.hasTarget()) {
            result["target"] = extractLrfTarget(msg.getTarget())
        }
        result["pointer-mode"] =
            msg
                .getPointerMode()
                .name
                .lowercase()
                .replace("_", "-")
        result["fogModeEnabled"] = msg.getFogModeEnabled()
        result["is-refining"] = msg.getIsRefining()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataTarget
     */
    private fun extractLrfTarget(msg: JonSharedDataLrf.JonGuiDataTarget): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["distance-3b"] = msg.getDistance3B()
        result["session-id"] = msg.getSessionId()
        result["uuid-part4"] = msg.getUuidPart4()
        result["uuid-part2"] = msg.getUuidPart2()
        result["target-altitude"] = msg.getTargetAltitude()
        result["uuid-part3"] = msg.getUuidPart3()
        if (msg.hasTargetColor()) {
            result["target-color"] = extractLrfTargetTargetColor(msg.getTargetColor())
        }
        result["type"] = msg.getType()
        result["observer-altitude"] = msg.getObserverAltitude()
        result["target-id"] = msg.getTargetId()
        result["observer-fix-type"] =
            msg
                .getObserverFixType()
                .name
                .lowercase()
                .replace("_", "-")
        result["observer-elevation"] = msg.getObserverElevation()
        result["observer-azimuth"] = msg.getObserverAzimuth()
        result["observer-longitude"] = msg.getObserverLongitude()
        result["distance-2d"] = msg.getDistance2D()
        result["observer-latitude"] = msg.getObserverLatitude()
        result["target-longitude"] = msg.getTargetLongitude()
        result["timestamp"] = msg.getTimestamp()
        result["uuid-part1"] = msg.getUuidPart1()
        result["target-latitude"] = msg.getTargetLatitude()
        result["observer-bank"] = msg.getObserverBank()

        return result
    }

    /**
     * Extract Transit data from RgbColor
     */
    private fun extractLrfTargetTargetColor(msg: JonSharedDataLrf.RgbColor): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["red"] = msg.getRed()
        result["green"] = msg.getGreen()
        result["blue"] = msg.getBlue()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataDayCamGlassHeater
     */
    private fun extractDayCamGlassHeater(msg: JonSharedDataDayCamGlassHeater.JonGuiDataDayCamGlassHeater): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["temperature"] = msg.getTemperature()
        result["status"] = msg.getStatus()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataRotary
     */
    private fun extractRotary(msg: JonSharedDataRotary.JonGuiDataRotary): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["azimuth-speed"] = msg.getAzimuthSpeed()
        result["sun-azimuth"] = msg.getSunAzimuth()
        result["use-rotary-as-compass"] = msg.getUseRotaryAsCompass()
        result["elevation"] = msg.getElevation()
        if (msg.hasCurrentScanNode()) {
            result["current-scan-node"] = extractRotaryCurrentScanNode(msg.getCurrentScanNode())
        }
        result["is-moving"] = msg.getIsMoving()
        result["platform-elevation"] = msg.getPlatformElevation()
        result["mode"] =
            msg
                .getMode()
                .name
                .lowercase()
                .replace("_", "-")
        result["platform-bank"] = msg.getPlatformBank()
        result["platform-azimuth"] = msg.getPlatformAzimuth()
        result["is-scanning"] = msg.getIsScanning()
        result["is-scanning-paused"] = msg.getIsScanningPaused()
        result["azimuth"] = msg.getAzimuth()
        result["elevation-speed"] = msg.getElevationSpeed()
        result["scan-target-max"] = msg.getScanTargetMax()
        result["scan-target"] = msg.getScanTarget()
        result["sun-elevation"] = msg.getSunElevation()

        return result
    }

    /**
     * Extract Transit data from ScanNode
     */
    private fun extractRotaryCurrentScanNode(msg: JonSharedDataRotary.ScanNode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["index"] = msg.getIndex()
        result["DayZoomTableValue"] = msg.getDayZoomTableValue()
        result["HeatZoomTableValue"] = msg.getHeatZoomTableValue()
        result["azimuth"] = msg.getAzimuth()
        result["elevation"] = msg.getElevation()
        result["linger"] = msg.getLinger()
        result["speed"] = msg.getSpeed()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataCameraDay
     */
    private fun extractCameraDay(msg: JonSharedDataCameraDay.JonGuiDataCameraDay): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["iris-pos"] = msg.getIrisPos()
        result["fx-mode"] =
            msg
                .getFxMode()
                .name
                .lowercase()
                .replace("_", "-")
        result["digital-zoom-level"] = msg.getDigitalZoomLevel()
        result["focus-pos"] = msg.getFocusPos()
        result["zoom-table-pos-max"] = msg.getZoomTablePosMax()
        result["zoom-table-pos"] = msg.getZoomTablePos()
        result["auto-iris"] = msg.getAutoIris()
        result["clahe-level"] = msg.getClaheLevel()
        result["auto-focus"] = msg.getAutoFocus()
        result["zoom-pos"] = msg.getZoomPos()
        result["infrared-filter"] = msg.getInfraredFilter()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataRecOsd
     */
    private fun extractRecOsd(msg: JonSharedDataRecOsd.JonGuiDataRecOsd): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["screen"] =
            msg
                .getScreen()
                .name
                .lowercase()
                .replace("_", "-")
        result["heat-osd-enabled"] = msg.getHeatOsdEnabled()
        result["day-osd-enabled"] = msg.getDayOsdEnabled()
        result["heat-crosshair-offset-horizontal"] = msg.getHeatCrosshairOffsetHorizontal()
        result["heat-crosshair-offset-vertical"] = msg.getHeatCrosshairOffsetVertical()
        result["day-crosshair-offset-horizontal"] = msg.getDayCrosshairOffsetHorizontal()
        result["day-crosshair-offset-vertical"] = msg.getDayCrosshairOffsetVertical()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataCompass
     */
    private fun extractCompass(msg: JonSharedDataCompass.JonGuiDataCompass): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["azimuth"] = msg.getAzimuth()
        result["elevation"] = msg.getElevation()
        result["bank"] = msg.getBank()
        result["offsetAzimuth"] = msg.getOffsetAzimuth()
        result["offsetElevation"] = msg.getOffsetElevation()
        result["magneticDeclination"] = msg.getMagneticDeclination()
        result["calibrating"] = msg.getCalibrating()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataCompassCalibration
     */
    private fun extractCompassCalibration(msg: JonSharedDataCompassCalibration.JonGuiDataCompassCalibration): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["stage"] = msg.getStage()
        result["final-stage"] = msg.getFinalStage()
        result["target-azimuth"] = msg.getTargetAzimuth()
        result["target-elevation"] = msg.getTargetElevation()
        result["target-bank"] = msg.getTargetBank()
        result["status"] =
            msg
                .getStatus()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from JonGuiDataCameraHeat
     */
    private fun extractCameraHeat(msg: JonSharedDataCameraHeat.JonGuiDataCameraHeat): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["fx-mode"] =
            msg
                .getFxMode()
                .name
                .lowercase()
                .replace("_", "-")
        result["digital-zoom-level"] = msg.getDigitalZoomLevel()
        result["dde-enabled"] = msg.getDdeEnabled()
        result["zoom-table-pos-max"] = msg.getZoomTablePosMax()
        result["zoom-table-pos"] = msg.getZoomTablePos()
        result["filter"] =
            msg
                .getFilter()
                .name
                .lowercase()
                .replace("_", "-")
        result["clahe-level"] = msg.getClaheLevel()
        result["auto-focus"] = msg.getAutoFocus()
        result["dde-level"] = msg.getDdeLevel()
        result["zoom-pos"] = msg.getZoomPos()
        result["agc-mode"] =
            msg
                .getAgcMode()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from JonGuiDataActualSpaceTime
     */
    private fun extractActualSpaceTime(msg: JonSharedDataActualSpaceTime.JonGuiDataActualSpaceTime): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["azimuth"] = msg.getAzimuth()
        result["elevation"] = msg.getElevation()
        result["bank"] = msg.getBank()
        result["latitude"] = msg.getLatitude()
        result["longitude"] = msg.getLongitude()
        result["altitude"] = msg.getAltitude()
        result["timestamp"] = msg.getTimestamp()

        return result
    }
}

/**
 * Transit write handler for state messages
 */
class GeneratedStateWriteHandler : WriteHandler<JonSharedData.JonGUIState, Any> {
    override fun tag(o: JonSharedData.JonGUIState): String = "state"

    override fun rep(o: JonSharedData.JonGUIState): Any = GeneratedStateHandlers.extractState(o)

    override fun stringRep(o: JonSharedData.JonGUIState): String? = null

    override fun <V : Any> getVerboseHandler(): WriteHandler<JonSharedData.JonGUIState, V>? = null
}
