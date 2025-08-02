package potatoclient.kotlin.transit

import cmd.HeatCamera.JonSharedCmdHeatCamera
import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory
import ser.JonSharedDataTypes

/**
 * Builder for thermal (heat) camera control commands
 */
object HeatCameraCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val cameraMsg = when (action) {
            // Basic control
            "heat-camera-start" -> buildStart()
            "heat-camera-stop" -> buildStop()
            "heat-camera-photo" -> buildPhoto()
            
            // AGC and filters
            "heat-camera-set-agc" -> buildSetAgc(params)
            "heat-camera-set-filters" -> buildSetFilters(params)
            
            // Zoom control
            "heat-camera-zoom-in" -> buildZoomIn()
            "heat-camera-zoom-out" -> buildZoomOut()
            "heat-camera-zoom-stop" -> buildZoomStop()
            
            // Focus control
            "heat-camera-focus-in" -> buildFocusIn()
            "heat-camera-focus-out" -> buildFocusOut()
            "heat-camera-focus-stop" -> buildFocusStop()
            "heat-camera-focus-step-plus" -> buildFocusStepPlus()
            "heat-camera-focus-step-minus" -> buildFocusStepMinus()
            "heat-camera-set-auto-focus" -> buildSetAutoFocus(params)
            
            // Calibration
            "heat-camera-calibrate" -> buildCalibrate()
            "heat-camera-set-calib-mode" -> buildSetCalibMode()
            
            // DDE (Digital Detail Enhancement)
            "heat-camera-set-dde-level" -> buildSetDdeLevel(params)
            "heat-camera-shift-dde" -> buildShiftDde(params)
            "heat-camera-enable-dde" -> buildEnableDde()
            "heat-camera-disable-dde" -> buildDisableDde()
            
            // FX modes
            "heat-camera-set-fx-mode" -> buildSetFxMode(params)
            "heat-camera-next-fx-mode" -> buildNextFxMode()
            "heat-camera-prev-fx-mode" -> buildPrevFxMode()
            "heat-camera-refresh-fx-mode" -> buildRefreshFxMode()
            
            // Digital zoom and enhancement
            "heat-camera-set-digital-zoom-level" -> buildSetDigitalZoomLevel(params)
            "heat-camera-set-clahe-level" -> buildSetClaheLevel(params)
            "heat-camera-shift-clahe-level" -> buildShiftClaheLevel(params)
            
            // Zoom table commands
            "heat-camera-zoom-set-table-value" -> buildZoomSetTableValue(params)
            "heat-camera-zoom-next-table-pos" -> buildZoomNextTablePos()
            "heat-camera-zoom-prev-table-pos" -> buildZoomPrevTablePos()
            
