# TypeScript CMD Functions Reference

Auto-generated list of all TypeScript cmd functions for implementation reference.
Generated on: Fri Aug  8 11:56:59 AM CEST 2025

## cmdSender/cmdCamDayGlassHeater.ts

- **Line 4**: `function dayCameraGlassHeaterStart(): void`
- **Line 12**: `function dayCameraGlassHeaterStop(): void`
- **Line 20**: `function dayCameraGlassHeaterTurnOn(): void`
- **Line 28**: `function dayCameraGlassHeaterTurnOff(): void`
- **Line 36**: `function dayCameraGlassHeaterGetMeteo(): void`

## cmdSender/cmdCompass.ts

- **Line 4**: `function compassStart(): void`
- **Line 11**: `function getMeteo(): void`
- **Line 19**: `function compassStop(): void`
- **Line 26**: `function setMagneticDeclination(value: number): void`
- **Line 33**: `function setOffsetAngleAzimuth(value: number): void`
- **Line 40**: `function setOffsetAngleElevation(value: number): void`
- **Line 47**: `function calibrateLongStart(): void`
- **Line 54**: `function calibrateShortStart(): void`
- **Line 61**: `function calibrateNext(): void`
- **Line 68**: `function calibrateCancel(): void`
- **Line 75**: `function setUseRotaryPosition(useRotary: boolean): void`

## cmdSender/cmdCV.ts

- **Line 7**: `function CVStartTrackNDC(channel: JonGuiDataVideoChannel, x: number, y:number): void`
- **Line 80**: `function CVStopTrack(): void`
- **Line 87**: `function CVSetAutoFocus(value: boolean, channel: JonGuiDataVideoChannel): void`
- **Line 94**: `function CVEnableVampireMode(): void`
- **Line 101**: `function CVDisableVampireMode(): void`
- **Line 108**: `function CVEnableStabilizationMode(): void`
- **Line 115**: `function CVDisableStabilizationMode(): void`
- **Line 122**: `function CVDumpStart (): void`
- **Line 129**: `function CVDumpStop (): void`

## cmdSender/cmdDayCamera.ts

- **Line 5**: `function dayCameraSetInfraRedFilter(value: boolean): void`
- **Line 12**: `function dayCameraSetIris(value: number): void`
- **Line 19**: `function dayCameraSetAutoIris(value: boolean): void`
- **Line 26**: `function dayCameraTakePhoto(): void`
- **Line 37**: `function dayCameraStart(): void`
- **Line 44**: `function dayCameraStop(): void`
- **Line 51**: `function dayCameraSetFocus(value: number): void`
- **Line 59**: `function dayCameraMoveFocus(targetValue: number, speed: number): void`
- **Line 67**: `function dayCameraHaltFocus(): void`
- **Line 75**: `function dayCameraOffsetFocus(offsetValue: number): void`
- **Line 83**: `function dayCameraResetFocus(): void`
- **Line 91**: `function dayCameraSaveFocusToTable(): void`
- **Line 99**: `function dayCameraSetZoom(value: number): void`
- **Line 107**: `function dayCameraMoveZoom(targetValue: number, speed: number): void`
- **Line 115**: `function dayCameraHaltZoom(): void`
- **Line 123**: `function dayCameraOffsetZoom(offsetValue: number): void`
- **Line 131**: `function dayCameraResetZoom(): void`
- **Line 139**: `function dayCameraSaveZoomToTable(): void`
- **Line 147**: `function dayCameraSetZoomTableValue(value: number): void`
- **Line 155**: `function dayCameraSetDigitalZoomLevel(value: number): void`
- **Line 163**: `function dayCameraNextZoomTablePos(): void`
- **Line 171**: `function dayCameraPrevZoomTablePos(): void`
- **Line 179**: `function getMeteo(): void`
- **Line 186**: `function setFxMode(mode: Types.JonGuiDataFxModeDay): void`
- **Line 193**: `function nextFxMode(): void`
- **Line 200**: `function prevFxMode(): void`
- **Line 207**: `function setClaheLevel(value: number): void`
- **Line 214**: `function  shiftClaheLevel(shift: number): void`

## cmdSender/cmdGps.ts

