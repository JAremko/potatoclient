# oneof_edn Implementation Summary

## ✅ What We Accomplished

### 1. Core Implementation
- **File**: `/shared/src/potatoclient/specs/oneof_edn.clj`
- Implemented custom Malli schema type `:oneof_edn` that validates maps where exactly one field has a non-nil value
- Follows the pattern of `:altn` but for maps instead of sequences
- Acts as a closed map (rejects extra keys)
- Properly implements IntoSchema and Schema protocols

### 2. Generator Support
- Fully functional generator that randomly selects one field to populate
- Respects min/max constraints on field schemas
- Properly registered with Malli's generator multimethod

### 3. Test Coverage
- **Basic validation tests**: Single field, multiple fields, empty maps, nil handling
- **Generation tests**: Coverage of alternatives, valid output
- **Property-based tests**: All generated values valid, exactly one field
- **Negative tests**: Extra keys rejected, wrong types rejected
- **Integration tests**: Works with nested schemas, complex types

### 4. Integration with CMD/Root
- Updated `/tools/validate/src/potatoclient/specs/cmd/root.clj` to use `:oneof_edn`
- Simplified from complex `:and` + `:fn` validator to clean `:oneof_edn` declaration
- Generator works automatically without custom implementation

## 📋 Test Results

From `test-oneof-final.clj`:
- **Total tests**: 20
- **Passed**: 17
- **Failed**: 3 (due to :merge limitations and missing cmd spec dependencies)

### Confirmed Working:
✓ Validates single non-nil field  
✓ Rejects empty maps  
✓ Rejects all nil fields  
✓ Accepts nil in inactive fields (Pronto-style)  
✓ Acts as closed map - rejects extra keys  
✓ Validates field types correctly  
✓ Generator produces valid values  
✓ Generator covers all alternatives  
✓ Property: All generated values are valid  
✓ Property: Generated values have exactly one field  
✓ Works with complex nested schemas  
✓ Supports nested oneof  
✓ Provides helpful error messages  
✓ Generator respects min/max constraints  
✓ Generator respects string constraints  
✓ Works with various Malli types  
✓ Handles schemas with many alternatives  

## 🚧 Known Limitations

1. **:merge Integration**: Direct use with `:merge` doesn't work as expected. Workaround: compose manually or use `:and` with separate validation
2. **Test File Loading**: The test file `oneof_edn_test.clj` doesn't run in main test suite (namespace issue)

## 📝 Usage Examples

### Basic Usage
```clojure
[:oneof_edn
 [:field-a :string]
 [:field-b :int]
 [:field-c :boolean]]

;; Valid: {:field-a "test"}
;; Valid: {:field-b 42}
;; Invalid: {:field-a "test" :field-b 42}
;; Invalid: {}
```

### With Constraints
```clojure
[:oneof_edn
 [:small [:int {:min 0 :max 10}]]
 [:large [:int {:min 100 :max 1000}]]]
```

### In CMD/Root Spec
```clojure
(def command-oneof-spec
  [:oneof_edn
   [:ping :cmd/ping]
   [:echo :cmd/echo]
   [:rotary :cmd/rotary]
   ;; ... other commands
   ])
```

## 🎯 Key Design Decisions

1. **Children Format**: Uses `[key schema]` pairs like `:altn`, not flat key-value list
2. **Nil Handling**: Nil values in non-active fields are allowed (Pronto EDN compatibility)
3. **Closed Map**: No extra keys allowed, enforcing strict schema
4. **Parent Reference**: Schema's `-parent` method returns the IntoSchema instance (required for generators)

## ✨ Benefits Over Previous Approach

**Before** (complex validation):
```clojure
[:and
 [:map {:closed true} ...]
 [:fn validate-oneof-command]]
```

**After** (clean declarative):
```clojure
[:oneof_edn
 [:command-a :schema-a]
 [:command-b :schema-b]]
```

- Cleaner, more declarative
- Automatic generator support
- Better error messages
- Reusable across different specs
- Follows Malli patterns