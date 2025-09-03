(ns potatoclient.cmd.root
  "Root-level command functions (ping, noop, frozen).
   These commands are at the root level of the cmd proto, not under any sub-message."
  (:require
            [malli.core :as m]
    [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Root-level Empty Commands
;; ============================================================================

(defn ping
  "Create a ping command to keep connection alive.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:ping {}})) 
 (m/=> ping [:=> [:cat] :cmd/root])

(defn noop
  "Create a no-operation command.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:noop {}})) 
 (m/=> noop [:=> [:cat] :cmd/root])

(defn frozen
  "Create a frozen command to indicate system freeze state.
   Returns a fully formed cmd root ready to send."
  []
  (core/create-command {:frozen {}})) 
 (m/=> frozen [:=> [:cat] :cmd/root])