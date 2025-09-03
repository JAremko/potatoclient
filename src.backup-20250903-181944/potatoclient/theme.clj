(ns potatoclient.theme
  "Theme management for PotatoClient using DarkLaf"
  (:require [clojure.java.io :as io]
            [malli.core :as m]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.ui-specs :as specs]
            [seesaw.core :as seesaw])
  (:import com.github.weisj.darklaf.LafManager
           (com.github.weisj.darklaf.theme
             HighContrastDarkTheme
             OneDarkTheme
             SolarizedDarkTheme
             SolarizedLightTheme)
           (java.text SimpleDateFormat)
           (java.util Date)))

;; Theme configuration atom
(def ^:private theme-config (atom {:current-theme :sol-dark}))

(defn get-current-theme
  "Get the current theme key"
  []
  (:current-theme @theme-config)) 
 (m/=> get-current-theme [:=> [:cat] :potatoclient.ui-specs/theme-key])

;; Forward declaration for icon cache clearing
(declare clear-icon-cache!)

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
 (m/=> apply-theme! [:=> [:cat :potatoclient.ui-specs/theme-key] :nil])

(defn set-theme!
  "Set and apply a new theme"
  [theme-key]
  (if (m/validate ::specs/theme-key theme-key)
    (do
      ;; Clear icon cache when changing themes
      (clear-icon-cache!)
      (swap! theme-config assoc :current-theme theme-key)
      (apply-theme! theme-key)
      true)
    (do
      (logging/log-error {:msg (str "Invalid theme key: " theme-key)})
      false))) 
 (m/=> set-theme! [:=> [:cat :potatoclient.ui-specs/theme-key] :boolean])

(defn initialize-theme!
  "Initialize the theming system with default or saved theme"
  [initial-theme]
  (when (m/validate ::specs/theme-key initial-theme)
    (set-theme! initial-theme))
  nil) 
 (m/=> initialize-theme! [:=> [:cat :potatoclient.ui-specs/theme-key] :nil])

(defn get-theme-i18n-key
  "Get the i18n key for a theme"
  [theme-key]
  (case theme-key
    :sol-light :theme-sol-light
    :sol-dark :theme-sol-dark
    :dark :theme-dark
    :hi-dark :theme-hi-dark
    :theme-unknown)) 
 (m/=> get-theme-i18n-key [:=> [:cat :potatoclient.ui-specs/theme-key] :keyword])

(defn get-available-themes
  "Get a vector of available theme keys"
  []
  [:sol-light :sol-dark :dark :hi-dark]) 
 (m/=> get-available-themes [:=> [:cat] [:sequential :potatoclient.ui-specs/theme-key]])

(defn- is-development-mode?
  "Check if running in development mode."
  []
  (not (runtime/release-build?))) 
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

;; Icon cache - key is [theme-key icon-key], value is the loaded icon
(def ^:private icon-cache (atom {}))

(defn clear-icon-cache!
  "Clear the icon cache. Useful when changing themes."
  []
  (reset! icon-cache {}))

(declare key->icon)

(defn key->icon
  "Load theme-aware icon by key with caching. Returns nil if icon not found."
  [icon-key]
  (let [theme-key (get-current-theme)
        cache-key [theme-key icon-key]]
    ;; Check cache first
    (if-let [cached-icon (get @icon-cache cache-key)]
      cached-icon
      ;; Not in cache, load it
      (let [icon-name (name icon-key)
            theme-name (name theme-key)
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
              ;; Cache the loaded icon
              (swap! icon-cache assoc cache-key icon)
              icon)
            (catch Exception e
              (when (is-development-mode?)
                (println (log-theme "ERROR" (format "Error loading icon: %s - %s"
                                                    icon-name (.getMessage e)))))
              nil))))))) 
 (m/=> key->icon [:=> [:cat :keyword] [:maybe :potatoclient.ui-specs/icon]])

(defn get-icon
  "Get theme-aware icon by key. Alias for key->icon for clarity."
  [icon-key]
  (key->icon icon-key)) 
 (m/=> get-icon [:=> [:cat :keyword] [:maybe :potatoclient.ui-specs/icon]])

(defn preload-theme-icons!
  "Preload all icons for the current theme to ensure they're available."
  []
  (when (is-development-mode?)
    (println (log-theme "INFO" (format "Preloading icons for theme: %s" (name (get-current-theme))))))
  (let [icons-to-preload [:actions-group-menu :actions-group-theme :icon-languages
                          :file-export :sol-dark :sol-light :dark :hi-dark
                          :day :heat :status-bar-icon-good :status-bar-icon-warn :status-bar-icon-bad]]
    (doseq [icon-key icons-to-preload]
      (key->icon icon-key))
    (when (is-development-mode?)
      (println (log-theme "INFO" "Icon preloading completed"))))) 
 (m/=> preload-theme-icons! [:=> [:cat] :nil])