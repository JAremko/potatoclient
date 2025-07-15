# Orchestra to Malli Migration Guide

## Overview

This document outlines the migration from Orchestra's `defn-spec` to Malli's function schema system. The migration will preserve our current development/release build distinction while modernizing our runtime validation approach.

## Current State (Orchestra)

- **Version**: Orchestra 2021.01.01-1
- **Files affected**: 17 source files + 1 documentation file
- **Total functions**: ~151 `defn-spec` declarations
- **Instrumentation**: Enabled in development, disabled in release builds

## Target State (Malli)

- **Version**: Malli 0.19.1
- **Approach**: Function schema annotations with `m/=>` 
- **Instrumentation**: Via `malli.dev` in development, disabled in release builds
- **Benefits**: 
  - More active maintenance and development
  - Better error messages with `malli.dev.pretty`
  - Clj-kondo integration for static analysis
  - More flexible schema definitions
  - Better performance characteristics

## Migration Strategy

### Phase 1: Setup Infrastructure
1. Add Malli dependency to `deps.edn`
2. Create `potatoclient.specs` namespace for shared schemas
3. Update `main.clj` to support both Orchestra and Malli during transition
4. Set up Malli development instrumentation

### Phase 2: Gradual Migration
Migrate namespaces in dependency order to avoid breaking changes:
1. Start with leaf namespaces (no dependencies)
2. Move up the dependency tree
3. Keep both Orchestra and Malli active during migration

### Phase 3: Cleanup
1. Remove Orchestra dependency
2. Remove Orchestra imports
3. Update documentation
4. Update CI/CD configurations

## Migration Checklist

### Infrastructure Setup
- [ ] Add Malli dependency to `deps.edn`
- [ ] Create `src/potatoclient/specs.clj` for shared schemas
- [ ] Update `main.clj` to support dual instrumentation
- [ ] Add Malli dev configuration
- [ ] Update CLAUDE.md documentation

### File-by-File Migration Checklist

Each file migration involves:
1. Keep Orchestra import temporarily
2. Change `defn-spec` to regular `defn` or `defn-`
3. Add `m/=>` annotations after each function
4. Test that instrumentation still works
5. Remove Orchestra import when file is complete

#### Core Infrastructure (Migrate First)
- [ ] **src/potatoclient/specs.clj** (NEW FILE - create shared schemas)

#### Configuration & State (No Dependencies)
- [ ] **src/potatoclient/theme.clj** (11 functions)
  - [ ] `get-current-theme` (line 23)
  - [ ] `set-theme!` (line 40)
  - [ ] `initialize-theme!` (line 52)
  - [ ] `get-theme-name` (line 58)
  - [ ] `get-theme-i18n-key` (line 68)
  - [ ] `get-available-themes` (line 78)
  - [ ] `is-development-mode?` (line 87) - private
  - [ ] `log-theme` (line 98) - private
  - [ ] `key->icon` (line 106)
  - [ ] `preload-theme-icons!` (line 130)

- [ ] **src/potatoclient/i18n.clj** (5 functions)
  - [ ] `load-translation-file` (line 20)
  - [ ] `load-translations!` (line 33)
  - [ ] `reload-translations!` (line 47)
  - [ ] `tr` (line 56)
  - [ ] `init!` (line 70)

- [ ] **src/potatoclient/config.clj** (11 functions)
  - [ ] `get-config-dir` (line 18) - private
  - [ ] `get-config-file` (line 52) - private
  - [ ] `ensure-config-dir!` (line 57) - private
  - [ ] `load-config` (line 64)
  - [ ] `save-config!` (line 82)
  - [ ] `get-theme` (line 94)
  - [ ] `save-theme!` (line 99)
  - [ ] `get-domain` (line 105)
  - [ ] `save-domain!` (line 110)
  - [ ] `get-locale` (line 116)
  - [ ] `save-locale!` (line 121)
  - [ ] `update-config!` (line 127)
  - [ ] `get-config-location` (line 133)
  - [ ] `initialize!` (line 138)

