#!/usr/bin/env clojure

(ns migrate
  "Batch migration script for Guardrails to Malli"
  (:require [guardrails-migration.simple :as m]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def files-to-migrate
  "Files identified by the analyzer as containing Guardrails"
  ["src/potatoclient/config.clj"
   "src/potatoclient/state.clj"
   "src/potatoclient/theme.clj"
   "src/potatoclient/ui/startup_dialog.clj"
   "src/potatoclient/ui/main_frame.clj"
   "src/potatoclient/logging.clj"
   "src/potatoclient/i18n.clj"
   "src/potatoclient/main.clj"
   "src/potatoclient/dev_instrumentation.clj"
   "src/potatoclient/ui_specs.clj"
   "shared/src/potatoclient/malli/registry.clj"])

(defn migrate-all
  "Migrate all files, optionally with dry-run"
  [& {:keys [dry-run output-dir]
      :or {dry-run false
           output-dir "migrated"}}]
  (println (if dry-run
             "DRY RUN - No files will be modified"
             (str "Migrating files to " output-dir)))
  (println)
  
  (when-not dry-run
    (.mkdirs (io/file output-dir)))
  
  (doseq [file files-to-migrate]
    (let [input-file (io/file "../../" file)
          output-file (if dry-run
                        nil
                        (io/file output-dir (.getName input-file)))]
      (if (.exists input-file)
        (do
          (println (str "Processing: " file))
          (when-not dry-run
            (let [result (m/migrate-file (.getPath input-file) (.getPath output-file))]
              (if (= :success (:status result))
                (println (str "  ✓ Migrated to: " (.getPath output-file)))
                (println (str "  ✗ Error: " (:error result)))))))
        (println (str "  ✗ File not found: " file)))))
  
  (println)
  (println "Migration complete!")
  
  (when-not dry-run
    (println)
    (println "Next steps:")
    (println "1. Review migrated files in" output-dir)
    (println "2. Test that they compile: clojure -M -e '(compile 'namespace)'")
    (println "3. Replace original files if tests pass")
    (println "4. Run 'make test' to verify everything works")))

(defn -main [& args]
  (cond
    (some #{"--dry-run"} args)
    (migrate-all :dry-run true)
    
    (some #{"--help"} args)
    (do
      (println "Usage: clojure -M migrate.clj [options]")
      (println "Options:")
      (println "  --dry-run   Show what would be migrated without modifying files")
      (println "  --help      Show this help message")
      (println "  --output DIR  Specify output directory (default: 'migrated')"))
    
    :else
    (let [output-idx (.indexOf (vec args) "--output")
          output-dir (if (>= output-idx 0)
                       (get args (inc output-idx))
                       "migrated")]
      (migrate-all :output-dir output-dir))))

;; Run if invoked directly
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))