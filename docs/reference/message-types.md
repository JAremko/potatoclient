# Message Types Reference

Complete reference for all Transit message types used in PotatoClient IPC.

## Message Envelope

All messages follow a standard envelope format:

```clojure
{:msg-type :command|:state-update|:event|:response|:error
 :timestamp <unix-timestamp-ms>
 :correlation-id <uuid-string>
 :data <message-specific-data>}
```

## Command Messages

Commands flow from Clojure → Kotlin subprocesses.

### Structure

```clojure
{:msg-type :command
 :timestamp 1234567890123
 :correlation-id "550e8400-e29b-41d4-a716-446655440000"
 :data {:rotary {:goto-ndc {:channel :heat
                           :x 0.5
                           :y -0.25}}}}
```

### Command Categories

#### System Commands

```clojure
;; Ping - Health check
{:ping {}}

;; Recording control
{:system {:start-rec {}}}
{:system {:stop-rec {}}}

;; Localization
{:system {:localization {:accuracy :high}}}

;; Freeze state
{:frozen {:freeze true}}
```

#### Platform Control

```clojure
;; Rotary platform
{:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}}
{:rotary {:halt {}}}
{:rotary {:set-velocity {:vx 0.1 :vy 0.0}}}
{:rotary {:goto-angles {:yaw 45.0 :pitch 10.0}}}
```

#### Camera Control

```clojure
;; Heat camera
{:heat-camera {:set-zoom {:index 2}}}
{:heat-camera {:next-zoom-table-pos {}}}
{:heat-camera {:prev-zoom-table-pos {}}}
{:heat-camera {:calibrate {}}}

;; Day camera
{:day-camera {:set-zoom {:value 10.0}}}
{:day-camera {:set-focus-mode {:mode :auto}}}
{:day-camera {:set-exposure {:mode :manual :value 100}}}
```

#### Computer Vision

```clojure
;; Tracking
{:cv {:start-track-ndc {:channel :heat :x 0.5 :y 0.0}}}
{:cv {:stop-track {}}}
{:cv {:set-mode {:mode :detection}}}
```

#### Laser Range Finder

```clojure
;; LRF operations
{:lrf {:measure-once {}}}
{:lrf {:start-continuous {:rate 10}}}
{:lrf {:stop-continuous {}}}

;; LRF alignment
{:lrf-align {:start {:channel :heat}}}
{:lrf-align {:stop {}}}
```

## State Update Messages

State updates flow from Kotlin → Clojure.

### Structure

```clojure
{:msg-type :state-update
 :timestamp 1234567890123
 :data {:rotary {:current {:yaw 45.2
                          :pitch 10.1}
                :target {:yaw 45.0
                         :pitch 10.0}
                :velocity {:vx 0.1
                          :vy 0.0}
                :status :moving}
        :system {:recording false
                :battery 85
                :temperature 42.5}}}
```

### State Categories

#### System State

```clojure
{:system {:recording false
          :battery 85              ; Percentage
          :temperature 42.5        ; Celsius
          :gps {:lat 50.45
                :lon 30.52
                :alt 120.5
                :fix :3d}
          :time {:utc 1234567890123
                 :local 1234567890123}}}
```

#### Platform State

```clojure
{:rotary {:current {:yaw 45.2 :pitch 10.1}
          :target {:yaw 45.0 :pitch 10.0}
          :velocity {:vx 0.1 :vy 0.0}
          :acceleration {:ax 0.01 :ay 0.0}
          :status :moving|:stopped|:error
          :limits {:yaw-min -180.0
                  :yaw-max 180.0
                  :pitch-min -90.0
                  :pitch-max 90.0}}}
```

#### Camera State

```clojure
;; Heat camera
{:heat-camera {:zoom-index 2
               :zoom-table [1.0 2.0 4.0 8.0 16.0]
               :temperature {:min -20.0
                           :max 150.0
                           :avg 25.5}
               :status :ready}}

;; Day camera
{:day-camera {:zoom 10.0
              :zoom-range {:min 1.0 :max 30.0}
              :focus-mode :auto|:manual
              :focus-distance 100.0
              :exposure {:mode :auto
                        :value 100}
              :status :ready}}
```

#### CV State

```clojure
{:cv {:tracking false
      :mode :idle|:detection|:tracking
      :target {:x 0.5 :y 0.0 :confidence 0.95}
      :detections [{:class :person
                   :bbox {:x 0.4 :y 0.3 :w 0.2 :h 0.4}
                   :confidence 0.92}]}}
```

#### LRF State