- [ ] **src/potatoclient/state.clj** (17 functions)
  - [ ] `get-stream` (line 44)
  - [ ] `set-stream!` (line 50)
  - [ ] `clear-stream!` (line 58)
  - [ ] `all-streams` (line 64)
  - [ ] `get-domain` (line 70)
  - [ ] `set-domain!` (line 75)
  - [ ] `add-log-entry!` (line 92)
  - [ ] `flush-log-buffer!` (line 102)
  - [ ] `clear-logs!` (line 107)
  - [ ] `get-log-entries` (line 112)
  - [ ] `get-update-scheduled?` (line 117)
  - [ ] `set-update-scheduled!` (line 122)
  - [ ] `register-ui-element!` (line 128)
  - [ ] `get-ui-element` (line 136)
  - [ ] `unregister-ui-element!` (line 142)
  - [ ] `get-locale` (line 149)
  - [ ] `set-locale!` (line 154)
  - [ ] `current-state` (line 167)

#### Events (Minimal Dependencies)
- [ ] **src/potatoclient/events/stream.clj** (7 functions)
  - [ ] `format-window-event` (line 72)
  - [ ] `format-navigation-event` (line 143)
  - [ ] `handle-response-event` (line 171)
  - [ ] `handle-navigation-event` (line 187)
  - [ ] `handle-window-event` (line 200)
  - [ ] `stream-connected?` (line 214)
  - [ ] `all-streams-connected?` (line 219)

- [ ] **src/potatoclient/events/log.clj** (6 functions)
  - [ ] `format-log-entry` (line 38)
  - [ ] `add-log-entry!` (line 68)
  - [ ] `handle-log-event` (line 98)
  - [ ] `log-info` (line 112)
  - [ ] `log-error` (line 120)
  - [ ] `log-warning` (line 131)

#### Core Features (Medium Dependencies)
- [ ] **src/potatoclient/proto.clj** (17 functions)
  - [ ] `serialize-cmd` (line 42)
  - [ ] `deserialize-state` (line 64)
  - [ ] `cmd-ping` (line 93)
  - [ ] `cmd-noop` (line 101)
  - [ ] `cmd-frozen` (line 109)
  - [ ] `get-system-info` (line 118)
  - [ ] `get-camera-day` (line 123)
  - [ ] `get-camera-heat` (line 128)
  - [ ] `get-gps-info` (line 133)
  - [ ] `get-compass-info` (line 138)
  - [ ] `get-lrf-info` (line 143)
  - [ ] `get-time-info` (line 148)
  - [ ] `get-location` (line 154)
  - [ ] `cameras-available?` (line 164)
  - [ ] `valid-command?` (line 171)
  - [ ] `explain-invalid-command` (line 176)

- [ ] **src/potatoclient/log_writer.clj** (9 functions)
  - [ ] `get-log-filename` (line 31) - private
  - [ ] `ensure-logs-directory!` (line 37) - private
  - [ ] `create-log-writer!` (line 44) - private
  - [ ] `format-log-entry` (line 49) - private
  - [ ] `write-log-entry!` (line 59)
  - [ ] `start-logging!` (line 71)
  - [ ] `stop-logging!` (line 88)
  - [ ] `is-logging-enabled?` (line 99)

- [ ] **src/potatoclient/ipc.clj** (5 functions)
  - [ ] `start-stream` (line 64)
  - [ ] `stop-stream` (line 89)
  - [ ] `restart-stream` (line 106)
  - [ ] `send-command-to-stream` (line 117)
  - [ ] `broadcast-command` (line 130)

- [ ] **src/potatoclient/process.clj** (5 functions)
  - [ ] `start-stream-process` (line 182)
  - [ ] `send-command` (line 194)
  - [ ] `stop-stream` (line 217)
  - [ ] `cleanup-all-processes` (line 240)
  - [ ] `process-alive?` (line 253)

#### UI Components (High Dependencies)
- [ ] **src/potatoclient/ui/log_export.clj** (1 function)
  - [ ] `save-logs-dialog` (line 10)

- [ ] **src/potatoclient/ui/log_table.clj** (4 functions)
  - [ ] `get-type-color` (line 19) - private
  - [ ] `create-type-renderer` (line 49) - private
  - [ ] `show-raw-data` (line 63) - private
  - [ ] `create` (line 70)

- [ ] **src/potatoclient/ui/control_panel.clj** (5 functions)
  - [ ] `create-domain-field` (line 23) - private
  - [ ] `create-log-controls` (line 34) - private
  - [ ] `create-header-section` (line 52) - private
  - [ ] `create-log-section` (line 64) - private
  - [ ] `create` (line 72)

