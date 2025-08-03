package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.HeatCamera.JonSharedCmdHeatCamera
import com.cognitect.transit.TransitFactory

/**
 * Builder for HeatCamera commands
 * Generated from protobuf specs
 */
object HeatCameraCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val heatcameraMsg = when (action) {
            "heatcamera-prev-zoom-table-pos" -> buildPrevZoomTablePos()
            "heatcamera-set-dde-level" -> buildSetDdeLevel()
            "heatcamera-set-calib-mode" -> buildSetCalibMode()
            "heatcamera-zoom" -> buildZoom()
            "heatcamera-set-agc" -> buildSetAgc()
            "heatcamera-shift-dde" -> buildShiftDde()
            "heatcamera-set-clahe-level" -> buildSetClaheLevel()
            "heatcamera-disable-dde" -> buildDisableDde()
            "heatcamera-prev-fx-mode" -> buildPrevFxMode()
            "heatcamera-start" -> buildStart()
            "heatcamera-focus-step-minus" -> buildFocusStepMinus()
            "heatcamera-set-digital-zoom-level" -> buildSetDigitalZoomLevel()
            "heatcamera-enable-dde" -> buildEnableDde()
            "heatcamera-focus-stop" -> buildFocusStop()
            "heatcamera-stop" -> buildStop()
            "heatcamera-reset-zoom" -> buildResetZoom()
            "heatcamera-zoom-out" -> buildZoomOut()
            "heatcamera-photo" -> buildPhoto()
            "heatcamera-zoom-in" -> buildZoomIn()
            "heatcamera-get-meteo" -> buildGetMeteo()
            "heatcamera-next-zoom-table-pos" -> buildNextZoomTablePos()
            "heatcamera-focus-step-plus" -> buildFocusStepPlus()
            "heatcamera-set-value" -> buildSetValue()
            "heatcamera-set-zoom-table-value" -> buildSetZoomTableValue()
            "heatcamera-set-filters" -> buildSetFilters()
            "heatcamera-set-fx-mode" -> buildSetFxMode()
            "heatcamera-refresh-fx-mode" -> buildRefreshFxMode()
            "heatcamera-halt" -> buildHalt()
            "heatcamera-focus-out" -> buildFocusOut()
            "heatcamera-set-auto-focus" -> buildSetAutoFocus()
            "heatcamera-zoom-stop" -> buildZoomStop()
            "heatcamera-save-to-table" -> buildSaveToTable()
            "heatcamera-next-fx-mode" -> buildNextFxMode()
            "heatcamera-calibrate" -> buildCalibrate()
            "heatcamera-shift-clahe-level" -> buildShiftClaheLevel()
            "heatcamera-focus-in" -> buildFocusIn()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown HeatCamera command: $action")
            )
        }
        
        return heatcameraMsg.map { heatcamera ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setHeatCamera(heatcamera)
                .build()
        }
    }
    
    private fun buildPrevZoomTablePos(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setPrevZoomTablePos(JonSharedCmdHeatCamera.PrevZoomTablePos.newBuilder().build())
            .build()
    )

    private fun buildSetDdeLevel(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetDdeLevel(JonSharedCmdHeatCamera.SetDdeLevel.newBuilder().build())
            .build()
    )

    private fun buildSetCalibMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetCalibMode(JonSharedCmdHeatCamera.SetCalibMode.newBuilder().build())
            .build()
    )

    private fun buildZoom(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoom(JonSharedCmdHeatCamera.Zoom.newBuilder().build())
            .build()
    )

    private fun buildSetAgc(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetAgc(JonSharedCmdHeatCamera.SetAgc.newBuilder().build())
            .build()
    )

    private fun buildShiftDde(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setShiftDde(JonSharedCmdHeatCamera.ShiftDde.newBuilder().build())
            .build()
    )

    private fun buildSetClaheLevel(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetClaheLevel(JonSharedCmdHeatCamera.SetClaheLevel.newBuilder().build())
            .build()
    )

    private fun buildDisableDde(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setDisableDde(JonSharedCmdHeatCamera.DisableDde.newBuilder().build())
            .build()
    )

    private fun buildPrevFxMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setPrevFxMode(JonSharedCmdHeatCamera.PrevFxMode.newBuilder().build())
            .build()
    )

    private fun buildStart(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setStart(JonSharedCmdHeatCamera.Start.newBuilder().build())
            .build()
    )

    private fun buildFocusStepMinus(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusStepMinus(JonSharedCmdHeatCamera.FocusStepMinus.newBuilder().build())
            .build()
    )

    private fun buildSetDigitalZoomLevel(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetDigitalZoomLevel(JonSharedCmdHeatCamera.SetDigitalZoomLevel.newBuilder().build())
            .build()
    )

    private fun buildEnableDde(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setEnableDde(JonSharedCmdHeatCamera.EnableDde.newBuilder().build())
            .build()
    )

    private fun buildFocusStop(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusStop(JonSharedCmdHeatCamera.FocusStop.newBuilder().build())
            .build()
    )

    private fun buildStop(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setStop(JonSharedCmdHeatCamera.Stop.newBuilder().build())
            .build()
    )

    private fun buildResetZoom(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setResetZoom(JonSharedCmdHeatCamera.ResetZoom.newBuilder().build())
            .build()
    )

    private fun buildZoomOut(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoomOut(JonSharedCmdHeatCamera.ZoomOut.newBuilder().build())
            .build()
    )

    private fun buildPhoto(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setPhoto(JonSharedCmdHeatCamera.Photo.newBuilder().build())
            .build()
    )

    private fun buildZoomIn(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoomIn(JonSharedCmdHeatCamera.ZoomIn.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setGetMeteo(JonSharedCmdHeatCamera.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildNextZoomTablePos(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setNextZoomTablePos(JonSharedCmdHeatCamera.NextZoomTablePos.newBuilder().build())
            .build()
    )

    private fun buildFocusStepPlus(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusStepPlus(JonSharedCmdHeatCamera.FocusStepPlus.newBuilder().build())
            .build()
    )

    private fun buildSetValue(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetValue(JonSharedCmdHeatCamera.SetValue.newBuilder().build())
            .build()
    )

    private fun buildSetZoomTableValue(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetZoomTableValue(JonSharedCmdHeatCamera.SetZoomTableValue.newBuilder().build())
            .build()
    )

    private fun buildSetFilters(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetFilters(JonSharedCmdHeatCamera.SetFilters.newBuilder().build())
            .build()
    )

    private fun buildSetFxMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetFxMode(JonSharedCmdHeatCamera.SetFxMode.newBuilder().build())
            .build()
    )

    private fun buildRefreshFxMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setRefreshFxMode(JonSharedCmdHeatCamera.RefreshFxMode.newBuilder().build())
            .build()
    )

    private fun buildHalt(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setHalt(JonSharedCmdHeatCamera.Halt.newBuilder().build())
            .build()
    )

    private fun buildFocusOut(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusOut(JonSharedCmdHeatCamera.FocusOut.newBuilder().build())
            .build()
    )

    private fun buildSetAutoFocus(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSetAutoFocus(JonSharedCmdHeatCamera.SetAutoFocus.newBuilder().build())
            .build()
    )

    private fun buildZoomStop(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setZoomStop(JonSharedCmdHeatCamera.ZoomStop.newBuilder().build())
            .build()
    )

    private fun buildSaveToTable(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setSaveToTable(JonSharedCmdHeatCamera.SaveToTable.newBuilder().build())
            .build()
    )

    private fun buildNextFxMode(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setNextFxMode(JonSharedCmdHeatCamera.NextFxMode.newBuilder().build())
            .build()
    )

    private fun buildCalibrate(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setCalibrate(JonSharedCmdHeatCamera.Calibrate.newBuilder().build())
            .build()
    )

    private fun buildShiftClaheLevel(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setShiftClaheLevel(JonSharedCmdHeatCamera.ShiftClaheLevel.newBuilder().build())
            .build()
    )

    private fun buildFocusIn(): Result<JonSharedCmdHeatCamera.Root> = Result.success(
        JonSharedCmdHeatCamera.Root.newBuilder()
            .setFocusIn(JonSharedCmdHeatCamera.FocusIn.newBuilder().build())
            .build()
    )
}
