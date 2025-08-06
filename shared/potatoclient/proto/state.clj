(ns potatoclient.proto.state
  "State index - re-exports all state namespaces"
  (:require [potatoclient.proto.ser :as ser]))

;; Re-export all public functions from sub-namespaces
;; This supports testing without needing to know the internal namespace
;; structure


