# TODO: Proto Command Files Analysis

This file tracks the analysis of all protobuf command files to ensure every command is documented in the Action Registry design.

## Proto Files to Analyze

- [x] `jon_shared_cmd.proto` - Root command file
  - `Ping` - Heartbeat/keepalive (no params)
  - `Noop` - No operation (no params)
  - `Frozen` - Mark important state (no params)
- [x] `jon_shared_cmd_compass.proto` - Compass-related commands
  - `Start` - Start compass (no params)
  - `Stop` - Stop compass (no params)
  - `SetMagneticDeclination` - Set magnetic declination (value: float)
  - `SetOffsetAngleAzimuth` - Set azimuth offset (value: float)
  - `SetOffsetAngleElevation` - Set elevation offset (value: float)
  - `SetUseRotaryPosition` - Use rotary position (flag: bool)
  - `CalibrateStartLong` - Start long calibration (no params)
  - `CalibrateStartShort` - Start short calibration (no params)
  - `CalibrateNext` - Next calibration step (no params)
  - `CalibrateCencel` - Cancel calibration (no params)
  - `GetMeteo` - Get meteorological data (no params)
- [x] `jon_shared_cmd_cv.proto` - Computer Vision tracking commands
  - `SetAutoFocus` - Set auto focus (channel, value: bool)
  - `StartTrackNDC` - Start NDC tracking (channel, x, y, frame_time)
  - `StopTrack` - Stop tracking (no params)
  - `VampireModeEnable` - Enable vampire mode (no params)
  - `VampireModeDisable` - Disable vampire mode (no params)
  - `StabilizationModeEnable` - Enable stabilization (no params)
  - `StabilizationModeDisable` - Disable stabilization (no params)
  - `DumpStart` - Start dump (no params)
  - `DumpStop` - Stop dump (no params)
- [x] `jon_shared_cmd_day_camera.proto` - Day camera control commands
  - `Start` - Start day camera (no params)
  - `Stop` - Stop day camera (no params)
  - `Photo` - Take photo (no params)
  - `HaltAll` - Halt all operations (no params)
  - `SetIris` - Set iris value (value: float)
  - `SetAutoIris` - Set auto iris (value: bool)
  - `SetInfraRedFilter` - Set IR filter (value: bool)
  - `SetFxMode` - Set FX mode (mode: enum)
  - `NextFxMode` - Next FX mode (no params)
  - `PrevFxMode` - Previous FX mode (no params)
  - `RefreshFxMode` - Refresh FX mode (no params)
  - `GetMeteo` - Get meteorological data (no params)
  - `SetDigitalZoomLevel` - Set digital zoom level (value: float)
  - `SetClaheLevel` - Set CLAHE level (value: float)
  - `ShiftClaheLevel` - Shift CLAHE level (value: float)
  - Focus commands:
    - `Focus.SetValue` - Set focus value (value: float)
    - `Focus.Move` - Move focus (target_value, speed: float)
    - `Focus.Halt` - Halt focus (no params)
    - `Focus.Offset` - Offset focus (offset_value: float)
    - `Focus.ResetFocus` - Reset focus (no params)
    - `Focus.SaveToTableFocus` - Save focus to table (no params)
  - Zoom commands:
    - `Zoom.SetValue` - Set zoom value (value: float)
    - `Zoom.Move` - Move zoom (target_value, speed: float)
    - `Zoom.Halt` - Halt zoom (no params)
    - `Zoom.SetZoomTableValue` - Set zoom table value (value: int)
    - `Zoom.NextZoomTablePos` - Next zoom table position (no params)
    - `Zoom.PrevZoomTablePos` - Previous zoom table position (no params)
    - `Zoom.Offset` - Offset zoom (offset_value: float)
    - `Zoom.ResetZoom` - Reset zoom (no params)
    - `Zoom.SaveToTable` - Save zoom to table (no params)
