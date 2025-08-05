# Proto Explorer CLI Usage Examples

## Basic Commands

### Find specs by pattern (fuzzy matching)
```bash
# Find all specs containing "rotary"
bb find rotary

# Find all specs containing "camera"  
bb find camera

# Output (EDN format):
{:found 4
 :specs [{:spec :cmd.RotaryPlatform/rotate-to-gps
          :namespace "cmd.RotaryPlatform"
          :name "rotate-to-gps"}
         {:spec :cmd.RotaryPlatform/rotate-to-ndc
          :namespace "cmd.RotaryPlatform"  
          :name "rotate-to-ndc"}
         ...]}
```

### Get spec definition
```bash
# Get spec by full qualified name
bb spec :cmd.RotaryPlatform/set-azimuth-value

# Or without the colon
bb spec cmd.RotaryPlatform/set-azimuth-value

# Output:
{:spec :cmd.RotaryPlatform/set-azimuth-value
 :definition [:map 
              [:value [:and [:maybe :double] [:>= 0] [:< 360]]]
              [:direction [:and [:maybe :ser/jon-gui-data-rotary-direction] 
                           [:not [:enum [0]]]]]]}
```

### Generate example data
```bash
# Generate a single example
bb example :cmd.RotaryPlatform/set-azimuth-value

# Output:
{:spec :cmd.RotaryPlatform/set-azimuth-value
 :example {:value 245.7 :direction 1}}

# Generate multiple examples
bb examples :cmd.RotaryPlatform/set-elevation-value 5

# Output:
{:spec :cmd.RotaryPlatform/set-elevation-value
 :count 5
 :examples [{:value -45.2} 
            {:value 0.0} 
            {:value 67.8} 
            {:value -89.9} 
            {:value 45.0}]}
```

### List messages
```bash
# List all messages
bb list

# List messages in specific package
bb list cmd.RotaryPlatform

# Output:
{:total 10
 :packages ["cmd.RotaryPlatform"]
 :by-package {"cmd.RotaryPlatform" ["set-azimuth-value"
                                    "set-elevation-value"
                                    "rotate-to-gps"
                                    ...]}}
```

### Show statistics
```bash
bb stats

# Output:
{:total-specs 228
 :total-packages 14
 :by-package {"cmd" 4
              "cmd.CV" 10
              "cmd.RotaryPlatform" 15
              ...}}
```

## Batch Processing

Create a file `queries.edn` with multiple operations:

```clojure
[{:op :find :pattern "set-azimuth"}
 {:op :spec :spec :cmd.RotaryPlatform/set-azimuth-value}
 {:op :example :spec :cmd.RotaryPlatform/set-azimuth-value}
 {:op :example :spec :cmd.RotaryPlatform/set-elevation-value}]
```

Process the batch:
```bash
bb batch < queries.edn > results.edn
```

Output in `results.edn`:
```clojure
{:batch-results
 [{:op :find
   :pattern "set-azimuth"
   :results [:cmd.RotaryPlatform/set-azimuth-value]}
  {:op :spec
   :spec :cmd.RotaryPlatform/set-azimuth-value
   :definition [:map [:value [:and [:maybe :double] [:>= 0] [:< 360]]]...]}
  {:op :example
   :spec :cmd.RotaryPlatform/set-azimuth-value
   :example {:value 123.45 :direction 1}}
  {:op :example
   :spec :cmd.RotaryPlatform/set-elevation-value
   :example {:value -67.8}}]}
```

## Integration with Shell Scripts

### Generate test data for CI
```bash
#!/bin/bash
# generate-test-data.sh

# Generate examples for all rotary platform commands
for cmd in set-azimuth-value set-elevation-value set-velocity; do
  bb example cmd.RotaryPlatform/$cmd | \
    jq -r '.example' > test-data/$cmd.json
done
```

### Find and validate specs
```bash
#!/bin/bash
# validate-specs.sh

# Find all CV-related specs and generate examples
bb find CV | \
  grep -oE ':cmd\.CV/[a-z-]+' | \
  while read spec; do
    echo "Testing $spec..."
    bb example "$spec" || echo "Failed to generate example for $spec"
  done
```

### Use in Clojure tests
```clojure
;; In your test file
(require '[clojure.java.shell :as sh]
         '[clojure.edn :as edn])

(deftest test-rotary-commands
  ;; Generate constraint-aware test data
  (let [{:keys [out]} (sh/sh "bb" "example" ":cmd.RotaryPlatform/set-azimuth-value"
                            :dir "tools/proto-explorer")
        {:keys [example]} (edn/read-string out)]
    ;; Use the generated example in your tests
    (is (< 0 (:value example) 360))
    (is (not= 0 (:direction example)))))
```

## Why EDN Output?

The CLI outputs EDN (Extensible Data Notation) because:
1. **Native Clojure format** - Can be directly read by Clojure/ClojureScript
2. **Preserves keywords** - Spec names remain as qualified keywords
3. **Human readable** - Easy to inspect and debug
4. **Supports all types** - Handles nested maps, vectors, sets, etc.
5. **No JSON conversion** - Avoids lossy string/keyword conversions

## Performance Tips

1. **Batch operations** - Use batch mode for multiple queries
2. **Cache results** - Specs are pre-loaded, queries are fast
3. **Direct spec access** - Use exact spec names when known
4. **Package filtering** - Filter by package to reduce output size

## Error Handling

All errors are returned as EDN maps with `:error` key:

```clojure
{:error "Spec not found: :cmd/NonExistent"}
{:error "Failed to generate example: No generator for constraint"}
{:error "Usage: bb proto-explorer find <pattern>"}
```

This makes it easy to handle errors programmatically in scripts.