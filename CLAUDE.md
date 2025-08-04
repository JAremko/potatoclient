# PotatoClient Developer Guide

This guide provides essential context for AI assistants working on PotatoClient. For detailed documentation, see the [docs/](docs/) directory.

## 📚 Documentation Structure

All detailed documentation is organized in the `docs/` directory:

- **[Architecture](docs/architecture/)** - System design and protocols
  - [System Overview](docs/architecture/system-overview.md)
  - [Transit Protocol](docs/architecture/transit-protocol.md)
  - [Video Streaming](docs/architecture/video-streaming.md)
  - [Command System](docs/architecture/command-system.md)

- **[Development](docs/development/)** - Guides and standards
  - [Getting Started](docs/development/getting-started.md)
  - [Code Standards](docs/development/code-standards.md)

- **[Tools](docs/tools/)** - Development tool documentation
- **[Guides](docs/guides/)** - How-to guides
- **[Reference](docs/reference/)** - API and configuration

## 🎯 Key Principles

1. **Clean Architecture** - No backward compatibility, clean implementations only
2. **Keywords Everywhere** - All data uses keywords (except log message text)
3. **Guardrails Usage** - Functions use `>defn`/`>defn-` (23 exceptions in transit layer for performance)
4. **Transit Protocol** - All IPC uses Transit/MessagePack
5. **Protobuf Isolation** - Complete isolation in Kotlin subprocesses

## 🏗️ Architecture Overview

```
Main Process (Clojure)          Subprocesses (Kotlin)
├── UI (Swing)                  ├── Command Process
├── State Management            ├── State Process
├── Transit IPC                 └── Video Streams (2)
└── No Protobuf                     ├── H.264 Decoding
                                   ├── Gesture Recognition
                                   └── Protobuf Handling
```

**Key Points**:
- Clojure process never touches protobuf
- All subprocess communication via Transit
- Static code generation for protobuf ↔ Transit
- Keywords for all enum values

## ⚡ Quick Command Reference

```clojure
;; All commands use nested keyword maps
(require '[potatoclient.transit.commands :as cmd])

;; Platform control
(cmd/send-command! {:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}})
(cmd/send-command! {:rotary {:halt {}}})

;; Recording
(cmd/send-command! {:system {:start-rec {}}})
(cmd/send-command! {:system {:stop-rec {}}})

;; Camera control
(cmd/send-command! {:heat-camera {:next-zoom-table-pos {}}})
(cmd/send-command! {:day-camera {:set-focus-mode {:mode :auto}}})
```

## 🛠️ Development Workflow

### Essential Commands

```bash
make dev          # Development with validation
make nrepl        # REPL on port 7888
make test         # Run tests with logging
make lint         # Check code quality
make help         # All commands with docs
```

### Function Development

**Standard Pattern** (use for most code):
```clojure
(>defn process-data
  "Process data with validation"
  [data options]
  [map? map? => map?]  ; Specs for args => return
  (merge data options))
```

**Exceptions** (23 functions use raw `defn` for performance):
- Transit serialization functions
- Keyword conversion handlers
- Instrumentation utilities

See [Code Standards](docs/development/code-standards.md) for details.

## 📁 Key Files and Namespaces

### Clojure Core
- `potatoclient.main` - Entry point
- `potatoclient.core` - UI initialization
- `potatoclient.transit.*` - IPC layer
- `potatoclient.ui-specs` - Domain schemas

### Kotlin Subprocesses
- `CommandSubprocess.kt` - Command routing
- `StateSubprocess.kt` - State updates
- `VideoStreamManager.kt` - Video coordination
- `GeneratedHandlers.kt` - Auto-generated Transit ↔ Protobuf

### Shared
- `shared/specs/` - Shared Malli specifications
- `resources/config/` - Configuration files

## 🧪 Testing Guidelines

```clojure
;; Test structure
(deftest feature-name-test
  (testing "Specific behavior"
    (is (= expected (actual)))))

;; Use test fixtures
(use-fixtures :each reset-app-db-fixture)

;; Property testing with Malli
(deftest command-generation
  (checking "all commands valid" 100
    [cmd (mg/generator ::cmd/command)]
    (is (m/validate ::cmd/command cmd))))
```

## 🚨 Common Pitfalls

1. **Don't use raw `defn`** - Use `>defn` or `>defn-` (except transit layer)
2. **Keywords not strings** - `:heat` not `"heat"` or `"HEAT"`
3. **No manual protobuf** - Use generated handlers
4. **Check subprocess logs** - Each has individual log file
5. **Transit only for IPC** - No direct subprocess communication

## 🔍 Debugging Tips

```bash
# Enable debug logging
GST_DEBUG=3 make dev  # GStreamer debug

# Check logs
tail -f logs/*.log

# Specific subprocess
tail -f logs/command-subprocess-*.log

# Test with mock tool
cd tools/mock-video-stream && make process
```

## 📝 When Adding Features

1. **Commands**: Update `CommandBuilder.kt` → regenerate handlers
2. **State**: Update protobuf → regenerate handlers
3. **UI**: Add schemas to `ui-specs` namespace
4. **Gestures**: Update `GestureRecognizer.kt`
5. **Always**: Add tests and update docs

## 🎓 Learning Path

1. Start with [Getting Started](docs/development/getting-started.md)
2. Understand [System Architecture](docs/architecture/system-overview.md)
3. Learn [Transit Protocol](docs/architecture/transit-protocol.md)
4. Try [Mock Video Stream](docs/tools/mock-video-stream.md)
5. Read [Code Standards](docs/development/code-standards.md)

## 🔗 Quick Links

- **Issue with unspecced functions?** → `make report-unspecced`
- **Protobuf changes?** → `make proto` then regenerate handlers
- **Lint issues?** → `make lint-report-filtered` (removes false positives)
- **Test failures?** → Check `logs/test-runs/latest/`

Remember: We prioritize clean, idiomatic code over backward compatibility. When in doubt, check the docs!