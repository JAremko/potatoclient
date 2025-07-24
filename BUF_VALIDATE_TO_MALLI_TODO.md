# TODO: Port All buf.validate Constraints to Malli Schemas

This document tracks the porting of all buf.validate constraints from protobuf files to Malli schemas for runtime validation.

## Legend
- ✅ = Already implemented correctly
- ⚠️ = Implemented but needs adjustment
- ❌ = Not implemented yet
- 🔍 = Needs investigation

## Command Schemas (`potatoclient.specs`)

### Angle Values
- ✅ `azimuth-degrees` - Fixed to use `[:double {:min 0.0 :max 359.999999}]` for `lt: 360`
- ✅ `elevation-degrees` - `[:double {:min -90.0 :max 90.0}]` matches proto
- ✅ `bank-angle` - Fixed to use `[:double {:min -180.0 :max 179.999999}]` for `lt: 180`
- ✅ `relative-angle` - Fixed to use `[:double {:min -359.999999 :max 359.999999}]` for `gt: -360, lt: 360`

### GPS Values
- ✅ `gps-latitude` - `[:double {:min -90.0 :max 90.0}]` matches proto
- ✅ `gps-longitude` - Fixed to use `[:double {:min -180.0 :max 179.999999}]` for `lt: 180`
- ✅ `gps-altitude` - `[:double {:min -433.0 :max 8848.86}]` matches proto (Dead Sea to Everest)

### Normalized Values
- ✅ `normalized-value` - `[:double {:min 0.0 :max 1.0}]` matches proto
- ✅ `rotation-speed` - `[:double {:min 0.0 :max 1.0}]` matches proto
- ✅ `focus-value` - `[:double {:min 0.0 :max 1.0}]` matches proto
- ✅ `iris-value` - `[:double {:min 0.0 :max 1.0}]` matches proto
- ✅ `clahe-level` - `[:double {:min 0.0 :max 1.0}]` matches proto
- ✅ `clahe-shift` - `[:double {:min -1.0 :max 1.0}]` matches proto

### Zoom Values
- ✅ `zoom-level` - Correctly uses `[:double {:min 1.0 :max 100.0}]` for zoom level (not position)
- ✅ `digital-zoom-level` - `[:double {:min 1.0}]` matches proto (no upper limit)
- ✅ `zoom-table-index` - `[:int {:min 0}]` matches proto

### LRF Offset Values
- ✅ `offset-value` - `[:int {:min -1920 :max 1920}]` matches proto for X
- ✅ Added `offset-y-value` - `[:int {:min -1080 :max 1080}]`
- ✅ `offset-shift` - Same ranges as offset-value

### DDE Values
- ✅ `dde-level` - Fixed to `[:int {:min 0 :max 512}]`
- ✅ `dde-shift` - Fixed to `[:int {:min -100 :max 100}]`

### Speed Values
- ✅ `speed-percentage` - `[:int {:min 0 :max 100}]` matches proto
- ✅ `scan-speed` - Fixed to `[:double {:min 0.0000001 :max 1.0}]` for `gt: 0.0`

### Special Values
- ✅ `protocol-version` - Fixed to `[:int {:min 1}]` (gt: 0)
- ✅ `magnetic-declination` - Fixed to `[:float {:min -180.0 :max 179.999999}]` (lt: 180)
- ✅ `ndc-coordinate` - Fixed to `[:float {:min -1.0 :max 1.0}]`

## State Schemas (`potatoclient.state.schemas`)

