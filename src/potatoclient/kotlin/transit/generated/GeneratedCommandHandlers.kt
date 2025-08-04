package potatoclient.kotlin.transit.generated

import clojure.lang.Keyword
import cmd.CV.JonSharedCmdCv
import cmd.Compass.JonSharedCmdCompass
import cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater
import cmd.DayCamera.JonSharedCmdDayCamera
import cmd.Gps.JonSharedCmdGps
import cmd.HeatCamera.JonSharedCmdHeatCamera
import cmd.JonSharedCmd
import cmd.Lira.JonSharedCmdLira
import cmd.Lrf.JonSharedCmdLrf
import cmd.Lrf_calib.JonSharedCmdLrfAlign
import cmd.OSD.JonSharedCmdOsd
import cmd.RotaryPlatform.JonSharedCmdRotary
import cmd.System.JonSharedCmdSystem
import com.cognitect.transit.ReadHandler
import com.cognitect.transit.WriteHandler
import com.google.protobuf.Message
import potatoclient.kotlin.transit.LoggingUtils

/**
 * Generated Transit handlers for command messages.
 *
 * This file is auto-generated from protobuf definitions.
 * DO NOT EDIT - regenerate with: bb generate-kotlin-handlers.clj
 *
 * Generated on: Mon Aug 04 08:09:39 CEST 2025
 */
object GeneratedCommandHandlers {
    /**
     * Build command root from Transit data
     */
    fun buildCommand(data: Map<*, *>): JonSharedCmd.Root {
        val builder = JonSharedCmd.Root.newBuilder()

        // Set protocol version if present
        val version = data["protocol-version"] ?: data["protocolVersion"]
        if (version != null) {
            builder.protocolVersion = convertInt(version)
        }

        // Find and build the command
        for ((key, value) in data) {
            if (value == null || key == "protocol-version" || key == "protocolVersion") continue

            when (keyToString(key)) {
                "osd" -> builder.setOsd(buildOsd(value as Map<*, *>))
                "ping" -> builder.setPing(buildPing(value as Map<*, *>))
                "system" -> builder.setSystem(buildSystem(value as Map<*, *>))
                "noop" -> builder.setNoop(buildNoop(value as Map<*, *>))
                "cv" -> builder.setCv(buildCv(value as Map<*, *>))
                "gps" -> builder.setGps(buildGps(value as Map<*, *>))
                "lrf" -> builder.setLrf(buildLrf(value as Map<*, *>))
                "day-cam-glass-heater" -> builder.setDayCamGlassHeater(buildDayCamGlassHeater(value as Map<*, *>))
                "day-camera" -> builder.setDayCamera(buildDayCamera(value as Map<*, *>))
                "heat-camera" -> builder.setHeatCamera(buildHeatCamera(value as Map<*, *>))
                "lira" -> builder.setLira(buildLira(value as Map<*, *>))
                "lrf-calib" -> builder.setLrfCalib(buildLrfCalib(value as Map<*, *>))
                "rotary" -> builder.setRotary(buildRotary(value as Map<*, *>))
                "compass" -> builder.setCompass(buildCompass(value as Map<*, *>))
                "frozen" -> builder.setFrozen(buildFrozen(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown command type: \$key")
            }
        }

        return builder.build()
    }

    /**
     * Extract Transit data from command message
     */
    fun extractCommand(root: JonSharedCmd.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        result["protocol-version"] = root.protocolVersion

        when (root.payloadCase) {
            JonSharedCmd.Root.PayloadCase.OSD ->
                result["osd"] = extractOsd(root.osd)
            JonSharedCmd.Root.PayloadCase.PING ->
                result["ping"] = extractPing(root.ping)
            JonSharedCmd.Root.PayloadCase.SYSTEM ->
                result["system"] = extractSystem(root.system)
            JonSharedCmd.Root.PayloadCase.NOOP ->
                result["noop"] = extractNoop(root.noop)
            JonSharedCmd.Root.PayloadCase.CV ->
                result["cv"] = extractCv(root.cv)
            JonSharedCmd.Root.PayloadCase.GPS ->
                result["gps"] = extractGps(root.gps)
            JonSharedCmd.Root.PayloadCase.LRF ->
                result["lrf"] = extractLrf(root.lrf)
            JonSharedCmd.Root.PayloadCase.DAY_CAM_GLASS_HEATER ->
                result["day-cam-glass-heater"] = extractDayCamGlassHeater(root.dayCamGlassHeater)
            JonSharedCmd.Root.PayloadCase.DAY_CAMERA ->
                result["day-camera"] = extractDayCamera(root.dayCamera)
            JonSharedCmd.Root.PayloadCase.HEAT_CAMERA ->
                result["heat-camera"] = extractHeatCamera(root.heatCamera)
            JonSharedCmd.Root.PayloadCase.LIRA ->
                result["lira"] = extractLira(root.lira)
            JonSharedCmd.Root.PayloadCase.LRF_CALIB ->
                result["lrf-calib"] = extractLrfCalib(root.lrfCalib)
            JonSharedCmd.Root.PayloadCase.ROTARY ->
                result["rotary"] = extractRotary(root.rotary)
            JonSharedCmd.Root.PayloadCase.COMPASS ->
                result["compass"] = extractCompass(root.compass)
            JonSharedCmd.Root.PayloadCase.FROZEN ->
                result["frozen"] = extractFrozen(root.frozen)
            JonSharedCmd.Root.PayloadCase.PAYLOAD_NOT_SET ->
                LoggingUtils.log("WARN", "Command not set")
            else ->
                LoggingUtils.log("WARN", "Unknown payload case: \${root.payloadCase}")
        }

        return result
    }

    /**
     * Build Root from Transit data
     */
    private fun buildOsd(data: Map<*, *>): JonSharedCmdOsd.Root {
        val builder = JonSharedCmdOsd.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "show-default-screen" -> builder.setShowDefaultScreen(buildOsdShowDefaultScreen(value as Map<*, *>))
                "show-lrf-measure-screen" -> builder.setShowLrfMeasureScreen(buildOsdShowLrfMeasureScreen(value as Map<*, *>))
                "show-lrf-result-screen" -> builder.setShowLrfResultScreen(buildOsdShowLrfResultScreen(value as Map<*, *>))
                "show-lrf-result-simplified-screen" ->
                    builder.setShowLrfResultSimplifiedScreen(
                        buildOsdShowLrfResultSimplifiedScreen(value as Map<*, *>),
                    )
                "enable-heat-osd" -> builder.setEnableHeatOsd(buildOsdEnableHeatOsd(value as Map<*, *>))
                "disable-heat-osd" -> builder.setDisableHeatOsd(buildOsdDisableHeatOsd(value as Map<*, *>))
                "enable-day-osd" -> builder.setEnableDayOsd(buildOsdEnableDayOsd(value as Map<*, *>))
                "disable-day-osd" -> builder.setDisableDayOsd(buildOsdDisableDayOsd(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build ShowDefaultScreen from Transit data
     */
    private fun buildOsdShowDefaultScreen(data: Map<*, *>): JonSharedCmdOsd.ShowDefaultScreen {
        val builder = JonSharedCmdOsd.ShowDefaultScreen.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShowDefaultScreen")
            }
        }

        return builder.build()
    }

    /**
     * Build ShowLRFMeasureScreen from Transit data
     */
    private fun buildOsdShowLrfMeasureScreen(data: Map<*, *>): JonSharedCmdOsd.ShowLRFMeasureScreen {
        val builder = JonSharedCmdOsd.ShowLRFMeasureScreen.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShowLRFMeasureScreen")
            }
        }

        return builder.build()
    }

    /**
     * Build ShowLRFResultScreen from Transit data
     */
    private fun buildOsdShowLrfResultScreen(data: Map<*, *>): JonSharedCmdOsd.ShowLRFResultScreen {
        val builder = JonSharedCmdOsd.ShowLRFResultScreen.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShowLRFResultScreen")
            }
        }

