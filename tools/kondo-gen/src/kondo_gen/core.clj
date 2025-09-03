(ns kondo-gen.core
  "Generate clj-kondo configs from Malli function schemas.
   
   This tool collects all Malli function schemas from the codebase
   and generates clj-kondo type configurations for static analysis."
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [kondo-gen.discovery :as discovery]
            [malli.clj-kondo :as mc]
            [malli.core :as m]
            [malli.instrument :as mi]
            [malli.registry :as mr]
            [potatoclient.init :as init])
  (:gen-class))

(def ^:private namespaces-with-schemas
  "List of namespaces that contain Malli function schemas."
  ['potatoclient.cmd.builder
   'potatoclient.cmd.cam-day-glass-heater
   'potatoclient.cmd.compass
   'potatoclient.cmd.core
   'potatoclient.cmd.cv
   'potatoclient.cmd.day-camera
   'potatoclient.cmd.gps
   'potatoclient.cmd.heat-camera
   'potatoclient.cmd.lrf-alignment
   'potatoclient.cmd.lrf
   'potatoclient.cmd.osd
   'potatoclient.cmd.root
   'potatoclient.cmd.rotary
   'potatoclient.cmd.system
   'potatoclient.cmd.validation
   'potatoclient.config
   'potatoclient.i18n
   'potatoclient.init
   'potatoclient.ipc.core
   'potatoclient.ipc.handlers
   'potatoclient.ipc.transit
   'potatoclient.logging
   'potatoclient.main
   'potatoclient.proto.deserialize
   'potatoclient.proto.serialize
   'potatoclient.reports
   'potatoclient.runtime
   'potatoclient.state
   'potatoclient.state.server.core
   'potatoclient.streams.config
   'potatoclient.streams.coordinator
   'potatoclient.streams.core
   'potatoclient.streams.events
   'potatoclient.streams.process
   'potatoclient.streams.state
   'potatoclient.theme
   'potatoclient.ui.bind-group
   'potatoclient.ui.control-panel
   'potatoclient.ui.debounce
   'potatoclient.ui.frames.connection.core
   'potatoclient.ui.frames.initial.core
   'potatoclient.ui.help.about
   'potatoclient.ui.help.menu
   'potatoclient.ui.log-viewer
   'potatoclient.ui.main-frame
   'potatoclient.ui.menu-bar
   'potatoclient.ui.status-bar.core
   'potatoclient.ui.status-bar.helpers
   'potatoclient.ui.status-bar.messages
   'potatoclient.ui.status-bar.validation
   'potatoclient.ui.tabs
   'potatoclient.ui.tabs-helpers
   'potatoclient.ui.tabs-windows
   'potatoclient.ui.utils
   'potatoclient.url-parser])

(defn load-namespaces!
  "Load all namespaces that contain Malli schemas.
   First tries to discover namespaces dynamically, then falls back to hardcoded list."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (println "Discovering namespaces with schemas...")
  (let [discovered (try 
                     (discovery/discover-project-namespaces)
                     (catch Exception e
                       (println (str "⚠ Discovery failed: " (.getMessage e) ", using hardcoded list"))
                       nil))
        namespaces (or discovered namespaces-with-schemas)]
    (println (str "Found " (count namespaces) " namespaces to load"))
    (println "Loading namespaces...")
    (doseq [ns-sym namespaces]
      (try
        (require ns-sym)
        (print ".")
        (flush)
        (catch Exception e
          (println (str "\n⚠ Warning: Could not load " ns-sym ": " (.getMessage e))))))
    (println "\n✓ Namespaces loaded")))

(defn collect-schemas!
  "Collect all function schemas from loaded namespaces."
  {:malli/schema [:=> [:cat] :int]}
  []
  (println "Collecting function schemas...")
  (let [all-nss (all-ns)
        collected (atom [])
        errors (atom [])]
    ;; Collect schemas namespace by namespace to handle errors gracefully
    (doseq [ns all-nss]
      (try
        (let [ns-collected (mi/collect! {:ns [ns]})]
          (swap! collected concat ns-collected))
        (catch Exception e
          (swap! errors conj {:ns ns :error (.getMessage e)}))))
    
    ;; Report errors if any
    (when (seq @errors)
      (println (str "⚠ Warning: Failed to collect schemas from " (count @errors) " namespaces:"))
      (doseq [{:keys [ns error]} (take 5 @errors)]
        (println (str "  - " ns ": " error)))
      (when (> (count @errors) 5)
        (println (str "  ... and " (- (count @errors) 5) " more"))))
    
    (println (str "✓ Collected schemas from " (count @collected) " functions"))
    (count @collected)))

(defn generate-configs!
  "Generate clj-kondo configurations from collected schemas."
  {:malli/schema [:=> [:cat [:? :string]] :nil]}
  ([]
   (generate-configs! ".clj-kondo"))
  ([output-dir]
   (let [config-dir (io/file output-dir)
         _ (println (str "Generating clj-kondo configs in " (.getAbsolutePath config-dir) "..."))
         schemas (m/function-schemas)]
     
     (if (empty? schemas)
       (println "⚠ No function schemas found! Make sure schemas are collected first.")
       (do
         ;; Create directory if it doesn't exist
         (.mkdirs config-dir)
         
         ;; Generate and write configs
         (mc/emit! {:config-dir (.getPath config-dir)})
         
         ;; Also generate a summary file
         (let [summary-file (io/file config-dir "malli-schemas.edn")]
           (spit summary-file
                 (with-out-str
                   (pprint/pprint
                    {:generated-at (java.util.Date.)
                     :schema-count (count schemas)
                     :namespaces (distinct (map (comp :ns val) schemas))})))
           (println (str "✓ Generated configs for " (count schemas) " functions"))
           (println (str "✓ Summary written to " (.getPath summary-file)))))))))

(defn verify-configs
  "Verify that generated configs are valid."
  {:malli/schema [:=> [:cat :string] :boolean]}
  [config-dir]
  (let [config-file (io/file config-dir "config.edn")]
    (if (.exists config-file)
      (try
        (let [config (read-string (slurp config-file))]
          (println (str "✓ Config file is valid with " 
                       (count (:linters config)) " linters configured"))
          true)
        (catch Exception e
          (println (str "✗ Config file is invalid: " (.getMessage e)))
          false))
      (do
        (println "✗ Config file does not exist")
        false))))

(defn -main
  "Main entry point for kondo-gen tool.
   
   Usage:
     clojure -M:run              # Generate configs in .clj-kondo
     clojure -M:run <output-dir> # Generate configs in custom directory
     clojure -M:run --discover   # Show discovered namespaces and exit"
  {:malli/schema [:=> [:cat [:* :string]] :nil]}
  [& args]
  (println "\n=== Clj-Kondo Config Generator ===\n")
  
  ;; Check for --discover flag
  (when (= "--discover" (first args))
    (println "Discovering namespaces with Malli schemas...")
    (let [namespaces (discovery/discover-project-namespaces)]
      (println (str "\nFound " (count namespaces) " namespaces with schemas:\n"))
      (doseq [ns-sym namespaces]
        (println (str "  " ns-sym)))
      (System/exit 0)))
  
  (try
    ;; Initialize registry only (not full application initialization)
    (print "Initializing Malli registry...")
    (flush)
    (init/ensure-registry!)
    (println " ✓")
    
    ;; Load namespaces
    (load-namespaces!)
    
    ;; Collect schemas
    (let [schema-count (collect-schemas!)]
      (if (zero? schema-count)
        (do
          (println "\n✗ No schemas found - cannot generate configs")
          (System/exit 1))
        (do
          ;; Generate configs
          (if-let [output-dir (first args)]
            (generate-configs! output-dir)
            (generate-configs!))
          
          ;; Verify
          (let [config-dir (or (first args) ".clj-kondo")]
            (println "\nVerifying generated configs...")
            (if (verify-configs config-dir)
              (do
                (println "\n✓ Clj-kondo configs generated successfully!")
                (println (str "  Location: " (.getAbsolutePath (io/file config-dir))))
                (println "  You can now use clj-kondo for static analysis")
                (System/exit 0))
              (do
                (println "\n✗ Config generation failed")
                (System/exit 1)))))))
    
    (catch Exception e
      (println (str "\n✗ Error: " (.getMessage e)))
      (.printStackTrace e)
      (System/exit 1))))