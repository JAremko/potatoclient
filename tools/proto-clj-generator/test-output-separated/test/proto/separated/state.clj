(ns test.proto.separated.state
  "State index - re-exports all state namespaces"
  (:require [test.proto.separated.ser :as ser]))

;; Re-export all public functions from sub-namespaces
;; This supports testing without needing to know the internal namespace
;; structure


