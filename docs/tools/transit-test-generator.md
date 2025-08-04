# Transit Test Generator

JVM tool for generating and validating Transit command messages using Malli specs.

## Overview

The Transit Test Generator is a command-line tool that generates, validates, and tests Transit messages based on PotatoClient's Malli specifications. It's essential for:

- Testing Kotlin/Clojure integration
- Generating test data for unit tests
- Validating Transit message formats
- Ensuring roundtrip encoding works correctly

## Installation

```bash
cd tools/transit-test-generator
make build  # Creates uberjar
```

## Core Features

### 1. Generate Commands

Create Transit command messages with optional field overrides:

```bash
# Basic command
java -jar target/transit-test-generator-*.jar generate \
  --command ping \
  --output-file ping.edn

# Command with overrides
echo '{:channel "heat" :x 0.5 :y -0.25}' > overrides.edn
java -jar target/transit-test-generator-*.jar generate \
  --command cv.start-track-ndc \
  --output-file track.edn \
  --overrides-file overrides.edn

# Transit MessagePack format
java -jar target/transit-test-generator-*.jar generate \
  --command rotary.goto-ndc \
  --output-file goto.transit \
  --format transit
```

### 2. Validate Messages

Check if Transit/EDN files conform to specifications:

```bash
# Validate EDN
java -jar target/transit-test-generator-*.jar validate \
  --input-file track.edn

# Validate Transit
java -jar target/transit-test-generator-*.jar validate \
  --input-file goto.transit \
  --format transit
```

### 3. Test Roundtrips

Ensure messages survive encoding/decoding:

```bash
java -jar target/transit-test-generator-*.jar roundtrip \
  --input-file track.edn
```

### 4. Batch Operations

Generate comprehensive test sets:

```bash
# Generate all command types
java -jar target/transit-test-generator-*.jar batch \
  --output-dir test-data/

# Generate random commands
java -jar target/transit-test-generator-*.jar generate-batch \
  --output-dir random-data/ \
  --count 100 \
  --random-seed 12345

# Validate directory
java -jar target/transit-test-generator-*.jar validate-batch \
  --input-dir test-data/
```

## Command Path Format

Commands use dot notation for nested structures:

- `ping` - Top-level command
- `cv.start-track-ndc` - CV subsystem command
- `rotary.goto-ndc` - Rotary platform command
- `heat-camera.set-zoom` - Camera command

## Output Format

All commands output JSON for easy parsing:

### Success Response
```json
{
  "status": "success",
  "data": {
    "command": "cv.start-track-ndc",
    "file": "track.edn",
    "format": "edn"
  }
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Validation failed",
  "details": {
    "errors": ["Missing required field: channel"]
  }
}
```

## Integration Examples

### Kotlin Integration

```kotlin
val process = ProcessBuilder(
    "java", "-jar", "transit-test-generator.jar",
    "generate", "--command", "ping",
    "--output-file", "test.transit",
    "--format", "transit"
).start()

val result = process.inputStream
    .bufferedReader()
    .readText()
    .let { Json.parseToJsonElement(it) }
```

### Clojure Integration

```clojure
(require '[clojure.java.shell :as shell]
         '[clojure.data.json :as json])

(let [{:keys [exit out err]} 
      (shell/sh "java" "-jar" "transit-test-generator.jar"
                "validate" "--input-file" "command.edn")]
  (when (zero? exit)
    (json/read-str out :key-fn keyword)))
```

## Testing Workflow

1. **Generate test data**: Create EDN/Transit files
2. **Validate messages**: Ensure spec compliance
3. **Test roundtrips**: Verify encoding/decoding
4. **Batch validation**: Test comprehensive sets

## Common Use Cases

### Unit Test Data Generation
```bash
# Generate test fixtures
make -C tools/transit-test-generator test-data
```

### Integration Testing
```bash
# Validate all commands in CI
java -jar transit-test-generator.jar validate-batch \
  --input-dir src/test/resources/commands/
```

### Debugging Transit Issues
```bash
# Check specific message
java -jar transit-test-generator.jar validate \
  --input-file problematic.transit \
  --format transit
```

## See Also

- [Transit Protocol](../architecture/transit-protocol.md)
- [Command System](../architecture/command-system.md)
- [Proto Explorer](./proto-explorer.md)