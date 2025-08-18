import * as Cmd from "ts/proto/jon/index.cmd";
import * as CSShared from "ts/cmd/cmdSender/cmdSenderShared";
import {JonGuiDataVideoChannel} from "../../proto/jon/jon_shared_data_types";
import {DeviceStateDispatch} from "../../statePub/deviceStateDispatch";
import Long from "long";

export function CVStartTrackNDC(channel: JonGuiDataVideoChannel, x: number, y:number): void {
    // Get timestamps from device state
    const dispatch = DeviceStateDispatch.getInstance();

    // Start async frame data retrieval without waiting
    (async () => {
        try {
            // Get the appropriate frame data based on channel
            const frameData = channel === JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
                ? await dispatch.getDayFrameData()
                : await dispatch.getHeatFrameData();

            if (!frameData) {
                console.warn("No frame data available. Using 0 as fallback timestamp.");
                sendStartTrackCommand(channel, x, y, Long.UZERO);
                return;
            }

            // Get the BigInt timestamp directly
            const bigintTimestamp = frameData.timestamp;
            const bigintDuration = frameData.duration;

            // Convert BigInt to a string first to create a Long object
            // This ensures proper handling as a 64-bit integer in protobuf
            let frameTimeLong: Long;
            try {
                frameTimeLong = Long.fromString(bigintTimestamp.toString(), true); // true for unsigned
            } catch (error) {
                console.error(`Failed to convert timestamp to Long: ${error}`);
                console.warn("Using 0 as fallback timestamp");
                frameTimeLong = Long.UZERO; // Unsigned zero
            }

            // Log timestamp and duration information
            const channelName = channel === JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY ? 'DAY' : 'HEAT';
            console.log(`Channel: ${channelName}
            Timestamp: ${bigintTimestamp.toString()} (${DeviceStateDispatch.formatTimestamp(bigintTimestamp, true)})
            Duration: ${bigintDuration.toString()} (${DeviceStateDispatch.formatDuration(bigintDuration)})`);

            // Send the command with the appropriate timestamp
            sendStartTrackCommand(channel, x, y, frameTimeLong);
        } catch (error) {
            // On any error, fallback to zero timestamp
            console.error("Error fetching frame data:", error);
            sendStartTrackCommand(channel, x, y, Long.UZERO);
        }
    })();
}

/**
 * Helper function to send the StartTrackNDC command
 */
function sendStartTrackCommand(channel: JonGuiDataVideoChannel, x: number, y: number, frameTime: Long): void {
    // Create the command with the Long value directly
    // The protobuf library will handle this correctly for uint64 fields
    let rootMsg = CSShared.createRootMessage();

    // We need to create the object manually with the Long value
    const startTrackNdcObj = {
        channel: channel,
        x: x,
        y: y,
        frameTime: frameTime // Use Long directly
    };

    // Create the command with this object
    rootMsg.cv = Cmd.CV.Root.create({
        startTrackNdc: startTrackNdcObj
    });

    CSShared.sendCmdMessage(rootMsg);
}

export function CVStopTrack(): void {
    console.log(`Sending stop track command`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({stopTrack: {}});
    CSShared.sendCmdMessage(rootMsg);
}

export function CVSetAutoFocus(value: boolean, channel: JonGuiDataVideoChannel): void {
    console.log(`Setting auto focus to ${value} for channel ${channel === JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY ? 'DAY' : 'HEAT'}`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({setAutoFocus: {value, channel}});
    CSShared.sendCmdMessage(rootMsg);
}

export function CVEnableVampireMode(): void {
    console.log(`Enabling vampire mode`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({vampireModeEnable: {}});
    CSShared.sendCmdMessage(rootMsg);
}

export function CVDisableVampireMode(): void {
    console.log(`Disabling vampire mode`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({vampireModeDisable: {}});
    CSShared.sendCmdMessage(rootMsg);
}

export function CVEnableStabilizationMode(): void {
    console.log(`Enabling stabilization mode`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({stabilizationModeEnable: {}});
    CSShared.sendCmdMessage(rootMsg);
}

export function CVDisableStabilizationMode(): void {
    console.log(`Disabling stabilization mode`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({stabilizationModeDisable: {}});
    CSShared.sendCmdMessage(rootMsg);
}

export function CVDumpStart (): void {
    console.log(`Starting dump`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({dumpStart: {}});
    CSShared.sendCmdMessage(rootMsg);
}

export function CVDumpStop (): void {
    console.log(`Stopping dump`);
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({dumpStop: {}});
    CSShared.sendCmdMessage(rootMsg);
}
