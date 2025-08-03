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
            "daycamera-prev-zoom-table-pos" -> buildPrevZoomTablePos()
            "daycamera-zoom" -> buildZoom()
            "daycamera-set-infra-red-filter" -> buildSetInfraRedFilter()
            "daycamera-offset" -> buildOffset()
            "daycamera-set-clahe-level" -> buildSetClaheLevel()
            "daycamera-prev-fx-mode" -> buildPrevFxMode()
            "daycamera-start" -> buildStart()
            "daycamera-move" -> buildMove()
            "daycamera-reset-focus" -> buildResetFocus()
            "daycamera-halt-all" -> buildHaltAll()
            "daycamera-get-pos" -> buildGetPos()
            "daycamera-set-digital-zoom-level" -> buildSetDigitalZoomLevel()
            "daycamera-stop" -> buildStop()
            "daycamera-reset-zoom" -> buildResetZoom()
            "daycamera-save-to-table-focus" -> buildSaveToTableFocus()
            "daycamera-photo" -> buildPhoto()
            "daycamera-get-meteo" -> buildGetMeteo()
            "daycamera-next-zoom-table-pos" -> buildNextZoomTablePos()
            "daycamera-focus" -> buildFocus()
            "daycamera-set-value" -> buildSetValue()
            "daycamera-set-zoom-table-value" -> buildSetZoomTableValue()
            "daycamera-set-fx-mode" -> buildSetFxMode()
            "daycamera-set-iris" -> buildSetIris()
            "daycamera-refresh-fx-mode" -> buildRefreshFxMode()
            "daycamera-halt" -> buildHalt()
            "daycamera-set-auto-iris" -> buildSetAutoIris()
            "daycamera-save-to-table" -> buildSaveToTable()
            "daycamera-next-fx-mode" -> buildNextFxMode()
            "daycamera-shift-clahe-level" -> buildShiftClaheLevel()
            
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
    
    private fun buildPrevZoomTablePos(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setPrevZoomTablePos(JonSharedCmdDayCamera.PrevZoomTablePos.newBuilder().build())
            .build()
    )

    private fun buildZoom(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setZoom(JonSharedCmdDayCamera.Zoom.newBuilder().build())
            .build()
    )

    private fun buildSetInfraRedFilter(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetInfraRedFilter(JonSharedCmdDayCamera.SetInfraRedFilter.newBuilder().build())
            .build()
    )

    private fun buildOffset(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setOffset(JonSharedCmdDayCamera.Offset.newBuilder().build())
            .build()
    )

    private fun buildSetClaheLevel(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetClaheLevel(JonSharedCmdDayCamera.SetClaheLevel.newBuilder().build())
            .build()
    )

    private fun buildPrevFxMode(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setPrevFxMode(JonSharedCmdDayCamera.PrevFxMode.newBuilder().build())
            .build()
    )

    private fun buildStart(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setStart(JonSharedCmdDayCamera.Start.newBuilder().build())
            .build()
    )

    private fun buildMove(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setMove(JonSharedCmdDayCamera.Move.newBuilder().build())
            .build()
    )

    private fun buildResetFocus(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setResetFocus(JonSharedCmdDayCamera.ResetFocus.newBuilder().build())
            .build()
    )

    private fun buildHaltAll(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setHaltAll(JonSharedCmdDayCamera.HaltAll.newBuilder().build())
            .build()
    )

    private fun buildGetPos(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setGetPos(JonSharedCmdDayCamera.GetPos.newBuilder().build())
            .build()
    )

    private fun buildSetDigitalZoomLevel(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetDigitalZoomLevel(JonSharedCmdDayCamera.SetDigitalZoomLevel.newBuilder().build())
            .build()
    )

    private fun buildStop(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setStop(JonSharedCmdDayCamera.Stop.newBuilder().build())
            .build()
    )

    private fun buildResetZoom(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setResetZoom(JonSharedCmdDayCamera.ResetZoom.newBuilder().build())
            .build()
    )

    private fun buildSaveToTableFocus(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSaveToTableFocus(JonSharedCmdDayCamera.SaveToTableFocus.newBuilder().build())
            .build()
    )

    private fun buildPhoto(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setPhoto(JonSharedCmdDayCamera.Photo.newBuilder().build())
            .build()
    )

    private fun buildGetMeteo(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setGetMeteo(JonSharedCmdDayCamera.GetMeteo.newBuilder().build())
            .build()
    )

    private fun buildNextZoomTablePos(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setNextZoomTablePos(JonSharedCmdDayCamera.NextZoomTablePos.newBuilder().build())
            .build()
    )

    private fun buildFocus(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setFocus(JonSharedCmdDayCamera.Focus.newBuilder().build())
            .build()
    )

    private fun buildSetValue(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetValue(JonSharedCmdDayCamera.SetValue.newBuilder().build())
            .build()
    )

    private fun buildSetZoomTableValue(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetZoomTableValue(JonSharedCmdDayCamera.SetZoomTableValue.newBuilder().build())
            .build()
    )

    private fun buildSetFxMode(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetFxMode(JonSharedCmdDayCamera.SetFxMode.newBuilder().build())
            .build()
    )

    private fun buildSetIris(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetIris(JonSharedCmdDayCamera.SetIris.newBuilder().build())
            .build()
    )

    private fun buildRefreshFxMode(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setRefreshFxMode(JonSharedCmdDayCamera.RefreshFxMode.newBuilder().build())
            .build()
    )

    private fun buildHalt(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setHalt(JonSharedCmdDayCamera.Halt.newBuilder().build())
            .build()
    )

    private fun buildSetAutoIris(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSetAutoIris(JonSharedCmdDayCamera.SetAutoIris.newBuilder().build())
            .build()
    )

    private fun buildSaveToTable(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setSaveToTable(JonSharedCmdDayCamera.SaveToTable.newBuilder().build())
            .build()
    )

    private fun buildNextFxMode(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setNextFxMode(JonSharedCmdDayCamera.NextFxMode.newBuilder().build())
            .build()
    )

    private fun buildShiftClaheLevel(): Result<JonSharedCmdDayCamera.Root> = Result.success(
        JonSharedCmdDayCamera.Root.newBuilder()
            .setShiftClaheLevel(JonSharedCmdDayCamera.ShiftClaheLevel.newBuilder().build())
            .build()
    )
}