- **Line 4**: `function gpsStart(): void`
- **Line 11**: `function gpsStop(): void`
- **Line 18**: `function setManualPosition(latitude: number, longitude: number, altitude: number): void`
- **Line 30**: `function setUseManualPosition(useManual: boolean): void`
- **Line 38**: `function getMeteo(): void`

## cmdSender/cmdHeatCamera.ts

- **Line 5**: `function heatCameraTakePhoto(): void`
- **Line 16**: `function heatCameraSetAgc(value: Types.JonGuiDataVideoChannelHeatAGCModes): void`
- **Line 23**: `function heatCameraSetFilter(value: Types.JonGuiDataVideoChannelHeatFilters): void`
- **Line 30**: `function heatCameraSetZoomTableValue(value: number): void`
- **Line 38**: `function heatCameraSetDigitalZoomLevel(value: number): void`
- **Line 45**: `function stringToHeatCameraAgcMode(value: string): Types.JonGuiDataVideoChannelHeatAGCModes`
- **Line 58**: `function stringToHeatCameraFilter(value: string): Types.JonGuiDataVideoChannelHeatFilters`
- **Line 71**: `function heatCameraCalibrate(): void`
- **Line 78**: `function heatCameraStart(): void`
- **Line 85**: `function heatCameraStop(): void`
- **Line 92**: `function heatCameraZoomIn(): void`
- **Line 99**: `function heatCameraZoomOut(): void`
- **Line 106**: `function heatCameraZoomStop(): void`
- **Line 120**: `function heatCameraSetAutoFocusOn(): void`
- **Line 125**: `function heatCameraSetAutoFocusOff(): void`
- **Line 130**: `function heatCameraFocusStop(): void`
- **Line 137**: `function heatCameraFocusIn(): void`
- **Line 144**: `function heatCameraFocusOut(): void`
- **Line 151**: `function heatCameraFocusStepPLus(): void`
- **Line 158**: `function heatCameraFocusStepMinus(): void`
- **Line 165**: `function heatCameraNextZoomTablePos(): void`
- **Line 173**: `function heatCameraPrevZoomTablePos(): void`
- **Line 181**: `function heatCameraResetZoom(): void`
- **Line 188**: `function heatCameraSaveZoomToTable(): void`
- **Line 195**: `function getMeteo(): void`
- **Line 202**: `function enableDDE(): void`
- **Line 209**: `function disableDDE(): void`
- **Line 216**: `function setDDELevel(level: number): void`
- **Line 223**: `function shiftDDELevel(shift: number): void`
- **Line 230**: `function setFxMode(mode: Types.JonGuiDataFxModeHeat): void`
- **Line 237**: `function nextFxMode(): void`
- **Line 244**: `function prevFxMode(): void`
- **Line 251**: `function setClaheLevel(value: number): void`
- **Line 258**: `function shiftClaheLevel(shift: number): void`

## cmdSender/cmdLRFAlignment.ts

- **Line 4**: `function lrfCalibSetDayOffsets(x: number, y: number): void`
- **Line 14**: `function lrfCalibSetHeatOffsets(x: number, y: number): void`
- **Line 24**: `function lrfCalibShiftDayOffsets(x: number, y: number): void`
- **Line 34**: `function lrfCalibShiftHeatOffsets(x: number, y: number): void`
- **Line 44**: `function lrfCalibSaveDayOffsets(): void`
- **Line 54**: `function lrfCalibSaveHeatOffsets(): void`
- **Line 64**: `function lrfCalibResetDayOffsets(): void`
- **Line 74**: `function lrfCalibResetHeatOffsets(): void`

## cmdSender/cmdLRF.ts

- **Line 5**: `function lrfStart(): void`
- **Line 12**: `function lrfStop(): void`
- **Line 19**: `function lrfNewSession(): void`
- **Line 26**: `function lrfScanOn(): void`
- **Line 33**: `function refineOn (): void`
- **Line 40**: `function refineOff (): void`
- **Line 47**: `function lrfScanOff(): void`
- **Line 54**: `function lrfMeasure(): void`
- **Line 69**: `function lrfEnableFogMode(): void`
- **Line 76**: `function lrfDisableFogMode(): void`
- **Line 83**: `function lrfTargetDesignatorOff(): void`
- **Line 90**: `function lrfTargetDesignatorOnModeA(): void`
- **Line 97**: `function lrfTargetDesignatorOnModeB(): void`
- **Line 104**: `function getMeteo(): void`

