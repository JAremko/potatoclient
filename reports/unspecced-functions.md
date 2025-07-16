# Unspecced Functions Report

*Generated: 2025-07-16 16:16:36*

## Summary

![Unspecced Functions](https://img.shields.io/badge/Unspecced%20Functions-83-orange)

⚠️ Found **83** functions without Malli specs across **18** namespaces.

## Unspecced Functions by Namespace

### potatoclient.config

- `get-config-location`
- `get-domain`
- `get-locale`
- `get-theme`
- `initialize!`
- `load-config`
- `save-config!`
- `save-domain!`
- `save-locale!`
- `save-theme!`
- `update-config!`

### potatoclient.core

- `-main`

### potatoclient.events.stream

- `all-streams-connected?`
- `format-navigation-event`
- `format-window-event`
- `handle-navigation-event`
- `handle-response-event`
- `handle-window-event`
- `stream-connected?`

### potatoclient.i18n

- `init!`
- `load-translation-file`
- `load-translations!`
- `reload-translations!`
- `tr`

### potatoclient.ipc

- `broadcast-command`
- `restart-stream`
- `send-command-to-stream`
- `start-stream`
- `stop-stream`

### potatoclient.logging

- `init!`
- `shutdown!`

### potatoclient.main

- `-main`

### potatoclient.process

- `cleanup-all-processes`
- `process-alive?`
- `send-command`
- `start-stream-process`
- `stop-stream`

### potatoclient.proto

- `->ProtoMapper_potatoclient_proto_proto-mapper`
- `cameras-available?`
- `cmd-frozen`
- `cmd-noop`
- `cmd-ping`
- `deserialize-state`
- `explain-invalid-command`
- `get-camera-day`
- `get-camera-heat`
- `get-compass-info`
- `get-gps-info`
- `get-location`
- `get-lrf-info`
- `get-system-info`
- `get-time-info`
- `map->ProtoMapper_potatoclient_proto_proto-mapper`
- `serialize-cmd`
- `valid-command?`

### potatoclient.reports

- `generate-all-reports!`
- `generate-unspecced-functions-report!`

### potatoclient.runtime

- `release-build?`

### potatoclient.state

- `current-state`

### potatoclient.state.config

- `get-config`
- `get-domain`
- `get-locale`
- `set-domain!`
- `set-locale!`

### potatoclient.state.streams

- `all-streams`
- `clear-stream!`
- `get-stream`
- `set-stream!`

### potatoclient.state.ui

- `all-ui-elements`
- `get-ui-element`
- `register-ui-element!`

### potatoclient.theme

- `get-available-themes`
- `get-current-theme`
- `get-theme-i18n-key`
- `get-theme-name`
- `initialize-theme!`
- `key->icon`
- `preload-theme-icons!`
- `set-theme!`

### potatoclient.ui.control-panel

- `create`

### potatoclient.ui.main-frame

- `create-main-frame`
- `preserve-window-state`
- `restore-window-state!`


## Statistics

| Namespace | Count |
| --- | --- |
| `potatoclient.proto` | 18 |
| `potatoclient.config` | 11 |
| `potatoclient.theme` | 8 |
| `potatoclient.events.stream` | 7 |
| `potatoclient.state.config` | 5 |
| `potatoclient.i18n` | 5 |
| `potatoclient.ipc` | 5 |
| `potatoclient.process` | 5 |
| `potatoclient.state.streams` | 4 |
| `potatoclient.state.ui` | 3 |
| `potatoclient.ui.main-frame` | 3 |
| `potatoclient.reports` | 2 |
| `potatoclient.logging` | 2 |
| `potatoclient.state` | 1 |
| `potatoclient.main` | 1 |
| `potatoclient.runtime` | 1 |
| `potatoclient.core` | 1 |
| `potatoclient.ui.control-panel` | 1 |

## Next Steps

1. Add Malli specs for the unspecced functions to `src/potatoclient/instrumentation.clj`
2. Follow the pattern: `(m/=> namespace/function [:=> [:cat ...args...] return-type])`
3. Run `(potatoclient.reports/generate-unspecced-functions-report!)` again to update this report

