import * as Cmd from "ts/proto/jon/index.cmd";
import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";
import * as Types from "ts/proto/jon/jon_shared_data_types";

export function lrfStart(): void {
    //console.log("Sending LRF start");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({start: Cmd.Lrf.Start.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfStop(): void {
    //console.log("Sending LRF stop");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({stop: Cmd.Lrf.Stop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfNewSession(): void {
    //console.log("Sending LRF new session");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({newSession: Cmd.Lrf.NewSession.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfScanOn(): void {
    //console.log("Sending LRF scan on");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({scanOn: Cmd.Lrf.ScanOn.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function refineOn (): void {
    //console.log("Sending LRF refine on");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({refineOn: Cmd.Lrf.RefineOn.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function refineOff (): void {
    //console.log("Sending LRF refine off");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({refineOff: Cmd.Lrf.RefineOff.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfScanOff(): void {
    //console.log("Sending LRF scan off");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({scanOff: Cmd.Lrf.ScanOff.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfMeasure(): void {
    //console.log("Sending LRF measure");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({measure: Cmd.Lrf.Measure.create()});
    CSShared.sendCmdMessage(rootMsg);
}


// export function lrfSetScanMode(mode: Types.JonGuiDataLrfScanModes): void {
//     //console.log("Sending LRF set scan mode");
//     let rootMsg = CSShared.createRootMessage();
//     rootMsg.lrf = Cmd.Lrf.Root.create({setScanMode: Cmd.Lrf.setScanMode.create({mode: mode})});
//     CSShared.sendCmdMessage(rootMsg);
// }

export function lrfEnableFogMode(): void {
    //console.log("Sending LRF enable fog mode");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({enableFogMode: Cmd.Lrf.EnableFogMode.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfDisableFogMode(): void {
    //console.log("Sending LRF disable fog mode");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({disableFogMode: Cmd.Lrf.DisableFogMode.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfTargetDesignatorOff(): void {
    //console.log("Sending LRF target designator off");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({targetDesignatorOff: Cmd.Lrf.TargetDesignatorOff.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfTargetDesignatorOnModeA(): void {
    //console.log("Sending LRF target designator on mode A");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({targetDesignatorOnModeA: Cmd.Lrf.TargetDesignatorOnModeA.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfTargetDesignatorOnModeB(): void {
    //console.log("Sending LRF target designator on mode B");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({targetDesignatorOnModeB: Cmd.Lrf.TargetDesignatorOnModeB.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function getMeteo(): void {
    //console.log("Requesting LRF meteo data");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrf = Cmd.Lrf.Root.create({getMeteo: Cmd.Lrf.GetMeteo.create()});
    CSShared.sendCmdMessage(rootMsg);
}
