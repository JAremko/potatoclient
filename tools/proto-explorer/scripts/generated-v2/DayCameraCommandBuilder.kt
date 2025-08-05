package potatoclient.kotlin.transit.builders

import cmd.JonSharedCmd
import cmd.DayCamera.JonSharedCmdDayCamera
import com.cognitect.transit.TransitFactory

/**
 * Builder for DayCamera commands
 * Generated from protobuf specs
 */
object DayCameraCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val daycameraMsg = when (action) {
            "daycamera-prev-zoom-table-pos" -> buildPrevZoomTablePos(params)
            "daycamera-zoom" -> buildZoom(params)
            "daycamera-set-infra-red-filter" -> buildSetInfraRedFilter(params)
            "daycamera-offset" -> buildOffset(params)
            "daycamera-set-clahe-level" -> buildSetClaheLevel(params)
            "daycamera-prev-fx-mode" -> buildPrevFxMode(params)
            "daycamera-start" -> buildStart(params)
            "daycamera-move" -> buildMove(params)
            "daycamera-reset-focus" -> buildResetFocus(params)
            "daycamera-halt-all" -> buildHaltAll(params)
            "daycamera-get-pos" -> buildGetPos(params)
            "daycamera-set-digital-zoom-level" -> buildSetDigitalZoomLevel(params)
            "daycamera-stop" -> buildStop(params)
            "daycamera-reset-zoom" -> buildResetZoom(params)
            "daycamera-save-to-table-focus" -> buildSaveToTableFocus(params)
            "daycamera-photo" -> buildPhoto(params)
            "daycamera-get-meteo" -> buildGetMeteo(params)
            "daycamera-next-zoom-table-pos" -> buildNextZoomTablePos(params)
            "daycamera-focus" -> buildFocus(params)
            "daycamera-set-value" -> buildSetValue(params)
            "daycamera-set-zoom-table-value" -> buildSetZoomTableValue(params)
            "daycamera-set-fx-mode" -> buildSetFxMode(params)
            "daycamera-set-iris" -> buildSetIris(params)
            "daycamera-refresh-fx-mode" -> buildRefreshFxMode(params)
            "daycamera-halt" -> buildHalt(params)
            "daycamera-set-auto-iris" -> buildSetAutoIris(params)
            "daycamera-save-to-table" -> buildSaveToTable(params)
            "daycamera-next-fx-mode" -> buildNextFxMode(params)
            "daycamera-shift-clahe-level" -> buildShiftClaheLevel(params)
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown DayCamera command: $action")
            )
        }
        
        return daycameraMsg.map { daycamera ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setDayCamera(daycamera)
                .build()
        }
    }
    
    private fun buildPrevZoomTablePos(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setPrevZoomTablePos(JonSharedCmdDayCamera.PrevZoomTablePos.newBuilder().build())
            .build()
    )

    private fun buildZoom(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setZoom(JonSharedCmdDayCamera.Zoom.newBuilder().build())
            .build()
    )

    private fun buildSetInfraRedFilter(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetInfraRedFilter(JonSharedCmdDayCamera.SetInfraRedFilter.newBuilder().build())
            .build()
    )

    private fun buildOffset(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setOffset(JonSharedCmdDayCamera.Offset.newBuilder().build())
            .build()
    )

    private fun buildSetClaheLevel(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetClaheLevel(JonSharedCmdDayCamera.SetClaheLevel.newBuilder().build())
            .build()
    )

    private fun buildPrevFxMode(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setPrevFxMode(JonSharedCmdDayCamera.PrevFxMode.newBuilder().build())
            .build()
    )

    private fun buildStart(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setStart(JonSharedCmdDayCamera.Start.newBuilder().build())
            .build()
    )

    private fun buildMove(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setMove(JonSharedCmdDayCamera.Move.newBuilder().build())
            .build()
    )

    private fun buildResetFocus(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setResetFocus(JonSharedCmdDayCamera.ResetFocus.newBuilder().build())
            .build()
    )

    private fun buildHaltAll(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setHaltAll(JonSharedCmdDayCamera.HaltAll.newBuilder().build())
            .build()
    )

    private fun buildGetPos(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setGetPos(JonSharedCmdDayCamera.GetPos.newBuilder().build())
            .build()
    )

    private fun buildSetDigitalZoomLevel(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetDigitalZoomLevel(JonSharedCmdDayCamera.SetDigitalZoomLevel.newBuilder().build())
            .build()
    )

    private fun buildStop(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setStop(JonSharedCmdDayCamera.Stop.newBuilder().build())
            .build()
    )

    private fun buildResetZoom(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setResetZoom(JonSharedCmdDayCamera.ResetZoom.newBuilder().build())
            .build()
    )

    private fun buildSaveToTableFocus(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSaveToTableFocus(JonSharedCmdDayCamera.SaveToTableFocus.newBuilder().build())
            .build()
    )

    private fun buildPhoto(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setPhoto(JonSharedCmdDayCamera.Photo.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setGetMeteo(JonSharedCmdDayCamera.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildNextZoomTablePos(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setNextZoomTablePos(JonSharedCmdDayCamera.NextZoomTablePos.newBuilder().build())
            .build()
    )

    private fun buildFocus(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setFocus(JonSharedCmdDayCamera.Focus.newBuilder().build())
            .build()
    )

    private fun buildSetValue(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetValue(JonSharedCmdDayCamera.SetValue.newBuilder().build())
            .build()
    )

    private fun buildSetZoomTableValue(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetZoomTableValue(JonSharedCmdDayCamera.SetZoomTableValue.newBuilder().build())
            .build()
    )

    private fun buildSetFxMode(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetFxMode(JonSharedCmdDayCamera.SetFxMode.newBuilder().build())
            .build()
    )

    private fun buildSetIris(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetIris(JonSharedCmdDayCamera.SetIris.newBuilder().build())
            .build()
    )

    private fun buildRefreshFxMode(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setRefreshFxMode(JonSharedCmdDayCamera.RefreshFxMode.newBuilder().build())
            .build()
    )

    private fun buildHalt(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setHalt(JonSharedCmdDayCamera.Halt.newBuilder().build())
            .build()
    )

    private fun buildSetAutoIris(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetAutoIris(JonSharedCmdDayCamera.SetAutoIris.newBuilder().build())
            .build()
    )

    private fun buildSaveToTable(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSaveToTable(JonSharedCmdDayCamera.SaveToTable.newBuilder().build())
            .build()
    )

    private fun buildNextFxMode(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setNextFxMode(JonSharedCmdDayCamera.NextFxMode.newBuilder().build())
            .build()
    )

    private fun buildShiftClaheLevel(params: Map<*, *>): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setShiftClaheLevel(JonSharedCmdDayCamera.ShiftClaheLevel.newBuilder().build())
            .build()
    )
}
