# Proto-Explorer

**Note: For Claude AI users, use the `proto-class-explorer` agent instead of direct proto-explorer commands. The agent provides better integration and automatic handling of the proto-explorer tool.**

A streamlined 2-step tool for exploring Protocol Buffer messages in your codebase. Search for messages, then get comprehensive details including Java class mappings, Pronto EDN representations, and field information.

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
4. **Integration**: Understanding how to work with messages in Clojure (via Pronto EDN and schema)
5. **Validation**: Seeing field constraints from buf.validate annotations

Proto-Explorer solves these problems by providing a unified interface to search, discover, and inspect protobuf messages with all their representations in one place.

## Features

- **🎯 2-Step Workflow**: Search first, then get details with the exact query string
- **🔍 Unified Search**: Single command searches both message names and Java classes
- **📋 Complete Listing**: Browse all messages organized by package
- **🔗 Actionable Results**: Search results provide ready-to-use query strings
- **📊 Comprehensive Info**: Java mappings, Pronto EDN, Pronto schema, field details, and buf.validate constraints in one view
- **✅ Validation Constraints**: Automatically extracts and displays buf.validate constraints from compiled protobuf classes
- **🛡️ Robust & Foolproof**: Automatic class compilation, intelligent fallbacks, and clear warnings
- **🔄 Smart Class Resolution**: Multiple resolution strategies with fuzzy matching
- **📦 Deep Nesting Support**: Handles complex nested messages and oneof structures
- **⚡ Fast & Simple**: Stateless tool with clear, predictable commands

## Installation

Proto-Explorer is included in the PotatoClient project. No separate installation needed.

### Prerequisites

- Clojure CLI tools
- Java 11+
- Pronto library for EDN conversion
- JSON descriptors in `output/json-descriptors/`

**Note**: Proto classes are automatically compiled when needed. The tool ensures all required Java classes are available before running.

## Usage: 2-Step Workflow

**For Claude AI users**: Instead of using these commands directly, use the `proto-class-explorer` agent which automatically handles the proto-explorer tool for you. Simply ask Claude to "use the proto-class-explorer agent to find information about [your protobuf message or Java class]".

### Step 1: Search or List Messages

```bash
# Search by message name or Java class
make proto-search QUERY=root
make proto-search QUERY=gps
make proto-search QUERY=JonSharedCmd

# OR list all messages (with optional filter)
make proto-list
make proto-list FILTER=cmd
```

### Step 2: Get Detailed Information

Use the exact query string shown in the search results:

```bash
# Copy the query string from step 1 results
make proto-info QUERY='cmd.JonSharedCmd$Root'
```

**Note**: Use single quotes for inner classes with `$` to prevent shell expansion.

### Example Workflow

```bash
# Step 1: Search
$ make proto-search QUERY=root

Search results for: "root"
 1. Root    cmd.JonSharedCmd$Root (score: 0.85)
    → Query: cmd.JonSharedCmd$Root

# Step 2: Get details (copy-paste the query string)
$ make proto-info QUERY='cmd.JonSharedCmd$Root'

[Detailed message information...]
```

## API Reference

### Step 1: Search

#### `make proto-search QUERY=<term>`
Searches for protobuf messages by name or Java class.

- **Input**: Search term (case-insensitive)
- **Output**: Ranked results with query strings for step 2
- **Algorithm**: Checks both message names and Java class names

#### `make proto-list [FILTER=<package>]`
Lists all protobuf messages, optionally filtered by package.

- **Input**: Optional package prefix filter
- **Output**: All messages with Java class names for step 2

### Step 2: Get Information

#### `make proto-info QUERY='<result>'`
Provides comprehensive information about a message.

- **Input**: Query string from step 1 (usually a Java class name)
- **Output**: 
  - Java class details (methods, fields)
  - Pronto EDN structure for Clojure
  - Pronto schema showing field types and constraints
  - Field definitions with types and numbers
  - buf.validate constraints (when defined in proto files)
  - Proto file and package information

## Common Use Cases

