# Getting Started with PotatoClient

This guide will help you set up and run PotatoClient for development.

## Prerequisites

### Required Software

1. **Java 17 or higher**
   ```bash
   java -version  # Should show 17+
   ```

2. **Clojure CLI tools**
   ```bash
   # Install via your package manager or:
   curl -L -O https://github.com/clojure/brew-install/releases/latest/download/linux-install.sh
   chmod +x linux-install.sh
   sudo ./linux-install.sh
   ```

3. **GStreamer 1.0+**
   ```bash
   # Ubuntu/Debian
   sudo apt install gstreamer1.0-tools gstreamer1.0-plugins-good \
                    gstreamer1.0-plugins-bad gstreamer1.0-plugins-ugly \
                    gstreamer1.0-libav

   # macOS
   brew install gstreamer

   # Arch Linux
   sudo pacman -S gstreamer gst-plugins-good gst-plugins-bad gst-plugins-ugly
   ```

4. **Hardware Video Decoder** (Optional but recommended)
   - NVIDIA: Install CUDA and gstreamer1.0-plugins-bad-nvidia
   - Intel: Install intel-media-driver
   - AMD: Install mesa-va-drivers

### System Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/JAremko/potatoclient.git
   cd potatoclient
   ```

2. **Verify prerequisites**
   ```bash
   make check-deps  # Checks all dependencies
   ```

## Quick Start

### Running in Development Mode

```bash
# Standard development mode with all validation
make dev

# With reflection warnings (slower but catches issues)
make dev-reflect

# With REPL on port 7888
make nrepl
```

### First Run Configuration

1. **Domain Setup**
   - On first run, you'll be prompted for the WebSocket server domain
   - Enter your server address (e.g., `sych.local`)
   - This is saved to platform-specific config location

2. **Video Streams**
   - Heat stream: 900×720 thermal camera
   - Day stream: 1920×1080 visible camera
   - Both start automatically when configured

3. **Default Settings**
   - Theme: Sol Dark
   - Language: English
   - Logging: Console + timestamped file in `./logs/`

## Development Workflow

### 1. REPL Development

```bash
# Start REPL
make nrepl

# In your editor, connect to nrepl://localhost:7888
# Emacs: M-x cider-connect
# VS Code: Calva connect
# IntelliJ: Cursive remote REPL
```

### 2. Running Tests

```bash
# Run all tests
make test

# Run specific test namespace
make test-ns NS=potatoclient.transit-test

# View test summary
make test-summary
```

### 3. Checking Code Quality

```bash
# Run all linters
make lint

# Check for functions missing Guardrails
make report-unspecced

# Generate filtered lint report (removes false positives)
make lint-report-filtered
```

### 4. Building

```bash
# Build JAR
make build

# Run the built JAR (production-like)
make run

# Create release build
make release
```

## Project Structure

```
potatoclient/
├── src/
│   ├── potatoclient/      # Clojure source
│   ├── kotlin/            # Kotlin subprocesses
│   └── java/              # Java interfaces
├── test/                  # Tests
├── resources/             # Config, i18n, assets
├── tools/                 # Development tools
├── docs/                  # Documentation
├── shared/specs/          # Shared Malli specs
└── logs/                  # Development logs
```

## Key Concepts

### 1. Multi-Process Architecture
- **Main Process** (Clojure): UI and coordination
- **Command Subprocess** (Kotlin): Sends commands
- **State Subprocess** (Kotlin): Receives state
- **Video Subprocesses** (Kotlin): Handle streams

### 2. Transit Protocol
All inter-process communication uses Transit with MessagePack:
- Type-safe message passing
- Automatic keyword conversion
- No manual serialization

### 3. Guardrails
Most functions use Guardrails for validation:
```clojure
(>defn process-data
  [data options]
  [map? map? => map?]
  (merge data options))
```

**Note**: Some transit-related namespaces use raw `defn` for performance.

### 4. Keywords Everywhere
All data uses keywords except actual text:
```clojure
;; Good - keywords for data
{:stream-type :heat
 :mode :stabilized}

;; Strings only for human text
{:message "Connection established"}
```

## Common Tasks

### Adding a Command
See [Adding Commands Guide](../guides/adding-commands.md)

### Adding a Language
See [Adding Languages Guide](../guides/adding-languages.md)

### Debugging Subprocesses
See [Debugging Guide](../guides/debugging-subprocesses.md)

## Troubleshooting

### GStreamer Issues
```bash
# Check GStreamer installation
gst-inspect-1.0 --version

# Test hardware decoder
gst-inspect-1.0 nvh264dec  # NVIDIA
gst-inspect-1.0 vaapih264dec  # Intel/AMD
```

### Java Native Access
If you see native access warnings:
```bash
# Add to your shell profile
export _JAVA_OPTIONS="--enable-native-access=ALL-UNNAMED"
```

### Port Already in Use
```bash
# Find process using port 7888
lsof -i :7888

# Kill it if needed
kill -9 <PID>
```

### Subprocess Crashes
Check individual logs:
```bash
ls logs/
# Look for command-subprocess-*.log
# Look for state-subprocess-*.log
# Look for video-stream-*.log
```

## Next Steps

1. Read [Code Standards](./code-standards.md) for development practices
2. Explore [Architecture Overview](../architecture/system-overview.md)
3. Try the [Mock Video Stream Tool](../tools/mock-video-stream.md)
4. Join development discussions

## Getting Help

- Check logs in `./logs/` directory
- Run with `GST_DEBUG=3` for video issues
- Use `make help` to see all available commands
- Refer to inline Makefile documentation