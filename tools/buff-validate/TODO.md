# Buff-Validate Tool - TODO

## Current Status
The buff-validate tool is being developed to validate binary protobuf payloads using buf.validate constraints. The core validation functionality is working, but tests need to be fixed and the tool needs to use idiomatic Pronto throughout.

## Completed ✅
- [x] Core validator implementation with buf.validate
- [x] Support for both cmd (JonSharedCmd$Root) and state (JonSharedData$JonGUIState) message validation  
- [x] Auto-detection of message types (improved to use validation for disambiguation)
- [x] Makefile with proto generation and compilation targets
- [x] Proto generation script with Docker and buf support
- [x] Basic test harness setup
- [x] Fixed client_type enum references (must use JonSharedDataTypes$JonGuiDataClientType)
- [x] Created idiomatic Pronto test data file with performance best practices
- [x] Fixed all import statements in pronto_test_data.clj using proto-explorer
  - JonSharedDataRotary$JonGuiDataRotary (not JonGuiDataRotaryPlatform)
  - JonSharedDataDayCamGlassHeater$JonGuiDataDayCamGlassHeater
  - JonSharedDataTypes$JonGuiDataMeteo (not JonSharedDataMeteoInternal)
  - JonSharedDataLrf$JonGuiDataLrf (capitalization matters)
  - JonSharedDataRecOsd$JonGuiDataRecOsd (capitalization matters)
- [x] Fixed import statements in test_data.clj to match correct class names

## In Progress 🚧
- [ ] Fix field names throughout test_data.clj to match proto definitions
  - Some methods have different names (e.g., setCpuTemperature not setCpuTemp)
  - Need to verify all field setter names

## TODO 📝

### High Priority
1. **Fix all test imports and class references**
   - Use proto-explorer to get exact class names for all messages
   - Update pronto_test_data.clj with correct imports
   - Ensure all nested classes use correct $ notation

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
   - `buff-validate.pronto-test-data` → `buff_validate/pronto_test_data.clj`

2. **Required fields in protobuf messages**
   - State messages have many required fields that must all be present
   - Commands require valid client_type (not 0/UNSPECIFIED)

3. **Import issues**
   - Some generated classes have different names than expected
   - Need to verify exact class structure from generated Java files

## Proto-Explorer Usage Guide 🔍

Proto-Explorer is our essential tool for discovering protobuf message structures. Use it to:
1. Find exact Java class names for imports
2. Get Pronto EDN shapes for creating test data
3. Understand message field structure

### How to Use Proto-Explorer Effectively

#### Step 1: Search for Messages
```bash
# Find all root messages
make -C ../proto-explorer proto-search QUERY=root

# Find GPS-related messages  
make -C ../proto-explorer proto-search QUERY=gps

# Find command messages
make -C ../proto-explorer proto-search QUERY=JonSharedCmd

# List all messages in a package
make -C ../proto-explorer proto-list FILTER=ser
make -C ../proto-explorer proto-list FILTER=cmd
```

#### Step 2: Get Detailed Information
```bash
# Get exact class name and Pronto EDN structure
make -C ../proto-explorer proto-info QUERY='cmd.JonSharedCmd$Root'
make -C ../proto-explorer proto-info QUERY='ser.JonSharedData$JonGUIState'

# This gives you:
# - Exact Java class name for imports
# - Pronto EDN structure showing all fields
# - Field types and numbers
```

### Example: Finding Correct Import Names
```bash
# Problem: Need to import rotary platform message
$ make -C ../proto-explorer proto-search QUERY=rotary

Search results for: "rotary"
 1. JonGuiDataRotaryPlatform    ser.JonSharedDataRotary$JonGuiDataRotaryPlatform
    → Query: ser.JonSharedDataRotary$JonGuiDataRotaryPlatform

# Now get the exact structure:
$ make -C ../proto-explorer proto-info QUERY='ser.JonSharedDataRotary$JonGuiDataRotaryPlatform'

# This tells us the import should be:
# [ser JonSharedDataRotary$JonGuiDataRotaryPlatform]
```

## Test Data Resources 📊

### State-Explorer EDN Files
We have real Pronto EDN maps from state-explorer in `/home/jare/git/potatoclient/tools/state-explorer/output/`:
- These are actual state messages captured from the system
- Can be loaded directly as Pronto maps for testing
- Can be modified to create invalid messages for negative tests

#### Using State-Explorer EDN for Tests
```clojure
;; Load real state data
(def real-state-edn 
  (edn/read-string (slurp "../state-explorer/output/1754664759800.edn")))

;; Convert to Pronto proto-map
(def state-proto-map
  (p/clj-map->proto state-mapper 
    {:ser.JonSharedData$JonGUIState real-state-edn}))

;; Modify for negative tests (out of range values)
(def invalid-state-edn
  (assoc-in real-state-edn [:gps :latitude] 200.0)) ; Invalid: latitude > 90
```

### Creating Test Data with Proto-Explorer

