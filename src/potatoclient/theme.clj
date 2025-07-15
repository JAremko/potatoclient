(ns potatoclient.theme
  "Theme management for PotatoClient using DarkLaf"
  (:require [clojure.java.io :as io]
            [seesaw.core :as seesaw]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:import com.github.weisj.darklaf.LafManager
           [com.github.weisj.darklaf.theme
            SolarizedLightTheme
            SolarizedDarkTheme
            OneDarkTheme
            HighContrastDarkTheme]
           [java.text SimpleDateFormat]
           [java.util Date]))

;; Theme configuration atom
(def ^:private theme-config (atom {:current-theme :sol-dark}))


(defn get-current-theme
  "Get the current theme key"
  []
  (:current-theme @theme-config))

(m/=> get-current-theme [:=> [:cat] ::specs/theme-key])

(defn- apply-theme!
  "Apply the given theme using DarkLaf"
  [theme-key]
  (case theme-key
    :sol-light (LafManager/setTheme (SolarizedLightTheme.))
    :sol-dark (LafManager/setTheme (SolarizedDarkTheme.))
    :dark (LafManager/setTheme (OneDarkTheme.))
    :hi-dark (LafManager/setTheme (HighContrastDarkTheme.))
    ;; Default fallback
    (LafManager/setTheme (SolarizedDarkTheme.)))
  (LafManager/install))

(defn set-theme!
  "Set and apply a new theme"
  [theme-key]
  (if (m/validate ::specs/theme-key theme-key)
    (do
      (swap! theme-config assoc :current-theme theme-key)
      (apply-theme! theme-key)
      true)
    (do
      (println (str "Invalid theme key: " theme-key))
      false)))

(m/=> set-theme! [:=> [:cat ::specs/theme-key] :boolean])

(defn initialize-theme!
  "Initialize the theming system with default or saved theme"
  [initial-theme]
  (when (m/validate ::specs/theme-key initial-theme)
    (set-theme! initial-theme)))

(m/=> initialize-theme! [:=> [:cat ::specs/theme-key] :any])

(defn get-theme-name
  "Get human-readable name for a theme key"
  [theme-key]
  (case theme-key
    :sol-light "Sol Light"
    :sol-dark "Sol Dark"
    :dark "Dark"
    :hi-dark "Hi-Dark"
    "Unknown"))

(m/=> get-theme-name [:=> [:cat ::specs/theme-key] :string])

(defn get-theme-i18n-key
  "Get the i18n key for a theme"
  [theme-key]
  (case theme-key
    :sol-light :theme-sol-light
    :sol-dark :theme-sol-dark
    :dark :theme-dark
    :hi-dark :theme-hi-dark
    :theme-unknown))

(m/=> get-theme-i18n-key [:=> [:cat ::specs/theme-key] :keyword])

(defn get-available-themes
  "Get a vector of available theme keys"
  []
  [:sol-light :sol-dark :dark :hi-dark])

(m/=> get-available-themes [:=> [:cat] [:vector ::specs/theme-key]])


(defn- is-development-mode?
  "Check if running in development mode."
  []
  (not (or (System/getProperty "potatoclient.release")
           (System/getenv "POTATOCLIENT_RELEASE"))))

(m/=> is-development-mode? [:=> [:cat] :boolean])

;; Date formatter for logging
(def ^:private ^ThreadLocal log-formatter
  (ThreadLocal/withInitial
   #(SimpleDateFormat. "HH:mm:ss.SSS")))

(defn- log-theme
  "Log theme-related messages in development mode."
  [level message]
  (let [timestamp (.format ^SimpleDateFormat (.get log-formatter) (Date.))]
    (format "[%s] THEME %s: %s" timestamp level message)))

(m/=> log-theme [:=> [:cat :string :string] :string])

(declare key->icon)

(defn key->icon
  "Load theme-aware icon by key. Returns nil if icon not found."
  [icon-key]
  (let [icon-name (name icon-key)
        theme-name (name (get-current-theme))
        path (str "skins/" theme-name "/icons/" icon-name ".png")
        resource (io/resource path)]
    (when (is-development-mode?)
      (if resource
        (println (log-theme "INFO" (format "Loading icon: %s for theme: %s" icon-name theme-name)))
        (println (log-theme "WARN" (format "Icon not found: %s for theme: %s (path: %s)" 
                                          icon-name theme-name path)))))
    (when resource
      (try
        (let [icon (seesaw/icon resource)]
          (when (is-development-mode?)
            (println (log-theme "INFO" (format "Icon loaded successfully: %s" icon-name))))
          icon)
        (catch Exception e
          (when (is-development-mode?)
            (println (log-theme "ERROR" (format "Error loading icon: %s - %s" 
                                               icon-name (.getMessage e)))))
          nil)))))

(m/=> key->icon [:=> [:cat :keyword] ::specs/icon])

(defn preload-theme-icons!
  "Preload all icons for the current theme to ensure they're available."
  []
  (when (is-development-mode?)
    (println (log-theme "INFO" (format "Preloading icons for theme: %s" (name (get-current-theme))))))
  (let [icons-to-preload [:actions-group-menu :actions-group-theme :icon-languages
                         :file-export :sol-dark :sol-light :dark :hi-dark
                         :day :heat]
        theme-name (name (get-current-theme))]
    (doseq [icon-key icons-to-preload]
      (key->icon icon-key))
    (when (is-development-mode?)
      (println (log-theme "INFO" "Icon preloading completed")))))

(m/=> preload-theme-icons! [:=> [:cat] :any])