### Finding GPS-Related Messages
```bash
# Step 1: Search
$ make proto-search QUERY=gps
 1. RotateToGPS    cmd.RotaryPlatform$RotateToGPS
    → Query: cmd.RotaryPlatform$RotateToGPS

# Step 2: Get details
$ make proto-info QUERY='cmd.RotaryPlatform$RotateToGPS'
```

### Understanding the Main Root Message
```bash
# Step 1: Search for root messages
$ make proto-search QUERY=JonSharedCmd
 1. Root    cmd.JonSharedCmd$Root
    → Query: cmd.JonSharedCmd$Root

# Step 2: Get complete structure
$ make proto-info QUERY='cmd.JonSharedCmd$Root'
```

### Exploring a Subsystem
```bash
# Step 1: List messages for a specific package
$ make proto-list FILTER=cmd.Compass

# Step 2: Get details for any message
$ make proto-info QUERY='cmd.Compass$SetMagneticDeclination'
```

### Getting Pronto EDN and Schema for Clojure
```bash
# The info command shows both Pronto EDN structure and schema
$ make proto-info QUERY='cmd.RotaryPlatform$RotateAzimuthTo'

=== PRONTO EDN STRUCTURE ===
{:angle 0.0, :speed 0.0}

=== PRONTO SCHEMA ===
{:angle float, :speed float}
```

## Examples

### Complete 2-Step Workflow

```bash
# Step 1: Search for azimuth-related messages
$ make proto-search QUERY=azimuth

Search results for: "azimuth"
 1. Azimuth                   cmd.RotaryPlatform$Azimuth
    → Query: cmd.RotaryPlatform$Azimuth
 2. RotateAzimuthTo           cmd.RotaryPlatform$RotateAzimuthTo
    → Query: cmd.RotaryPlatform$RotateAzimuthTo

# Step 2: Get details (copy-paste query string)
$ make proto-info QUERY='cmd.RotaryPlatform$RotateAzimuthTo'

PROTOBUF MESSAGE: RotateAzimuthTo
Java Class: cmd.RotaryPlatform$RotateAzimuthTo

=== FIELD DETAILS ===
  [ 1] angle                     : type-float
  [ 2] speed                     : type-float

=== PRONTO EDN STRUCTURE ===
{:angle 0.0, :speed 0.0}

=== PRONTO SCHEMA ===
{:angle float, :speed float}
```

### Example with buf.validate Constraints

When protobuf messages have buf.validate constraints defined, they are automatically extracted and displayed:

```bash
$ make proto-info QUERY='ser.JonSharedDataGps$JonGuiDataGps'

PROTOBUF MESSAGE: JonGuiDataGps
Java Class: ser.JonSharedDataGps$JonGuiDataGps

=== BUF.VALIDATE CONSTRAINTS ===
  longitude                 : {:gte -180.0, :lte 180.0}
  latitude                  : {:gte -90.0, :lte 90.0}
  altitude                  : {:gte -433.0, :lte 8848.86}
  manual_longitude          : {:gte -180.0, :lte 180.0}
  manual_latitude           : {:gte -90.0, :lte 90.0}
  manual_altitude           : {:gte -433.0, :lte 8848.86}

=== FIELD DETAILS ===
  [ 1] longitude                 : type-double
                                   Constraints: {:gte -180.0, :lte 180.0}
  [ 2] latitude                  : type-double
                                   Constraints: {:gte -90.0, :lte 90.0}
  [ 3] altitude                  : type-double
                                   Constraints: {:gte -433.0, :lte 8848.86}
  ...

=== PRONTO EDN STRUCTURE ===
{:longitude 0.0, :latitude 0.0, :altitude 0.0, ...}
```

### Search Examples

```bash
# Search is case-insensitive and handles substrings
make proto-search QUERY=gps
make proto-search QUERY=GPS
make proto-search QUERY=set        # Finds all Set* commands
make proto-search QUERY=JonShared  # Finds by Java class pattern
```

### List Examples

