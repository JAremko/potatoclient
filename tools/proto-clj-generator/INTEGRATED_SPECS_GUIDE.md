# Integrated Specs and Generators Guide

## Overview

We've successfully merged our Malli specs and test.check generators into single s-expressions, making them easier to maintain and ensuring consistency between validation and test data generation.

## Key Improvements

### 1. Integrated Specs with Generators

Instead of maintaining specs and generators separately, we now define them together:

```clojure
(def TypeReference
  "A protobuf type reference (e.g., '.com.example.Message')"
  [:and
   {:gen/gen (gen/fmap (fn [[pkg type-name]]
                         (str "." pkg "." type-name))
                       (gen/tuple 
                        (realistic-package-gen)
                        (realistic-type-gen)))}
   :string
   [:fn {:error/message "must start with dot"} 
    #(clojure.string/starts-with? % ".")]])
```

The `{:gen/gen ...}` property attaches a custom generator directly to the spec.

### 2. Realistic Data Generation

We moved from completely random strings to realistic protobuf patterns:

#### Before (Random):
```
Package: jyhaqrbiwfgbjmeleqpzfvzqm.tkmv.xsufwqajcideslgostclkqeydmu
Type: Jtihoulmrnrgymubuvzvcunvfrzwgj
Field: d_v_cjg_d_eqt_c_pb_m_md
```

#### After (Realistic):
```
Package: com.api.service.v1
Type: UserRequest
Field: created_at
```

### 3. Using Regal for Pattern Generation

While Regal can generate complex regex patterns, we found that for our use case, curated lists of realistic components work better:

```clojure
(def package-components
  ["com" "org" "io" "net" "api" "service" "core" "proto" 
   "grpc" "rpc" "model" "domain" "data" "types" "common"
   "client" "server" "internal" "external" "v1" "v2"])

(defn realistic-package-gen []
  (gen/fmap (fn [parts]
              (clojure.string/join "." parts))
            (gen/vector (gen/elements package-components) 2 4)))
```

This generates packages like:
- `com.api.service`
- `org.proto.v1`
- `internal.grpc.model`

### 4. Frequency-Based Generation

We use frequency to generate more common patterns:

```clojure
(def ScalarType
  [:map
   {:gen/gen (gen/fmap (fn [s] {:scalar s})
                       (gen/frequency 
                        [[5 (gen/return :string)]   ; Most common
                         [4 (gen/return :int32)]
                         [3 (gen/return :int64)]
                         [3 (gen/return :bool)]
                         [2 (gen/return :double)]
                         [2 (gen/return :bytes)]
                         [1 (gen/elements [:float :uint32 :uint64])]]))}  ; Less common
   [:scalar proto-scalar-types]])
```

## File Structure

1. **`specs_with_generators.clj`** - First version with Regal patterns (too random)
2. **`specs_with_generators_v2.clj`** - Refined version with realistic patterns
3. **`integrated_spec_test.clj`** - Tests showing data variety
4. **`realistic_spec_test.clj`** - Tests showing realistic proto generation

## Usage

```clojure
(require '[generator.specs-with-generators-v2 :as specs])
(require '[malli.generator :as mg])

;; Generate a complete descriptor set
(def descriptor (mg/generate specs/DescriptorSet {:registry specs/registry}))

;; Validate data
(specs/valid? specs/DescriptorSet descriptor)  ; => true

;; Generate specific types
(def message (mg/generate specs/MessageDef {:registry specs/registry}))
(def field (mg/generate specs/Field {:registry specs/registry}))
```

## Benefits

1. **Single Source of Truth**: Specs and generators are defined together
2. **Realistic Test Data**: Generated data looks like real protobuf definitions
3. **Better Test Coverage**: Property tests can explore more realistic scenarios
4. **Maintainability**: Changes to specs automatically update generators
5. **Validation**: All generated data is guaranteed to be valid according to specs

## Example Generated Proto

```proto
// File: api.proto
syntax = "proto3";

package com.api.service.v1;

import "common.proto";
import "types.proto";

enum StatusType {
  UNKNOWN = 0;
  SUCCESS = 1;
  ERROR_INVALID = 2;
  PENDING = 3;
}

message UserRequest {
  string user_id = 1;
  string email = 2;
  repeated Status status = 3;
  int64 created_at = 4;
  optional Config config = 5;
}

message UserResponse {
  User user = 1;
  StatusType status = 2;
  string message = 3;
  int64 timestamp = 4;
}
```

## Next Steps

1. Consider adding support for more protobuf features (oneofs, maps, etc.)
2. Add generators for service definitions
3. Consider using these specs for the actual production code validation
4. Create generators for the enriched specs if needed