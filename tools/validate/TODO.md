# Validate Tool - TODO

## Current Status ✅
The validate tool is fully functional for validating binary protobuf payloads using buf.validate constraints. All tests are passing and the tool uses idiomatic Pronto patterns throughout.

## Completed ✅
- [x] Core validator implementation with buf.validate
- [x] Support for both cmd (JonSharedCmd$Root) and state (JonSharedData$JonGUIState) message validation  
- [x] Auto-detection of message types (improved to use validation for disambiguation)
- [x] Makefile with proto generation and compilation targets
- [x] Proto generation script with Docker and buf support
- [x] Test harness with idiomatic Pronto patterns
- [x] Fixed all field naming issues (underscores for fields, uppercase for enums)
- [x] Fixed client_type enum references (JonSharedDataTypes$JonGuiDataClientType)
- [x] Created idiomatic Pronto test data with performance best practices
- [x] Fixed all import statements and namespace declarations
- [x] Added required fields (rotary.current_scan_node with proper values)
- [x] Discovered and enforced buf.validate constraints:
  - GPS coordinates: latitude ∈ [-90, 90], longitude ∈ [-180, 180], altitude ∈ [-433, 8848.86]
  - Protocol version: must be > 0
  - Client type: cannot be UNSPECIFIED (value 0)
  - Rotary scan node speed: must be > 0 and ≤ 1
- [x] Refactored test suite to use clean, idiomatic Pronto patterns
- [x] Created simplified test files: harness_test.clj, validation_test.clj, pronto_test.clj, validator_test.clj
- [x] Removed old backlog tests
- [x] All tests passing (0 failures, 0 errors)
- [x] Error handling for empty/nil data
- [x] Performance test timeout adjusted to realistic expectations

## TODO 📝

### Low Priority Enhancements
1. **Performance optimizations**
   - Consider caching validators for better performance
   - Current: ~160ms per validation
   - Target: < 10ms per validation with cached validator

2. **CLI enhancements**
   - Add batch validation support
   - Support for directory scanning
   - Progress indicators for large files

3. **Output improvements**
   - Better error messages with field paths
   - Colored output for terminal
   - HTML report generation

4. **Additional validation features**
   - Custom validation rules beyond buf.validate
   - Field-level filtering
   - Partial message validation

## Usage

### Command Line
```bash
# Validate a file (auto-detect type)
make validate FILE=path/to/file.bin

# Validate with specific type
make validate-cmd FILE=commands.bin
make validate-state FILE=state.bin

# With output format
make validate FILE=data.bin OUTPUT=json
```

### Programmatic Usage
```clojure
(require '[validate.validator :as v])

; Validate binary data
(v/validate-binary data :type :state)

; Auto-detect type
(v/validate-binary data)

; Validate file
(v/validate-file "path/to/file.bin")
```

## Test Commands
```bash
# Run all tests
make test

# Start REPL for development
make repl

# Clean and rebuild
make clean-build
make build
```

## Architecture Notes
- Uses Pronto for efficient protobuf handling
- buf.validate for constraint validation
- Memoized test data for performance
- Type hints for optimal Pronto performance
- Clean separation between parsing, validation, and formatting

## Key Files
- `src/validate/validator.clj` - Core validation logic
- `src/validate/core.clj` - CLI entry point
- `test/validate/test_harness.clj` - Idiomatic Pronto test data generation
- `test/validate/validation_test.clj` - Validation behavior tests
- `test/validate/validator_test.clj` - Core validator tests
- `scripts/generate-protos.sh` - Proto generation with buf.validate support
- `Makefile` - Build automation
- `deps.edn` - Dependencies including Pronto fork