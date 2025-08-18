import * as Types from "ts/proto/jon/jon_shared_data_types";
import * as Cmd from "ts/proto/jon/index.cmd";
import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";

export function rotaryStart(): void {
    //console.log("Sending rotary start command");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({start: Cmd.RotaryPlatform.Start.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function rotaryStop(): void {
    //console.log("Sending rotary stop command");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({stop: Cmd.RotaryPlatform.Stop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function rotaryHalt(): void {
    //console.log("Sending rotary halt command");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({halt: Cmd.RotaryPlatform.Halt.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function rotarySetPlatformAzimuth(value: number): void {
    //console.log(`Setting platform azimuth to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({setPlatformAzimuth: Cmd.RotaryPlatform.SetPlatformAzimuth.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function rotarySetPlatformElevation(value: number): void {
    //console.log(`Setting platform elevation to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({setPlatformElevation: Cmd.RotaryPlatform.SetPlatformElevation.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function rotarySetPlatformBank(value: number): void {
    //console.log(`Setting platform bank to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({setPlatformBank: Cmd.RotaryPlatform.SetPlatformBank.create({value})});
    CSShared.sendCmdMessage(rootMsg);
}

export function rotaryHaltAzimuth(): void {
    //console.log("Sending halt azimuth command");
    let azimuthMsg = Cmd.RotaryPlatform.Azimuth.create({
        halt: Cmd.RotaryPlatform.HaltAzimuth.create()
    });
    CSShared.sendRotaryAxisCommand({azimuth: azimuthMsg});
}

export function rotaryHaltElevation(): void {
    //console.log("Sending halt elevation command");
    let elevationMsg = Cmd.RotaryPlatform.Elevation.create({
        halt: Cmd.RotaryPlatform.HaltElevation.create()
    });
    CSShared.sendRotaryAxisCommand({elevation: elevationMsg});
}

export function rotaryHaltElevationAndAzimuth(): void {
    //console.log("Sending halt elevation and azimuth command");
    let azimuthMsg = Cmd.RotaryPlatform.Azimuth.create({
        halt: Cmd.RotaryPlatform.HaltAzimuth.create()
    });
    let elevationMsg = Cmd.RotaryPlatform.Elevation.create({
        halt: Cmd.RotaryPlatform.HaltElevation.create()
    });
    CSShared.sendRotaryAxisCommand({
        azimuth: azimuthMsg,
        elevation: elevationMsg
    });
}

export function rotaryAzimuthSetValue(value: number, direction: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Setting azimuth value to ${value} with direction ${direction}`);
    let setAzimuthMsg = Cmd.RotaryPlatform.Azimuth.create({
        setValue: Cmd.RotaryPlatform.SetAzimuthValue.create({value, direction})
    });
    CSShared.sendRotaryAxisCommand({azimuth: setAzimuthMsg});
}

export function rotaryAzimuthRotateTo(targetValue: number, speed: number, direction: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Rotating azimuth to ${targetValue} with speed ${speed} and direction ${direction}`);
    let rotateAzimuthMsg = Cmd.RotaryPlatform.Azimuth.create({
        rotateTo: Cmd.RotaryPlatform.RotateAzimuthTo.create({
            targetValue: targetValue,
            speed,
            direction
        })
    });
    CSShared.sendRotaryAxisCommand({azimuth: rotateAzimuthMsg});
}

export function rotaryAzimuthRotateRelative(value: number, speed: number, direction: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Rotating azimuth RELATIVE to the current position by ${value} at speed ${speed} with direction ${direction}`);
    let rotateAzimuthRelativeMsg = Cmd.RotaryPlatform.Azimuth.create({
        relative: Cmd.RotaryPlatform.RotateAzimuthRelative.create({
            value,
            speed,
            direction
        })
    });
    CSShared.sendRotaryAxisCommand({azimuth: rotateAzimuthRelativeMsg});
}

export function rotaryElevationRotateRelative(value: number, speed: number, direction: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Rotating elevation RELATIVE to the current position by ${value} at speed ${speed} with direction ${direction}`);
    let rotateElevationRelativeMsg = Cmd.RotaryPlatform.Elevation.create({
        relative: Cmd.RotaryPlatform.RotateElevationRelative.create({
            value,
            speed,
            direction
        })
    });
    CSShared.sendRotaryAxisCommand({elevation: rotateElevationRelativeMsg});
}