        return builder.build()
    }

    /**
     * Build ShowLRFResultSimplifiedScreen from Transit data
     */
    private fun buildOsdShowLrfResultSimplifiedScreen(data: Map<*, *>): JonSharedCmdOsd.ShowLRFResultSimplifiedScreen {
        val builder = JonSharedCmdOsd.ShowLRFResultSimplifiedScreen.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShowLRFResultSimplifiedScreen")
            }
        }

        return builder.build()
    }

    /**
     * Build EnableHeatOSD from Transit data
     */
    private fun buildOsdEnableHeatOsd(data: Map<*, *>): JonSharedCmdOsd.EnableHeatOSD {
        val builder = JonSharedCmdOsd.EnableHeatOSD.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for EnableHeatOSD")
            }
        }

        return builder.build()
    }

    /**
     * Build DisableHeatOSD from Transit data
     */
    private fun buildOsdDisableHeatOsd(data: Map<*, *>): JonSharedCmdOsd.DisableHeatOSD {
        val builder = JonSharedCmdOsd.DisableHeatOSD.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for DisableHeatOSD")
            }
        }

        return builder.build()
    }

    /**
     * Build EnableDayOSD from Transit data
     */
    private fun buildOsdEnableDayOsd(data: Map<*, *>): JonSharedCmdOsd.EnableDayOSD {
        val builder = JonSharedCmdOsd.EnableDayOSD.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for EnableDayOSD")
            }
        }

        return builder.build()
    }

    /**
     * Build DisableDayOSD from Transit data
     */
    private fun buildOsdDisableDayOsd(data: Map<*, *>): JonSharedCmdOsd.DisableDayOSD {
        val builder = JonSharedCmdOsd.DisableDayOSD.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for DisableDayOSD")
            }
        }

        return builder.build()
    }

    /**
     * Build Ping from Transit data
     */
    private fun buildPing(data: Map<*, *>): JonSharedCmd.Ping {
        val builder = JonSharedCmd.Ping.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Ping")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildSystem(data: Map<*, *>): JonSharedCmdSystem.Root {
        val builder = JonSharedCmdSystem.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "geodesic-mode-disable" -> builder.setGeodesicModeDisable(buildSystemGeodesicModeDisable(value as Map<*, *>))
                "start-all" -> builder.setStartAll(buildSystemStartAll(value as Map<*, *>))
                "geodesic-mode-enable" -> builder.setGeodesicModeEnable(buildSystemGeodesicModeEnable(value as Map<*, *>))
                "localization" -> builder.setLocalization(buildSystemLocalization(value as Map<*, *>))
                "unmark-rec-important" -> builder.setUnmarkRecImportant(buildSystemUnmarkRecImportant(value as Map<*, *>))
                "stop-rec" -> builder.setStopRec(buildSystemStopRec(value as Map<*, *>))
                "reboot" -> builder.setReboot(buildSystemReboot(value as Map<*, *>))
                "start-rec" -> builder.setStartRec(buildSystemStartRec(value as Map<*, *>))
                "power-off" -> builder.setPowerOff(buildSystemPowerOff(value as Map<*, *>))
                "reset-configs" -> builder.setResetConfigs(buildSystemResetConfigs(value as Map<*, *>))
                "stop-all" -> builder.setStopAll(buildSystemStopAll(value as Map<*, *>))
                "enter-transport" -> builder.setEnterTransport(buildSystemEnterTransport(value as Map<*, *>))
                "mark-rec-important" -> builder.setMarkRecImportant(buildSystemMarkRecImportant(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build DisableGeodesicMode from Transit data
     */
    private fun buildSystemGeodesicModeDisable(data: Map<*, *>): JonSharedCmdSystem.DisableGeodesicMode {
        val builder = JonSharedCmdSystem.DisableGeodesicMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for DisableGeodesicMode")
            }
        }

        return builder.build()
    }

    /**
     * Build StartALl from Transit data
     */
    private fun buildSystemStartAll(data: Map<*, *>): JonSharedCmdSystem.StartALl {
        val builder = JonSharedCmdSystem.StartALl.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for StartALl")
            }
        }

        return builder.build()
    }

    /**
     * Build EnableGeodesicMode from Transit data
     */
    private fun buildSystemGeodesicModeEnable(data: Map<*, *>): JonSharedCmdSystem.EnableGeodesicMode {
        val builder = JonSharedCmdSystem.EnableGeodesicMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for EnableGeodesicMode")
            }
        }

        return builder.build()
    }

    /**
     * Build SetLocalization from Transit data
     */
    private fun buildSystemLocalization(data: Map<*, *>): JonSharedCmdSystem.SetLocalization {
        val builder = JonSharedCmdSystem.SetLocalization.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "loc" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setLoc(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetLocalization")
            }
        }

        return builder.build()
    }

    /**
     * Build UnmarkRecImportant from Transit data
     */
    private fun buildSystemUnmarkRecImportant(data: Map<*, *>): JonSharedCmdSystem.UnmarkRecImportant {
        val builder = JonSharedCmdSystem.UnmarkRecImportant.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for UnmarkRecImportant")
            }
        }

        return builder.build()
    }

    /**
     * Build StopRec from Transit data
     */
    private fun buildSystemStopRec(data: Map<*, *>): JonSharedCmdSystem.StopRec {
        val builder = JonSharedCmdSystem.StopRec.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for StopRec")
            }
        }

        return builder.build()
    }

    /**
     * Build Reboot from Transit data
     */
    private fun buildSystemReboot(data: Map<*, *>): JonSharedCmdSystem.Reboot {
        val builder = JonSharedCmdSystem.Reboot.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Reboot")
            }
        }

        return builder.build()
    }

    /**
     * Build StartRec from Transit data
     */
    private fun buildSystemStartRec(data: Map<*, *>): JonSharedCmdSystem.StartRec {
        val builder = JonSharedCmdSystem.StartRec.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for StartRec")
            }
        }

        return builder.build()
    }

    /**
     * Build PowerOff from Transit data
     */
    private fun buildSystemPowerOff(data: Map<*, *>): JonSharedCmdSystem.PowerOff {
        val builder = JonSharedCmdSystem.PowerOff.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for PowerOff")
            }
        }

        return builder.build()
    }

    /**
     * Build ResetConfigs from Transit data
     */
    private fun buildSystemResetConfigs(data: Map<*, *>): JonSharedCmdSystem.ResetConfigs {
        val builder = JonSharedCmdSystem.ResetConfigs.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ResetConfigs")
            }
        }

        return builder.build()
    }

    /**
     * Build StopALl from Transit data
     */
    private fun buildSystemStopAll(data: Map<*, *>): JonSharedCmdSystem.StopALl {
        val builder = JonSharedCmdSystem.StopALl.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for StopALl")
            }
        }

        return builder.build()
    }

    /**
     * Build EnterTransport from Transit data
     */
    private fun buildSystemEnterTransport(data: Map<*, *>): JonSharedCmdSystem.EnterTransport {
        val builder = JonSharedCmdSystem.EnterTransport.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for EnterTransport")
            }
        }

        return builder.build()
    }

    /**
     * Build MarkRecImportant from Transit data
     */
    private fun buildSystemMarkRecImportant(data: Map<*, *>): JonSharedCmdSystem.MarkRecImportant {
        val builder = JonSharedCmdSystem.MarkRecImportant.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for MarkRecImportant")
            }
        }

        return builder.build()
    }

    /**
     * Build Noop from Transit data
     */
    private fun buildNoop(data: Map<*, *>): JonSharedCmd.Noop {
        val builder = JonSharedCmd.Noop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Noop")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildCv(data: Map<*, *>): JonSharedCmdCv.Root {
        val builder = JonSharedCmdCv.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "vampire-mode-enable" -> builder.setVampireModeEnable(buildCvVampireModeEnable(value as Map<*, *>))
                "vampire-mode-disable" -> builder.setVampireModeDisable(buildCvVampireModeDisable(value as Map<*, *>))
                "dump-stop" -> builder.setDumpStop(buildCvDumpStop(value as Map<*, *>))
                "stabilization-mode-disable" -> builder.setStabilizationModeDisable(buildCvStabilizationModeDisable(value as Map<*, *>))
                "set-auto-focus" -> builder.setSetAutoFocus(buildCvSetAutoFocus(value as Map<*, *>))
                "start-track-ndc" -> builder.setStartTrackNdc(buildCvStartTrackNdc(value as Map<*, *>))
                "dump-start" -> builder.setDumpStart(buildCvDumpStart(value as Map<*, *>))
                "stop-track" -> builder.setStopTrack(buildCvStopTrack(value as Map<*, *>))
                "stabilization-mode-enable" -> builder.setStabilizationModeEnable(buildCvStabilizationModeEnable(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build VampireModeEnable from Transit data
     */
    private fun buildCvVampireModeEnable(data: Map<*, *>): JonSharedCmdCv.VampireModeEnable {
        val builder = JonSharedCmdCv.VampireModeEnable.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for VampireModeEnable")
            }
        }

        return builder.build()
    }

    /**
     * Build VampireModeDisable from Transit data
     */
    private fun buildCvVampireModeDisable(data: Map<*, *>): JonSharedCmdCv.VampireModeDisable {
        val builder = JonSharedCmdCv.VampireModeDisable.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for VampireModeDisable")
            }
        }

        return builder.build()
    }

    /**
     * Build DumpStop from Transit data
     */
    private fun buildCvDumpStop(data: Map<*, *>): JonSharedCmdCv.DumpStop {
        val builder = JonSharedCmdCv.DumpStop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for DumpStop")
            }
        }

        return builder.build()
    }

    /**
     * Build StabilizationModeDisable from Transit data
     */
    private fun buildCvStabilizationModeDisable(data: Map<*, *>): JonSharedCmdCv.StabilizationModeDisable {
        val builder = JonSharedCmdCv.StabilizationModeDisable.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for StabilizationModeDisable")
            }
        }

        return builder.build()
    }

    /**
     * Build SetAutoFocus from Transit data
     */
    private fun buildCvSetAutoFocus(data: Map<*, *>): JonSharedCmdCv.SetAutoFocus {
        val builder = JonSharedCmdCv.SetAutoFocus.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "channel" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setChannel(SomeEnumType.valueOf(enumValue))
                }
                "value" -> builder.setValue(convertBoolean(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetAutoFocus")
            }
        }

        return builder.build()
    }

    /**
     * Build StartTrackNDC from Transit data
     */
    private fun buildCvStartTrackNdc(data: Map<*, *>): JonSharedCmdCv.StartTrackNDC {
        val builder = JonSharedCmdCv.StartTrackNDC.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "channel" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setChannel(SomeEnumType.valueOf(enumValue))
                }
                "x" -> builder.setX(convertFloat(value))
                "y" -> builder.setY(convertFloat(value))
                "frame-time" -> builder.setFrameTime(convertLong(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for StartTrackNDC")
            }
        }

        return builder.build()
    }

    /**
     * Build DumpStart from Transit data
     */
    private fun buildCvDumpStart(data: Map<*, *>): JonSharedCmdCv.DumpStart {
        val builder = JonSharedCmdCv.DumpStart.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for DumpStart")
            }
        }

        return builder.build()
    }

    /**
     * Build StopTrack from Transit data
     */
    private fun buildCvStopTrack(data: Map<*, *>): JonSharedCmdCv.StopTrack {
        val builder = JonSharedCmdCv.StopTrack.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for StopTrack")
            }
        }

        return builder.build()
    }

    /**
     * Build StabilizationModeEnable from Transit data
     */
    private fun buildCvStabilizationModeEnable(data: Map<*, *>): JonSharedCmdCv.StabilizationModeEnable {
        val builder = JonSharedCmdCv.StabilizationModeEnable.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for StabilizationModeEnable")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildGps(data: Map<*, *>): JonSharedCmdGps.Root {
        val builder = JonSharedCmdGps.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "start" -> builder.setStart(buildGpsStart(value as Map<*, *>))
                "stop" -> builder.setStop(buildGpsStop(value as Map<*, *>))
                "set-manual-position" -> builder.setSetManualPosition(buildGpsSetManualPosition(value as Map<*, *>))
                "set-use-manual-position" -> builder.setSetUseManualPosition(buildGpsSetUseManualPosition(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildGpsGetMeteo(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildGpsStart(data: Map<*, *>): JonSharedCmdGps.Start {
        val builder = JonSharedCmdGps.Start.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Start")
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildGpsStop(data: Map<*, *>): JonSharedCmdGps.Stop {
        val builder = JonSharedCmdGps.Stop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Stop")
            }
        }

        return builder.build()
    }

    /**
     * Build SetManualPosition from Transit data
     */
    private fun buildGpsSetManualPosition(data: Map<*, *>): JonSharedCmdGps.SetManualPosition {
        val builder = JonSharedCmdGps.SetManualPosition.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "latitude" -> builder.setLatitude(convertFloat(value))
                "longitude" -> builder.setLongitude(convertFloat(value))
                "altitude" -> builder.setAltitude(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetManualPosition")
            }
        }

        return builder.build()
    }

    /**
     * Build SetUseManualPosition from Transit data
     */
    private fun buildGpsSetUseManualPosition(data: Map<*, *>): JonSharedCmdGps.SetUseManualPosition {
        val builder = JonSharedCmdGps.SetUseManualPosition.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "flag" -> builder.setFlag(convertBoolean(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetUseManualPosition")
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildGpsGetMeteo(data: Map<*, *>): JonSharedCmdGps.GetMeteo {
        val builder = JonSharedCmdGps.GetMeteo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for GetMeteo")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildLrf(data: Map<*, *>): JonSharedCmdLrf.Root {
        val builder = JonSharedCmdLrf.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "target-designator-off" -> builder.setTargetDesignatorOff(buildLrfTargetDesignatorOff(value as Map<*, *>))
                "target-designator-on-mode-b" -> builder.setTargetDesignatorOnModeB(buildLrfTargetDesignatorOnModeB(value as Map<*, *>))
                "disable-fog-mode" -> builder.setDisableFogMode(buildLrfDisableFogMode(value as Map<*, *>))
                "set-scan-mode" -> builder.setSetScanMode(buildLrfSetScanMode(value as Map<*, *>))
                "refine-off" -> builder.setRefineOff(buildLrfRefineOff(value as Map<*, *>))
                "scan-off" -> builder.setScanOff(buildLrfScanOff(value as Map<*, *>))
                "refine-on" -> builder.setRefineOn(buildLrfRefineOn(value as Map<*, *>))
                "start" -> builder.setStart(buildLrfStart(value as Map<*, *>))
                "measure" -> builder.setMeasure(buildLrfMeasure(value as Map<*, *>))
                "scan-on" -> builder.setScanOn(buildLrfScanOn(value as Map<*, *>))
                "stop" -> builder.setStop(buildLrfStop(value as Map<*, *>))
                "new-session" -> builder.setNewSession(buildLrfNewSession(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildLrfGetMeteo(value as Map<*, *>))
                "enable-fog-mode" -> builder.setEnableFogMode(buildLrfEnableFogMode(value as Map<*, *>))
                "target-designator-on-mode-a" -> builder.setTargetDesignatorOnModeA(buildLrfTargetDesignatorOnModeA(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build TargetDesignatorOff from Transit data
     */
    private fun buildLrfTargetDesignatorOff(data: Map<*, *>): JonSharedCmdLrf.TargetDesignatorOff {
        val builder = JonSharedCmdLrf.TargetDesignatorOff.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for TargetDesignatorOff")
            }
        }

        return builder.build()
    }

    /**
     * Build TargetDesignatorOnModeB from Transit data
     */
    private fun buildLrfTargetDesignatorOnModeB(data: Map<*, *>): JonSharedCmdLrf.TargetDesignatorOnModeB {
        val builder = JonSharedCmdLrf.TargetDesignatorOnModeB.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for TargetDesignatorOnModeB")
            }
        }

        return builder.build()
    }

    /**
     * Build DisableFogMode from Transit data
     */
    private fun buildLrfDisableFogMode(data: Map<*, *>): JonSharedCmdLrf.DisableFogMode {
        val builder = JonSharedCmdLrf.DisableFogMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for DisableFogMode")
            }
        }

        return builder.build()
    }

    /**
     * Build SetScanMode from Transit data
     */
    private fun buildLrfSetScanMode(data: Map<*, *>): JonSharedCmdLrf.SetScanMode {
        val builder = JonSharedCmdLrf.SetScanMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "mode" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setMode(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetScanMode")
            }
        }

        return builder.build()
    }

    /**
     * Build RefineOff from Transit data
     */
    private fun buildLrfRefineOff(data: Map<*, *>): JonSharedCmdLrf.RefineOff {
        val builder = JonSharedCmdLrf.RefineOff.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RefineOff")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanOff from Transit data
     */
    private fun buildLrfScanOff(data: Map<*, *>): JonSharedCmdLrf.ScanOff {
        val builder = JonSharedCmdLrf.ScanOff.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanOff")
            }
        }

        return builder.build()
    }

    /**
     * Build RefineOn from Transit data
     */
    private fun buildLrfRefineOn(data: Map<*, *>): JonSharedCmdLrf.RefineOn {
        val builder = JonSharedCmdLrf.RefineOn.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RefineOn")
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildLrfStart(data: Map<*, *>): JonSharedCmdLrf.Start {
        val builder = JonSharedCmdLrf.Start.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Start")
            }
        }

        return builder.build()
    }

    /**
     * Build Measure from Transit data
     */
    private fun buildLrfMeasure(data: Map<*, *>): JonSharedCmdLrf.Measure {
        val builder = JonSharedCmdLrf.Measure.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Measure")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanOn from Transit data
     */
    private fun buildLrfScanOn(data: Map<*, *>): JonSharedCmdLrf.ScanOn {
        val builder = JonSharedCmdLrf.ScanOn.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanOn")
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildLrfStop(data: Map<*, *>): JonSharedCmdLrf.Stop {
        val builder = JonSharedCmdLrf.Stop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Stop")
            }
        }

        return builder.build()
    }

    /**
     * Build NewSession from Transit data
     */
    private fun buildLrfNewSession(data: Map<*, *>): JonSharedCmdLrf.NewSession {
        val builder = JonSharedCmdLrf.NewSession.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for NewSession")
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildLrfGetMeteo(data: Map<*, *>): JonSharedCmdLrf.GetMeteo {
        val builder = JonSharedCmdLrf.GetMeteo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for GetMeteo")
            }
        }

        return builder.build()
    }

    /**
     * Build EnableFogMode from Transit data
     */
    private fun buildLrfEnableFogMode(data: Map<*, *>): JonSharedCmdLrf.EnableFogMode {
        val builder = JonSharedCmdLrf.EnableFogMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for EnableFogMode")
            }
        }

        return builder.build()
    }

    /**
     * Build TargetDesignatorOnModeA from Transit data
     */
    private fun buildLrfTargetDesignatorOnModeA(data: Map<*, *>): JonSharedCmdLrf.TargetDesignatorOnModeA {
        val builder = JonSharedCmdLrf.TargetDesignatorOnModeA.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for TargetDesignatorOnModeA")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildDayCamGlassHeater(data: Map<*, *>): JonSharedCmdDayCamGlassHeater.Root {
        val builder = JonSharedCmdDayCamGlassHeater.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "start" -> builder.setStart(buildDayCamGlassHeaterStart(value as Map<*, *>))
                "stop" -> builder.setStop(buildDayCamGlassHeaterStop(value as Map<*, *>))
                "turn-on" -> builder.setTurnOn(buildDayCamGlassHeaterTurnOn(value as Map<*, *>))
                "turn-off" -> builder.setTurnOff(buildDayCamGlassHeaterTurnOff(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildDayCamGlassHeaterGetMeteo(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildDayCamGlassHeaterStart(data: Map<*, *>): JonSharedCmdDayCamGlassHeater.Start {
        val builder = JonSharedCmdDayCamGlassHeater.Start.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Start")
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildDayCamGlassHeaterStop(data: Map<*, *>): JonSharedCmdDayCamGlassHeater.Stop {
        val builder = JonSharedCmdDayCamGlassHeater.Stop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Stop")
            }
        }

        return builder.build()
    }

    /**
     * Build TurnOn from Transit data
     */
    private fun buildDayCamGlassHeaterTurnOn(data: Map<*, *>): JonSharedCmdDayCamGlassHeater.TurnOn {
        val builder = JonSharedCmdDayCamGlassHeater.TurnOn.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for TurnOn")
            }
        }

        return builder.build()
    }

    /**
     * Build TurnOff from Transit data
     */
    private fun buildDayCamGlassHeaterTurnOff(data: Map<*, *>): JonSharedCmdDayCamGlassHeater.TurnOff {
        val builder = JonSharedCmdDayCamGlassHeater.TurnOff.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for TurnOff")
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildDayCamGlassHeaterGetMeteo(data: Map<*, *>): JonSharedCmdDayCamGlassHeater.GetMeteo {
        val builder = JonSharedCmdDayCamGlassHeater.GetMeteo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for GetMeteo")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildDayCamera(data: Map<*, *>): JonSharedCmdDayCamera.Root {
        val builder = JonSharedCmdDayCamera.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "zoom" -> builder.setZoom(buildDayCameraZoom(value as Map<*, *>))
                "set-infra-red-filter" -> builder.setSetInfraRedFilter(buildDayCameraSetInfraRedFilter(value as Map<*, *>))
                "set-clahe-level" -> builder.setSetClaheLevel(buildDayCameraSetClaheLevel(value as Map<*, *>))
                "prev-fx-mode" -> builder.setPrevFxMode(buildDayCameraPrevFxMode(value as Map<*, *>))
                "start" -> builder.setStart(buildDayCameraStart(value as Map<*, *>))
                "halt-all" -> builder.setHaltAll(buildDayCameraHaltAll(value as Map<*, *>))
                "set-digital-zoom-level" -> builder.setSetDigitalZoomLevel(buildDayCameraSetDigitalZoomLevel(value as Map<*, *>))
                "stop" -> builder.setStop(buildDayCameraStop(value as Map<*, *>))
                "photo" -> builder.setPhoto(buildDayCameraPhoto(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildDayCameraGetMeteo(value as Map<*, *>))
                "focus" -> builder.setFocus(buildDayCameraFocus(value as Map<*, *>))
                "set-fx-mode" -> builder.setSetFxMode(buildDayCameraSetFxMode(value as Map<*, *>))
                "set-iris" -> builder.setSetIris(buildDayCameraSetIris(value as Map<*, *>))
                "refresh-fx-mode" -> builder.setRefreshFxMode(buildDayCameraRefreshFxMode(value as Map<*, *>))
                "set-auto-iris" -> builder.setSetAutoIris(buildDayCameraSetAutoIris(value as Map<*, *>))
                "next-fx-mode" -> builder.setNextFxMode(buildDayCameraNextFxMode(value as Map<*, *>))
                "shift-clahe-level" -> builder.setShiftClaheLevel(buildDayCameraShiftClaheLevel(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build Zoom from Transit data
     */
    private fun buildDayCameraZoom(data: Map<*, *>): JonSharedCmdDayCamera.Zoom {
        val builder = JonSharedCmdDayCamera.Zoom.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "prev-zoom-table-pos" -> builder.setPrevZoomTablePos(buildDayCameraZoomPrevZoomTablePos(value as Map<*, *>))
                "offset" -> builder.setOffset(buildDayCameraZoomOffset(value as Map<*, *>))
                "move" -> builder.setMove(buildDayCameraZoomMove(value as Map<*, *>))
                "reset-zoom" -> builder.setResetZoom(buildDayCameraZoomResetZoom(value as Map<*, *>))
                "next-zoom-table-pos" -> builder.setNextZoomTablePos(buildDayCameraZoomNextZoomTablePos(value as Map<*, *>))
                "set-value" -> builder.setSetValue(buildDayCameraZoomSetValue(value as Map<*, *>))
                "set-zoom-table-value" -> builder.setSetZoomTableValue(buildDayCameraZoomSetZoomTableValue(value as Map<*, *>))
                "halt" -> builder.setHalt(buildDayCameraZoomHalt(value as Map<*, *>))
                "save-to-table" -> builder.setSaveToTable(buildDayCameraZoomSaveToTable(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Zoom")
            }
        }

        return builder.build()
    }

    /**
     * Build PrevZoomTablePos from Transit data
     */
    private fun buildDayCameraZoomPrevZoomTablePos(data: Map<*, *>): JonSharedCmdDayCamera.PrevZoomTablePos {
        val builder = JonSharedCmdDayCamera.PrevZoomTablePos.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for PrevZoomTablePos")
            }
        }

        return builder.build()
    }

    /**
     * Build Offset from Transit data
     */
    private fun buildDayCameraZoomOffset(data: Map<*, *>): JonSharedCmdDayCamera.Offset {
        val builder = JonSharedCmdDayCamera.Offset.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "offset-value" -> builder.setOffsetValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Offset")
            }
        }

        return builder.build()
    }

    /**
     * Build Move from Transit data
     */
    private fun buildDayCameraZoomMove(data: Map<*, *>): JonSharedCmdDayCamera.Move {
        val builder = JonSharedCmdDayCamera.Move.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "target-value" -> builder.setTargetValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Move")
            }
        }

        return builder.build()
    }

    /**
     * Build ResetZoom from Transit data
     */
    private fun buildDayCameraZoomResetZoom(data: Map<*, *>): JonSharedCmdDayCamera.ResetZoom {
        val builder = JonSharedCmdDayCamera.ResetZoom.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ResetZoom")
            }
        }

        return builder.build()
    }

    /**
     * Build NextZoomTablePos from Transit data
     */
    private fun buildDayCameraZoomNextZoomTablePos(data: Map<*, *>): JonSharedCmdDayCamera.NextZoomTablePos {
        val builder = JonSharedCmdDayCamera.NextZoomTablePos.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for NextZoomTablePos")
            }
        }

        return builder.build()
    }

    /**
     * Build SetValue from Transit data
     */
    private fun buildDayCameraZoomSetValue(data: Map<*, *>): JonSharedCmdDayCamera.SetValue {
        val builder = JonSharedCmdDayCamera.SetValue.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetValue")
            }
        }

        return builder.build()
    }

    /**
     * Build SetZoomTableValue from Transit data
     */
    private fun buildDayCameraZoomSetZoomTableValue(data: Map<*, *>): JonSharedCmdDayCamera.SetZoomTableValue {
        val builder = JonSharedCmdDayCamera.SetZoomTableValue.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetZoomTableValue")
            }
        }

        return builder.build()
    }

    /**
     * Build Halt from Transit data
     */
    private fun buildDayCameraZoomHalt(data: Map<*, *>): JonSharedCmdDayCamera.Halt {
        val builder = JonSharedCmdDayCamera.Halt.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Halt")
            }
        }

        return builder.build()
    }

    /**
     * Build SaveToTable from Transit data
     */
    private fun buildDayCameraZoomSaveToTable(data: Map<*, *>): JonSharedCmdDayCamera.SaveToTable {
        val builder = JonSharedCmdDayCamera.SaveToTable.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SaveToTable")
            }
        }

        return builder.build()
    }

    /**
     * Build SetInfraRedFilter from Transit data
     */
    private fun buildDayCameraSetInfraRedFilter(data: Map<*, *>): JonSharedCmdDayCamera.SetInfraRedFilter {
        val builder = JonSharedCmdDayCamera.SetInfraRedFilter.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertBoolean(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetInfraRedFilter")
            }
        }

        return builder.build()
    }

    /**
     * Build SetClaheLevel from Transit data
     */
    private fun buildDayCameraSetClaheLevel(data: Map<*, *>): JonSharedCmdDayCamera.SetClaheLevel {
        val builder = JonSharedCmdDayCamera.SetClaheLevel.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetClaheLevel")
            }
        }

        return builder.build()
    }

    /**
     * Build PrevFxMode from Transit data
     */
    private fun buildDayCameraPrevFxMode(data: Map<*, *>): JonSharedCmdDayCamera.PrevFxMode {
        val builder = JonSharedCmdDayCamera.PrevFxMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for PrevFxMode")
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildDayCameraStart(data: Map<*, *>): JonSharedCmdDayCamera.Start {
        val builder = JonSharedCmdDayCamera.Start.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Start")
            }
        }

        return builder.build()
    }

    /**
     * Build HaltAll from Transit data
     */
    private fun buildDayCameraHaltAll(data: Map<*, *>): JonSharedCmdDayCamera.HaltAll {
        val builder = JonSharedCmdDayCamera.HaltAll.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for HaltAll")
            }
        }

        return builder.build()
    }

    /**
     * Build SetDigitalZoomLevel from Transit data
     */
    private fun buildDayCameraSetDigitalZoomLevel(data: Map<*, *>): JonSharedCmdDayCamera.SetDigitalZoomLevel {
        val builder = JonSharedCmdDayCamera.SetDigitalZoomLevel.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetDigitalZoomLevel")
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildDayCameraStop(data: Map<*, *>): JonSharedCmdDayCamera.Stop {
        val builder = JonSharedCmdDayCamera.Stop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Stop")
            }
        }

        return builder.build()
    }

    /**
     * Build Photo from Transit data
     */
    private fun buildDayCameraPhoto(data: Map<*, *>): JonSharedCmdDayCamera.Photo {
        val builder = JonSharedCmdDayCamera.Photo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Photo")
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildDayCameraGetMeteo(data: Map<*, *>): JonSharedCmdDayCamera.GetMeteo {
        val builder = JonSharedCmdDayCamera.GetMeteo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for GetMeteo")
            }
        }

        return builder.build()
    }

    /**
     * Build Focus from Transit data
     */
    private fun buildDayCameraFocus(data: Map<*, *>): JonSharedCmdDayCamera.Focus {
        val builder = JonSharedCmdDayCamera.Focus.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "set-value" -> builder.setSetValue(buildDayCameraFocusSetValue(value as Map<*, *>))
                "move" -> builder.setMove(buildDayCameraFocusMove(value as Map<*, *>))
                "halt" -> builder.setHalt(buildDayCameraFocusHalt(value as Map<*, *>))
                "offset" -> builder.setOffset(buildDayCameraFocusOffset(value as Map<*, *>))
                "reset-focus" -> builder.setResetFocus(buildDayCameraFocusResetFocus(value as Map<*, *>))
                "save-to-table-focus" -> builder.setSaveToTableFocus(buildDayCameraFocusSaveToTableFocus(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Focus")
            }
        }

        return builder.build()
    }

    /**
     * Build SetValue from Transit data
     */
    private fun buildDayCameraFocusSetValue(data: Map<*, *>): JonSharedCmdDayCamera.SetValue {
        val builder = JonSharedCmdDayCamera.SetValue.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetValue")
            }
        }

        return builder.build()
    }

    /**
     * Build Move from Transit data
     */
    private fun buildDayCameraFocusMove(data: Map<*, *>): JonSharedCmdDayCamera.Move {
        val builder = JonSharedCmdDayCamera.Move.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "target-value" -> builder.setTargetValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Move")
            }
        }

        return builder.build()
    }

    /**
     * Build Halt from Transit data
     */
    private fun buildDayCameraFocusHalt(data: Map<*, *>): JonSharedCmdDayCamera.Halt {
        val builder = JonSharedCmdDayCamera.Halt.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Halt")
            }
        }

        return builder.build()
    }

    /**
     * Build Offset from Transit data
     */
    private fun buildDayCameraFocusOffset(data: Map<*, *>): JonSharedCmdDayCamera.Offset {
        val builder = JonSharedCmdDayCamera.Offset.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "offset-value" -> builder.setOffsetValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Offset")
            }
        }

        return builder.build()
    }

    /**
     * Build ResetFocus from Transit data
     */
    private fun buildDayCameraFocusResetFocus(data: Map<*, *>): JonSharedCmdDayCamera.ResetFocus {
        val builder = JonSharedCmdDayCamera.ResetFocus.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ResetFocus")
            }
        }

        return builder.build()
    }

    /**
     * Build SaveToTableFocus from Transit data
     */
    private fun buildDayCameraFocusSaveToTableFocus(data: Map<*, *>): JonSharedCmdDayCamera.SaveToTableFocus {
        val builder = JonSharedCmdDayCamera.SaveToTableFocus.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SaveToTableFocus")
            }
        }

        return builder.build()
    }

    /**
     * Build SetFxMode from Transit data
     */
    private fun buildDayCameraSetFxMode(data: Map<*, *>): JonSharedCmdDayCamera.SetFxMode {
        val builder = JonSharedCmdDayCamera.SetFxMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "mode" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setMode(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetFxMode")
            }
        }

        return builder.build()
    }

    /**
     * Build SetIris from Transit data
     */
    private fun buildDayCameraSetIris(data: Map<*, *>): JonSharedCmdDayCamera.SetIris {
        val builder = JonSharedCmdDayCamera.SetIris.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetIris")
            }
        }

        return builder.build()
    }

    /**
     * Build RefreshFxMode from Transit data
     */
    private fun buildDayCameraRefreshFxMode(data: Map<*, *>): JonSharedCmdDayCamera.RefreshFxMode {
        val builder = JonSharedCmdDayCamera.RefreshFxMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RefreshFxMode")
            }
        }

        return builder.build()
    }

    /**
     * Build SetAutoIris from Transit data
     */
    private fun buildDayCameraSetAutoIris(data: Map<*, *>): JonSharedCmdDayCamera.SetAutoIris {
        val builder = JonSharedCmdDayCamera.SetAutoIris.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertBoolean(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetAutoIris")
            }
        }

        return builder.build()
    }

    /**
     * Build NextFxMode from Transit data
     */
    private fun buildDayCameraNextFxMode(data: Map<*, *>): JonSharedCmdDayCamera.NextFxMode {
        val builder = JonSharedCmdDayCamera.NextFxMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for NextFxMode")
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftClaheLevel from Transit data
     */
    private fun buildDayCameraShiftClaheLevel(data: Map<*, *>): JonSharedCmdDayCamera.ShiftClaheLevel {
        val builder = JonSharedCmdDayCamera.ShiftClaheLevel.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShiftClaheLevel")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildHeatCamera(data: Map<*, *>): JonSharedCmdHeatCamera.Root {
        val builder = JonSharedCmdHeatCamera.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "set-dde-level" -> builder.setSetDdeLevel(buildHeatCameraSetDdeLevel(value as Map<*, *>))
                "set-calib-mode" -> builder.setSetCalibMode(buildHeatCameraSetCalibMode(value as Map<*, *>))
                "zoom" -> builder.setZoom(buildHeatCameraZoom(value as Map<*, *>))
                "set-agc" -> builder.setSetAgc(buildHeatCameraSetAgc(value as Map<*, *>))
                "shift-dde" -> builder.setShiftDde(buildHeatCameraShiftDde(value as Map<*, *>))
                "set-filter" -> builder.setSetFilter(buildHeatCameraSetFilter(value as Map<*, *>))
                "set-clahe-level" -> builder.setSetClaheLevel(buildHeatCameraSetClaheLevel(value as Map<*, *>))
                "disable-dde" -> builder.setDisableDde(buildHeatCameraDisableDde(value as Map<*, *>))
                "prev-fx-mode" -> builder.setPrevFxMode(buildHeatCameraPrevFxMode(value as Map<*, *>))
                "start" -> builder.setStart(buildHeatCameraStart(value as Map<*, *>))
                "focus-step-minus" -> builder.setFocusStepMinus(buildHeatCameraFocusStepMinus(value as Map<*, *>))
                "set-digital-zoom-level" -> builder.setSetDigitalZoomLevel(buildHeatCameraSetDigitalZoomLevel(value as Map<*, *>))
                "enable-dde" -> builder.setEnableDde(buildHeatCameraEnableDde(value as Map<*, *>))
                "focus-stop" -> builder.setFocusStop(buildHeatCameraFocusStop(value as Map<*, *>))
                "stop" -> builder.setStop(buildHeatCameraStop(value as Map<*, *>))
                "reset-zoom" -> builder.setResetZoom(buildHeatCameraResetZoom(value as Map<*, *>))
                "zoom-out" -> builder.setZoomOut(buildHeatCameraZoomOut(value as Map<*, *>))
                "photo" -> builder.setPhoto(buildHeatCameraPhoto(value as Map<*, *>))
                "zoom-in" -> builder.setZoomIn(buildHeatCameraZoomIn(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildHeatCameraGetMeteo(value as Map<*, *>))
                "focus-step-plus" -> builder.setFocusStepPlus(buildHeatCameraFocusStepPlus(value as Map<*, *>))
                "set-fx-mode" -> builder.setSetFxMode(buildHeatCameraSetFxMode(value as Map<*, *>))
                "refresh-fx-mode" -> builder.setRefreshFxMode(buildHeatCameraRefreshFxMode(value as Map<*, *>))
                "focus-out" -> builder.setFocusOut(buildHeatCameraFocusOut(value as Map<*, *>))
                "set-auto-focus" -> builder.setSetAutoFocus(buildHeatCameraSetAutoFocus(value as Map<*, *>))
                "zoom-stop" -> builder.setZoomStop(buildHeatCameraZoomStop(value as Map<*, *>))
                "save-to-table" -> builder.setSaveToTable(buildHeatCameraSaveToTable(value as Map<*, *>))
                "next-fx-mode" -> builder.setNextFxMode(buildHeatCameraNextFxMode(value as Map<*, *>))
                "calibrate" -> builder.setCalibrate(buildHeatCameraCalibrate(value as Map<*, *>))
                "shift-clahe-level" -> builder.setShiftClaheLevel(buildHeatCameraShiftClaheLevel(value as Map<*, *>))
                "focus-in" -> builder.setFocusIn(buildHeatCameraFocusIn(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build SetDDELevel from Transit data
     */
    private fun buildHeatCameraSetDdeLevel(data: Map<*, *>): JonSharedCmdHeatCamera.SetDDELevel {
        val builder = JonSharedCmdHeatCamera.SetDDELevel.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetDDELevel")
            }
        }

        return builder.build()
    }

    /**
     * Build SetCalibMode from Transit data
     */
    private fun buildHeatCameraSetCalibMode(data: Map<*, *>): JonSharedCmdHeatCamera.SetCalibMode {
        val builder = JonSharedCmdHeatCamera.SetCalibMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetCalibMode")
            }
        }

        return builder.build()
    }

    /**
     * Build Zoom from Transit data
     */
    private fun buildHeatCameraZoom(data: Map<*, *>): JonSharedCmdHeatCamera.Zoom {
        val builder = JonSharedCmdHeatCamera.Zoom.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "set-zoom-table-value" -> builder.setSetZoomTableValue(buildHeatCameraZoomSetZoomTableValue(value as Map<*, *>))
                "next-zoom-table-pos" -> builder.setNextZoomTablePos(buildHeatCameraZoomNextZoomTablePos(value as Map<*, *>))
                "prev-zoom-table-pos" -> builder.setPrevZoomTablePos(buildHeatCameraZoomPrevZoomTablePos(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Zoom")
            }
        }

        return builder.build()
    }

    /**
     * Build SetZoomTableValue from Transit data
     */
    private fun buildHeatCameraZoomSetZoomTableValue(data: Map<*, *>): JonSharedCmdHeatCamera.SetZoomTableValue {
        val builder = JonSharedCmdHeatCamera.SetZoomTableValue.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetZoomTableValue")
            }
        }

        return builder.build()
    }

    /**
     * Build NextZoomTablePos from Transit data
     */
    private fun buildHeatCameraZoomNextZoomTablePos(data: Map<*, *>): JonSharedCmdHeatCamera.NextZoomTablePos {
        val builder = JonSharedCmdHeatCamera.NextZoomTablePos.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for NextZoomTablePos")
            }
        }

        return builder.build()
    }

    /**
     * Build PrevZoomTablePos from Transit data
     */
    private fun buildHeatCameraZoomPrevZoomTablePos(data: Map<*, *>): JonSharedCmdHeatCamera.PrevZoomTablePos {
        val builder = JonSharedCmdHeatCamera.PrevZoomTablePos.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for PrevZoomTablePos")
            }
        }

        return builder.build()
    }

    /**
     * Build SetAGC from Transit data
     */
    private fun buildHeatCameraSetAgc(data: Map<*, *>): JonSharedCmdHeatCamera.SetAGC {
        val builder = JonSharedCmdHeatCamera.SetAGC.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setValue(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetAGC")
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftDDE from Transit data
     */
    private fun buildHeatCameraShiftDde(data: Map<*, *>): JonSharedCmdHeatCamera.ShiftDDE {
        val builder = JonSharedCmdHeatCamera.ShiftDDE.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShiftDDE")
            }
        }

        return builder.build()
    }

    /**
     * Build SetFilters from Transit data
     */
    private fun buildHeatCameraSetFilter(data: Map<*, *>): JonSharedCmdHeatCamera.SetFilters {
        val builder = JonSharedCmdHeatCamera.SetFilters.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setValue(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetFilters")
            }
        }

        return builder.build()
    }

    /**
     * Build SetClaheLevel from Transit data
     */
    private fun buildHeatCameraSetClaheLevel(data: Map<*, *>): JonSharedCmdHeatCamera.SetClaheLevel {
        val builder = JonSharedCmdHeatCamera.SetClaheLevel.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetClaheLevel")
            }
        }

        return builder.build()
    }

    /**
     * Build DisableDDE from Transit data
     */
    private fun buildHeatCameraDisableDde(data: Map<*, *>): JonSharedCmdHeatCamera.DisableDDE {
        val builder = JonSharedCmdHeatCamera.DisableDDE.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for DisableDDE")
            }
        }

        return builder.build()
    }

    /**
     * Build PrevFxMode from Transit data
     */
    private fun buildHeatCameraPrevFxMode(data: Map<*, *>): JonSharedCmdHeatCamera.PrevFxMode {
        val builder = JonSharedCmdHeatCamera.PrevFxMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for PrevFxMode")
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildHeatCameraStart(data: Map<*, *>): JonSharedCmdHeatCamera.Start {
        val builder = JonSharedCmdHeatCamera.Start.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Start")
            }
        }

        return builder.build()
    }

    /**
     * Build FocusStepMinus from Transit data
     */
    private fun buildHeatCameraFocusStepMinus(data: Map<*, *>): JonSharedCmdHeatCamera.FocusStepMinus {
        val builder = JonSharedCmdHeatCamera.FocusStepMinus.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for FocusStepMinus")
            }
        }

        return builder.build()
    }

    /**
     * Build SetDigitalZoomLevel from Transit data
     */
    private fun buildHeatCameraSetDigitalZoomLevel(data: Map<*, *>): JonSharedCmdHeatCamera.SetDigitalZoomLevel {
        val builder = JonSharedCmdHeatCamera.SetDigitalZoomLevel.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetDigitalZoomLevel")
            }
        }

        return builder.build()
    }

    /**
     * Build EnableDDE from Transit data
     */
    private fun buildHeatCameraEnableDde(data: Map<*, *>): JonSharedCmdHeatCamera.EnableDDE {
        val builder = JonSharedCmdHeatCamera.EnableDDE.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for EnableDDE")
            }
        }

        return builder.build()
    }

    /**
     * Build FocusStop from Transit data
     */
    private fun buildHeatCameraFocusStop(data: Map<*, *>): JonSharedCmdHeatCamera.FocusStop {
        val builder = JonSharedCmdHeatCamera.FocusStop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for FocusStop")
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildHeatCameraStop(data: Map<*, *>): JonSharedCmdHeatCamera.Stop {
        val builder = JonSharedCmdHeatCamera.Stop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Stop")
            }
        }

        return builder.build()
    }

    /**
     * Build ResetZoom from Transit data
     */
    private fun buildHeatCameraResetZoom(data: Map<*, *>): JonSharedCmdHeatCamera.ResetZoom {
        val builder = JonSharedCmdHeatCamera.ResetZoom.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ResetZoom")
            }
        }

        return builder.build()
    }

    /**
     * Build ZoomOut from Transit data
     */
    private fun buildHeatCameraZoomOut(data: Map<*, *>): JonSharedCmdHeatCamera.ZoomOut {
        val builder = JonSharedCmdHeatCamera.ZoomOut.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ZoomOut")
            }
        }

        return builder.build()
    }

    /**
     * Build Photo from Transit data
     */
    private fun buildHeatCameraPhoto(data: Map<*, *>): JonSharedCmdHeatCamera.Photo {
        val builder = JonSharedCmdHeatCamera.Photo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Photo")
            }
        }

        return builder.build()
    }

    /**
     * Build ZoomIn from Transit data
     */
    private fun buildHeatCameraZoomIn(data: Map<*, *>): JonSharedCmdHeatCamera.ZoomIn {
        val builder = JonSharedCmdHeatCamera.ZoomIn.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ZoomIn")
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildHeatCameraGetMeteo(data: Map<*, *>): JonSharedCmdHeatCamera.GetMeteo {
        val builder = JonSharedCmdHeatCamera.GetMeteo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for GetMeteo")
            }
        }

        return builder.build()
    }

    /**
     * Build FocusStepPlus from Transit data
     */
    private fun buildHeatCameraFocusStepPlus(data: Map<*, *>): JonSharedCmdHeatCamera.FocusStepPlus {
        val builder = JonSharedCmdHeatCamera.FocusStepPlus.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for FocusStepPlus")
            }
        }

        return builder.build()
    }

    /**
     * Build SetFxMode from Transit data
     */
    private fun buildHeatCameraSetFxMode(data: Map<*, *>): JonSharedCmdHeatCamera.SetFxMode {
        val builder = JonSharedCmdHeatCamera.SetFxMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "mode" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setMode(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetFxMode")
            }
        }

        return builder.build()
    }

    /**
     * Build RefreshFxMode from Transit data
     */
    private fun buildHeatCameraRefreshFxMode(data: Map<*, *>): JonSharedCmdHeatCamera.RefreshFxMode {
        val builder = JonSharedCmdHeatCamera.RefreshFxMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RefreshFxMode")
            }
        }

        return builder.build()
    }

    /**
     * Build FocusOut from Transit data
     */
    private fun buildHeatCameraFocusOut(data: Map<*, *>): JonSharedCmdHeatCamera.FocusOut {
        val builder = JonSharedCmdHeatCamera.FocusOut.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for FocusOut")
            }
        }

        return builder.build()
    }

    /**
     * Build SetAutoFocus from Transit data
     */
    private fun buildHeatCameraSetAutoFocus(data: Map<*, *>): JonSharedCmdHeatCamera.SetAutoFocus {
        val builder = JonSharedCmdHeatCamera.SetAutoFocus.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertBoolean(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetAutoFocus")
            }
        }

        return builder.build()
    }

    /**
     * Build ZoomStop from Transit data
     */
    private fun buildHeatCameraZoomStop(data: Map<*, *>): JonSharedCmdHeatCamera.ZoomStop {
        val builder = JonSharedCmdHeatCamera.ZoomStop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ZoomStop")
            }
        }

        return builder.build()
    }

    /**
     * Build SaveToTable from Transit data
     */
    private fun buildHeatCameraSaveToTable(data: Map<*, *>): JonSharedCmdHeatCamera.SaveToTable {
        val builder = JonSharedCmdHeatCamera.SaveToTable.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SaveToTable")
            }
        }

        return builder.build()
    }

    /**
     * Build NextFxMode from Transit data
     */
    private fun buildHeatCameraNextFxMode(data: Map<*, *>): JonSharedCmdHeatCamera.NextFxMode {
        val builder = JonSharedCmdHeatCamera.NextFxMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for NextFxMode")
            }
        }

        return builder.build()
    }

    /**
     * Build Calibrate from Transit data
     */
    private fun buildHeatCameraCalibrate(data: Map<*, *>): JonSharedCmdHeatCamera.Calibrate {
        val builder = JonSharedCmdHeatCamera.Calibrate.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Calibrate")
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftClaheLevel from Transit data
     */
    private fun buildHeatCameraShiftClaheLevel(data: Map<*, *>): JonSharedCmdHeatCamera.ShiftClaheLevel {
        val builder = JonSharedCmdHeatCamera.ShiftClaheLevel.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShiftClaheLevel")
            }
        }

        return builder.build()
    }

    /**
     * Build FocusIn from Transit data
     */
    private fun buildHeatCameraFocusIn(data: Map<*, *>): JonSharedCmdHeatCamera.FocusIn {
        val builder = JonSharedCmdHeatCamera.FocusIn.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for FocusIn")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildLira(data: Map<*, *>): JonSharedCmdLira.Root {
        val builder = JonSharedCmdLira.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "refine-target" -> builder.setRefineTarget(buildLiraRefineTarget(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build Refine_target from Transit data
     */
    private fun buildLiraRefineTarget(data: Map<*, *>): JonSharedCmdLira.Refine_target {
        val builder = JonSharedCmdLira.Refine_target.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "target" -> builder.setTarget(buildLiraRefineTargetTarget(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Refine_target")
            }
        }

        return builder.build()
    }

    /**
     * Build JonGuiDataLiraTarget from Transit data
     */
    private fun buildLiraRefineTargetTarget(data: Map<*, *>): JonSharedCmdLira.JonGuiDataLiraTarget {
        val builder = JonSharedCmdLira.JonGuiDataLiraTarget.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "uuid-part4" -> builder.setUuidPart4(convertInt(value))
                "uuid-part2" -> builder.setUuidPart2(convertInt(value))
                "target-altitude" -> builder.setTargetAltitude(convertDouble(value))
                "uuid-part3" -> builder.setUuidPart3(convertInt(value))
                "target-azimuth" -> builder.setTargetAzimuth(convertDouble(value))
                "distance" -> builder.setDistance(convertDouble(value))
                "target-longitude" -> builder.setTargetLongitude(convertDouble(value))
                "timestamp" -> builder.setTimestamp(convertLong(value))
                "uuid-part1" -> builder.setUuidPart1(convertInt(value))
                "target-latitude" -> builder.setTargetLatitude(convertDouble(value))
                "target-elevation" -> builder.setTargetElevation(convertDouble(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for JonGuiDataLiraTarget")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildLrfCalib(data: Map<*, *>): JonSharedCmdLrfAlign.Root {
        val builder = JonSharedCmdLrfAlign.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "day" -> builder.setDay(buildLrfCalibDay(value as Map<*, *>))
                "heat" -> builder.setHeat(buildLrfCalibHeat(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build Offsets from Transit data
     */
    private fun buildLrfCalibDay(data: Map<*, *>): JonSharedCmdLrfAlign.Offsets {
        val builder = JonSharedCmdLrfAlign.Offsets.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "set" -> builder.setSet(buildLrfCalibDaySet(value as Map<*, *>))
                "save" -> builder.setSave(buildLrfCalibDaySave(value as Map<*, *>))
                "reset" -> builder.setReset(buildLrfCalibDayReset(value as Map<*, *>))
                "shift" -> builder.setShift(buildLrfCalibDayShift(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Offsets")
            }
        }

        return builder.build()
    }

    /**
     * Build SetOffsets from Transit data
     */
    private fun buildLrfCalibDaySet(data: Map<*, *>): JonSharedCmdLrfAlign.SetOffsets {
        val builder = JonSharedCmdLrfAlign.SetOffsets.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "x" -> builder.setX(convertInt(value))
                "y" -> builder.setY(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetOffsets")
            }
        }

        return builder.build()
    }

    /**
     * Build SaveOffsets from Transit data
     */
    private fun buildLrfCalibDaySave(data: Map<*, *>): JonSharedCmdLrfAlign.SaveOffsets {
        val builder = JonSharedCmdLrfAlign.SaveOffsets.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SaveOffsets")
            }
        }

        return builder.build()
    }

    /**
     * Build ResetOffsets from Transit data
     */
    private fun buildLrfCalibDayReset(data: Map<*, *>): JonSharedCmdLrfAlign.ResetOffsets {
        val builder = JonSharedCmdLrfAlign.ResetOffsets.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ResetOffsets")
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftOffsetsBy from Transit data
     */
    private fun buildLrfCalibDayShift(data: Map<*, *>): JonSharedCmdLrfAlign.ShiftOffsetsBy {
        val builder = JonSharedCmdLrfAlign.ShiftOffsetsBy.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "x" -> builder.setX(convertInt(value))
                "y" -> builder.setY(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShiftOffsetsBy")
            }
        }

        return builder.build()
    }

    /**
     * Build Offsets from Transit data
     */
    private fun buildLrfCalibHeat(data: Map<*, *>): JonSharedCmdLrfAlign.Offsets {
        val builder = JonSharedCmdLrfAlign.Offsets.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "set" -> builder.setSet(buildLrfCalibHeatSet(value as Map<*, *>))
                "save" -> builder.setSave(buildLrfCalibHeatSave(value as Map<*, *>))
                "reset" -> builder.setReset(buildLrfCalibHeatReset(value as Map<*, *>))
                "shift" -> builder.setShift(buildLrfCalibHeatShift(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Offsets")
            }
        }

        return builder.build()
    }

    /**
     * Build SetOffsets from Transit data
     */
    private fun buildLrfCalibHeatSet(data: Map<*, *>): JonSharedCmdLrfAlign.SetOffsets {
        val builder = JonSharedCmdLrfAlign.SetOffsets.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "x" -> builder.setX(convertInt(value))
                "y" -> builder.setY(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetOffsets")
            }
        }

        return builder.build()
    }

    /**
     * Build SaveOffsets from Transit data
     */
    private fun buildLrfCalibHeatSave(data: Map<*, *>): JonSharedCmdLrfAlign.SaveOffsets {
        val builder = JonSharedCmdLrfAlign.SaveOffsets.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SaveOffsets")
            }
        }

        return builder.build()
    }

    /**
     * Build ResetOffsets from Transit data
     */
    private fun buildLrfCalibHeatReset(data: Map<*, *>): JonSharedCmdLrfAlign.ResetOffsets {
        val builder = JonSharedCmdLrfAlign.ResetOffsets.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ResetOffsets")
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftOffsetsBy from Transit data
     */
    private fun buildLrfCalibHeatShift(data: Map<*, *>): JonSharedCmdLrfAlign.ShiftOffsetsBy {
        val builder = JonSharedCmdLrfAlign.ShiftOffsetsBy.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "x" -> builder.setX(convertInt(value))
                "y" -> builder.setY(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ShiftOffsetsBy")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildRotary(data: Map<*, *>): JonSharedCmdRotary.Root {
        val builder = JonSharedCmdRotary.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "rotate-to-gps" -> builder.setRotateToGps(buildRotaryRotateToGps(value as Map<*, *>))
                "scan-pause" -> builder.setScanPause(buildRotaryScanPause(value as Map<*, *>))
                "rotate-to-ndc" -> builder.setRotateToNdc(buildRotaryRotateToNdc(value as Map<*, *>))
                "scan-start" -> builder.setScanStart(buildRotaryScanStart(value as Map<*, *>))
                "set-platform-azimuth" -> builder.setSetPlatformAzimuth(buildRotarySetPlatformAzimuth(value as Map<*, *>))
                "scan-stop" -> builder.setScanStop(buildRotaryScanStop(value as Map<*, *>))
                "start" -> builder.setStart(buildRotaryStart(value as Map<*, *>))
                "stop" -> builder.setStop(buildRotaryStop(value as Map<*, *>))
                "set-origin-gps" -> builder.setSetOriginGps(buildRotarySetOriginGps(value as Map<*, *>))
                "scan-next" -> builder.setScanNext(buildRotaryScanNext(value as Map<*, *>))
                "set-platform-bank" -> builder.setSetPlatformBank(buildRotarySetPlatformBank(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildRotaryGetMeteo(value as Map<*, *>))
                "set-use-rotary-as-compass" -> builder.setSetUseRotaryAsCompass(buildRotarySetUseRotaryAsCompass(value as Map<*, *>))
                "scan-prev" -> builder.setScanPrev(buildRotaryScanPrev(value as Map<*, *>))
                "scan-add-node" -> builder.setScanAddNode(buildRotaryScanAddNode(value as Map<*, *>))
                "set-platform-elevation" -> builder.setSetPlatformElevation(buildRotarySetPlatformElevation(value as Map<*, *>))
                "scan-select-node" -> builder.setScanSelectNode(buildRotaryScanSelectNode(value as Map<*, *>))
                "halt" -> builder.setHalt(buildRotaryHalt(value as Map<*, *>))
                "scan-delete-node" -> builder.setScanDeleteNode(buildRotaryScanDeleteNode(value as Map<*, *>))
                "axis" -> builder.setAxis(buildRotaryAxis(value as Map<*, *>))
                "scan-unpause" -> builder.setScanUnpause(buildRotaryScanUnpause(value as Map<*, *>))
                "set-mode" -> builder.setSetMode(buildRotarySetMode(value as Map<*, *>))
                "scan-refresh-node-list" -> builder.setScanRefreshNodeList(buildRotaryScanRefreshNodeList(value as Map<*, *>))
                "scan-update-node" -> builder.setScanUpdateNode(buildRotaryScanUpdateNode(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateToGPS from Transit data
     */
    private fun buildRotaryRotateToGps(data: Map<*, *>): JonSharedCmdRotary.RotateToGPS {
        val builder = JonSharedCmdRotary.RotateToGPS.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "latitude" -> builder.setLatitude(convertFloat(value))
                "longitude" -> builder.setLongitude(convertFloat(value))
                "altitude" -> builder.setAltitude(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateToGPS")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanPause from Transit data
     */
    private fun buildRotaryScanPause(data: Map<*, *>): JonSharedCmdRotary.ScanPause {
        val builder = JonSharedCmdRotary.ScanPause.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanPause")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateToNDC from Transit data
     */
    private fun buildRotaryRotateToNdc(data: Map<*, *>): JonSharedCmdRotary.RotateToNDC {
        val builder = JonSharedCmdRotary.RotateToNDC.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "channel" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setChannel(SomeEnumType.valueOf(enumValue))
                }
                "x" -> builder.setX(convertFloat(value))
                "y" -> builder.setY(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateToNDC")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanStart from Transit data
     */
    private fun buildRotaryScanStart(data: Map<*, *>): JonSharedCmdRotary.ScanStart {
        val builder = JonSharedCmdRotary.ScanStart.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanStart")
            }
        }

        return builder.build()
    }

    /**
     * Build SetPlatformAzimuth from Transit data
     */
    private fun buildRotarySetPlatformAzimuth(data: Map<*, *>): JonSharedCmdRotary.SetPlatformAzimuth {
        val builder = JonSharedCmdRotary.SetPlatformAzimuth.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetPlatformAzimuth")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanStop from Transit data
     */
    private fun buildRotaryScanStop(data: Map<*, *>): JonSharedCmdRotary.ScanStop {
        val builder = JonSharedCmdRotary.ScanStop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanStop")
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildRotaryStart(data: Map<*, *>): JonSharedCmdRotary.Start {
        val builder = JonSharedCmdRotary.Start.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Start")
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildRotaryStop(data: Map<*, *>): JonSharedCmdRotary.Stop {
        val builder = JonSharedCmdRotary.Stop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Stop")
            }
        }

        return builder.build()
    }

    /**
     * Build SetOriginGPS from Transit data
     */
    private fun buildRotarySetOriginGps(data: Map<*, *>): JonSharedCmdRotary.SetOriginGPS {
        val builder = JonSharedCmdRotary.SetOriginGPS.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "latitude" -> builder.setLatitude(convertFloat(value))
                "longitude" -> builder.setLongitude(convertFloat(value))
                "altitude" -> builder.setAltitude(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetOriginGPS")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanNext from Transit data
     */
    private fun buildRotaryScanNext(data: Map<*, *>): JonSharedCmdRotary.ScanNext {
        val builder = JonSharedCmdRotary.ScanNext.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanNext")
            }
        }

        return builder.build()
    }

    /**
     * Build SetPlatformBank from Transit data
     */
    private fun buildRotarySetPlatformBank(data: Map<*, *>): JonSharedCmdRotary.SetPlatformBank {
        val builder = JonSharedCmdRotary.SetPlatformBank.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetPlatformBank")
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildRotaryGetMeteo(data: Map<*, *>): JonSharedCmdRotary.GetMeteo {
        val builder = JonSharedCmdRotary.GetMeteo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for GetMeteo")
            }
        }

        return builder.build()
    }

    /**
     * Build setUseRotaryAsCompass from Transit data
     */
    private fun buildRotarySetUseRotaryAsCompass(data: Map<*, *>): JonSharedCmdRotary.setUseRotaryAsCompass {
        val builder = JonSharedCmdRotary.setUseRotaryAsCompass.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "flag" -> builder.setFlag(convertBoolean(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for setUseRotaryAsCompass")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanPrev from Transit data
     */
    private fun buildRotaryScanPrev(data: Map<*, *>): JonSharedCmdRotary.ScanPrev {
        val builder = JonSharedCmdRotary.ScanPrev.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanPrev")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanAddNode from Transit data
     */
    private fun buildRotaryScanAddNode(data: Map<*, *>): JonSharedCmdRotary.ScanAddNode {
        val builder = JonSharedCmdRotary.ScanAddNode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "index" -> builder.setIndex(convertInt(value))
                "DayZoomTableValue" -> builder.setDayZoomTableValue(convertInt(value))
                "HeatZoomTableValue" -> builder.setHeatZoomTableValue(convertInt(value))
                "azimuth" -> builder.setAzimuth(convertDouble(value))
                "elevation" -> builder.setElevation(convertDouble(value))
                "linger" -> builder.setLinger(convertDouble(value))
                "speed" -> builder.setSpeed(convertDouble(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanAddNode")
            }
        }

        return builder.build()
    }

    /**
     * Build SetPlatformElevation from Transit data
     */
    private fun buildRotarySetPlatformElevation(data: Map<*, *>): JonSharedCmdRotary.SetPlatformElevation {
        val builder = JonSharedCmdRotary.SetPlatformElevation.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetPlatformElevation")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanSelectNode from Transit data
     */
    private fun buildRotaryScanSelectNode(data: Map<*, *>): JonSharedCmdRotary.ScanSelectNode {
        val builder = JonSharedCmdRotary.ScanSelectNode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "index" -> builder.setIndex(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanSelectNode")
            }
        }

        return builder.build()
    }

    /**
     * Build Halt from Transit data
     */
    private fun buildRotaryHalt(data: Map<*, *>): JonSharedCmdRotary.Halt {
        val builder = JonSharedCmdRotary.Halt.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Halt")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanDeleteNode from Transit data
     */
    private fun buildRotaryScanDeleteNode(data: Map<*, *>): JonSharedCmdRotary.ScanDeleteNode {
        val builder = JonSharedCmdRotary.ScanDeleteNode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "index" -> builder.setIndex(convertInt(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanDeleteNode")
            }
        }

        return builder.build()
    }

    /**
     * Build Axis from Transit data
     */
    private fun buildRotaryAxis(data: Map<*, *>): JonSharedCmdRotary.Axis {
        val builder = JonSharedCmdRotary.Axis.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "azimuth" -> builder.setAzimuth(buildRotaryAxisAzimuth(value as Map<*, *>))
                "elevation" -> builder.setElevation(buildRotaryAxisElevation(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Axis")
            }
        }

        return builder.build()
    }

    /**
     * Build Azimuth from Transit data
     */
    private fun buildRotaryAxisAzimuth(data: Map<*, *>): JonSharedCmdRotary.Azimuth {
        val builder = JonSharedCmdRotary.Azimuth.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "set-value" -> builder.setSetValue(buildRotaryAxisAzimuthSetValue(value as Map<*, *>))
                "rotate-to" -> builder.setRotateTo(buildRotaryAxisAzimuthRotateTo(value as Map<*, *>))
                "rotate" -> builder.setRotate(buildRotaryAxisAzimuthRotate(value as Map<*, *>))
                "relative" -> builder.setRelative(buildRotaryAxisAzimuthRelative(value as Map<*, *>))
                "relative-set" -> builder.setRelativeSet(buildRotaryAxisAzimuthRelativeSet(value as Map<*, *>))
                "halt" -> builder.setHalt(buildRotaryAxisAzimuthHalt(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Azimuth")
            }
        }

        return builder.build()
    }

    /**
     * Build SetAzimuthValue from Transit data
     */
    private fun buildRotaryAxisAzimuthSetValue(data: Map<*, *>): JonSharedCmdRotary.SetAzimuthValue {
        val builder = JonSharedCmdRotary.SetAzimuthValue.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                "direction" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setDirection(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetAzimuthValue")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateAzimuthTo from Transit data
     */
    private fun buildRotaryAxisAzimuthRotateTo(data: Map<*, *>): JonSharedCmdRotary.RotateAzimuthTo {
        val builder = JonSharedCmdRotary.RotateAzimuthTo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "target-value" -> builder.setTargetValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setDirection(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateAzimuthTo")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateAzimuth from Transit data
     */
    private fun buildRotaryAxisAzimuthRotate(data: Map<*, *>): JonSharedCmdRotary.RotateAzimuth {
        val builder = JonSharedCmdRotary.RotateAzimuth.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setDirection(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateAzimuth")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateAzimuthRelative from Transit data
     */
    private fun buildRotaryAxisAzimuthRelative(data: Map<*, *>): JonSharedCmdRotary.RotateAzimuthRelative {
        val builder = JonSharedCmdRotary.RotateAzimuthRelative.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setDirection(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateAzimuthRelative")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateAzimuthRelativeSet from Transit data
     */
    private fun buildRotaryAxisAzimuthRelativeSet(data: Map<*, *>): JonSharedCmdRotary.RotateAzimuthRelativeSet {
        val builder = JonSharedCmdRotary.RotateAzimuthRelativeSet.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                "direction" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setDirection(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateAzimuthRelativeSet")
            }
        }

        return builder.build()
    }

    /**
     * Build HaltAzimuth from Transit data
     */
    private fun buildRotaryAxisAzimuthHalt(data: Map<*, *>): JonSharedCmdRotary.HaltAzimuth {
        val builder = JonSharedCmdRotary.HaltAzimuth.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for HaltAzimuth")
            }
        }

        return builder.build()
    }

    /**
     * Build Elevation from Transit data
     */
    private fun buildRotaryAxisElevation(data: Map<*, *>): JonSharedCmdRotary.Elevation {
        val builder = JonSharedCmdRotary.Elevation.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "set-value" -> builder.setSetValue(buildRotaryAxisElevationSetValue(value as Map<*, *>))
                "rotate-to" -> builder.setRotateTo(buildRotaryAxisElevationRotateTo(value as Map<*, *>))
                "rotate" -> builder.setRotate(buildRotaryAxisElevationRotate(value as Map<*, *>))
                "relative" -> builder.setRelative(buildRotaryAxisElevationRelative(value as Map<*, *>))
                "relative-set" -> builder.setRelativeSet(buildRotaryAxisElevationRelativeSet(value as Map<*, *>))
                "halt" -> builder.setHalt(buildRotaryAxisElevationHalt(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Elevation")
            }
        }

        return builder.build()
    }

    /**
     * Build SetElevationValue from Transit data
     */
    private fun buildRotaryAxisElevationSetValue(data: Map<*, *>): JonSharedCmdRotary.SetElevationValue {
        val builder = JonSharedCmdRotary.SetElevationValue.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetElevationValue")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateElevationTo from Transit data
     */
    private fun buildRotaryAxisElevationRotateTo(data: Map<*, *>): JonSharedCmdRotary.RotateElevationTo {
        val builder = JonSharedCmdRotary.RotateElevationTo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "target-value" -> builder.setTargetValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateElevationTo")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateElevation from Transit data
     */
    private fun buildRotaryAxisElevationRotate(data: Map<*, *>): JonSharedCmdRotary.RotateElevation {
        val builder = JonSharedCmdRotary.RotateElevation.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setDirection(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateElevation")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateElevationRelative from Transit data
     */
    private fun buildRotaryAxisElevationRelative(data: Map<*, *>): JonSharedCmdRotary.RotateElevationRelative {
        val builder = JonSharedCmdRotary.RotateElevationRelative.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setDirection(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateElevationRelative")
            }
        }

        return builder.build()
    }

    /**
     * Build RotateElevationRelativeSet from Transit data
     */
    private fun buildRotaryAxisElevationRelativeSet(data: Map<*, *>): JonSharedCmdRotary.RotateElevationRelativeSet {
        val builder = JonSharedCmdRotary.RotateElevationRelativeSet.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                "direction" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setDirection(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for RotateElevationRelativeSet")
            }
        }

        return builder.build()
    }

    /**
     * Build HaltElevation from Transit data
     */
    private fun buildRotaryAxisElevationHalt(data: Map<*, *>): JonSharedCmdRotary.HaltElevation {
        val builder = JonSharedCmdRotary.HaltElevation.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for HaltElevation")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanUnpause from Transit data
     */
    private fun buildRotaryScanUnpause(data: Map<*, *>): JonSharedCmdRotary.ScanUnpause {
        val builder = JonSharedCmdRotary.ScanUnpause.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanUnpause")
            }
        }

        return builder.build()
    }

    /**
     * Build SetMode from Transit data
     */
    private fun buildRotarySetMode(data: Map<*, *>): JonSharedCmdRotary.SetMode {
        val builder = JonSharedCmdRotary.SetMode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "mode" -> {
                    // WARNING: Could not determine enum class from type-ref
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.uppercase().replace("-", "_")
                    // TODO: Manually specify enum type
                    // builder.setMode(SomeEnumType.valueOf(enumValue))
                }
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetMode")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanRefreshNodeList from Transit data
     */
    private fun buildRotaryScanRefreshNodeList(data: Map<*, *>): JonSharedCmdRotary.ScanRefreshNodeList {
        val builder = JonSharedCmdRotary.ScanRefreshNodeList.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanRefreshNodeList")
            }
        }

        return builder.build()
    }

    /**
     * Build ScanUpdateNode from Transit data
     */
    private fun buildRotaryScanUpdateNode(data: Map<*, *>): JonSharedCmdRotary.ScanUpdateNode {
        val builder = JonSharedCmdRotary.ScanUpdateNode.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "index" -> builder.setIndex(convertInt(value))
                "DayZoomTableValue" -> builder.setDayZoomTableValue(convertInt(value))
                "HeatZoomTableValue" -> builder.setHeatZoomTableValue(convertInt(value))
                "azimuth" -> builder.setAzimuth(convertDouble(value))
                "elevation" -> builder.setElevation(convertDouble(value))
                "linger" -> builder.setLinger(convertDouble(value))
                "speed" -> builder.setSpeed(convertDouble(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for ScanUpdateNode")
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildCompass(data: Map<*, *>): JonSharedCmdCompass.Root {
        val builder = JonSharedCmdCompass.Root.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "calibrate-cencel" -> builder.setCalibrateCencel(buildCompassCalibrateCencel(value as Map<*, *>))
                "start" -> builder.setStart(buildCompassStart(value as Map<*, *>))
                "set-offset-angle-elevation" -> builder.setSetOffsetAngleElevation(buildCompassSetOffsetAngleElevation(value as Map<*, *>))
                "stop" -> builder.setStop(buildCompassStop(value as Map<*, *>))
                "calibrate-next" -> builder.setCalibrateNext(buildCompassCalibrateNext(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildCompassGetMeteo(value as Map<*, *>))
                "set-use-rotary-position" -> builder.setSetUseRotaryPosition(buildCompassSetUseRotaryPosition(value as Map<*, *>))
                "set-magnetic-declination" -> builder.setSetMagneticDeclination(buildCompassSetMagneticDeclination(value as Map<*, *>))
                "start-calibrate-short" -> builder.setStartCalibrateShort(buildCompassStartCalibrateShort(value as Map<*, *>))
                "start-calibrate-long" -> builder.setStartCalibrateLong(buildCompassStartCalibrateLong(value as Map<*, *>))
                "set-offset-angle-azimuth" -> builder.setSetOffsetAngleAzimuth(buildCompassSetOffsetAngleAzimuth(value as Map<*, *>))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Root")
            }
        }

        return builder.build()
    }

    /**
     * Build CalibrateCencel from Transit data
     */
    private fun buildCompassCalibrateCencel(data: Map<*, *>): JonSharedCmdCompass.CalibrateCencel {
        val builder = JonSharedCmdCompass.CalibrateCencel.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for CalibrateCencel")
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildCompassStart(data: Map<*, *>): JonSharedCmdCompass.Start {
        val builder = JonSharedCmdCompass.Start.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Start")
            }
        }

        return builder.build()
    }

    /**
     * Build SetOffsetAngleElevation from Transit data
     */
    private fun buildCompassSetOffsetAngleElevation(data: Map<*, *>): JonSharedCmdCompass.SetOffsetAngleElevation {
        val builder = JonSharedCmdCompass.SetOffsetAngleElevation.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetOffsetAngleElevation")
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildCompassStop(data: Map<*, *>): JonSharedCmdCompass.Stop {
        val builder = JonSharedCmdCompass.Stop.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Stop")
            }
        }

        return builder.build()
    }

    /**
     * Build CalibrateNext from Transit data
     */
    private fun buildCompassCalibrateNext(data: Map<*, *>): JonSharedCmdCompass.CalibrateNext {
        val builder = JonSharedCmdCompass.CalibrateNext.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for CalibrateNext")
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildCompassGetMeteo(data: Map<*, *>): JonSharedCmdCompass.GetMeteo {
        val builder = JonSharedCmdCompass.GetMeteo.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for GetMeteo")
            }
        }

        return builder.build()
    }

    /**
     * Build SetUseRotaryPosition from Transit data
     */
    private fun buildCompassSetUseRotaryPosition(data: Map<*, *>): JonSharedCmdCompass.SetUseRotaryPosition {
        val builder = JonSharedCmdCompass.SetUseRotaryPosition.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "flag" -> builder.setFlag(convertBoolean(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetUseRotaryPosition")
            }
        }

        return builder.build()
    }

    /**
     * Build SetMagneticDeclination from Transit data
     */
    private fun buildCompassSetMagneticDeclination(data: Map<*, *>): JonSharedCmdCompass.SetMagneticDeclination {
        val builder = JonSharedCmdCompass.SetMagneticDeclination.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetMagneticDeclination")
            }
        }

        return builder.build()
    }

    /**
     * Build CalibrateStartShort from Transit data
     */
    private fun buildCompassStartCalibrateShort(data: Map<*, *>): JonSharedCmdCompass.CalibrateStartShort {
        val builder = JonSharedCmdCompass.CalibrateStartShort.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for CalibrateStartShort")
            }
        }

        return builder.build()
    }

    /**
     * Build CalibrateStartLong from Transit data
     */
    private fun buildCompassStartCalibrateLong(data: Map<*, *>): JonSharedCmdCompass.CalibrateStartLong {
        val builder = JonSharedCmdCompass.CalibrateStartLong.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for CalibrateStartLong")
            }
        }

        return builder.build()
    }

    /**
     * Build SetOffsetAngleAzimuth from Transit data
     */
    private fun buildCompassSetOffsetAngleAzimuth(data: Map<*, *>): JonSharedCmdCompass.SetOffsetAngleAzimuth {
        val builder = JonSharedCmdCompass.SetOffsetAngleAzimuth.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                "value" -> builder.setValue(convertFloat(value))
                else -> LoggingUtils.log("WARN", "Unknown field \$key for SetOffsetAngleAzimuth")
            }
        }

        return builder.build()
    }

    /**
     * Build Frozen from Transit data
     */
    private fun buildFrozen(data: Map<*, *>): JonSharedCmd.Frozen {
        val builder = JonSharedCmd.Frozen.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (keyToString(key)) {
                else -> LoggingUtils.log("WARN", "Unknown field \$key for Frozen")
            }
        }

        return builder.build()
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractOsd(msg: JonSharedCmdOsd.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasShowDefaultScreen()) {
            result["show-default-screen"] = extractOsdShowDefaultScreen(msg.getShowDefaultScreen())
        }
        if (msg.hasShowLrfMeasureScreen()) {
            result["show-lrf-measure-screen"] = extractOsdShowLrfMeasureScreen(msg.getShowLrfMeasureScreen())
        }
        if (msg.hasShowLrfResultScreen()) {
            result["show-lrf-result-screen"] = extractOsdShowLrfResultScreen(msg.getShowLrfResultScreen())
        }
        if (msg.hasShowLrfResultSimplifiedScreen()) {
            result["show-lrf-result-simplified-screen"] = extractOsdShowLrfResultSimplifiedScreen(msg.getShowLrfResultSimplifiedScreen())
        }
        if (msg.hasEnableHeatOsd()) {
            result["enable-heat-osd"] = extractOsdEnableHeatOsd(msg.getEnableHeatOsd())
        }
        if (msg.hasDisableHeatOsd()) {
            result["disable-heat-osd"] = extractOsdDisableHeatOsd(msg.getDisableHeatOsd())
        }
        if (msg.hasEnableDayOsd()) {
            result["enable-day-osd"] = extractOsdEnableDayOsd(msg.getEnableDayOsd())
        }
        if (msg.hasDisableDayOsd()) {
            result["disable-day-osd"] = extractOsdDisableDayOsd(msg.getDisableDayOsd())
        }

        return result
    }

    /**
     * Extract Transit data from ShowDefaultScreen
     */
    private fun extractOsdShowDefaultScreen(msg: JonSharedCmdOsd.ShowDefaultScreen): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShowLRFMeasureScreen
     */
    private fun extractOsdShowLrfMeasureScreen(msg: JonSharedCmdOsd.ShowLRFMeasureScreen): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShowLRFResultScreen
     */
    private fun extractOsdShowLrfResultScreen(msg: JonSharedCmdOsd.ShowLRFResultScreen): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShowLRFResultSimplifiedScreen
     */
    private fun extractOsdShowLrfResultSimplifiedScreen(msg: JonSharedCmdOsd.ShowLRFResultSimplifiedScreen): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnableHeatOSD
     */
    private fun extractOsdEnableHeatOsd(msg: JonSharedCmdOsd.EnableHeatOSD): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from DisableHeatOSD
     */
    private fun extractOsdDisableHeatOsd(msg: JonSharedCmdOsd.DisableHeatOSD): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnableDayOSD
     */
    private fun extractOsdEnableDayOsd(msg: JonSharedCmdOsd.EnableDayOSD): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from DisableDayOSD
     */
    private fun extractOsdDisableDayOsd(msg: JonSharedCmdOsd.DisableDayOSD): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Ping
     */
    private fun extractPing(msg: JonSharedCmd.Ping): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractSystem(msg: JonSharedCmdSystem.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasGeodesicModeDisable()) {
            result["geodesic-mode-disable"] = extractSystemGeodesicModeDisable(msg.getGeodesicModeDisable())
        }
        if (msg.hasStartAll()) {
            result["start-all"] = extractSystemStartAll(msg.getStartAll())
        }
        if (msg.hasGeodesicModeEnable()) {
            result["geodesic-mode-enable"] = extractSystemGeodesicModeEnable(msg.getGeodesicModeEnable())
        }
        if (msg.hasLocalization()) {
            result["localization"] = extractSystemLocalization(msg.getLocalization())
        }
        if (msg.hasUnmarkRecImportant()) {
            result["unmark-rec-important"] = extractSystemUnmarkRecImportant(msg.getUnmarkRecImportant())
        }
        if (msg.hasStopRec()) {
            result["stop-rec"] = extractSystemStopRec(msg.getStopRec())
        }
        if (msg.hasReboot()) {
            result["reboot"] = extractSystemReboot(msg.getReboot())
        }
        if (msg.hasStartRec()) {
            result["start-rec"] = extractSystemStartRec(msg.getStartRec())
        }
        if (msg.hasPowerOff()) {
            result["power-off"] = extractSystemPowerOff(msg.getPowerOff())
        }
        if (msg.hasResetConfigs()) {
            result["reset-configs"] = extractSystemResetConfigs(msg.getResetConfigs())
        }
        if (msg.hasStopAll()) {
            result["stop-all"] = extractSystemStopAll(msg.getStopAll())
        }
        if (msg.hasEnterTransport()) {
            result["enter-transport"] = extractSystemEnterTransport(msg.getEnterTransport())
        }
        if (msg.hasMarkRecImportant()) {
            result["mark-rec-important"] = extractSystemMarkRecImportant(msg.getMarkRecImportant())
        }

        return result
    }

    /**
     * Extract Transit data from DisableGeodesicMode
     */
    private fun extractSystemGeodesicModeDisable(msg: JonSharedCmdSystem.DisableGeodesicMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StartALl
     */
    private fun extractSystemStartAll(msg: JonSharedCmdSystem.StartALl): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnableGeodesicMode
     */
    private fun extractSystemGeodesicModeEnable(msg: JonSharedCmdSystem.EnableGeodesicMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetLocalization
     */
    private fun extractSystemLocalization(msg: JonSharedCmdSystem.SetLocalization): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["loc"] =
            msg
                .getLoc()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from UnmarkRecImportant
     */
    private fun extractSystemUnmarkRecImportant(msg: JonSharedCmdSystem.UnmarkRecImportant): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StopRec
     */
    private fun extractSystemStopRec(msg: JonSharedCmdSystem.StopRec): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Reboot
     */
    private fun extractSystemReboot(msg: JonSharedCmdSystem.Reboot): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StartRec
     */
    private fun extractSystemStartRec(msg: JonSharedCmdSystem.StartRec): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from PowerOff
     */
    private fun extractSystemPowerOff(msg: JonSharedCmdSystem.PowerOff): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ResetConfigs
     */
    private fun extractSystemResetConfigs(msg: JonSharedCmdSystem.ResetConfigs): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StopALl
     */
    private fun extractSystemStopAll(msg: JonSharedCmdSystem.StopALl): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnterTransport
     */
    private fun extractSystemEnterTransport(msg: JonSharedCmdSystem.EnterTransport): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from MarkRecImportant
     */
    private fun extractSystemMarkRecImportant(msg: JonSharedCmdSystem.MarkRecImportant): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Noop
     */
    private fun extractNoop(msg: JonSharedCmd.Noop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractCv(msg: JonSharedCmdCv.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasVampireModeEnable()) {
            result["vampire-mode-enable"] = extractCvVampireModeEnable(msg.getVampireModeEnable())
        }
        if (msg.hasVampireModeDisable()) {
            result["vampire-mode-disable"] = extractCvVampireModeDisable(msg.getVampireModeDisable())
        }
        if (msg.hasDumpStop()) {
            result["dump-stop"] = extractCvDumpStop(msg.getDumpStop())
        }
        if (msg.hasStabilizationModeDisable()) {
            result["stabilization-mode-disable"] = extractCvStabilizationModeDisable(msg.getStabilizationModeDisable())
        }
        if (msg.hasSetAutoFocus()) {
            result["set-auto-focus"] = extractCvSetAutoFocus(msg.getSetAutoFocus())
        }
        if (msg.hasStartTrackNdc()) {
            result["start-track-ndc"] = extractCvStartTrackNdc(msg.getStartTrackNdc())
        }
        if (msg.hasDumpStart()) {
            result["dump-start"] = extractCvDumpStart(msg.getDumpStart())
        }
        if (msg.hasStopTrack()) {
            result["stop-track"] = extractCvStopTrack(msg.getStopTrack())
        }
        if (msg.hasStabilizationModeEnable()) {
            result["stabilization-mode-enable"] = extractCvStabilizationModeEnable(msg.getStabilizationModeEnable())
        }

        return result
    }

    /**
     * Extract Transit data from VampireModeEnable
     */
    private fun extractCvVampireModeEnable(msg: JonSharedCmdCv.VampireModeEnable): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from VampireModeDisable
     */
    private fun extractCvVampireModeDisable(msg: JonSharedCmdCv.VampireModeDisable): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from DumpStop
     */
    private fun extractCvDumpStop(msg: JonSharedCmdCv.DumpStop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StabilizationModeDisable
     */
    private fun extractCvStabilizationModeDisable(msg: JonSharedCmdCv.StabilizationModeDisable): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetAutoFocus
     */
    private fun extractCvSetAutoFocus(msg: JonSharedCmdCv.SetAutoFocus): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["channel"] =
            msg
                .getChannel()
                .name
                .lowercase()
                .replace("_", "-")
        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from StartTrackNDC
     */
    private fun extractCvStartTrackNdc(msg: JonSharedCmdCv.StartTrackNDC): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["channel"] =
            msg
                .getChannel()
                .name
                .lowercase()
                .replace("_", "-")
        result["x"] = msg.getX()
        result["y"] = msg.getY()
        result["frame-time"] = msg.getFrameTime()

        return result
    }

    /**
     * Extract Transit data from DumpStart
     */
    private fun extractCvDumpStart(msg: JonSharedCmdCv.DumpStart): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StopTrack
     */
    private fun extractCvStopTrack(msg: JonSharedCmdCv.StopTrack): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StabilizationModeEnable
     */
    private fun extractCvStabilizationModeEnable(msg: JonSharedCmdCv.StabilizationModeEnable): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractGps(msg: JonSharedCmdGps.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasStart()) {
            result["start"] = extractGpsStart(msg.getStart())
        }
        if (msg.hasStop()) {
            result["stop"] = extractGpsStop(msg.getStop())
        }
        if (msg.hasSetManualPosition()) {
            result["set-manual-position"] = extractGpsSetManualPosition(msg.getSetManualPosition())
        }
        if (msg.hasSetUseManualPosition()) {
            result["set-use-manual-position"] = extractGpsSetUseManualPosition(msg.getSetUseManualPosition())
        }
        if (msg.hasGetMeteo()) {
            result["get-meteo"] = extractGpsGetMeteo(msg.getGetMeteo())
        }

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractGpsStart(msg: JonSharedCmdGps.Start): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractGpsStop(msg: JonSharedCmdGps.Stop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetManualPosition
     */
    private fun extractGpsSetManualPosition(msg: JonSharedCmdGps.SetManualPosition): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["latitude"] = msg.getLatitude()
        result["longitude"] = msg.getLongitude()
        result["altitude"] = msg.getAltitude()

        return result
    }

    /**
     * Extract Transit data from SetUseManualPosition
     */
    private fun extractGpsSetUseManualPosition(msg: JonSharedCmdGps.SetUseManualPosition): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["flag"] = msg.getFlag()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractGpsGetMeteo(msg: JonSharedCmdGps.GetMeteo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractLrf(msg: JonSharedCmdLrf.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasTargetDesignatorOff()) {
            result["target-designator-off"] = extractLrfTargetDesignatorOff(msg.getTargetDesignatorOff())
        }
        if (msg.hasTargetDesignatorOnModeB()) {
            result["target-designator-on-mode-b"] = extractLrfTargetDesignatorOnModeB(msg.getTargetDesignatorOnModeB())
        }
        if (msg.hasDisableFogMode()) {
            result["disable-fog-mode"] = extractLrfDisableFogMode(msg.getDisableFogMode())
        }
        if (msg.hasSetScanMode()) {
            result["set-scan-mode"] = extractLrfSetScanMode(msg.getSetScanMode())
        }
        if (msg.hasRefineOff()) {
            result["refine-off"] = extractLrfRefineOff(msg.getRefineOff())
        }
        if (msg.hasScanOff()) {
            result["scan-off"] = extractLrfScanOff(msg.getScanOff())
        }
        if (msg.hasRefineOn()) {
            result["refine-on"] = extractLrfRefineOn(msg.getRefineOn())
        }
        if (msg.hasStart()) {
            result["start"] = extractLrfStart(msg.getStart())
        }
        if (msg.hasMeasure()) {
            result["measure"] = extractLrfMeasure(msg.getMeasure())
        }
        if (msg.hasScanOn()) {
            result["scan-on"] = extractLrfScanOn(msg.getScanOn())
        }
        if (msg.hasStop()) {
            result["stop"] = extractLrfStop(msg.getStop())
        }
        if (msg.hasNewSession()) {
            result["new-session"] = extractLrfNewSession(msg.getNewSession())
        }
        if (msg.hasGetMeteo()) {
            result["get-meteo"] = extractLrfGetMeteo(msg.getGetMeteo())
        }
        if (msg.hasEnableFogMode()) {
            result["enable-fog-mode"] = extractLrfEnableFogMode(msg.getEnableFogMode())
        }
        if (msg.hasTargetDesignatorOnModeA()) {
            result["target-designator-on-mode-a"] = extractLrfTargetDesignatorOnModeA(msg.getTargetDesignatorOnModeA())
        }

        return result
    }

    /**
     * Extract Transit data from TargetDesignatorOff
     */
    private fun extractLrfTargetDesignatorOff(msg: JonSharedCmdLrf.TargetDesignatorOff): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from TargetDesignatorOnModeB
     */
    private fun extractLrfTargetDesignatorOnModeB(msg: JonSharedCmdLrf.TargetDesignatorOnModeB): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from DisableFogMode
     */
    private fun extractLrfDisableFogMode(msg: JonSharedCmdLrf.DisableFogMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetScanMode
     */
    private fun extractLrfSetScanMode(msg: JonSharedCmdLrf.SetScanMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["mode"] =
            msg
                .getMode()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RefineOff
     */
    private fun extractLrfRefineOff(msg: JonSharedCmdLrf.RefineOff): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanOff
     */
    private fun extractLrfScanOff(msg: JonSharedCmdLrf.ScanOff): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from RefineOn
     */
    private fun extractLrfRefineOn(msg: JonSharedCmdLrf.RefineOn): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractLrfStart(msg: JonSharedCmdLrf.Start): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Measure
     */
    private fun extractLrfMeasure(msg: JonSharedCmdLrf.Measure): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanOn
     */
    private fun extractLrfScanOn(msg: JonSharedCmdLrf.ScanOn): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractLrfStop(msg: JonSharedCmdLrf.Stop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from NewSession
     */
    private fun extractLrfNewSession(msg: JonSharedCmdLrf.NewSession): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractLrfGetMeteo(msg: JonSharedCmdLrf.GetMeteo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnableFogMode
     */
    private fun extractLrfEnableFogMode(msg: JonSharedCmdLrf.EnableFogMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from TargetDesignatorOnModeA
     */
    private fun extractLrfTargetDesignatorOnModeA(msg: JonSharedCmdLrf.TargetDesignatorOnModeA): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractDayCamGlassHeater(msg: JonSharedCmdDayCamGlassHeater.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasStart()) {
            result["start"] = extractDayCamGlassHeaterStart(msg.getStart())
        }
        if (msg.hasStop()) {
            result["stop"] = extractDayCamGlassHeaterStop(msg.getStop())
        }
        if (msg.hasTurnOn()) {
            result["turn-on"] = extractDayCamGlassHeaterTurnOn(msg.getTurnOn())
        }
        if (msg.hasTurnOff()) {
            result["turn-off"] = extractDayCamGlassHeaterTurnOff(msg.getTurnOff())
        }
        if (msg.hasGetMeteo()) {
            result["get-meteo"] = extractDayCamGlassHeaterGetMeteo(msg.getGetMeteo())
        }

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractDayCamGlassHeaterStart(msg: JonSharedCmdDayCamGlassHeater.Start): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractDayCamGlassHeaterStop(msg: JonSharedCmdDayCamGlassHeater.Stop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from TurnOn
     */
    private fun extractDayCamGlassHeaterTurnOn(msg: JonSharedCmdDayCamGlassHeater.TurnOn): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from TurnOff
     */
    private fun extractDayCamGlassHeaterTurnOff(msg: JonSharedCmdDayCamGlassHeater.TurnOff): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractDayCamGlassHeaterGetMeteo(msg: JonSharedCmdDayCamGlassHeater.GetMeteo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractDayCamera(msg: JonSharedCmdDayCamera.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasZoom()) {
            result["zoom"] = extractDayCameraZoom(msg.getZoom())
        }
        if (msg.hasSetInfraRedFilter()) {
            result["set-infra-red-filter"] = extractDayCameraSetInfraRedFilter(msg.getSetInfraRedFilter())
        }
        if (msg.hasSetClaheLevel()) {
            result["set-clahe-level"] = extractDayCameraSetClaheLevel(msg.getSetClaheLevel())
        }
        if (msg.hasPrevFxMode()) {
            result["prev-fx-mode"] = extractDayCameraPrevFxMode(msg.getPrevFxMode())
        }
        if (msg.hasStart()) {
            result["start"] = extractDayCameraStart(msg.getStart())
        }
        if (msg.hasHaltAll()) {
            result["halt-all"] = extractDayCameraHaltAll(msg.getHaltAll())
        }
        if (msg.hasSetDigitalZoomLevel()) {
            result["set-digital-zoom-level"] = extractDayCameraSetDigitalZoomLevel(msg.getSetDigitalZoomLevel())
        }
        if (msg.hasStop()) {
            result["stop"] = extractDayCameraStop(msg.getStop())
        }
        if (msg.hasPhoto()) {
            result["photo"] = extractDayCameraPhoto(msg.getPhoto())
        }
        if (msg.hasGetMeteo()) {
            result["get-meteo"] = extractDayCameraGetMeteo(msg.getGetMeteo())
        }
        if (msg.hasFocus()) {
            result["focus"] = extractDayCameraFocus(msg.getFocus())
        }
        if (msg.hasSetFxMode()) {
            result["set-fx-mode"] = extractDayCameraSetFxMode(msg.getSetFxMode())
        }
        if (msg.hasSetIris()) {
            result["set-iris"] = extractDayCameraSetIris(msg.getSetIris())
        }
        if (msg.hasRefreshFxMode()) {
            result["refresh-fx-mode"] = extractDayCameraRefreshFxMode(msg.getRefreshFxMode())
        }
        if (msg.hasSetAutoIris()) {
            result["set-auto-iris"] = extractDayCameraSetAutoIris(msg.getSetAutoIris())
        }
        if (msg.hasNextFxMode()) {
            result["next-fx-mode"] = extractDayCameraNextFxMode(msg.getNextFxMode())
        }
        if (msg.hasShiftClaheLevel()) {
            result["shift-clahe-level"] = extractDayCameraShiftClaheLevel(msg.getShiftClaheLevel())
        }

        return result
    }

    /**
     * Extract Transit data from Zoom
     */
    private fun extractDayCameraZoom(msg: JonSharedCmdDayCamera.Zoom): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasPrevZoomTablePos()) {
            result["prev-zoom-table-pos"] = extractDayCameraZoomPrevZoomTablePos(msg.getPrevZoomTablePos())
        }
        if (msg.hasOffset()) {
            result["offset"] = extractDayCameraZoomOffset(msg.getOffset())
        }
        if (msg.hasMove()) {
            result["move"] = extractDayCameraZoomMove(msg.getMove())
        }
        if (msg.hasResetZoom()) {
            result["reset-zoom"] = extractDayCameraZoomResetZoom(msg.getResetZoom())
        }
        if (msg.hasNextZoomTablePos()) {
            result["next-zoom-table-pos"] = extractDayCameraZoomNextZoomTablePos(msg.getNextZoomTablePos())
        }
        if (msg.hasSetValue()) {
            result["set-value"] = extractDayCameraZoomSetValue(msg.getSetValue())
        }
        if (msg.hasSetZoomTableValue()) {
            result["set-zoom-table-value"] = extractDayCameraZoomSetZoomTableValue(msg.getSetZoomTableValue())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractDayCameraZoomHalt(msg.getHalt())
        }
        if (msg.hasSaveToTable()) {
            result["save-to-table"] = extractDayCameraZoomSaveToTable(msg.getSaveToTable())
        }

        return result
    }

    /**
     * Extract Transit data from PrevZoomTablePos
     */
    private fun extractDayCameraZoomPrevZoomTablePos(msg: JonSharedCmdDayCamera.PrevZoomTablePos): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Offset
     */
    private fun extractDayCameraZoomOffset(msg: JonSharedCmdDayCamera.Offset): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["offset-value"] = msg.getOffsetValue()

        return result
    }

    /**
     * Extract Transit data from Move
     */
    private fun extractDayCameraZoomMove(msg: JonSharedCmdDayCamera.Move): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["target-value"] = msg.getTargetValue()
        result["speed"] = msg.getSpeed()

        return result
    }

    /**
     * Extract Transit data from ResetZoom
     */
    private fun extractDayCameraZoomResetZoom(msg: JonSharedCmdDayCamera.ResetZoom): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from NextZoomTablePos
     */
    private fun extractDayCameraZoomNextZoomTablePos(msg: JonSharedCmdDayCamera.NextZoomTablePos): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetValue
     */
    private fun extractDayCameraZoomSetValue(msg: JonSharedCmdDayCamera.SetValue): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from SetZoomTableValue
     */
    private fun extractDayCameraZoomSetZoomTableValue(msg: JonSharedCmdDayCamera.SetZoomTableValue): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Halt
     */
    private fun extractDayCameraZoomHalt(msg: JonSharedCmdDayCamera.Halt): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SaveToTable
     */
    private fun extractDayCameraZoomSaveToTable(msg: JonSharedCmdDayCamera.SaveToTable): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetInfraRedFilter
     */
    private fun extractDayCameraSetInfraRedFilter(msg: JonSharedCmdDayCamera.SetInfraRedFilter): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from SetClaheLevel
     */
    private fun extractDayCameraSetClaheLevel(msg: JonSharedCmdDayCamera.SetClaheLevel): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from PrevFxMode
     */
    private fun extractDayCameraPrevFxMode(msg: JonSharedCmdDayCamera.PrevFxMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractDayCameraStart(msg: JonSharedCmdDayCamera.Start): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from HaltAll
     */
    private fun extractDayCameraHaltAll(msg: JonSharedCmdDayCamera.HaltAll): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetDigitalZoomLevel
     */
    private fun extractDayCameraSetDigitalZoomLevel(msg: JonSharedCmdDayCamera.SetDigitalZoomLevel): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractDayCameraStop(msg: JonSharedCmdDayCamera.Stop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Photo
     */
    private fun extractDayCameraPhoto(msg: JonSharedCmdDayCamera.Photo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractDayCameraGetMeteo(msg: JonSharedCmdDayCamera.GetMeteo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Focus
     */
    private fun extractDayCameraFocus(msg: JonSharedCmdDayCamera.Focus): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSetValue()) {
            result["set-value"] = extractDayCameraFocusSetValue(msg.getSetValue())
        }
        if (msg.hasMove()) {
            result["move"] = extractDayCameraFocusMove(msg.getMove())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractDayCameraFocusHalt(msg.getHalt())
        }
        if (msg.hasOffset()) {
            result["offset"] = extractDayCameraFocusOffset(msg.getOffset())
        }
        if (msg.hasResetFocus()) {
            result["reset-focus"] = extractDayCameraFocusResetFocus(msg.getResetFocus())
        }
        if (msg.hasSaveToTableFocus()) {
            result["save-to-table-focus"] = extractDayCameraFocusSaveToTableFocus(msg.getSaveToTableFocus())
        }

        return result
    }

    /**
     * Extract Transit data from SetValue
     */
    private fun extractDayCameraFocusSetValue(msg: JonSharedCmdDayCamera.SetValue): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Move
     */
    private fun extractDayCameraFocusMove(msg: JonSharedCmdDayCamera.Move): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["target-value"] = msg.getTargetValue()
        result["speed"] = msg.getSpeed()

        return result
    }

    /**
     * Extract Transit data from Halt
     */
    private fun extractDayCameraFocusHalt(msg: JonSharedCmdDayCamera.Halt): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Offset
     */
    private fun extractDayCameraFocusOffset(msg: JonSharedCmdDayCamera.Offset): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["offset-value"] = msg.getOffsetValue()

        return result
    }

    /**
     * Extract Transit data from ResetFocus
     */
    private fun extractDayCameraFocusResetFocus(msg: JonSharedCmdDayCamera.ResetFocus): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SaveToTableFocus
     */
    private fun extractDayCameraFocusSaveToTableFocus(msg: JonSharedCmdDayCamera.SaveToTableFocus): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetFxMode
     */
    private fun extractDayCameraSetFxMode(msg: JonSharedCmdDayCamera.SetFxMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["mode"] =
            msg
                .getMode()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from SetIris
     */
    private fun extractDayCameraSetIris(msg: JonSharedCmdDayCamera.SetIris): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from RefreshFxMode
     */
    private fun extractDayCameraRefreshFxMode(msg: JonSharedCmdDayCamera.RefreshFxMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetAutoIris
     */
    private fun extractDayCameraSetAutoIris(msg: JonSharedCmdDayCamera.SetAutoIris): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from NextFxMode
     */
    private fun extractDayCameraNextFxMode(msg: JonSharedCmdDayCamera.NextFxMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShiftClaheLevel
     */
    private fun extractDayCameraShiftClaheLevel(msg: JonSharedCmdDayCamera.ShiftClaheLevel): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractHeatCamera(msg: JonSharedCmdHeatCamera.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSetDdeLevel()) {
            result["set-dde-level"] = extractHeatCameraSetDdeLevel(msg.getSetDdeLevel())
        }
        if (msg.hasSetCalibMode()) {
            result["set-calib-mode"] = extractHeatCameraSetCalibMode(msg.getSetCalibMode())
        }
        if (msg.hasZoom()) {
            result["zoom"] = extractHeatCameraZoom(msg.getZoom())
        }
        if (msg.hasSetAgc()) {
            result["set-agc"] = extractHeatCameraSetAgc(msg.getSetAgc())
        }
        if (msg.hasShiftDde()) {
            result["shift-dde"] = extractHeatCameraShiftDde(msg.getShiftDde())
        }
        if (msg.hasSetFilter()) {
            result["set-filter"] = extractHeatCameraSetFilter(msg.getSetFilter())
        }
        if (msg.hasSetClaheLevel()) {
            result["set-clahe-level"] = extractHeatCameraSetClaheLevel(msg.getSetClaheLevel())
        }
        if (msg.hasDisableDde()) {
            result["disable-dde"] = extractHeatCameraDisableDde(msg.getDisableDde())
        }
        if (msg.hasPrevFxMode()) {
            result["prev-fx-mode"] = extractHeatCameraPrevFxMode(msg.getPrevFxMode())
        }
        if (msg.hasStart()) {
            result["start"] = extractHeatCameraStart(msg.getStart())
        }
        if (msg.hasFocusStepMinus()) {
            result["focus-step-minus"] = extractHeatCameraFocusStepMinus(msg.getFocusStepMinus())
        }
        if (msg.hasSetDigitalZoomLevel()) {
            result["set-digital-zoom-level"] = extractHeatCameraSetDigitalZoomLevel(msg.getSetDigitalZoomLevel())
        }
        if (msg.hasEnableDde()) {
            result["enable-dde"] = extractHeatCameraEnableDde(msg.getEnableDde())
        }
        if (msg.hasFocusStop()) {
            result["focus-stop"] = extractHeatCameraFocusStop(msg.getFocusStop())
        }
        if (msg.hasStop()) {
            result["stop"] = extractHeatCameraStop(msg.getStop())
        }
        if (msg.hasResetZoom()) {
            result["reset-zoom"] = extractHeatCameraResetZoom(msg.getResetZoom())
        }
        if (msg.hasZoomOut()) {
            result["zoom-out"] = extractHeatCameraZoomOut(msg.getZoomOut())
        }
        if (msg.hasPhoto()) {
            result["photo"] = extractHeatCameraPhoto(msg.getPhoto())
        }
        if (msg.hasZoomIn()) {
            result["zoom-in"] = extractHeatCameraZoomIn(msg.getZoomIn())
        }
        if (msg.hasGetMeteo()) {
            result["get-meteo"] = extractHeatCameraGetMeteo(msg.getGetMeteo())
        }
        if (msg.hasFocusStepPlus()) {
            result["focus-step-plus"] = extractHeatCameraFocusStepPlus(msg.getFocusStepPlus())
        }
        if (msg.hasSetFxMode()) {
            result["set-fx-mode"] = extractHeatCameraSetFxMode(msg.getSetFxMode())
        }
        if (msg.hasRefreshFxMode()) {
            result["refresh-fx-mode"] = extractHeatCameraRefreshFxMode(msg.getRefreshFxMode())
        }
        if (msg.hasFocusOut()) {
            result["focus-out"] = extractHeatCameraFocusOut(msg.getFocusOut())
        }
        if (msg.hasSetAutoFocus()) {
            result["set-auto-focus"] = extractHeatCameraSetAutoFocus(msg.getSetAutoFocus())
        }
        if (msg.hasZoomStop()) {
            result["zoom-stop"] = extractHeatCameraZoomStop(msg.getZoomStop())
        }
        if (msg.hasSaveToTable()) {
            result["save-to-table"] = extractHeatCameraSaveToTable(msg.getSaveToTable())
        }
        if (msg.hasNextFxMode()) {
            result["next-fx-mode"] = extractHeatCameraNextFxMode(msg.getNextFxMode())
        }
        if (msg.hasCalibrate()) {
            result["calibrate"] = extractHeatCameraCalibrate(msg.getCalibrate())
        }
        if (msg.hasShiftClaheLevel()) {
            result["shift-clahe-level"] = extractHeatCameraShiftClaheLevel(msg.getShiftClaheLevel())
        }
        if (msg.hasFocusIn()) {
            result["focus-in"] = extractHeatCameraFocusIn(msg.getFocusIn())
        }

        return result
    }

    /**
     * Extract Transit data from SetDDELevel
     */
    private fun extractHeatCameraSetDdeLevel(msg: JonSharedCmdHeatCamera.SetDDELevel): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from SetCalibMode
     */
    private fun extractHeatCameraSetCalibMode(msg: JonSharedCmdHeatCamera.SetCalibMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Zoom
     */
    private fun extractHeatCameraZoom(msg: JonSharedCmdHeatCamera.Zoom): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSetZoomTableValue()) {
            result["set-zoom-table-value"] = extractHeatCameraZoomSetZoomTableValue(msg.getSetZoomTableValue())
        }
        if (msg.hasNextZoomTablePos()) {
            result["next-zoom-table-pos"] = extractHeatCameraZoomNextZoomTablePos(msg.getNextZoomTablePos())
        }
        if (msg.hasPrevZoomTablePos()) {
            result["prev-zoom-table-pos"] = extractHeatCameraZoomPrevZoomTablePos(msg.getPrevZoomTablePos())
        }

        return result
    }

    /**
     * Extract Transit data from SetZoomTableValue
     */
    private fun extractHeatCameraZoomSetZoomTableValue(msg: JonSharedCmdHeatCamera.SetZoomTableValue): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from NextZoomTablePos
     */
    private fun extractHeatCameraZoomNextZoomTablePos(msg: JonSharedCmdHeatCamera.NextZoomTablePos): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from PrevZoomTablePos
     */
    private fun extractHeatCameraZoomPrevZoomTablePos(msg: JonSharedCmdHeatCamera.PrevZoomTablePos): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetAGC
     */
    private fun extractHeatCameraSetAgc(msg: JonSharedCmdHeatCamera.SetAGC): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] =
            msg
                .getValue()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from ShiftDDE
     */
    private fun extractHeatCameraShiftDde(msg: JonSharedCmdHeatCamera.ShiftDDE): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from SetFilters
     */
    private fun extractHeatCameraSetFilter(msg: JonSharedCmdHeatCamera.SetFilters): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] =
            msg
                .getValue()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from SetClaheLevel
     */
    private fun extractHeatCameraSetClaheLevel(msg: JonSharedCmdHeatCamera.SetClaheLevel): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from DisableDDE
     */
    private fun extractHeatCameraDisableDde(msg: JonSharedCmdHeatCamera.DisableDDE): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from PrevFxMode
     */
    private fun extractHeatCameraPrevFxMode(msg: JonSharedCmdHeatCamera.PrevFxMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractHeatCameraStart(msg: JonSharedCmdHeatCamera.Start): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from FocusStepMinus
     */
    private fun extractHeatCameraFocusStepMinus(msg: JonSharedCmdHeatCamera.FocusStepMinus): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetDigitalZoomLevel
     */
    private fun extractHeatCameraSetDigitalZoomLevel(msg: JonSharedCmdHeatCamera.SetDigitalZoomLevel): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from EnableDDE
     */
    private fun extractHeatCameraEnableDde(msg: JonSharedCmdHeatCamera.EnableDDE): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from FocusStop
     */
    private fun extractHeatCameraFocusStop(msg: JonSharedCmdHeatCamera.FocusStop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractHeatCameraStop(msg: JonSharedCmdHeatCamera.Stop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ResetZoom
     */
    private fun extractHeatCameraResetZoom(msg: JonSharedCmdHeatCamera.ResetZoom): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ZoomOut
     */
    private fun extractHeatCameraZoomOut(msg: JonSharedCmdHeatCamera.ZoomOut): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Photo
     */
    private fun extractHeatCameraPhoto(msg: JonSharedCmdHeatCamera.Photo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ZoomIn
     */
    private fun extractHeatCameraZoomIn(msg: JonSharedCmdHeatCamera.ZoomIn): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractHeatCameraGetMeteo(msg: JonSharedCmdHeatCamera.GetMeteo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from FocusStepPlus
     */
    private fun extractHeatCameraFocusStepPlus(msg: JonSharedCmdHeatCamera.FocusStepPlus): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetFxMode
     */
    private fun extractHeatCameraSetFxMode(msg: JonSharedCmdHeatCamera.SetFxMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["mode"] =
            msg
                .getMode()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RefreshFxMode
     */
    private fun extractHeatCameraRefreshFxMode(msg: JonSharedCmdHeatCamera.RefreshFxMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from FocusOut
     */
    private fun extractHeatCameraFocusOut(msg: JonSharedCmdHeatCamera.FocusOut): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetAutoFocus
     */
    private fun extractHeatCameraSetAutoFocus(msg: JonSharedCmdHeatCamera.SetAutoFocus): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from ZoomStop
     */
    private fun extractHeatCameraZoomStop(msg: JonSharedCmdHeatCamera.ZoomStop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SaveToTable
     */
    private fun extractHeatCameraSaveToTable(msg: JonSharedCmdHeatCamera.SaveToTable): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from NextFxMode
     */
    private fun extractHeatCameraNextFxMode(msg: JonSharedCmdHeatCamera.NextFxMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Calibrate
     */
    private fun extractHeatCameraCalibrate(msg: JonSharedCmdHeatCamera.Calibrate): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShiftClaheLevel
     */
    private fun extractHeatCameraShiftClaheLevel(msg: JonSharedCmdHeatCamera.ShiftClaheLevel): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from FocusIn
     */
    private fun extractHeatCameraFocusIn(msg: JonSharedCmdHeatCamera.FocusIn): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractLira(msg: JonSharedCmdLira.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasRefineTarget()) {
            result["refine-target"] = extractLiraRefineTarget(msg.getRefineTarget())
        }

        return result
    }

    /**
     * Extract Transit data from Refine_target
     */
    private fun extractLiraRefineTarget(msg: JonSharedCmdLira.Refine_target): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasTarget()) {
            result["target"] = extractLiraRefineTargetTarget(msg.getTarget())
        }

        return result
    }

    /**
     * Extract Transit data from JonGuiDataLiraTarget
     */
    private fun extractLiraRefineTargetTarget(msg: JonSharedCmdLira.JonGuiDataLiraTarget): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["uuid-part4"] = msg.getUuidPart4()
        result["uuid-part2"] = msg.getUuidPart2()
        result["target-altitude"] = msg.getTargetAltitude()
        result["uuid-part3"] = msg.getUuidPart3()
        result["target-azimuth"] = msg.getTargetAzimuth()
        result["distance"] = msg.getDistance()
        result["target-longitude"] = msg.getTargetLongitude()
        result["timestamp"] = msg.getTimestamp()
        result["uuid-part1"] = msg.getUuidPart1()
        result["target-latitude"] = msg.getTargetLatitude()
        result["target-elevation"] = msg.getTargetElevation()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractLrfCalib(msg: JonSharedCmdLrfAlign.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasDay()) {
            result["day"] = extractLrfCalibDay(msg.getDay())
        }
        if (msg.hasHeat()) {
            result["heat"] = extractLrfCalibHeat(msg.getHeat())
        }

        return result
    }

    /**
     * Extract Transit data from Offsets
     */
    private fun extractLrfCalibDay(msg: JonSharedCmdLrfAlign.Offsets): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet()) {
            result["set"] = extractLrfCalibDaySet(msg.getSet())
        }
        if (msg.hasSave()) {
            result["save"] = extractLrfCalibDaySave(msg.getSave())
        }
        if (msg.hasReset()) {
            result["reset"] = extractLrfCalibDayReset(msg.getReset())
        }
        if (msg.hasShift()) {
            result["shift"] = extractLrfCalibDayShift(msg.getShift())
        }

        return result
    }

    /**
     * Extract Transit data from SetOffsets
     */
    private fun extractLrfCalibDaySet(msg: JonSharedCmdLrfAlign.SetOffsets): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from SaveOffsets
     */
    private fun extractLrfCalibDaySave(msg: JonSharedCmdLrfAlign.SaveOffsets): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ResetOffsets
     */
    private fun extractLrfCalibDayReset(msg: JonSharedCmdLrfAlign.ResetOffsets): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShiftOffsetsBy
     */
    private fun extractLrfCalibDayShift(msg: JonSharedCmdLrfAlign.ShiftOffsetsBy): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from Offsets
     */
    private fun extractLrfCalibHeat(msg: JonSharedCmdLrfAlign.Offsets): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet()) {
            result["set"] = extractLrfCalibHeatSet(msg.getSet())
        }
        if (msg.hasSave()) {
            result["save"] = extractLrfCalibHeatSave(msg.getSave())
        }
        if (msg.hasReset()) {
            result["reset"] = extractLrfCalibHeatReset(msg.getReset())
        }
        if (msg.hasShift()) {
            result["shift"] = extractLrfCalibHeatShift(msg.getShift())
        }

        return result
    }

    /**
     * Extract Transit data from SetOffsets
     */
    private fun extractLrfCalibHeatSet(msg: JonSharedCmdLrfAlign.SetOffsets): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from SaveOffsets
     */
    private fun extractLrfCalibHeatSave(msg: JonSharedCmdLrfAlign.SaveOffsets): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ResetOffsets
     */
    private fun extractLrfCalibHeatReset(msg: JonSharedCmdLrfAlign.ResetOffsets): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShiftOffsetsBy
     */
    private fun extractLrfCalibHeatShift(msg: JonSharedCmdLrfAlign.ShiftOffsetsBy): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractRotary(msg: JonSharedCmdRotary.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasRotateToGps()) {
            result["rotate-to-gps"] = extractRotaryRotateToGps(msg.getRotateToGps())
        }
        if (msg.hasScanPause()) {
            result["scan-pause"] = extractRotaryScanPause(msg.getScanPause())
        }
        if (msg.hasRotateToNdc()) {
            result["rotate-to-ndc"] = extractRotaryRotateToNdc(msg.getRotateToNdc())
        }
        if (msg.hasScanStart()) {
            result["scan-start"] = extractRotaryScanStart(msg.getScanStart())
        }
        if (msg.hasSetPlatformAzimuth()) {
            result["set-platform-azimuth"] = extractRotarySetPlatformAzimuth(msg.getSetPlatformAzimuth())
        }
        if (msg.hasScanStop()) {
            result["scan-stop"] = extractRotaryScanStop(msg.getScanStop())
        }
        if (msg.hasStart()) {
            result["start"] = extractRotaryStart(msg.getStart())
        }
        if (msg.hasStop()) {
            result["stop"] = extractRotaryStop(msg.getStop())
        }
        if (msg.hasSetOriginGps()) {
            result["set-origin-gps"] = extractRotarySetOriginGps(msg.getSetOriginGps())
        }
        if (msg.hasScanNext()) {
            result["scan-next"] = extractRotaryScanNext(msg.getScanNext())
        }
        if (msg.hasSetPlatformBank()) {
            result["set-platform-bank"] = extractRotarySetPlatformBank(msg.getSetPlatformBank())
        }
        if (msg.hasGetMeteo()) {
            result["get-meteo"] = extractRotaryGetMeteo(msg.getGetMeteo())
        }
        if (msg.hasSetUseRotaryAsCompass()) {
            result["set-use-rotary-as-compass"] = extractRotarySetUseRotaryAsCompass(msg.getSetUseRotaryAsCompass())
        }
        if (msg.hasScanPrev()) {
            result["scan-prev"] = extractRotaryScanPrev(msg.getScanPrev())
        }
        if (msg.hasScanAddNode()) {
            result["scan-add-node"] = extractRotaryScanAddNode(msg.getScanAddNode())
        }
        if (msg.hasSetPlatformElevation()) {
            result["set-platform-elevation"] = extractRotarySetPlatformElevation(msg.getSetPlatformElevation())
        }
        if (msg.hasScanSelectNode()) {
            result["scan-select-node"] = extractRotaryScanSelectNode(msg.getScanSelectNode())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractRotaryHalt(msg.getHalt())
        }
        if (msg.hasScanDeleteNode()) {
            result["scan-delete-node"] = extractRotaryScanDeleteNode(msg.getScanDeleteNode())
        }
        if (msg.hasAxis()) {
            result["axis"] = extractRotaryAxis(msg.getAxis())
        }
        if (msg.hasScanUnpause()) {
            result["scan-unpause"] = extractRotaryScanUnpause(msg.getScanUnpause())
        }
        if (msg.hasSetMode()) {
            result["set-mode"] = extractRotarySetMode(msg.getSetMode())
        }
        if (msg.hasScanRefreshNodeList()) {
            result["scan-refresh-node-list"] = extractRotaryScanRefreshNodeList(msg.getScanRefreshNodeList())
        }
        if (msg.hasScanUpdateNode()) {
            result["scan-update-node"] = extractRotaryScanUpdateNode(msg.getScanUpdateNode())
        }

        return result
    }

    /**
     * Extract Transit data from RotateToGPS
     */
    private fun extractRotaryRotateToGps(msg: JonSharedCmdRotary.RotateToGPS): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["latitude"] = msg.getLatitude()
        result["longitude"] = msg.getLongitude()
        result["altitude"] = msg.getAltitude()

        return result
    }

    /**
     * Extract Transit data from ScanPause
     */
    private fun extractRotaryScanPause(msg: JonSharedCmdRotary.ScanPause): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from RotateToNDC
     */
    private fun extractRotaryRotateToNdc(msg: JonSharedCmdRotary.RotateToNDC): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["channel"] =
            msg
                .getChannel()
                .name
                .lowercase()
                .replace("_", "-")
        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from ScanStart
     */
    private fun extractRotaryScanStart(msg: JonSharedCmdRotary.ScanStart): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetPlatformAzimuth
     */
    private fun extractRotarySetPlatformAzimuth(msg: JonSharedCmdRotary.SetPlatformAzimuth): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from ScanStop
     */
    private fun extractRotaryScanStop(msg: JonSharedCmdRotary.ScanStop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractRotaryStart(msg: JonSharedCmdRotary.Start): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractRotaryStop(msg: JonSharedCmdRotary.Stop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetOriginGPS
     */
    private fun extractRotarySetOriginGps(msg: JonSharedCmdRotary.SetOriginGPS): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["latitude"] = msg.getLatitude()
        result["longitude"] = msg.getLongitude()
        result["altitude"] = msg.getAltitude()

        return result
    }

    /**
     * Extract Transit data from ScanNext
     */
    private fun extractRotaryScanNext(msg: JonSharedCmdRotary.ScanNext): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetPlatformBank
     */
    private fun extractRotarySetPlatformBank(msg: JonSharedCmdRotary.SetPlatformBank): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractRotaryGetMeteo(msg: JonSharedCmdRotary.GetMeteo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from setUseRotaryAsCompass
     */
    private fun extractRotarySetUseRotaryAsCompass(msg: JonSharedCmdRotary.setUseRotaryAsCompass): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["flag"] = msg.getFlag()

        return result
    }

    /**
     * Extract Transit data from ScanPrev
     */
    private fun extractRotaryScanPrev(msg: JonSharedCmdRotary.ScanPrev): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanAddNode
     */
    private fun extractRotaryScanAddNode(msg: JonSharedCmdRotary.ScanAddNode): Map<String, Any?> {
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
     * Extract Transit data from SetPlatformElevation
     */
    private fun extractRotarySetPlatformElevation(msg: JonSharedCmdRotary.SetPlatformElevation): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from ScanSelectNode
     */
    private fun extractRotaryScanSelectNode(msg: JonSharedCmdRotary.ScanSelectNode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["index"] = msg.getIndex()

        return result
    }

    /**
     * Extract Transit data from Halt
     */
    private fun extractRotaryHalt(msg: JonSharedCmdRotary.Halt): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanDeleteNode
     */
    private fun extractRotaryScanDeleteNode(msg: JonSharedCmdRotary.ScanDeleteNode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["index"] = msg.getIndex()

        return result
    }

    /**
     * Extract Transit data from Axis
     */
    private fun extractRotaryAxis(msg: JonSharedCmdRotary.Axis): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasAzimuth()) {
            result["azimuth"] = extractRotaryAxisAzimuth(msg.getAzimuth())
        }
        if (msg.hasElevation()) {
            result["elevation"] = extractRotaryAxisElevation(msg.getElevation())
        }

        return result
    }

    /**
     * Extract Transit data from Azimuth
     */
    private fun extractRotaryAxisAzimuth(msg: JonSharedCmdRotary.Azimuth): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSetValue()) {
            result["set-value"] = extractRotaryAxisAzimuthSetValue(msg.getSetValue())
        }
        if (msg.hasRotateTo()) {
            result["rotate-to"] = extractRotaryAxisAzimuthRotateTo(msg.getRotateTo())
        }
        if (msg.hasRotate()) {
            result["rotate"] = extractRotaryAxisAzimuthRotate(msg.getRotate())
        }
        if (msg.hasRelative()) {
            result["relative"] = extractRotaryAxisAzimuthRelative(msg.getRelative())
        }
        if (msg.hasRelativeSet()) {
            result["relative-set"] = extractRotaryAxisAzimuthRelativeSet(msg.getRelativeSet())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractRotaryAxisAzimuthHalt(msg.getHalt())
        }

        return result
    }

    /**
     * Extract Transit data from SetAzimuthValue
     */
    private fun extractRotaryAxisAzimuthSetValue(msg: JonSharedCmdRotary.SetAzimuthValue): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["direction"] =
            msg
                .getDirection()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateAzimuthTo
     */
    private fun extractRotaryAxisAzimuthRotateTo(msg: JonSharedCmdRotary.RotateAzimuthTo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["target-value"] = msg.getTargetValue()
        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateAzimuth
     */
    private fun extractRotaryAxisAzimuthRotate(msg: JonSharedCmdRotary.RotateAzimuth): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateAzimuthRelative
     */
    private fun extractRotaryAxisAzimuthRelative(msg: JonSharedCmdRotary.RotateAzimuthRelative): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateAzimuthRelativeSet
     */
    private fun extractRotaryAxisAzimuthRelativeSet(msg: JonSharedCmdRotary.RotateAzimuthRelativeSet): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["direction"] =
            msg
                .getDirection()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from HaltAzimuth
     */
    private fun extractRotaryAxisAzimuthHalt(msg: JonSharedCmdRotary.HaltAzimuth): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Elevation
     */
    private fun extractRotaryAxisElevation(msg: JonSharedCmdRotary.Elevation): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSetValue()) {
            result["set-value"] = extractRotaryAxisElevationSetValue(msg.getSetValue())
        }
        if (msg.hasRotateTo()) {
            result["rotate-to"] = extractRotaryAxisElevationRotateTo(msg.getRotateTo())
        }
        if (msg.hasRotate()) {
            result["rotate"] = extractRotaryAxisElevationRotate(msg.getRotate())
        }
        if (msg.hasRelative()) {
            result["relative"] = extractRotaryAxisElevationRelative(msg.getRelative())
        }
        if (msg.hasRelativeSet()) {
            result["relative-set"] = extractRotaryAxisElevationRelativeSet(msg.getRelativeSet())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractRotaryAxisElevationHalt(msg.getHalt())
        }

        return result
    }

    /**
     * Extract Transit data from SetElevationValue
     */
    private fun extractRotaryAxisElevationSetValue(msg: JonSharedCmdRotary.SetElevationValue): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from RotateElevationTo
     */
    private fun extractRotaryAxisElevationRotateTo(msg: JonSharedCmdRotary.RotateElevationTo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["target-value"] = msg.getTargetValue()
        result["speed"] = msg.getSpeed()

        return result
    }

    /**
     * Extract Transit data from RotateElevation
     */
    private fun extractRotaryAxisElevationRotate(msg: JonSharedCmdRotary.RotateElevation): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateElevationRelative
     */
    private fun extractRotaryAxisElevationRelative(msg: JonSharedCmdRotary.RotateElevationRelative): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateElevationRelativeSet
     */
    private fun extractRotaryAxisElevationRelativeSet(msg: JonSharedCmdRotary.RotateElevationRelativeSet): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["direction"] =
            msg
                .getDirection()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from HaltElevation
     */
    private fun extractRotaryAxisElevationHalt(msg: JonSharedCmdRotary.HaltElevation): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanUnpause
     */
    private fun extractRotaryScanUnpause(msg: JonSharedCmdRotary.ScanUnpause): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetMode
     */
    private fun extractRotarySetMode(msg: JonSharedCmdRotary.SetMode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["mode"] =
            msg
                .getMode()
                .name
                .lowercase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from ScanRefreshNodeList
     */
    private fun extractRotaryScanRefreshNodeList(msg: JonSharedCmdRotary.ScanRefreshNodeList): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanUpdateNode
     */
    private fun extractRotaryScanUpdateNode(msg: JonSharedCmdRotary.ScanUpdateNode): Map<String, Any?> {
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
     * Extract Transit data from Root
     */
    private fun extractCompass(msg: JonSharedCmdCompass.Root): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasCalibrateCencel()) {
            result["calibrate-cencel"] = extractCompassCalibrateCencel(msg.getCalibrateCencel())
        }
        if (msg.hasStart()) {
            result["start"] = extractCompassStart(msg.getStart())
        }
        if (msg.hasSetOffsetAngleElevation()) {
            result["set-offset-angle-elevation"] = extractCompassSetOffsetAngleElevation(msg.getSetOffsetAngleElevation())
        }
        if (msg.hasStop()) {
            result["stop"] = extractCompassStop(msg.getStop())
        }
        if (msg.hasCalibrateNext()) {
            result["calibrate-next"] = extractCompassCalibrateNext(msg.getCalibrateNext())
        }
        if (msg.hasGetMeteo()) {
            result["get-meteo"] = extractCompassGetMeteo(msg.getGetMeteo())
        }
        if (msg.hasSetUseRotaryPosition()) {
            result["set-use-rotary-position"] = extractCompassSetUseRotaryPosition(msg.getSetUseRotaryPosition())
        }
        if (msg.hasSetMagneticDeclination()) {
            result["set-magnetic-declination"] = extractCompassSetMagneticDeclination(msg.getSetMagneticDeclination())
        }
        if (msg.hasStartCalibrateShort()) {
            result["start-calibrate-short"] = extractCompassStartCalibrateShort(msg.getStartCalibrateShort())
        }
        if (msg.hasStartCalibrateLong()) {
            result["start-calibrate-long"] = extractCompassStartCalibrateLong(msg.getStartCalibrateLong())
        }
        if (msg.hasSetOffsetAngleAzimuth()) {
            result["set-offset-angle-azimuth"] = extractCompassSetOffsetAngleAzimuth(msg.getSetOffsetAngleAzimuth())
        }

        return result
    }

    /**
     * Extract Transit data from CalibrateCencel
     */
    private fun extractCompassCalibrateCencel(msg: JonSharedCmdCompass.CalibrateCencel): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractCompassStart(msg: JonSharedCmdCompass.Start): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetOffsetAngleElevation
     */
    private fun extractCompassSetOffsetAngleElevation(msg: JonSharedCmdCompass.SetOffsetAngleElevation): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractCompassStop(msg: JonSharedCmdCompass.Stop): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from CalibrateNext
     */
    private fun extractCompassCalibrateNext(msg: JonSharedCmdCompass.CalibrateNext): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractCompassGetMeteo(msg: JonSharedCmdCompass.GetMeteo): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetUseRotaryPosition
     */
    private fun extractCompassSetUseRotaryPosition(msg: JonSharedCmdCompass.SetUseRotaryPosition): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["flag"] = msg.getFlag()

        return result
    }

    /**
     * Extract Transit data from SetMagneticDeclination
     */
    private fun extractCompassSetMagneticDeclination(msg: JonSharedCmdCompass.SetMagneticDeclination): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from CalibrateStartShort
     */
    private fun extractCompassStartCalibrateShort(msg: JonSharedCmdCompass.CalibrateStartShort): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from CalibrateStartLong
     */
    private fun extractCompassStartCalibrateLong(msg: JonSharedCmdCompass.CalibrateStartLong): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetOffsetAngleAzimuth
     */
    private fun extractCompassSetOffsetAngleAzimuth(msg: JonSharedCmdCompass.SetOffsetAngleAzimuth): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Frozen
     */
    private fun extractFrozen(msg: JonSharedCmd.Frozen): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    // Utility conversion functions
    private fun keyToString(key: Any?): String =
        when (key) {
            is String -> key
            is com.cognitect.transit.Keyword -> key.name
            null -> "null"
            else -> key.toString()
        }

    private fun convertInt(value: Any): Int =
        when (value) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.toInt()
            else -> throw IllegalArgumentException("Cannot convert to int: \$value")
        }

    private fun convertLong(value: Any): Long =
        when (value) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLong()
            else -> throw IllegalArgumentException("Cannot convert to long: \$value")
        }

    private fun convertFloat(value: Any): Float =
        when (value) {
            is Float -> value
            is Number -> value.toFloat()
            is String -> value.toFloat()
            else -> throw IllegalArgumentException("Cannot convert to float: \$value")
        }

    private fun convertDouble(value: Any): Double =
        when (value) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.toDouble()
            else -> throw IllegalArgumentException("Cannot convert to double: \$value")
        }

    private fun convertBoolean(value: Any): Boolean =
        when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> throw IllegalArgumentException("Cannot convert to boolean: \$value")
        }

    private fun convertString(value: Any): String = value.toString()
}

/**
 * Transit read handler for command messages
 */
class GeneratedCommandReadHandler : ReadHandler<Message, Map<*, *>> {
    override fun fromRep(rep: Map<*, *>): Message = GeneratedCommandHandlers.buildCommand(rep)
}

/**
 * Transit write handler for command messages
 */
class GeneratedCommandWriteHandler : WriteHandler<JonSharedCmd.Root, Any> {
    override fun tag(o: JonSharedCmd.Root): String = "cmd"

    override fun rep(o: JonSharedCmd.Root): Any = GeneratedCommandHandlers.extractCommand(o)

    override fun stringRep(o: JonSharedCmd.Root): String? = null

    override fun <V : Any> getVerboseHandler(): WriteHandler<JonSharedCmd.Root, V>? = null
}
