(ns potatoclient.config
  "Configuration management for PotatoClient"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [potatoclient.theme :as theme]))

(def ^:private config-file-name "potatoclient-config.edn")

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
                        :domain "sych.local"
                        :locale :english}]
    (if (.exists ^java.io.File config-file)
      (try
        (-> config-file
            slurp
            edn/read-string
            (merge default-config))
        (catch Exception e
          (println "Error loading config:" (.getMessage ^Exception e))
          default-config))
      default-config)))

(defn save-config!
  "Save configuration to file"
  [config]
  (try
    (ensure-config-dir!)
    (let [config-file (get-config-file)]
      (spit config-file (pr-str config)))
    true
    (catch Exception e
      (println "Error saving config:" (.getMessage ^Exception e))
      false)))

(defn get-theme
  "Get the saved theme from config"
  []
  (:theme (load-config)))

(defn save-theme!
  "Save the current theme to config"
  [theme-key]
  (let [config (load-config)]
    (save-config! (assoc config :theme theme-key))))

(defn get-domain
  "Get the saved domain from config"
  []
  (:domain (load-config)))

(defn save-domain!
  "Save the domain to config"
  [domain]
  (let [config (load-config)]
    (save-config! (assoc config :domain domain))))

(defn get-locale
  "Get the saved locale from config"
  []
  (:locale (load-config)))

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
  (let [config (load-config)]
    ;; Log config location on first run
    (println "Configuration file location:" (get-config-location))
    ;; Initialize theme from saved config
    (theme/initialize-theme! (:theme config))
    ;; Set initial locale
    (when-let [locale (:locale config)]
      (require '[potatoclient.state :as state])
      ((resolve 'potatoclient.state/set-locale!) locale))
    ;; Return the loaded config
    config))