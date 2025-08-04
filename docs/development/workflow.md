# Development Workflow

This guide covers the day-to-day development workflow for PotatoClient.

## Daily Development Flow

### 1. Starting Development

```bash
# Pull latest changes
git pull origin main

# Clean any stale build artifacts
make clean

# Start development mode with full validation
make dev
```

### 2. REPL Development

For interactive development, use the REPL:

```bash
# Start nREPL server on port 7888
make nrepl

# Connect from your editor:
# - Emacs: M-x cider-connect
# - VS Code: Calva Connect
# - IntelliJ: Clojure Remote REPL
```

Common REPL workflows:

```clojure
;; Reload changed namespaces
(require '[clojure.tools.namespace.repl :refer [refresh]])
(refresh)

;; Test a specific function
(require '[potatoclient.transit.commands :as cmd])
(cmd/send-command! {:ping {}})

;; Inspect app state
(require '[potatoclient.state :as state])
@state/app-db
```

### 3. Making Changes

#### Code Changes

1. **Clojure code**: Changes reload automatically in dev mode
2. **Kotlin code**: Requires subprocess restart
   ```bash
   # After Kotlin changes
   make compile-kotlin
   # Then restart the app
   ```
3. **Protobuf changes**: Full regeneration needed
   ```bash
   make proto
   make compile-kotlin
   ```

#### Adding New Features

1. **Write specs first** using Malli
2. **Implement with Guardrails** (`>defn`)
3. **Add tests** in parallel with implementation
4. **Update documentation** as you go

### 4. Testing During Development

```bash
# Run all tests
make test

# Run specific test file
clojure -M:test -n potatoclient.transit-test

# Run tests with coverage
make test-coverage

# View test results
make test-summary
```

### 5. Code Quality Checks

Before committing:

```bash
# Format code
make fmt

# Run linters
make lint

# Check for unspecced functions
make report-unspecced

# Full quality check
make fmt lint test
```

### 6. Debugging

#### Subprocess Issues

```bash
# Check individual subprocess logs
tail -f logs/command-subprocess-*.log
tail -f logs/state-subprocess-*.log
tail -f logs/video-stream-*.log

# Enable verbose logging
GST_DEBUG=3 make dev  # GStreamer debug
```

#### Performance Issues

```clojure
;; Profile a function
(require '[clj-async-profiler.core :as prof])
(prof/profile
  (dotimes [_ 1000]
    (your-function-here)))
```

## Git Workflow

### Branch Strategy

```bash
# Feature branch
git checkout -b feature/your-feature

# Bug fix
git checkout -b fix/issue-description

# Make changes and commit
git add -p  # Review changes
git commit -m "Clear, concise message"
```

### Commit Guidelines

- Use present tense ("Add feature" not "Added feature")
- Keep first line under 50 characters
- Reference issues when applicable
- One logical change per commit

### Pre-Push Checklist

1. ✓ All tests pass (`make test`)
2. ✓ Code is formatted (`make fmt`)
3. ✓ No lint errors (`make lint`)
4. ✓ Documentation updated
5. ✓ Commit messages are clear

## Common Development Tasks

### Adding a New Command

1. Update protobuf definition
2. Regenerate code: `make proto`
3. Update command builder
4. Add Transit handlers
5. Test with mock-video-stream tool
6. Document in [Adding Commands](../guides/adding-commands.md)

### Debugging Transit Messages

```clojure
;; Enable Transit debug logging
(require '[potatoclient.transit.debug :as debug])
(debug/enable-logging!)

;; Inspect raw messages
(debug/log-message some-transit-data)
```

### Working with Gestures

```clojure
;; Test gesture configuration
(require '[potatoclient.gestures.config :as gesture-config])
(gesture-config/reload-config!)

;; Simulate gestures
(require '[potatoclient.gestures.handler :as gesture])
(gesture/handle-pan-start {:x 100 :y 100})
```

## Troubleshooting

### Common Issues

**Subprocess won't start**
- Check Java version: `java -version` (needs 17+)
- Verify protobuf compilation: `make proto`
- Check subprocess logs in `logs/`

**REPL connection issues**
- Ensure port 7888 is free
- Check firewall settings
- Try `lsof -i :7888` to see what's using the port

**Build failures**
- Clean and rebuild: `make clean && make build`
- Check for uncommitted protobuf changes
- Verify all dependencies: `make check-deps`

## Productivity Tips

### Editor Setup

**Emacs/CIDER**
```elisp
;; Auto-format on save
(add-hook 'cider-mode-hook #'cider-format-buffer-on-save)

;; Jump to test
(define-key cider-mode-map (kbd "C-c t") #'projectile-toggle-between-implementation-and-test)
```

**VS Code/Calva**
- Install "Calva" extension
- Use `Ctrl+Alt+C Enter` to connect
- `Ctrl+Alt+C T` to run tests

### Useful Aliases

Add to your shell profile:

```bash
alias pd="cd ~/potatoclient && make dev"
alias pr="cd ~/potatoclient && make nrepl"
alias pt="cd ~/potatoclient && make test"
alias pf="cd ~/potatoclient && make fmt lint"
```

### Performance Monitoring

```clojure
;; Monitor subprocess communication
(require '[potatoclient.instrumentation :as inst])
(inst/start!)  ; Start collecting metrics

;; View metrics
(inst/report)
```

## Next Steps

- Learn about [Testing](./testing.md)
- Understand [Code Standards](./code-standards.md)
- Explore [Architecture](../architecture/system-overview.md)
- Read [Debugging Guide](../guides/debugging-subprocesses.md)