export function rotaryElevationRotateRelativeSet(value: number, direction: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Setting elevation value RELATIVE to the current position to ${value} with direction ${direction}`);
    let rotateElevationRelativeSetMsg = Cmd.RotaryPlatform.Elevation.create({
        relativeSet: Cmd.RotaryPlatform.RotateElevationRelativeSet.create({
            value,
            direction
        })
    });
    CSShared.sendRotaryAxisCommand({elevation: rotateElevationRelativeSetMsg});
}

export function rotaryAzimuthRotateRelativeSet(value: number, direction: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Setting azimuth value RELATIVE to the current position to ${value} with direction ${direction}`);
    let rotateAzimuthRelativeSetMsg = Cmd.RotaryPlatform.Azimuth.create({
        relativeSet: Cmd.RotaryPlatform.RotateAzimuthRelativeSet.create({
            value,
            direction
        })
    });
    CSShared.sendRotaryAxisCommand({azimuth: rotateAzimuthRelativeSetMsg});
}

export function rotaryAzimuthRotate(speed: number, direction: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Rotating azimuth continuously at speed ${speed} with direction ${direction}`);
    let rotateAzimuthMsg = Cmd.RotaryPlatform.Azimuth.create({
        rotate: Cmd.RotaryPlatform.RotateAzimuth.create({speed, direction})
    });
    CSShared.sendRotaryAxisCommand({azimuth: rotateAzimuthMsg});
}

export function rotaryElevationRotate(speed: number, direction: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Rotating elevation continuously at speed ${speed} with direction ${direction}`);
    let rotateElevationMsg = Cmd.RotaryPlatform.Elevation.create({
        rotate: Cmd.RotaryPlatform.RotateElevation.create({speed, direction})
    });
    CSShared.sendRotaryAxisCommand({elevation: rotateElevationMsg});
}

export function rotaryElevationSetValue(value: number): void {
    //console.log(`Setting elevation value to ${value}`);
    let rotateElevationMsg = Cmd.RotaryPlatform.Elevation.create({
        setValue: Cmd.RotaryPlatform.SetElevationValue.create({value})
    });
    CSShared.sendRotaryAxisCommand({elevation: rotateElevationMsg});
}

export function rotaryElevationRotateTo(targetValue: number, speed: number,): void {
    //console.log(`Rotating elevation to ${targetValue} at speed ${speed}`);
    let rotateElevationMsg = Cmd.RotaryPlatform.Elevation.create({
        rotateTo: Cmd.RotaryPlatform.RotateElevationTo.create({
            targetValue: targetValue,
            speed
        })
    });
    CSShared.sendRotaryAxisCommand({elevation: rotateElevationMsg});
}

export function rotateBothTo(azimuth: number, azimuthSpeed: number, azimuthDirection: Types.JonGuiDataRotaryDirection, elevation: number, elevationSpeed: number): void {

    //console.log(`Rotating azimuth to ${azimuth} at speed ${azimuthSpeed} and elevation to ${elevation} at speed ${elevationSpeed} and azimuth direction ${azimuthDirection}`);
    let rotateElevationToMsg = Cmd.RotaryPlatform.Elevation.create({
        rotateTo: Cmd.RotaryPlatform.RotateElevationTo.create({
            targetValue: elevation,
            speed: elevationSpeed
        })
    });
    let rotateAzimuthToMsg = Cmd.RotaryPlatform.Azimuth.create({
        rotateTo: Cmd.RotaryPlatform.RotateAzimuthTo.create({
            targetValue: azimuth,
            speed: azimuthSpeed,
            direction: azimuthDirection
        })
    });
    CSShared.sendRotaryAxisCommand({
        elevation: rotateElevationToMsg,
        azimuth: rotateAzimuthToMsg
    });
}

export function rotateBoth(azimuthSpeed: number, azimuthDirection: Types.JonGuiDataRotaryDirection, elevationSpeed: number, elevationDirection: Types.JonGuiDataRotaryDirection): void {
    //console.log(`Rotating azimuth at speed ${azimuthSpeed} and elevation at speed ${elevationSpeed} and azimuth direction ${azimuthDirection} and elevation direction ${elevationDirection}`);
    let rotateElevationMsg = Cmd.RotaryPlatform.Elevation.create({
        rotate: Cmd.RotaryPlatform.RotateElevation.create({
            speed: elevationSpeed,
            direction: elevationDirection
        })
    });
    let rotateAzimuthMsg = Cmd.RotaryPlatform.Azimuth.create({
        rotate: Cmd.RotaryPlatform.RotateAzimuth.create({
            speed: azimuthSpeed,
            direction: azimuthDirection
        })
    });
    CSShared.sendRotaryAxisCommand({
        elevation: rotateElevationMsg,
        azimuth: rotateAzimuthMsg
    });
}

