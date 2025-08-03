# Validation Sanity Checks - Complete Report

## Overview

This document verifies that each stage of our validation pipeline actually performs checks and can signal failures. This is CRITICAL to ensure we don't have "silent passes" where validation appears to work but isn't actually checking anything.

## Stage 1: Guardrails (Clojure Argument Validation) ✅ VERIFIED

**Test**: Invalid enum value
```clojure
(cmd/heat-camera-palette :invalid-palette)
```

**Result**: ✅ Throws exception with detailed error:
```
Guardrails:
  (potatoclient.transit.commands/heat-camera-palette :invalid-palette)
  Arg Errors: [["should be either :white-hot, :black-hot, :rainbow, :ironbow, :lava or :arctic"]]
```

**Other verified checks**:
- Wrong argument types (string instead of keyword)
- Missing required fields in maps
- Values outside specified ranges

## Stage 2: Transit Serialization ✅ VERIFIED

**Tests implemented** in `sanity_check_validation_test.clj`:
1. **Truncated data** - Transit correctly fails to parse incomplete messages
2. **Corrupted bytes** - Transit detects corruption in message body

**Code**:
```clojure
;; Truncate Transit data
(let [truncated (byte-array (/ (count valid-bytes) 2))]
  ;; Transit will throw exception on parse
```

## Stage 3: Kotlin Command Building ✅ VERIFIED

**Tests implemented** in `TestCommandProcessor.kt`:
1. **Unknown command types** - Returns null or empty proto
2. **Wrong structure** - e.g., `{:rotary "not-a-map"}` fails
3. **Missing nested commands** - e.g., `{:cv {}}` fails

**GeneratedCommandHandlers behavior**:
- Returns `null` for unrecognized structures
- Returns proto with `PAYLOAD_NOT_SET` for invalid commands

## Stage 4: Protobuf Required Fields ✅ VERIFIED

**Test**: Missing required coordinates
```kotlin
val command = mapOf(
    "cv" to mapOf(
        "start-track-ndc" to mapOf(
            "channel" to "heat"
            // Missing required x, y
        )
    )
)
```

**Result**: Protobuf builder enforces required fields at construction time

## Stage 5: buf.validate Constraints ✅ VERIFIED

**Tests implemented** in `BufValidateTest.kt` and `ValidatorSanityTest.kt`:

1. **Azimuth > 360** ✅ Correctly rejected
   ```
   Validation failed: rotary.goto.azimuth: value must be less than or equal to 360
   ```

2. **Elevation < -30** ✅ Correctly rejected
   ```
   Validation failed: rotary.goto.elevation: value must be greater than or equal to -30
   ```

3. **Multiple violations** ✅ Both reported
   ```
   Should have at least 2 violations
   ```

4. **GPS latitude > 90** ✅ Correctly rejected
5. **Heat camera zoom > 8** ✅ Correctly rejected

## Stage 6: Binary Roundtrip ✅ VERIFIED

**Test implemented** in `ValidatorSanityTest.kt`:
```kotlin
val binary = proto.toByteArray()
val deserialized = JonSharedCmd.Root.parseFrom(binary)

// Verify data preserved
assertEquals(original.cv.startTrackNdc.x, deserialized.cv.startTrackNdc.x)
```

**Result**: Binary serialization preserves all data correctly

## Stage 7: Java Equals Detection ✅ VERIFIED

**Tests implemented**:
1. **Same proto equals itself** ✅
   ```kotlin
   assertEquals(original, deserialized)
   assertEquals(original.hashCode(), deserialized.hashCode())
   ```

2. **Different protos not equal** ✅
   ```kotlin
   val ping = buildCommand(mapOf("ping" to emptyMap()))
   val noop = buildCommand(mapOf("noop" to emptyMap()))
   assertNotEquals(ping, noop)
   ```

## Malli Generation Validation ✅ VERIFIED

**Tests created**:
1. `malli_generation_test.clj` - Tests Malli can generate valid data
2. `simple_malli_validation_test.clj` - Tests generated data creates valid commands
3. `kotlin_malli_integration_test.clj` - Full integration with Kotlin validation

**Key findings**:
- Malli respects all constraints (min/max values, enums)
- Generated data successfully creates commands
- Commands serialize through Transit correctly

## Summary

✅ **All validation stages are verified to actually perform checks and signal failures**

Each stage has been tested with both valid and invalid data to ensure:
1. Valid data passes through correctly
2. Invalid data is rejected with meaningful errors
3. No "silent passes" where validation is skipped
4. Error messages indicate which constraint was violated

## Test Execution

To run all sanity checks:

```bash
# Clojure sanity checks
clojure -M:test -n potatoclient.transit.sanity-check-validation-test

# Kotlin sanity checks  
cd test/kotlin
gradle test --tests ValidatorSanityTest

# Malli generation tests
clojure -M:test -n potatoclient.transit.simple-malli-validation-test
```