- [x] `jon_shared_cmd_day_cam_glass_heater.proto` - Glass heater commands for day camera
  - `Start` - Start glass heater system (no params)
  - `Stop` - Stop glass heater system (no params)
  - `TurnOn` - Turn on heater (no params)
  - `TurnOff` - Turn off heater (no params)
  - `GetMeteo` - Get meteorological data (no params)
- [x] `jon_shared_cmd_gps.proto` - GPS control commands
  - `Start` - Start GPS (no params)
  - `Stop` - Stop GPS (no params)
  - `SetManualPosition` - Set manual GPS position (latitude, longitude, altitude: float)
  - `SetUseManualPosition` - Use manual position (flag: bool)
  - `GetMeteo` - Get meteorological data (no params)
- [x] `jon_shared_cmd_heat_camera.proto` - Thermal camera control commands
  - `Start` - Start heat camera (no params)
  - `Stop` - Stop heat camera (no params)
  - `Photo` - Take photo (no params)
  - `SetAGC` - Set AGC mode (value: enum)
  - `SetFilters` - Set filters (value: enum)
  - `ZoomIn` - Zoom in (no params)
  - `ZoomOut` - Zoom out (no params)
  - `ZoomStop` - Stop zoom (no params)
  - `FocusIn` - Focus in (no params)
  - `FocusOut` - Focus out (no params)
  - `FocusStop` - Stop focus (no params)
  - `FocusStepPlus` - Focus step forward (no params)
  - `FocusStepMinus` - Focus step backward (no params)
  - `Calibrate` - Calibrate camera (no params)
  - `SetDDELevel` - Set DDE level (value: int)
  - `ShiftDDE` - Shift DDE (value: int)
  - `EnableDDE` - Enable DDE (no params)
  - `DisableDDE` - Disable DDE (no params)
  - `SetAutoFocus` - Set auto focus (value: bool)
  - `SetFxMode` - Set FX mode (mode: enum)
  - `NextFxMode` - Next FX mode (no params)
  - `PrevFxMode` - Previous FX mode (no params)
  - `RefreshFxMode` - Refresh FX mode (no params)
  - `GetMeteo` - Get meteorological data (no params)
  - `ResetZoom` - Reset zoom (no params)
  - `SaveToTable` - Save to table (no params)
  - `SetCalibMode` - Set calibration mode (no params)
  - `SetDigitalZoomLevel` - Set digital zoom level (value: float)
  - `SetClaheLevel` - Set CLAHE level (value: float)
  - `ShiftClaheLevel` - Shift CLAHE level (value: float)
  - Zoom commands:
    - `Zoom.SetZoomTableValue` - Set zoom table value (value: int)
    - `Zoom.NextZoomTablePos` - Next zoom table position (no params)
    - `Zoom.PrevZoomTablePos` - Previous zoom table position (no params)
- [x] `jon_shared_cmd_lira.proto` - LIRA system commands
  - `Refine_target` - Refine target position (target: complex object with timestamp, coordinates, distance, UUID)
- [x] `jon_shared_cmd_lrf_align.proto` - LRF alignment commands
  - Channel-specific commands (day/heat):
    - `SetOffsets` - Set LRF offsets (x, y: int)
    - `SaveOffsets` - Save current offsets (no params)
    - `ResetOffsets` - Reset offsets to default (no params)
    - `ShiftOffsetsBy` - Shift offsets by delta (x, y: int)