export function rotateBothRelative(azimuth: number, azimuthSpeed: number, azimuthDirection: Types.JonGuiDataRotaryDirection, elevation: number, elevationSpeed: number, elevationDirection: Types.JonGuiDataRotaryDirection): void {

    //console.log(`Rotating azimuth relative to ${azimuth} at speed ${azimuthSpeed} and elevation relative to ${elevation} at speed ${elevationSpeed} and azimuth direction ${azimuthDirection} and elevation direction ${elevationDirection}`);
    let rotateElevationRelativeMsg = Cmd.RotaryPlatform.Elevation.create({
        relative: Cmd.RotaryPlatform.RotateElevationRelative.create({
            value: elevation,
            speed: elevationSpeed,
            direction: elevationDirection
        })
    });
    let rotateAzimuthRelativeMsg = Cmd.RotaryPlatform.Azimuth.create({
        relative: Cmd.RotaryPlatform.RotateAzimuthRelative.create({
            value: azimuth,
            speed: azimuthSpeed,
            direction: azimuthDirection
        })
    });
    CSShared.sendRotaryAxisCommand({
        elevation: rotateElevationRelativeMsg,
        azimuth: rotateAzimuthRelativeMsg
    });
}

export function rotateBothRelativeSet(azimuth: number, azimuthDirection: Types.JonGuiDataRotaryDirection, elevation: number, elevationDirection: Types.JonGuiDataRotaryDirection): void {

    //console.log(`Setting azimuth relative to ${azimuth} and elevation relative to ${elevation} and azimuth direction ${azimuthDirection} and elevation direction ${elevationDirection}`);
    let rotateElevationRelativeSetMsg = Cmd.RotaryPlatform.Elevation.create({
        relativeSet: Cmd.RotaryPlatform.RotateElevationRelativeSet.create({
            value: elevation,
            direction: elevationDirection
        })
    });
    let rotateAzimuthRelativeSetMsg = Cmd.RotaryPlatform.Azimuth.create({
        relativeSet: Cmd.RotaryPlatform.RotateAzimuthRelativeSet.create({
            value: azimuth,
            direction: azimuthDirection
        })
    });
    CSShared.sendRotaryAxisCommand({
        elevation: rotateElevationRelativeSetMsg,
        azimuth: rotateAzimuthRelativeSetMsg
    });
}

export function setBothTo(azimuth: number, elevation: number, azimuthDirection: Types.JonGuiDataRotaryDirection,): void {
    //console.log(`Setting azimuth to ${azimuth} and elevation to ${elevation} and azimuth direction ${azimuthDirection}`);
    let setAzimuthMsg = Cmd.RotaryPlatform.Azimuth.create({
        setValue: Cmd.RotaryPlatform.SetAzimuthValue.create({
            value: azimuth,
            direction: azimuthDirection
        })
    });
    let setElevationMsg = Cmd.RotaryPlatform.Elevation.create({
        setValue: Cmd.RotaryPlatform.SetElevationValue.create({value: elevation})
    });
    CSShared.sendRotaryAxisCommand({
        azimuth: setAzimuthMsg,
        elevation: setElevationMsg
    });
}

export function setCalculateBasePositionFromCompass(value: boolean): void {
    //console.log(`Setting calculate base position from compass to ${value}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({setUseRotaryAsCompass: {flag: value}});
    CSShared.sendCmdMessage(rootMsg);
}

export function getMeteo(): void {
    //console.log("Requesting rotary meteo data");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({getMeteo: Cmd.RotaryPlatform.GetMeteo.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function setRotateToGps(lon : number, lat :number, alt:number): void {
    //console.log(`Setting rotate to GPS to ${lon}, ${lat}, ${alt}`);
    let rotateToGpsMsg = Cmd.RotaryPlatform.RotateToGPS.create({
        longitude: lon,
        latitude: lat,
        altitude: alt
    });
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({rotateToGps: rotateToGpsMsg});
    CSShared.sendCmdMessage(rootMsg);
}

