(ns potatoclient.ui.status-bar.validation
  "Validation utilities that report to status bar."
  (:require
    [malli.core :as m]
    [malli.error :as me]
    [potatoclient.logging :as logging]
    [potatoclient.ui.status-bar.messages :as msg]))

(defn- resolve-schema
  "Resolve a spec to a Malli schema.
   If spec is a keyword, looks it up in the registry.
   Otherwise returns the spec as-is (assuming it's already a schema)."
  [spec]
  (if (keyword? spec)
    (m/schema spec)
    spec)) 
 (m/=> resolve-schema [:=> [:cat :any] :any])

(defn validate
  "Validate a value against a Malli spec. Reports validation errors to status bar and logs.
   Returns true if valid, false if invalid.
   
   The spec can be:
   - A keyword that resolves from the registry
   - A Malli schema directly
   
   Examples:
   (validate :int 42)           ;; => true
   (validate :int \"not-int\")    ;; => false (shows error in status bar and logs)
   (validate [:map [:x :int]] {:x 1}) ;; => true"
  [spec value]
  (let [schema (resolve-schema spec)]
    (if (m/validate schema value)
      true
      (do
        (let [explanation (m/explain schema value)
              errors (me/humanize explanation)
              error-msg (str "Validation failed: " (pr-str errors))]
          ;; Log the validation failure with details
          (logging/log-warn {:msg "Validation failed"
                             :spec (if (keyword? spec) spec (m/form schema))
                             :value value
                             :errors errors})
          ;; Report to status bar
          (msg/set-error! error-msg))
        false)))) 
 (m/=> validate [:=> [:cat :any :any] :boolean])

(defn validate-with-details
  "Validate a value against a spec and return detailed results.
   Returns a map with :valid? and optionally :errors keys.
   Also reports errors to status bar and logs if invalid."
  [spec value]
  (let [schema (resolve-schema spec)]
    (if (m/validate schema value)
      {:valid? true}
      (let [explanation (m/explain schema value)
            errors (me/humanize explanation)
            error-msg (str "Validation failed: " (pr-str errors))]
        ;; Log the validation failure with details
        (logging/log-warn {:msg "Validation failed (with details)"
                           :spec (if (keyword? spec) spec (m/form schema))
                           :value value
                           :errors errors})
        ;; Report to status bar
        (msg/set-error! error-msg)
        {:valid? false
         :errors errors})))) 
 (m/=> validate-with-details [:=> [:cat :any :any] [:map [:valid? :boolean] [:errors {:optional true} :any]]])

(defn valid?
  "Check if value is valid according to spec without reporting to status bar.
   Silent version of validate."
  [spec value]
  (let [schema (resolve-schema spec)]
    (m/validate schema value))) 
 (m/=> valid? [:=> [:cat :any :any] :boolean])

(defn explain-validation
  "Get human-readable validation errors without reporting to status bar."
  [spec value]
  (let [schema (resolve-schema spec)]
    (when-not (m/validate schema value)
      (me/humanize (m/explain schema value))))) 
 (m/=> explain-validation [:=> [:cat :any :any] [:maybe :any]])