1. **Get the Pronto EDN shape and schema** from proto-explorer:
```bash
# Note: Use 'make info' not 'make proto-info'
make -C ../proto-explorer info QUERY='cmd.JonSharedCmd$Root'

# Shows both EDN structure and Pronto schema:
# === PRONTO EDN STRUCTURE ===
# {:osd nil, :ping nil, :client_type :JON_GUI_DATA_CLIENT_TYPE_UNSPECIFIED, ...}
#
# === PRONTO SCHEMA ===
# {:client_type #{"JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK" ...},
#  :protocol_version int,
#  :session_id int,
#  :payload/ping cmd.JonSharedCmd$Ping, ...}
```

2. **Build test messages** using the shape:
```clojure
(def ping-cmd
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root
    :protocol_version 1
    :session_id 1000
    :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
    :ping (p/proto-map cmd-mapper cmd.JonSharedCmd$Ping)))
```

### How Proto-Explorer's Pronto Schema Helps Us (FULLY WORKING WITH NESTED MESSAGES!)

The **ENHANCED Pronto Schema feature** in proto-explorer now includes:
- **Deep nesting support** - correctly handles all nested message types
- **Smart class resolution** - automatically finds correct Java classes with fuzzy matching
- **Robust & foolproof** - automatic compilation and intelligent fallbacks

The tool shows:

1. **Field Types**: Exact Clojure/Pronto types for each field
   - Scalars: `int`, `float`, `boolean`, `string`
   - Enums: Sets of valid string values
   - Messages: Full Java class names for nested messages
   - Payloads: Marked with `payload/` prefix for one-of fields

2. **Enum Values**: Complete set of valid enum values
   ```
   :client_type #{"JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK" 
                  "JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV" ...}
   ```

3. **One-of Fields**: Clearly marked with `payload/` prefix
   - Shows all possible payload types for cmd root
   - Helps understand which fields are mutually exclusive

4. **Default Values**: EDN structure shows defaults
   - `nil` for unset message fields
   - `0` for numeric fields
   - `:UNSPECIFIED` for enums
   - `false` for booleans

5. **Validation Planning**: Schema helps identify:
   - Which fields need validation
   - What types to expect
   - Which enums have UNSPECIFIED that shouldn't be used
   - Required vs optional fields

Example usage for test data creation:
```bash
# Get complete schema for state root (FULLY WORKING!)
make -C ../proto-explorer info QUERY='ser.JonSharedData$JonGUIState'
# Shows all nested message types with correct Java class names:
# :gps ser.JonSharedDataGps$JonGuiDataGps
# :system ser.JonSharedDataSystem$JonGuiDataSystem
# :rotary ser.JonSharedDataRotary$JonGuiDataRotary
# etc.

# Get detailed info for ANY nested message (NEW!)
make -C ../proto-explorer info QUERY='ser.JonSharedDataGps$JonGuiDataGps'
# Shows complete EDN structure and Pronto schema with:
# - All fields with default values
# - Enum values (e.g., GPS fix types)
# - Field types (double, boolean, etc.)

# Get complete schema for cmd root
make -C ../proto-explorer info QUERY='cmd.JonSharedCmd$Root'
# Shows all payload types and enum values

# Use the schema to create valid test data with all required fields
# Use the EDN structure as a template for creating proto-maps
```

**Important Discoveries**: 
1. Proto-explorer now correctly handles **ALL nested messages** - you can query any deeply nested type
2. The Pronto schema shows exact Java class names for imports (e.g., `ser.JonSharedDataGps$JonGuiDataGps`)
3. We can directly copy these class names to fix our import statements
4. The tool has **smart class resolution** with fuzzy matching - it will find the right class even with slight variations
5. **Note**: Some class names may differ slightly from expectations:
   - Expected: `JonGuiDataRotaryPlatform` 
   - Actual: `JonGuiDataRotary`
   - Use proto-explorer to verify exact names!

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

### Reference Implementation Pattern
All test files should follow this pattern:
```clojure
;; 1. Define mappers once and reuse
(p/defmapper cmd-mapper [cmd.JonSharedCmd$Root])

;; 2. Use proto-map for creation with all fields
(defn create-message []
  (p/proto-map cmd-mapper cmd.JonSharedCmd$Root ...))

;; 3. Use p/p-> with hints for updates
(defn update-message [msg]
  (p/with-hints [(p/hint msg cmd.JonSharedCmd$Root cmd-mapper)]
    (p/p-> msg
           (assoc :field1 val1)
           (assoc :field2 val2))))

;; 4. Use p/p-> with hints for reads
(defn read-fields [msg]
  (p/with-hints [(p/hint msg cmd.JonSharedCmd$Root cmd-mapper)]
    {:field1 (p/p-> msg :field1)
     :nested (p/p-> msg :nested :field2)}))
```

## Core Pronto Concepts (from README)

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
- State-explorer output provides real-world test data in EDN format
- Proto-explorer is essential for finding correct class names and message structures
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
- `src/buff_validate/validator.clj` - Core validation logic
- `test/buff_validate/pronto_test_data.clj` - Idiomatic Pronto test data (reference implementation)
- `scripts/generate-protos.sh` - Proto generation with buf.validate support
- `Makefile` - Build automation
- `deps.edn` - Dependencies including Pronto fork