- [x] `jon_shared_cmd_lrf.proto` - Laser Range Finder commands
  - `Start` - Start LRF (no params)
  - `Stop` - Stop LRF (no params)
  - `Measure` - Measure range (no params)
  - `ScanOn` - Turn on scanning (no params)
  - `ScanOff` - Turn off scanning (no params)
  - `RefineOn` - Turn on refinement (no params)
  - `RefineOff` - Turn off refinement (no params)
  - `TargetDesignatorOff` - Turn off target designator (no params)
  - `TargetDesignatorOnModeA` - Target designator mode A (no params)
  - `TargetDesignatorOnModeB` - Target designator mode B (no params)
  - `EnableFogMode` - Enable fog mode (no params)
  - `DisableFogMode` - Disable fog mode (no params)
  - `SetScanMode` - Set scan mode (mode: enum)
  - `NewSession` - Start new session (no params)
  - `GetMeteo` - Get meteorological data (no params)
- [x] `jon_shared_cmd_osd.proto` - On-Screen Display commands
  - `ShowDefaultScreen` - Show default OSD screen (no params)
  - `ShowLRFMeasureScreen` - Show LRF measurement screen (no params)
  - `ShowLRFResultScreen` - Show LRF results screen (no params)
  - `ShowLRFResultSimplifiedScreen` - Show simplified LRF results (no params)
  - `EnableHeatOSD` - Enable thermal camera OSD (no params)
  - `DisableHeatOSD` - Disable thermal camera OSD (no params)
  - `EnableDayOSD` - Enable day camera OSD (no params)
  - `DisableDayOSD` - Disable day camera OSD (no params)
- [x] `jon_shared_cmd_rotary.proto` - Rotary platform control commands
  - `Start` - Start rotary (no params)
  - `Stop` - Stop rotary (no params)
  - `Halt` - Halt rotary (no params)
  - `SetPlatformAzimuth` - Set platform azimuth (value: float)
  - `SetPlatformElevation` - Set platform elevation (value: float)
  - `SetPlatformBank` - Set platform bank (value: float)
  - `setUseRotaryAsCompass` - Use rotary as compass (flag: bool)
  - `RotateToGPS` - Rotate to GPS coordinates (latitude, longitude, altitude: float)
  - `SetOriginGPS` - Set GPS origin (latitude, longitude, altitude: float)
  - `SetMode` - Set rotary mode (mode: enum)
  - `RotateToNDC` - Rotate to NDC coordinates (channel, x, y: float)
  - `GetMeteo` - Get meteorological data (no params)
  - `ScanStart` - Start scanning (no params)
  - `ScanStop` - Stop scanning (no params)
  - `ScanPause` - Pause scanning (no params)
  - `ScanUnpause` - Unpause scanning (no params)
  - `ScanPrev` - Previous scan position (no params)
  - `ScanNext` - Next scan position (no params)
  - `ScanRefreshNodeList` - Refresh scan node list (no params)
  - `ScanSelectNode` - Select scan node (index: int)
  - `ScanDeleteNode` - Delete scan node (index: int)
  - `ScanUpdateNode` - Update scan node (index, DayZoomTableValue, HeatZoomTableValue: int, azimuth, elevation, linger, speed: double)
  - `ScanAddNode` - Add scan node (index, DayZoomTableValue, HeatZoomTableValue: int, azimuth, elevation, linger, speed: double)
  - Azimuth commands:
    - `Azimuth.SetAzimuthValue` - Set azimuth value (value: float, direction: enum)
    - `Azimuth.RotateAzimuthTo` - Rotate azimuth to (target_value, speed: float, direction: enum)
    - `Azimuth.RotateAzimuth` - Rotate azimuth (speed: float, direction: enum)
    - `Azimuth.RotateAzimuthRelative` - Rotate azimuth relative (value, speed: float, direction: enum)
    - `Azimuth.RotateAzimuthRelativeSet` - Set azimuth relative (value: float, direction: enum)
    - `Azimuth.HaltAzimuth` - Halt azimuth (no params)
  - Elevation commands:
    - `Elevation.SetElevationValue` - Set elevation value (value: float)
    - `Elevation.RotateElevationTo` - Rotate elevation to (target_value, speed: float)
    - `Elevation.RotateElevation` - Rotate elevation (speed: float, direction: enum)
    - `Elevation.RotateElevationRelative` - Rotate elevation relative (value, speed: float, direction: enum)
    - `Elevation.RotateElevationRelativeSet` - Set elevation relative (value: float, direction: enum)
    - `Elevation.HaltElevation` - Halt elevation (no params)
