# Proto Explorer Documentation

Proto Explorer is a Babashka-based tool that generates Malli specifications from Protocol Buffer definitions, providing type-safe validation and test data generation for PotatoClient.

## Overview

Proto Explorer converts protobuf definitions into Clojure-friendly Malli schemas with:
- Complete buf.validate constraint support
- Automatic kebab-case conversion
- Constraint-aware data generation
- Fast CLI for spec queries
- Keyword tree generation for static code generation

## Architecture

```
.proto files → protogen → .json descriptors → Proto Explorer → Malli specs
                                                    ↓
                                            Keyword trees → Kotlin handlers
```

## Installation

Proto Explorer is included in the PotatoClient repository:

```bash
cd tools/proto-explorer

# No installation needed - uses Babashka
bb --version  # Verify Babashka is installed
```

## Usage

### Generating Specs

After protobuf changes, regenerate specs:

```bash
# Generate protobuf classes and JSON descriptors
make proto

# Generate Malli specs from JSON
make generate-specs

# Generate keyword trees for static code generation
bb generate-keyword-tree-cmd    # Command tree
bb generate-keyword-tree-state  # State tree

# Generate Kotlin handlers from trees
bb generate-kotlin-handlers.clj
```

### CLI Commands

Proto Explorer provides a fast CLI for working with specs:

#### Find Specs

Search for specs by pattern (fuzzy matching supported):

```bash
# Find all rotary-related specs
bb find rotary

# Fuzzy search works too
bb find setvel  # Finds set-velocity

# Case insensitive
bb find GOTO    # Finds goto-ndc
```

#### Get Spec Definition

View a specific spec (requires full namespace):

```bash
bb spec :potatoclient.specs.cmd.RotaryPlatform/set-velocity

# Output:
[:map
 [:velocity [:and [:maybe :double] [:not [:enum [0.0]]]]]]
```

#### Generate Examples

Generate constraint-aware test data:

```bash
# Single example
bb example :potatoclient.specs.cmd.RotaryPlatform/set-azimuth-value

# Output:
{:spec :potatoclient.specs.cmd.RotaryPlatform/set-azimuth-value
 :example {:value 245.7 :direction 1}}

# Multiple examples
bb examples :potatoclient.specs.cmd.RotaryPlatform/set-elevation-value 5
```

#### View Statistics

```bash
bb stats

# Output:
Total specs: 127
By namespace:
  potatoclient.specs.cmd: 45
  potatoclient.specs.ser: 82
```

### Java Reflection Features

For detailed protobuf class information (requires uberjar):

```bash
# Build uberjar first (one time)
make uberjar

# Get Java class info
bb java-class Root

# Get field mappings
bb java-fields SetVelocity

# Get builder methods
bb java-builder GotoNdc
```

## Generated Specs

### Location

Specs are generated to:
```
shared/specs/protobuf/
├── cmd.cljc                 # Root command specs
├── cmd/
│   ├── RotaryPlatform.cljc  # Rotary commands
│   ├── Cv.cljc             # CV commands
│   └── ...
└── ser.cljc                # Data type specs
```

### Using in Code

```clojure
;; Require generated specs
(require '[potatoclient.specs.cmd :as cmd-specs])
(require '[potatoclient.specs.cmd.RotaryPlatform :as rotary])

;; Validate commands
(m/validate cmd-specs/Root 
            {:protocol-version 1
             :cmd {:rotary {:set-velocity {:velocity 0.5}}}})

;; Get validation errors
(m/explain rotary/set-azimuth-value {:value 400})
;; => {:errors [{:path [:value], :in [:value], 
;;               :schema [:< 360], :value 400}]}
```

## Constraint Support

Proto Explorer extracts all buf.validate constraints:

### Numeric Constraints

```protobuf
message SetAzimuthValue {
    double value = 1 [(buf.validate.field).double = {
        gte: 0.0,
        lt: 360.0
    }];
}
```

Becomes:
```clojure
[:map
 [:value [:and [:maybe :double] [:>= 0.0] [:< 360.0]]]]
```

### Enum Constraints

```protobuf
enum Direction {
    DIRECTION_INVALID = 0;
    DIRECTION_CLOCKWISE = 1;
    DIRECTION_COUNTER_CLOCKWISE = 2;
}

message SetSpeed {
    Direction direction = 1 [(buf.validate.field).enum = {
        not_in: [0]  // Cannot be INVALID
    }];
}
```

Becomes:
```clojure
[:direction [:and [:maybe ::Direction] [:not [:enum [0]]]]]
```

### String Constraints

```protobuf
string name = 1 [(buf.validate.field).string = {
    min_len: 1,
    max_len: 100,
    pattern: "^[a-zA-Z0-9_-]+$"
}];
```

Becomes:
```clojure
[:name [:and :string 
        [:fn #(>= (count %) 1)]
        [:fn #(<= (count %) 100)]
        [:re #"^[a-zA-Z0-9_-]+$"]]]
```

## Keyword Trees

Proto Explorer generates keyword trees for static code generation:

### Command Tree Structure

```clojure
;; proto_keyword_tree_cmd.clj
{:ping {}
 :rotary {:goto-ndc {:x :double
                     :y :double}
          :set-velocity {:velocity :double}}
 :cv {:start-track-ndc {:channel ::VideoChannel
                       :x :double
                       :y :double}}}
```

### Using Trees

Trees are used to generate static Kotlin handlers:

```bash
# Generate trees
bb generate-keyword-tree-cmd
bb generate-keyword-tree-state

# Generate Kotlin code
bb generate-kotlin-handlers.clj

# Output: src/kotlin/.../GeneratedCommandHandlers.kt
#         src/kotlin/.../GeneratedStateHandlers.kt
```

## Property-Based Testing

Use generated specs for property tests:

```clojure
(require '[clojure.test.check.generators :as gen])
(require '[clojure.test.check.properties :as prop])

(def valid-azimuth-commands
  (prop/for-all [cmd (mg/generator rotary/set-azimuth-value)]
    (and (number? (:value cmd))
         (>= (:value cmd) 0)
         (< (:value cmd) 360))))

(tc/quick-check 100 valid-azimuth-commands)
```

## Development Workflow

1. **Modify .proto files** in protogen repository
2. **Generate classes**: `make proto`
3. **Generate specs**: `make generate-specs`
4. **Generate trees**: `bb generate-keyword-tree-*`
5. **Generate handlers**: `bb generate-kotlin-handlers.clj`
6. **Use in code**: Require and validate

## Troubleshooting

### Missing Specs

If specs are missing after generation:

```bash
# Check JSON descriptors exist
ls resources/protobuf-json/*.json

# Regenerate from scratch
make clean
make proto
make generate-specs
```

### Invalid Examples

If generated examples fail validation:

```bash
# Debug specific spec
bb debug-spec :potatoclient.specs.cmd/Root

# Check constraints
bb constraints :potatoclient.specs.cmd.RotaryPlatform/set-velocity
```

### Performance

For large spec operations, use the uberjar:

```bash
# Faster for bulk operations
java -jar target/proto-explorer.jar find-all
```

## Best Practices

1. **Regenerate after proto changes** - Keep specs in sync
2. **Use constraints** - Let buf.validate enforce rules
3. **Test with examples** - Verify generated data
4. **Commit generated specs** - They're part of the codebase
5. **Don't edit generated files** - Changes will be lost

## See Also

- [Protobuf Command System](../architecture/command-system.md)
- [Adding Commands Guide](../guides/adding-commands.md)
- [buf.validate Documentation](https://buf.build/docs/bsr/module/buf/protovalidate)