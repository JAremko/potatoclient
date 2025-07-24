# Buf.validate Constraints Summary

This document provides a comprehensive list of all `buf.validate` constraints found in the PotatoClient protobuf files, organized by proto file and message type.

## Command Proto Files

### jon_shared_cmd.proto
- **Root**
  - `protocol_version`: `uint32` - `gt: 0`
  - `client_type`: `enum` - `defined_only: true, not_in: [0]`
  - `payload`: `oneof` - `required: true`

### jon_shared_cmd_compass.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **SetMagneticDeclination**
  - `value`: `float` - `gte: -180, lt: 180`
- **SetOffsetAngleAzimuth**
  - `value`: `float` - `gte: -180, lt: 180`
- **SetOffsetAngleElevation**
  - `value`: `float` - `gte: -90, lte: 90`

### jon_shared_cmd_cv.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **SetAutoFocus**
  - `channel`: `enum` - `defined_only: true, not_in: [0]`
- **StartTrackNDC**
  - `channel`: `enum` - `defined_only: true, not_in: [0]`
  - `x`: `float` - `gte: -1.0, lte: 1.0`
  - `y`: `float` - `gte: -1.0, lte: 1.0`

### jon_shared_cmd_day_camera.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **Focus**
  - `cmd`: `oneof` - `required: true`
- **Zoom**
  - `cmd`: `oneof` - `required: true`
- **SetValue**
  - `value`: `float` - `gte: 0.0, lte: 1.0`
- **Move**
  - `target_value`: `float` - `gte: 0.0, lte: 1.0`
  - `speed`: `float` - `gte: 0.0, lte: 1.0`
- **Offset**
  - `offset_value`: `float` - `gte: -1.0, lte: 1.0`
- **SetClaheLevel**
  - `value`: `float` - `gte: 0.0, lte: 1.0`
- **ShiftClaheLevel**
  - `value`: `float` - `gte: -1.0, lte: 1.0`
- **SetFxMode**
  - `mode`: `enum` - `defined_only: true, not_in: [0]`
- **SetDigitalZoomLevel**
  - `value`: `float` - `gte: 1.0`
- **SetIris**
  - `value`: `float` - `gte: 0.0, lte: 1.0`
- **SetZoomTableValue**
  - `value`: `int32` - `gte: 0`

### jon_shared_cmd_day_cam_glass_heater.proto
- **Root**
  - `cmd`: `oneof` - `required: true`

### jon_shared_cmd_gps.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **SetManualPosition**
  - `latitude`: `float` - `gte: -90.0, lte: 90.0`
  - `longitude`: `float` - `gte: -180.0, lt: 180.0`
  - `altitude`: `float` - `gte: -432.0, lte: 8848.0`

### jon_shared_cmd_heat_camera.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **Zoom**
  - `cmd`: `oneof` - `required: true`
- **SetFxMode**
  - `mode`: `enum` - `defined_only: true, not_in: [0]`
- **SetClaheLevel**
  - `value`: `float` - `gte: 0.0, lte: 1.0`
- **ShiftClaheLevel**
  - `value`: `float` - `gte: -1.0, lte: 1.0`
- **SetValue**
  - `value`: `float` - `gte: 0.0, lte: 1.0`
- **SetDDELevel**
  - `value`: `int32` - `gte: 0, lte: 100`
- **SetDigitalZoomLevel**
  - `value`: `float` - `gte: 1.0`
- **ShiftDDE**
  - `value`: `int32` - `gte: -100, lte: 100`
- **SetZoomTableValue**
  - `value`: `int32` - `gte: 0`
- **SetAGC**
  - `value`: `enum` - `defined_only: true, not_in: [0]`
- **SetFilters**
  - `value`: `enum` - `defined_only: true, not_in: [0]`

