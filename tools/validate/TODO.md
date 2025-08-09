# Validate Tool - TODO

## Current Status
The validate tool is being developed to validate binary protobuf payloads using buf.validate constraints. The core validation functionality is working, but tests need to be fixed and the tool needs to use idiomatic Pronto throughout.

## Completed ✅
- [x] Core validator implementation with buf.validate
- [x] Support for both cmd (JonSharedCmd$Root) and state (JonSharedData$JonGUIState) message validation  
- [x] Auto-detection of message types (improved to use validation for disambiguation)
- [x] Makefile with proto generation and compilation targets
- [x] Proto generation script with Docker and buf support
- [x] Basic test harness setup
- [x] Fixed client_type enum references (must use JonSharedDataTypes$JonGuiDataClientType)
- [x] Created idiomatic Pronto test data file with performance best practices
- [x] Fixed all import statements in test files
- [x] Discovered buf.validate constraints:
  - GPS coordinates: latitude ∈ [-90, 90], longitude ∈ [-180, 180], altitude ∈ [-433, 8848.86]
  - Protocol version: must be > 0
  - Client type: cannot be UNSPECIFIED (value 0)
- [x] Refactored test suite to use clean, idiomatic Pronto patterns
- [x] Created simplified test files to reduce nesting complexity
- [x] Moved old tests to backlog/ directory for reference

## In Progress 🚧
- [ ] Fix field naming issues in test harness
  - All proto field names use underscores, not hyphens
  - Need to systematically update real-state-edn in test_harness.clj
  - Fields like :cpu_load, :protocol_version, :day_cam_glass_heater

## TODO 📝

### High Priority
1. **Complete test suite refactoring**
   - Moved old tests to backlog/ directory
   - Created clean test harness with real EDN
   - Split tests into smaller files: harness_test.clj, validation_test.clj, pronto_test.clj
   - Need to fix remaining field name issues

2. **Update all tests to use Pronto-based data**
   - Replace builder pattern tests with Pronto proto-maps
   - Use performant p/p-> macro with hints where appropriate
   - Ensure tests serve as good reference examples

3. **Fix remaining test failures**
   - comprehensive_test.clj - needs Pronto-based test data
   - e2e_test.clj - fix Files API calls and Jimfs usage
   - validator_test.clj - update to use new test data
   - simple_message_test.clj - migrate to Pronto

4. **Remove or fix commented-out code**
   - Clean up any disabled tests
   - Remove temporary test files

### Medium Priority
5. **Ensure all tests pass**
   - Run `make test` and fix any remaining failures
   - Verify auto-detection works correctly
   - Test validation constraints are properly enforced

6. **Update documentation**
   - Update README with usage examples using Pronto
   - Document the two validation modes (cmd vs state)
   - Add examples of validation output

7. **Update Makefile if needed**
   - Ensure all targets work correctly
   - Add any missing convenience targets

### Low Priority
8. **Performance optimizations**
   - Add benchmarks for validation performance
   - Consider caching validators if needed

9. **Additional features**
   - Add CLI interface for validating files
   - Support for streaming validation
   - Better error messages with field paths

## Known Issues 🐛
1. **Namespace/filename mismatch** - Clojure requires files with underscores for namespaces with hyphens
   - `validate.pronto-test-data` → `validate/pronto_test_data.clj`

2. **Required fields in protobuf messages**
   - State messages have many required fields that must all be present
   - Commands require valid client_type (not 0/UNSPECIFIED)

3. **Import issues**
   - Some generated classes have different names than expected
   - Need to verify exact class structure from generated Java files

## Pronto Performance Best Practices 🚀

### Critical Performance Guidelines
Our tests serve as reference implementations for Pronto usage throughout the application. Always follow these performance best practices:

#### 1. Read Performance
- **Direct keyword access** is faster than regular Clojure maps for proto-maps with >8 fields
- **Use hints for fastest reads**: Direct Java getter calls via `p/p->` with hints
```clojure
;; Fast: Direct Java getter with hints
(p/with-hints [(p/hint my-proto-map People$Person my-mapper)]
  (p/p-> my-proto-map :address :city))

;; Slower: Dynamic dispatch on keywords
(get-in my-proto-map [:address :city])
```

#### 2. Write Performance
- **Single assoc is costly**: Each assoc does a proto->builder->proto roundtrip
- **Batch operations with `p/p->`**: All mutations in single builder roundtrip
```clojure
;; FAST: Single builder roundtrip
(p/p-> my-proto-map
       (assoc :field1 val1)
       (assoc :field2 val2)
       (assoc :field3 val3))

;; SLOW: Multiple builder roundtrips
(-> my-proto-map
    (assoc :field1 val1)  ; builder roundtrip 1
    (assoc :field2 val2)  ; builder roundtrip 2
    (assoc :field3 val3)) ; builder roundtrip 3
```

