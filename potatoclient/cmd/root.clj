(ns potatoclient.cmd.root
  "Root-level command functions (ping, noop, frozen).
   These commands are at the root level of the cmd proto, not under any sub-message."
  (:require
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Root-level Empty Commands
;; ============================================================================

(defn ping
  "Create a ping command to keep connection alive.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:ping {}}))

(defn noop
  "Create a no-operation command.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:noop {}}))

(defn frozen
  "Create a frozen command to indicate system freeze state.
   Returns a fully formed cmd root ready to send."
  {:malli/schema [:=> [:cat] :cmd/root]}
  []
  (core/create-command {:frozen {}}))