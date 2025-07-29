(ns potatoclient.instrumentation
  "Function instrumentation using Malli schemas.
  This namespace is excluded from AOT compilation and only used during development."
  (:require [malli.core :as m]
            [malli.dev :as malli-dev]
            [malli.dev.pretty :as malli-pretty]
            [potatoclient.specs :as specs]))

;; ============================================================================
;; Core Namespace Instrumentation
;; ============================================================================

;; Add function schemas for cmd.core WebSocket functions
(m/=> potatoclient.cmd.core/init-websocket! [:=> [:cat string? ifn? ifn?] nil?])
(m/=> potatoclient.cmd.core/stop-websocket! [:=> [:cat] nil?])

;; ============================================================================
;; State Management Instrumentation
;; ============================================================================

;; state.edn functions
(m/=> potatoclient.state.edn/create-empty-state [:=> [:cat] map?])
(m/=> potatoclient.state.edn/create-subsystem-state [:=> [:cat keyword? map?] map?])
(m/=> potatoclient.state.edn/validate-state [:=> [:cat any?] boolean?])
(m/=> potatoclient.state.edn/explain-state [:=> [:cat any?] any?])
(m/=> potatoclient.state.edn/validate-subsystem [:=> [:cat keyword? any?] boolean?])
(m/=> potatoclient.state.edn/subsystem-changed? [:=> [:cat [:maybe map?] [:maybe map?] keyword?] boolean?])
(m/=> potatoclient.state.edn/changed-subsystems [:=> [:cat [:maybe map?] [:maybe map?]] set?])
(m/=> potatoclient.state.edn/merge-state [:=> [:cat map? map?] map?])
(m/=> potatoclient.state.edn/update-subsystem [:=> [:cat map? keyword? any?] map?])
(m/=> potatoclient.state.edn/extract-subsystems [:=> [:cat map?] map?])
(m/=> potatoclient.state.edn/get-subsystem [:=> [:cat map? keyword?] any?])
(m/=> potatoclient.state.edn/generate-test-state [:=> [:cat [:* keyword?]] map?])

;; state.proto-bridge functions
(m/=> potatoclient.state.proto-bridge/binary->edn-state [:=> [:cat bytes?] [:maybe map?]])
(m/=> potatoclient.state.proto-bridge/edn-state->binary [:=> [:cat map?] [:maybe bytes?]])
(m/=> potatoclient.state.proto-bridge/proto-msg->edn-state [:=> [:cat any?] map?])
(m/=> potatoclient.state.proto-bridge/extract-subsystem-edn [:=> [:cat any? keyword?] [:maybe map?]])
(m/=> potatoclient.state.proto-bridge/has-subsystem? [:=> [:cat any? keyword?] boolean?])
(m/=> potatoclient.state.proto-bridge/changed? [:=> [:cat [:maybe map?] [:maybe map?]] boolean?])
(m/=> potatoclient.state.proto-bridge/subsystem-changed? [:=> [:cat [:maybe map?] [:maybe map?] keyword?] boolean?])
(m/=> potatoclient.state.proto-bridge/parse-gui-state [:=> [:cat bytes?] any?])
(m/=> potatoclient.state.proto-bridge/extract-all-subsystems [:=> [:cat any?] map?])

;; state.dispatch functions
(m/=> potatoclient.state.dispatch/get-instance [:=> [:cat] any?])
(m/=> potatoclient.state.dispatch/handle-binary-state [:=> [:cat bytes?] nil?])
(m/=> potatoclient.state.dispatch/get-state-channel [:=> [:cat] any?])
(m/=> potatoclient.state.dispatch/dispose! [:=> [:cat] nil?])
(m/=> potatoclient.state.dispatch/enable-validation! [:=> [:cat boolean?] nil?])
(m/=> potatoclient.state.dispatch/enable-debug! [:=> [:cat boolean?] nil?])

;; ============================================================================
;; Instrumentation Control
;; ============================================================================

(defn start!
  "Start Malli instrumentation for development.
  Only call this in development mode."
  []
  (malli-dev/start! {:report (malli-pretty/reporter)}))

(defn stop!
  "Stop Malli instrumentation."
  []
  (malli-dev/stop!))