```bash
# List all messages
make proto-list

# List messages in specific package
make proto-list FILTER=cmd.Compass
make proto-list FILTER=ser
```

## Implementation Details

### Architecture

Proto-Explorer uses a stateless architecture that:

1. **Reads** `descriptor-set.json` - A unified JSON descriptor containing all proto definitions
2. **Maps** Proto packages to Java class names using naming conventions
3. **Searches** Using efficient string matching algorithms
4. **Reflects** Java classes for method/field information
5. **Extracts** buf.validate constraints from protobuf field options via Java reflection
6. **Converts** Messages to EDN using Pronto for Clojure integration

### Class Resolution System

The tool uses a multi-tier resolution system for maximum reliability:

1. **Exact Match**: Tries the provided class name directly
2. **Smart Variations**: Generates likely class names based on proto patterns
3. **Fuzzy Matching**: Scans all available proto classes and finds best match
4. **Clear Warnings**: Shows when fallback methods are used with match scores

This ensures the tool always finds the correct Java class, even with complex naming patterns like:
- `cmd.RotaryPlatform` → `cmd.RotaryPlatform.JonSharedCmdRotary$Message`
- `cmd.Gps` → `cmd.Gps.JonSharedCmdGps$Message`
- `ser` → `ser.JonSharedDataGps$Message`

### Key Components

- **descriptor-set-search**: Searches and indexes messages from descriptors
- **simple-api**: Core API for search, list, and info operations
- **class-resolver**: Intelligent class resolution with fallback mechanisms
- **cli-final**: Command-line interface and formatting
- **java-class-info**: Java reflection utilities
- **validation-extractor**: Extracts buf.validate constraints from Java classes
- **JavaReflectionHelper**: Java component for accessing protobuf descriptors and field options
- **pronto-integration**: Pronto EDN conversion and schema extraction

### Proto to Java Mapping Rules

The tool automatically maps proto definitions to Java classes:

- `jon_shared_cmd.proto` (package `cmd`) → `cmd.JonSharedCmd$Message`
- `jon_shared_data.proto` (package `ser`) → `ser.JonSharedData$Message`
- Other files use package as outer class: `cmd.Compass$Message`

### buf.validate Constraints Extraction

Proto-Explorer automatically extracts validation constraints from protobuf messages:

1. **Detection**: Uses Java reflection to access the protobuf descriptor's field options
2. **Extraction**: Parses buf.validate field extensions from the compiled Java classes
3. **Display**: Shows constraints in a dedicated section and inline with field details
4. **Supported Constraints**: 
   - Numeric ranges (gte, gt, lte, lt)
   - String constraints (min_len, max_len, pattern)
   - Enum constraints (defined_only, not_in)
   - Message constraints (required)

The constraints are extracted directly from the compiled classes, ensuring they're always accurate and up-to-date with the proto definitions.

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
- From the tools/proto-explorer directory (Claude AI users: use proto-class-explorer agent instead)
- As part of automated scripts

## Troubleshooting

The tool is designed to be robust and self-healing. Most issues are automatically resolved:

### Automatic Fixes

1. **Missing Proto Classes**: Automatically compiled when needed
2. **Class Name Mismatches**: Resolved via intelligent fallback system
3. **Complex Nesting**: Handled transparently for all message depths

### Manual Fixes (Rare)

1. **"Descriptor not found" errors**: Regenerate descriptors:
   ```bash
   make proto  # Regenerates from proto files
   ```

2. **Dollar sign in class names**: Use single quotes in shell:
   ```bash
   make proto-info QUERY='cmd.JonSharedCmd$Root'  # Correct
   make proto-info QUERY=cmd.JonSharedCmd$Root    # Wrong - $ interpreted by shell
   ```

### Understanding Warnings

When you see warnings like:
```
⚠️ RESOLUTION WARNING:
Class resolved using fuzzy matching. Original: X, Found: Y (score: 0.85, type: message-name-match)
```

This means the tool successfully found your class using its fallback system. The class will work correctly, but you may want to use the suggested class name in future queries for faster resolution.

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