```clojure
{:lrf {:range 1250.5           ; Meters
       :status :ready|:measuring|:error
       :mode :single|:continuous
       :last-measurement {:range 1250.5
                         :timestamp 1234567890123
                         :quality :good}}}
```

## Event Messages

Events notify about system occurrences.

### Structure

```clojure
{:msg-type :event
 :timestamp 1234567890123
 :data {:event-type :video-connected
        :details {:stream :heat
                  :resolution {:width 900 :height 720}
                  :fps 30}}}
```

### Event Types

#### Connection Events

```clojure
;; Video stream events
{:event-type :video-connected
 :details {:stream :heat|:day
           :resolution {:width 1920 :height 1080}
           :fps 30}}

{:event-type :video-disconnected
 :details {:stream :heat|:day
           :reason "Connection timeout"}}

;; Command server events
{:event-type :server-connected
 :details {:url "wss://server:8443"}}

{:event-type :server-disconnected
 :details {:reason "Authentication failed"}}
```

#### System Events

```clojure
;; Recording events
{:event-type :recording-started
 :details {:filename "rec_20240104_120000.mp4"}}

{:event-type :recording-stopped
 :details {:filename "rec_20240104_120000.mp4"
           :duration 3600000
           :size 1073741824}}

;; Error events
{:event-type :error
 :details {:component :rotary
           :error "Motor overheating"
           :severity :warning|:error|:critical}}
```

#### User Interface Events

```clojure
;; Gesture events
{:event-type :gesture
 :details {:type :tap|:double-tap|:pan|:swipe
           :position {:x 500 :y 300}
           :video-stream :heat|:day}}

;; UI state changes
{:event-type :ui-state-changed
 :details {:component :video-panel
           :state {:heat-visible true
                   :day-visible true
                   :side-by-side false}}}
```

## Response Messages

Responses to commands.

### Success Response

```clojure
{:msg-type :response
 :timestamp 1234567890123
 :correlation-id "550e8400-e29b-41d4-a716-446655440000"
 :data {:status :success
        :command {:rotary {:goto-ndc {:x 0.5 :y -0.5}}}
        :result {:estimated-time 2500}}}
```

### Error Response

```clojure
{:msg-type :error
 :timestamp 1234567890123
 :correlation-id "550e8400-e29b-41d4-a716-446655440000"
 :data {:status :error
        :command {:rotary {:goto-ndc {:x 2.0 :y 0.0}}}
        :error {:code :invalid-parameters
                :message "NDC coordinates must be in range [-1, 1]"
                :details {:x 2.0}}}}
```

## Special Message Types

### Subprocess Communication

#### Initialization

```clojure
;; Subprocess ready
{:msg-type :subprocess-ready
 :data {:process :command|:state|:video-heat|:video-day
        :pid 12345
        :version "1.0.0"}}
```

#### Health Check

```clojure
;; Heartbeat
{:msg-type :heartbeat
 :data {:process :command
        :uptime 3600000
        :memory {:used 256 :total 512}
        :queued-messages 0}}
```

## Message Flow Patterns

### Command-Response Pattern

```
Clojure                    Kotlin
   |                         |
   |---- Command -------->   |
   |                         |
   |<--- Response -------    |
   |                         |
```

### State Streaming Pattern

```
Clojure                    Kotlin
   |                         |
   |<-- State Update ----    |
   |<-- State Update ----    |
   |<-- State Update ----    |
   |      (continuous)       |
```

### Event Notification Pattern

```
Clojure                    Kotlin
   |                         |
   |<---- Event --------     |
   |   (async)              |
```

## Validation

All messages are validated against Malli specs:

```clojure
(require '[malli.core :as m])

;; Command validation
(m/validate ::msg/command-message
  {:msg-type :command
   :timestamp 1234567890123
   :data {:ping {}}})

;; State validation
(m/validate ::msg/state-update
  {:msg-type :state-update
   :timestamp 1234567890123
   :data {:system {:recording false}}})
```

## Error Codes

Standard error codes used in error responses:

- `:invalid-parameters` - Command parameters failed validation
- `:not-implemented` - Command not yet implemented
- `:hardware-error` - Hardware communication failure
- `:timeout` - Operation timed out
- `:permission-denied` - Insufficient permissions
- `:resource-busy` - Resource in use
- `:unknown-command` - Command type not recognized

## Best Practices

1. **Always include correlation IDs** for command tracking
2. **Use keywords for enums** never strings
3. **Validate before sending** to avoid errors
4. **Handle all message types** in receivers
5. **Log unusual patterns** for debugging

## See Also

- [Transit Protocol](../architecture/transit-protocol.md)
- [Command System](../architecture/command-system.md)
- [Transit Test Generator](../tools/transit-test-generator.md)