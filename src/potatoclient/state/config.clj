(ns potatoclient.state.config
  "Runtime configuration state management."
  (:require [potatoclient.config :as config]
            [malli.core :as m]
            [potatoclient.specs :as specs]
            [clojure.string]))

;; Runtime configuration state
(defonce ^:private app-config
  (atom {:locale :english}))

(defn get-locale
  "Get the current locale."
  []
  (:locale @app-config))

(defn set-locale!
  "Set the current locale."
  [locale]
  {:pre [(m/validate specs/locale locale)]}
  (swap! app-config assoc :locale locale)
  ;; Also update default Locale
  (let [locale-map {:english ["en" "US"]
                    :ukrainian ["uk" "UA"]}
        [lang country] (get locale-map locale ["en" "US"])]
    (java.util.Locale/setDefault
     (java.util.Locale. ^String lang ^String country))))

(defn get-domain
  "Get the current domain configuration from persistent config."
  []
  (config/get-domain))

(defn set-domain!
  "Update the domain configuration persistently."
  [domain]
  {:pre [(string? domain)
         (not (clojure.string/blank? domain))]}
  (config/save-domain! domain))

(defn get-config
  "Get the entire runtime config map."
  []
  @app-config)