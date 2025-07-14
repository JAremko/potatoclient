(ns potatoclient.config
  "Configuration management for PotatoClient"
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [potatoclient.theme :as theme]))

(def ^:private config-file-name "potatoclient-config.edn")

(defn- get-config-dir
  "Get the configuration directory path"
  []
  (let [user-home (System/getProperty "user.home")
        os-name (.toLowerCase (System/getProperty "os.name"))]
    (cond
      (.contains os-name "win") (io/file user-home "AppData" "Local" "PotatoClient")
      (.contains os-name "mac") (io/file user-home "Library" "Application Support" "PotatoClient")
      :else (io/file user-home ".config" "potatoclient"))))

(defn- get-config-file
  "Get the configuration file"
  []
  (io/file (get-config-dir) config-file-name))

(defn- ensure-config-dir!
  "Ensure the configuration directory exists"
  []
  (let [config-dir (get-config-dir)]
    (when-not (.exists config-dir)
      (.mkdirs config-dir))))

(defn load-config
  "Load configuration from file, return default if not found"
  []
  (let [config-file (get-config-file)
        default-config {:theme :sol-dark
                        :domain "sych.local"}]
    (if (.exists config-file)
      (try
        (-> config-file
            slurp
            edn/read-string
            (merge default-config))
        (catch Exception e
          (println "Error loading config:" (.getMessage e))
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
      (println "Error saving config:" (.getMessage e))
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

(defn initialize!
  "Initialize configuration system"
  []
  (let [config (load-config)]
    ;; Initialize theme from saved config
    (theme/initialize-theme! (:theme config))
    ;; Return the loaded config
    config))