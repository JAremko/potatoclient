(ns protobuf.generators
  "Test data generators for protobuf messages"
  (:require [protobuf.cmd-specs :as cmd]
            [protobuf.state-specs :as state]
            [malli.core :as m]
            [malli.generator :as mg]))

;; Command generators

(defn generate-command
  "Generate a valid command message"
  [command-type & {:keys [seed size] :or {size 10}}]
  (let [spec [:ref command-type]]
    (mg/generate cmd/schema {:seed seed :size size})))

(defn generate-cmd-root
  "Generate a root command message"
  [& opts]
  (apply generate-command :cmd/JonSharedCmd/Root opts))

;; State generators

(defn generate-state
  "Generate a valid state message"
  [state-type & {:keys [seed size] :or {size 10}}]
  (let [spec [:ref state-type]]
    (mg/generate state/schema {:seed seed :size size})))

(defn generate-state-root
  "Generate a root state message"
  [& opts]
  (apply generate-state :ser/JonSharedState/Root opts))

;; Validation helpers

(defn valid-command?
  "Check if data is a valid command"
  [command-type data]
  (m/validate [:ref command-type] data cmd/schema))

(defn valid-state?
  "Check if data is a valid state message"
  [state-type data]
  (m/validate [:ref state-type] data state/schema))

;; Test data factory

(defn create-test-factory
  "Create a test data factory with overrides"
  []
  {:generate-command (fn [cmd-type & {:keys [overrides] :as opts}]
                      (let [base (apply generate-command cmd-type opts)]
                        (merge base overrides)))
   :generate-state (fn [state-type & {:keys [overrides] :as opts}]
                    (let [base (apply generate-state state-type opts)]
                      (merge base overrides)))
   :validate-command valid-command?
   :validate-state valid-state?})
