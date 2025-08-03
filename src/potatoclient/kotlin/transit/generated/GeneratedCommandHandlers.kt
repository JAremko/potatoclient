package potatoclient.kotlin.transit.generated

import clojure.lang.Keyword
import cmd.*
import cmd.CV.*
import cmd.Compass.*
import cmd.DayCamGlassHeater.*
import cmd.DayCamera.*
import cmd.Frozen.*
import cmd.Gps.*
import cmd.HeatCamera.*
import cmd.Lira.*
import cmd.Lrf.*
import cmd.OSD.*
import cmd.RotaryPlatform.*
import cmd.System.*
import com.cognitect.transit.ReadHandler
import com.cognitect.transit.WriteHandler
import com.google.protobuf.Message
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Generated Transit handlers for command messages.
 *
 * This file is auto-generated from protobuf definitions.
 * DO NOT EDIT - regenerate with: bb generate-kotlin-handlers.clj
 *
 * Generated on: Sun Aug 03 16:05:33 CEST 2025
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

            when (key.toString()) {
                "osd" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setOsd(buildOsd(value as Map<*, *>))
                            .build()
                "ping" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setPing(buildPing(value as Map<*, *>))
                            .build()
                "system" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setSystem(buildSystem(value as Map<*, *>))
                            .build()
                "noop" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setNoop(buildNoop(value as Map<*, *>))
                            .build()
                "cv" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setCv(buildCv(value as Map<*, *>))
                            .build()
                "gps" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setGps(buildGps(value as Map<*, *>))
                            .build()
                "lrf" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setLrf(buildLrf(value as Map<*, *>))
                            .build()
                "day-cam-glass-heater" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setDayCamGlassHeater(buildDayCamGlassHeater(value as Map<*, *>))
                            .build()
                "day-camera" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setDayCamera(buildDayCamera(value as Map<*, *>))
                            .build()
                "heat-camera" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setHeatCamera(buildHeatCamera(value as Map<*, *>))
                            .build()
                "lira" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setLira(buildLira(value as Map<*, *>))
                            .build()
                "lrf-calib" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setLrfCalib(buildLrfCalib(value as Map<*, *>))
                            .build()
                "rotary" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setRotary(buildRotary(value as Map<*, *>))
                            .build()
                "compass" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setCompass(buildCompass(value as Map<*, *>))
                            .build()
                "frozen" ->
                    builder.cmd =
                        JonSharedCmd.Cmd
                            .newBuilder()
                            .setFrozen(buildFrozen(value as Map<*, *>))
                            .build()
                else -> logger.warn { "Unknown command type: \$key" }
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

        when (root.cmd.cmdCase) {
            JonSharedCmd.Cmd.CmdCase.OSD ->
                result["osd"] = extractOsd(root.cmd.osd)
            JonSharedCmd.Cmd.CmdCase.PING ->
                result["ping"] = extractPing(root.cmd.ping)
            JonSharedCmd.Cmd.CmdCase.SYSTEM ->
                result["system"] = extractSystem(root.cmd.system)
            JonSharedCmd.Cmd.CmdCase.NOOP ->
                result["noop"] = extractNoop(root.cmd.noop)
            JonSharedCmd.Cmd.CmdCase.CV ->
                result["cv"] = extractCv(root.cmd.cv)
            JonSharedCmd.Cmd.CmdCase.GPS ->
                result["gps"] = extractGps(root.cmd.gps)
            JonSharedCmd.Cmd.CmdCase.LRF ->
                result["lrf"] = extractLrf(root.cmd.lrf)
            JonSharedCmd.Cmd.CmdCase.DAY_CAM_GLASS_HEATER ->
                result["day-cam-glass-heater"] = extractDayCamGlassHeater(root.cmd.dayCamGlassHeater)
            JonSharedCmd.Cmd.CmdCase.DAY_CAMERA ->
                result["day-camera"] = extractDayCamera(root.cmd.dayCamera)
            JonSharedCmd.Cmd.CmdCase.HEAT_CAMERA ->
                result["heat-camera"] = extractHeatCamera(root.cmd.heatCamera)
            JonSharedCmd.Cmd.CmdCase.LIRA ->
                result["lira"] = extractLira(root.cmd.lira)
            JonSharedCmd.Cmd.CmdCase.LRF_CALIB ->
                result["lrf-calib"] = extractLrfCalib(root.cmd.lrfCalib)
            JonSharedCmd.Cmd.CmdCase.ROTARY ->
                result["rotary"] = extractRotary(root.cmd.rotary)
            JonSharedCmd.Cmd.CmdCase.COMPASS ->
                result["compass"] = extractCompass(root.cmd.compass)
            JonSharedCmd.Cmd.CmdCase.FROZEN ->
                result["frozen"] = extractFrozen(root.cmd.frozen)
            JonSharedCmd.Cmd.CmdCase.CMD_NOT_SET ->
                logger.warn { "Command not set" }
            else ->
                logger.warn { "Unknown command case: \${root.cmd.cmdCase}" }
        }

        return result
    }

    /**
     * Build Root from Transit data
     */
    private fun buildOsd(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$Root` {
        val builder = `cmd.OSD.JonSharedCmdOsd$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "show-default-screen" -> builder.setShowDefaultScreen(buildShowDefaultScreen(value as Map<*, *>))
                "show-lrf-measure-screen" -> builder.setShowLrfMeasureScreen(buildShowLrfMeasureScreen(value as Map<*, *>))
                "show-lrf-result-screen" -> builder.setShowLrfResultScreen(buildShowLrfResultScreen(value as Map<*, *>))
                "show-lrf-result-simplified-screen" ->
                    builder.setShowLrfResultSimplifiedScreen(
                        buildShowLrfResultSimplifiedScreen(value as Map<*, *>),
                    )
                "enable-heat-osd" -> builder.setEnableHeatOsd(buildEnableHeatOsd(value as Map<*, *>))
                "disable-heat-osd" -> builder.setDisableHeatOsd(buildDisableHeatOsd(value as Map<*, *>))
                "enable-day-osd" -> builder.setEnableDayOsd(buildEnableDayOsd(value as Map<*, *>))
                "disable-day-osd" -> builder.setDisableDayOsd(buildDisableDayOsd(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShowDefaultScreen from Transit data
     */
    private fun buildShowDefaultScreen(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen` {
        val builder = `cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ShowDefaultScreen" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShowLRFMeasureScreen from Transit data
     */
    private fun buildShowLrfMeasureScreen(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen` {
        val builder = `cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ShowLRFMeasureScreen" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShowLRFResultScreen from Transit data
     */
    private fun buildShowLrfResultScreen(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen` {
        val builder = `cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ShowLRFResultScreen" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShowLRFResultSimplifiedScreen from Transit data
     */
    private fun buildShowLrfResultSimplifiedScreen(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen` {
        val builder = `cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ShowLRFResultSimplifiedScreen" }
            }
        }

        return builder.build()
    }

    /**
     * Build EnableHeatOSD from Transit data
     */
    private fun buildEnableHeatOsd(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$EnableHeatOSD` {
        val builder = `cmd.OSD.JonSharedCmdOsd$EnableHeatOSD`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for EnableHeatOSD" }
            }
        }

        return builder.build()
    }

    /**
     * Build DisableHeatOSD from Transit data
     */
    private fun buildDisableHeatOsd(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$DisableHeatOSD` {
        val builder = `cmd.OSD.JonSharedCmdOsd$DisableHeatOSD`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for DisableHeatOSD" }
            }
        }

        return builder.build()
    }

    /**
     * Build EnableDayOSD from Transit data
     */
    private fun buildEnableDayOsd(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$EnableDayOSD` {
        val builder = `cmd.OSD.JonSharedCmdOsd$EnableDayOSD`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for EnableDayOSD" }
            }
        }

        return builder.build()
    }

    /**
     * Build DisableDayOSD from Transit data
     */
    private fun buildDisableDayOsd(data: Map<*, *>): `cmd.OSD.JonSharedCmdOsd$DisableDayOSD` {
        val builder = `cmd.OSD.JonSharedCmdOsd$DisableDayOSD`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for DisableDayOSD" }
            }
        }

        return builder.build()
    }

    /**
     * Build Ping from Transit data
     */
    private fun buildPing(data: Map<*, *>): `cmd.JonSharedCmd$Ping` {
        val builder = `cmd.JonSharedCmd$Ping`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Ping" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildSystem(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$Root` {
        val builder = `cmd.System.JonSharedCmdSystem$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "geodesic-mode-disable" -> builder.setGeodesicModeDisable(buildGeodesicModeDisable(value as Map<*, *>))
                "start-all" -> builder.setStartAll(buildStartAll(value as Map<*, *>))
                "geodesic-mode-enable" -> builder.setGeodesicModeEnable(buildGeodesicModeEnable(value as Map<*, *>))
                "localization" -> builder.setLocalization(buildLocalization(value as Map<*, *>))
                "unmark-rec-important" -> builder.setUnmarkRecImportant(buildUnmarkRecImportant(value as Map<*, *>))
                "stop-rec" -> builder.setStopRec(buildStopRec(value as Map<*, *>))
                "reboot" -> builder.setReboot(buildReboot(value as Map<*, *>))
                "start-rec" -> builder.setStartRec(buildStartRec(value as Map<*, *>))
                "power-off" -> builder.setPowerOff(buildPowerOff(value as Map<*, *>))
                "reset-configs" -> builder.setResetConfigs(buildResetConfigs(value as Map<*, *>))
                "stop-all" -> builder.setStopAll(buildStopAll(value as Map<*, *>))
                "enter-transport" -> builder.setEnterTransport(buildEnterTransport(value as Map<*, *>))
                "mark-rec-important" -> builder.setMarkRecImportant(buildMarkRecImportant(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build DisableGeodesicMode from Transit data
     */
    private fun buildGeodesicModeDisable(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$DisableGeodesicMode` {
        val builder = `cmd.System.JonSharedCmdSystem$DisableGeodesicMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for DisableGeodesicMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build StartALl from Transit data
     */
    private fun buildStartAll(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$StartALl` {
        val builder = `cmd.System.JonSharedCmdSystem$StartALl`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for StartALl" }
            }
        }

        return builder.build()
    }

    /**
     * Build EnableGeodesicMode from Transit data
     */
    private fun buildGeodesicModeEnable(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$EnableGeodesicMode` {
        val builder = `cmd.System.JonSharedCmdSystem$EnableGeodesicMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for EnableGeodesicMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetLocalization from Transit data
     */
    private fun buildLocalization(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$SetLocalization` {
        val builder = `cmd.System.JonSharedCmdSystem$SetLocalization`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "loc" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setLoc(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for SetLocalization" }
            }
        }

        return builder.build()
    }

    /**
     * Build UnmarkRecImportant from Transit data
     */
    private fun buildUnmarkRecImportant(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$UnmarkRecImportant` {
        val builder = `cmd.System.JonSharedCmdSystem$UnmarkRecImportant`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for UnmarkRecImportant" }
            }
        }

        return builder.build()
    }

    /**
     * Build StopRec from Transit data
     */
    private fun buildStopRec(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$StopRec` {
        val builder = `cmd.System.JonSharedCmdSystem$StopRec`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for StopRec" }
            }
        }

        return builder.build()
    }

    /**
     * Build Reboot from Transit data
     */
    private fun buildReboot(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$Reboot` {
        val builder = `cmd.System.JonSharedCmdSystem$Reboot`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Reboot" }
            }
        }

        return builder.build()
    }

    /**
     * Build StartRec from Transit data
     */
    private fun buildStartRec(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$StartRec` {
        val builder = `cmd.System.JonSharedCmdSystem$StartRec`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for StartRec" }
            }
        }

        return builder.build()
    }

    /**
     * Build PowerOff from Transit data
     */
    private fun buildPowerOff(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$PowerOff` {
        val builder = `cmd.System.JonSharedCmdSystem$PowerOff`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for PowerOff" }
            }
        }

        return builder.build()
    }

    /**
     * Build ResetConfigs from Transit data
     */
    private fun buildResetConfigs(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$ResetConfigs` {
        val builder = `cmd.System.JonSharedCmdSystem$ResetConfigs`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ResetConfigs" }
            }
        }

        return builder.build()
    }

    /**
     * Build StopALl from Transit data
     */
    private fun buildStopAll(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$StopALl` {
        val builder = `cmd.System.JonSharedCmdSystem$StopALl`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for StopALl" }
            }
        }

        return builder.build()
    }

    /**
     * Build EnterTransport from Transit data
     */
    private fun buildEnterTransport(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$EnterTransport` {
        val builder = `cmd.System.JonSharedCmdSystem$EnterTransport`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for EnterTransport" }
            }
        }

        return builder.build()
    }

    /**
     * Build MarkRecImportant from Transit data
     */
    private fun buildMarkRecImportant(data: Map<*, *>): `cmd.System.JonSharedCmdSystem$MarkRecImportant` {
        val builder = `cmd.System.JonSharedCmdSystem$MarkRecImportant`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for MarkRecImportant" }
            }
        }

        return builder.build()
    }

    /**
     * Build Noop from Transit data
     */
    private fun buildNoop(data: Map<*, *>): `cmd.JonSharedCmd$Noop` {
        val builder = `cmd.JonSharedCmd$Noop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Noop" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildCv(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$Root` {
        val builder = `cmd.CV.JonSharedCmdCv$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "vampire-mode-enable" -> builder.setVampireModeEnable(buildVampireModeEnable(value as Map<*, *>))
                "vampire-mode-disable" -> builder.setVampireModeDisable(buildVampireModeDisable(value as Map<*, *>))
                "dump-stop" -> builder.setDumpStop(buildDumpStop(value as Map<*, *>))
                "stabilization-mode-disable" -> builder.setStabilizationModeDisable(buildStabilizationModeDisable(value as Map<*, *>))
                "set-auto-focus" -> builder.setSetAutoFocus(buildSetAutoFocus(value as Map<*, *>))
                "start-track-ndc" -> builder.setStartTrackNdc(buildStartTrackNdc(value as Map<*, *>))
                "dump-start" -> builder.setDumpStart(buildDumpStart(value as Map<*, *>))
                "stop-track" -> builder.setStopTrack(buildStopTrack(value as Map<*, *>))
                "stabilization-mode-enable" -> builder.setStabilizationModeEnable(buildStabilizationModeEnable(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build VampireModeEnable from Transit data
     */
    private fun buildVampireModeEnable(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$VampireModeEnable` {
        val builder = `cmd.CV.JonSharedCmdCv$VampireModeEnable`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for VampireModeEnable" }
            }
        }

        return builder.build()
    }

    /**
     * Build VampireModeDisable from Transit data
     */
    private fun buildVampireModeDisable(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$VampireModeDisable` {
        val builder = `cmd.CV.JonSharedCmdCv$VampireModeDisable`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for VampireModeDisable" }
            }
        }

        return builder.build()
    }

    /**
     * Build DumpStop from Transit data
     */
    private fun buildDumpStop(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$DumpStop` {
        val builder = `cmd.CV.JonSharedCmdCv$DumpStop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for DumpStop" }
            }
        }

        return builder.build()
    }

    /**
     * Build StabilizationModeDisable from Transit data
     */
    private fun buildStabilizationModeDisable(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$StabilizationModeDisable` {
        val builder = `cmd.CV.JonSharedCmdCv$StabilizationModeDisable`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for StabilizationModeDisable" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetAutoFocus from Transit data
     */
    private fun buildSetAutoFocus(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$SetAutoFocus` {
        val builder = `cmd.CV.JonSharedCmdCv$SetAutoFocus`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "channel" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setChannel(enumValue)
                }
                "value" -> builder.setValue(convertBoolean(value))
                else -> logger.warn { "Unknown field \$key for SetAutoFocus" }
            }
        }

        return builder.build()
    }

    /**
     * Build StartTrackNDC from Transit data
     */
    private fun buildStartTrackNdc(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$StartTrackNDC` {
        val builder = `cmd.CV.JonSharedCmdCv$StartTrackNDC`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "channel" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setChannel(enumValue)
                }
                "x" -> builder.setX(convertFloat(value))
                "y" -> builder.setY(convertFloat(value))
                "frame-time" -> builder.setFrameTime(convertLong(value))
                else -> logger.warn { "Unknown field \$key for StartTrackNDC" }
            }
        }

        return builder.build()
    }

    /**
     * Build DumpStart from Transit data
     */
    private fun buildDumpStart(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$DumpStart` {
        val builder = `cmd.CV.JonSharedCmdCv$DumpStart`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for DumpStart" }
            }
        }

        return builder.build()
    }

    /**
     * Build StopTrack from Transit data
     */
    private fun buildStopTrack(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$StopTrack` {
        val builder = `cmd.CV.JonSharedCmdCv$StopTrack`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for StopTrack" }
            }
        }

        return builder.build()
    }

    /**
     * Build StabilizationModeEnable from Transit data
     */
    private fun buildStabilizationModeEnable(data: Map<*, *>): `cmd.CV.JonSharedCmdCv$StabilizationModeEnable` {
        val builder = `cmd.CV.JonSharedCmdCv$StabilizationModeEnable`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for StabilizationModeEnable" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildGps(data: Map<*, *>): `cmd.Gps.JonSharedCmdGps$Root` {
        val builder = `cmd.Gps.JonSharedCmdGps$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "start" -> builder.setStart(buildStart(value as Map<*, *>))
                "stop" -> builder.setStop(buildStop(value as Map<*, *>))
                "set-manual-position" -> builder.setSetManualPosition(buildSetManualPosition(value as Map<*, *>))
                "set-use-manual-position" -> builder.setSetUseManualPosition(buildSetUseManualPosition(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildGetMeteo(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildStart(data: Map<*, *>): `cmd.Gps.JonSharedCmdGps$Start` {
        val builder = `cmd.Gps.JonSharedCmdGps$Start`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Start" }
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildStop(data: Map<*, *>): `cmd.Gps.JonSharedCmdGps$Stop` {
        val builder = `cmd.Gps.JonSharedCmdGps$Stop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Stop" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetManualPosition from Transit data
     */
    private fun buildSetManualPosition(data: Map<*, *>): `cmd.Gps.JonSharedCmdGps$SetManualPosition` {
        val builder = `cmd.Gps.JonSharedCmdGps$SetManualPosition`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "latitude" -> builder.setLatitude(convertFloat(value))
                "longitude" -> builder.setLongitude(convertFloat(value))
                "altitude" -> builder.setAltitude(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetManualPosition" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetUseManualPosition from Transit data
     */
    private fun buildSetUseManualPosition(data: Map<*, *>): `cmd.Gps.JonSharedCmdGps$SetUseManualPosition` {
        val builder = `cmd.Gps.JonSharedCmdGps$SetUseManualPosition`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "flag" -> builder.setFlag(convertBoolean(value))
                else -> logger.warn { "Unknown field \$key for SetUseManualPosition" }
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildGetMeteo(data: Map<*, *>): `cmd.Gps.JonSharedCmdGps$GetMeteo` {
        val builder = `cmd.Gps.JonSharedCmdGps$GetMeteo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for GetMeteo" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildLrf(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$Root` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "target-designator-off" -> builder.setTargetDesignatorOff(buildTargetDesignatorOff(value as Map<*, *>))
                "target-designator-on-mode-b" -> builder.setTargetDesignatorOnModeB(buildTargetDesignatorOnModeB(value as Map<*, *>))
                "disable-fog-mode" -> builder.setDisableFogMode(buildDisableFogMode(value as Map<*, *>))
                "set-scan-mode" -> builder.setSetScanMode(buildSetScanMode(value as Map<*, *>))
                "refine-off" -> builder.setRefineOff(buildRefineOff(value as Map<*, *>))
                "scan-off" -> builder.setScanOff(buildScanOff(value as Map<*, *>))
                "refine-on" -> builder.setRefineOn(buildRefineOn(value as Map<*, *>))
                "start" -> builder.setStart(buildStart(value as Map<*, *>))
                "measure" -> builder.setMeasure(buildMeasure(value as Map<*, *>))
                "scan-on" -> builder.setScanOn(buildScanOn(value as Map<*, *>))
                "stop" -> builder.setStop(buildStop(value as Map<*, *>))
                "new-session" -> builder.setNewSession(buildNewSession(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildGetMeteo(value as Map<*, *>))
                "enable-fog-mode" -> builder.setEnableFogMode(buildEnableFogMode(value as Map<*, *>))
                "target-designator-on-mode-a" -> builder.setTargetDesignatorOnModeA(buildTargetDesignatorOnModeA(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build TargetDesignatorOff from Transit data
     */
    private fun buildTargetDesignatorOff(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for TargetDesignatorOff" }
            }
        }

        return builder.build()
    }

    /**
     * Build TargetDesignatorOnModeB from Transit data
     */
    private fun buildTargetDesignatorOnModeB(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for TargetDesignatorOnModeB" }
            }
        }

        return builder.build()
    }

    /**
     * Build DisableFogMode from Transit data
     */
    private fun buildDisableFogMode(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$DisableFogMode` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$DisableFogMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for DisableFogMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetScanMode from Transit data
     */
    private fun buildSetScanMode(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$SetScanMode` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$SetScanMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "mode" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setMode(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for SetScanMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build RefineOff from Transit data
     */
    private fun buildRefineOff(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$RefineOff` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$RefineOff`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for RefineOff" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanOff from Transit data
     */
    private fun buildScanOff(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$ScanOff` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$ScanOff`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanOff" }
            }
        }

        return builder.build()
    }

    /**
     * Build RefineOn from Transit data
     */
    private fun buildRefineOn(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$RefineOn` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$RefineOn`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for RefineOn" }
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildStart(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$Start` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$Start`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Start" }
            }
        }

        return builder.build()
    }

    /**
     * Build Measure from Transit data
     */
    private fun buildMeasure(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$Measure` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$Measure`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Measure" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanOn from Transit data
     */
    private fun buildScanOn(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$ScanOn` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$ScanOn`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanOn" }
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildStop(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$Stop` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$Stop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Stop" }
            }
        }

        return builder.build()
    }

    /**
     * Build NewSession from Transit data
     */
    private fun buildNewSession(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$NewSession` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$NewSession`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for NewSession" }
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildGetMeteo(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$GetMeteo` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$GetMeteo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for GetMeteo" }
            }
        }

        return builder.build()
    }

    /**
     * Build EnableFogMode from Transit data
     */
    private fun buildEnableFogMode(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$EnableFogMode` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$EnableFogMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for EnableFogMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build TargetDesignatorOnModeA from Transit data
     */
    private fun buildTargetDesignatorOnModeA(data: Map<*, *>): `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA` {
        val builder = `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for TargetDesignatorOnModeA" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildDayCamGlassHeater(data: Map<*, *>): `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root` {
        val builder = `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "start" -> builder.setStart(buildStart(value as Map<*, *>))
                "stop" -> builder.setStop(buildStop(value as Map<*, *>))
                "turn-on" -> builder.setTurnOn(buildTurnOn(value as Map<*, *>))
                "turn-off" -> builder.setTurnOff(buildTurnOff(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildGetMeteo(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildStart(data: Map<*, *>): `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start` {
        val builder = `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Start" }
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildStop(data: Map<*, *>): `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop` {
        val builder = `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Stop" }
            }
        }

        return builder.build()
    }

    /**
     * Build TurnOn from Transit data
     */
    private fun buildTurnOn(data: Map<*, *>): `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn` {
        val builder = `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for TurnOn" }
            }
        }

        return builder.build()
    }

    /**
     * Build TurnOff from Transit data
     */
    private fun buildTurnOff(data: Map<*, *>): `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff` {
        val builder = `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for TurnOff" }
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildGetMeteo(data: Map<*, *>): `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo` {
        val builder = `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for GetMeteo" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildDayCamera(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Root` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "zoom" -> builder.setZoom(buildZoom(value as Map<*, *>))
                "set-infra-red-filter" -> builder.setSetInfraRedFilter(buildSetInfraRedFilter(value as Map<*, *>))
                "set-clahe-level" -> builder.setSetClaheLevel(buildSetClaheLevel(value as Map<*, *>))
                "prev-fx-mode" -> builder.setPrevFxMode(buildPrevFxMode(value as Map<*, *>))
                "start" -> builder.setStart(buildStart(value as Map<*, *>))
                "halt-all" -> builder.setHaltAll(buildHaltAll(value as Map<*, *>))
                "set-digital-zoom-level" -> builder.setSetDigitalZoomLevel(buildSetDigitalZoomLevel(value as Map<*, *>))
                "stop" -> builder.setStop(buildStop(value as Map<*, *>))
                "photo" -> builder.setPhoto(buildPhoto(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildGetMeteo(value as Map<*, *>))
                "focus" -> builder.setFocus(buildFocus(value as Map<*, *>))
                "set-fx-mode" -> builder.setSetFxMode(buildSetFxMode(value as Map<*, *>))
                "set-iris" -> builder.setSetIris(buildSetIris(value as Map<*, *>))
                "refresh-fx-mode" -> builder.setRefreshFxMode(buildRefreshFxMode(value as Map<*, *>))
                "set-auto-iris" -> builder.setSetAutoIris(buildSetAutoIris(value as Map<*, *>))
                "next-fx-mode" -> builder.setNextFxMode(buildNextFxMode(value as Map<*, *>))
                "shift-clahe-level" -> builder.setShiftClaheLevel(buildShiftClaheLevel(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build Zoom from Transit data
     */
    private fun buildZoom(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Zoom` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Zoom`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "prev-zoom-table-pos" -> builder.setPrevZoomTablePos(buildPrevZoomTablePos(value as Map<*, *>))
                "offset" -> builder.setOffset(buildOffset(value as Map<*, *>))
                "move" -> builder.setMove(buildMove(value as Map<*, *>))
                "reset-zoom" -> builder.setResetZoom(buildResetZoom(value as Map<*, *>))
                "next-zoom-table-pos" -> builder.setNextZoomTablePos(buildNextZoomTablePos(value as Map<*, *>))
                "set-value" -> builder.setSetValue(buildSetValue(value as Map<*, *>))
                "set-zoom-table-value" -> builder.setSetZoomTableValue(buildSetZoomTableValue(value as Map<*, *>))
                "halt" -> builder.setHalt(buildHalt(value as Map<*, *>))
                "save-to-table" -> builder.setSaveToTable(buildSaveToTable(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Zoom" }
            }
        }

        return builder.build()
    }

    /**
     * Build PrevZoomTablePos from Transit data
     */
    private fun buildPrevZoomTablePos(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for PrevZoomTablePos" }
            }
        }

        return builder.build()
    }

    /**
     * Build Offset from Transit data
     */
    private fun buildOffset(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Offset` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Offset`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "offset-value" -> builder.setOffsetValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for Offset" }
            }
        }

        return builder.build()
    }

    /**
     * Build Move from Transit data
     */
    private fun buildMove(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Move` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Move`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "target-value" -> builder.setTargetValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for Move" }
            }
        }

        return builder.build()
    }

    /**
     * Build ResetZoom from Transit data
     */
    private fun buildResetZoom(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ResetZoom" }
            }
        }

        return builder.build()
    }

    /**
     * Build NextZoomTablePos from Transit data
     */
    private fun buildNextZoomTablePos(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for NextZoomTablePos" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetValue from Transit data
     */
    private fun buildSetValue(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetValue` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetValue`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetValue" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetZoomTableValue from Transit data
     */
    private fun buildSetZoomTableValue(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertInt(value))
                else -> logger.warn { "Unknown field \$key for SetZoomTableValue" }
            }
        }

        return builder.build()
    }

    /**
     * Build Halt from Transit data
     */
    private fun buildHalt(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Halt` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Halt`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Halt" }
            }
        }

        return builder.build()
    }

    /**
     * Build SaveToTable from Transit data
     */
    private fun buildSaveToTable(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for SaveToTable" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetInfraRedFilter from Transit data
     */
    private fun buildSetInfraRedFilter(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertBoolean(value))
                else -> logger.warn { "Unknown field \$key for SetInfraRedFilter" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetClaheLevel from Transit data
     */
    private fun buildSetClaheLevel(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetClaheLevel" }
            }
        }

        return builder.build()
    }

    /**
     * Build PrevFxMode from Transit data
     */
    private fun buildPrevFxMode(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for PrevFxMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildStart(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Start` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Start`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Start" }
            }
        }

        return builder.build()
    }

    /**
     * Build HaltAll from Transit data
     */
    private fun buildHaltAll(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$HaltAll` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$HaltAll`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for HaltAll" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetDigitalZoomLevel from Transit data
     */
    private fun buildSetDigitalZoomLevel(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetDigitalZoomLevel" }
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildStop(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Stop` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Stop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Stop" }
            }
        }

        return builder.build()
    }

    /**
     * Build Photo from Transit data
     */
    private fun buildPhoto(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Photo` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Photo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Photo" }
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildGetMeteo(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for GetMeteo" }
            }
        }

        return builder.build()
    }

    /**
     * Build Focus from Transit data
     */
    private fun buildFocus(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Focus` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Focus`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "set-value" -> builder.setSetValue(buildSetValue(value as Map<*, *>))
                "move" -> builder.setMove(buildMove(value as Map<*, *>))
                "halt" -> builder.setHalt(buildHalt(value as Map<*, *>))
                "offset" -> builder.setOffset(buildOffset(value as Map<*, *>))
                "reset-focus" -> builder.setResetFocus(buildResetFocus(value as Map<*, *>))
                "save-to-table-focus" -> builder.setSaveToTableFocus(buildSaveToTableFocus(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Focus" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetValue from Transit data
     */
    private fun buildSetValue(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetValue` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetValue`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetValue" }
            }
        }

        return builder.build()
    }

    /**
     * Build Move from Transit data
     */
    private fun buildMove(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Move` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Move`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "target-value" -> builder.setTargetValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for Move" }
            }
        }

        return builder.build()
    }

    /**
     * Build Halt from Transit data
     */
    private fun buildHalt(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Halt` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Halt`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Halt" }
            }
        }

        return builder.build()
    }

    /**
     * Build Offset from Transit data
     */
    private fun buildOffset(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$Offset` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$Offset`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "offset-value" -> builder.setOffsetValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for Offset" }
            }
        }

        return builder.build()
    }

    /**
     * Build ResetFocus from Transit data
     */
    private fun buildResetFocus(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ResetFocus" }
            }
        }

        return builder.build()
    }

    /**
     * Build SaveToTableFocus from Transit data
     */
    private fun buildSaveToTableFocus(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for SaveToTableFocus" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetFxMode from Transit data
     */
    private fun buildSetFxMode(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "mode" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setMode(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for SetFxMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetIris from Transit data
     */
    private fun buildSetIris(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetIris` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetIris`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetIris" }
            }
        }

        return builder.build()
    }

    /**
     * Build RefreshFxMode from Transit data
     */
    private fun buildRefreshFxMode(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for RefreshFxMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetAutoIris from Transit data
     */
    private fun buildSetAutoIris(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertBoolean(value))
                else -> logger.warn { "Unknown field \$key for SetAutoIris" }
            }
        }

        return builder.build()
    }

    /**
     * Build NextFxMode from Transit data
     */
    private fun buildNextFxMode(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for NextFxMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftClaheLevel from Transit data
     */
    private fun buildShiftClaheLevel(data: Map<*, *>): `cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel` {
        val builder = `cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for ShiftClaheLevel" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildHeatCamera(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$Root` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "set-dde-level" -> builder.setSetDdeLevel(buildSetDdeLevel(value as Map<*, *>))
                "set-calib-mode" -> builder.setSetCalibMode(buildSetCalibMode(value as Map<*, *>))
                "zoom" -> builder.setZoom(buildZoom(value as Map<*, *>))
                "set-agc" -> builder.setSetAgc(buildSetAgc(value as Map<*, *>))
                "shift-dde" -> builder.setShiftDde(buildShiftDde(value as Map<*, *>))
                "set-filter" -> builder.setSetFilter(buildSetFilter(value as Map<*, *>))
                "set-clahe-level" -> builder.setSetClaheLevel(buildSetClaheLevel(value as Map<*, *>))
                "disable-dde" -> builder.setDisableDde(buildDisableDde(value as Map<*, *>))
                "prev-fx-mode" -> builder.setPrevFxMode(buildPrevFxMode(value as Map<*, *>))
                "start" -> builder.setStart(buildStart(value as Map<*, *>))
                "focus-step-minus" -> builder.setFocusStepMinus(buildFocusStepMinus(value as Map<*, *>))
                "set-digital-zoom-level" -> builder.setSetDigitalZoomLevel(buildSetDigitalZoomLevel(value as Map<*, *>))
                "enable-dde" -> builder.setEnableDde(buildEnableDde(value as Map<*, *>))
                "focus-stop" -> builder.setFocusStop(buildFocusStop(value as Map<*, *>))
                "stop" -> builder.setStop(buildStop(value as Map<*, *>))
                "reset-zoom" -> builder.setResetZoom(buildResetZoom(value as Map<*, *>))
                "zoom-out" -> builder.setZoomOut(buildZoomOut(value as Map<*, *>))
                "photo" -> builder.setPhoto(buildPhoto(value as Map<*, *>))
                "zoom-in" -> builder.setZoomIn(buildZoomIn(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildGetMeteo(value as Map<*, *>))
                "focus-step-plus" -> builder.setFocusStepPlus(buildFocusStepPlus(value as Map<*, *>))
                "set-fx-mode" -> builder.setSetFxMode(buildSetFxMode(value as Map<*, *>))
                "refresh-fx-mode" -> builder.setRefreshFxMode(buildRefreshFxMode(value as Map<*, *>))
                "focus-out" -> builder.setFocusOut(buildFocusOut(value as Map<*, *>))
                "set-auto-focus" -> builder.setSetAutoFocus(buildSetAutoFocus(value as Map<*, *>))
                "zoom-stop" -> builder.setZoomStop(buildZoomStop(value as Map<*, *>))
                "save-to-table" -> builder.setSaveToTable(buildSaveToTable(value as Map<*, *>))
                "next-fx-mode" -> builder.setNextFxMode(buildNextFxMode(value as Map<*, *>))
                "calibrate" -> builder.setCalibrate(buildCalibrate(value as Map<*, *>))
                "shift-clahe-level" -> builder.setShiftClaheLevel(buildShiftClaheLevel(value as Map<*, *>))
                "focus-in" -> builder.setFocusIn(buildFocusIn(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetDDELevel from Transit data
     */
    private fun buildSetDdeLevel(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertInt(value))
                else -> logger.warn { "Unknown field \$key for SetDDELevel" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetCalibMode from Transit data
     */
    private fun buildSetCalibMode(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for SetCalibMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build Zoom from Transit data
     */
    private fun buildZoom(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "set-zoom-table-value" -> builder.setSetZoomTableValue(buildSetZoomTableValue(value as Map<*, *>))
                "next-zoom-table-pos" -> builder.setNextZoomTablePos(buildNextZoomTablePos(value as Map<*, *>))
                "prev-zoom-table-pos" -> builder.setPrevZoomTablePos(buildPrevZoomTablePos(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Zoom" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetZoomTableValue from Transit data
     */
    private fun buildSetZoomTableValue(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertInt(value))
                else -> logger.warn { "Unknown field \$key for SetZoomTableValue" }
            }
        }

        return builder.build()
    }

    /**
     * Build NextZoomTablePos from Transit data
     */
    private fun buildNextZoomTablePos(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for NextZoomTablePos" }
            }
        }

        return builder.build()
    }

    /**
     * Build PrevZoomTablePos from Transit data
     */
    private fun buildPrevZoomTablePos(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for PrevZoomTablePos" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetAGC from Transit data
     */
    private fun buildSetAgc(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setValue(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for SetAGC" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftDDE from Transit data
     */
    private fun buildShiftDde(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertInt(value))
                else -> logger.warn { "Unknown field \$key for ShiftDDE" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetFilters from Transit data
     */
    private fun buildSetFilter(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setValue(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for SetFilters" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetClaheLevel from Transit data
     */
    private fun buildSetClaheLevel(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetClaheLevel" }
            }
        }

        return builder.build()
    }

    /**
     * Build DisableDDE from Transit data
     */
    private fun buildDisableDde(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for DisableDDE" }
            }
        }

        return builder.build()
    }

    /**
     * Build PrevFxMode from Transit data
     */
    private fun buildPrevFxMode(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for PrevFxMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildStart(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$Start` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$Start`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Start" }
            }
        }

        return builder.build()
    }

    /**
     * Build FocusStepMinus from Transit data
     */
    private fun buildFocusStepMinus(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for FocusStepMinus" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetDigitalZoomLevel from Transit data
     */
    private fun buildSetDigitalZoomLevel(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetDigitalZoomLevel" }
            }
        }

        return builder.build()
    }

    /**
     * Build EnableDDE from Transit data
     */
    private fun buildEnableDde(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for EnableDDE" }
            }
        }

        return builder.build()
    }

    /**
     * Build FocusStop from Transit data
     */
    private fun buildFocusStop(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for FocusStop" }
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildStop(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$Stop` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$Stop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Stop" }
            }
        }

        return builder.build()
    }

    /**
     * Build ResetZoom from Transit data
     */
    private fun buildResetZoom(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ResetZoom" }
            }
        }

        return builder.build()
    }

    /**
     * Build ZoomOut from Transit data
     */
    private fun buildZoomOut(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ZoomOut" }
            }
        }

        return builder.build()
    }

    /**
     * Build Photo from Transit data
     */
    private fun buildPhoto(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$Photo` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$Photo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Photo" }
            }
        }

        return builder.build()
    }

    /**
     * Build ZoomIn from Transit data
     */
    private fun buildZoomIn(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ZoomIn" }
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildGetMeteo(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for GetMeteo" }
            }
        }

        return builder.build()
    }

    /**
     * Build FocusStepPlus from Transit data
     */
    private fun buildFocusStepPlus(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for FocusStepPlus" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetFxMode from Transit data
     */
    private fun buildSetFxMode(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "mode" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setMode(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for SetFxMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build RefreshFxMode from Transit data
     */
    private fun buildRefreshFxMode(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for RefreshFxMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build FocusOut from Transit data
     */
    private fun buildFocusOut(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for FocusOut" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetAutoFocus from Transit data
     */
    private fun buildSetAutoFocus(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertBoolean(value))
                else -> logger.warn { "Unknown field \$key for SetAutoFocus" }
            }
        }

        return builder.build()
    }

    /**
     * Build ZoomStop from Transit data
     */
    private fun buildZoomStop(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ZoomStop" }
            }
        }

        return builder.build()
    }

    /**
     * Build SaveToTable from Transit data
     */
    private fun buildSaveToTable(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for SaveToTable" }
            }
        }

        return builder.build()
    }

    /**
     * Build NextFxMode from Transit data
     */
    private fun buildNextFxMode(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for NextFxMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build Calibrate from Transit data
     */
    private fun buildCalibrate(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Calibrate" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftClaheLevel from Transit data
     */
    private fun buildShiftClaheLevel(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for ShiftClaheLevel" }
            }
        }

        return builder.build()
    }

    /**
     * Build FocusIn from Transit data
     */
    private fun buildFocusIn(data: Map<*, *>): `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn` {
        val builder = `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for FocusIn" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildLira(data: Map<*, *>): `cmd.Lira.JonSharedCmdLira$Root` {
        val builder = `cmd.Lira.JonSharedCmdLira$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "refine-target" -> builder.setRefineTarget(buildRefineTarget(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build Refine_target from Transit data
     */
    private fun buildRefineTarget(data: Map<*, *>): `cmd.Lira.JonSharedCmdLira$Refine_target` {
        val builder = `cmd.Lira.JonSharedCmdLira$Refine_target`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "target" -> builder.setTarget(buildTarget(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Refine_target" }
            }
        }

        return builder.build()
    }

    /**
     * Build JonGuiDataLiraTarget from Transit data
     */
    private fun buildTarget(data: Map<*, *>): `cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget` {
        val builder = `cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
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
                else -> logger.warn { "Unknown field \$key for JonGuiDataLiraTarget" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildLrfCalib(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$Root` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "day" -> builder.setDay(buildDay(value as Map<*, *>))
                "heat" -> builder.setHeat(buildHeat(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build Offsets from Transit data
     */
    private fun buildDay(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "set" -> builder.setSet(buildSet(value as Map<*, *>))
                "save" -> builder.setSave(buildSave(value as Map<*, *>))
                "reset" -> builder.setReset(buildReset(value as Map<*, *>))
                "shift" -> builder.setShift(buildShift(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Offsets" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetOffsets from Transit data
     */
    private fun buildSet(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "x" -> builder.setX(convertInt(value))
                "y" -> builder.setY(convertInt(value))
                else -> logger.warn { "Unknown field \$key for SetOffsets" }
            }
        }

        return builder.build()
    }

    /**
     * Build SaveOffsets from Transit data
     */
    private fun buildSave(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for SaveOffsets" }
            }
        }

        return builder.build()
    }

    /**
     * Build ResetOffsets from Transit data
     */
    private fun buildReset(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ResetOffsets" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftOffsetsBy from Transit data
     */
    private fun buildShift(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "x" -> builder.setX(convertInt(value))
                "y" -> builder.setY(convertInt(value))
                else -> logger.warn { "Unknown field \$key for ShiftOffsetsBy" }
            }
        }

        return builder.build()
    }

    /**
     * Build Offsets from Transit data
     */
    private fun buildHeat(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "set" -> builder.setSet(buildSet(value as Map<*, *>))
                "save" -> builder.setSave(buildSave(value as Map<*, *>))
                "reset" -> builder.setReset(buildReset(value as Map<*, *>))
                "shift" -> builder.setShift(buildShift(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Offsets" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetOffsets from Transit data
     */
    private fun buildSet(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "x" -> builder.setX(convertInt(value))
                "y" -> builder.setY(convertInt(value))
                else -> logger.warn { "Unknown field \$key for SetOffsets" }
            }
        }

        return builder.build()
    }

    /**
     * Build SaveOffsets from Transit data
     */
    private fun buildSave(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for SaveOffsets" }
            }
        }

        return builder.build()
    }

    /**
     * Build ResetOffsets from Transit data
     */
    private fun buildReset(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ResetOffsets" }
            }
        }

        return builder.build()
    }

    /**
     * Build ShiftOffsetsBy from Transit data
     */
    private fun buildShift(data: Map<*, *>): `cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy` {
        val builder = `cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "x" -> builder.setX(convertInt(value))
                "y" -> builder.setY(convertInt(value))
                else -> logger.warn { "Unknown field \$key for ShiftOffsetsBy" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildRotary(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$Root` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "rotate-to-gps" -> builder.setRotateToGps(buildRotateToGps(value as Map<*, *>))
                "scan-pause" -> builder.setScanPause(buildScanPause(value as Map<*, *>))
                "rotate-to-ndc" -> builder.setRotateToNdc(buildRotateToNdc(value as Map<*, *>))
                "scan-start" -> builder.setScanStart(buildScanStart(value as Map<*, *>))
                "set-platform-azimuth" -> builder.setSetPlatformAzimuth(buildSetPlatformAzimuth(value as Map<*, *>))
                "scan-stop" -> builder.setScanStop(buildScanStop(value as Map<*, *>))
                "start" -> builder.setStart(buildStart(value as Map<*, *>))
                "stop" -> builder.setStop(buildStop(value as Map<*, *>))
                "set-origin-gps" -> builder.setSetOriginGps(buildSetOriginGps(value as Map<*, *>))
                "scan-next" -> builder.setScanNext(buildScanNext(value as Map<*, *>))
                "set-platform-bank" -> builder.setSetPlatformBank(buildSetPlatformBank(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildGetMeteo(value as Map<*, *>))
                "set-use-rotary-as-compass" -> builder.setSetUseRotaryAsCompass(buildSetUseRotaryAsCompass(value as Map<*, *>))
                "scan-prev" -> builder.setScanPrev(buildScanPrev(value as Map<*, *>))
                "scan-add-node" -> builder.setScanAddNode(buildScanAddNode(value as Map<*, *>))
                "set-platform-elevation" -> builder.setSetPlatformElevation(buildSetPlatformElevation(value as Map<*, *>))
                "scan-select-node" -> builder.setScanSelectNode(buildScanSelectNode(value as Map<*, *>))
                "halt" -> builder.setHalt(buildHalt(value as Map<*, *>))
                "scan-delete-node" -> builder.setScanDeleteNode(buildScanDeleteNode(value as Map<*, *>))
                "axis" -> builder.setAxis(buildAxis(value as Map<*, *>))
                "scan-unpause" -> builder.setScanUnpause(buildScanUnpause(value as Map<*, *>))
                "set-mode" -> builder.setSetMode(buildSetMode(value as Map<*, *>))
                "scan-refresh-node-list" -> builder.setScanRefreshNodeList(buildScanRefreshNodeList(value as Map<*, *>))
                "scan-update-node" -> builder.setScanUpdateNode(buildScanUpdateNode(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateToGPS from Transit data
     */
    private fun buildRotateToGps(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "latitude" -> builder.setLatitude(convertFloat(value))
                "longitude" -> builder.setLongitude(convertFloat(value))
                "altitude" -> builder.setAltitude(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for RotateToGPS" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanPause from Transit data
     */
    private fun buildScanPause(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanPause" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateToNDC from Transit data
     */
    private fun buildRotateToNdc(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "channel" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setChannel(enumValue)
                }
                "x" -> builder.setX(convertFloat(value))
                "y" -> builder.setY(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for RotateToNDC" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanStart from Transit data
     */
    private fun buildScanStart(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanStart" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetPlatformAzimuth from Transit data
     */
    private fun buildSetPlatformAzimuth(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetPlatformAzimuth" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanStop from Transit data
     */
    private fun buildScanStop(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanStop" }
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildStart(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$Start` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$Start`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Start" }
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildStop(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$Stop` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$Stop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Stop" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetOriginGPS from Transit data
     */
    private fun buildSetOriginGps(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "latitude" -> builder.setLatitude(convertFloat(value))
                "longitude" -> builder.setLongitude(convertFloat(value))
                "altitude" -> builder.setAltitude(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetOriginGPS" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanNext from Transit data
     */
    private fun buildScanNext(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanNext" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetPlatformBank from Transit data
     */
    private fun buildSetPlatformBank(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetPlatformBank" }
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildGetMeteo(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for GetMeteo" }
            }
        }

        return builder.build()
    }

    /**
     * Build setUseRotaryAsCompass from Transit data
     */
    private fun buildSetUseRotaryAsCompass(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "flag" -> builder.setFlag(convertBoolean(value))
                else -> logger.warn { "Unknown field \$key for setUseRotaryAsCompass" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanPrev from Transit data
     */
    private fun buildScanPrev(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanPrev" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanAddNode from Transit data
     */
    private fun buildScanAddNode(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "index" -> builder.setIndex(convertInt(value))
                "DayZoomTableValue" -> builder.setDayzoomtablevalue(convertInt(value))
                "HeatZoomTableValue" -> builder.setHeatzoomtablevalue(convertInt(value))
                "azimuth" -> builder.setAzimuth(convertDouble(value))
                "elevation" -> builder.setElevation(convertDouble(value))
                "linger" -> builder.setLinger(convertDouble(value))
                "speed" -> builder.setSpeed(convertDouble(value))
                else -> logger.warn { "Unknown field \$key for ScanAddNode" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetPlatformElevation from Transit data
     */
    private fun buildSetPlatformElevation(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetPlatformElevation" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanSelectNode from Transit data
     */
    private fun buildScanSelectNode(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "index" -> builder.setIndex(convertInt(value))
                else -> logger.warn { "Unknown field \$key for ScanSelectNode" }
            }
        }

        return builder.build()
    }

    /**
     * Build Halt from Transit data
     */
    private fun buildHalt(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$Halt` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$Halt`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Halt" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanDeleteNode from Transit data
     */
    private fun buildScanDeleteNode(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "index" -> builder.setIndex(convertInt(value))
                else -> logger.warn { "Unknown field \$key for ScanDeleteNode" }
            }
        }

        return builder.build()
    }

    /**
     * Build Axis from Transit data
     */
    private fun buildAxis(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$Axis` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$Axis`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "azimuth" -> builder.setAzimuth(buildAzimuth(value as Map<*, *>))
                "elevation" -> builder.setElevation(buildElevation(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Axis" }
            }
        }

        return builder.build()
    }

    /**
     * Build Azimuth from Transit data
     */
    private fun buildAzimuth(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "set-value" -> builder.setSetValue(buildSetValue(value as Map<*, *>))
                "rotate-to" -> builder.setRotateTo(buildRotateTo(value as Map<*, *>))
                "rotate" -> builder.setRotate(buildRotate(value as Map<*, *>))
                "relative" -> builder.setRelative(buildRelative(value as Map<*, *>))
                "relative-set" -> builder.setRelativeSet(buildRelativeSet(value as Map<*, *>))
                "halt" -> builder.setHalt(buildHalt(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Azimuth" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetAzimuthValue from Transit data
     */
    private fun buildSetValue(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                "direction" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setDirection(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for SetAzimuthValue" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateAzimuthTo from Transit data
     */
    private fun buildRotateTo(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "target-value" -> builder.setTargetValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setDirection(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for RotateAzimuthTo" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateAzimuth from Transit data
     */
    private fun buildRotate(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setDirection(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for RotateAzimuth" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateAzimuthRelative from Transit data
     */
    private fun buildRelative(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setDirection(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for RotateAzimuthRelative" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateAzimuthRelativeSet from Transit data
     */
    private fun buildRelativeSet(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                "direction" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setDirection(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for RotateAzimuthRelativeSet" }
            }
        }

        return builder.build()
    }

    /**
     * Build HaltAzimuth from Transit data
     */
    private fun buildHalt(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for HaltAzimuth" }
            }
        }

        return builder.build()
    }

    /**
     * Build Elevation from Transit data
     */
    private fun buildElevation(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$Elevation` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$Elevation`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "set-value" -> builder.setSetValue(buildSetValue(value as Map<*, *>))
                "rotate-to" -> builder.setRotateTo(buildRotateTo(value as Map<*, *>))
                "rotate" -> builder.setRotate(buildRotate(value as Map<*, *>))
                "relative" -> builder.setRelative(buildRelative(value as Map<*, *>))
                "relative-set" -> builder.setRelativeSet(buildRelativeSet(value as Map<*, *>))
                "halt" -> builder.setHalt(buildHalt(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Elevation" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetElevationValue from Transit data
     */
    private fun buildSetValue(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetElevationValue" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateElevationTo from Transit data
     */
    private fun buildRotateTo(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "target-value" -> builder.setTargetValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for RotateElevationTo" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateElevation from Transit data
     */
    private fun buildRotate(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setDirection(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for RotateElevation" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateElevationRelative from Transit data
     */
    private fun buildRelative(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                "speed" -> builder.setSpeed(convertFloat(value))
                "direction" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setDirection(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for RotateElevationRelative" }
            }
        }

        return builder.build()
    }

    /**
     * Build RotateElevationRelativeSet from Transit data
     */
    private fun buildRelativeSet(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                "direction" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setDirection(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for RotateElevationRelativeSet" }
            }
        }

        return builder.build()
    }

    /**
     * Build HaltElevation from Transit data
     */
    private fun buildHalt(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for HaltElevation" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanUnpause from Transit data
     */
    private fun buildScanUnpause(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanUnpause" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetMode from Transit data
     */
    private fun buildSetMode(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$SetMode` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$SetMode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "mode" -> {
                    val enumValue =
                        when (value) {
                            is Keyword -> value.name
                            else -> value.toString()
                        }.toUpperCase().replace("-", "_")
                    // TODO: Convert to actual enum type
                    builder.setMode(enumValue)
                }
                else -> logger.warn { "Unknown field \$key for SetMode" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanRefreshNodeList from Transit data
     */
    private fun buildScanRefreshNodeList(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for ScanRefreshNodeList" }
            }
        }

        return builder.build()
    }

    /**
     * Build ScanUpdateNode from Transit data
     */
    private fun buildScanUpdateNode(data: Map<*, *>): `cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode` {
        val builder = `cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "index" -> builder.setIndex(convertInt(value))
                "DayZoomTableValue" -> builder.setDayzoomtablevalue(convertInt(value))
                "HeatZoomTableValue" -> builder.setHeatzoomtablevalue(convertInt(value))
                "azimuth" -> builder.setAzimuth(convertDouble(value))
                "elevation" -> builder.setElevation(convertDouble(value))
                "linger" -> builder.setLinger(convertDouble(value))
                "speed" -> builder.setSpeed(convertDouble(value))
                else -> logger.warn { "Unknown field \$key for ScanUpdateNode" }
            }
        }

        return builder.build()
    }

    /**
     * Build Root from Transit data
     */
    private fun buildCompass(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$Root` {
        val builder = `cmd.Compass.JonSharedCmdCompass$Root`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "calibrate-cencel" -> builder.setCalibrateCencel(buildCalibrateCencel(value as Map<*, *>))
                "start" -> builder.setStart(buildStart(value as Map<*, *>))
                "set-offset-angle-elevation" -> builder.setSetOffsetAngleElevation(buildSetOffsetAngleElevation(value as Map<*, *>))
                "stop" -> builder.setStop(buildStop(value as Map<*, *>))
                "calibrate-next" -> builder.setCalibrateNext(buildCalibrateNext(value as Map<*, *>))
                "get-meteo" -> builder.setGetMeteo(buildGetMeteo(value as Map<*, *>))
                "set-use-rotary-position" -> builder.setSetUseRotaryPosition(buildSetUseRotaryPosition(value as Map<*, *>))
                "set-magnetic-declination" -> builder.setSetMagneticDeclination(buildSetMagneticDeclination(value as Map<*, *>))
                "start-calibrate-short" -> builder.setStartCalibrateShort(buildStartCalibrateShort(value as Map<*, *>))
                "start-calibrate-long" -> builder.setStartCalibrateLong(buildStartCalibrateLong(value as Map<*, *>))
                "set-offset-angle-azimuth" -> builder.setSetOffsetAngleAzimuth(buildSetOffsetAngleAzimuth(value as Map<*, *>))
                else -> logger.warn { "Unknown field \$key for Root" }
            }
        }

        return builder.build()
    }

    /**
     * Build CalibrateCencel from Transit data
     */
    private fun buildCalibrateCencel(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$CalibrateCencel` {
        val builder = `cmd.Compass.JonSharedCmdCompass$CalibrateCencel`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for CalibrateCencel" }
            }
        }

        return builder.build()
    }

    /**
     * Build Start from Transit data
     */
    private fun buildStart(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$Start` {
        val builder = `cmd.Compass.JonSharedCmdCompass$Start`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Start" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetOffsetAngleElevation from Transit data
     */
    private fun buildSetOffsetAngleElevation(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation` {
        val builder = `cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetOffsetAngleElevation" }
            }
        }

        return builder.build()
    }

    /**
     * Build Stop from Transit data
     */
    private fun buildStop(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$Stop` {
        val builder = `cmd.Compass.JonSharedCmdCompass$Stop`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Stop" }
            }
        }

        return builder.build()
    }

    /**
     * Build CalibrateNext from Transit data
     */
    private fun buildCalibrateNext(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$CalibrateNext` {
        val builder = `cmd.Compass.JonSharedCmdCompass$CalibrateNext`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for CalibrateNext" }
            }
        }

        return builder.build()
    }

    /**
     * Build GetMeteo from Transit data
     */
    private fun buildGetMeteo(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$GetMeteo` {
        val builder = `cmd.Compass.JonSharedCmdCompass$GetMeteo`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for GetMeteo" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetUseRotaryPosition from Transit data
     */
    private fun buildSetUseRotaryPosition(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition` {
        val builder = `cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "flag" -> builder.setFlag(convertBoolean(value))
                else -> logger.warn { "Unknown field \$key for SetUseRotaryPosition" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetMagneticDeclination from Transit data
     */
    private fun buildSetMagneticDeclination(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination` {
        val builder = `cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetMagneticDeclination" }
            }
        }

        return builder.build()
    }

    /**
     * Build CalibrateStartShort from Transit data
     */
    private fun buildStartCalibrateShort(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$CalibrateStartShort` {
        val builder = `cmd.Compass.JonSharedCmdCompass$CalibrateStartShort`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for CalibrateStartShort" }
            }
        }

        return builder.build()
    }

    /**
     * Build CalibrateStartLong from Transit data
     */
    private fun buildStartCalibrateLong(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$CalibrateStartLong` {
        val builder = `cmd.Compass.JonSharedCmdCompass$CalibrateStartLong`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for CalibrateStartLong" }
            }
        }

        return builder.build()
    }

    /**
     * Build SetOffsetAngleAzimuth from Transit data
     */
    private fun buildSetOffsetAngleAzimuth(data: Map<*, *>): `cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth` {
        val builder = `cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                "value" -> builder.setValue(convertFloat(value))
                else -> logger.warn { "Unknown field \$key for SetOffsetAngleAzimuth" }
            }
        }

        return builder.build()
    }

    /**
     * Build Frozen from Transit data
     */
    private fun buildFrozen(data: Map<*, *>): `cmd.JonSharedCmd$Frozen` {
        val builder = `cmd.JonSharedCmd$Frozen`.newBuilder()

        for ((key, value) in data) {
            if (value == null) continue

            when (key.toString()) {
                else -> logger.warn { "Unknown field \$key for Frozen" }
            }
        }

        return builder.build()
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractOsd(msg: `cmd.OSD.JonSharedCmdOsd$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasShow_default_screen()) {
            result["show-default-screen"] = extractShowDefaultScreen(msg.getShow_default_screen())
        }
        if (msg.hasShow_lrf_measure_screen()) {
            result["show-lrf-measure-screen"] = extractShowLrfMeasureScreen(msg.getShow_lrf_measure_screen())
        }
        if (msg.hasShow_lrf_result_screen()) {
            result["show-lrf-result-screen"] = extractShowLrfResultScreen(msg.getShow_lrf_result_screen())
        }
        if (msg.hasShow_lrf_result_simplified_screen()) {
            result["show-lrf-result-simplified-screen"] = extractShowLrfResultSimplifiedScreen(msg.getShow_lrf_result_simplified_screen())
        }
        if (msg.hasEnable_heat_osd()) {
            result["enable-heat-osd"] = extractEnableHeatOsd(msg.getEnable_heat_osd())
        }
        if (msg.hasDisable_heat_osd()) {
            result["disable-heat-osd"] = extractDisableHeatOsd(msg.getDisable_heat_osd())
        }
        if (msg.hasEnable_day_osd()) {
            result["enable-day-osd"] = extractEnableDayOsd(msg.getEnable_day_osd())
        }
        if (msg.hasDisable_day_osd()) {
            result["disable-day-osd"] = extractDisableDayOsd(msg.getDisable_day_osd())
        }

        return result
    }

    /**
     * Extract Transit data from ShowDefaultScreen
     */
    private fun extractShowDefaultScreen(msg: `cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShowLRFMeasureScreen
     */
    private fun extractShowLrfMeasureScreen(msg: `cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShowLRFResultScreen
     */
    private fun extractShowLrfResultScreen(msg: `cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShowLRFResultSimplifiedScreen
     */
    private fun extractShowLrfResultSimplifiedScreen(msg: `cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnableHeatOSD
     */
    private fun extractEnableHeatOsd(msg: `cmd.OSD.JonSharedCmdOsd$EnableHeatOSD`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from DisableHeatOSD
     */
    private fun extractDisableHeatOsd(msg: `cmd.OSD.JonSharedCmdOsd$DisableHeatOSD`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnableDayOSD
     */
    private fun extractEnableDayOsd(msg: `cmd.OSD.JonSharedCmdOsd$EnableDayOSD`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from DisableDayOSD
     */
    private fun extractDisableDayOsd(msg: `cmd.OSD.JonSharedCmdOsd$DisableDayOSD`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Ping
     */
    private fun extractPing(msg: `cmd.JonSharedCmd$Ping`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractSystem(msg: `cmd.System.JonSharedCmdSystem$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasGeodesic_mode_disable()) {
            result["geodesic-mode-disable"] = extractGeodesicModeDisable(msg.getGeodesic_mode_disable())
        }
        if (msg.hasStart_all()) {
            result["start-all"] = extractStartAll(msg.getStart_all())
        }
        if (msg.hasGeodesic_mode_enable()) {
            result["geodesic-mode-enable"] = extractGeodesicModeEnable(msg.getGeodesic_mode_enable())
        }
        if (msg.hasLocalization()) {
            result["localization"] = extractLocalization(msg.getLocalization())
        }
        if (msg.hasUnmark_rec_important()) {
            result["unmark-rec-important"] = extractUnmarkRecImportant(msg.getUnmark_rec_important())
        }
        if (msg.hasStop_rec()) {
            result["stop-rec"] = extractStopRec(msg.getStop_rec())
        }
        if (msg.hasReboot()) {
            result["reboot"] = extractReboot(msg.getReboot())
        }
        if (msg.hasStart_rec()) {
            result["start-rec"] = extractStartRec(msg.getStart_rec())
        }
        if (msg.hasPower_off()) {
            result["power-off"] = extractPowerOff(msg.getPower_off())
        }
        if (msg.hasReset_configs()) {
            result["reset-configs"] = extractResetConfigs(msg.getReset_configs())
        }
        if (msg.hasStop_all()) {
            result["stop-all"] = extractStopAll(msg.getStop_all())
        }
        if (msg.hasEnter_transport()) {
            result["enter-transport"] = extractEnterTransport(msg.getEnter_transport())
        }
        if (msg.hasMark_rec_important()) {
            result["mark-rec-important"] = extractMarkRecImportant(msg.getMark_rec_important())
        }

        return result
    }

    /**
     * Extract Transit data from DisableGeodesicMode
     */
    private fun extractGeodesicModeDisable(msg: `cmd.System.JonSharedCmdSystem$DisableGeodesicMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StartALl
     */
    private fun extractStartAll(msg: `cmd.System.JonSharedCmdSystem$StartALl`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnableGeodesicMode
     */
    private fun extractGeodesicModeEnable(msg: `cmd.System.JonSharedCmdSystem$EnableGeodesicMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetLocalization
     */
    private fun extractLocalization(msg: `cmd.System.JonSharedCmdSystem$SetLocalization`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["loc"] =
            msg
                .getLoc()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from UnmarkRecImportant
     */
    private fun extractUnmarkRecImportant(msg: `cmd.System.JonSharedCmdSystem$UnmarkRecImportant`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StopRec
     */
    private fun extractStopRec(msg: `cmd.System.JonSharedCmdSystem$StopRec`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Reboot
     */
    private fun extractReboot(msg: `cmd.System.JonSharedCmdSystem$Reboot`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StartRec
     */
    private fun extractStartRec(msg: `cmd.System.JonSharedCmdSystem$StartRec`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from PowerOff
     */
    private fun extractPowerOff(msg: `cmd.System.JonSharedCmdSystem$PowerOff`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ResetConfigs
     */
    private fun extractResetConfigs(msg: `cmd.System.JonSharedCmdSystem$ResetConfigs`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StopALl
     */
    private fun extractStopAll(msg: `cmd.System.JonSharedCmdSystem$StopALl`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnterTransport
     */
    private fun extractEnterTransport(msg: `cmd.System.JonSharedCmdSystem$EnterTransport`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from MarkRecImportant
     */
    private fun extractMarkRecImportant(msg: `cmd.System.JonSharedCmdSystem$MarkRecImportant`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Noop
     */
    private fun extractNoop(msg: `cmd.JonSharedCmd$Noop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractCv(msg: `cmd.CV.JonSharedCmdCv$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasVampire_mode_enable()) {
            result["vampire-mode-enable"] = extractVampireModeEnable(msg.getVampire_mode_enable())
        }
        if (msg.hasVampire_mode_disable()) {
            result["vampire-mode-disable"] = extractVampireModeDisable(msg.getVampire_mode_disable())
        }
        if (msg.hasDump_stop()) {
            result["dump-stop"] = extractDumpStop(msg.getDump_stop())
        }
        if (msg.hasStabilization_mode_disable()) {
            result["stabilization-mode-disable"] = extractStabilizationModeDisable(msg.getStabilization_mode_disable())
        }
        if (msg.hasSet_auto_focus()) {
            result["set-auto-focus"] = extractSetAutoFocus(msg.getSet_auto_focus())
        }
        if (msg.hasStart_track_ndc()) {
            result["start-track-ndc"] = extractStartTrackNdc(msg.getStart_track_ndc())
        }
        if (msg.hasDump_start()) {
            result["dump-start"] = extractDumpStart(msg.getDump_start())
        }
        if (msg.hasStop_track()) {
            result["stop-track"] = extractStopTrack(msg.getStop_track())
        }
        if (msg.hasStabilization_mode_enable()) {
            result["stabilization-mode-enable"] = extractStabilizationModeEnable(msg.getStabilization_mode_enable())
        }

        return result
    }

    /**
     * Extract Transit data from VampireModeEnable
     */
    private fun extractVampireModeEnable(msg: `cmd.CV.JonSharedCmdCv$VampireModeEnable`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from VampireModeDisable
     */
    private fun extractVampireModeDisable(msg: `cmd.CV.JonSharedCmdCv$VampireModeDisable`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from DumpStop
     */
    private fun extractDumpStop(msg: `cmd.CV.JonSharedCmdCv$DumpStop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StabilizationModeDisable
     */
    private fun extractStabilizationModeDisable(msg: `cmd.CV.JonSharedCmdCv$StabilizationModeDisable`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetAutoFocus
     */
    private fun extractSetAutoFocus(msg: `cmd.CV.JonSharedCmdCv$SetAutoFocus`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["channel"] =
            msg
                .getChannel()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from StartTrackNDC
     */
    private fun extractStartTrackNdc(msg: `cmd.CV.JonSharedCmdCv$StartTrackNDC`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["channel"] =
            msg
                .getChannel()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["x"] = msg.getX()
        result["y"] = msg.getY()
        result["frame-time"] = msg.getFrame_time()

        return result
    }

    /**
     * Extract Transit data from DumpStart
     */
    private fun extractDumpStart(msg: `cmd.CV.JonSharedCmdCv$DumpStart`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StopTrack
     */
    private fun extractStopTrack(msg: `cmd.CV.JonSharedCmdCv$StopTrack`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from StabilizationModeEnable
     */
    private fun extractStabilizationModeEnable(msg: `cmd.CV.JonSharedCmdCv$StabilizationModeEnable`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractGps(msg: `cmd.Gps.JonSharedCmdGps$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasStart()) {
            result["start"] = extractStart(msg.getStart())
        }
        if (msg.hasStop()) {
            result["stop"] = extractStop(msg.getStop())
        }
        if (msg.hasSet_manual_position()) {
            result["set-manual-position"] = extractSetManualPosition(msg.getSet_manual_position())
        }
        if (msg.hasSet_use_manual_position()) {
            result["set-use-manual-position"] = extractSetUseManualPosition(msg.getSet_use_manual_position())
        }
        if (msg.hasGet_meteo()) {
            result["get-meteo"] = extractGetMeteo(msg.getGet_meteo())
        }

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractStart(msg: `cmd.Gps.JonSharedCmdGps$Start`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractStop(msg: `cmd.Gps.JonSharedCmdGps$Stop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetManualPosition
     */
    private fun extractSetManualPosition(msg: `cmd.Gps.JonSharedCmdGps$SetManualPosition`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["latitude"] = msg.getLatitude()
        result["longitude"] = msg.getLongitude()
        result["altitude"] = msg.getAltitude()

        return result
    }

    /**
     * Extract Transit data from SetUseManualPosition
     */
    private fun extractSetUseManualPosition(msg: `cmd.Gps.JonSharedCmdGps$SetUseManualPosition`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["flag"] = msg.getFlag()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractGetMeteo(msg: `cmd.Gps.JonSharedCmdGps$GetMeteo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractLrf(msg: `cmd.Lrf.JonSharedCmdLrf$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasTarget_designator_off()) {
            result["target-designator-off"] = extractTargetDesignatorOff(msg.getTarget_designator_off())
        }
        if (msg.hasTarget_designator_on_mode_b()) {
            result["target-designator-on-mode-b"] = extractTargetDesignatorOnModeB(msg.getTarget_designator_on_mode_b())
        }
        if (msg.hasDisable_fog_mode()) {
            result["disable-fog-mode"] = extractDisableFogMode(msg.getDisable_fog_mode())
        }
        if (msg.hasSet_scan_mode()) {
            result["set-scan-mode"] = extractSetScanMode(msg.getSet_scan_mode())
        }
        if (msg.hasRefine_off()) {
            result["refine-off"] = extractRefineOff(msg.getRefine_off())
        }
        if (msg.hasScan_off()) {
            result["scan-off"] = extractScanOff(msg.getScan_off())
        }
        if (msg.hasRefine_on()) {
            result["refine-on"] = extractRefineOn(msg.getRefine_on())
        }
        if (msg.hasStart()) {
            result["start"] = extractStart(msg.getStart())
        }
        if (msg.hasMeasure()) {
            result["measure"] = extractMeasure(msg.getMeasure())
        }
        if (msg.hasScan_on()) {
            result["scan-on"] = extractScanOn(msg.getScan_on())
        }
        if (msg.hasStop()) {
            result["stop"] = extractStop(msg.getStop())
        }
        if (msg.hasNew_session()) {
            result["new-session"] = extractNewSession(msg.getNew_session())
        }
        if (msg.hasGet_meteo()) {
            result["get-meteo"] = extractGetMeteo(msg.getGet_meteo())
        }
        if (msg.hasEnable_fog_mode()) {
            result["enable-fog-mode"] = extractEnableFogMode(msg.getEnable_fog_mode())
        }
        if (msg.hasTarget_designator_on_mode_a()) {
            result["target-designator-on-mode-a"] = extractTargetDesignatorOnModeA(msg.getTarget_designator_on_mode_a())
        }

        return result
    }

    /**
     * Extract Transit data from TargetDesignatorOff
     */
    private fun extractTargetDesignatorOff(msg: `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from TargetDesignatorOnModeB
     */
    private fun extractTargetDesignatorOnModeB(msg: `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from DisableFogMode
     */
    private fun extractDisableFogMode(msg: `cmd.Lrf.JonSharedCmdLrf$DisableFogMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetScanMode
     */
    private fun extractSetScanMode(msg: `cmd.Lrf.JonSharedCmdLrf$SetScanMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["mode"] =
            msg
                .getMode()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RefineOff
     */
    private fun extractRefineOff(msg: `cmd.Lrf.JonSharedCmdLrf$RefineOff`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanOff
     */
    private fun extractScanOff(msg: `cmd.Lrf.JonSharedCmdLrf$ScanOff`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from RefineOn
     */
    private fun extractRefineOn(msg: `cmd.Lrf.JonSharedCmdLrf$RefineOn`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractStart(msg: `cmd.Lrf.JonSharedCmdLrf$Start`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Measure
     */
    private fun extractMeasure(msg: `cmd.Lrf.JonSharedCmdLrf$Measure`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanOn
     */
    private fun extractScanOn(msg: `cmd.Lrf.JonSharedCmdLrf$ScanOn`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractStop(msg: `cmd.Lrf.JonSharedCmdLrf$Stop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from NewSession
     */
    private fun extractNewSession(msg: `cmd.Lrf.JonSharedCmdLrf$NewSession`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractGetMeteo(msg: `cmd.Lrf.JonSharedCmdLrf$GetMeteo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from EnableFogMode
     */
    private fun extractEnableFogMode(msg: `cmd.Lrf.JonSharedCmdLrf$EnableFogMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from TargetDesignatorOnModeA
     */
    private fun extractTargetDesignatorOnModeA(msg: `cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractDayCamGlassHeater(msg: `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasStart()) {
            result["start"] = extractStart(msg.getStart())
        }
        if (msg.hasStop()) {
            result["stop"] = extractStop(msg.getStop())
        }
        if (msg.hasTurn_on()) {
            result["turn-on"] = extractTurnOn(msg.getTurn_on())
        }
        if (msg.hasTurn_off()) {
            result["turn-off"] = extractTurnOff(msg.getTurn_off())
        }
        if (msg.hasGet_meteo()) {
            result["get-meteo"] = extractGetMeteo(msg.getGet_meteo())
        }

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractStart(msg: `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Start`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractStop(msg: `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$Stop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from TurnOn
     */
    private fun extractTurnOn(msg: `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOn`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from TurnOff
     */
    private fun extractTurnOff(msg: `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$TurnOff`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractGetMeteo(msg: `cmd.DayCamGlassHeater.JonSharedCmdDayCamGlassHeater$GetMeteo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractDayCamera(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasZoom()) {
            result["zoom"] = extractZoom(msg.getZoom())
        }
        if (msg.hasSet_infra_red_filter()) {
            result["set-infra-red-filter"] = extractSetInfraRedFilter(msg.getSet_infra_red_filter())
        }
        if (msg.hasSet_clahe_level()) {
            result["set-clahe-level"] = extractSetClaheLevel(msg.getSet_clahe_level())
        }
        if (msg.hasPrev_fx_mode()) {
            result["prev-fx-mode"] = extractPrevFxMode(msg.getPrev_fx_mode())
        }
        if (msg.hasStart()) {
            result["start"] = extractStart(msg.getStart())
        }
        if (msg.hasHalt_all()) {
            result["halt-all"] = extractHaltAll(msg.getHalt_all())
        }
        if (msg.hasSet_digital_zoom_level()) {
            result["set-digital-zoom-level"] = extractSetDigitalZoomLevel(msg.getSet_digital_zoom_level())
        }
        if (msg.hasStop()) {
            result["stop"] = extractStop(msg.getStop())
        }
        if (msg.hasPhoto()) {
            result["photo"] = extractPhoto(msg.getPhoto())
        }
        if (msg.hasGet_meteo()) {
            result["get-meteo"] = extractGetMeteo(msg.getGet_meteo())
        }
        if (msg.hasFocus()) {
            result["focus"] = extractFocus(msg.getFocus())
        }
        if (msg.hasSet_fx_mode()) {
            result["set-fx-mode"] = extractSetFxMode(msg.getSet_fx_mode())
        }
        if (msg.hasSet_iris()) {
            result["set-iris"] = extractSetIris(msg.getSet_iris())
        }
        if (msg.hasRefresh_fx_mode()) {
            result["refresh-fx-mode"] = extractRefreshFxMode(msg.getRefresh_fx_mode())
        }
        if (msg.hasSet_auto_iris()) {
            result["set-auto-iris"] = extractSetAutoIris(msg.getSet_auto_iris())
        }
        if (msg.hasNext_fx_mode()) {
            result["next-fx-mode"] = extractNextFxMode(msg.getNext_fx_mode())
        }
        if (msg.hasShift_clahe_level()) {
            result["shift-clahe-level"] = extractShiftClaheLevel(msg.getShift_clahe_level())
        }

        return result
    }

    /**
     * Extract Transit data from Zoom
     */
    private fun extractZoom(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Zoom`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasPrev_zoom_table_pos()) {
            result["prev-zoom-table-pos"] = extractPrevZoomTablePos(msg.getPrev_zoom_table_pos())
        }
        if (msg.hasOffset()) {
            result["offset"] = extractOffset(msg.getOffset())
        }
        if (msg.hasMove()) {
            result["move"] = extractMove(msg.getMove())
        }
        if (msg.hasReset_zoom()) {
            result["reset-zoom"] = extractResetZoom(msg.getReset_zoom())
        }
        if (msg.hasNext_zoom_table_pos()) {
            result["next-zoom-table-pos"] = extractNextZoomTablePos(msg.getNext_zoom_table_pos())
        }
        if (msg.hasSet_value()) {
            result["set-value"] = extractSetValue(msg.getSet_value())
        }
        if (msg.hasSet_zoom_table_value()) {
            result["set-zoom-table-value"] = extractSetZoomTableValue(msg.getSet_zoom_table_value())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractHalt(msg.getHalt())
        }
        if (msg.hasSave_to_table()) {
            result["save-to-table"] = extractSaveToTable(msg.getSave_to_table())
        }

        return result
    }

    /**
     * Extract Transit data from PrevZoomTablePos
     */
    private fun extractPrevZoomTablePos(msg: `cmd.DayCamera.JonSharedCmdDayCamera$PrevZoomTablePos`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Offset
     */
    private fun extractOffset(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Offset`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["offset-value"] = msg.getOffset_value()

        return result
    }

    /**
     * Extract Transit data from Move
     */
    private fun extractMove(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Move`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["target-value"] = msg.getTarget_value()
        result["speed"] = msg.getSpeed()

        return result
    }

    /**
     * Extract Transit data from ResetZoom
     */
    private fun extractResetZoom(msg: `cmd.DayCamera.JonSharedCmdDayCamera$ResetZoom`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from NextZoomTablePos
     */
    private fun extractNextZoomTablePos(msg: `cmd.DayCamera.JonSharedCmdDayCamera$NextZoomTablePos`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetValue
     */
    private fun extractSetValue(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetValue`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from SetZoomTableValue
     */
    private fun extractSetZoomTableValue(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetZoomTableValue`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Halt
     */
    private fun extractHalt(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Halt`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SaveToTable
     */
    private fun extractSaveToTable(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SaveToTable`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetInfraRedFilter
     */
    private fun extractSetInfraRedFilter(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetInfraRedFilter`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from SetClaheLevel
     */
    private fun extractSetClaheLevel(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetClaheLevel`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from PrevFxMode
     */
    private fun extractPrevFxMode(msg: `cmd.DayCamera.JonSharedCmdDayCamera$PrevFxMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractStart(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Start`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from HaltAll
     */
    private fun extractHaltAll(msg: `cmd.DayCamera.JonSharedCmdDayCamera$HaltAll`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetDigitalZoomLevel
     */
    private fun extractSetDigitalZoomLevel(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetDigitalZoomLevel`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractStop(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Stop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Photo
     */
    private fun extractPhoto(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Photo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractGetMeteo(msg: `cmd.DayCamera.JonSharedCmdDayCamera$GetMeteo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Focus
     */
    private fun extractFocus(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Focus`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet_value()) {
            result["set-value"] = extractSetValue(msg.getSet_value())
        }
        if (msg.hasMove()) {
            result["move"] = extractMove(msg.getMove())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractHalt(msg.getHalt())
        }
        if (msg.hasOffset()) {
            result["offset"] = extractOffset(msg.getOffset())
        }
        if (msg.hasReset_focus()) {
            result["reset-focus"] = extractResetFocus(msg.getReset_focus())
        }
        if (msg.hasSave_to_table_focus()) {
            result["save-to-table-focus"] = extractSaveToTableFocus(msg.getSave_to_table_focus())
        }

        return result
    }

    /**
     * Extract Transit data from SetValue
     */
    private fun extractSetValue(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetValue`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Move
     */
    private fun extractMove(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Move`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["target-value"] = msg.getTarget_value()
        result["speed"] = msg.getSpeed()

        return result
    }

    /**
     * Extract Transit data from Halt
     */
    private fun extractHalt(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Halt`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Offset
     */
    private fun extractOffset(msg: `cmd.DayCamera.JonSharedCmdDayCamera$Offset`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["offset-value"] = msg.getOffset_value()

        return result
    }

    /**
     * Extract Transit data from ResetFocus
     */
    private fun extractResetFocus(msg: `cmd.DayCamera.JonSharedCmdDayCamera$ResetFocus`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SaveToTableFocus
     */
    private fun extractSaveToTableFocus(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SaveToTableFocus`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetFxMode
     */
    private fun extractSetFxMode(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetFxMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["mode"] =
            msg
                .getMode()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from SetIris
     */
    private fun extractSetIris(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetIris`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from RefreshFxMode
     */
    private fun extractRefreshFxMode(msg: `cmd.DayCamera.JonSharedCmdDayCamera$RefreshFxMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetAutoIris
     */
    private fun extractSetAutoIris(msg: `cmd.DayCamera.JonSharedCmdDayCamera$SetAutoIris`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from NextFxMode
     */
    private fun extractNextFxMode(msg: `cmd.DayCamera.JonSharedCmdDayCamera$NextFxMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShiftClaheLevel
     */
    private fun extractShiftClaheLevel(msg: `cmd.DayCamera.JonSharedCmdDayCamera$ShiftClaheLevel`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractHeatCamera(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet_dde_level()) {
            result["set-dde-level"] = extractSetDdeLevel(msg.getSet_dde_level())
        }
        if (msg.hasSet_calib_mode()) {
            result["set-calib-mode"] = extractSetCalibMode(msg.getSet_calib_mode())
        }
        if (msg.hasZoom()) {
            result["zoom"] = extractZoom(msg.getZoom())
        }
        if (msg.hasSet_agc()) {
            result["set-agc"] = extractSetAgc(msg.getSet_agc())
        }
        if (msg.hasShift_dde()) {
            result["shift-dde"] = extractShiftDde(msg.getShift_dde())
        }
        if (msg.hasSet_filter()) {
            result["set-filter"] = extractSetFilter(msg.getSet_filter())
        }
        if (msg.hasSet_clahe_level()) {
            result["set-clahe-level"] = extractSetClaheLevel(msg.getSet_clahe_level())
        }
        if (msg.hasDisable_dde()) {
            result["disable-dde"] = extractDisableDde(msg.getDisable_dde())
        }
        if (msg.hasPrev_fx_mode()) {
            result["prev-fx-mode"] = extractPrevFxMode(msg.getPrev_fx_mode())
        }
        if (msg.hasStart()) {
            result["start"] = extractStart(msg.getStart())
        }
        if (msg.hasFocus_step_minus()) {
            result["focus-step-minus"] = extractFocusStepMinus(msg.getFocus_step_minus())
        }
        if (msg.hasSet_digital_zoom_level()) {
            result["set-digital-zoom-level"] = extractSetDigitalZoomLevel(msg.getSet_digital_zoom_level())
        }
        if (msg.hasEnable_dde()) {
            result["enable-dde"] = extractEnableDde(msg.getEnable_dde())
        }
        if (msg.hasFocus_stop()) {
            result["focus-stop"] = extractFocusStop(msg.getFocus_stop())
        }
        if (msg.hasStop()) {
            result["stop"] = extractStop(msg.getStop())
        }
        if (msg.hasReset_zoom()) {
            result["reset-zoom"] = extractResetZoom(msg.getReset_zoom())
        }
        if (msg.hasZoom_out()) {
            result["zoom-out"] = extractZoomOut(msg.getZoom_out())
        }
        if (msg.hasPhoto()) {
            result["photo"] = extractPhoto(msg.getPhoto())
        }
        if (msg.hasZoom_in()) {
            result["zoom-in"] = extractZoomIn(msg.getZoom_in())
        }
        if (msg.hasGet_meteo()) {
            result["get-meteo"] = extractGetMeteo(msg.getGet_meteo())
        }
        if (msg.hasFocus_step_plus()) {
            result["focus-step-plus"] = extractFocusStepPlus(msg.getFocus_step_plus())
        }
        if (msg.hasSet_fx_mode()) {
            result["set-fx-mode"] = extractSetFxMode(msg.getSet_fx_mode())
        }
        if (msg.hasRefresh_fx_mode()) {
            result["refresh-fx-mode"] = extractRefreshFxMode(msg.getRefresh_fx_mode())
        }
        if (msg.hasFocus_out()) {
            result["focus-out"] = extractFocusOut(msg.getFocus_out())
        }
        if (msg.hasSet_auto_focus()) {
            result["set-auto-focus"] = extractSetAutoFocus(msg.getSet_auto_focus())
        }
        if (msg.hasZoom_stop()) {
            result["zoom-stop"] = extractZoomStop(msg.getZoom_stop())
        }
        if (msg.hasSave_to_table()) {
            result["save-to-table"] = extractSaveToTable(msg.getSave_to_table())
        }
        if (msg.hasNext_fx_mode()) {
            result["next-fx-mode"] = extractNextFxMode(msg.getNext_fx_mode())
        }
        if (msg.hasCalibrate()) {
            result["calibrate"] = extractCalibrate(msg.getCalibrate())
        }
        if (msg.hasShift_clahe_level()) {
            result["shift-clahe-level"] = extractShiftClaheLevel(msg.getShift_clahe_level())
        }
        if (msg.hasFocus_in()) {
            result["focus-in"] = extractFocusIn(msg.getFocus_in())
        }

        return result
    }

    /**
     * Extract Transit data from SetDDELevel
     */
    private fun extractSetDdeLevel(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetDDELevel`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from SetCalibMode
     */
    private fun extractSetCalibMode(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetCalibMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Zoom
     */
    private fun extractZoom(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$Zoom`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet_zoom_table_value()) {
            result["set-zoom-table-value"] = extractSetZoomTableValue(msg.getSet_zoom_table_value())
        }
        if (msg.hasNext_zoom_table_pos()) {
            result["next-zoom-table-pos"] = extractNextZoomTablePos(msg.getNext_zoom_table_pos())
        }
        if (msg.hasPrev_zoom_table_pos()) {
            result["prev-zoom-table-pos"] = extractPrevZoomTablePos(msg.getPrev_zoom_table_pos())
        }

        return result
    }

    /**
     * Extract Transit data from SetZoomTableValue
     */
    private fun extractSetZoomTableValue(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetZoomTableValue`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from NextZoomTablePos
     */
    private fun extractNextZoomTablePos(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$NextZoomTablePos`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from PrevZoomTablePos
     */
    private fun extractPrevZoomTablePos(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$PrevZoomTablePos`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetAGC
     */
    private fun extractSetAgc(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetAGC`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] =
            msg
                .getValue()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from ShiftDDE
     */
    private fun extractShiftDde(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftDDE`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from SetFilters
     */
    private fun extractSetFilter(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetFilters`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] =
            msg
                .getValue()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from SetClaheLevel
     */
    private fun extractSetClaheLevel(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetClaheLevel`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from DisableDDE
     */
    private fun extractDisableDde(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$DisableDDE`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from PrevFxMode
     */
    private fun extractPrevFxMode(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$PrevFxMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractStart(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$Start`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from FocusStepMinus
     */
    private fun extractFocusStepMinus(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepMinus`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetDigitalZoomLevel
     */
    private fun extractSetDigitalZoomLevel(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetDigitalZoomLevel`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from EnableDDE
     */
    private fun extractEnableDde(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$EnableDDE`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from FocusStop
     */
    private fun extractFocusStop(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractStop(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$Stop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ResetZoom
     */
    private fun extractResetZoom(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$ResetZoom`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ZoomOut
     */
    private fun extractZoomOut(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomOut`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Photo
     */
    private fun extractPhoto(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$Photo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ZoomIn
     */
    private fun extractZoomIn(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomIn`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractGetMeteo(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$GetMeteo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from FocusStepPlus
     */
    private fun extractFocusStepPlus(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusStepPlus`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetFxMode
     */
    private fun extractSetFxMode(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetFxMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["mode"] =
            msg
                .getMode()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RefreshFxMode
     */
    private fun extractRefreshFxMode(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$RefreshFxMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from FocusOut
     */
    private fun extractFocusOut(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusOut`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetAutoFocus
     */
    private fun extractSetAutoFocus(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SetAutoFocus`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from ZoomStop
     */
    private fun extractZoomStop(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$ZoomStop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SaveToTable
     */
    private fun extractSaveToTable(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$SaveToTable`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from NextFxMode
     */
    private fun extractNextFxMode(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$NextFxMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Calibrate
     */
    private fun extractCalibrate(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$Calibrate`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShiftClaheLevel
     */
    private fun extractShiftClaheLevel(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$ShiftClaheLevel`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from FocusIn
     */
    private fun extractFocusIn(msg: `cmd.HeatCamera.JonSharedCmdHeatCamera$FocusIn`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractLira(msg: `cmd.Lira.JonSharedCmdLira$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasRefine_target()) {
            result["refine-target"] = extractRefineTarget(msg.getRefine_target())
        }

        return result
    }

    /**
     * Extract Transit data from Refine_target
     */
    private fun extractRefineTarget(msg: `cmd.Lira.JonSharedCmdLira$Refine_target`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasTarget()) {
            result["target"] = extractTarget(msg.getTarget())
        }

        return result
    }

    /**
     * Extract Transit data from JonGuiDataLiraTarget
     */
    private fun extractTarget(msg: `cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["uuid-part4"] = msg.getUuid_part4()
        result["uuid-part2"] = msg.getUuid_part2()
        result["target-altitude"] = msg.getTarget_altitude()
        result["uuid-part3"] = msg.getUuid_part3()
        result["target-azimuth"] = msg.getTarget_azimuth()
        result["distance"] = msg.getDistance()
        result["target-longitude"] = msg.getTarget_longitude()
        result["timestamp"] = msg.getTimestamp()
        result["uuid-part1"] = msg.getUuid_part1()
        result["target-latitude"] = msg.getTarget_latitude()
        result["target-elevation"] = msg.getTarget_elevation()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractLrfCalib(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasDay()) {
            result["day"] = extractDay(msg.getDay())
        }
        if (msg.hasHeat()) {
            result["heat"] = extractHeat(msg.getHeat())
        }

        return result
    }

    /**
     * Extract Transit data from Offsets
     */
    private fun extractDay(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet()) {
            result["set"] = extractSet(msg.getSet())
        }
        if (msg.hasSave()) {
            result["save"] = extractSave(msg.getSave())
        }
        if (msg.hasReset()) {
            result["reset"] = extractReset(msg.getReset())
        }
        if (msg.hasShift()) {
            result["shift"] = extractShift(msg.getShift())
        }

        return result
    }

    /**
     * Extract Transit data from SetOffsets
     */
    private fun extractSet(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from SaveOffsets
     */
    private fun extractSave(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ResetOffsets
     */
    private fun extractReset(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShiftOffsetsBy
     */
    private fun extractShift(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from Offsets
     */
    private fun extractHeat(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet()) {
            result["set"] = extractSet(msg.getSet())
        }
        if (msg.hasSave()) {
            result["save"] = extractSave(msg.getSave())
        }
        if (msg.hasReset()) {
            result["reset"] = extractReset(msg.getReset())
        }
        if (msg.hasShift()) {
            result["shift"] = extractShift(msg.getShift())
        }

        return result
    }

    /**
     * Extract Transit data from SetOffsets
     */
    private fun extractSet(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from SaveOffsets
     */
    private fun extractSave(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ResetOffsets
     */
    private fun extractReset(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ShiftOffsetsBy
     */
    private fun extractShift(msg: `cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from Root
     */
    private fun extractRotary(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasRotate_to_gps()) {
            result["rotate-to-gps"] = extractRotateToGps(msg.getRotate_to_gps())
        }
        if (msg.hasScan_pause()) {
            result["scan-pause"] = extractScanPause(msg.getScan_pause())
        }
        if (msg.hasRotate_to_ndc()) {
            result["rotate-to-ndc"] = extractRotateToNdc(msg.getRotate_to_ndc())
        }
        if (msg.hasScan_start()) {
            result["scan-start"] = extractScanStart(msg.getScan_start())
        }
        if (msg.hasSet_platform_azimuth()) {
            result["set-platform-azimuth"] = extractSetPlatformAzimuth(msg.getSet_platform_azimuth())
        }
        if (msg.hasScan_stop()) {
            result["scan-stop"] = extractScanStop(msg.getScan_stop())
        }
        if (msg.hasStart()) {
            result["start"] = extractStart(msg.getStart())
        }
        if (msg.hasStop()) {
            result["stop"] = extractStop(msg.getStop())
        }
        if (msg.hasSet_origin_gps()) {
            result["set-origin-gps"] = extractSetOriginGps(msg.getSet_origin_gps())
        }
        if (msg.hasScan_next()) {
            result["scan-next"] = extractScanNext(msg.getScan_next())
        }
        if (msg.hasSet_platform_bank()) {
            result["set-platform-bank"] = extractSetPlatformBank(msg.getSet_platform_bank())
        }
        if (msg.hasGet_meteo()) {
            result["get-meteo"] = extractGetMeteo(msg.getGet_meteo())
        }
        if (msg.hasSet_use_rotary_as_compass()) {
            result["set-use-rotary-as-compass"] = extractSetUseRotaryAsCompass(msg.getSet_use_rotary_as_compass())
        }
        if (msg.hasScan_prev()) {
            result["scan-prev"] = extractScanPrev(msg.getScan_prev())
        }
        if (msg.hasScan_add_node()) {
            result["scan-add-node"] = extractScanAddNode(msg.getScan_add_node())
        }
        if (msg.hasSet_platform_elevation()) {
            result["set-platform-elevation"] = extractSetPlatformElevation(msg.getSet_platform_elevation())
        }
        if (msg.hasScan_select_node()) {
            result["scan-select-node"] = extractScanSelectNode(msg.getScan_select_node())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractHalt(msg.getHalt())
        }
        if (msg.hasScan_delete_node()) {
            result["scan-delete-node"] = extractScanDeleteNode(msg.getScan_delete_node())
        }
        if (msg.hasAxis()) {
            result["axis"] = extractAxis(msg.getAxis())
        }
        if (msg.hasScan_unpause()) {
            result["scan-unpause"] = extractScanUnpause(msg.getScan_unpause())
        }
        if (msg.hasSet_mode()) {
            result["set-mode"] = extractSetMode(msg.getSet_mode())
        }
        if (msg.hasScan_refresh_node_list()) {
            result["scan-refresh-node-list"] = extractScanRefreshNodeList(msg.getScan_refresh_node_list())
        }
        if (msg.hasScan_update_node()) {
            result["scan-update-node"] = extractScanUpdateNode(msg.getScan_update_node())
        }

        return result
    }

    /**
     * Extract Transit data from RotateToGPS
     */
    private fun extractRotateToGps(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateToGPS`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["latitude"] = msg.getLatitude()
        result["longitude"] = msg.getLongitude()
        result["altitude"] = msg.getAltitude()

        return result
    }

    /**
     * Extract Transit data from ScanPause
     */
    private fun extractScanPause(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanPause`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from RotateToNDC
     */
    private fun extractRotateToNdc(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["channel"] =
            msg
                .getChannel()
                .name
                .toLowerCase()
                .replace("_", "-")
        result["x"] = msg.getX()
        result["y"] = msg.getY()

        return result
    }

    /**
     * Extract Transit data from ScanStart
     */
    private fun extractScanStart(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanStart`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetPlatformAzimuth
     */
    private fun extractSetPlatformAzimuth(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformAzimuth`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from ScanStop
     */
    private fun extractScanStop(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanStop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractStart(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$Start`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractStop(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$Stop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetOriginGPS
     */
    private fun extractSetOriginGps(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$SetOriginGPS`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["latitude"] = msg.getLatitude()
        result["longitude"] = msg.getLongitude()
        result["altitude"] = msg.getAltitude()

        return result
    }

    /**
     * Extract Transit data from ScanNext
     */
    private fun extractScanNext(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanNext`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetPlatformBank
     */
    private fun extractSetPlatformBank(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformBank`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractGetMeteo(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$GetMeteo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from setUseRotaryAsCompass
     */
    private fun extractSetUseRotaryAsCompass(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$setUseRotaryAsCompass`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["flag"] = msg.getFlag()

        return result
    }

    /**
     * Extract Transit data from ScanPrev
     */
    private fun extractScanPrev(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanPrev`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanAddNode
     */
    private fun extractScanAddNode(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode`): Map<String, Any?> {
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
     * Extract Transit data from SetPlatformElevation
     */
    private fun extractSetPlatformElevation(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$SetPlatformElevation`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from ScanSelectNode
     */
    private fun extractScanSelectNode(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanSelectNode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["index"] = msg.getIndex()

        return result
    }

    /**
     * Extract Transit data from Halt
     */
    private fun extractHalt(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$Halt`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanDeleteNode
     */
    private fun extractScanDeleteNode(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanDeleteNode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["index"] = msg.getIndex()

        return result
    }

    /**
     * Extract Transit data from Axis
     */
    private fun extractAxis(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$Axis`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasAzimuth()) {
            result["azimuth"] = extractAzimuth(msg.getAzimuth())
        }
        if (msg.hasElevation()) {
            result["elevation"] = extractElevation(msg.getElevation())
        }

        return result
    }

    /**
     * Extract Transit data from Azimuth
     */
    private fun extractAzimuth(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet_value()) {
            result["set-value"] = extractSetValue(msg.getSet_value())
        }
        if (msg.hasRotate_to()) {
            result["rotate-to"] = extractRotateTo(msg.getRotate_to())
        }
        if (msg.hasRotate()) {
            result["rotate"] = extractRotate(msg.getRotate())
        }
        if (msg.hasRelative()) {
            result["relative"] = extractRelative(msg.getRelative())
        }
        if (msg.hasRelative_set()) {
            result["relative-set"] = extractRelativeSet(msg.getRelative_set())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractHalt(msg.getHalt())
        }

        return result
    }

    /**
     * Extract Transit data from SetAzimuthValue
     */
    private fun extractSetValue(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$SetAzimuthValue`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["direction"] =
            msg
                .getDirection()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateAzimuthTo
     */
    private fun extractRotateTo(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthTo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["target-value"] = msg.getTarget_value()
        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateAzimuth
     */
    private fun extractRotate(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuth`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateAzimuthRelative
     */
    private fun extractRelative(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelative`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateAzimuthRelativeSet
     */
    private fun extractRelativeSet(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateAzimuthRelativeSet`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["direction"] =
            msg
                .getDirection()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from HaltAzimuth
     */
    private fun extractHalt(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$HaltAzimuth`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Elevation
     */
    private fun extractElevation(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$Elevation`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasSet_value()) {
            result["set-value"] = extractSetValue(msg.getSet_value())
        }
        if (msg.hasRotate_to()) {
            result["rotate-to"] = extractRotateTo(msg.getRotate_to())
        }
        if (msg.hasRotate()) {
            result["rotate"] = extractRotate(msg.getRotate())
        }
        if (msg.hasRelative()) {
            result["relative"] = extractRelative(msg.getRelative())
        }
        if (msg.hasRelative_set()) {
            result["relative-set"] = extractRelativeSet(msg.getRelative_set())
        }
        if (msg.hasHalt()) {
            result["halt"] = extractHalt(msg.getHalt())
        }

        return result
    }

    /**
     * Extract Transit data from SetElevationValue
     */
    private fun extractSetValue(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$SetElevationValue`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from RotateElevationTo
     */
    private fun extractRotateTo(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationTo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["target-value"] = msg.getTarget_value()
        result["speed"] = msg.getSpeed()

        return result
    }

    /**
     * Extract Transit data from RotateElevation
     */
    private fun extractRotate(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevation`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateElevationRelative
     */
    private fun extractRelative(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelative`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["speed"] = msg.getSpeed()
        result["direction"] =
            msg
                .getDirection()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from RotateElevationRelativeSet
     */
    private fun extractRelativeSet(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$RotateElevationRelativeSet`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()
        result["direction"] =
            msg
                .getDirection()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from HaltElevation
     */
    private fun extractHalt(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$HaltElevation`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanUnpause
     */
    private fun extractScanUnpause(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanUnpause`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetMode
     */
    private fun extractSetMode(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$SetMode`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["mode"] =
            msg
                .getMode()
                .name
                .toLowerCase()
                .replace("_", "-")

        return result
    }

    /**
     * Extract Transit data from ScanRefreshNodeList
     */
    private fun extractScanRefreshNodeList(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanRefreshNodeList`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from ScanUpdateNode
     */
    private fun extractScanUpdateNode(msg: `cmd.RotaryPlatform.JonSharedCmdRotary$ScanUpdateNode`): Map<String, Any?> {
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
     * Extract Transit data from Root
     */
    private fun extractCompass(msg: `cmd.Compass.JonSharedCmdCompass$Root`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        if (msg.hasCalibrate_cencel()) {
            result["calibrate-cencel"] = extractCalibrateCencel(msg.getCalibrate_cencel())
        }
        if (msg.hasStart()) {
            result["start"] = extractStart(msg.getStart())
        }
        if (msg.hasSet_offset_angle_elevation()) {
            result["set-offset-angle-elevation"] = extractSetOffsetAngleElevation(msg.getSet_offset_angle_elevation())
        }
        if (msg.hasStop()) {
            result["stop"] = extractStop(msg.getStop())
        }
        if (msg.hasCalibrate_next()) {
            result["calibrate-next"] = extractCalibrateNext(msg.getCalibrate_next())
        }
        if (msg.hasGet_meteo()) {
            result["get-meteo"] = extractGetMeteo(msg.getGet_meteo())
        }
        if (msg.hasSet_use_rotary_position()) {
            result["set-use-rotary-position"] = extractSetUseRotaryPosition(msg.getSet_use_rotary_position())
        }
        if (msg.hasSet_magnetic_declination()) {
            result["set-magnetic-declination"] = extractSetMagneticDeclination(msg.getSet_magnetic_declination())
        }
        if (msg.hasStart_calibrate_short()) {
            result["start-calibrate-short"] = extractStartCalibrateShort(msg.getStart_calibrate_short())
        }
        if (msg.hasStart_calibrate_long()) {
            result["start-calibrate-long"] = extractStartCalibrateLong(msg.getStart_calibrate_long())
        }
        if (msg.hasSet_offset_angle_azimuth()) {
            result["set-offset-angle-azimuth"] = extractSetOffsetAngleAzimuth(msg.getSet_offset_angle_azimuth())
        }

        return result
    }

    /**
     * Extract Transit data from CalibrateCencel
     */
    private fun extractCalibrateCencel(msg: `cmd.Compass.JonSharedCmdCompass$CalibrateCencel`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from Start
     */
    private fun extractStart(msg: `cmd.Compass.JonSharedCmdCompass$Start`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetOffsetAngleElevation
     */
    private fun extractSetOffsetAngleElevation(msg: `cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Stop
     */
    private fun extractStop(msg: `cmd.Compass.JonSharedCmdCompass$Stop`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from CalibrateNext
     */
    private fun extractCalibrateNext(msg: `cmd.Compass.JonSharedCmdCompass$CalibrateNext`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from GetMeteo
     */
    private fun extractGetMeteo(msg: `cmd.Compass.JonSharedCmdCompass$GetMeteo`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetUseRotaryPosition
     */
    private fun extractSetUseRotaryPosition(msg: `cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["flag"] = msg.getFlag()

        return result
    }

    /**
     * Extract Transit data from SetMagneticDeclination
     */
    private fun extractSetMagneticDeclination(msg: `cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from CalibrateStartShort
     */
    private fun extractStartCalibrateShort(msg: `cmd.Compass.JonSharedCmdCompass$CalibrateStartShort`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from CalibrateStartLong
     */
    private fun extractStartCalibrateLong(msg: `cmd.Compass.JonSharedCmdCompass$CalibrateStartLong`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    /**
     * Extract Transit data from SetOffsetAngleAzimuth
     */
    private fun extractSetOffsetAngleAzimuth(msg: `cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        result["value"] = msg.getValue()

        return result
    }

    /**
     * Extract Transit data from Frozen
     */
    private fun extractFrozen(msg: `cmd.JonSharedCmd$Frozen`): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        return result
    }

    // Utility conversion functions
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
class GeneratedCommandReadHandler : ReadHandler<Map<*, *>, Message> {
    override fun fromRep(rep: Map<*, *>): Message = GeneratedCommandHandlers.buildCommand(rep)
}

/**
 * Transit write handler for command messages
 */
class GeneratedCommandWriteHandler : WriteHandler<JonSharedCmd.Root> {
    override fun tag(o: JonSharedCmd.Root): String = "cmd"

    override fun rep(o: JonSharedCmd.Root): Any = GeneratedCommandHandlers.extractCommand(o)
}
