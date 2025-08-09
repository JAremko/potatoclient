(ns validate.specs.cmd.common
  "Common command specs used across multiple command types.
   All maps use {:closed true} to catch typos and invalid keys."
  (:require
   [potatoclient.malli.registry :as registry]))

;; Common command types that appear in multiple places

;; Ping command (heartbeat) - empty map
(def ping-command-spec
  [:map {:closed true}])

(registry/register! :cmd/ping ping-command-spec)

;; Noop command (no operation) - empty map
(def noop-command-spec
  [:map {:closed true}])

(registry/register! :cmd/noop noop-command-spec)

;; Frozen command (freeze state)
(def frozen-command-spec
  [:map {:closed true}
   [:flag :boolean]])

(registry/register! :cmd/frozen frozen-command-spec)