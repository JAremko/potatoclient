# oneof_edn - Malli Schema for Protobuf-style Oneof Fields

## Overview

`oneof_edn` is a custom Malli schema type that validates maps where exactly one field must have a non-nil value. This pattern matches Protobuf's `oneof` fields and Pronto's EDN representation where inactive branches have `nil` values.

## Features

- ✅ **Validates exactly one non-nil field** - Core oneof behavior
- ✅ **Closed map semantics** - Rejects extra keys not in schema
- ✅ **Automatic generation** - Built-in generator that randomly selects fields
- ✅ **Nil compatibility** - Works with Pronto EDN (nil for inactive fields)
- ✅ **Full Malli integration** - Works with validation, generation, parsing, explaining
- ✅ **Composable** - Can be used as field values in larger map schemas
- ✅ **Property-tested** - 58 comprehensive tests including property-based testing

## Installation

The schema is automatically registered when you load the namespace:

```clojure
(require '[potatoclient.specs.oneof-edn :as oneof-edn]
         '[potatoclient.malli.registry :as registry])

;; Register in global registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))
```

## Usage

### Basic Usage

```clojure
;; Define a oneof schema
(def my-schema
  [:oneof_edn
   [:option-a :string]
   [:option-b :int]
   [:option-c :boolean]])

;; Valid values (exactly one field)
(m/validate my-schema {:option-a "hello"})        ; => true
(m/validate my-schema {:option-b 42})             ; => true
(m/validate my-schema {:option-c false})          ; => true

;; Valid with nil fields (Pronto style)
(m/validate my-schema {:option-a "hello" :option-b nil :option-c nil}) ; => true

;; Invalid values
(m/validate my-schema {})                         ; => false (no fields)
(m/validate my-schema {:option-a nil})            ; => false (all nil)
(m/validate my-schema {:option-a "x" :option-b 1}) ; => false (multiple non-nil)
(m/validate my-schema {:option-a "x" :extra 123}) ; => false (extra key)
```

### With Constraints

```clojure
(def constrained-schema
  [:oneof_edn
   [:small-number [:int {:min 0 :max 10}]]
   [:large-number [:int {:min 1000 :max 10000}]]
   [:short-text [:string {:min 1 :max 5}]]
   [:long-text [:string {:min 20 :max 100}]]])

;; Generator respects constraints
(mg/generate constrained-schema)
; => {:small-number 7} or {:large-number 5234} or {:short-text "hi"} etc.
```

### As Map Field

The most common usage is as a field within a larger map:

```clojure
(def message-schema
  [:map {:closed true}
   [:id :uuid]
   [:timestamp :int]
   [:payload [:oneof_edn
             [:text-message [:map [:content :string]]]
             [:image-message [:map [:url :string] [:size :int]]]
             [:video-message [:map [:url :string] [:duration :int]]]]]])

;; Valid message
(m/validate message-schema
  {:id #uuid "550e8400-e29b-41d4-a716-446655440000"
   :timestamp 1234567890
   :payload {:text-message {:content "Hello!"}}})
; => true
```

### Complex Nested Schemas

```clojure
(def complex-schema
  [:oneof_edn
   [:simple :keyword]
   [:nested [:map {:closed true}
            [:id :int]
            [:data [:oneof_edn
                   [:foo :string]
                   [:bar :boolean]]]]]
   [:collection [:vector [:oneof_edn
                         [:num :int]
                         [:text :string]]]]])

;; Examples
{:simple :my-keyword}
{:nested {:id 1 :data {:foo "test"}}}
{:collection [{:num 1} {:text "a"} {:num 2}]}
```

## Integration Patterns

### Pattern 1: Clean Nested Structure (Recommended for New Schemas)

```clojure
(def command-schema
  [:map {:closed true}
   [:protocol_version :int]
   [:client_type [:enum :ground :web]]
   ;; Oneof commands as a nested field
   [:command [:oneof_edn
             [:ping [:map [:id :int]]]
             [:echo [:map [:message :string]]]
             [:noop [:map]]]]])

;; Usage
{:protocol_version 1
 :client_type :ground
 :command {:ping {:id 123}}}  ; Clean, clear structure
```

### Pattern 2: Flat Protobuf Structure (For Compatibility)

When you need to maintain protobuf's flat structure where command fields are at root level:

