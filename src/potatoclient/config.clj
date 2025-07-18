(ns potatoclient.config
  "Configuration management for PotatoClient"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.theme :as theme]
            [malli.core :as m]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging])
  (:import [java.net URL]))

(def ^:private config-file-name "potatoclient-config.edn")


(defn- extract-domain
  "Extract domain/IP from various URL formats."
  [input]
  (let [cleaned (str/trim input)]
    ;; If it's already just a domain/IP (no protocol, no path), return as-is
    (if (and (not (str/includes? cleaned "://"))
             (not (re-find #"[/?#&:]" cleaned)))
      cleaned
      ;; Otherwise extract the domain/IP part
      (let [;; Remove protocol if present
            after-protocol (if-let [idx (str/index-of cleaned "://")]
                             (subs cleaned (+ idx 3))
                             cleaned)
            ;; Take everything up to the first separator (excluding port)
            domain (if-let [sep-idx (some #(str/index-of after-protocol %)
                                          ["/" "?" "#" "&"])]
                     (subs after-protocol 0 sep-idx)
                     after-protocol)
            ;; Remove port if present
            domain (if-let [port-idx (str/index-of domain ":")]
                     (subs domain 0 port-idx)
                     domain)]
        ;; Return the extracted domain or original if extraction failed
        (if (str/blank? domain)
          cleaned
          domain)))))


(defn- get-config-dir
  "Get the configuration directory path using platform-specific conventions"
  []
  (let [os-name (.toLowerCase ^String (System/getProperty "os.name"))]
    (cond
      ;; Windows - use LOCALAPPDATA if available, fallback to APPDATA
      (.contains ^String os-name "win")
      (let [local-appdata (System/getenv "LOCALAPPDATA")
            appdata (System/getenv "APPDATA")
            user-home (System/getProperty "user.home")]
        (io/file (or local-appdata
                     appdata
                     (str user-home "/AppData/Local"))
                 "PotatoClient"))
      
      ;; macOS - use standard Application Support directory
      (.contains ^String os-name "mac")
      (io/file (System/getProperty "user.home")
               "Library"
               "Application Support"
               "PotatoClient")
      
      ;; Linux/Unix - follow XDG Base Directory specification
      :else
      (let [xdg-config (System/getenv "XDG_CONFIG_HOME")
            user-home (System/getProperty "user.home")
            ;; Only use XDG_CONFIG_HOME if it's a proper path
            config-base (if (and xdg-config 
                                (.startsWith ^String xdg-config "/")
                                (not= xdg-config user-home))
                         xdg-config
                         (io/file user-home ".config"))]
        (io/file config-base "potatoclient")))))


(defn- get-config-file
  "Get the configuration file"
  []
  (io/file (get-config-dir) config-file-name))


(defn- ensure-config-dir!
  "Ensure the configuration directory exists"
  []
  (let [config-dir (get-config-dir)]
    (when-not (.exists ^java.io.File config-dir)
      (.mkdirs ^java.io.File config-dir))))


(defn load-config
  "Load configuration from file, return default if not found"
  []
  (let [config-file (get-config-file)
        default-config {:theme :sol-dark
                        :url "wss://sych.local"
                        :locale :english}]
    (if (.exists ^java.io.File config-file)
      (try
        (let [file-data (-> config-file
                            slurp
                            edn/read-string)
              ;; Handle backward compatibility: convert domain to url if needed
              migrated-data (if (and (:domain file-data) (not (:url file-data)))
                              (-> file-data
                                  (assoc :url (str "wss://" (:domain file-data)))
                                  (dissoc :domain))
                              file-data)
              merged-config (merge default-config migrated-data)]
          ;; Validate the merged config
          (if (m/validate ::specs/config merged-config)
            merged-config
            (do
              (logging/log-warn {:id ::invalid-config
                                 :data {:config merged-config
                                        :errors (m/explain ::specs/config merged-config)}
                                 :msg "Invalid config detected, using empty config"})
              ;; Return empty config so UI doesn't prefill with defaults
              {})))
        (catch Exception e
          (logging/log-error (str "Error loading config: " (.getMessage ^Exception e)))
          default-config))
      default-config)))


(defn save-config!
  "Save configuration to file"
  [config]
  (if (m/validate ::specs/config config)
    (try
      (ensure-config-dir!)
      (let [config-file (get-config-file)]
        (spit config-file (pr-str config)))
      true
      (catch Exception e
        (logging/log-error (str "Error saving config: " (.getMessage ^Exception e)))
        false))
    (do
      (logging/log-error (str "Invalid config, not saving: " (m/explain ::specs/config config)))
      false)))


(defn get-theme
  "Get the saved theme from config"
  []
  (or (:theme (load-config)) :sol-dark))


(defn save-theme!
  "Save the current theme to config"
  [theme-key]
  (let [config (load-config)]
    (save-config! (assoc config :theme theme-key))))


(defn get-url
  "Get the saved URL from config"
  []
  (or (:url (load-config)) ""))


(defn save-url!
  "Save the URL to config"
  [url]
  (let [config (load-config)]
    (save-config! (assoc config :url url))))


(defn get-domain
  "Get the domain extracted from the saved URL (or legacy domain field)"
  []
  (let [config (load-config)]
    (if-let [url (:url config)]
      ;; Extract domain from URL
      (extract-domain url)
      ;; Fallback to legacy domain field
      (:domain config "sych.local"))))


(defn save-domain!
  "Save the domain to config (stores as URL for consistency)"
  [domain]
  ;; Only add wss:// if it's not already a URL
  (if (or (clojure.string/includes? domain "://")
          (clojure.string/starts-with? domain "//"))
    (save-url! domain)
    (save-url! (str "wss://" domain))))


(defn get-locale
  "Get the saved locale from config"
  []
  (or (:locale (load-config)) :english))


(defn save-locale!
  "Save the locale to config"
  [locale]
  (let [config (load-config)]
    (save-config! (assoc config :locale locale))))


(defn update-config!
  "Update a specific configuration key-value pair"
  [key value]
  (let [config (load-config)]
    (save-config! (assoc config key value))))


(defn get-config-location
  "Get the full path to the configuration file (for debugging)"
  []
  (.getAbsolutePath ^java.io.File (get-config-file)))


(defn initialize!
  "Initialize configuration system"
  []
  (let [config (load-config)
        config-file (get-config-file)]
    ;; Log config location on first run
    (logging/log-info (str "Configuration file location: " (get-config-location)))
    ;; Only save defaults if config file doesn't exist
    (when (not (.exists ^java.io.File config-file))
      (logging/log-info "Creating default config file")
      (save-config! config))
    ;; Initialize theme from saved config (or default)
    (theme/initialize-theme! (or (:theme config) :sol-dark))
    ;; Set initial locale
    (when-let [locale (or (:locale config) :english)]
      (require '[potatoclient.state :as state])
      ((resolve 'potatoclient.state/set-locale!) locale))
    ;; Return the loaded config
    config))

