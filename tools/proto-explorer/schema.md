# Complete Protobuf Schema Documentation

## Overview
This document provides a comprehensive schema reference for the command and state protobuf messages used in the system. All fields include their types and buf.validate constraints.

## Table of Contents
1. [State Messages (ser.JonGUIState)](#state-messages)
2. [Command Messages (cmd.Root)](#command-messages)
3. [Common Enumerations](#common-enumerations)
4. [Validation Constraints Reference](#validation-constraints-reference)

---

## State Messages

### ser.JonGUIState (Root State Container)
**Proto File:** `jon_shared_data.proto`  
**Java Class:** `ser.JonSharedData$JonGUIState`  
**Purpose:** Main container for all system state information

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | protocol_version | uint32 | `gt: 0` | Protocol version identifier |
| 2 | system | [ser.JonGuiDataSystem](#serjonguistatasystem) | `required: true` | System state and status |
| 3 | meteo_internal | ser.JonGuiDataMeteo | `required: true` | Internal meteorological data |
| 4 | lrf | [ser.JonGuiDataLrf](#serjonguistatalrf) | `required: true` | Laser Range Finder state |
| 5 | time | ser.JonGuiDataTime | `required: true` | Time information |
| 6 | gps | [ser.JonGuiDataGps](#serjonguistatagps) | `required: true` | GPS state data |
| 7 | compass | [ser.JonGuiDataCompass](#serjonguistatacompass) | `required: true` | Compass state |
| 8 | rotary | [ser.JonGuiDataRotary](#serjonguistatarotary) | `required: true` | Rotary platform state |
| 9 | camera_day | [ser.JonGuiDataCameraDay](#serjonguistatacameraday) | `required: true` | Day camera state |
| 10 | camera_heat | [ser.JonGuiDataCameraHeat](#serjonguistatacameraheat) | `required: true` | Heat camera state |
| 11 | compass_calibration | ser.JonGuiDataCompassCalibration | `required: true` | Compass calibration state |
| 12 | rec_osd | ser.JonGuiDataRecOsd | `required: true` | Recording/OSD state |
| 13 | day_cam_glass_heater | ser.JonGuiDataDayCamGlassHeater | `required: true` | Glass heater state |
| 14 | actual_space_time | ser.JonGuiDataActualSpaceTime | `required: true` | Actual space-time data |

### ser.JonGuiDataSystem
**Java Class:** `ser.JonSharedDataSystem$JonGuiDataSystem`  
**Purpose:** System health and operational status

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | cpu_temperature | float | `gte: -273.15, lte: 150.0` | CPU temperature in Celsius |
| 2 | gpu_temperature | float | `gte: -273.15, lte: 150.0` | GPU temperature in Celsius |
| 3 | gpu_load | float | `gte: 0.0, lte: 100.0` | GPU load percentage |
| 4 | cpu_load | float | `gte: 0.0, lte: 100.0` | CPU load percentage |
| 5 | power_consumption | float | `gte: 0.0, lte: 1000.0` | Power consumption in watts |
| 6 | loc | enum | `defined_only: true, not_in: 0` | Localization setting |
| 7-12 | cur_video_rec_dir_* | int32 | `gte: 0` | Video recording directory timestamps |
| 13 | rec_enabled | bool | - | Recording enabled flag |
| 14 | important_rec_enabled | bool | - | Important recording flag |
| 15 | low_disk_space | bool | - | Low disk space warning |
| 16 | no_disk_space | bool | - | No disk space error |
| 17 | disk_space | int32 | `gte: 0, lte: 100` | Available disk space percentage |
| 18 | cv_dumping | bool | - | Computer vision dumping active |
| 19 | vampire_mode | bool | - | Vampire mode active |
| 20 | tracking | bool | - | Tracking mode active |
| 21 | stabilization_mode | bool | - | Stabilization mode active |
| 22 | geodesic_mode | bool | - | Geodesic mode active |

### ser.JonGuiDataGps
**Java Class:** `ser.JonSharedDataGps$JonGuiDataGps`  
**Purpose:** GPS position and fix information

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | longitude | double | `gte: -180.0, lte: 180.0` | Current longitude |
| 2 | latitude | double | `gte: -90.0, lte: 90.0` | Current latitude |
| 3 | altitude | double | `gte: -433.0, lte: 8848.86` | Current altitude (meters) |
| 4 | manual_longitude | double | `gte: -180.0, lte: 180.0` | Manual longitude override |
| 5 | manual_latitude | double | `gte: -90.0, lte: 90.0` | Manual latitude override |
| 6 | manual_altitude | double | `gte: -433.0, lte: 8848.86` | Manual altitude override |
| 7 | fix_type | enum | `defined_only: true, not_in: 0` | GPS fix quality |
| 8 | use_manual | bool | - | Use manual position flag |

### ser.JonGuiDataCompass
**Java Class:** `ser.JonSharedDataCompass$JonGuiDataCompass`  
**Purpose:** Compass orientation data

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | azimuth | double | `gte: 0.0, lt: 360.0` | Compass azimuth (degrees) |
| 2 | elevation | double | `gte: -90.0, lte: 90.0` | Compass elevation (degrees) |
| 3 | bank | double | `gte: -180.0, lt: 180.0` | Bank angle (degrees) |
| 4 | offsetAzimuth | double | `gte: -180.0, lt: 180.0` | Azimuth offset correction |
| 5 | offsetElevation | double | `gte: -90.0, lte: 90.0` | Elevation offset correction |
| 6 | magneticDeclination | double | `gte: -180.0, lt: 180.0` | Magnetic declination |
| 7 | calibrating | bool | - | Calibration in progress |

### ser.JonGuiDataRotary
**Java Class:** `ser.JonSharedDataRotary$JonGuiDataRotary`  
**Purpose:** Rotary platform position and status

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | azimuth | float | `gte: 0.0, lt: 360.0` | Current azimuth position |
| 2 | azimuth_speed | float | `gte: -1.0, lte: 1.0` | Azimuth movement speed |
| 3 | elevation | float | `gte: -90.0, lte: 90.0` | Current elevation position |
| 4 | elevation_speed | float | `gte: -1.0, lte: 1.0` | Elevation movement speed |
| 5 | platform_azimuth | float | `gte: 0.0, lt: 360.0` | Platform azimuth |
| 6 | platform_elevation | float | `gte: -90.0, lte: 90.0` | Platform elevation |
| 7 | platform_bank | float | `gte: -180.0, lt: 180.0` | Platform bank angle |
| 8 | is_moving | bool | - | Platform is moving |
| 9 | mode | enum | `defined_only: true, not_in: 0` | Operating mode |
| 10 | is_scanning | bool | - | Scanning active |
| 11 | is_scanning_paused | bool | - | Scanning paused |
| 12 | use_rotary_as_compass | bool | - | Use rotary for compass |
| 13 | scan_target | int32 | `gte: 0` | Current scan target index |
| 14 | scan_target_max | int32 | `gte: 0` | Maximum scan targets |
| 15 | sun_azimuth | float | `gte: 0.0, lt: 360.0` | Sun azimuth position |
| 16 | sun_elevation | float | `gte: 0.0, lt: 360.0` | Sun elevation position |
| 17 | current_scan_node | [ser.ScanNode](#serscannode) | `required: true` | Current scan node details |

### ser.JonGuiDataCameraDay
**Java Class:** `ser.JonSharedDataCameraDay$JonGuiDataCameraDay`  
**Purpose:** Day camera state and settings

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | focus_pos | float | `gte: 0.0, lte: 1.0` | Focus position (0-1) |
| 2 | zoom_pos | float | `gte: 0.0, lte: 1.0` | Zoom position (0-1) |
| 3 | iris_pos | float | `gte: 0.0, lte: 1.0` | Iris position (0-1) |
| 4 | infrared_filter | bool | - | IR filter enabled |
| 5 | zoom_table_pos | int32 | `gte: 0` | Zoom table position |
| 6 | zoom_table_pos_max | int32 | `gte: 0` | Maximum zoom table position |
| 7 | fx_mode | enum | `defined_only: true` | Effects mode |
| 8 | auto_focus | bool | - | Auto-focus enabled |
| 9 | auto_iris | bool | - | Auto-iris enabled |
| 10 | digital_zoom_level | float | `gte: 1.0` | Digital zoom multiplier |
| 11 | clahe_level | float | `gte: 0.0, lte: 1.0` | CLAHE enhancement level |

### ser.JonGuiDataCameraHeat
**Java Class:** `ser.JonSharedDataCameraHeat$JonGuiDataCameraHeat`  
**Purpose:** Thermal camera state and settings

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | zoom_pos | float | `gte: 0.0, lte: 1.0` | Zoom position (0-1) |
| 2 | agc_mode | enum | `defined_only: true, not_in: 0` | AGC mode |
| 3 | filter | enum | `defined_only: true, not_in: 0` | Color filter mode |
| 4 | auto_focus | bool | - | Auto-focus enabled |
| 5 | zoom_table_pos | int32 | `gte: 0` | Zoom table position |
| 6 | zoom_table_pos_max | int32 | `gte: 0` | Maximum zoom table position |
| 7 | dde_level | int32 | `gte: 0, lte: 512` | DDE enhancement level |
| 8 | dde_enabled | bool | - | DDE enabled |
| 9 | fx_mode | enum | `defined_only: true` | Effects mode |
| 10 | digital_zoom_level | float | `gte: 1.0` | Digital zoom multiplier |
| 11 | clahe_level | float | `gte: 0.0, lte: 1.0` | CLAHE enhancement level |

### ser.JonGuiDataLrf
**Java Class:** `ser.JonSharedDataLrf$JonGuiDataLrf`  
**Purpose:** Laser Range Finder state

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | is_scanning | bool | - | LRF scanning active |
| 2 | is_measuring | bool | - | LRF measuring active |
| 3 | measure_id | int32 | `gte: 0` | Current measurement ID |
| 4 | target | [ser.JonGuiDataTarget](#serjonguistatatarget) | - | Target information |
| 5 | pointer_mode | enum | `defined_only: true` | Laser pointer mode |
| 6 | fogModeEnabled | bool | - | Fog mode enabled |
| 7 | is_refining | bool | - | Refining measurement |

### ser.JonGuiDataTarget
**Java Class:** `ser.JonSharedDataLrf$JonGuiDataTarget`  
**Purpose:** LRF target measurement data

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | timestamp | int64 | `gte: 0` | Measurement timestamp |
| 2 | target_longitude | double | `gte: -180.0, lte: 180.0` | Target longitude |
| 3 | target_latitude | double | `gte: -90.0, lte: 90.0` | Target latitude |
| 4 | target_altitude | double | - | Target altitude |
| 5 | observer_longitude | double | `gte: -180.0, lte: 180.0` | Observer longitude |
| 6 | observer_latitude | double | `gte: -90.0, lte: 90.0` | Observer latitude |
| 7 | observer_altitude | double | - | Observer altitude |
| 8 | observer_azimuth | double | `gte: 0.0, lt: 360.0` | Observer azimuth |
| 9 | observer_elevation | double | `gte: -90.0, lte: 90.0` | Observer elevation |
| 10 | observer_bank | double | `gte: -180.0, lt: 180.0` | Observer bank angle |
| 11 | distance_2d | double | `gte: 0.0, lte: 500000.0` | 2D distance (meters) |
| 12 | distance_3d | double | `gte: 0.0, lte: 500000.0` | 3D distance (meters) |
| 13 | observer_fix_type | enum | `defined_only: true, not_in: 0` | GPS fix type |
| 14 | session_id | int32 | `gte: 0` | Session identifier |
| 15 | target_id | int32 | `gte: 0` | Target identifier |
| 16 | type | int32 | - | Target type |
| 17 | target_color | [ser.RgbColor](#serrgbcolor) | - | Target display color |
| 18-21 | uuid_part1-4 | int32 | - | UUID components |

### ser.ScanNode
**Java Class:** `ser.JonSharedDataRotary$ScanNode`  
**Purpose:** Scan point definition

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | index | int32 | `gte: 0` | Node index |
| 2 | DayZoomTableValue | int32 | `gte: 0` | Day camera zoom setting |
| 3 | HeatZoomTableValue | int32 | `gte: 0` | Heat camera zoom setting |
| 4 | azimuth | double | `gte: 0.0, lt: 360.0` | Scan azimuth |
| 5 | elevation | double | `gte: -90.0, lte: 90.0` | Scan elevation |
| 6 | linger | double | `gte: 0` | Dwell time (seconds) |
| 7 | speed | double | `gt: 0.0, lte: 1.0` | Movement speed |

### ser.RgbColor
**Java Class:** `ser.JonSharedDataLrf$RgbColor`  
**Purpose:** RGB color definition

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | red | uint32 | `gte: 0, lte: 255` | Red component |
| 2 | green | uint32 | `gte: 0, lte: 255` | Green component |
| 3 | blue | uint32 | `gte: 0, lte: 255` | Blue component |

---

## Command Messages

### cmd.Root
**Proto File:** `jon_shared_cmd.proto`  
**Java Class:** `cmd.JonSharedCmd$Root`  
**Purpose:** Root command container with payload selection

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | protocol_version | uint32 | `gt: 0` | Protocol version |
| 2 | session_id | uint32 | - | Session identifier |
| 3 | important | bool | - | Mark as important |
| 4 | from_cv_subsystem | bool | - | From CV subsystem |
| 5 | client_type | enum | `defined_only: true, not_in: 0` | Client type |

**Payload Fields (one-of):**
| Field | Name | Type | Description |
|-------|------|------|-------------|
| 20 | day_camera | [cmd.DayCamera.Root](#cmddaycameraroot) | Day camera commands |
| 21 | heat_camera | [cmd.HeatCamera.Root](#cmdheatcameraroot) | Heat camera commands |
| 22 | gps | cmd.Gps.Root | GPS commands |
| 23 | compass | cmd.Compass.Root | Compass commands |
| 24 | lrf | cmd.Lrf.Root | LRF commands |
| 25 | lrf_calib | cmd.Lrf_calib.Root | LRF calibration |
| 26 | rotary | [cmd.RotaryPlatform.Root](#cmdrotaryplatformroot) | Rotary platform commands |
| 27 | osd | cmd.OSD.Root | OSD commands |
| 28 | ping | cmd.Ping | Ping command |
| 29 | noop | cmd.Noop | No operation |
| 30 | frozen | cmd.Frozen | Frozen command |
| 31 | system | cmd.System.Root | System commands |
| 32 | cv | cmd.CV.Root | Computer vision commands |
| 33 | day_cam_glass_heater | cmd.DayCamGlassHeater.Root | Glass heater commands |
| 34 | lira | cmd.Lira.Root | LIRA commands |

### cmd.DayCamera.Root
**Java Class:** `cmd.DayCamera.JonSharedCmdDayCamera$Root`  
**Purpose:** Day camera command selection (17 commands)

**Available Commands (one-of):**
- `focus`: Focus control commands
- `zoom`: Zoom control commands
- `set_iris`: Iris position setting
- `set_infra_red_filter`: IR filter control
- `start`: Start camera
- `stop`: Stop camera
- `photo`: Capture photo
- `set_auto_iris`: Auto-iris control
- `halt_all`: Stop all operations
- `set_fx_mode`: Effects mode selection
- `set_auto_focus`: Auto-focus control
- `set_digital_zoom_level`: Digital zoom setting
- `set_clahe_level`: CLAHE enhancement
- `set_optical_pos`: Optical position
- `set_optical_day_zoom_table_pos`: Zoom table position
- `get_optical_day_zoom_table_pos`: Query zoom position
- `move_optical_day_zoom_table`: Move zoom table

### cmd.HeatCamera.Root
**Java Class:** `cmd.HeatCamera.JonSharedCmdHeatCamera$Root`  
**Purpose:** Heat camera command selection (31 commands)

**Available Commands (one-of):**
- Focus controls: `focus_in`, `focus_out`, `focus_step`, `focus_stop`
- Zoom controls: `zoom_in`, `zoom_out`, `zoom_step`, `zoom_stop`
- AGC controls: `agc_1`, `agc_2`, `agc_3`
- DDE controls: `dde_increase`, `dde_decrease`, `dde_set`
- Calibration: `calibrate`, `calibrate_cooler`
- Filter controls: Various filter modes
- FX mode controls
- Digital zoom controls
- CLAHE controls
- Auto-focus controls

### cmd.RotaryPlatform.Root
**Java Class:** `cmd.RotaryPlatform.JonSharedCmdRotary$Root`  
**Purpose:** Rotary platform command selection (24 commands)

**Available Commands (one-of):**
- Movement: `move`, `stop`, `targeting`, `targeting_gps`
- Position: `set_position`, `set_platform_orientation`
- Scanning: `scan_start`, `scan_stop`, `scan_pause`, `scan_navigate`
- Scan management: `scan_add_node`, `scan_remove_node`, `scan_clear`
- GPS targeting: `move_to_gps`
- Platform adjustments: `set_platform_bank`, `set_platform_elevation`

### cmd.RotaryPlatform.Move
**Java Class:** `cmd.RotaryPlatform.JonSharedCmdRotary$Move`  
**Purpose:** Rotary movement command

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | azimuth_speed | float | `gte: -1.0, lte: 1.0` | Azimuth speed (-1 to 1) |
| 2 | elevation_speed | float | `gte: -1.0, lte: 1.0` | Elevation speed (-1 to 1) |

### cmd.RotaryPlatform.SetPosition
**Java Class:** `cmd.RotaryPlatform.JonSharedCmdRotary$SetPosition`  
**Purpose:** Set absolute position

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | azimuth | float | `gte: 0.0, lt: 360.0` | Target azimuth |
| 2 | elevation | float | `gte: -90.0, lte: 90.0` | Target elevation |

### cmd.RotaryPlatform.ScanAddNode
**Java Class:** `cmd.RotaryPlatform.JonSharedCmdRotary$ScanAddNode`  
**Purpose:** Add scan node

| Field | Name | Type | Constraints | Description |
|-------|------|------|-------------|-------------|
| 1 | index | int32 | `gte: 0` | Node index |
| 2 | DayZoomTableValue | int32 | `gte: 0` | Day zoom setting |
| 3 | HeatZoomTableValue | int32 | `gte: 0` | Heat zoom setting |
| 4 | azimuth | double | `gte: 0.0, lt: 360.0` | Node azimuth |
| 5 | elevation | double | `gte: -90.0, lte: 90.0` | Node elevation |
| 6 | linger | double | `gte: 0` | Dwell time |
| 7 | speed | double | `gt: 0.0, lte: 1.0` | Movement speed |

---

## Common Enumerations

### JonGuiDataSystemLocalizations
- `JON_GUI_DATA_SYSTEM_LOCALIZATION_UNSPECIFIED` (0) - Invalid
- `JON_GUI_DATA_SYSTEM_LOCALIZATION_CS` - Czech
- `JON_GUI_DATA_SYSTEM_LOCALIZATION_AR` - Arabic
- `JON_GUI_DATA_SYSTEM_LOCALIZATION_EN` - English
- `JON_GUI_DATA_SYSTEM_LOCALIZATION_UA` - Ukrainian

### JonGuiDataGpsFixType
- `JON_GUI_DATA_GPS_FIX_TYPE_UNSPECIFIED` (0) - Invalid
- `JON_GUI_DATA_GPS_FIX_TYPE_NONE` - No fix
- `JON_GUI_DATA_GPS_FIX_TYPE_1D` - 1D fix
- `JON_GUI_DATA_GPS_FIX_TYPE_2D` - 2D fix
- `JON_GUI_DATA_GPS_FIX_TYPE_3D` - 3D fix
- `JON_GUI_DATA_GPS_FIX_TYPE_MANUAL` - Manual position

### JonGuiDataRotaryMode
- `JON_GUI_DATA_ROTARY_MODE_UNSPECIFIED` (0) - Invalid
- `JON_GUI_DATA_ROTARY_MODE_INITIALIZATION` - Initializing
- `JON_GUI_DATA_ROTARY_MODE_POSITION` - Position mode
- `JON_GUI_DATA_ROTARY_MODE_SPEED` - Speed mode
- `JON_GUI_DATA_ROTARY_MODE_TARGETING` - Targeting mode
- `JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER` - Video tracking
- `JON_GUI_DATA_ROTARY_MODE_STABILIZATION` - Stabilization

### JonGuiDataFxModeDay
- `JON_GUI_DATA_FX_MODE_DAY_DEFAULT` - Default mode
- `JON_GUI_DATA_FX_MODE_DAY_A` through `JON_GUI_DATA_FX_MODE_DAY_F` - Effect modes A-F

### JonGuiDataFxModeHeat
- `JON_GUI_DATA_FX_MODE_HEAT_DEFAULT` - Default mode
- `JON_GUI_DATA_FX_MODE_HEAT_A` through `JON_GUI_DATA_FX_MODE_HEAT_F` - Effect modes A-F

### JonGuiDataVideoChannelHeatAGCModes
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_UNSPECIFIED` (0) - Invalid
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_1` - AGC Mode 1
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_2` - AGC Mode 2
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_AGC_MODE_3` - AGC Mode 3

### JonGuiDataVideoChannelHeatFilters
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_UNSPECIFIED` (0) - Invalid
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_BLACK` - Hot black
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_HOT_WHITE` - Hot white
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA` - Sepia
- `JON_GUI_DATA_VIDEO_CHANNEL_HEAT_FILTER_SEPIA_INVERSE` - Inverse sepia

### JonGuiDataLrfLaserPointerModes
- `JON_GUI_DATA_LRF_LASER_POINTER_MODE_OFF` - Laser off
- `JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_1` - Mode 1
- `JON_GUI_DATA_LRF_LASER_POINTER_MODE_ON_2` - Mode 2

### JonGuiDataClientType
- `JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED` (0) - Invalid
- `JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK` - Local network client
- `JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED` - Certificate-protected client
- `JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV` - Internal CV subsystem
- `JON_GUI_DATA_CLIENT_TYPE_LIRA` - LIRA system

---

## Validation Constraints Reference

### Constraint Types Used

#### Numeric Constraints
- `gt: N` - Greater than N
- `gte: N` - Greater than or equal to N
- `lt: N` - Less than N
- `lte: N` - Less than or equal to N

#### Field Constraints
- `required: true` - Field must be present
- `defined_only: true` - Enum must be a defined value
- `not_in: 0` - Value cannot be 0 (typically for enums)

### Common Validation Patterns

#### Temperature Ranges
- CPU/GPU: `-273.15°C ≤ temp ≤ 150.0°C`

#### Percentage Values
- Load/Disk: `0% ≤ value ≤ 100%`

#### Geographic Coordinates
- Longitude: `-180° ≤ lon ≤ 180°`
- Latitude: `-90° ≤ lat ≤ 90°`
- Altitude: `-433m ≤ alt ≤ 8848.86m` (Dead Sea to Everest)

#### Angular Measurements
- Azimuth: `0° ≤ azimuth < 360°`
- Elevation: `-90° ≤ elevation ≤ 90°`
- Bank: `-180° ≤ bank < 180°`

#### Speed Values
- Normalized: `-1.0 ≤ speed ≤ 1.0`
- Positive only: `0.0 < speed ≤ 1.0`

#### Distance Measurements
- LRF Range: `0m ≤ distance ≤ 500,000m`

#### Zoom/Focus Positions
- Normalized: `0.0 ≤ position ≤ 1.0`
- Digital zoom: `zoom ≥ 1.0`

#### Color Components
- RGB: `0 ≤ component ≤ 255`

---

## Usage Examples

### Creating a State Message
```protobuf
ser.JonGUIState state = {
  protocol_version: 1,  // Must be > 0
  system: { ... },       // Required
  gps: { ... },         // Required
  compass: { ... },     // Required
  rotary: { ... },      // Required
  // All fields must be present
};
```

### Creating a Command Message
```protobuf
cmd.Root command = {
  protocol_version: 1,  // Must be > 0
  session_id: 12345,
  client_type: JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK,
  
  // One payload field:
  day_camera: {
    zoom: {
      target_value: 0.5,  // 0.0 ≤ value ≤ 1.0
      speed: 0.8          // 0.0 ≤ value ≤ 1.0
    }
  }
};
```

### Validation Examples

#### Valid GPS Position
```protobuf
gps: {
  longitude: -122.4194,    // Valid: -180 ≤ value ≤ 180
  latitude: 37.7749,       // Valid: -90 ≤ value ≤ 90
  altitude: 52.0,          // Valid: -433 ≤ value ≤ 8848.86
  fix_type: JON_GUI_DATA_GPS_FIX_TYPE_3D  // Valid: defined value
}
```

#### Invalid Examples
```protobuf
// INVALID: Temperature out of range
cpu_temperature: -300.0  // Error: < -273.15

// INVALID: Enum not defined
fix_type: 0  // Error: not_in: 0

// INVALID: Angle out of range
azimuth: 360.0  // Error: must be < 360.0

// INVALID: Missing required field
ser.JonGUIState state = {
  protocol_version: 1
  // Error: missing required 'system' field
};
```

---

## Notes and Best Practices

1. **Protocol Version**: Always set `protocol_version > 0` in both command and state messages
2. **Required Fields**: All state sub-messages are required in `ser.JonGUIState`
3. **Enum Validation**: Most enums exclude value 0 (UNSPECIFIED) with `not_in: 0`
4. **Angular Ranges**: Be careful with inclusive vs exclusive bounds (e.g., azimuth < 360°)
5. **One-of Fields**: Command messages use one-of for payload selection
6. **Normalization**: Many values use 0.0-1.0 normalized range for consistency
7. **Safety Limits**: Physical constraints (temperature, altitude) prevent invalid hardware states
8. **Session Management**: Use `session_id` for tracking command sequences
9. **Client Types**: Set appropriate `client_type` for access control
10. **Measurement Units**: 
    - Angles: degrees
    - Distance: meters
    - Temperature: Celsius
    - Speed: normalized (-1 to 1)
    - Time: Unix timestamp (int64)