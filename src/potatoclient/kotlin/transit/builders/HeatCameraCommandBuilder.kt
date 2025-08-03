package potatoclient.kotlin.transit.builders

import cmd.HeatCamera.JonSharedCmdHeatCamera
import cmd.JonSharedCmd

/**
 * Builder for HeatCamera commands
 * Generated from protobuf specs
 */
object HeatCameraCommandBuilder {
    fun build(
        action: String,
        params: Map<*, *>,
    ): Result<JonSharedCmd.Root> {
        val heatcameraMsg =
            when (action) {
                "heatcamera-prev-zoom-table-pos" -> buildPrevZoomTablePos(params)
                "heatcamera-set-dde-level" -> buildSetDdeLevel(params)
                "heatcamera-set-calib-mode" -> buildSetCalibMode(params)
                "heatcamera-zoom" -> buildZoom(params)
                "heatcamera-set-agc" -> buildSetAgc(params)
                "heatcamera-shift-dde" -> buildShiftDde(params)
                "heatcamera-set-clahe-level" -> buildSetClaheLevel(params)
                "heatcamera-disable-dde" -> buildDisableDde(params)
                "heatcamera-prev-fx-mode" -> buildPrevFxMode(params)
                "heatcamera-start" -> buildStart(params)
                "heatcamera-focus-step-minus" -> buildFocusStepMinus(params)
                "heatcamera-set-digital-zoom-level" -> buildSetDigitalZoomLevel(params)
                "heatcamera-enable-dde" -> buildEnableDde(params)
                "heatcamera-focus-stop" -> buildFocusStop(params)
                "heatcamera-stop" -> buildStop(params)
                "heatcamera-reset-zoom" -> buildResetZoom(params)
                "heatcamera-zoom-out" -> buildZoomOut(params)
                "heatcamera-photo" -> buildPhoto(params)
                "heatcamera-zoom-in" -> buildZoomIn(params)
                "heatcamera-get-meteo" -> buildGetMeteo(params)
                "heatcamera-next-zoom-table-pos" -> buildNextZoomTablePos(params)
                "heatcamera-focus-step-plus" -> buildFocusStepPlus(params)
                "heatcamera-set-value" -> buildSetValue(params)
                "heatcamera-set-zoom-table-value" -> buildSetZoomTableValue(params)
                "heatcamera-set-filters" -> buildSetFilters(params)
                "heatcamera-set-fx-mode" -> buildSetFxMode(params)
                "heatcamera-refresh-fx-mode" -> buildRefreshFxMode(params)
                "heatcamera-halt" -> buildHalt(params)
                "heatcamera-focus-out" -> buildFocusOut(params)
                "heatcamera-set-auto-focus" -> buildSetAutoFocus(params)
                "heatcamera-zoom-stop" -> buildZoomStop(params)
                "heatcamera-save-to-table" -> buildSaveToTable(params)
                "heatcamera-next-fx-mode" -> buildNextFxMode(params)
                "heatcamera-calibrate" -> buildCalibrate(params)
                "heatcamera-shift-clahe-level" -> buildShiftClaheLevel(params)
                "heatcamera-focus-in" -> buildFocusIn(params)

                else -> return Result.failure(
                    IllegalArgumentException("Unknown HeatCamera command: $action"),
                )
            }

        return heatcameraMsg.map { heatcamera ->
            JonSharedCmd.Root
                .newBuilder()
                .setProtocolVersion(1)
                .setHeatCamera(heatcamera)
                .build()
        }
    }

    private fun buildPrevZoomTablePos(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setPrevZoomTablePos(JonSharedCmdHeatCamera.PrevZoomTablePos.newBuilder().build())
                .build(),
        )

    private fun buildSetDdeLevel(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetDdeLevel(JonSharedCmdHeatCamera.SetDdeLevel.newBuilder().build())
                .build(),
        )

    private fun buildSetCalibMode(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetCalibMode(JonSharedCmdHeatCamera.SetCalibMode.newBuilder().build())
                .build(),
        )

    private fun buildZoom(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setZoom(JonSharedCmdHeatCamera.Zoom.newBuilder().build())
                .build(),
        )

    private fun buildSetAgc(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetAgc(JonSharedCmdHeatCamera.SetAgc.newBuilder().build())
                .build(),
        )

    private fun buildShiftDde(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setShiftDde(JonSharedCmdHeatCamera.ShiftDde.newBuilder().build())
                .build(),
        )

    private fun buildSetClaheLevel(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetClaheLevel(JonSharedCmdHeatCamera.SetClaheLevel.newBuilder().build())
                .build(),
        )

