package potatoclient.kotlin.transit

import cmd.DayCamera.JonSharedCmdDayCamera
import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory
import ser.JonSharedDataTypes

/**
 * Builder for day camera control commands
 */
object DayCameraCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val cameraMsg = when (action) {
            // Basic control
            "day-camera-start" -> buildStart()
            "day-camera-stop" -> buildStop()
            "day-camera-photo" -> buildPhoto()
            "day-camera-halt-all" -> buildHaltAll()
            
            // Iris control
            "day-camera-set-iris" -> buildSetIris(params)
            "day-camera-set-auto-iris" -> buildSetAutoIris(params)
            
            // Filters
            "day-camera-set-infra-red-filter" -> buildSetInfraRedFilter(params)
            
            // FX modes
            "day-camera-set-fx-mode" -> buildSetFxMode(params)
            "day-camera-next-fx-mode" -> buildNextFxMode()
            "day-camera-prev-fx-mode" -> buildPrevFxMode()
            "day-camera-refresh-fx-mode" -> buildRefreshFxMode()
            
            // Digital zoom and enhancement
            "day-camera-set-digital-zoom-level" -> buildSetDigitalZoomLevel(params)
            "day-camera-set-clahe-level" -> buildSetClaheLevel(params)
            "day-camera-shift-clahe-level" -> buildShiftClaheLevel(params)
            
            // Meteorological data
            "day-camera-get-meteo" -> buildGetMeteo()
            
            // Focus commands
            "day-camera-focus-set-value" -> buildFocusSetValue(params)
            "day-camera-focus-move" -> buildFocusMove(params)
            "day-camera-focus-halt" -> buildFocusHalt()
            "day-camera-focus-offset" -> buildFocusOffset(params)
            "day-camera-focus-reset" -> buildFocusReset()
            "day-camera-focus-save-to-table" -> buildFocusSaveToTable()
            
