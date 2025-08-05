# Gesture Event Spec Redesign Analysis

## Current Issues

The current `gesture-event` spec in both `ui_specs.clj` and `shared/specs/video/stream.clj` uses a single schema with many optional fields. This approach has several problems:

1. **Lack of precision**: A tap event can pass validation even with `delta-x` and `delta-y` fields, which make no sense for taps
2. **Missing constraints**: The spec doesn't enforce which fields must appear together
3. **Documentation unclear**: It's not obvious which fields are for which gesture types

## Evidence from Codebase

### 1. Kotlin Sealed Class Structure (`GestureEvent.kt`)
```kotlin
sealed class GestureEvent {
    abstract val timestamp: Long
    
    data class Tap(val x: Int, val y: Int, override val timestamp: Long)
    data class DoubleTap(val x: Int, val y: Int, override val timestamp: Long)
    data class PanStart(val x: Int, val y: Int, override val timestamp: Long)
    data class PanMove(val x: Int, val y: Int, val deltaX: Int, val deltaY: Int, override val timestamp: Long)
    data class PanStop(val x: Int, val y: Int, override val timestamp: Long)
}
```

### 2. MouseEventHandler.kt Shows Conditional Field Addition
- **All gestures** get: type, gesture-type, timestamp, canvas dimensions, aspect-ratio, stream-type
- **Tap/DoubleTap/PanStart/PanStop** add: x, y, ndc-x, ndc-y
- **PanMove** adds: x, y, delta-x, delta-y, ndc-delta-x, ndc-delta-y
- **DoubleTap** optionally adds: frame-timestamp, frame-duration (for CV tracking)

### 3. Mock Video Stream (`gesture_sim.clj` and `core.clj`)
Creates gesture events conditionally:
```clojure
(cond-> base-event
  (and x y) (merge {:x x :y y} ...)
  (and delta-x delta-y) (merge {:delta-x delta-x :delta-y delta-y} ...)
  frame-data (merge {:frame-timestamp ... :frame-duration ...}))
```

## Proposed Solution

Use Malli's `:or` to create precise specs for each gesture type:

```clojure
(def gesture-event
  [:or
   tap-event       ; has x, y, ndc-x, ndc-y
   double-tap-event ; has x, y, ndc-x, ndc-y, optional frame-timestamp/duration
   pan-start-event  ; has x, y, ndc-x, ndc-y
   pan-move-event   ; has x, y, delta-x, delta-y, ndc-delta-x, ndc-delta-y
   pan-stop-event]) ; has x, y
```

## Benefits

1. **Type Safety**: Invalid field combinations will fail validation
2. **Self-Documenting**: Each gesture type clearly shows its required fields
3. **Better Error Messages**: Malli can explain exactly which gesture type failed
4. **Matches Implementation**: Aligns with Kotlin sealed class design
5. **Guardrails Integration**: More precise specs mean better runtime checking

## Implementation Steps

1. Create individual specs for each gesture type using `:merge` with base spec
2. Combine them with `:or` for the main `gesture-event` spec
3. Update both `ui_specs.clj` and `shared/specs/video/stream.clj`
4. Test with existing gesture handlers and mock tools
5. Update tests to use more precise validation

## Compatibility

This change is backward compatible because:
- Valid events under the old spec remain valid
- Invalid events that passed before will now correctly fail
- The `:or` spec can still be used anywhere the old spec was used