```clojure
;; Validation schema (flat)
(def flat-validation-schema
  [:and
   [:map {:closed true}
    [:protocol_version :int]
    [:client_type [:enum :ground :web]]
    ;; Commands as optional root-level fields
    [:ping {:optional true} [:maybe [:map [:id :int]]]]
    [:echo {:optional true} [:maybe [:map [:message :string]]]]
    [:noop {:optional true} [:maybe [:map]]]]
   [:fn {:error/message "must have exactly one command"}
    (fn [m]
      (= 1 (count (filter some? [(m :ping) (m :echo) (m :noop)]))))]])

;; Generator using oneof internally
(defn generate-flat-command []
  (let [nested (mg/generate
                [:map {:closed true}
                 [:protocol_version :int]
                 [:client_type [:enum :ground :web]]
                 [:_cmd [:oneof_edn
                        [:ping [:map [:id :int]]]
                        [:echo [:map [:message :string]]]
                        [:noop [:map]]]]])]
    (merge (dissoc nested :_cmd) (:_cmd nested))))

;; Result maintains flat structure
{:protocol_version 1 :client_type :ground :ping {:id 123}}
```

## Error Messages

```clojure
(m/explain my-schema {})
; => {:errors [{:path []
;               :in []
;               :schema [:oneof_edn [:option-a :string] [:option-b :int]]
;               :value {}
;               :message "must have exactly one non-nil field"}]}

(m/explain my-schema {:option-a "x" :option-b 1})
; => {:errors [{:message "multiple non-nil fields: [:option-a :option-b]"}]}

(m/explain my-schema {:option-a "x" :extra 123})
; => {:errors [{:message "unexpected keys: [:extra]"}]}
```

## Generation

The built-in generator automatically:
- Randomly selects one field to populate
- Respects constraints (min/max, etc.)
- Ensures even distribution across alternatives

```clojure
;; Generate 10 samples
(repeatedly 10 #(mg/generate my-schema))
; => ({:option-b 42}
;     {:option-a "hello"}
;     {:option-c true}
;     {:option-b -5}
;     {:option-a "world"}
;     ...)
```

## Implementation Details

### How It Works

1. **Children Format**: Like `:altn`, uses `[key schema]` pairs
2. **Validation**: Checks exactly one non-nil field exists
3. **Closed Map**: No extra keys allowed
4. **Generation**: Uses `gen/elements` to pick field, then generates value

### Protocol Implementation

The schema implements Malli's core protocols:
- `IntoSchema` - Schema type definition
- `Schema` - Validation, parsing, transformation
- `-parent` returns IntoSchema (required for generators)

### Registry

The schema is registered with key `:oneof_edn` and can be used anywhere Malli schemas are accepted.

## Testing

Comprehensive test coverage includes:
- **Basic validation** - Single fields, nil handling, rejection cases
- **Generation** - Coverage, constraints, distribution
- **Complex schemas** - Nested maps, collections, deep nesting
- **Property-based** - 100+ generated test cases
- **Error handling** - Explanations and messages
- **Integration** - Map fields, multiple oneofs
- **Edge cases** - Single option, many options, nil semantics

Run tests:
```bash
clojure -M test-oneof-thorough.clj
# => 58 tests, all passing
```

## Performance

- Validation: O(n) where n is number of fields
- Generation: O(1) field selection + O(g) generation of selected field
- Memory: Minimal overhead, reuses child validators

## Limitations

1. **Direct :merge not supported** - Cannot merge oneof_edn directly with maps
   - Workaround: Use as field value in maps
2. **Humanized errors** - Shows "[unknown error]" in some cases
   - Raw errors via `m/explain` are detailed

## Examples in Production

### Command Messages (cmd/root)
```clojure
;; Protobuf: message Root { oneof command { Ping ping = 1; Echo echo = 2; ... } }
[:oneof_edn
 [:ping :cmd/ping]
 [:echo :cmd/echo]
 [:rotary :cmd/rotary]
 ;; ... 15 more command types
 ]
```

### API Responses
```clojure
[:oneof_edn
 [:success [:map [:data :any] [:timestamp :int]]]
 [:error [:map [:code :int] [:message :string]]]
 [:redirect [:map [:url :string] [:permanent :boolean]]]]
```

### Configuration
```clojure
[:oneof_edn
 [:file [:map [:path :string]]]
 [:url [:map [:endpoint :string] [:timeout :int]]]
 [:inline [:map [:content :string]]]]
```

## Best Practices

1. **Use as map field** for clean structure
2. **Keep alternatives simple** - Complex logic in nested schemas
3. **Name fields clearly** - Indicates the variant type
4. **Document alternatives** - Explain when each is used
5. **Test generation** - Ensure all variants are covered

## Migration Guide

From custom validators:
```clojure
;; Before (complex)
[:and
 [:map ...]
 [:fn validate-exactly-one-command]]

;; After (clean)
[:map {:closed true}
 [:metadata ...]
 [:command [:oneof_edn ...]]]
```

## Support

- Issues: Create in project repository
- Tests: See test-oneof-thorough.clj
- Examples: See test files in tools/validate/

## License

Part of the PotatoClient project.