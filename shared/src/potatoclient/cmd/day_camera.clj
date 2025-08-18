import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";
import * as Cmd from "ts/proto/jon/index.cmd";
import * as Types from "ts/proto/jon/jon_shared_data_types";

export function dayCameraSetInfraRedFilter(value: boolean): void {
    //console.log(`Day Camera Setting infra red filter to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({setInfraRedFilter: {value}});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraSetIris(value: number): void {
    //console.log(`Day Camera Setting iris to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({setIris: {value}});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraSetAutoIris(value: boolean): void {
    //console.log(`Day Camera Setting auto iris to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({setAutoIris: {value}});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraTakePhoto(): void {
    //console.log("Day Camera Taking photo");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({
        photo: function (): Cmd.DayCamera.Photo {
            return Cmd.DayCamera.Photo.create();
        }
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraStart(): void {
    //console.log("Day Camera Sending dayCamera start");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({start: Cmd.DayCamera.Start.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraStop(): void {
    //console.log("Day Camera Sending dayCamera stop");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({stop: Cmd.DayCamera.Stop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraSetFocus(value: number): void {
    //console.log(`Day Camera Setting day camera focus value to ${value}`);
    let focus = Cmd.DayCamera.Focus.create({setValue: {value}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({focus});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraMoveFocus(targetValue: number, speed: number): void {
    //console.log(`Moving day camera focus to ${targetValue} at speed ${speed}`);
    let focus = Cmd.DayCamera.Focus.create({move: {targetValue, speed}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({focus});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraHaltFocus(): void {
    //console.log("Halting day camera focus");
    let focus = Cmd.DayCamera.Focus.create({halt: {}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({focus});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraOffsetFocus(offsetValue: number): void {
    //console.log(`Offsetting day camera focus by ${offsetValue}`);
    let focus = Cmd.DayCamera.Focus.create({offset: {offsetValue}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({focus});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraResetFocus(): void {
    //console.log("Resetting day camera focus");
    let focus = Cmd.DayCamera.Focus.create({resetFocus: {}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({focus});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraSaveFocusToTable(): void {
    //console.log("Saving day camera focus to table");
    let focus = Cmd.DayCamera.Focus.create({saveToTableFocus: {}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({focus});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraSetZoom(value: number): void {
    //console.log(`Day Camera Setting day camera zoom value to ${value}`);
    let zoom = Cmd.DayCamera.Zoom.create({setValue: {value}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraMoveZoom(targetValue: number, speed: number): void {
    //console.log(`Moving day camera zoom to ${targetValue} at speed ${speed}`);
    let zoom = Cmd.DayCamera.Zoom.create({move: {targetValue, speed}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraHaltZoom(): void {
    //console.log("Halting day camera zoom");
    let zoom = Cmd.DayCamera.Zoom.create({halt: {}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraOffsetZoom(offsetValue: number): void {
    //console.log(`Offsetting day camera zoom by ${offsetValue}`);
    let zoom = Cmd.DayCamera.Zoom.create({offset: {offsetValue}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraResetZoom(): void {
    //console.log("Resetting day camera zoom");
    let zoom = Cmd.DayCamera.Zoom.create({resetZoom: {}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraSaveZoomToTable(): void {
    //console.log("Saving day camera zoom to table");
    let zoom = Cmd.DayCamera.Zoom.create({saveToTable: {}});
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraSetZoomTableValue(value: number): void {
    //console.log(`Day Camera Setting optical zoom table value to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.DayCamera.Zoom.create({setZoomTableValue: {value}});
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraSetDigitalZoomLevel(value: number): void {
    //console.log(`Day Camera Setting digital zoom level to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.DayCamera.Root.create({setDigitalZoomLevel: {value}});
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({setDigitalZoomLevel: Cmd.DayCamera.SetDigitalZoomLevel.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraNextZoomTablePos(): void {
    //console.log(`Day Camera Setting next optical zoom table position`);
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.DayCamera.Zoom.create({nextZoomTablePos: {}});
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraPrevZoomTablePos(): void {
    //console.log(`Day Camera Setting previous optical zoom table position`);
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.DayCamera.Zoom.create({prevZoomTablePos: {}});
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function getMeteo(): void {
    //console.log("Requesting camera day meteo data");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({getMeteo: Cmd.DayCamera.GetMeteo.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function setFxMode(mode: Types.JonGuiDataFxModeDay): void {
    //console.log(`Setting FX mode to ${mode}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({setFxMode: Cmd.DayCamera.SetFxMode.create({mode})});
    CSShared.sendCmdMessage(rootMsg);
}

export function nextFxMode(): void {
    //console.log(`Setting next FX mode`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({nextFxMode: Cmd.DayCamera.NextFxMode.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function prevFxMode(): void {
    //console.log(`Setting previous FX mode`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({prevFxMode: Cmd.DayCamera.PrevFxMode.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function setClaheLevel(value: number): void {
    //console.log(`Setting day CLAHE level to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({setClaheLevel: Cmd.DayCamera.SetClaheLevel.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function  shiftClaheLevel(shift: number): void {
    //console.log(`Shifting day CLAHE level by ${shift}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({shiftClaheLevel: Cmd.DayCamera.ShiftClaheLevel.create({value: shift})});
    CSShared.sendCmdMessage(rootMsg);
}