### jon_shared_cmd_lira.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **JonGuiDataLiraTarget**
  - `timestamp`: `int64` - `gte: 0`
  - `target_longitude`: `double` - `gte: -180, lte: 180`
  - `target_latitude`: `double` - `gte: -90, lte: 90`
  - `target_azimuth`: `double` - `gte: 0, lt: 360`
  - `target_elevation`: `double` - `gte: -90, lte: 90`
  - `distance`: `double` - `gte: 0`

### jon_shared_cmd_lrf_align.proto
- **Offsets**
  - `cmd`: `oneof` - `required: true`
- **SetOffsets**
  - `x`: `int32` - `gte: -1920, lte: 1920`
  - `y`: `int32` - `gte: -1080, lte: 1080`
- **ShiftOffsetsBy**
  - `x`: `int32` - `gte: -1920, lte: 1920`
  - `y`: `int32` - `gte: -1080, lte: 1080`

### jon_shared_cmd_lrf.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **SetScanMode**
  - `mode`: `enum` - `defined_only: true, not_in: [0]`

### jon_shared_cmd_osd.proto
- **Root**
  - `cmd`: `oneof` - `required: true`

### jon_shared_cmd_rotary.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **Azimuth**
  - `cmd`: `oneof` - `required: true`
- **Elevation**
  - `cmd`: `oneof` - `required: true`
- **SetMode**
  - `mode`: `enum` - `defined_only: true, not_in: [0]`
- **SetAzimuthValue**
  - `value`: `float` - `gte: 0.0, lt: 360.0`
  - `direction`: `enum` - `defined_only: true, not_in: [0]`
- **RotateAzimuthTo**
  - `target_value`: `float` - `gte: 0.0, lt: 360.0`
  - `speed`: `float` - `gte: 0.0, lte: 1.0`
  - `direction`: `enum` - `defined_only: true, not_in: [0]`
- **RotateAzimuth**
  - `speed`: `float` - `gte: 0.0, lte: 1.0`
  - `direction`: `enum` - `defined_only: true, not_in: [0]`
- **RotateElevation**
  - `speed`: `float` - `gte: 0.0, lte: 1.0`
  - `direction`: `enum` - `defined_only: true, not_in: [0]`
- **SetElevationValue**
  - `value`: `float` - `gte: -90.0, lte: 90.0`
- **RotateElevationTo**
  - `target_value`: `float` - `gte: -90.0, lte: 90.0`
  - `speed`: `float` - `gte: 0.0, lte: 1.0`
- **RotateElevationRelative**
  - `speed`: `float` - `gte: 0.0, lte: 1.0`
  - `direction`: `enum` - `defined_only: true, not_in: [0]`
- **RotateElevationRelativeSet**
  - `direction`: `enum` - `defined_only: true, not_in: [0]`
- **RotateAzimuthRelative**
  - `speed`: `float` - `gte: 0.0, lte: 1.0`
  - `direction`: `enum` - `defined_only: true, not_in: [0]`
- **RotateAzimuthRelativeSet**
  - `direction`: `enum` - `defined_only: true, not_in: [0]`
- **SetPlatformAzimuth**
  - `value`: `float` - `gt: -360.0, lt: 360.0`
- **SetPlatformElevation**
  - `value`: `float` - `gte: -90.0, lte: 90.0`
- **SetPlatformBank**
  - `value`: `float` - `gte: -180.0, lt: 180.0`
- **ScanSelectNode**
  - `index`: `int32` - `gte: 0`
- **ScanDeleteNode**
  - `index`: `int32` - `gte: 0`
- **ScanUpdateNode**
  - `index`: `int32` - `gte: 0`
  - `DayZoomTableValue`: `int32` - `gte: 0`
  - `HeatZoomTableValue`: `int32` - `gte: 0`
  - `azimuth`: `double` - `gte: 0, lt: 360`
  - `elevation`: `double` - `gte: -90, lte: 90`
  - `linger`: `double` - `gte: 0`
  - `speed`: `double` - `gt: 0.0, lte: 1.0`
