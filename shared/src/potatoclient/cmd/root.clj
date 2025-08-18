(ns potatoclient.cmd.root
  "Root-level command functions (ping, noop, frozen).
   These commands are at the root level of the cmd proto, not under any sub-message."
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => | ?]]
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Root-level Empty Commands
;; ============================================================================

(>defn ping
  "Send a ping command to keep connection alive.
   This is a root-level command in the payload oneof."
  []
  [=> :cmd/root]
  (core/send-command! {:ping {}}))

(>defn noop
  "Send a no-operation command.
   This is a root-level command in the payload oneof."
  []
  [=> :cmd/root]
  (core/send-command! {:noop {}}))

(>defn frozen
  "Send a frozen command to indicate system freeze state.
   This is a root-level command in the payload oneof."
  []
  [=> :cmd/root]
  (core/send-command! {:frozen {}}))