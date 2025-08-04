# Transit Quick Reference

Quick reference for Transit protocol usage in PotatoClient.

## Common Commands

### Platform Control

```clojure
;; Goto NDC position
{:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}}

;; Halt all movement
{:rotary {:halt {}}}

;; Set velocity
{:rotary {:set-velocity {:vx 0.1 :vy 0.0}}}

;; Goto angles
{:rotary {:goto-angles {:yaw 45.0 :pitch 10.0}}}
```

### Camera Control

```clojure
;; Heat camera zoom
{:heat-camera {:next-zoom-table-pos {}}}
{:heat-camera {:prev-zoom-table-pos {}}}
{:heat-camera {:set-zoom {:index 2}}}

;; Day camera
{:day-camera {:set-zoom {:value 10.0}}}
{:day-camera {:set-focus-mode {:mode :auto}}}
{:day-camera {:set-exposure {:mode :manual :value 100}}}
```

### System Commands

```clojure
;; Recording
{:system {:start-rec {}}}
{:system {:stop-rec {}}}

;; Health check
{:ping {}}

;; Freeze state
{:frozen {:freeze true}}
```

### Computer Vision

```clojure
;; Start tracking
{:cv {:start-track-ndc {:channel :heat :x 0.5 :y 0.0}}}

;; Stop tracking
{:cv {:stop-track {}}}

;; Set mode
{:cv {:set-mode {:mode :detection}}}
```

### LRF Commands

```clojure
;; Single measurement
{:lrf {:measure-once {}}}

;; Continuous mode
{:lrf {:start-continuous {:rate 10}}}
{:lrf {:stop-continuous {}}}
```

## Message Patterns

### Command with Response

```clojure
;; Send
(cmd/send-command! 
  {:rotary {:goto-ndc {:channel :heat :x 0.5 :y 0.0}}})

;; Response
{:msg-type :response
 :correlation-id "..."
 :status :success
 :result {:estimated-time 2500}}
```

### State Updates

```clojure
;; Received state
{:msg-type :state-update
 :data {:rotary {:current {:yaw 45.2 :pitch 10.1}
                :target {:yaw 45.0 :pitch 10.0}
                :status :moving}
        :system {:recording true
                :battery 85}}}
```

### Events

```clojure
;; Connection event
{:msg-type :event
 :event-type :video-connected
 :details {:stream :heat
           :resolution {:width 900 :height 720}}}

;; Error event
{:msg-type :event
 :event-type :error
 :details {:component :rotary
           :error "Motor overheating"}}
```

## Keyword Mappings

### Channels
- `:heat` - Thermal camera
- `:day` - Visible light camera
- `:ir` - Infrared camera

### Status Values
- `:ready` - Ready for commands
- `:moving` - Currently in motion
- `:stopped` - Motion complete
- `:error` - Error state

### Modes
- `:auto` - Automatic mode
- `:manual` - Manual control
- `:idle` - No activity
- `:detection` - Object detection
- `:tracking` - Active tracking

## Quick Debug

### Enable Logging

```clojure
(require '[potatoclient.transit.debug :as debug])
(debug/enable-logging!)
```

### Inspect Messages

```clojure
;; Log raw transit
(debug/log-message some-data)

;; Pretty print
(debug/pp-transit some-data)
```

### Test Commands

```clojure
;; In REPL
(require '[potatoclient.transit.commands :as cmd])

;; Simple test
(cmd/send-command! {:ping {}})

;; Check state
@potatoclient.state/app-db
```

## Common Patterns

### Conditional Commands

```clojure
;; Only move if not recording
(when-not (get-in @state/app-db [:system :recording])
  (cmd/send-command! 
    {:rotary {:goto-ndc {:channel :heat :x 0.5 :y 0.0}}}))
```

### Command Sequences

```clojure
;; Stop, then start recording
(do
  (cmd/send-command! {:system {:stop-rec {}}})
  (Thread/sleep 100)
  (cmd/send-command! {:system {:start-rec {}}}))
```

### State Monitoring

```clojure
;; Watch for state changes
(add-watch state/app-db :my-watcher
  (fn [_ _ old new]
    (when (not= (:rotary old) (:rotary new))
      (println "Rotary state changed:" (:rotary new)))))
```

## Error Handling

### Command Errors

```clojure
;; Invalid parameters
{:msg-type :error
 :error {:code :invalid-parameters
         :message "NDC coordinates must be in range [-1, 1]"}}

;; Hardware error
{:msg-type :error
 :error {:code :hardware-error
         :message "Camera not responding"}}
```

### Common Error Codes
- `:invalid-parameters` - Bad input
- `:not-implemented` - Feature not ready
- `:hardware-error` - Device issue
- `:timeout` - Operation timeout
- `:permission-denied` - Not authorized

## Performance Tips

### Batch Commands

```clojure
;; Don't do this
(doseq [i (range 10)]
  (cmd/send-command! {:heat-camera {:set-zoom {:index i}}}))

;; Do this (if supported)
(cmd/send-command! 
  {:batch [{:heat-camera {:set-zoom {:index 2}}}
           {:day-camera {:set-zoom {:value 10.0}}}]})
```

### Throttle Updates

```clojure
;; Limit pan updates
(def last-pan (atom 0))

(defn send-pan-command [x y]
  (let [now (System/currentTimeMillis)]
    (when (> (- now @last-pan) 100) ; 100ms throttle
      (reset! last-pan now)
      (cmd/send-command! 
        {:rotary {:goto-ndc {:channel :heat :x x :y y}}}))))
```

## Subprocess Commands

### For Testing

```clojure
;; Direct subprocess communication
(require '[potatoclient.transit.core :as transit])

;; Send to specific subprocess
(transit/send-to-subprocess :command {:ping {}})

;; Read subprocess response
(transit/read-from-subprocess :command)
```

## Useful REPL Commands

```clojure
;; Import everything needed
(require '[potatoclient.transit.commands :as cmd]
         '[potatoclient.state :as state]
         '[potatoclient.transit.debug :as debug])

;; Quick state check
(keys @state/app-db)

;; Pretty print state section
(clojure.pprint/pprint (:rotary @state/app-db))

;; Send test command
(cmd/send-command! {:ping {}})

;; Clear state (dev only!)
(reset! state/app-db {})
```

## See Also

- [Transit Protocol](../architecture/transit-protocol.md) - Full protocol details
- [Message Types](./message-types.md) - Complete message reference
- [Command System](../architecture/command-system.md) - Command architecture