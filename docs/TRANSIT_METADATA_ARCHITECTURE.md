# Transit Metadata Architecture for Protobuf Commands

## Overview

This document describes a new architecture for handling protobuf commands using Transit metadata instead of action-based routing. This approach solves the problem of nested messages with duplicate names while providing a more elegant and maintainable solution.

## The Problem

In deeply nested protobuf structures, the same message names appear in different contexts:

```protobuf
// Different "Start" messages in different contexts
message RotaryPlatform {
  message Root {
    oneof data {
      Start start = 1;    // cmd.JonSharedCmdRotaryPlatform$Start
      Stop stop = 2;
    }
  }
}

message VideoRecording {
  message Root {
    oneof data {
      Start start = 1;    // cmd.JonSharedCmdVideoRecording$Start (different class!)
      Stop stop = 2;
    }
  }
}
```

The current action-based approach requires:
- Complex action naming schemes ("rotaryplatform-start" vs "videorecording-start")
- Manual routing in Kotlin with large switch statements
- Maintaining a registry of action names

## The Solution: Metadata-Based Transit Handlers

Instead of action tags, we send the complete EDN structure with metadata specifying the exact protobuf type:

### Clojure Side

```clojure
;; Instead of this:
(send-command "rotaryplatform-goto" {:azimuth 180.0 :elevation 45.0})

;; We do this:
(send-command {:goto {:azimuth 180.0 :elevation 45.0}}
              [:rotary-platform :goto])

;; The function attaches metadata:
^{:proto-type "cmd.JonSharedCmdRotaryPlatform$Root"
  :proto-path [:rotary-platform :goto]}
{:goto {:azimuth 180.0 :elevation 45.0}}
```

### Transit Wire Format

Transit preserves metadata during serialization:

```edn
;; Transit representation (conceptual)
["^ " "^meta" ["^ " ":proto-type" "cmd.JonSharedCmdRotaryPlatform$Root"
                    ":proto-path" ["rotary-platform" "goto"]]
      ":goto" ["^ " ":azimuth" 180.0 ":elevation" 45.0]]
```

### Kotlin Side

The Transit handler reads the metadata and instantiates the correct protobuf class:

```kotlin
class ProtobufReadHandler : ReadHandler<Message, Map<*, *>> {
    override fun fromRep(rep: Map<*, *>): Message {
        val metadata = rep[TransitFactory.keyword("^meta")] as? Map<*, *>
        val protoType = metadata?.get(TransitFactory.keyword("proto-type")) as? String
        
        // Use reflection to instantiate the exact protobuf class
        val clazz = Class.forName(protoType)
        val builder = clazz.getMethod("newBuilder").invoke(null) as Message.Builder
        
        // Populate from the data
        populateBuilder(builder, data)
        return builder.build()
    }
}
```

## Benefits

### 1. No Name Collisions
Each command carries its full type information, eliminating ambiguity:
```clojure
;; Both can have "start" without collision
(send-command {:start {}} [:rotary-platform :start])
(send-command {:start {}} [:video-recording :start])
```

### 2. No Action Registry
- No need to maintain action-to-class mappings
- No large switch statements in Kotlin
- Adding new commands requires no registry updates

### 3. Type Safety with Validation
```clojure
(>defn send-command
  [command-map type-path]
  [map? vector? => map?]
  ;; Validate against the appropriate spec
  (let [spec (derive-spec-from-path type-path)]
    (when-not (m/validate spec command-map)
      (throw (ex-info "Validation failed" {...}))))
  ;; Attach metadata and send
  ...)
```

### 4. Natural Clojure Syntax
Commands look like data, not string-based actions:
```clojure
;; Natural nested structure
(send-command 
  {:day {:offsets {:set {:x-offset 10 :y-offset -5}}}}
  [:lrf-calib :day :offsets :set])

;; Instead of artificial flattening
(send-command "lrf-day-offsets-set" {:x-offset 10 :y-offset -5})
```

### 5. Automatic Proto Discovery
The type path can be derived from the proto structure:
```clojure
;; Proto structure naturally maps to paths
cmd.JonSharedCmd$Root/SetGpsManual → [:root :set-gps-manual]
cmd.JonSharedCmdRotaryPlatform$Root/Goto → [:rotary-platform :goto]
cmd.JonSharedCmdLrfCalib$Root/Day/Offsets/Set → [:lrf-calib :day :offsets :set]
```

## Implementation Strategy

### Phase 1: Core Infrastructure
1. Implement `send-command` function with metadata attachment
2. Create `ProtobufTransitHandler` with reflection-based building
3. Test with simple commands (ping, noop)

### Phase 2: Type Registry
1. Build registry mapping paths to protobuf classes
2. Could be generated from proto descriptors
3. Validate at compile time that classes exist

### Phase 3: Validation Integration
1. Use proto-explorer specs for validation
2. Derive spec keys from type paths
3. Guardrails integration for development-time checks

### Phase 4: Migration
1. Keep existing action-based commands working
2. Gradually migrate to metadata approach
3. Eventually remove action registry

## Example Usage

### Simple Commands
```clojure
;; Ping
(send-command {:ping {}} [:root :ping])

;; System commands
(send-command {:use-manual true
               :latitude 51.5
               :longitude -0.1
               :altitude 100.0}
              [:root :set-gps-manual])
```

### Nested Commands
```clojure
;; Rotary platform
(send-command {:goto {:azimuth 180.0 :elevation 45.0}}
              [:rotary-platform :goto])

;; Camera controls
(send-command {:zoom {:zoom 4.0}}
              [:heat-camera :zoom])

;; Complex nesting
(send-command {:day {:offsets {:set {:x-offset 10 :y-offset -5}}}}
              [:lrf-calib :day :offsets :set])
```

### Convenience Functions
```clojure
;; Still provide friendly APIs
(defn rotary-goto [azimuth elevation]
  (send-command {:goto {:azimuth azimuth :elevation elevation}}
                [:rotary-platform :goto]))

(defn heat-camera-zoom [zoom]
  (send-command {:zoom {:zoom zoom}}
                [:heat-camera :zoom]))
```

## Metadata Schema

The metadata attached to each command:

```clojure
{:proto-type "fully.qualified.ProtoClass$Name"  ; Java class name
 :proto-path [:category :subcategory :command]   ; Logical path
 :proto-spec :spec.name/command                  ; Optional: Malli spec
 :validated? true}                               ; Optional: Was validated
```

## Advantages Over Current Approach

| Current (Action-based) | New (Metadata-based) |
|------------------------|----------------------|
| String action names | Data with metadata |
| Manual routing | Automatic type resolution |
| Registry maintenance | No registry needed |
| Name collision issues | Full type paths |
| Flattened commands | Natural nesting |
| Switch statements | Transit handlers |

## Conclusion

This metadata-based approach leverages Transit's strengths to create a more maintainable and elegant command system. It eliminates the complexity of action routing while providing better type safety and validation.