- **ScanAddNode**
  - `index`: `int32` - `gte: 0`
  - `DayZoomTableValue`: `int32` - `gte: 0`
  - `HeatZoomTableValue`: `int32` - `gte: 0`
  - `azimuth`: `double` - `gte: 0, lt: 360`
  - `elevation`: `double` - `gte: -90, lte: 90`
  - `linger`: `double` - `gte: 0`
  - `speed`: `double` - `gt: 0.0, lte: 1.0`
- **RotateToGPS**
  - `latitude`: `float` - `gte: -90.0, lte: 90.0`
  - `longitude`: `float` - `gte: -180.0, lt: 180.0`
- **SetOriginGPS**
  - `latitude`: `float` - `gte: -90.0, lte: 90.0`
  - `longitude`: `float` - `gte: -180.0, lt: 180.0`
- **RotateToNDC**
  - `channel`: `enum` - `defined_only: true, not_in: [0]`
  - `x`: `float` - `gte: -1.0, lte: 1.0`
  - `y`: `float` - `gte: -1.0, lte: 1.0`

### jon_shared_cmd_system.proto
- **Root**
  - `cmd`: `oneof` - `required: true`
- **SetLocalization**
  - `loc`: `enum` - `defined_only: true, not_in: [0]`

## Data Proto Files

### jon_shared_data.proto
- **JonGUIState**
  - `protocol_version`: `uint32` - `gt: 0`
  - `system`: `message` - `required: true`
  - `meteo_internal`: `message` - `required: true`
  - `lrf`: `message` - `required: true`
  - `time`: `message` - `required: true`
  - `gps`: `message` - `required: true`
  - `compass`: `message` - `required: true`
  - `rotary`: `message` - `required: true`
  - `camera_day`: `message` - `required: true`
  - `camera_heat`: `message` - `required: true`
  - `compass_calibration`: `message` - `required: true`
  - `rec_osd`: `message` - `required: true`
  - `day_cam_glass_heater`: `message` - `required: true`
  - `actual_space_time`: `message` - `required: true`

### jon_shared_data_actual_space_time.proto
- **JonGuiDataActualSpaceTime**
  - `azimuth`: `float` - `gte: 0.0, lt: 360.0`
  - `elevation`: `float` - `gte: -90.0, lte: 90.0`
  - `bank`: `float` - `gte: -180.0, lt: 180.0`
  - `latitude`: `float` - `gte: -90.0, lte: 90.0`
  - `longitude`: `float` - `gte: -180.0, lt: 180.0`
  - `altitude`: `double` - `gte: -433.0, lte: 8848.86`
  - `timestamp`: `int64` - `gte: 0`

### jon_shared_data_camera_day.proto
- **JonGuiDataCameraDay**
  - `focus_pos`: `float` - `gte: 0.0, lte: 1.0`
  - `zoom_pos`: `float` - `gte: 0.0, lte: 1.0`
  - `iris_pos`: `float` - `gte: 0.0, lte: 1.0`
  - `zoom_table_pos`: `int32` - `gte: 0`
  - `zoom_table_pos_max`: `int32` - `gte: 0`
  - `fx_mode`: `enum` - `defined_only: true`
  - `digital_zoom_level`: `float` - `gte: 1.0`
  - `clahe_level`: `float` - `gte: 0.0, lte: 1.0`

### jon_shared_data_camera_heat.proto
- **JonGuiDataCameraHeat**
  - `zoom_pos`: `float` - `gte: 0.0, lte: 1.0`
  - `agc_mode`: `enum` - `defined_only: true, not_in: [0]`
  - `filter`: `enum` - `defined_only: true, not_in: [0]`
  - `zoom_table_pos`: `int32` - `gte: 0`
  - `zoom_table_pos_max`: `int32` - `gte: 0`
  - `dde_level`: `int32` - `gte: 0, lte: 512`
  - `fx_mode`: `enum` - `defined_only: true`
  - `digital_zoom_level`: `float` - `gte: 1.0`
  - `clahe_level`: `float` - `gte: 0.0, lte: 1.0`

