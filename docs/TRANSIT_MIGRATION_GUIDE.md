# Transit Architecture Migration Guide

This guide helps you migrate from the direct protobuf WebSocket architecture to the new Transit-based architecture.

## Overview

The new Transit architecture completely isolates protobuf handling in Kotlin subprocesses, providing:
- Clean Clojure code without protobuf dependencies
- Single app-db atom for all state
- Automatic validation and rate limiting
- Better error handling and reconnection

## Quick Migration Example

### Before (Direct Protobuf)
```clojure
(require '[potatoclient.cmd.core :as cmd])

;; Initialize with callbacks
(cmd/init-websocket! "sych.local" 
                     (fn [error] (println "Error:" error))
                     (fn [state-bytes] (handle-state state-bytes)))

;; Send commands
(cmd/send-cmd-ping)
(cmd/send-rotary-axis-command {:azimuth azimuth-builder})
```

### After (Transit)
```clojure
(require '[potatoclient.transit.websocket-manager :as ws-manager])
(require '[potatoclient.transit.commands :as commands])
(require '[potatoclient.transit.app-db :as app-db])

;; Initialize (no callbacks needed)
(ws-manager/init! "sych.local")

;; Send commands (simpler API)
(commands/ping)
(commands/rotary-goto {:azimuth 45.0 :elevation 30.0})

;; Access state directly
(app-db/get-subsystem :gps)
```

## Step-by-Step Migration

### 1. Update Dependencies

Replace requires in your namespace:

```clojure
;; OLD
(:require [potatoclient.cmd.core :as cmd]
          [potatoclient.proto :as proto])

;; NEW
(:require [potatoclient.transit.websocket-manager :as ws-manager]
          [potatoclient.transit.commands :as commands]
          [potatoclient.transit.app-db :as app-db])
```

### 2. Remove Protobuf Imports

Remove all Java imports:
```clojure
;; REMOVE THESE
(:import (cmd JonSharedCmd$Root$Builder)
         (ser JonSharedData$JonGUIState))
```

### 3. Update Initialization

The new system doesn't need callbacks - state is automatically managed:

```clojure
;; OLD
(cmd/init-websocket! domain 
                     error-callback 
                     state-callback)

;; NEW
(ws-manager/init! domain)
```

### 4. Update Command Sending

Commands now use simple maps instead of protobuf builders:

```clojure
;; OLD - Complex builder pattern
(let [axis-msg (cmd/create-axis-message)]
  (.setAzimuth axis-msg azimuth-builder)
  (.setElevation axis-msg elevation-builder)
  (cmd/send-rotary-axis-command {:azimuth azimuth
                                  :elevation elevation}))

;; NEW - Simple map
(commands/rotary-goto {:azimuth 45.0 
                       :elevation 30.0})
```

### 5. Update State Access

State is now in a single app-db atom:

```clojure
;; OLD - Handle raw protobuf in callback
(defn state-callback [proto-bytes]
  (let [state (proto/deserialize-state proto-bytes)]
    (reset! my-state-atom state)))

;; NEW - Direct access
(def gps-data (app-db/get-subsystem :gps))
(def full-state (app-db/get-server-state))
```

### 6. Watch for State Changes

If you need to react to state changes:

```clojure
;; Add a watcher
(add-watch app-db/app-db ::my-watcher
  (fn [_ _ old-state new-state]
    (when (not= (:server-state old-state)
                (:server-state new-state))
      (handle-state-change (:server-state new-state)))))

;; Remove when done
(remove-watch app-db/app-db ::my-watcher)
```

## Command API Changes

### Basic Commands
```clojure
;; OLD                          NEW
(cmd/send-cmd-ping)         → (commands/ping)
(cmd/send-cmd-noop)         → (commands/noop)
(cmd/send-cmd-frozen)       → (commands/frozen)
```

### System Commands
```clojure
;; OLD - Using builders
;; Complex protobuf builder code...

;; NEW - Simple functions
(commands/set-localization "en")
(commands/set-recording true)
```

### GPS Commands
```clojure
;; NEW
(commands/set-gps-manual {:use-manual true
                          :latitude 51.5074
                          :longitude -0.1278
                          :altitude 35.0})
```

### Camera Commands
```clojure
;; NEW
(commands/day-camera-zoom 10.0)
(commands/day-camera-focus {:mode "auto"})
(commands/heat-camera-palette "white-hot")
```

### Rotary Platform
```clojure
;; NEW
(commands/rotary-goto {:azimuth 90.0 :elevation 45.0})
(commands/rotary-set-velocity {:azimuth-velocity 10.0 
                                :elevation-velocity 5.0})
(commands/rotary-stop)
```

## State Access Patterns

### Get Specific Subsystems
```clojure
(app-db/get-subsystem :gps)      ; GPS data
(app-db/get-subsystem :compass)  ; Compass data
(app-db/get-subsystem :lrf)      ; Laser range finder
(app-db/get-subsystem :rotary)   ; Rotary platform
```

### Get UI State
```clojure
(app-db/get-ui-state)     ; Theme, locale, etc.
(app-db/get-connection)   ; Connection status
(app-db/get-processes)    ; Subprocess statuses
```

### Update Local State
```clojure
(app-db/set-theme! :sol-dark)
(app-db/set-locale! :english)
(app-db/set-read-only-mode! true)
```

## Advanced Features

### Rate Limiting
```clojure
;; Set max state update rate (Hz)
(commands/set-state-rate-limit! 30)

;; Get rate limit metrics
(app-db/get-rate-limits)
```

### Validation
```clojure
;; Validation is automatic in dev/test
;; Access validation errors
(app-db/get-validation-errors)
```

### Process Management
```clojure
;; Get subprocess status
(app-db/get-process-state :cmd-proc)
(app-db/get-process-state :state-proc)
```

## Temporary Compatibility Layer

During migration, you can use the compatibility layer:

```clojure
(require '[potatoclient.transit.migration :as migration])

;; These will log warnings but work
(migration/init-websocket! domain error-fn state-fn)
(migration/send-cmd-ping)
```

## Common Issues

### Issue: State callbacks no longer called
**Solution**: Use app-db watchers or poll the state atom directly

### Issue: Protobuf classes not found
**Solution**: Remove all protobuf imports - they're no longer needed

### Issue: Commands not sending
**Solution**: Check that WebSocket manager is initialized and connected

### Issue: Complex command builders
**Solution**: Use the simple map-based API in commands namespace

## Benefits After Migration

1. **Cleaner Code**: No protobuf builders or imports
2. **Better State Management**: Single source of truth
3. **Automatic Features**: Rate limiting, validation, reconnection
4. **Better Testing**: Pure Clojure data structures
5. **Improved Performance**: Debouncing and rate limiting in Kotlin

## Next Steps

1. Start with non-critical namespaces
2. Use compatibility layer during transition
3. Remove old WebSocket manager references
4. Update tests to use new APIs
5. Remove compatibility layer when complete