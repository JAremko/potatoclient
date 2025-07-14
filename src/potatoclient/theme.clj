(ns potatoclient.theme
  "Theme management for PotatoClient using DarkLaf"
  (:require [clojure.spec.alpha :as s]
            [orchestra.core :refer [defn-spec]]
            [orchestra.spec.test :as st])
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

(defn-spec get-current-theme ::theme-key
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

(defn-spec set-theme! boolean?
  "Set and apply a new theme"
  [theme-key ::theme-key]
  (if (s/valid? ::theme-key theme-key)
    (do
      (swap! theme-config assoc :current-theme theme-key)
      (apply-theme! theme-key)
      true)
    (do
      (println (str "Invalid theme key: " theme-key))
      false)))

(defn-spec initialize-theme! any?
  "Initialize the theming system with default or saved theme"
  [initial-theme ::theme-key]
  (when (s/valid? ::theme-key initial-theme)
    (set-theme! initial-theme)))

(defn-spec get-theme-name string?
  "Get human-readable name for a theme key"
  [theme-key ::theme-key]
  (case theme-key
    :sol-light "Solarized Light"
    :sol-dark "Solarized Dark"
    :dark "Real Dark"
    :light "Light"
    "Unknown"))

(defn-spec get-available-themes vector?
  "Get a vector of available theme keys"
  []
  [:sol-light :sol-dark :dark :light])