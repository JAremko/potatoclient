(ns potatoclient.theme
  "Theme management for PotatoClient using DarkLaf"
  (:require [clojure.spec.alpha :as s])
  (:import com.github.weisj.darklaf.LafManager
           [com.github.weisj.darklaf.theme
            SolarizedLightTheme
            SolarizedDarkTheme
            OneDarkTheme
            IntelliJTheme]))

;; Theme configuration atom
(def ^:private theme-config (atom {:current-theme :sol-dark}))

;; Valid theme keys
(s/def ::theme-key #{:sol-light :sol-dark :dark :light})

(defn get-current-theme
  "Get the current theme key"
  []
  (:current-theme @theme-config))

(defn- apply-theme!
  "Apply the given theme using DarkLaf"
  [theme-key]
  (case theme-key
    :sol-light (LafManager/setTheme (SolarizedLightTheme.))
    :sol-dark (LafManager/setTheme (SolarizedDarkTheme.))
    :dark (LafManager/setTheme (OneDarkTheme.))
    :light (LafManager/setTheme (IntelliJTheme.))
    ;; Default fallback
    (LafManager/setTheme (SolarizedDarkTheme.)))
  (LafManager/install))

(defn set-theme!
  "Set and apply a new theme"
  [theme-key]
  (if (s/valid? ::theme-key theme-key)
    (do
      (swap! theme-config assoc :current-theme theme-key)
      (apply-theme! theme-key)
      true)
    (do
      (println (str "Invalid theme key: " theme-key))
      false)))

(defn initialize-theme!
  "Initialize the theming system with default or saved theme"
  [initial-theme]
  (when (s/valid? ::theme-key initial-theme)
    (set-theme! initial-theme)))

(defn get-theme-name
  "Get human-readable name for a theme key"
  [theme-key]
  (case theme-key
    :sol-light "Solarized Light"
    :sol-dark "Solarized Dark"
    :dark "Real Dark"
    :light "Light"
    "Unknown"))

(defn get-available-themes
  "Get a vector of available theme keys"
  []
  [:sol-light :sol-dark :dark :light])