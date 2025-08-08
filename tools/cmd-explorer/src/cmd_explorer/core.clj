(ns cmd-explorer.core
  (:require
   [com.fulcrologic.guardrails.malli.core :refer [>defn >def | ? =>]]
   [cmd-explorer.registry :as registry]
   [cmd-explorer.specs.oneof-payload :as oneof]))

(defn setup-malli-registry!
  "Configure Malli to use a global registry for all specs.
   This ensures specs can be reused across namespaces."
  []
  (registry/setup-global-registry!
   {:oneof-pronto (oneof/register-oneof-pronto-schema!)}))

(defn initialize!
  "Initialize the cmd-explorer system.
   Sets up global Malli registry and guardrails."
  []
  (setup-malli-registry!)
  (println "CMD-Explorer initialized:")
  (println "  - Malli global registry configured")
  (println "  - Guardrails using Malli namespace")
  (println "  - Ready for spec registration"))

;; Initialize on namespace load
(initialize!)