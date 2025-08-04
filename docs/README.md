# PotatoClient Documentation

Welcome to the PotatoClient documentation. This directory contains comprehensive documentation organized by topic.

## 📁 Documentation Structure

### [Architecture](./architecture/)
Core system architecture and design decisions
- [System Overview](./architecture/system-overview.md) - High-level architecture
- [Transit Protocol](./architecture/transit-protocol.md) - Inter-process communication
- [Video Streaming](./architecture/video-streaming.md) - Video subsystem architecture
- [Command System](./architecture/command-system.md) - Command routing and protobuf integration

### [Development](./development/)
Development guides and best practices
- [Getting Started](./development/getting-started.md) - Quick start guide
- [Development Workflow](./development/workflow.md) - Day-to-day development
- [Code Standards](./development/code-standards.md) - Guardrails, Malli, and conventions
- [Testing Guide](./development/testing.md) - Testing strategies and tools

### [Tools](./tools/)
Documentation for development tools
- [Proto Explorer](./tools/proto-explorer.md) - Protobuf to Malli spec generation
- [Mock Video Stream](./tools/mock-video-stream.md) - Testing video streams
- [Guardrails Check](./tools/guardrails-check.md) - Finding unspecced functions
- [Transit Test Generator](./tools/transit-test-generator.md) - Test data generation

### [Guides](./guides/)
How-to guides for common tasks
- [Adding Commands](./guides/adding-commands.md) - Adding new protobuf commands
- [Adding Languages](./guides/adding-languages.md) - Internationalization
- [Adding Themes](./guides/adding-themes.md) - UI theming
- [Debugging Subprocesses](./guides/debugging-subprocesses.md) - Subprocess debugging

### [Reference](./reference/)
API and configuration reference
- [Configuration](./reference/configuration.md) - Config file format and options
- [Build Targets](./reference/build-targets.md) - Makefile targets reference
- [Message Types](./reference/message-types.md) - Transit message specifications
- [Keyboard Shortcuts](./reference/keyboard-shortcuts.md) - UI shortcuts

## 🎯 Key Principles

1. **Clean Architecture** - No backward compatibility, clean implementations only
2. **Keywords Everywhere** - All data uses keywords (except log message text)
3. **Guardrails Required** - All functions use `>defn` or `>defn-` (never raw `defn`)
4. **Transit Protocol** - All IPC uses Transit/MessagePack
5. **Protobuf Isolation** - Complete isolation in Kotlin subprocesses

## 🚀 Quick Links

- **Start Here**: [Getting Started](./development/getting-started.md)
- **Architecture**: [System Overview](./architecture/system-overview.md)
- **Contributing**: [Code Standards](./development/code-standards.md)
- **Testing**: [Testing Guide](./development/testing.md)

## 📝 Document Status

This documentation reflects the current state of PotatoClient as of August 2025. We maintain:
- No legacy documentation
- No versioning (always current)
- Clean, idiomatic examples
- Verified, working code samples

For the main project overview, see the [root README.md](../README.md).
For AI assistant context, see [CLAUDE.md](../CLAUDE.md).