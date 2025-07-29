# Code Coverage Guide

This guide explains the code coverage setup for PotatoClient and how to generate coverage reports.

## Overview

PotatoClient uses:
- **Cloverage** for Clojure code coverage
- **JaCoCo** for Java/Kotlin code coverage

## Quick Start

```bash
# Run demo coverage (subset of namespaces)
./scripts/demo-coverage.sh

# View report
open target/demo-coverage/index.html
```

## Coverage Commands

### Basic Coverage (Recommended)

```bash
make coverage
```

This runs coverage on Clojure namespaces that don't depend on protobuf classes.

### Full Coverage Suite

```bash
make test-coverage
```

This attempts to run full coverage including Java/Kotlin code with JaCoCo.

### Demo Coverage

```bash
./scripts/demo-coverage.sh
```

Demonstrates working coverage on core namespaces:
- `potatoclient.specs` - 85% coverage
- `potatoclient.runtime` - 95% coverage  
- `potatoclient.state.schemas` - 96% coverage
- `potatoclient.i18n` - 29% coverage
- `potatoclient.config` - 1% coverage
- `potatoclient.theme` - 1% coverage
- `potatoclient.logging` - 4% coverage

## Understanding Coverage Results

### Coverage Metrics

- **% Forms**: Percentage of Clojure forms (expressions) covered
- **% Lines**: Percentage of source lines covered

### Color Coding

- 🟢 **Green** (>80%): Good coverage
- 🟡 **Yellow** (50-80%): Moderate coverage  
- 🔴 **Red** (<50%): Low coverage

### HTML Reports

Coverage reports include:
- `index.html` - Summary of all namespaces
- Individual `.html` files - Line-by-line coverage
- `coverage.txt` - Text summary
- `codecov.json` - Machine-readable format

## Known Limitations

### Protobuf Dependencies

Namespaces that depend on protobuf classes currently can't be instrumented by Cloverage:
- `potatoclient.cmd.*` - Command namespaces
- `potatoclient.proto` - Protobuf serialization
- `potatoclient.ipc` - IPC messaging

This is because Cloverage tries to instrument code before protobuf classes are on the classpath.

### Workarounds

1. **Exclude problematic namespaces**: Use `--ns-exclude-regex` to skip them
2. **Run specific namespaces**: Use `-n` flag to test only certain namespaces
3. **Ensure compilation**: Always run `make build` before coverage

## Configuration

### deps.edn

The `:test-coverage` alias configures Cloverage:

```clojure
:test-coverage {:extra-paths ["test" "target/classes" "target/test-classes"]
                :extra-deps {cloverage/cloverage {:mvn/version "1.2.4"}}
                :jvm-opts ["-Dguardrails.enabled=true"]
                :main-opts ["-m" "cloverage.coverage"
                           "-p" "src"
                           "-s" "test"
                           "--ns-exclude-regex" ".*instrumentation.*|.*cmd\\..*|.*proto.*|.*ipc.*"
                           "--codecov"
                           "--junit"
                           "--text"
                           "--html"
                           "--output" "target/coverage"]}
```

### Excluded Namespaces

- `.*instrumentation.*` - Malli instrumentation (dev only)
- `.*cmd\\..*` - Command namespaces (protobuf deps)
- `.*proto.*` - Protobuf serialization
- `.*ipc.*` - IPC messaging

## Best Practices

1. **Run tests first**: Ensure all tests pass before coverage
2. **Check compilation**: Run `make build` to compile all classes
3. **Focus on core logic**: Prioritize coverage for business logic
4. **Exclude generated code**: Don't measure protobuf generated classes
5. **Regular monitoring**: Run coverage as part of CI/CD

## Improving Coverage

### Low Coverage Areas

Based on demo results:
- `potatoclient.config` (1%) - Needs test for configuration loading
- `potatoclient.theme` (1%) - Needs test for theme switching
- `potatoclient.logging` (4%) - Needs test for log functions
- `potatoclient.i18n` (29%) - Needs test for all translations

### Writing Tests for Coverage

```clojure
;; Example: Testing config namespace
(deftest test-config-loading
  (testing "Config file loading"
    (is (map? (config/load-config)))
    (is (contains? (config/load-config) :theme))))

;; Example: Testing theme namespace  
(deftest test-theme-functions
  (testing "Available themes"
    (is (seq (theme/get-available-themes)))
    (is (contains? (theme/get-available-themes) :sol-dark))))
```

## Alternative Approaches

Based on research, here are some approaches for mixed Clojure/Java/Protobuf codebases:

### 1. Namespace Exclusion (Current Approach)

Exclude problematic namespaces from instrumentation:
```bash
clojure -M:test-coverage \
  --ns-exclude-regex ".*cmd\\..*|.*proto.*|.*ipc.*"
```

### 2. Selective Namespace Inclusion

Only instrument specific namespaces:
```bash
clojure -M:test-coverage \
  -n "potatoclient.specs" \
  -n "potatoclient.theme" \
  -n "potatoclient.config"
```

### 3. Pre-compilation Strategy

1. Compile all Java/Kotlin/Protobuf classes first
2. Add compiled classes to classpath
3. Run coverage with proper classpath ordering

### 4. Separate Test Runs

1. Run tests first to ensure all classes are loaded
2. Run coverage as a separate step
3. Use test selectors to run specific test suites

## Future Improvements

1. **Fix classpath issues**: Resolve protobuf class loading for full coverage
2. **Integrate with CI**: Add coverage checks to GitHub Actions
3. **Coverage badges**: Display coverage percentage in README
4. **Minimum thresholds**: Fail builds if coverage drops below threshold
5. **Incremental coverage**: Show coverage changes per PR
6. **Mixed language coverage**: Combine Clojure (Cloverage) + Java/Kotlin (JaCoCo) reports

## Troubleshooting

### ClassNotFoundException

**Problem**: `cmd.RotaryPlatform.JonSharedCmdRotary$AngleTo`

**Solution**: Exclude cmd namespaces or ensure protobuf classes are compiled first

### No HTML Report Generated

**Problem**: Only junit.xml created

**Solution**: Check for errors in coverage.log, ensure tests actually run

### Low Coverage Numbers

**Problem**: Coverage shows 0% or very low

**Solution**: Verify tests are actually exercising the code, check test selectors