(ns potatoclient.state.config
  "Runtime configuration state management."
  (:require [clojure.string]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn]]
            [potatoclient.config :as config])
  (:import (java.util Locale)))

;; Runtime configuration state
(defonce ^:private app-config
  (atom {:locale :english}))

(>defn get-locale
  "Get the current locale."
  []
  [=> :potatoclient.specs/locale]
  (:locale @app-config))

(>defn set-locale!
  "Set the current locale."
  [locale]
  [:potatoclient.specs/locale => nil?]
  (swap! app-config assoc :locale locale)
  ;; Also update default Locale
  (let [locale-map {:english ["en" "US"]
                    :ukrainian ["uk" "UA"]}
        [lang country] (get locale-map locale ["en" "US"])]
    (Locale/setDefault
      (Locale. ^String lang ^String country))))

(>defn get-domain
  "Get the current domain configuration from persistent config."
  []
  [=> :potatoclient.specs/domain]
  (config/get-domain))

(>defn set-domain!
  "Update the domain in runtime state (not persisted)."
  [domain]
  [:potatoclient.specs/domain => nil?]
  ;; Only update runtime state, domain is derived from URL history
  (swap! app-config assoc :domain domain)
  nil)

(>defn get-config
  "Get the entire runtime config map."
  []
  [=> [:map
       [:locale :potatoclient.specs/locale]
       [:domain :potatoclient.specs/domain]]]
  @app-config)