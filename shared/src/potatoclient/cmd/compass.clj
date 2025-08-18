import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";
import * as Cmd from "ts/proto/jon/index.cmd";

export function compassStart(): void {
    //console.log("Sending compass start");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({start: Cmd.Compass.Start.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function getMeteo(): void {
    //console.log("Requesting compass meteo data");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({getMeteo: Cmd.Compass.GetMeteo.create()});
    CSShared.sendCmdMessage(rootMsg);

}

export function compassStop(): void {
    //console.log("Sending compass stop");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({stop: Cmd.Compass.Stop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function setMagneticDeclination(value: number): void {
    //console.log(`Setting magnetic declination to ${value} mils`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({setMagneticDeclination: {value}});
    CSShared.sendCmdMessage(rootMsg);
}

export function setOffsetAngleAzimuth(value: number): void {
    //console.log(`Setting offset angle azimuth to ${value} mils`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({setOffsetAngleAzimuth: {value}});
    CSShared.sendCmdMessage(rootMsg);
}

export function setOffsetAngleElevation(value: number): void {
    //console.log(`Setting offset angle elevation to ${value} mils`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({setOffsetAngleElevation: {value}});
    CSShared.sendCmdMessage(rootMsg);
}

export function calibrateLongStart(): void {
    //console.log("Sending calibrate long start");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({startCalibrateLong: Cmd.Compass.CalibrateStartLong.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function calibrateShortStart(): void {
    //console.log("Sending calibrate short start");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({startCalibrateShort: Cmd.Compass.CalibrateStartShort.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function calibrateNext(): void {
    //console.log("Sending calibrate next");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({calibrateNext: Cmd.Compass.CalibrateNext.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function calibrateCancel(): void {
    //console.log("Sending calibrate cancel");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.compass = Cmd.Compass.Root.create({calibrateCencel: Cmd.Compass.CalibrateCencel.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function setUseRotaryPosition(useRotary: boolean): void {
    //console.log("Sending use rotary position");
    let rootMsg = CSShared.createRootMessage();
    let position = Cmd.Compass.SetUseRotaryPosition.create({flag: useRotary});
    rootMsg.compass = Cmd.Compass.Root.create({setUseRotaryPosition: position});
    CSShared.sendCmdMessage(rootMsg);
}