#### 3. Use Hints for Maximum Performance
Hints enable direct Java method calls, bypassing keyword dispatch:
```clojure
;; Define hint once, use throughout scope
(p/with-hints [(p/hint state-proto-map ser.JonSharedData$JonGUIState state-mapper)]
  (p/p-> state-proto-map
         (assoc :protocol_version 2)
         (update-in [:system :cpu_load] + 10.0)
         (assoc-in [:gps :latitude] 51.5074)))
```

#### 4. Creation Best Practices
```clojure
;; FAST: Create with all fields at once
(p/proto-map cmd-mapper cmd.JonSharedCmd$Root
             :protocol_version 1
             :session_id 1000
             :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)

;; SLOW: Create empty then assoc
(-> (p/proto-map cmd-mapper cmd.JonSharedCmd$Root)
    (assoc :protocol_version 1)
    (assoc :session_id 1000))
```

#### 5. Memory Efficiency
- Proto-maps have only 24 bytes overhead per message
- Far more memory-efficient than equivalent Clojure maps
- Use proto-maps directly; avoid unnecessary EDN conversions

### Performance Benchmarks
Based on benchmarks on i7-8750H, 2.2 GHz, JDK 17:
- **Reads**: Proto-maps faster than Clojure maps for >8 fields
- **Writes with `p/p->`**: Outperforms Clojure at ~5+ mutations
- **With hints**: Performance approaches raw Java speed
- **Memory**: Proto-maps use significantly less memory than Clojure maps

## Core Pronto Concepts

### Proto-Map Fundamentals
- **Proto-maps** are thin wrappers around Java POJOs that implement Clojure map interfaces
- They maintain **schema integrity** - reject operations that would break the schema
- **Immutable** - assoc creates new instances
- **Memory efficient** - only 24 bytes overhead per message
- **Type safe** - fail-fast on invalid keys or wrong value types

### Key Differences from Clojure Maps
1. **Complete schema always present** - all fields exist with defaults
2. **No dissoc** - fields cannot be removed
3. **Closed maps** - unknown keys throw errors (not nil)
4. **Type enforcement** - wrong types throw errors immediately
5. **Nil semantics**:
   - Scalar fields: never nil, use zero-values
   - Message fields: can be nil when unset

### Creating Proto-Maps
```clojure
;; Empty proto-map
(p/proto-map mapper People$Person)

;; With initial values (FASTEST)
(p/proto-map mapper People$Person 
             :name "Rich" 
             :id 0 
             :pet_names ["FOO" "BAR"])

;; From Clojure map
(p/clj-map->proto-map mapper People$Person 
                      {:id 0 :name "hello" :address {:city "London"}})

;; From byte array
(p/bytes->proto-map mapper People$Person byte-array)

;; Wrap existing POJO
(p/proto->proto-map mapper existing-pojo)
```

### Field Types
- **Scalars**: Follow protobuf-Java mappings, support Clojure numerics
- **Messages**: Return nested proto-maps
- **Repeated**: Return Clojure vectors `["foo" "bar"]`
- **Maps**: Return Clojure maps `{"key" {:nested "value"}}`
- **Enums**: Keywords `:LOW`, `:MEDIUM`, `:HIGH`
- **One-ofs**: Use `(p/which-one-of map :field)` to check

### Conversion Functions
```clojure
;; To bytes
(p/proto-map->bytes proto-map)

;; To POJO (for Java interop)
(p/proto-map->proto proto-map)

;; To Clojure map (avoid in hot paths!)
(p/proto-map->clj-map proto-map)
```

## Notes 📌
- Using JAremko's Pronto fork from gitlibs (see deps.edn)
- Proto files come from protogen repo (cloned via Docker in generate script)
- Validation uses buf.validate annotations in proto files
- **Test data MUST use idiomatic Pronto with p/proto-map and p/p-> for performance**
- **The tool serves as a reference implementation for Pronto usage in the broader application**
- All tests should demonstrate performant Pronto patterns with hints
- Avoid EDN conversions in hot paths; work with proto-maps directly

## Testing Commands
```bash
# Generate protos
make proto

# Compile Java sources  
make compile

# Run tests
make test

# Clean build artifacts
make clean

# Validate a binary file (when CLI is implemented)
make validate-cmd FILE=path/to/cmd.bin
make validate-state FILE=path/to/state.bin
```

## Key Files
- `src/validate/validator.clj` - Core validation logic
- `test/validate/pronto_test_data.clj` - Idiomatic Pronto test data (reference implementation)
- `scripts/generate-protos.sh` - Proto generation with buf.validate support
- `Makefile` - Build automation
- `deps.edn` - Dependencies including Pronto fork