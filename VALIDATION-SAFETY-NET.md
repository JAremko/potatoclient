# Validation as a Safety Net

## Summary

We've implemented comprehensive validation breakage tests to ensure Malli specs catch invalid values that might otherwise pass through the system. This serves as a critical safety net because:

1. **Protobuf doesn't validate ranges at build time** - Values outside the specified ranges in `.proto` files are accepted
2. **Type safety is only at compile time** - Runtime validation is needed for dynamic data
3. **State updates need validation** - Invalid states could corrupt the UI or cause crashes

## Test Results

### 1. Protobuf Limitations

```clojure
;; Protobuf accepts invalid GPS coordinates!
(let [gps-builder (JonSharedDataGps$JonGuiDataGps/newBuilder)]
  (.setLatitude gps-builder 200.0)  ; > 90 (invalid!)
  (.setLongitude gps-builder 300.0) ; > 180 (invalid!)
  (.build gps-builder))              ; Builds successfully!
```

### 2. Command Parameter Validation

```clojure
;; LRF offset commands accept out-of-range values
(let [set-offsets (-> (JonSharedCmdLrfAlign$SetOffsets/newBuilder)
                      (.setX 5000)    ; > 1920 (invalid!)
                      (.setY -3000)   ; < -1080 (invalid!)
                      (.build))]      ; Builds successfully!
  ;; Protobuf doesn't enforce the buf.validate constraints
  )
```

### 3. Malli Specs as Safety Net

Our Malli specs correctly catch these invalid values:

```clojure
;; Offset values
(m/validate ::specs/offset-value 1920)  ; => true
(m/validate ::specs/offset-value 2000)  ; => false

;; GPS coordinates
(m/validate state-schemas/gps-latitude 90.0)   ; => true
(m/validate state-schemas/gps-latitude 200.0)  ; => false

;; Zoom levels
(m/validate ::specs/zoom-level 50.0)  ; => true
(m/validate ::specs/zoom-level 0.0)   ; => false
```

## Implementation

### 1. Command Validation Specs

Added to `potatoclient.specs`:

```clojure
(def offset-value
  "LRF alignment offset value in pixels [-1920, 1920] for X, [-1080, 1080] for Y"
  [:int {:min -1920 :max 1920}])

(def offset-shift
  "LRF alignment offset shift in pixels"
  [:int {:min -1920 :max 1920}])

(def speed-percentage
  "Speed as percentage [0, 100]"
  [:int {:min 0 :max 100}])

(def zoom-level
  "Camera zoom level (1.0 to 100.0)"
  [:double {:min 1.0 :max 100.0}])
```

### 2. State Validation Schemas

In `potatoclient.state.schemas`:

- GPS: latitude [-90, 90], longitude [-180, 180]
- Compass: azimuth [0, 360), pitch [-90, 90], roll [-180, 180)
- LRF: distance [0, 50000]
- Camera: zoom positions [0, 1], digital zoom >= 1.0

### 3. Testing Strategy

Created two test files:

1. **validation_breakage_test.clj** - Tests that invalid values are caught
2. **validation_safety_test.clj** - Tests command parameter validation

## Recommendations

1. **Always validate at boundaries** - When receiving data from:
   - WebSocket messages
   - User input
   - Command parameters

2. **Use Guardrails in development** - Catches invalid function arguments early

3. **Performance considerations**:
   - Validation of 1000 states takes ~170ms
   - Acceptable for real-time use
   - Consider caching validation results for repeated values

4. **Edge cases to watch**:
   - Float precision (use tolerance for comparisons)
   - NaN and Infinity values
   - Nil values in required fields
   - Empty strings where text is expected

## Running Validation Tests

```bash
# Run validation breakage tests
clojure -M:test -n potatoclient.validation-breakage-test

# Run command validation safety tests  
clojure -M:test -n potatoclient.cmd.validation-safety-test

# Run with Guardrails enabled (catches more issues)
clojure -M:dev:test
```

## Conclusion

Malli validation provides essential runtime safety that protobuf's `buf.validate` constraints don't enforce at build time. This prevents invalid data from propagating through the system and causing unexpected behavior or crashes.