import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";
import * as Cmd from "ts/proto/jon/index.cmd";
import * as Types from "ts/proto/jon/jon_shared_data_types";

export function heatCameraTakePhoto(): void {
    //console.log("Taking photo");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({
        photo: function (): Cmd.HeatCamera.Photo {
            return Cmd.HeatCamera.Photo.create();
        }
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraSetAgc(value: Types.JonGuiDataVideoChannelHeatAGCModes): void {
    //console.log(`Heat Camera Setting agc to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({setAgc: Cmd.HeatCamera.SetAGC.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraSetFilter(value: Types.JonGuiDataVideoChannelHeatFilters): void {
    //console.log(`Heat Camera Setting filter to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({setFilter: Cmd.HeatCamera.SetFilters.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraSetZoomTableValue(value: number): void {
    //console.log(`Heat Camera Setting optical zoom table value to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.HeatCamera.Zoom.create({setZoomTableValue: {value}});
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraSetDigitalZoomLevel(value: number): void {
    //console.log(`Heat Camera Setting digital zoom level to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({setDigitalZoomLevel: Cmd.HeatCamera.SetDigitalZoomLevel.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function stringToHeatCameraAgcMode(value: string): Types.JonGuiDataVideoChannelHeatAGCModes {
    switch (value.toLowerCase()) {
        case 'mode_1':
            return Types.JonGuiDataVideoChannelHeatAGCModes.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1;
        case 'mode_2':
            return Types.JonGuiDataVideoChannelHeatAGCModes.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2;
        case 'mode_3':
            return Types.JonGuiDataVideoChannelHeatAGCModes.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3;
        default:
            return Types.JonGuiDataVideoChannelHeatAGCModes.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED;
    }
}

export function stringToHeatCameraFilter(value: string): Types.JonGuiDataVideoChannelHeatFilters {
    switch (value.toLowerCase()) {
        case 'hot_black':
            return Types.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK;
        case 'hot_white':
            return Types.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE;
        case 'sepia':
            return Types.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA;
        default:
            return Types.JonGuiDataVideoChannelHeatFilters.JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED;
    }
}

export function heatCameraCalibrate(): void {
    //console.log("Sending heatCamera calibrate");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({calibrate: Cmd.HeatCamera.Calibrate.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraStart(): void {
    //console.log("Sending heatCamera start");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({start: Cmd.HeatCamera.Start.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraStop(): void {
    //console.log("Sending heatCamera stop");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({stop: Cmd.HeatCamera.Stop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraZoomIn(): void {
    //console.log("Sending heatCamera zoom in");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({zoomIn: Cmd.HeatCamera.ZoomIn.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraZoomOut(): void {
    //console.log("Sending heatCamera zoom out");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({zoomOut: Cmd.HeatCamera.ZoomOut.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraZoomStop(): void {
    //console.log("Sending heatCamera zoom stop");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({zoomStop: Cmd.HeatCamera.ZoomStop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

function heatCameraSetAutoFocus(value: boolean): void {
    //console.log(`Heat Camera Setting auto focus to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({setAutoFocus: Cmd.HeatCamera.SetAutoFocus.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraSetAutoFocusOn(): void {
    //console.log("Setting auto focus on");
    heatCameraSetAutoFocus(true);
}

export function heatCameraSetAutoFocusOff(): void {
    //console.log("Setting auto focus off");
    heatCameraSetAutoFocus(false);
}

export function heatCameraFocusStop(): void {
    //console.log("Sending heatCamera focus stop");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({focusStop: Cmd.HeatCamera.FocusStop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraFocusIn(): void {
    //console.log("Sending heatCamera focus in");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({focusIn: Cmd.HeatCamera.FocusIn.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraFocusOut(): void {
    //console.log("Sending heatCamera focus out");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({focusOut: Cmd.HeatCamera.FocusOut.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraFocusStepPLus(): void {
    //console.log("Sending heatCamera focus step plus");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({focusStepPlus: Cmd.HeatCamera.FocusStepPlus.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraFocusStepMinus(): void {
    //console.log("Sending heatCamera focus step minus");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({focusStepMinus: Cmd.HeatCamera.FocusStepMinus.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraNextZoomTablePos(): void {
    //console.log(`Heat Camera Setting next optical zoom table position`);
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.HeatCamera.Zoom.create({nextZoomTablePos: {}});
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraPrevZoomTablePos(): void {
    //console.log(`Heat Camera Setting previous optical zoom table position`);
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.HeatCamera.Zoom.create({prevZoomTablePos: {}});
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraResetZoom(): void {
    //console.log("Resetting heat camera zoom");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({resetZoom: Cmd.HeatCamera.ResetZoom.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function heatCameraSaveZoomToTable(): void {
    //console.log("Saving heat camera zoom to table");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({saveToTable: Cmd.HeatCamera.SaveToTable.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function getMeteo(): void {
    //console.log("Requesting camera heat meteo data");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({getMeteo: Cmd.HeatCamera.GetMeteo.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function enableDDE(): void {
    //console.log("Enabling DDE");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({enableDde: Cmd.HeatCamera.EnableDDE.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function disableDDE(): void {
    //console.log("Disabling DDE");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({disableDde: Cmd.HeatCamera.DisableDDE.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function setDDELevel(level: number): void {
    //console.log(`Setting DDE level to ${level}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({setDdeLevel: Cmd.HeatCamera.SetDDELevel.create({value: level})});
    CSShared.sendCmdMessage(rootMsg);
}

export function shiftDDELevel(shift: number): void {
    //console.log(`Shifting DDE level by ${shift}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({shiftDde: Cmd.HeatCamera.ShiftDDE.create({value: shift})});
    CSShared.sendCmdMessage(rootMsg);
}

export function setFxMode(mode: Types.JonGuiDataFxModeHeat): void {
    //console.log(`Setting FX mode to ${mode}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({setFxMode: Cmd.HeatCamera.SetFxMode.create({mode})});
    CSShared.sendCmdMessage(rootMsg);
}

export function nextFxMode(): void {
    //console.log(`Setting next FX mode`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({nextFxMode: Cmd.HeatCamera.NextFxMode.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function prevFxMode(): void {
    //console.log(`Setting previous FX mode`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({prevFxMode: Cmd.HeatCamera.PrevFxMode.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function setClaheLevel(value: number): void {
    //console.log(`Setting heat CLAHE level to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({setClaheLevel: Cmd.HeatCamera.SetClaheLevel.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function shiftClaheLevel(shift: number): void {
    //console.log(`Shifting heat CLAHE level by ${shift}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.heatCamera = Cmd.HeatCamera.Root.create({shiftClaheLevel: Cmd.HeatCamera.ShiftClaheLevel.create({value: shift})});
    CSShared.sendCmdMessage(rootMsg);
}
