# Adding Commands Guide

This guide explains how to add new commands to PotatoClient's command system.

## Overview

Commands flow through these layers:
1. **Protobuf Definition** - Define structure
2. **Code Generation** - Auto-generate handlers
3. **Clojure Usage** - Send commands from UI
4. **Testing** - Validate behavior

## Step 1: Define in Protobuf

Add your command to the appropriate `.proto` file:

```protobuf
// In jon_shared_cmd_rotary.proto
message SetSpeed {
    double speed = 1;
    Direction direction = 2;
}

// In jon_shared_cmd.proto (if adding new subsystem)
message NewSubsystem {
    oneof command {
        DoSomething do_something = 1;
        Configure configure = 2;
    }
}
```

## Step 2: Generate Code

```bash
# Generate protobuf classes
make proto

# Generate Transit handlers
cd tools/proto-explorer
bb generate-kotlin-handlers
```

This creates:
- Java protobuf classes in `cmd` package
- Kotlin handlers in `GeneratedCommandHandlers.kt`
- Automatic Transit ↔ Protobuf conversion

## Step 3: Use in Clojure

Commands automatically work with the generated code:

```clojure
(require '[potatoclient.transit.commands :as cmd])

;; Simple command
(cmd/send-command! {:new-subsystem {:do-something {}}})

;; With parameters (note keywords!)
(cmd/send-command! {:rotary {:set-speed {:speed 0.5
                                        :direction :clockwise}}})

;; All enum values become keywords
(cmd/send-command! {:camera {:set-palette {:palette :white-hot}}})
```

## Step 4: Add Validation (Optional)

For complex commands, add Malli specs:

```clojure
(ns potatoclient.specs.commands
  (:require [malli.core :as m]))

(def set-speed
  [:map
   [:speed [:and number? [:>= 0.0] [:<= 1.0]]]
   [:direction [:enum :clockwise :counter-clockwise]]])

;; Validate before sending
(defn send-set-speed! [speed direction]
  (let [command {:rotary {:set-speed {:speed speed
                                     :direction direction}}}]
    (when (m/validate set-speed (get-in command [:rotary :set-speed]))
      (cmd/send-command! command))))
```

## Step 5: Test Your Command

### Unit Test

```clojure
(deftest set-speed-command-test
  (testing "Set speed command structure"
    (let [command {:rotary {:set-speed {:speed 0.5
                                       :direction :clockwise}}}]
      (is (m/validate ::specs/command command))
      (is (= 0.5 (get-in command [:rotary :set-speed :speed])))
      (is (= :clockwise (get-in command [:rotary :set-speed :direction]))))))
```

### Integration Test

```clojure
(deftest set-speed-integration-test
  (with-test-system
    (testing "Speed command updates state"
      (cmd/send-command! {:rotary {:set-speed {:speed 0.7
                                              :direction :counter-clockwise}}})
      (wait-for-state [:rotary :current-speed] 0.7)
      (is (= :counter-clockwise (get-in @app-db [:rotary :direction]))))))
```

### Mock Video Stream Test

```bash
cd tools/mock-video-stream

# Add test scenario
cat >> resources/scenarios/set-speed.edn << 'EOF'
{:description "Test set speed command"
 :commands [{:rotary {:set-speed {:speed 0.5 :direction :clockwise}}}]
 :expected-state {:rotary {:current-speed 0.5
                          :direction :clockwise}}}
EOF

# Validate scenario
make validate
```

## Common Patterns

### Commands with Channels

Many commands target specific video channels:

```clojure
;; Always use keyword for channel
(cmd/send-command! {:cv {:start-track {:channel :heat  ; not "heat" or "HEAT"
                                      :target-id 42}}})
```

### Commands with Enums

All protobuf enums become keywords:

```clojure
;; Protobuf: enum FocusMode { AUTO = 0; MANUAL = 1; INFINITY = 2; }
;; Clojure usage:
(cmd/send-command! {:day-camera {:set-focus-mode {:mode :auto}}})     ; not "AUTO"
(cmd/send-command! {:day-camera {:set-focus-mode {:mode :manual}}})   ; not "MANUAL"
(cmd/send-command! {:day-camera {:set-focus-mode {:mode :infinity}}}) ; not "INFINITY"
```

### Nested Commands

Commands can be deeply nested:

```clojure
(cmd/send-command! 
  {:system {:configure {:video {:codec :h264
                               :bitrate 5000000
                               :keyframe-interval 30}}}})
```

## Keyword Conversion Rules

The system automatically converts protobuf enums to keywords:

| Protobuf | Keyword |
|----------|---------|
| `VIDEO_CHANNEL_HEAT` | `:heat` |
| `VIDEO_CHANNEL_DAY` | `:day` |
| `DIRECTION_CLOCKWISE` | `:clockwise` |
| `FOCUS_MODE_AUTO` | `:auto` |
| `LOCALIZATION_ENGLISH` | `:en` |
| `LOCALIZATION_UKRAINIAN` | `:uk` |

Rules:
1. Remove enum type prefix
2. Convert to lowercase
3. Replace underscores with hyphens
4. Special cases (ENGLISH → en, UKRAINIAN → uk)

## Troubleshooting

### Command Not Working?

1. **Check protobuf generation**:
   ```bash
   ls target/classes/cmd/  # Should see your command classes
   ```

2. **Verify handler generation**:
   ```bash
   grep -n "your-command" src/kotlin/potatoclient/kotlin/transit/GeneratedCommandHandlers.kt
   ```

3. **Enable debug logging**:
   ```clojure
   (log/set-level! :potatoclient.transit :debug)
   ```

4. **Check subprocess logs**:
   ```bash
   tail -f logs/command-subprocess-*.log
   ```

### Common Mistakes

❌ **Using strings instead of keywords**:
```clojure
;; Wrong
{:set-mode {:mode "auto"}}

;; Correct
{:set-mode {:mode :auto}}
```

❌ **Wrong nesting structure**:
```clojure
;; Wrong - flat structure
{:action :set-speed :speed 0.5}

;; Correct - nested structure
{:rotary {:set-speed {:speed 0.5}}}
```

❌ **Manual protobuf handling**:
```clojure
;; Wrong - don't build protobuf in Clojure
(let [builder (SomeProto$Builder.)]
  ...)

;; Correct - use Transit commands
(cmd/send-command! {:subsystem {:command {...}}})
```

## Best Practices

1. **Always use keywords** for enum-like values
2. **Match protobuf structure** exactly
3. **Add validation** for complex commands
4. **Write tests** for new commands
5. **Document** special parameters
6. **Use mock tool** for testing

## See Also

- [Command System Architecture](../architecture/command-system.md)
- [Transit Protocol](../architecture/transit-protocol.md)
- [Proto Explorer Tool](../tools/proto-explorer.md)