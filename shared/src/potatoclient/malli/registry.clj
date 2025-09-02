(ns
  potatoclient.malli.registry
  "Global registry management for Malli specs"
  (:require
    [malli.core :as m]
    [malli.registry :as mr]
    [malli.util :as mu]
    [potatoclient.malli.oneof :as oneof]))

(defonce registry-atom (atom {}))

(defn
  register-spec!
  "Register a spec in the global mutable registry"
  [spec-key spec]
  (swap! registry-atom assoc spec-key spec))

(defn
  register!
  "DEPRECATED: Use register-spec! instead"
  [spec-key spec]
  (register-spec! spec-key spec))

(defn
  setup-global-registry!
  "Configure Malli to use a global registry for all specs.\n   This ensures specs can be reused across namespaces.\n   Automatically includes the :oneof custom schema."
  [& custom-schemas]
  (let
    [oneof-registry
     (oneof/register-oneof-schema! {})
     base-registry
     (merge oneof-registry {:bytes bytes?})]
    (mr/set-default-registry!
      (apply
        mr/composite-registry
        (concat
          [(m/default-schemas)
           (mu/schemas)
           base-registry
           (mr/mutable-registry registry-atom)]
          custom-schemas)))
    base-registry))

(defn
  get-registry
  "Get the current contents of the mutable registry"
  []
  @registry-atom)
