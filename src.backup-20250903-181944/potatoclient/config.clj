(ns potatoclient.config
  "Configuration management for PotatoClient"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [malli.core :as m]
            [potatoclient.logging :as logging]
            [potatoclient.theme :as theme]
            [potatoclient.ui-specs :as specs])
  (:import (clojure.lang EdnReader$ReaderException)
           (java.io File IOException)
           (java.util Date)))

(def ^:private config-file-name "potatoclient-config.edn")

(defn- extract-domain*
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
 (m/=> extract-domain* [:=> [:cat :string] :potatoclient.ui-specs/domain])

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
 (m/=> get-config-dir [:=> [:cat] :potatoclient.ui-specs/file])

(defn- get-config-file
  "Get the configuration file"
  []
  (io/file (get-config-dir) config-file-name)) 
 (m/=> get-config-file [:=> [:cat] :potatoclient.ui-specs/file])

(defn- ensure-config-dir!
  "Ensure the configuration directory exists"
  []
  (let [config-dir (get-config-dir)]
    (when-not (.exists ^File config-dir)
      (.mkdirs ^File config-dir)))) 
 (m/=> ensure-config-dir! [:=> [:cat] :nil])

(defn load-config
  "Load configuration from file, return minimal config if not found"
  []
  (let [config-file (get-config-file)
        ;; Minimal config - no URL by default
        minimal-config {:theme :sol-dark
                        :locale :english}]
    (if (.exists ^File config-file)
      (try
        (let [file-data (-> config-file
                            slurp
                            edn/read-string)
              ;; Handle backward compatibility: convert domain to url if needed
              migrated-data (if (and (:domain file-data) (not (:url file-data)))
                              (-> file-data
                                  (assoc :url (str "wss://" (:domain file-data)))
                                  (dissoc :domain))
                              file-data)]
          ;; Validate without merging defaults
          (if (m/validate ::specs/config migrated-data)
            migrated-data
            (do
              (logging/log-warn {:id ::invalid-config
                                 :data {:config migrated-data
                                        :errors (m/explain ::specs/config migrated-data)}
                                 :msg "Invalid config detected, using minimal config"})
              minimal-config)))
        (catch IOException e
          (logging/log-error {:msg "Config file read error"
                              :error (.getMessage ^Exception e)
                              :file (.getPath config-file)})
          minimal-config)
        (catch EdnReader$ReaderException e
          (logging/log-error {:msg "Invalid config file format (EDN parsing failed)"
                              :error (.getMessage ^Exception e)
                              :file (.getPath config-file)})
          minimal-config)
        (catch RuntimeException e
          (logging/log-error {:msg "Unexpected error loading config"
                              :error (.getMessage ^Exception e)
                              :file (.getPath config-file)})
          minimal-config))
      minimal-config))) 
 (m/=> load-config [:=> [:cat] :potatoclient.ui-specs/config])

(defn save-config!
  "Save configuration to file"
  [config]
  (if (m/validate ::specs/config config)
    (try
      (ensure-config-dir!)
      (let [config-file (get-config-file)]
        (spit config-file (pr-str config)))
      true
      (catch IOException e
        (logging/log-error {:msg "Failed to write config file"
                            :error (.getMessage ^Exception e)
                            :file (.getPath (get-config-file))})
        false)
      (catch SecurityException e
        (logging/log-error {:msg "Permission denied saving config"
                            :error (.getMessage ^Exception e)
                            :file (.getPath (get-config-file))})
        false)
      (catch Exception e
        (logging/log-error {:msg "Unexpected error saving config"
                            :error (.getMessage ^Exception e)})
        false))
    (do
      (logging/log-error {:msg (str "Invalid config, not saving: " (m/explain ::specs/config config))})
      false))) 
 (m/=> save-config! [:=> [:cat :potatoclient.ui-specs/config] :boolean])

(defn save-theme!
  "Save the current theme to config"
  [theme-key]
  (let [config (load-config)]
    (save-config! (assoc config :theme theme-key)))) 
 (m/=> save-theme! [:=> [:cat :potatoclient.ui-specs/theme-key] :boolean])

(defn update-config!
  "Update a specific configuration key-value pair"
  [key value]
  (let [config (load-config)]
    (save-config! (assoc config key value)))) 
 (m/=> update-config! [:=> [:cat :potatoclient.ui-specs/config-key [:or :potatoclient.ui-specs/theme-key :potatoclient.ui-specs/domain :potatoclient.ui-specs/locale :potatoclient.ui-specs/url-history]] :boolean])

(defn get-config-location
  "Get the full path to the configuration file (for debugging)"
  []
  (.getAbsolutePath ^File (get-config-file))) 
 (m/=> get-config-location [:=> [:cat] :string])

(defn initialize!
  "Initialize configuration system"
  []
  (let [config (load-config)
        config-file (get-config-file)]
    ;; Log config location on first run
    (logging/log-info {:msg (str "Configuration file location: " (get-config-location))})
    ;; Only save defaults if config file doesn't exist
    (when (not (.exists ^File config-file))
      (logging/log-info {:msg "Creating default config file"})
      (save-config! config))
    ;; Initialize theme from saved config (or default)
    (theme/initialize-theme! (or (:theme config) :sol-dark))
    ;; Set initial locale
    (when-let [locale (or (:locale config) :english)]
      (require '[potatoclient.state :as state])
      ((resolve 'potatoclient.state/set-locale!) locale))
    ;; Return the loaded config
    config)) 
 (m/=> initialize! [:=> [:cat] :potatoclient.ui-specs/config])

;; -----------------------------------------------------------------------------
;; URL History Management
;; -----------------------------------------------------------------------------

(defn get-url-history
  "Get URL history from current config"
  []
  (let [config (load-config)]
    (or (:url-history config) []))) 
 (m/=> get-url-history [:=> [:cat] :potatoclient.ui-specs/url-history])

(defn get-recent-urls
  "Get up to 10 most recent unique URLs as a vector, sorted by timestamp (newest first)"
  []
  (vec (map :url
            (take 10
                  (sort-by :timestamp #(compare %2 %1) (get-url-history)))))) 
 (m/=> get-recent-urls [:=> [:cat] [:vector :string]])

(defn get-most-recent-url
  "Get the most recently used URL from history"
  []
  (when-let [history (seq (get-url-history))]
    (:url (first (sort-by :timestamp #(compare %2 %1) history))))) 
 (m/=> get-most-recent-url [:=> [:cat] [:maybe :string]])

(defn get-domain
  "Get the domain from the most recent URL or legacy domain field"
  []
  (let [config (load-config)]
    ;; Try to get domain from most recent URL in history
    (if-let [recent-url (get-most-recent-url)]
      (extract-domain* recent-url)
      ;; Fallback to legacy domain field, or use localhost
      (or (:domain config) "localhost")))) 
 (m/=> get-domain [:=> [:cat] :potatoclient.ui-specs/domain])

(defn add-url-to-history
  "Add a URL to the history with current timestamp, maintaining max 10 unique URLs"
  [url]
  (when (and url (not (clojure.string/blank? url)))
    (let [config (load-config)
          current-history (get-url-history)
          ;; Remove any existing entry for this URL
          filtered-history (vec (remove #(= (:url %) url) current-history))
          ;; Add new entry with current timestamp (as milliseconds)
          new-entry {:url url :timestamp (.getTime (Date.))}
          new-history (conj filtered-history new-entry)
          ;; Keep only the 10 most recent URLs by timestamp
          sorted-history (sort-by :timestamp #(compare %2 %1) new-history)
          trimmed-history (vec (take 10 sorted-history))]
      (save-config! (assoc config :url-history trimmed-history))
      nil))) 
 (m/=> add-url-to-history [:=> [:cat :string] :nil])
