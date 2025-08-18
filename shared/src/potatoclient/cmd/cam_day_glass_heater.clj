import * as Cmd from "ts/proto/jon/index.cmd";
import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";

export function dayCameraGlassHeaterStart(): void {
    //console.log("Sending Day Camera Glass Heater start");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamGlassHeater
        = Cmd.DayCamGlassHeater.Root.create({start: Cmd.DayCamGlassHeater.Start.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraGlassHeaterStop(): void {
    //console.log("Sending Day Camera Glass Heater stop");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamGlassHeater
        = Cmd.DayCamGlassHeater.Root.create({stop: Cmd.DayCamGlassHeater.Stop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraGlassHeaterTurnOn(): void {
    //console.log("Sending Day Camera Glass Heater turn on");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamGlassHeater
        = Cmd.DayCamGlassHeater.Root.create({turnOn: Cmd.DayCamGlassHeater.TurnOn.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraGlassHeaterTurnOff(): void {
    //console.log("Sending Day Camera Glass Heater turn off");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamGlassHeater
        = Cmd.DayCamGlassHeater.Root.create({turnOff: Cmd.DayCamGlassHeater.TurnOff.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraGlassHeaterGetMeteo(): void {
    //console.log("Sending Day Camera Glass Heater get meteo");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.dayCamGlassHeater
        = Cmd.DayCamGlassHeater.Root.create({getMeteo: Cmd.DayCamGlassHeater.GetMeteo.create()});
    CSShared.sendCmdMessage(rootMsg);
}
