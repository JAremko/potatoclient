# State-Explorer

A diagnostic tool for capturing and analyzing WebSocket state messages from the PotatoClient state endpoint (`/ws/ws_state`). This tool connects to the WebSocket endpoint, captures binary Protocol Buffer payloads, and converts them to human-readable EDN format for analysis.

## Features

- **WebSocket State Capture**: Connect to `wss://<domain>/ws/ws_state` endpoint
- **Binary Payload Storage**: Save raw protobuf messages for debugging
- **Full EDN Conversion**: Automatic conversion to Clojure EDN format with complete data extraction
- **Pretty Formatting**: Uses zprint for beautiful, readable EDN output
- **Self-Contained**: Includes its own protobuf compilation (no external dependencies)
- **Configurable Capture**: Control number of payloads, output directory, and connection parameters
- **Progress Tracking**: Real-time progress during capture
- **Well-Formed EDN**: Properly structured, readable Clojure data with keyword conversion
- **SSL Support**: Works with both secure (wss://) and insecure (ws://) connections

## Quick Start

```bash
# Show available commands
make

# Capture one state message (most common usage)
make run

# View the captured data
make view-latest
```

## Installation

### Prerequisites

1. **Clojure CLI tools** (1.11+)
2. **Java** (11 or higher)
3. **Protocol Buffers compiler** (protoc) - available in system PATH

### Setup

```bash
cd tools/state-explorer

# First time setup - downloads dependencies and builds protobuf classes
make deps
make build

# Or just run directly - it will build automatically if needed
make run
```

## Usage

### Basic Capture

```bash
# Capture a single state payload from default server (sych.local)
make run
```

This will:
1. Connect to `wss://sych.local/ws/ws_state`
2. Capture one state message
3. Save two files:
   - `output/<timestamp>.bin` - Raw protobuf binary
   - `output/<timestamp>.edn` - Human-readable EDN with full data

### Capture Multiple Payloads

```bash
# Capture 10 payloads
make capture-many

# Or specify custom count
make capture COUNT=20

# Capture 100 payloads to custom directory
make capture COUNT=100 OUTPUT=/tmp/analysis
```

### Custom Server

```bash
# Different domain
make capture DOMAIN=myserver.com

# Local development without SSL
make capture DOMAIN=localhost NO_SSL=true

# Production server with verbose logging
make capture DOMAIN=prod.example.com VERBOSE=true COUNT=5
```

### Continuous Monitoring

```bash
# Capture every 5 seconds continuously (Ctrl+C to stop)
make watch
```

## Output Format

Each captured payload is saved as two files with matching timestamps:

### Binary File (`<timestamp>.bin`)
- Raw protobuf binary data
- Exact bytes received from WebSocket
- Can be used for replay or debugging
- Typical size: 400-500 bytes

### EDN File (`<timestamp>.edn`)
- Human-readable Clojure data structure
- Pretty-formatted using zprint for optimal readability
- Complete extraction of all protobuf fields
- Properly converted field names (snake_case → kebab-case)
- Enum values as keywords

Example EDN structure:
```clojure
{:protocol-version 1
 :system {:cpu-load 42.0
          :gpu-load 42.0
          :disk-space 95
          :low-disk-space true
          :rec-enabled true
          :loc :jon-gui-data-system-localization-en
          ...}
 :gps {:latitude 50.0236
       :longitude 15.8152
       :altitude 0.2916
       :fix-type :jon-gui-data-gps-fix-type-3d
       ...}
 :camera-day {:zoom-current 1.0
              :focus-mode :auto
              :brightness 50
              ...}
 :camera-heat {:palette :white-hot
               :gain 2.5
               ...}
 :rotary {:azimuth 45.0
          :elevation 10.0
          ...}
 :compass {:heading 270.0
           :declination 5.0
           ...}
 :lrf {:measure-id 52
       :target {:observer-latitude 8.0
                :observer-longitude 7.0
                ...}}
 ...}
```

## Viewing and Analyzing Data

### View Latest Capture

```bash
# Pretty-print the most recent EDN file (120 char width)
make view-latest

# Compact view with 80 character width
make view-compact
```

### Analyze All Captures

```bash
# Show summary of all captured files
make analyze
```

This displays:
- File names and sizes
- Top-level keys for each capture
- Useful for comparing state changes

### Manual Analysis

```bash
# List all captures
ls -la output/

# View binary file (hex dump)
xxd output/1234567890.bin | less

# Load EDN in Clojure REPL
clojure -M -e "(require 'clojure.edn) (clojure.pprint/pprint (clojure.edn/read-string (slurp \"output/1234567890.edn\")))"
```

## Command Line Options

Run directly with Clojure for full control:

```bash
clojure -M:run [options]
```

Options:
- `-d, --domain DOMAIN` - WebSocket server domain (default: `sych.local`)
- `-c, --count COUNT` - Number of payloads to capture (default: `1`)
- `-o, --output-dir DIR` - Output directory (default: `./output`)
- `-p, --port PORT` - WebSocket port if not standard
- `-e, --endpoint ENDPOINT` - WebSocket path (default: `/ws/ws_state`)
- `-s, --no-ssl` - Use ws:// instead of wss://
- `-v, --verbose` - Enable verbose logging
- `-h, --help` - Show help message

## Development

### REPL

Start a REPL for interactive development:

```bash
make repl
```

Example REPL usage:
```clojure
(require '[state-explorer.core :as core])
(require '[state-explorer.pronto-handler :as proto])

;; Capture directly
(core/run-capture {:domain "sych.local" :count 2})

;; Parse existing binary file
(def binary-data (java.nio.file.Files/readAllBytes (java.nio.file.Paths/get "output/1234567890.bin" (into-array String []))))
(def proto-map (proto/parse-state-message binary-data))
(def edn-data (proto/proto->edn proto-map))
```

### Testing

```bash
# Run test suite
make test
```

### Project Structure

```
state-explorer/
├── src/state_explorer/
│   ├── core.clj                 # Main entry point
│   ├── websocket_simple.clj     # WebSocket client
│   ├── capture.clj              # File management
│   ├── proto_handler_simple.clj # Protobuf handling
│   ├── proto_to_edn.clj        # EDN conversion
│   └── cli.clj                  # CLI interface
├── output/                       # Captured files (gitignored)
├── target/classes/               # Compiled protobuf classes
├── deps.edn                      # Dependencies
├── Makefile                      # User interface
└── build_proto.sh               # Build script
```

## How It Works

1. **Connection**: Uses Java 11+ HTTP client for WebSocket
2. **SSL Handling**: Trusts all certificates for development
3. **Message Reception**: Receives binary protobuf messages
4. **Parsing**: Uses `JonSharedData$JonGUIState` protobuf class
5. **Conversion**: Reflection-based extraction to EDN
6. **Storage**: Saves both binary and EDN with millisecond timestamps

## EDN Conversion Details

The tool performs sophisticated protobuf-to-EDN conversion:

- **Field Names**: `snake_case` → `:kebab-case` keywords
- **Enums**: Converted to keywords (e.g., `:jon-gui-data-gps-fix-type-3d`)
- **Nested Messages**: Recursively converted to maps
- **Collections**: Converted to Clojure vectors
- **Numbers**: Preserved as appropriate Clojure types
- **Timestamps**: Unix timestamps as longs

## Troubleshooting

### "Protobuf classes not found"

```bash
# Build protobuf classes
make build
```

### "Connection refused"

- Verify server is running
- Check domain name resolution
- Try with `--no-ssl` for local servers

### "SSL handshake failed"

The tool trusts all certificates by default. If still failing:
- Check server configuration
- Verify port is correct
- Try non-SSL connection

### "No EDN file created"

The tool now includes full EDN conversion. If EDN is missing:
- Check for error messages in output
- Verify protobuf classes are up to date
- Use `-v` flag for detailed logging

## Clean Up

```bash
# Remove captured files only
make clean

# Remove everything (build artifacts and captures)
make clean-build
```

## Performance

- **Capture Speed**: ~200ms per payload
- **File Size**: Binary ~450 bytes, EDN ~2-3KB
- **Memory Usage**: Minimal, streaming architecture
- **Max Payload Size**: 2MB (configurable)

## Related Tools

- [Proto-Explorer](../proto-explorer/README.md) - Explore protobuf message definitions (Claude AI users: use the `proto-class-explorer` agent instead)
- [WebSocket Documentation](../../docs/websocket-endpoints.md) - Detailed endpoint information

## Future Enhancements

Potential improvements:
- Command endpoint support (`/ws/ws_cmd`)
- Payload filtering by content
- Diff tool between captures
- Real-time state change notifications
- Export to JSON/CSV formats
- State replay functionality

## License

Part of the PotatoClient project.

## Version

1.1.0 - Added zprint for beautiful EDN formatting with configurable views