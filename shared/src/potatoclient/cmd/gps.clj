import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";
import * as Cmd from "ts/proto/jon/index.cmd";

export function gpsStart(): void {
    //console.log("Sending gps start");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.gps = Cmd.Gps.Root.create({start: Cmd.Gps.Start.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function gpsStop(): void {
    //console.log("Sending gps stop");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.gps = Cmd.Gps.Root.create({stop: Cmd.Gps.Stop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function setManualPosition(latitude: number, longitude: number, altitude: number): void {
    //console.log("Sending manual position");
    let rootMsg = CSShared.createRootMessage();
    let position = Cmd.Gps.SetManualPosition.create({
        latitude: latitude,
        longitude: longitude,
        altitude: altitude
    });
    rootMsg.gps = Cmd.Gps.Root.create({setManualPosition: position});
    CSShared.sendCmdMessage(rootMsg);
}

export function setUseManualPosition(useManual: boolean): void {
    //console.log("Sending use manual position");
    let rootMsg = CSShared.createRootMessage();
    let position = Cmd.Gps.SetUseManualPosition.create({flag: useManual});
    rootMsg.gps = Cmd.Gps.Root.create({setUseManualPosition: position});
    CSShared.sendCmdMessage(rootMsg);
}

export function getMeteo(): void {
    //console.log("Requesting GPS meteo data");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.gps = Cmd.Gps.Root.create({getMeteo: Cmd.Gps.GetMeteo.create()});
    CSShared.sendCmdMessage(rootMsg);
}