            // Zoom commands
            "day-camera-zoom-set-value" -> buildZoomSetValue(params)
            "day-camera-zoom-move" -> buildZoomMove(params)
            "day-camera-zoom-halt" -> buildZoomHalt()
            "day-camera-zoom-set-table-value" -> buildZoomSetTableValue(params)
            "day-camera-zoom-next-table-pos" -> buildZoomNextTablePos()
            "day-camera-zoom-prev-table-pos" -> buildZoomPrevTablePos()
            "day-camera-zoom-offset" -> buildZoomOffset(params)
            "day-camera-zoom-reset" -> buildZoomReset()
            "day-camera-zoom-save-to-table" -> buildZoomSaveToTable()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown day camera command: $action")
            )
        }
        
        return cameraMsg.map { camera ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setDayCamera(camera)
                .build()
        }
    }
    
    // Basic control
    private fun buildStart(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setStart(JonSharedCmdDayCamera.Start.newBuilder().build())
            .build()
    )
    
    private fun buildStop(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setStop(JonSharedCmdDayCamera.Stop.newBuilder().build())
            .build()
    )
    
    private fun buildPhoto(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setPhoto(JonSharedCmdDayCamera.Photo.newBuilder().build())
            .build()
    )
    
    private fun buildHaltAll(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setHaltAll(JonSharedCmdDayCamera.HaltAll.newBuilder().build())
            .build()
    )
    
    // Iris control
    private fun buildSetIris(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setIris = JonSharedCmdDayCamera.SetIris.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setSetIris(setIris)
                .build()
        )
    }
    
    private fun buildSetAutoIris(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getBooleanParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setAutoIris = JonSharedCmdDayCamera.SetAutoIris.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setSetAutoIris(setAutoIris)
                .build()
        )
    }
    
    // Filters
    private fun buildSetInfraRedFilter(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getBooleanParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setFilter = JonSharedCmdDayCamera.SetInfraRedFilter.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setSetInfraRedFilter(setFilter)
                .build()
        )
    }
    
    // FX modes
    private fun buildSetFxMode(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val mode = getStringParam(params, "mode")
            ?: return Result.failure(IllegalArgumentException("Missing mode parameter"))
        
        val fxMode = parseFxMode(mode)
        val setFxMode = JonSharedCmdDayCamera.SetFxMode.newBuilder()
            .setMode(fxMode)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setSetFxMode(setFxMode)
                .build()
        )
    }
    
    private fun buildNextFxMode(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setNextFxMode(JonSharedCmdDayCamera.NextFxMode.newBuilder().build())
            .build()
    )
    
    private fun buildPrevFxMode(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setPrevFxMode(JonSharedCmdDayCamera.PrevFxMode.newBuilder().build())
            .build()
    )
    
    private fun buildRefreshFxMode(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setRefreshFxMode(JonSharedCmdDayCamera.RefreshFxMode.newBuilder().build())
            .build()
    )
    
    // Digital zoom and enhancement
    private fun buildSetDigitalZoomLevel(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setZoom = JonSharedCmdDayCamera.SetDigitalZoomLevel.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setSetDigitalZoomLevel(setZoom)
                .build()
        )
    }
    
    private fun buildSetClaheLevel(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setClahe = JonSharedCmdDayCamera.SetClaheLevel.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setSetClaheLevel(setClahe)
                .build()
        )
    }
    
    private fun buildShiftClaheLevel(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val shiftClahe = JonSharedCmdDayCamera.ShiftClaheLevel.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setShiftClaheLevel(shiftClahe)
                .build()
        )
    }
    
    // Meteorological data
    private fun buildGetMeteo(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setGetMeteo(JonSharedCmdDayCamera.GetMeteo.newBuilder().build())
            .build()
    )
    
    // Focus commands
    private fun buildFocusSetValue(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setValue = JonSharedCmdDayCamera.SetValue.newBuilder()
            .setValue(value)
            .build()
        val focus = JonSharedCmdDayCamera.Focus.newBuilder()
            .setSetValue(setValue)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setFocus(focus)
                .build()
        )
    }
    
    private fun buildFocusMove(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val targetValue = getFloatParam(params, "target-value")
            ?: return Result.failure(IllegalArgumentException("Missing target-value parameter"))
        val speed = getFloatParam(params, "speed")
            ?: return Result.failure(IllegalArgumentException("Missing speed parameter"))
        
        val move = JonSharedCmdDayCamera.Move.newBuilder()
            .setTargetValue(targetValue)
            .setSpeed(speed)
            .build()
        val focus = JonSharedCmdDayCamera.Focus.newBuilder()
            .setMove(move)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setFocus(focus)
                .build()
        )
    }
    
    private fun buildFocusHalt(): Result<JonSharedCmdDayCamera.Root> {
        val halt = JonSharedCmdDayCamera.Halt.newBuilder().build()
        val focus = JonSharedCmdDayCamera.Focus.newBuilder()
            .setHalt(halt)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setFocus(focus)
                .build()
        )
    }
    
    private fun buildFocusOffset(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val offsetValue = getFloatParam(params, "offset-value")
            ?: return Result.failure(IllegalArgumentException("Missing offset-value parameter"))
        
        val offset = JonSharedCmdDayCamera.Offset.newBuilder()
            .setOffsetValue(offsetValue)
            .build()
        val focus = JonSharedCmdDayCamera.Focus.newBuilder()
            .setOffset(offset)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setFocus(focus)
                .build()
        )
    }
    
    private fun buildFocusReset(): Result<JonSharedCmdDayCamera.Root> {
        val reset = JonSharedCmdDayCamera.ResetFocus.newBuilder().build()
        val focus = JonSharedCmdDayCamera.Focus.newBuilder()
            .setResetFocus(reset)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setFocus(focus)
                .build()
        )
    }
    
    private fun buildFocusSaveToTable(): Result<JonSharedCmdDayCamera.Root> {
        val save = JonSharedCmdDayCamera.SaveToTableFocus.newBuilder().build()
        val focus = JonSharedCmdDayCamera.Focus.newBuilder()
            .setSaveToTableFocus(save)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setFocus(focus)
                .build()
        )
    }
    
    // Zoom commands
    private fun buildZoomSetValue(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setValue = JonSharedCmdDayCamera.SetValue.newBuilder()
            .setValue(value)
            .build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setSetValue(setValue)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomMove(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val targetValue = getFloatParam(params, "target-value")
            ?: return Result.failure(IllegalArgumentException("Missing target-value parameter"))
        val speed = getFloatParam(params, "speed")
            ?: return Result.failure(IllegalArgumentException("Missing speed parameter"))
        
        val move = JonSharedCmdDayCamera.Move.newBuilder()
            .setTargetValue(targetValue)
            .setSpeed(speed)
            .build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setMove(move)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomHalt(): Result<JonSharedCmdDayCamera.Root> {
        val halt = JonSharedCmdDayCamera.Halt.newBuilder().build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setHalt(halt)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomSetTableValue(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val value = getIntParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setTable = JonSharedCmdDayCamera.SetZoomTableValue.newBuilder()
            .setValue(value)
            .build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setSetZoomTableValue(setTable)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomNextTablePos(): Result<JonSharedCmdDayCamera.Root> {
        val next = JonSharedCmdDayCamera.NextZoomTablePos.newBuilder().build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setNextZoomTablePos(next)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomPrevTablePos(): Result<JonSharedCmdDayCamera.Root> {
        val prev = JonSharedCmdDayCamera.PrevZoomTablePos.newBuilder().build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setPrevZoomTablePos(prev)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomOffset(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> {
        val offsetValue = getFloatParam(params, "offset-value")
            ?: return Result.failure(IllegalArgumentException("Missing offset-value parameter"))
        
        val offset = JonSharedCmdDayCamera.Offset.newBuilder()
            .setOffsetValue(offsetValue)
            .build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setOffset(offset)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomReset(): Result<JonSharedCmdDayCamera.Root> {
        val reset = JonSharedCmdDayCamera.ResetZoom.newBuilder().build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setResetZoom(reset)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
    private fun buildZoomSaveToTable(): Result<JonSharedCmdDayCamera.Root> {
        val save = JonSharedCmdDayCamera.SaveToTable.newBuilder().build()
        val zoom = JonSharedCmdDayCamera.Zoom.newBuilder()
            .setSaveToTable(save)
            .build()
        
        return Result.success(
            JonSharedCmdDayCamera.Root.newBuilder()
                .setZoom(zoom)
                .build()
        )
    }
    
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
    
    private fun parseFxMode(modeStr: String): JonSharedDataTypes.JonGuiDataFxModeDay =
        when (modeStr.lowercase()) {
            "default" -> JonSharedDataTypes.JonGuiDataFxModeDay.JON_GUI_DATA_FX_MODE_DAY_DEFAULT
            "a" -> JonSharedDataTypes.JonGuiDataFxModeDay.JON_GUI_DATA_FX_MODE_DAY_A
            "b" -> JonSharedDataTypes.JonGuiDataFxModeDay.JON_GUI_DATA_FX_MODE_DAY_B
            "c" -> JonSharedDataTypes.JonGuiDataFxModeDay.JON_GUI_DATA_FX_MODE_DAY_C
            "d" -> JonSharedDataTypes.JonGuiDataFxModeDay.JON_GUI_DATA_FX_MODE_DAY_D
            "e" -> JonSharedDataTypes.JonGuiDataFxModeDay.JON_GUI_DATA_FX_MODE_DAY_E
            "f" -> JonSharedDataTypes.JonGuiDataFxModeDay.JON_GUI_DATA_FX_MODE_DAY_F
            else -> JonSharedDataTypes.JonGuiDataFxModeDay.JON_GUI_DATA_FX_MODE_DAY_DEFAULT
        }
}