### jon_shared_data_compass_calibration.proto
- **JonGuiDataCompassCalibration**
  - `stage`: `uint32` - `gte: 0`
  - `final_stage`: `uint32` - `gt: 0`
  - `target_azimuth`: `double` - `gte: 0, lt: 360`
  - `target_elevation`: `double` - `gte: -90, lte: 90`
  - `target_bank`: `double` - `gte: -180, lt: 180`
  - `status`: `enum` - `defined_only: true, not_in: [0]`

### jon_shared_data_compass.proto
- **JonGuiDataCompass**
  - `azimuth`: `double` - `gte: 0, lt: 360`
  - `elevation`: `double` - `gte: -90, lte: 90`
  - `bank`: `double` - `gte: -180, lt: 180`
  - `offsetAzimuth`: `double` - `gte: -180, lt: 180`
  - `offsetElevation`: `double` - `gte: -90, lte: 90`
  - `magneticDeclination`: `double` - `gte: -180, lt: 180`

### jon_shared_data_day_cam_glass_heater.proto
- **JonGuiDataDayCamGlassHeater**
  - `temperature`: `double` - `gte: -273.15, lte: 660.32`

### jon_shared_data_gps.proto
- **JonGuiDataGps**
  - `longitude`: `double` - `gte: -180.0, lte: 180.0`
  - `latitude`: `double` - `gte: -90.0, lte: 90.0`
  - `altitude`: `double` - `gte: -433.0, lte: 8848.86`
  - `manual_longitude`: `double` - `gte: -180.0, lte: 180.0`
  - `manual_latitude`: `double` - `gte: -90.0, lte: 90.0`
  - `manual_altitude`: `double` - `gte: -433.0, lte: 8848.86`
  - `fix_type`: `enum` - `defined_only: true, not_in: [0]`

### jon_shared_data_lrf.proto
- **JonGuiDataLrf**
  - `measure_id`: `int32` - `gte: 0`
  - `pointer_mode`: `enum` - `defined_only: true`
- **JonGuiDataTarget**
  - `timestamp`: `int64` - `gte: 0`
  - `target_longitude`: `double` - `gte: -180, lte: 180`
  - `target_latitude`: `double` - `gte: -90, lte: 90`
  - `observer_longitude`: `double` - `gte: -180, lte: 180`
  - `observer_latitude`: `double` - `gte: -90, lte: 90`
  - `observer_azimuth`: `double` - `gte: 0, lt: 360`
  - `observer_elevation`: `double` - `gte: -90, lte: 90`
  - `observer_bank`: `double` - `gte: -180, lt: 180`
  - `distance_2d`: `double` - `gte: 0, lte: 500000`
  - `distance_3b`: `double` - `gte: 0, lte: 500000`
  - `observer_fix_type`: `enum` - `defined_only: true, not_in: [0]`
  - `session_id`: `int32` - `gte: 0`
  - `target_id`: `int32` - `gte: 0`
- **RgbColor**
  - `red`: `uint32` - `gte: 0, lte: 255`
  - `green`: `uint32` - `gte: 0, lte: 255`
  - `blue`: `uint32` - `gte: 0, lte: 255`

### jon_shared_data_rec_osd.proto
- **JonGuiDataRecOsd**
  - `screen`: `enum` - `defined_only: true, not_in: [0]`

