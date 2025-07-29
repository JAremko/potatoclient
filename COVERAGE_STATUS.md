# Coverage Status Report

## Current State

The test coverage infrastructure is partially working for PotatoClient:

### ✅ Working
- **Demo coverage** on core namespaces shows 24.77% forms coverage, 68.99% line coverage
- **Cloverage** successfully instruments pure Clojure namespaces
- **HTML reports** are generated correctly when instrumentation succeeds
- **Test suite** passes with 116 tests, 3549 assertions

### ❌ Limitations
- **ClassNotFoundException** when trying to instrument namespaces that use protobuf classes
- Cannot achieve full project coverage due to mixed Clojure/Java/Protobuf codebase
- Namespaces with protobuf dependencies must be excluded from instrumentation

## Coverage Results (Demo)

From `./scripts/demo-coverage.sh`:

```
|----------------------------+---------+---------|
|                  Namespace | % Forms | % Lines |
|----------------------------+---------+---------|
|        potatoclient.config |    1.00 |   12.32 |
|          potatoclient.i18n |   29.89 |   59.09 |
|       potatoclient.logging |    4.57 |   16.67 |
|       potatoclient.runtime |   95.60 |  100.00 |
|         potatoclient.specs |   85.04 |   94.94 |
| potatoclient.state.schemas |   96.08 |   93.84 |
|         potatoclient.theme |    1.82 |   19.48 |
|----------------------------+---------+---------|
|                  ALL FILES |   24.77 |   68.99 |
|----------------------------+---------+---------|
```

## Root Cause

The issue stems from Cloverage's instrumentation timing:
1. Cloverage loads and instruments namespaces before tests run
2. When it encounters `import` statements for protobuf classes, those classes aren't on the classpath yet
3. This causes ClassNotFoundException for protobuf-generated classes

## Workarounds

### 1. Exclude Problematic Namespaces (Current)
```clojure
:main-opts ["-m" "cloverage.coverage"
            "--ns-exclude-regex" ".*cmd\\..*|.*proto.*|.*ipc.*"]
```

### 2. Run Coverage on Subset
```bash
./scripts/demo-coverage.sh  # Works, but limited coverage
```

### 3. Use Namespace Selection
```bash
clojure -M:test-coverage \
  -n "potatoclient.specs" \
  -n "potatoclient.runtime" \
  -n "potatoclient.state.schemas"
```

## Recommendations

1. **For CI/CD**: Use the demo coverage approach to track core namespace coverage
2. **For development**: Focus on improving coverage in measurable namespaces
3. **For full coverage**: Consider separating protobuf code into a separate artifact
4. **Alternative tools**: Investigate other coverage tools that handle mixed codebases better

## Commands Available

```bash
# Working demo (subset coverage)
./scripts/demo-coverage.sh

# Basic coverage (with exclusions)
make coverage

# Full test suite (no coverage)
make test
```

## Next Steps

1. Accept current limitations and focus on improving coverage in measurable namespaces
2. Consider restructuring code to separate protobuf dependencies
3. Investigate alternative coverage tools (e.g., JaCoCo for JVM bytecode)
4. Set up CI with current working coverage approach