- [ ] **src/potatoclient/ui/main_frame.clj** (11 functions)
  - [ ] `create-language-action` (line 37) - private
  - [ ] `create-theme-action` (line 56) - private
  - [ ] `create-theme-menu` (line 75) - private
  - [ ] `create-language-menu` (line 84) - private
  - [ ] `create-stream-toggle-button` (line 93) - private
  - [ ] `create-menu-bar` (line 134) - private
  - [ ] `create-main-content` (line 151) - private
  - [ ] `add-window-close-handler!` (line 158) - private
  - [ ] `preserve-window-state` (line 172)
  - [ ] `restore-window-state!` (line 178)
  - [ ] `ensure-on-edt` (line 185) - private
  - [ ] `create-main-frame` (line 193)

#### Application Entry Points (Migrate Last)
- [ ] **src/potatoclient/core.clj** (6 functions)
  - [ ] `get-version` (line 17) - private
  - [ ] `get-build-type` (line 24) - private
  - [ ] `setup-shutdown-hook!` (line 32) - private
  - [ ] `initialize-application!` (line 45) - private
  - [ ] `log-startup!` (line 53) - private
  - [ ] `-main` (line 64)

- [ ] **src/potatoclient/main.clj** (4 functions)
  - [ ] `release-build?` (line 9) - private
  - [ ] `enable-instrumentation!` (line 16) - private
  - [ ] `enable-dev-mode!` (line 26) - private
  - [ ] `-main` (line 33)

### Post-Migration Cleanup
- [ ] Remove Orchestra dependency from `deps.edn`
- [ ] Update all documentation references
- [ ] Update CI/CD build scripts
- [ ] Run full test suite
- [ ] Performance testing (compare dev vs release builds)

## Function Migration Pattern

### Orchestra (Current)
```clojure
(ns potatoclient.example
  (:require [orchestra.core :refer [defn-spec]]
            [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::age pos-int?)

(defn-spec greet string?
  [name ::name
   age ::age]
  (str "Hello " name ", age " age))

(defn-spec ^:private helper boolean?
  [x any?]
  (some? x))
```

### Malli (Target)
```clojure
(ns potatoclient.example
  (:require [malli.core :as m]
            [potatoclient.specs :as specs]))

(defn greet
  [name age]
  (str "Hello " name ", age " age))

(m/=> greet [:=> [:cat ::specs/name ::specs/age] :string])

(defn- helper
  [x]
  (some? x))

(m/=> helper [:=> [:cat :any] :boolean])
```

## Instrumentation Setup Pattern

### Main.clj Updates
```clojure
;; Development mode with both Orchestra and Malli
(defn enable-instrumentation! []
  (if (release-build?)
    (println "Running RELEASE build - instrumentation disabled")
    (do
      (println "Running DEVELOPMENT build - enabling instrumentation...")
      ;; Orchestra (during migration)
      (require '[orchestra.spec.test :as st])
      ((resolve 'st/instrument))
      ;; Malli
      (require '[malli.dev :as dev])
      (require '[malli.dev.pretty :as pretty])
      ((resolve 'dev/start!) {:report ((resolve 'pretty/thrower))})
      (println "Instrumentation enabled."))))
```

## Testing Strategy

1. **Parallel Testing**: Keep both systems active during migration
2. **Namespace Testing**: Test each namespace after migration
3. **Integration Testing**: Full application test after each phase
4. **Performance Testing**: Compare dev/release build performance

## Rollback Plan

If issues arise:
1. Git revert to previous working state
2. Orchestra and Malli can coexist - no need for big-bang migration
3. Can migrate namespace by namespace

## Success Criteria

- [ ] All functions migrated from `defn-spec` to `defn` + `m/=>`
- [ ] Development instrumentation working with clear error messages
- [ ] Release builds have no instrumentation overhead
- [ ] All existing tests pass
- [ ] Documentation updated
- [ ] No performance regression

## Notes

- Malli schemas will be centralized in `potatoclient.specs` for reuse
- Use namespace-qualified keywords for all specs (e.g., `::specs/name`)
- Private functions will use `defn-` instead of `^:private` metadata
- Multi-arity functions will use `:function` schema as needed