### jon_shared_data_rotary.proto
- **JonGuiDataRotary**
  - `azimuth`: `float` - `gte: 0, lt: 360`
  - `azimuth_speed`: `float` - `gte: -1.0, lte: 1.0`
  - `elevation`: `float` - `gte: -90, lte: 90`
  - `elevation_speed`: `float` - `gte: -1.0, lte: 1.0`
  - `platform_azimuth`: `float` - `gte: 0, lt: 360`
  - `platform_elevation`: `float` - `gte: -90, lte: 90`
  - `platform_bank`: `float` - `gte: -180, lt: 180`
  - `mode`: `enum` - `defined_only: true, not_in: [0]`
  - `scan_target`: `int32` - `gte: 0`
  - `scan_target_max`: `int32` - `gte: 0`
  - `sun_azimuth`: `float` - `gte: 0, lt: 360`
  - `sun_elevation`: `float` - `gte: 0, lt: 360`
  - `current_scan_node`: `message` - `required: true`
- **ScanNode**
  - `index`: `int32` - `gte: 0`
  - `DayZoomTableValue`: `int32` - `gte: 0`
  - `HeatZoomTableValue`: `int32` - `gte: 0`
  - `azimuth`: `double` - `gte: 0, lt: 360`
  - `elevation`: `double` - `gte: -90, lte: 90`
  - `linger`: `double` - `gte: 0`
  - `speed`: `double` - `gt: 0.0, lte: 1.0`

### jon_shared_data_system.proto
- **JonGuiDataSystem**
  - `cpu_temperature`: `float` - `gte: -273.15, lte: 150`
  - `gpu_temperature`: `float` - `gte: -273.15, lte: 150`
  - `gpu_load`: `float` - `gte: 0, lte: 100`
  - `cpu_load`: `float` - `gte: 0, lte: 100`
  - `power_consumption`: `float` - `gte: 0, lte: 1000`
  - `loc`: `enum` - `defined_only: true, not_in: [0]`
  - `cur_video_rec_dir_year`: `int32` - `gte: 0`
  - `cur_video_rec_dir_month`: `int32` - `gte: 0`
  - `cur_video_rec_dir_day`: `int32` - `gte: 0`
  - `cur_video_rec_dir_hour`: `int32` - `gte: 0`
  - `cur_video_rec_dir_minute`: `int32` - `gte: 0`
  - `cur_video_rec_dir_second`: `int32` - `gte: 0`
  - `disk_space`: `int32` - `gte: 0, lte: 100`

### jon_shared_data_time.proto
- **JonGuiDataTime**
  - `timestamp`: `int64` - `gte: 0`
  - `manual_timestamp`: `int64` - `gte: 0`

## Common Validation Patterns

### Numeric Ranges
- **Angles (Azimuth)**: `gte: 0, lt: 360` or `gte: 0, lte: 360`
- **Angles (Elevation)**: `gte: -90, lte: 90`
- **Angles (Bank/Roll)**: `gte: -180, lt: 180`
- **Normalized Values**: `gte: 0.0, lte: 1.0`
- **Speed Values**: `gte: 0.0, lte: 1.0`
- **GPS Latitude**: `gte: -90.0, lte: 90.0`
- **GPS Longitude**: `gte: -180.0, lte: 180.0` or `gte: -180.0, lt: 180.0`
- **Altitude**: `gte: -433.0, lte: 8848.86` (Dead Sea to Mt. Everest)
- **Temperature**: `gte: -273.15` (absolute zero)
- **Percentages**: `gte: 0, lte: 100`
- **RGB Colors**: `gte: 0, lte: 255`
- **Timestamps**: `gte: 0`

### Enum Validation
- Most enums use: `defined_only: true, not_in: [0]`
- Some enums only use: `defined_only: true`

### Required Fields
- Used extensively in `jon_shared_data.proto` for all subsystem messages
- All `oneof` command fields are required

### Special Constraints
- Digital zoom levels: `gte: 1.0` (no upper limit)
- Distance values: `gte: 0, lte: 500000` (50km in decimeters)
- LRF offsets: x: `gte: -1920, lte: 1920`, y: `gte: -1080, lte: 1080`
- DDE level: `gte: 0, lte: 512`
- Protocol version: `gt: 0`