export function setOriginGps(lon : number, lat :number, alt:number): void {
    //console.log(`Setting origin GPS to ${lon}, ${lat}, ${alt}`);
    let setOriginGpsMsg = Cmd.RotaryPlatform.SetOriginGPS.create({
        longitude: lon,
        latitude: lat,
        altitude: alt
    });
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({setOriginGps: setOriginGpsMsg});
    CSShared.sendCmdMessage(rootMsg);
}

export function stringToRotaryMode (value: string): Types.JonGuiDataRotaryMode {
    switch (value) {
        case "init":
            return Types.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_INITIALIZATION;
        case "speed":
            return Types.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_SPEED;
        case "position":
            return Types.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_POSITION;
        case "stabilization":
            return Types.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_STABILIZATION;
        case "targeting":
            return Types.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_TARGETING;
        case "video_tracker":
            return Types.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER;
        default:
            return Types.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED;
    }
}

export function setRotaryMode (mode: Types.JonGuiDataRotaryMode): void {
    //console.log(`Setting rotary mode to ${mode}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({setMode: {mode}});
    CSShared.sendCmdMessage(rootMsg);
}

export function stringToRotaryDirection(value: string): Types.JonGuiDataRotaryDirection {
    switch (value.toLowerCase()) {
        case 'clockwise':
            return Types.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE;
        case 'counterclockwise':
        case 'counter clockwise':
            return Types.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE;
        default:
            return Types.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_UNSPECIFIED;
    }
}

export function RotateToNDC(channel: Types.JonGuiDataVideoChannel, x: number, y: number): void {
    //console.log(`Rotating to NDC ${x}, ${y} on channel ${channel}`);
    let rotateToNDCMsg = Cmd.RotaryPlatform.RotateToNDC.create({
        channel,
        x,
        y
    });
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({rotateToNdc: rotateToNDCMsg});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanStart(): void {
    // console.log("Starting rotary scan");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanStart: Cmd.RotaryPlatform.ScanStart.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanPrev(): void {
    // console.log("rotary scan prev");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanPrev: Cmd.RotaryPlatform.ScanPrev.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanNext(): void {
    // console.log("rotary scan next");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanNext: Cmd.RotaryPlatform.ScanNext.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanStop(): void {
    // console.log("Stopping rotary scan");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanStop: Cmd.RotaryPlatform.ScanStop.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanPause(): void {
    // console.log("Pausing rotary scan");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanPause: Cmd.RotaryPlatform.ScanPause.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanUnpause(): void {
    // console.log("Unpausing rotary scan");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanUnpause: Cmd.RotaryPlatform.ScanUnpause.create()});
    CSShared.sendCmdMessage(rootMsg);
}


export function scanRefreshNodeList(): void {
    // console.log("Refreshing rotary scan node list");
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanRefreshNodeList: Cmd.RotaryPlatform.ScanRefreshNodeList.create()});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanSelectNode(index: number): void {
    // console.log(`Selecting rotary scan node ${node}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanSelectNode: Cmd.RotaryPlatform.ScanSelectNode.create({index})});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanDeleteNode(index: number): void {
    // console.log(`Deleting rotary scan node at index ${index}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({scanDeleteNode: Cmd.RotaryPlatform.ScanDeleteNode.create({index})});
    CSShared.sendCmdMessage(rootMsg);
}

export function scanUpdateNode(
    index: number,
    dayZoomTableValue: number,
    heatZoomTableValue: number,
    azimuth: number,
    elevation: number,
    linger: number,
    speed: number
): void {
    // console.log(`Updating rotary scan node at index ${index}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({
        scanUpdateNode: Cmd.RotaryPlatform.ScanUpdateNode.create({
            index,
            DayZoomTableValue: dayZoomTableValue,
            HeatZoomTableValue: heatZoomTableValue,
            azimuth,
            elevation,
            linger,
            speed
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function scanAddNode(
    index: number,
    dayZoomTableValue: number,
    heatZoomTableValue: number,
    azimuth: number,
    elevation: number,
    linger: number,
    speed: number
): void {
    console.log(`Add rotary scan node at index ${index}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({
        scanAddNode: Cmd.RotaryPlatform.ScanAddNode.create({
            index,
            DayZoomTableValue: dayZoomTableValue,
            HeatZoomTableValue: heatZoomTableValue,
            azimuth,
            elevation,
            linger,
            speed
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}