            // Utility commands
            "heat-camera-get-meteo" -> buildGetMeteo()
            "heat-camera-reset-zoom" -> buildResetZoom()
            "heat-camera-save-to-table" -> buildSaveToTable()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown heat camera command: $action")
            )
        }
        
        return cameraMsg.map { camera ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setHeatCamera(camera)
                .build()
        }
    }
    
    // Basic control
    private fun buildStart(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setStart(JonSharedCmdHeatCamera.Start.newBuilder().build())
            .build()
    )
    
    private fun buildStop(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setStop(JonSharedCmdHeatCamera.Stop.newBuilder().build())
            .build()
    )
    
    private fun buildPhoto(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setPhoto(JonSharedCmdHeatCamera.Photo.newBuilder().build())
            .build()
    )
    
    // AGC and filters
    private fun buildSetAgc(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getStringParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val agcMode = parseAgcMode(value)
        val setAgc = JonSharedCmdHeatCamera.SetAGC.newBuilder()
            .setValue(agcMode)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setSetAgc(setAgc)
                .build()
        )
    }
    
    private fun buildSetFilters(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getIntParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setFilters = JonSharedCmdHeatCamera.SetFilters.newBuilder()
            .setValue(parseFilters(value))
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setSetFilter(setFilters)
                .build()
        )
    }
    
    // Zoom control
    private fun buildZoomIn(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoomIn(JonSharedCmdHeatCamera.ZoomIn.newBuilder().build())
            .build()
    )
    
    private fun buildZoomOut(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoomOut(JonSharedCmdHeatCamera.ZoomOut.newBuilder().build())
            .build()
    )
    
    private fun buildZoomStop(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoomStop(JonSharedCmdHeatCamera.ZoomStop.newBuilder().build())
            .build()
    )
    
    // Focus control
    private fun buildFocusIn(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusIn(JonSharedCmdHeatCamera.FocusIn.newBuilder().build())
            .build()
    )
    
    private fun buildFocusOut(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusOut(JonSharedCmdHeatCamera.FocusOut.newBuilder().build())
            .build()
    )
    
    private fun buildFocusStop(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusStop(JonSharedCmdHeatCamera.FocusStop.newBuilder().build())
            .build()
    )
    
    private fun buildFocusStepPlus(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusStepPlus(JonSharedCmdHeatCamera.FocusStepPlus.newBuilder().build())
            .build()
    )
    
    private fun buildFocusStepMinus(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusStepMinus(JonSharedCmdHeatCamera.FocusStepMinus.newBuilder().build())
            .build()
    )
    
    private fun buildSetAutoFocus(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getBooleanParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setAutoFocus = JonSharedCmdHeatCamera.SetAutoFocus.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setSetAutoFocus(setAutoFocus)
                .build()
        )
    }
    
    // Calibration
    private fun buildCalibrate(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setCalibrate(JonSharedCmdHeatCamera.Calibrate.newBuilder().build())
            .build()
    )
    
    private fun buildSetCalibMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetCalibMode(JonSharedCmdHeatCamera.SetCalibMode.newBuilder().build())
            .build()
    )
    
    // DDE (Digital Detail Enhancement)
    private fun buildSetDdeLevel(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getIntParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setDde = JonSharedCmdHeatCamera.SetDDELevel.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setSetDdeLevel(setDde)
                .build()
        )
    }
    
    private fun buildShiftDde(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getIntParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val shiftDde = JonSharedCmdHeatCamera.ShiftDDE.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setShiftDde(shiftDde)
                .build()
        )
    }
    
    private fun buildEnableDde(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setEnableDde(JonSharedCmdHeatCamera.EnableDDE.newBuilder().build())
            .build()
    )
    
    private fun buildDisableDde(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setDisableDde(JonSharedCmdHeatCamera.DisableDDE.newBuilder().build())
            .build()
    )
    
    // FX modes
    private fun buildSetFxMode(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val mode = getStringParam(params, "mode")
            ?: return Result.failure(IllegalArgumentException("Missing mode parameter"))
        
        val fxMode = parseFxMode(mode)
        val setFxMode = JonSharedCmdHeatCamera.SetFxMode.newBuilder()
            .setMode(fxMode)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setSetFxMode(setFxMode)
                .build()
        )
    }
    
    private fun buildNextFxMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setNextFxMode(JonSharedCmdHeatCamera.NextFxMode.newBuilder().build())
            .build()
    )
    
    private fun buildPrevFxMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setPrevFxMode(JonSharedCmdHeatCamera.PrevFxMode.newBuilder().build())
            .build()
    )
    
    private fun buildRefreshFxMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setRefreshFxMode(JonSharedCmdHeatCamera.RefreshFxMode.newBuilder().build())
            .build()
    )
    
    // Digital zoom and enhancement
    private fun buildSetDigitalZoomLevel(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setZoom = JonSharedCmdHeatCamera.SetDigitalZoomLevel.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setSetDigitalZoomLevel(setZoom)
                .build()
        )
    }
    
    private fun buildSetClaheLevel(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setClahe = JonSharedCmdHeatCamera.SetClaheLevel.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setSetClaheLevel(setClahe)
                .build()
        )
    }
    
    private fun buildShiftClaheLevel(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val shiftClahe = JonSharedCmdHeatCamera.ShiftClaheLevel.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setShiftClaheLevel(shiftClahe)
                .build()
        )
    }
    
    // Zoom table commands
    private fun buildZoomSetTableValue(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> {
        val value = getIntParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setTable = JonSharedCmdHeatCamera.SetZoomTableValue.newBuilder()
            .setValue(value)
            .build()
        val zoom = JonSharedCmdHeatCamera.Zoom.newBuilder()
            .setSetZoomTableValue(setTable)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomNextTablePos(): Result<JonSharedCmdHeatCamera.Root> {
        val next = JonSharedCmdHeatCamera.NextZoomTablePos.newBuilder().build()
        val zoom = JonSharedCmdHeatCamera.Zoom.newBuilder()
            .setNextZoomTablePos(next)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomPrevTablePos(): Result<JonSharedCmdHeatCamera.Root> {
        val prev = JonSharedCmdHeatCamera.PrevZoomTablePos.newBuilder().build()
        val zoom = JonSharedCmdHeatCamera.Zoom.newBuilder()
            .setPrevZoomTablePos(prev)
            .build()
        
        return Result.success(
            JonSharedCmdHeatCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    // Utility commands
    private fun buildGetMeteo(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setGetMeteo(JonSharedCmdHeatCamera.GetMeteo.newBuilder().build())
            .build()
    )
    
    private fun buildResetZoom(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setResetZoom(JonSharedCmdHeatCamera.ResetZoom.newBuilder().build())
            .build()
    )
    
    private fun buildSaveToTable(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSaveToTable(JonSharedCmdHeatCamera.SaveToTable.newBuilder().build())
            .build()
    )
    
    // Helper functions
    private fun getStringParam(params: Map<*, *>, key: String): String? {
        return params[key] as? String ?: params[TransitFactory.keyword(key)] as? String
    }
    
    private fun getFloatParam(params: Map<*, *>, key: String): Float? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toFloat()
    }
    
    private fun getIntParam(params: Map<*, *>, key: String): Int? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toInt()
    }
    
    private fun getBooleanParam(params: Map<*, *>, key: String): Boolean? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return value as? Boolean
    }
    
    private fun parseFxMode(modeStr: String): JonSharedDataTypes.JonGuiDataFxModeHeat =
        when (modeStr.lowercase()) {
            "default" -> JonSharedDataTypes.JonGuiDataFxModeHeat.JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
            "a" -> JonSharedDataTypes.JonGuiDataFxModeHeat.JON_GUI_DATA_FX_MODE_HEAT_A
            "b" -> JonSharedDataTypes.JonGuiDataFxModeHeat.JON_GUI_DATA_FX_MODE_HEAT_B
            "c" -> JonSharedDataTypes.JonGuiDataFxModeHeat.JON_GUI_DATA_FX_MODE_HEAT_C
            "d" -> JonSharedDataTypes.JonGuiDataFxModeHeat.JON_GUI_DATA_FX_MODE_HEAT_D
            "e" -> JonSharedDataTypes.JonGuiDataFxModeHeat.JON_GUI_DATA_FX_MODE_HEAT_E
            "f" -> JonSharedDataTypes.JonGuiDataFxModeHeat.JON_GUI_DATA_FX_MODE_HEAT_F
            else -> JonSharedDataTypes.JonGuiDataFxModeHeat.JON_GUI_DATA_FX_MODE_HEAT_DEFAULT
        }
    
    private fun parseAgcMode(modeStr: String): JonSharedDataTypes.JonGuiDataVideoChannelHeatAGCModes =
        when (modeStr.lowercase()) {
            "1" -> JonSharedDataTypes.JonGuiDataVideoChannelHeatAGCModes.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
            "2" -> JonSharedDataTypes.JonGuiDataVideoChannelHeatAGCModes.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2
            "3" -> JonSharedDataTypes.JonGuiDataVideoChannelHeatAGCModes.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3
            else -> JonSharedDataTypes.JonGuiDataVideoChannelHeatAGCModes.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1
        }
    
    private fun parseFilters(filterNum: Int): JonSharedDataTypes.JonGuiDataVideoChannelHeatFilters =
        when (filterNum) {
            1 -> JonSharedDataTypes.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
            2 -> JonSharedDataTypes.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK
            3 -> JonSharedDataTypes.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA
            4 -> JonSharedDataTypes.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE
            else -> JonSharedDataTypes.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE
        }
}