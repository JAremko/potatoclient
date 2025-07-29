(ns debug-test
  (:require [potatoclient.state.proto-bridge :as bridge]))

(let [state {:protocol-version 1}
      result (bridge/edn-state->binary state)]
  (println "Result:" result)
  (println "Type:" (type result))
  (println "Nil?:" (nil? result)))