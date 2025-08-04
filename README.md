# PotatoClient

High-performance multi-process video streaming client with dual H.264 WebSocket streams. Built with clean architecture principles using Clojure for UI and Kotlin for system integration.

## 📚 Documentation

**Start Here**: [Getting Started Guide](docs/development/getting-started.md)

For comprehensive documentation, see the [docs/](docs/) directory:
- [Architecture Overview](docs/architecture/system-overview.md)
- [Development Guides](docs/development/)
- [API Reference](docs/reference/)

## ✨ Features

- **Dual Video Streams**: Heat (900×720) and Day (1920×1080) cameras
- **Hardware Acceleration**: Automatic GPU decoder selection
- **Zero-Allocation**: Optimized video pipeline
- **Multi-Process**: Clean separation of concerns
- **Type-Safe IPC**: Transit protocol with Malli validation
- **Cross-Platform**: Windows, macOS, Linux
- **Themes**: Sol Dark, Sol Light, Dark, Hi-Dark
- **i18n**: English and Ukrainian

## 🚀 Quick Start

```bash
# Clone and setup
git clone https://github.com/JAremko/potatoclient.git
cd potatoclient

# Development mode with validation
make dev

# REPL development
make nrepl
```

## 📋 Requirements

- **Java 17+** with `--enable-native-access=ALL-UNNAMED`
- **Clojure CLI tools**
- **GStreamer 1.0+** with H.264 support
- **Hardware Decoder** (optional): NVIDIA, Intel QSV, or VA-API

See [Getting Started](docs/development/getting-started.md) for detailed setup.

## 🏗️ Architecture

```
┌─────────────────┐     Transit/IPC    ┌──────────────────┐
│  Main Process   │ ←────────────────→ │ Command Process  │
│   (Clojure)     │                    │    (Kotlin)      │
│  - UI (Swing)   │                    │ - Protobuf       │
│  - State Mgmt   │                    │ - WebSocket      │
└─────────────────┘                    └──────────────────┘
         ↑                             
         │         Transit/IPC         ┌──────────────────┐
         ├────────────────────────────→│ State Process    │
         │                             │    (Kotlin)      │
         │                             │ - State Updates  │
         │                             └──────────────────┘
         │         Transit/IPC         
         └────────────────────────────→┌──────────────────┐
                                       │ Video Streams    │
                                       │    (Kotlin)      │
                                       │ - H.264 Decode   │
                                       │ - Gestures       │
                                       └──────────────────┘
```

**Key Principles**:
- **Protobuf Isolation**: All protobuf handling in Kotlin
- **Keywords Everywhere**: Data uses keywords (except logs)
- **Clean Architecture**: No backward compatibility
- **Type Safety**: Guardrails and Malli validation

See [Architecture Docs](docs/architecture/) for details.

## 🛠️ Development

### Common Commands

```bash
# Build and test
make build        # Build JAR
make test         # Run tests
make lint         # Code quality checks

# Development
make dev          # Run with validation
make nrepl        # REPL on port 7888
make help         # All commands
```

### Code Standards

Most functions use Guardrails for validation:
```clojure
(>defn process-data
  [data options]
  [map? map? => map?]
  (merge data options))
```

See [Code Standards](docs/development/code-standards.md) for guidelines.

## 📖 Command System

Commands use nested keyword maps:
```clojure
;; Platform control
(cmd/send-command! {:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}})

;; Recording
(cmd/send-command! {:system {:start-rec {}}})

;; Camera zoom
(cmd/send-command! {:heat-camera {:next-zoom-table-pos {}}})
```

See [Command System](docs/architecture/command-system.md) for details.

## 🧪 Testing

```bash
# Run all tests
make test

# View test summary
make test-summary

# Coverage report
make test-coverage
```

Tests include:
- Unit tests with Malli validation
- Integration tests with mock subprocesses
- Property-based testing
- Mock video stream tool

## 📝 Configuration

Settings in platform-specific locations:
- **Linux**: `~/.config/potatoclient/`
- **macOS**: `~/Library/Application Support/PotatoClient/`
- **Windows**: `%LOCALAPPDATA%\PotatoClient\`

```clojure
{:theme :sol-dark
 :domain "sych.local"
 :locale :english}
```

## 🔧 Tools

Development tools in `tools/`:
- [Proto Explorer](docs/tools/proto-explorer.md) - Protobuf → Malli specs
- [Mock Video Stream](docs/tools/mock-video-stream.md) - Testing tool
- [Guardrails Check](docs/tools/guardrails-check.md) - Find unspecced functions

## 📦 Releases

```bash
# Optimized release build
make release

# Platform packages
make build-linux    # AppImage
make build-windows  # .exe installer
make build-macos    # .dmg bundle
```

Release builds:
- No validation overhead
- Minimal logging
- AOT compilation
- Platform installers

## 🤝 Contributing

1. Read [Code Standards](docs/development/code-standards.md)
2. Use Guardrails for new functions
3. Add tests for new features
4. Update relevant documentation
5. Run `make lint` before committing

## 📄 License

See [LICENSE](LICENSE) file for details.

## 🔗 Links

- [Full Documentation](docs/)
- [Architecture Guide](docs/architecture/system-overview.md)
- [Getting Started](docs/development/getting-started.md)
- [AI Context (CLAUDE.md)](CLAUDE.md)