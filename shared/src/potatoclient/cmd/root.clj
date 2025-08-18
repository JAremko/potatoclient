(ns potatoclient.cmd.root
  "Root-level command functions (ping, noop, frozen).
   These commands are at the root level of the cmd proto, not under any sub-message."
  (:require
   [com.fulcrologic.guardrails.core :refer [>defn >defn- => | ?]]
   [clojure.spec.alpha :as s]
   [potatoclient.cmd.core :as core]))

;; ============================================================================
;; Specs
;; ============================================================================

(s/def ::cmd-root (s/and map?
                         #(some? (dissoc % :protocol_version :client_type 
                                        :session_id :important :from_cv_subsystem))))

;; ============================================================================
;; Root-level Empty Commands
;; ============================================================================

(>defn ping
  "Send a ping command to keep connection alive.
   This is a root-level command in the payload oneof."
  []
  [=> ::cmd-root]
  (core/send-command! {:ping {}}))

(>defn noop
  "Send a no-operation command.
   This is a root-level command in the payload oneof."
  []
  [=> ::cmd-root]
  (core/send-command! {:noop {}}))

(>defn frozen
  "Send a frozen command to indicate system freeze state.
   This is a root-level command in the payload oneof."
  []
  [=> ::cmd-root]
  (core/send-command! {:frozen {}}))