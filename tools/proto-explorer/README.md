# Proto-Explorer

A powerful Clojure tool for exploring and inspecting Protocol Buffer message definitions in your codebase. Proto-Explorer provides comprehensive information about protobuf messages including Java class mappings, Pronto EDN representations, and field details from descriptors.

## Table of Contents

- [Rationale](#rationale)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
  - [Command Line Interface](#command-line-interface)
  - [Makefile Integration](#makefile-integration)
- [API Reference](#api-reference)
- [Use Cases](#use-cases)
- [Examples](#examples)
- [Implementation Details](#implementation-details)

## Rationale

When working with Protocol Buffers in a Clojure/Java project, developers face several challenges:

1. **Discovery**: Finding the right protobuf message among hundreds of definitions
2. **Mapping**: Understanding how proto messages map to Java classes (e.g., `cmd.Root` → `cmd.JonSharedCmd$Root`)
3. **Structure**: Visualizing message structure and field types
4. **Integration**: Understanding how to work with messages in Clojure (via Pronto EDN)
5. **Validation**: Seeing field constraints from buf.validate annotations

Proto-Explorer solves these problems by providing a unified interface to search, discover, and inspect protobuf messages with all their representations in one place.

## Features

- **🔍 Smart Search**: Searches both message names and Java class names with intelligent matching
- **📋 Complete Listing**: Browse all messages organized by package
- **🔗 Java Mapping**: Automatic mapping between proto definitions and Java classes
- **📊 Pronto EDN**: View Clojure-friendly EDN representations via Pronto
- **📝 Field Details**: Complete field information including types and constraints
- **⚡ Fast**: Stateless tool that starts and responds quickly
- **🎯 Focused**: Two-step workflow - search/list then inspect
- **🔧 Flexible**: Auto-detects descriptor locations for seamless operation

## Installation

Proto-Explorer is included in the PotatoClient project. No separate installation needed.

### Prerequisites

- Clojure CLI tools
- Java 11+
- Compiled protobuf classes in `target/classes`
- Pronto library for EDN conversion
- JSON descriptors in `output/json-descriptors/`

## Usage

### Command Line Interface

Proto-Explorer provides a simple two-step workflow:

1. **Find messages** using search or list
2. **Get details** using the Java class name

#### Direct CLI Usage

From the `tools/proto-explorer` directory:

```bash
# Search for messages
clojure -M:run:test-protos search <query> [limit]

# Search specifically by Java class name
clojure -M:run:test-protos search-java <query> [limit]

# List all messages
clojure -M:run:test-protos list [package-filter]

# Get detailed info
clojure -M:run:test-protos info <java-class-name>

# Get info as EDN (for programmatic use)
clojure -M:run:test-protos info-edn <java-class-name>
```

### Makefile Integration (Recommended)

The preferred way to use Proto-Explorer is through the main Makefile:

```bash
# Search for protobuf messages
make proto-search QUERY=gps

# Search specifically by Java class name
make proto-search-java QUERY=Compass

# List all messages (optional filter)
make proto-list
make proto-list FILTER=cmd.System

# Get comprehensive info about a message
make proto-info CLASS='cmd.JonSharedCmd$Root'
```

**Note**: For inner classes with `$`, use single quotes in the Makefile to prevent shell expansion.

## API Reference

### Primary Commands

#### `search <query> [limit]`
Searches for protobuf messages using smart matching algorithm.

- **query**: Search term (case-insensitive)
- **limit**: Maximum results to return (default: 10)
- **Returns**: Ranked list of matching messages with scores

**Matching Algorithm**:
1. Exact match (score: 1.00)
2. Prefix match (score: 0.95)
3. Suffix match (score: 0.90)
4. Word boundary match (score: 0.85)
5. Substring match (score: 0.80, adjusted by position)
6. Fuzzy match for typos (score: 0.50 * similarity)

**Search Scope**: The search algorithm checks both:
- Message names (e.g., "Root", "Ping", "SetOriginGPS")
- Full Java class names (e.g., "cmd.JonSharedCmd$Root", "ser.JonSharedData$JonGUIState")

#### `search-java <query> [limit]`
Searches specifically by Java class name, focusing only on the Java class field.

- **query**: Java class search term (case-insensitive)
- **limit**: Maximum results to return (default: 10)
- **Returns**: Ranked list of matching messages with scores

**Key Differences from `search`**:
- Only searches in Java class names, not message names
- Better for finding messages by their Java implementation
- Prioritizes inner class name matches (after the `$`)
- Useful when you know the Java class pattern

#### `list [package-filter]`
Lists all protobuf messages, optionally filtered by package prefix.

- **package-filter**: Optional package prefix (e.g., "cmd", "cmd.System")
- **Returns**: All messages grouped by package with field counts

#### `info <java-class-name>`
Provides comprehensive information about a specific message.

- **java-class-name**: Full Java class name (e.g., `cmd.JonSharedCmd$Root`)
- **Returns**: 
  - Java class details (methods, fields)
  - Pronto EDN structure
  - Field definitions with types
  - Proto file location
  - Package information

#### `info-edn <java-class-name>`
Same as `info` but returns raw EDN data for programmatic processing.

### Legacy Commands

These commands are available for specific use cases:

- `java-class <message>` - Raw Java class information
- `java-fields <message>` - Proto field to Java method mapping
- `java-builder <message>` - Java builder information
- `java-summary <message>` - Human-readable Java class summary
- `pronto-edn <message>` - Just the Pronto EDN representation
- `descriptor-info <message>` - JSON descriptor information

## Use Cases

### 1. Finding the Right Message

**Problem**: "I need to send a GPS-related command but don't know the exact message name."

**Solution**:
```bash
$ make proto-search QUERY=gps

Search results for: "gps"
 1. RotateToGPS               cmd.RotaryPlatform$RotateToGPS (score: 0.90)
 2. SetOriginGPS              cmd.RotaryPlatform$SetOriginGPS (score: 0.90)
 3. JonGuiDataGps             ser$JonGuiDataGps (score: 0.90)
```

### 2. Understanding Message Structure

**Problem**: "What fields does the main command root message have?"

**Solution**:
```bash
$ make proto-info CLASS='cmd.JonSharedCmd$Root'

=== FIELD DETAILS ===
  [ 1] protocol_version          : type-uint32
  [ 2] session_id                : type-uint32
  [20] day_camera                : type-message    .cmd.DayCamera.Root
  [21] heat_camera               : type-message    .cmd.HeatCamera.Root
  ...
```

### 3. Generating Clojure Code

**Problem**: "How do I create this message in Clojure with Pronto?"

**Solution**:
```bash
$ make proto-info CLASS='cmd.JonSharedCmd$Ping'

=== PRONTO EDN STRUCTURE ===
{}  ; Empty message, no required fields
```

For a more complex example:
```bash
$ make proto-info CLASS='cmd.RotaryPlatform$RotateAzimuthTo'

=== PRONTO EDN STRUCTURE ===
{:angle 0.0, :speed 0.0}  ; Shows default values and field names
```

### 4. Exploring a Subsystem

**Problem**: "What commands are available for the compass subsystem?"

**Solution**:
```bash
$ make proto-list FILTER=cmd.Compass

=== cmd.Compass (13 messages) ===
  CalibrateCencel                cmd.Compass$CalibrateCencel (fields: 0)
  CalibrateNext                  cmd.Compass$CalibrateNext (fields: 0)
  CalibrateStartLong             cmd.Compass$CalibrateStartLong (fields: 0)
  SetMagneticDeclination         cmd.Compass$SetMagneticDeclination (fields: 1)
  ...
```

### 5. Mapping Proto to Java

**Problem**: "The error mentions 'cmd.Root' but I need the Java class name."

**Solution**:
```bash
$ make proto-search QUERY=root

# Shows: Root -> cmd.JonSharedCmd$Root
```

### 6. Creating Malli Specs

**Problem**: "I need to create Malli specs that match the protobuf structure."

**Solution**:
```bash
$ make proto-info CLASS='cmd.RotaryPlatform$SetOriginGPS'

=== PRONTO EDN STRUCTURE ===
{:latitude 0.0, :longitude 0.0, :altitude 0.0}

# Use this to create your Malli spec:
(def SetOriginGPS
  [:map
   [:latitude float?]
   [:longitude float?]
   [:altitude float?]])
```

### 7. Debugging Serialization Issues

**Problem**: "My protobuf message isn't serializing correctly."

**Solution**:
```bash
# Check field mappings and types
$ make proto-info CLASS='your.Message$Name'

# Look at:
# - Field numbers (must match proto definition)
# - Field types (ensure correct type conversion)
# - Java getter methods (for manual serialization)
```

## Examples

### Example 1: Complete Workflow

```bash
# Step 1: Search for azimuth-related messages
$ make proto-search QUERY=azimuth

Search results for: "azimuth"
 1. Azimuth                   cmd.RotaryPlatform$Azimuth (score: 1.00)
 2. HaltAzimuth               cmd.RotaryPlatform$HaltAzimuth (score: 1.00)
 3. RotateAzimuthTo           cmd.RotaryPlatform$RotateAzimuthTo (score: 0.98)

# Step 2: Get details about RotateAzimuthTo
$ make proto-info CLASS='cmd.RotaryPlatform$RotateAzimuthTo'

================================================================================
PROTOBUF MESSAGE: RotateAzimuthTo
Java Class: cmd.RotaryPlatform$RotateAzimuthTo
Proto Package: cmd.RotaryPlatform
Proto File: jon_shared_cmd_rotary.proto
================================================================================

=== FIELD DETAILS ===
  [ 1] angle                     : type-float
  [ 2] speed                     : type-float

=== PRONTO EDN STRUCTURE ===
{:angle 0.0, :speed 0.0}
```

### Example 2: Exploring Data Messages

```bash
# List all data serialization messages
$ make proto-list FILTER=ser

=== ser (17 messages) ===
  JonGUIState                    ser.JonSharedData$JonGUIState (fields: 14)
  JonGuiDataCameraDay            ser$JonGuiDataCameraDay (fields: 11)
  JonGuiDataGps                  ser$JonGuiDataGps (fields: 8)
  ...

# Inspect the main GUI state message
$ make proto-info CLASS='ser.JonSharedData$JonGUIState'
```

### Example 3: Finding Messages with Typos

```bash
# Even with typos, the fuzzy search helps
$ make proto-search QUERY=compas

Search results for: "compas"
 1. setUseRotaryAsCompass     cmd.RotaryPlatform$setUseRotaryAsCompass (score: 0.85)
 2. JonGuiDataCompass         ser$JonGuiDataCompass (score: 0.85)
```

### Example 4: Working with Substrings

```bash
# Find anything with "GPS" in the name
$ make proto-search QUERY=gps

# Find all "Set" commands
$ make proto-search QUERY=set

# Case doesn't matter
$ make proto-search QUERY=GPS
$ make proto-search QUERY=gPs
$ make proto-search QUERY=gps
# All return the same results
```

### Example 5: Searching by Java Class Names

```bash
# Search for messages from JonSharedCmd outer class
$ make proto-search QUERY=JonSharedCmd

Search results for: "JonSharedCmd"
 1. Root                      cmd.JonSharedCmd$Root (score: 0.85)
 2. Ping                      cmd.JonSharedCmd$Ping (score: 0.85)
 3. Noop                      cmd.JonSharedCmd$Noop (score: 0.85)
 4. Frozen                    cmd.JonSharedCmd$Frozen (score: 0.85)

# Search with full package prefix
$ make proto-search QUERY=cmd.JonSharedCmd

Search results for: "cmd.JonSharedCmd"
 1. Root                      cmd.JonSharedCmd$Root (score: 0.95)
 2. Ping                      cmd.JonSharedCmd$Ping (score: 0.95)
 3. Noop                      cmd.JonSharedCmd$Noop (score: 0.95)
 4. Frozen                    cmd.JonSharedCmd$Frozen (score: 0.95)

# Search for JonSharedData messages
$ make proto-search QUERY=JonSharedData

Search results for: "JonSharedData"
 1. JonGUIState               ser.JonSharedData$JonGUIState (score: 0.85)
```

### Example 6: Using Java-Specific Search

```bash
# Use search-java to focus only on Java class names
$ make proto-search-java QUERY=Compass

Search results for: "Compass"
 1. setUseRotaryAsCompass     cmd.RotaryPlatform$setUseRotaryAsCompass (score: 0.90)
 2. JonGuiDataCompass         ser$JonGuiDataCompass (score: 0.90)
 3. Root                      cmd.Compass$Root (score: 0.85)
 4. Start                     cmd.Compass$Start (score: 0.85)
 5. Stop                      cmd.Compass$Stop (score: 0.85)

# Compare with regular search which also matches message names
$ make proto-search QUERY=Compass

# search-java is more precise for finding by inner class name
$ make proto-search-java QUERY=Root

Search results for: "Root"
 1. Root                      cmd.Compass$Root (score: 0.98)
 2. Root                      cmd.Gps$Root (score: 0.98)
 3. Root                      cmd.Lrf$Root (score: 0.98)
 4. Root                      cmd.DayCamera$Root (score: 0.98)
 5. Root                      cmd.HeatCamera$Root (score: 0.98)
```

## Implementation Details

### Architecture

Proto-Explorer uses a stateless architecture that:

1. **Reads** `descriptor-set.json` - A unified JSON descriptor containing all proto definitions
2. **Maps** Proto packages to Java class names using naming conventions
3. **Searches** Using efficient string matching algorithms
4. **Reflects** Java classes for method/field information
5. **Converts** Messages to EDN using Pronto for Clojure integration

### Key Components

- **descriptor-set-search**: Searches and indexes messages from descriptors
- **simple-api**: Core API for search, list, and info operations
- **cli-final**: Command-line interface and formatting
- **java-class-info**: Java reflection utilities
- **pronto-integration**: Pronto EDN conversion

### Proto to Java Mapping Rules

The tool automatically maps proto definitions to Java classes:

- `jon_shared_cmd.proto` (package `cmd`) → `cmd.JonSharedCmd$Message`
- `jon_shared_data.proto` (package `ser`) → `ser.JonSharedData$Message`
- Other files use package as outer class: `cmd.Compass$Message`

### Performance

- Startup time: ~1-2 seconds
- Search response: <100ms for 200+ messages
- Memory usage: Minimal (stateless operation)

### Search Algorithm Details

The search algorithm prioritizes practical substring matching over pure fuzzy matching:

1. **Dual search scope**: Searches in both message names AND full Java class names
2. **Case-insensitive**: All comparisons ignore case
3. **Substring first**: Direct substring matches score highest
4. **Position matters**: Earlier matches in the name score higher
5. **Word boundaries**: Matches at word boundaries (CamelCase) get bonus points
6. **Fuzzy fallback**: Jaro-Winkler distance for handling typos

This means searching for "gps" will find:
- `SetOriginGPS` (suffix match in message name)
- `RotateToGPS` (suffix match in message name)
- `JonGuiDataGps` (suffix match in message name)
- `CompassGpsData` (substring match in message name)

And searching for "JonSharedCmd" will find:
- `cmd.JonSharedCmd$Root` (substring match in Java class name)
- `cmd.JonSharedCmd$Ping` (substring match in Java class name)
- All other messages in the JonSharedCmd outer class

### Path Resolution

Proto-Explorer automatically locates the descriptor files by checking multiple standard locations:
- `examples/protogen/output/json-descriptors/` (primary location)
- `output/json-descriptors/` (alternative location)
- Relative paths from the tool's location

This ensures the tool works correctly whether run:
- From the project root via Makefile
- From the tools/proto-explorer directory
- As part of automated scripts

## Troubleshooting

### Common Issues

1. **"Class not found" errors**: Ensure protobuf classes are compiled:
   ```bash
   make compile-java-proto
   ```

2. **"Descriptor not found" errors**: Ensure descriptors are generated:
   ```bash
   make proto  # Regenerates from proto files
   ```

3. **Pronto errors**: Compile Pronto classes:
   ```bash
   make ensure-pronto
   ```

4. **Dollar sign in class names**: Use single quotes in shell:
   ```bash
   make proto-info CLASS='cmd.JonSharedCmd$Root'  # Correct
   make proto-info CLASS=cmd.JonSharedCmd$Root    # Wrong - $ interpreted by shell
   ```

5. **No search results**: Check if protobuf classes are in classpath:
   ```bash
   make proto-list  # Should show all available messages
   ```

## Development

### Running Tests
```bash
cd tools/proto-explorer
clojure -M:test
```

### REPL Development
```bash
cd tools/proto-explorer
clojure -M:repl
```
