package potatoclient.kotlin.transit.generated

import com.cognitect.transit.WriteHandler
import mu.KotlinLogging
import ser.*

private val logger = KotlinLogging.logger {}

/**
 * Generated Transit handlers for state messages.
 *
 * This file is auto-generated from protobuf definitions.
 * DO NOT EDIT - regenerate with: bb generate-kotlin-handlers.clj
 *
 * Generated on: Sun Aug 03 16:05:33 CEST 2025
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
    private fun extractMeteoInternal(msg: `ser.JonSharedDataTypes$JonGuiDataMeteo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["temperature"] = msg.getTemperature()
        result["humidity"] = msg.getHumidity()
        result["pressure"] = msg.getPressure()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataSystem
     */
    private fun extractSystem(msg: `ser.JonSharedDataSystem$JonGuiDataSystem`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["cur-video-rec-dir-month"] = msg.getCur_video_rec_dir_month()
        result["cur-video-rec-dir-hour"] = msg.getCur_video_rec_dir_hour()
        result["cur-video-rec-dir-year"] = msg.getCur_video_rec_dir_year()
        result["cpu-temperature"] = msg.getCpu_temperature()
        result["low-disk-space"] = msg.getLow_disk_space()
        result["disk-space"] = msg.getDisk_space()
        result["power-consumption"] = msg.getPower_consumption()
        result["stabilization-mode"] = msg.getStabilization_mode()
        result["cv-dumping"] = msg.getCv_dumping()
        result["vampire-mode"] = msg.getVampire_mode()
        result["gpu-load"] = msg.getGpu_load()
        result["important-rec-enabled"] = msg.getImportant_rec_enabled()
        result["cur-video-rec-dir-second"] = msg.getCur_video_rec_dir_second()
        result["cur-video-rec-dir-day"] = msg.getCur_video_rec_dir_day()
        result["tracking"] = msg.getTracking()
        result["geodesic-mode"] = msg.getGeodesic_mode()
        result["cur-video-rec-dir-minute"] = msg.getCur_video_rec_dir_minute()
        result["loc"] =
            msg
                .getLoc()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["rec-enabled"] = msg.getRec_enabled()
        result["gpu-temperature"] = msg.getGpu_temperature()
        result["cpu-load"] = msg.getCpu_load()
        result["no-disk-space"] = msg.getNo_disk_space()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataGps
     */
    private fun extractGps(msg: `ser.JonSharedDataGps$JonGuiDataGps`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["longitude"] = msg.getLongitude()
        result["latitude"] = msg.getLatitude()
        result["altitude"] = msg.getAltitude()
        result["manual-longitude"] = msg.getManual_longitude()
        result["manual-latitude"] = msg.getManual_latitude()
        result["manual-altitude"] = msg.getManual_altitude()
        result["fix-type"] =
            msg
                .getFix_type()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["use-manual"] = msg.getUse_manual()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataTime
     */
    private fun extractTime(msg: `ser.JonSharedDataTime$JonGuiDataTime`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["timestamp"] = msg.getTimestamp()
        result["manual-timestamp"] = msg.getManual_timestamp()
        result["zone-id"] = msg.getZone_id()
        result["use-manual-time"] = msg.getUse_manual_time()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataLrf
     */
    private fun extractLrf(msg: `ser.JonSharedDataLrf$JonGuiDataLrf`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["is-scanning"] = msg.getIs_scanning()
        result["is-measuring"] = msg.getIs_measuring()
        result["measure-id"] = msg.getMeasure_id()
        if (msg.hasTarget()) {
            result["target"] = extractTarget(msg.getTarget())
        }
        result["pointer-mode"] =
            msg
                .getPointer_mode()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["fogModeEnabled"] = msg.getFogmodeenabled()
        result["is-refining"] = msg.getIs_refining()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataTarget
     */
    private fun extractTarget(msg: `ser.JonSharedDataLrf$JonGuiDataTarget`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["distance-3b"] = msg.getDistance_3b()
        result["session-id"] = msg.getSession_id()
        result["uuid-part4"] = msg.getUuid_part4()
        result["uuid-part2"] = msg.getUuid_part2()
        result["target-altitude"] = msg.getTarget_altitude()
        result["uuid-part3"] = msg.getUuid_part3()
        if (msg.hasTarget_color()) {
            result["target-color"] = extractTargetColor(msg.getTarget_color())
        }
        result["type"] = msg.getType()
        result["observer-altitude"] = msg.getObserver_altitude()
        result["target-id"] = msg.getTarget_id()
        result["observer-fix-type"] =
            msg
                .getObserver_fix_type()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["observer-elevation"] = msg.getObserver_elevation()
        result["observer-azimuth"] = msg.getObserver_azimuth()
        result["observer-longitude"] = msg.getObserver_longitude()
        result["distance-2d"] = msg.getDistance_2d()
        result["observer-latitude"] = msg.getObserver_latitude()
        result["target-longitude"] = msg.getTarget_longitude()
        result["timestamp"] = msg.getTimestamp()
        result["uuid-part1"] = msg.getUuid_part1()
        result["target-latitude"] = msg.getTarget_latitude()
        result["observer-bank"] = msg.getObserver_bank()

        return result
    }

    /**
     * Extract Transit data from RgbColor
     */
    private fun extractTargetColor(msg: `ser.JonSharedDataLrf$RgbColor`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["red"] = msg.getRed()
        result["green"] = msg.getGreen()
        result["blue"] = msg.getBlue()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataDayCamGlassHeater
     */
    private fun extractDayCamGlassHeater(msg: `ser.JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["temperature"] = msg.getTemperature()
        result["status"] = msg.getStatus()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataRotary
     */
    private fun extractRotary(msg: `ser.JonSharedDataRotary$JonGuiDataRotary`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["azimuth-speed"] = msg.getAzimuth_speed()
        result["sun-azimuth"] = msg.getSun_azimuth()
        result["use-rotary-as-compass"] = msg.getUse_rotary_as_compass()
        result["elevation"] = msg.getElevation()
        if (msg.hasCurrent_scan_node()) {
            result["current-scan-node"] = extractCurrentScanNode(msg.getCurrent_scan_node())
        }
        result["is-moving"] = msg.getIs_moving()
        result["platform-elevation"] = msg.getPlatform_elevation()
        result["mode"] =
            msg
                .getMode()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["platform-bank"] = msg.getPlatform_bank()
        result["platform-azimuth"] = msg.getPlatform_azimuth()
        result["is-scanning"] = msg.getIs_scanning()
        result["is-scanning-paused"] = msg.getIs_scanning_paused()
        result["azimuth"] = msg.getAzimuth()
        result["elevation-speed"] = msg.getElevation_speed()
        result["scan-target-max"] = msg.getScan_target_max()
        result["scan-target"] = msg.getScan_target()
        result["sun-elevation"] = msg.getSun_elevation()

        return result
    }

    /**
     * Extract Transit data from ScanNode
     */
    private fun extractCurrentScanNode(msg: `ser.JonSharedDataRotary$ScanNode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["index"] = msg.getIndex()
        result["DayZoomTableValue"] = msg.getDayzoomtablevalue()
        result["HeatZoomTableValue"] = msg.getHeatzoomtablevalue()
        result["azimuth"] = msg.getAzimuth()
        result["elevation"] = msg.getElevation()
        result["linger"] = msg.getLinger()
        result["speed"] = msg.getSpeed()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataCameraDay
     */
    private fun extractCameraDay(msg: `ser.JonSharedDataCameraDay$JonGuiDataCameraDay`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["iris-pos"] = msg.getIris_pos()
        result["fx-mode"] =
            msg
                .getFx_mode()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["digital-zoom-level"] = msg.getDigital_zoom_level()
        result["focus-pos"] = msg.getFocus_pos()
        result["zoom-table-pos-max"] = msg.getZoom_table_pos_max()
        result["zoom-table-pos"] = msg.getZoom_table_pos()
        result["auto-iris"] = msg.getAuto_iris()
        result["clahe-level"] = msg.getClahe_level()
        result["auto-focus"] = msg.getAuto_focus()
        result["zoom-pos"] = msg.getZoom_pos()
        result["infrared-filter"] = msg.getInfrared_filter()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataRecOsd
     */
    private fun extractRecOsd(msg: `ser.JonSharedDataRecOsd$JonGuiDataRecOsd`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["screen"] =
            msg
                .getScreen()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["heat-osd-enabled"] = msg.getHeat_osd_enabled()
        result["day-osd-enabled"] = msg.getDay_osd_enabled()
        result["heat-crosshair-offset-horizontal"] = msg.getHeat_crosshair_offset_horizontal()
        result["heat-crosshair-offset-vertical"] = msg.getHeat_crosshair_offset_vertical()
        result["day-crosshair-offset-horizontal"] = msg.getDay_crosshair_offset_horizontal()
        result["day-crosshair-offset-vertical"] = msg.getDay_crosshair_offset_vertical()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataCompass
     */
    private fun extractCompass(msg: `ser.JonSharedDataCompass$JonGuiDataCompass`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["azimuth"] = msg.getAzimuth()
        result["elevation"] = msg.getElevation()
        result["bank"] = msg.getBank()
        result["offsetAzimuth"] = msg.getOffsetazimuth()
        result["offsetElevation"] = msg.getOffsetelevation()
        result["magneticDeclination"] = msg.getMagneticdeclination()
        result["calibrating"] = msg.getCalibrating()

        return result
    }

    /**
     * Extract Transit data from JonGuiDataCompassCalibration
     */
    private fun extractCompassCalibration(msg: `ser.JonSharedDataCompassCalibration$JonGuiDataCompassCalibration`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["stage"] = msg.getStage()
        result["final-stage"] = msg.getFinal_stage()
        result["target-azimuth"] = msg.getTarget_azimuth()
        result["target-elevation"] = msg.getTarget_elevation()
        result["target-bank"] = msg.getTarget_bank()
        result["status"] =
            msg
                .getStatus()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from JonGuiDataCameraHeat
     */
    private fun extractCameraHeat(msg: `ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["fx-mode"] =
            msg
                .getFx_mode()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["digital-zoom-level"] = msg.getDigital_zoom_level()
        result["dde-enabled"] = msg.getDde_enabled()
        result["zoom-table-pos-max"] = msg.getZoom_table_pos_max()
        result["zoom-table-pos"] = msg.getZoom_table_pos()
        result["filter"] =
            msg
                .getFilter()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["clahe-level"] = msg.getClahe_level()
        result["auto-focus"] = msg.getAuto_focus()
        result["dde-level"] = msg.getDde_level()
        result["zoom-pos"] = msg.getZoom_pos()
        result["agc-mode"] =
            msg
                .getAgc_mode()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from JonGuiDataActualSpaceTime
     */
    private fun extractActualSpaceTime(msg: `ser.JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime`): Map<String, Any?> {
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
class GeneratedStateWriteHandler : WriteHandler<JonSharedData.JonGUIState> {
    override fun tag(o: JonSharedData.JonGUIState): String = "state"

    override fun rep(o: JonSharedData.JonGUIState): Any = GeneratedStateHandlers.extractState(o)
}