- [x] `jon_shared_cmd_system.proto` - System-level commands
  - `StartALl` - Start all systems (no params)
  - `StopALl` - Stop all systems (no params)
  - `Reboot` - Reboot system (no params)
  - `PowerOff` - Power off system (no params)
  - `SetLocalization` - Set localization (loc: enum)
  - `ResetConfigs` - Reset configurations (no params)
  - `StartRec` - Start recording (no params)
  - `StopRec` - Stop recording (no params)
  - `MarkRecImportant` - Mark recording as important (no params)
  - `UnmarkRecImportant` - Unmark recording as important (no params)
  - `EnterTransport` - Enter transport mode (no params)
  - `EnableGeodesicMode` - Enable geodesic mode (no params)
  - `DisableGeodesicMode` - Disable geodesic mode (no params)

## Instructions

For each proto file:
1. Open the file in `examples/protogen/proto/`
2. List all command messages it contains
3. Add them to the Action Registry design document under the appropriate module
4. Check off the file in this list
5. Note any commands that don't fit the current module structure

## Analysis Progress Summary

**Files Analyzed**: 13 of 13 (100%)
**Commands Found**: ~170+ unique command messages

### Key Patterns Observed:
- Most commands use kebab-case naming (e.g., `rotary-goto-ndc`, `cv-start-track-ndc`)
- Many commands have no parameters (e.g., Start, Stop, Halt)
- Common parameter types: float (often 0.0-1.0), int, bool, enums
- Nested command structures exist (e.g., DayCamera.Focus.*, Rotary.Azimuth.*)
- Validation constraints are specified using buf.validate annotations
- Many subsystems have `GetMeteo` command for meteorological data

### Analysis Complete!

All 13 proto command files have been analyzed and documented.

## Notes

- Some proto files might be empty or not contain actual commands
- Some commands might not be implemented in the current system
- Document ALL commands found, even if not currently used
- The action registry must handle both flat commands and nested command structures

## Implementation Guidelines for Action Registry

Based on the exhaustive analysis above, the Action Registry implementation should:

1. **Handle Nested Command Structures**:
   - Some commands are nested (e.g., `DayCamera.Focus.SetValue`)
   - Action names should use dot notation: `day-camera.focus.set-value`
   - Parent commands like `Axis` contain child commands

2. **Common Command Patterns**:
   - Many subsystems have identical commands: `Start`, `Stop`, `GetMeteo`
   - These should be prefixed: `gps-start`, `compass-start`, `lrf-start`
   - Consider shared validation for common patterns

3. **Parameter Types to Support**:
   - Primitives: `float`, `int32`, `int64`, `double`, `bool`
   - Enums: Various mode enums from `jon_shared_data_types.proto`
   - Complex objects: GPS coordinates, LIRA targets
   - No parameters: Many commands have empty message bodies

4. **Validation Constraints**:
   - Float ranges: Often 0.0-1.0, -1.0 to 1.0, or angle ranges
   - Enum validation: `defined_only: true, not_in: [0]`
   - Geographic constraints: latitude (-90 to 90), longitude (-180 to 180)
   - Hardware limits: zoom levels, focus ranges

5. **Action Naming Convention**:
   - Convert from ProtoCase to kebab-case
   - Examples:
     - `RotateToNDC` → `rotary-goto-ndc`
     - `SetAutoFocus` → `cv-set-auto-focus` or `heat-camera-set-auto-focus`
     - `StartTrackNDC` → `cv-start-track-ndc`

6. **Module Organization**:
   - Each proto file maps to a Java class
   - ~10-30 commands per module
   - Total: ~170 commands across 13 modules