import * as Cmd from "ts/proto/jon/index.cmd";
import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";

export function lrfCalibSetDayOffsets(x: number, y: number): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrfCalib = Cmd.Lrf_calib.Root.create({
        day: Cmd.Lrf_calib.Offsets.create({
            set: Cmd.Lrf_calib.SetOffsets.create({ x, y })
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfCalibSetHeatOffsets(x: number, y: number): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrfCalib = Cmd.Lrf_calib.Root.create({
        heat: Cmd.Lrf_calib.Offsets.create({
            set: Cmd.Lrf_calib.SetOffsets.create({ x, y })
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfCalibShiftDayOffsets(x: number, y: number): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrfCalib = Cmd.Lrf_calib.Root.create({
        day: Cmd.Lrf_calib.Offsets.create({
            shift: Cmd.Lrf_calib.ShiftOffsetsBy.create({ x, y })
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfCalibShiftHeatOffsets(x: number, y: number): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrfCalib = Cmd.Lrf_calib.Root.create({
        heat: Cmd.Lrf_calib.Offsets.create({
            shift: Cmd.Lrf_calib.ShiftOffsetsBy.create({ x, y })
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfCalibSaveDayOffsets(): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrfCalib = Cmd.Lrf_calib.Root.create({
        day: Cmd.Lrf_calib.Offsets.create({
            save: Cmd.Lrf_calib.SaveOffsets.create()
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfCalibSaveHeatOffsets(): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrfCalib = Cmd.Lrf_calib.Root.create({
        heat: Cmd.Lrf_calib.Offsets.create({
            save: Cmd.Lrf_calib.SaveOffsets.create()
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfCalibResetDayOffsets(): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrfCalib = Cmd.Lrf_calib.Root.create({
        day: Cmd.Lrf_calib.Offsets.create({
            reset: Cmd.Lrf_calib.ResetOffsets.create()
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}

export function lrfCalibResetHeatOffsets(): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.lrfCalib = Cmd.Lrf_calib.Root.create({
        heat: Cmd.Lrf_calib.Offsets.create({
            reset: Cmd.Lrf_calib.ResetOffsets.create()
        })
    });
    CSShared.sendCmdMessage(rootMsg);
}