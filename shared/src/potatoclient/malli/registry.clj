(ns potatoclient.malli.registry
  "Global registry management for Malli specs"
  (:require
   [malli.core :as m]
   [malli.registry :as mr]
   [malli.util :as mu]
   [potatoclient.malli.oneof :as oneof]))

;; Create a mutable registry atom for dynamic registration
(defonce ^:private registry-atom (atom {}))

(defn register-spec!
  "Register a spec in the global mutable registry"
  [spec-key spec]
  (swap! registry-atom assoc spec-key spec))

(defn register!
  "DEPRECATED: Use register-spec! instead"
  [spec-key spec]
  (register-spec! spec-key spec))

(defn setup-global-registry!
  "Configure Malli to use a global registry for all specs.
   This ensures specs can be reused across namespaces.
   Automatically includes the :oneof custom schema."
  [& custom-schemas]
  (let [oneof-registry (oneof/register-oneof-schema! {})]
    (mr/set-default-registry!
     (apply mr/composite-registry
            (concat
             [(m/default-schemas)                    ;; Include Malli's built-in schemas
              (mu/schemas)                           ;; Include utility schemas
              oneof-registry                         ;; Include oneof schema
              (mr/mutable-registry registry-atom)]   ;; Add mutable registry for dynamic specs
             custom-schemas)))
    ;; Return the complete registry for inspection if needed
    oneof-registry))

(defn get-registry
  "Get the current contents of the mutable registry"
  []
  @registry-atom)