## cmdSender/cmdOSD.ts

- **Line 5**: `function OSDShowDefaultScreen(): void`
- **Line 12**: `function OSDShowLRFMeasureScreen(): void`
- **Line 19**: `function OSDShowLRFResultScreen(): void`
- **Line 26**: `function OSDShowLRFResultSimplifiedScreen(): void`
- **Line 33**: `function OSDDisableDayOSD(): void`
- **Line 40**: `function OSDDisableHeatOSD(): void`
- **Line 47**: `function OSDEnableDayOSD(): void`
- **Line 54**: `function OSDEnableHeatOSD(): void`

## cmdSender/cmdRotary.ts

- **Line 5**: `function rotaryStart(): void`
- **Line 12**: `function rotaryStop(): void`
- **Line 19**: `function rotaryHalt(): void`
- **Line 26**: `function rotarySetPlatformAzimuth(value: number): void`
- **Line 33**: `function rotarySetPlatformElevation(value: number): void`
- **Line 40**: `function rotarySetPlatformBank(value: number): void`
- **Line 47**: `function rotaryHaltAzimuth(): void`
- **Line 55**: `function rotaryHaltElevation(): void`
- **Line 63**: `function rotaryHaltElevationAndAzimuth(): void`
- **Line 77**: `function rotaryAzimuthSetValue(value: number, direction: Types.JonGuiDataRotaryDirection): void`
- **Line 85**: `function rotaryAzimuthRotateTo(targetValue: number, speed: number, direction: Types.JonGuiDataRotaryDirection): void`
- **Line 97**: `function rotaryAzimuthRotateRelative(value: number, speed: number, direction: Types.JonGuiDataRotaryDirection): void`
- **Line 109**: `function rotaryElevationRotateRelative(value: number, speed: number, direction: Types.JonGuiDataRotaryDirection): void`
- **Line 121**: `function rotaryElevationRotateRelativeSet(value: number, direction: Types.JonGuiDataRotaryDirection): void`
- **Line 132**: `function rotaryAzimuthRotateRelativeSet(value: number, direction: Types.JonGuiDataRotaryDirection): void`
- **Line 143**: `function rotaryAzimuthRotate(speed: number, direction: Types.JonGuiDataRotaryDirection): void`
- **Line 151**: `function rotaryElevationRotate(speed: number, direction: Types.JonGuiDataRotaryDirection): void`
- **Line 159**: `function rotaryElevationSetValue(value: number): void`
- **Line 167**: `function rotaryElevationRotateTo(targetValue: number, speed: number,): void`
- **Line 178**: `function rotateBothTo(azimuth: number, azimuthSpeed: number, azimuthDirection: Types.JonGuiDataRotaryDirection, elevation: number, elevationSpeed: number): void`
- **Line 200**: `function rotateBoth(azimuthSpeed: number, azimuthDirection: Types.JonGuiDataRotaryDirection, elevationSpeed: number, elevationDirection: Types.JonGuiDataRotaryDirection): void`
- **Line 220**: `function rotateBothRelative(azimuth: number, azimuthSpeed: number, azimuthDirection: Types.JonGuiDataRotaryDirection, elevation: number, elevationSpeed: number, elevationDirection: Types.JonGuiDataRotaryDirection): void`
- **Line 243**: `function rotateBothRelativeSet(azimuth: number, azimuthDirection: Types.JonGuiDataRotaryDirection, elevation: number, elevationDirection: Types.JonGuiDataRotaryDirection): void`
- **Line 264**: `function setBothTo(azimuth: number, elevation: number, azimuthDirection: Types.JonGuiDataRotaryDirection,): void`
- **Line 281**: `function setCalculateBasePositionFromCompass(value: boolean): void`
- **Line 288**: `function getMeteo(): void`
- **Line 295**: `function setRotateToGps(lon : number, lat :number, alt:number): void`
- **Line 307**: `function setOriginGps(lon : number, lat :number, alt:number): void`
- **Line 319**: `function stringToRotaryMode (value: string): Types.JonGuiDataRotaryMode`
- **Line 338**: `function setRotaryMode (mode: Types.JonGuiDataRotaryMode): void`
- **Line 345**: `function stringToRotaryDirection(value: string): Types.JonGuiDataRotaryDirection`
- **Line 357**: `function RotateToNDC(channel: Types.JonGuiDataVideoChannel, x: number, y: number): void`
- **Line 369**: `function scanStart(): void`
- **Line 376**: `function scanPrev(): void`
- **Line 383**: `function scanNext(): void`
- **Line 390**: `function scanStop(): void`
- **Line 397**: `function scanPause(): void`
- **Line 404**: `function scanUnpause(): void`
- **Line 412**: `function scanRefreshNodeList(): void`
- **Line 419**: `function scanSelectNode(index: number): void`
- **Line 426**: `function scanDeleteNode(index: number): void`
- **Line 433**: `function scanUpdateNode(`
- **Line 458**: `function scanAddNode(`

