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