### Common Values
- ✅ `angle-azimuth` - `[:double {:min 0.0 :max 359.999999}]` correctly uses lt: 360
- ✅ `angle-elevation` - `[:double {:min -90.0 :max 90.0}]` matches proto
- ✅ `angle-bank` - `[:double {:min -180.0 :max 179.999999}]` correctly uses lt: 180
- ✅ `angle-offset` - `[:double {:min -180.0 :max 179.999999}]` correctly uses lt: 180
- ✅ `normalized-value` - `[:double {:min 0.0 :max 1.0}]` matches proto
- ✅ `percentage` - `[:double {:min 0.0 :max 100.0}]` matches proto
- ✅ `temperature-celsius` - Fixed to max 660.32 (melting point of aluminum)
- ✅ `gps-longitude` - `[:double {:min -180.0 :max 180.0}]` matches proto
- ✅ `gps-latitude` - `[:double {:min -90.0 :max 90.0}]` matches proto
- ✅ `gps-altitude` - `[:double {:min -433.0 :max 8848.86}]` matches proto
- ✅ `rgb-value` - `[:int {:min 0 :max 255}]` matches proto
- ✅ `distance-decimeters` - `[:double {:min 0.0 :max 500000.0}]` matches proto
- ✅ `non-negative-int` - `[:int {:min 0}]` matches proto
- ✅ `zoom-factor` - `[:double {:min 1.0}]` matches proto

### Enum Validations
- ✅ Most enums correctly exclude 0 (UNSPECIFIED) where required
- ✅ Fixed enums that still included UNSPECIFIED (compass-calibrate-status, time-format, rec-osd-screen)

### Required Fields
- ❌ Need to ensure all state subsystems are required in `jon-gui-state-schema`
- ❌ Need to ensure all command oneofs are required

### Special State Values
- ✅ `sun-azimuth` - Fixed to `[:float {:min 0.0 :max 359.999999}]`
- ✅ `sun-elevation` - Fixed to `[:float {:min 0.0 :max 359.999999}]` (unusual but matches proto!)
- ✅ `power-consumption` - Fixed to `[:float {:min 0.0 :max 1000.0}]`
- ✅ `disk-space` - Fixed to `[:int {:min 0 :max 100}]` percentage
- ✅ `timestamp` - Fixed to `[:int {:min 0}]` for all timestamp fields
- ✅ `measure-id` - Fixed to `[:int {:min 0}]`
- ✅ `session-id` - Fixed to `[:int {:min 0}]`
- ✅ `target-id` - Fixed to `[:int {:min 0}]`
- ✅ `stage`/`final-stage` - Fixed validation for compass calibration

## Missing Command Validation

### Rotary Commands
- ✅ `scan-node-index` validation - Fixed to `[:int {:min 0}]`
- ✅ `scan-linger-time` validation - Fixed to `[:double {:min 0.0}]`
- ✅ `platform-azimuth` - Fixed to `[:double {:min -359.999999 :max 359.999999}]`
- ✅ `platform-elevation` - Fixed to `[:double {:min -90.0 :max 90.0}]`
- ✅ `platform-bank` - Fixed to `[:double {:min -180.0 :max 179.999999}]`

### GPS Commands
- ❌ Manual position validation for commands
- ❌ Origin GPS validation

### Camera Commands
- ❌ Separate validation for day vs heat camera constraints
- ❌ CLAHE shift validation
- ❌ DDE level/shift validation

### CV Commands
- ❌ NDC coordinate validation for tracking

## Implementation Steps COMPLETED

1. ✅ **Fixed angle constraints** - Updated specs to use correct lt/lte boundaries
2. ✅ **Added missing command specs** - DDE, scan parameters, platform angles
3. ✅ **Added timestamp validation** - All timestamp fields >= 0
4. ✅ **Verified enum validation** - Fixed enums to exclude UNSPECIFIED where required
5. ❌ **Add required field validation** - For oneofs and message fields (still pending)
6. ✅ **Created separate specs for edge cases** - Sun elevation, Y offsets
7. ✅ **Updated temperature max values** - Now uses 660.32°C
8. ✅ **Added protocol version validation** - Must be > 0
9. ✅ **Tested all constraints** - Comprehensive boundary condition tests created

## Notes

- Proto uses `lt` (less than) vs `lte` (less than or equal) - we need to match exactly
- Some values like sun_elevation use unusual ranges (0-360 instead of -90 to 90)
- Digital zoom has no upper limit in proto
- All timestamps must be non-negative
- Most enums exclude the 0 value (UNSPECIFIED)
- Required fields in proto should map to non-optional keys in Malli