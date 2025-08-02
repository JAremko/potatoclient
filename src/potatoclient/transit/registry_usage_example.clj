(ns potatoclient.transit.registry-usage-example
  "Example of how to use ActionRegistry from Clojure code"
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- | ? =>]])
  (:import [potatoclient.transit ActionRegistry ActionDefinition]
           [com.cognitect.transit TransitFactory]))

(>defn lookup-action
  "Look up an action by name and return its metadata"
  [action-name]
  [string? => (? [:map
                  [:name string?]
                  [:description string?]
                  [:required-params [:set keyword?]]
                  [:optional-params [:set keyword?]]
                  [:implemented? boolean?]])]
  (when-let [action (ActionRegistry/getAction action-name)]
    {:name (.getName action)
     :description (.getDescription action)
     :required-params (set (map #(.getName %) (.getRequiredParams action)))
     :optional-params (set (map #(.getName %) (.getOptionalParams action)))
     :implemented? (.isImplemented action)}))

(>defn validate-command-params
  "Check if a command has all required parameters"
  [action-name params]
  [string? map? => boolean?]
  (ActionRegistry/hasRequiredParams action-name params))

(>defn list-commands-by-prefix
  "List all commands that start with a given prefix"
  [prefix]
  [string? => [:sequential string?]]
  (let [all-commands (ActionRegistry/getAllActionNames)]
    (vec (filter #(.startsWith % prefix) all-commands))))

(>defn get-command-stats
  "Get statistics about registered commands"
  []
  [=> [:map
       [:total pos-int?]
       [:implemented pos-int?]
       [:unimplemented int?]]]
  (let [stats (ActionRegistry/getStatistics)]
    {:total (get stats "total")
     :implemented (get stats "implemented")
     :unimplemented (get stats "unimplemented")}))

;; Example usage:
(comment
  ;; Look up a specific action
  (lookup-action "rotary-rotate-to-ndc")
  ;; => {:name "rotary-rotate-to-ndc"
  ;;     :description "Rotate to NDC coordinates"
  ;;     :required-params #{"channel" "x" "y"}
  ;;     :optional-params #{}
  ;;     :implemented? false}
  
  ;; Validate command parameters
  (validate-command-params "rotary-rotate-to-ndc" 
                           {"channel" "heat" "x" 0.5 "y" -0.5})
  ;; => true
  
  (validate-command-params "rotary-rotate-to-ndc"
                           {"channel" "heat" "x" 0.5})
  ;; => false (missing y)
  
  ;; List all rotary commands
  (list-commands-by-prefix "rotary-")
  ;; => ["rotary-axis-azimuth-halt" "rotary-axis-azimuth-relative" ...]
  
  ;; Get registry statistics
  (get-command-stats)
  ;; => {:total 65, :implemented 8, :unimplemented 57}
  )