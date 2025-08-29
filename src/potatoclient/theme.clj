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
  "Get the current theme key" {:malli/schema [:=> [:cat] :potatoclient.ui-specs/theme-key]}
  []
  (:current-theme @theme-config))

(defn- apply-theme!
  "Apply the given theme using DarkLaf" {:malli/schema [:=> [:cat :potatoclient.ui-specs/theme-key] :nil]}
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
  "Set and apply a new theme" {:malli/schema [:=> [:cat :potatoclient.ui-specs/theme-key] :boolean]}
  [theme-key]
  (if (m/validate ::specs/theme-key theme-key)
    (do
      (swap! theme-config assoc :current-theme theme-key)
      (apply-theme! theme-key)
      true)
    (do
      (logging/log-error {:msg (str "Invalid theme key: " theme-key)})
      false)))

(defn initialize-theme!
  "Initialize the theming system with default or saved theme" {:malli/schema [:=> [:cat :potatoclient.ui-specs/theme-key] :nil]}
  [initial-theme]
  (when (m/validate ::specs/theme-key initial-theme)
    (set-theme! initial-theme))
  nil)

(defn get-theme-i18n-key
  "Get the i18n key for a theme" {:malli/schema [:=> [:cat :potatoclient.ui-specs/theme-key] :keyword]}
  [theme-key]
  (case theme-key
    :sol-light :theme-sol-light
    :sol-dark :theme-sol-dark
    :dark :theme-dark
    :hi-dark :theme-hi-dark
    :theme-unknown))

(defn get-available-themes
  "Get a vector of available theme keys" {:malli/schema [:=> [:cat] [:sequential :potatoclient.ui-specs/theme-key]]}
  []
  [:sol-light :sol-dark :dark :hi-dark])

(defn- is-development-mode?
  "Check if running in development mode." {:malli/schema [:=> [:cat] :boolean]}
  []
  (not (runtime/release-build?)))

;; Date formatter for logging
(def ^:private ^ThreadLocal log-formatter
  (ThreadLocal/withInitial
    #(SimpleDateFormat. "HH:mm:ss.SSS")))

(defn- log-theme
  "Log theme-related messages in development mode." {:malli/schema [:=> [:cat :string :string] :string]}
  [level message]
  (let [timestamp (.format ^SimpleDateFormat (.get log-formatter) (Date.))]
    (format "[%s] THEME %s: %s" timestamp level message)))

(declare key->icon)

(defn key->icon
  "Load theme-aware icon by key. Returns nil if icon not found." {:malli/schema [:=> [:cat :keyword] [:maybe :potatoclient.ui-specs/icon]]}
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

(defn preload-theme-icons!
  "Preload all icons for the current theme to ensure they're available." {:malli/schema [:=> [:cat] :nil]}
  []
  (when (is-development-mode?)
    (println (log-theme "INFO" (format "Preloading icons for theme: %s" (name (get-current-theme))))))
  (let [icons-to-preload [:actions-group-menu :actions-group-theme :icon-languages
                          :file-export :sol-dark :sol-light :dark :hi-dark
                          :day :heat]]
    (doseq [icon-key icons-to-preload]
      (key->icon icon-key))
    (when (is-development-mode?)
      (println (log-theme "INFO" "Icon preloading completed")))))