## cmdSender/cmdSystem.ts

- **Line 5**: `function SystemReboot(): void`
- **Line 12**: `function SystemPowerOff(): void`
- **Line 19**: `function SystemResetConfigs(): void`
- **Line 26**: `function SystemStartAll(): void`
- **Line 33**: `function SystemStopAll(): void`
- **Line 40**: `function SystemMarkRecImportant(): void`
- **Line 47**: `function SystemUnmarkRecImportant(): void`
- **Line 54**: `function SystemSetLocalization(localization: Types.JonGuiDataSystemLocalizations ): void`
- **Line 61**: `function SystemEnterTransport(): void`
- **Line 68**: `function  SystemEnableGeodesicMode(): void`
- **Line 75**: `function SystemDisableGeodesicMode(): void`

## poi/poiApiClient.ts

- **Line 31**: `async function fetchPOIStatus(): Promise<POIStatus | null>`
- **Line 53**: `async function fetchPOI(index: number): Promise<PointOfInterest | null>`
- **Line 80**: `async function savePOI(index: number, poi: PointOfInterest): Promise<boolean>`
- **Line 107**: `async function deletePOI(index: number): Promise<boolean>`

## poi/poiCommands.ts

- **Line 13**: `async function lookAtPOI(index: number): Promise<void>`
- **Line 45**: `async function saveCurrentPositionToPOI(index: number): Promise<void>`
- **Line 87**: `function getPOIDisplayName(index: number): string`
- **Line 92**: `function lookAtPOI0() { lookAtPOI(0); }`
- **Line 93**: `function lookAtPOI1() { lookAtPOI(1); }`
- **Line 94**: `function lookAtPOI2() { lookAtPOI(2); }`
- **Line 95**: `function lookAtPOI3() { lookAtPOI(3); }`
- **Line 96**: `function lookAtPOI4() { lookAtPOI(4); }`
- **Line 97**: `function lookAtPOI5() { lookAtPOI(5); }`
- **Line 98**: `function lookAtPOI6() { lookAtPOI(6); }`
- **Line 99**: `function lookAtPOI7() { lookAtPOI(7); }`
- **Line 100**: `function lookAtPOI8() { lookAtPOI(8); }`
- **Line 101**: `function lookAtPOI9() { lookAtPOI(9); }`
- **Line 103**: `function saveToPOI0() { saveCurrentPositionToPOI(0); }`
- **Line 104**: `function saveToPOI1() { saveCurrentPositionToPOI(1); }`
- **Line 105**: `function saveToPOI2() { saveCurrentPositionToPOI(2); }`
- **Line 106**: `function saveToPOI3() { saveCurrentPositionToPOI(3); }`
- **Line 107**: `function saveToPOI4() { saveCurrentPositionToPOI(4); }`
- **Line 108**: `function saveToPOI5() { saveCurrentPositionToPOI(5); }`
- **Line 109**: `function saveToPOI6() { saveCurrentPositionToPOI(6); }`
- **Line 110**: `function saveToPOI7() { saveCurrentPositionToPOI(7); }`
- **Line 111**: `function saveToPOI8() { saveCurrentPositionToPOI(8); }`
- **Line 112**: `function saveToPOI9() { saveCurrentPositionToPOI(9); }`

## hotkeyCommands.ts



## Summary
- Total files analyzed: 14
- Total functions found: 203