    private fun buildDisableDde(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setDisableDde(JonSharedCmdHeatCamera.DisableDde.newBuilder().build())
                .build(),
        )

    private fun buildPrevFxMode(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setPrevFxMode(JonSharedCmdHeatCamera.PrevFxMode.newBuilder().build())
                .build(),
        )

    private fun buildStart(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setStart(JonSharedCmdHeatCamera.Start.newBuilder().build())
                .build(),
        )

    private fun buildFocusStepMinus(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setFocusStepMinus(JonSharedCmdHeatCamera.FocusStepMinus.newBuilder().build())
                .build(),
        )

    private fun buildSetDigitalZoomLevel(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetDigitalZoomLevel(JonSharedCmdHeatCamera.SetDigitalZoomLevel.newBuilder().build())
                .build(),
        )

    private fun buildEnableDde(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setEnableDde(JonSharedCmdHeatCamera.EnableDde.newBuilder().build())
                .build(),
        )

    private fun buildFocusStop(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setFocusStop(JonSharedCmdHeatCamera.FocusStop.newBuilder().build())
                .build(),
        )

    private fun buildStop(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setStop(JonSharedCmdHeatCamera.Stop.newBuilder().build())
                .build(),
        )

    private fun buildResetZoom(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setResetZoom(JonSharedCmdHeatCamera.ResetZoom.newBuilder().build())
                .build(),
        )

    private fun buildZoomOut(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setZoomOut(JonSharedCmdHeatCamera.ZoomOut.newBuilder().build())
                .build(),
        )

    private fun buildPhoto(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setPhoto(JonSharedCmdHeatCamera.Photo.newBuilder().build())
                .build(),
        )

    private fun buildZoomIn(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setZoomIn(JonSharedCmdHeatCamera.ZoomIn.newBuilder().build())
                .build(),
        )

    private fun buildGetMeteo(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setGetMeteo(JonSharedCmdHeatCamera.GetMeteo.newBuilder().build())
                .build(),
        )

    private fun buildNextZoomTablePos(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setNextZoomTablePos(JonSharedCmdHeatCamera.NextZoomTablePos.newBuilder().build())
                .build(),
        )

    private fun buildFocusStepPlus(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setFocusStepPlus(JonSharedCmdHeatCamera.FocusStepPlus.newBuilder().build())
                .build(),
        )

    private fun buildSetValue(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetValue(JonSharedCmdHeatCamera.SetValue.newBuilder().build())
                .build(),
        )

    private fun buildSetZoomTableValue(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetZoomTableValue(JonSharedCmdHeatCamera.SetZoomTableValue.newBuilder().build())
                .build(),
        )

    private fun buildSetFilters(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetFilters(JonSharedCmdHeatCamera.SetFilters.newBuilder().build())
                .build(),
        )

    private fun buildSetFxMode(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetFxMode(JonSharedCmdHeatCamera.SetFxMode.newBuilder().build())
                .build(),
        )

    private fun buildRefreshFxMode(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setRefreshFxMode(JonSharedCmdHeatCamera.RefreshFxMode.newBuilder().build())
                .build(),
        )

    private fun buildHalt(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setHalt(JonSharedCmdHeatCamera.Halt.newBuilder().build())
                .build(),
        )

    private fun buildFocusOut(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setFocusOut(JonSharedCmdHeatCamera.FocusOut.newBuilder().build())
                .build(),
        )

    private fun buildSetAutoFocus(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSetAutoFocus(JonSharedCmdHeatCamera.SetAutoFocus.newBuilder().build())
                .build(),
        )

    private fun buildZoomStop(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setZoomStop(JonSharedCmdHeatCamera.ZoomStop.newBuilder().build())
                .build(),
        )

    private fun buildSaveToTable(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setSaveToTable(JonSharedCmdHeatCamera.SaveToTable.newBuilder().build())
                .build(),
        )

    private fun buildNextFxMode(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setNextFxMode(JonSharedCmdHeatCamera.NextFxMode.newBuilder().build())
                .build(),
        )

    private fun buildCalibrate(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setCalibrate(JonSharedCmdHeatCamera.Calibrate.newBuilder().build())
                .build(),
        )

    private fun buildShiftClaheLevel(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setShiftClaheLevel(JonSharedCmdHeatCamera.ShiftClaheLevel.newBuilder().build())
                .build(),
        )

    private fun buildFocusIn(params: Map<*, *>): Result<JonSharedCmdHeatCamera.Root> =
        Result.success(
            JonSharedCmdHeatCamera.Root
                .newBuilder()
                .setFocusIn(JonSharedCmdHeatCamera.FocusIn.newBuilder().build())
                .build(),
        )
}
