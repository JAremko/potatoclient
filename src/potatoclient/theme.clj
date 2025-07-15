(ns potatoclient.theme
  "Theme management for PotatoClient using DarkLaf"
  (:require [clojure.spec.alpha :as s]
            [orchestra.core :refer [defn-spec]]
            [orchestra.spec.test :as st]
            [clojure.java.io :as io]
            [seesaw.core :as seesaw])
  (:import com.github.weisj.darklaf.LafManager
           [com.github.weisj.darklaf.theme
            SolarizedLightTheme
            SolarizedDarkTheme
            OneDarkTheme
            IntelliJTheme]
           [java.text SimpleDateFormat]
           [java.util Date]))

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

;; Icon loading specs
(s/def ::icon-key keyword?)
(s/def ::icon (s/nilable #(instance? javax.swing.Icon %)))

(defn-spec ^:private is-development-mode? boolean?
  "Check if running in development mode."
  []
  (not (or (System/getProperty "potatoclient.release")
           (System/getenv "POTATOCLIENT_RELEASE"))))

;; Date formatter for logging
(def ^:private ^ThreadLocal log-formatter
  (ThreadLocal/withInitial
   #(SimpleDateFormat. "HH:mm:ss.SSS")))

(defn-spec ^:private log-theme string?
  "Log theme-related messages in development mode."
  [level string?, message string?]
  (let [timestamp (.format ^SimpleDateFormat (.get log-formatter) (Date.))]
    (format "[%s] THEME %s: %s" timestamp level message)))

(declare key->icon)

(defn-spec key->icon ::icon
  "Load theme-aware icon by key. Returns nil if icon not found."
  [icon-key ::icon-key]
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

(defn-spec preload-theme-icons! any?
  "Preload all icons for the current theme to ensure they're available."
  []
  (when (is-development-mode?)
    (println (log-theme "INFO" (format "Preloading icons for theme: %s" (name (get-current-theme))))))
  (let [icons-to-preload [:actions-group-menu :actions-group-theme :icon-languages
                         :file-export :sol-dark :sol-light :dark :light]
        theme-name (name (get-current-theme))]
    (doseq [icon-key icons-to-preload]
      (key->icon icon-key))
    (when (is-development-mode?)
      (println (log-theme "INFO" "Icon preloading completed")))))