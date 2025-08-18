import * as Cmd from "ts/proto/jon/index.cmd";
import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";
import {JonGuiDataVideoChannel} from "../../proto/jon/jon_shared_data_types";

export function OSDShowDefaultScreen(): void {
    //console.log("Sending show default screen");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.osd = Cmd.OSD.Root.create({showDefaultScreen: Cmd.OSD.ShowDefaultScreen.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function OSDShowLRFMeasureScreen(): void {
    //console.log("Sending show LRF measure screen");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.osd = Cmd.OSD.Root.create({showLrfMeasureScreen: Cmd.OSD.ShowLRFMeasureScreen.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function OSDShowLRFResultScreen(): void {
    //console.log("Sending show LRF result screen");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.osd = Cmd.OSD.Root.create({showLrfResultScreen: Cmd.OSD.ShowLRFResultScreen.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function OSDShowLRFResultSimplifiedScreen(): void {
    //console.log("Sending show LRF result simplified screen");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.osd = Cmd.OSD.Root.create({showLrfResultSimplifiedScreen: Cmd.OSD.ShowLRFResultSimplifiedScreen.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function OSDDisableDayOSD(): void {
    //console.log("Sending disable day OSD");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.osd = Cmd.OSD.Root.create({disableDayOsd: Cmd.OSD.DisableDayOSD.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function OSDDisableHeatOSD(): void {
    //console.log("Sending disable heat OSD");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.osd = Cmd.OSD.Root.create({disableHeatOsd: Cmd.OSD.DisableHeatOSD.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function OSDEnableDayOSD(): void {
    //console.log("Sending enable day OSD");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.osd = Cmd.OSD.Root.create({enableDayOsd: Cmd.OSD.EnableDayOSD.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function OSDEnableHeatOSD(): void {
    //console.log("Sending enable heat OSD");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.osd = Cmd.OSD.Root.create({enableHeatOsd: Cmd.OSD.EnableHeatOSD.create()});
    CSShared.sendCmdMessage(rootMsg);
}