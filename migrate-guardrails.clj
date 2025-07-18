#!/usr/bin/env bb
;; Batch migration script for converting remaining files to Guardrails

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn add-guardrails-require [content]
  (let [lines (str/split-lines content)
        ns-idx (first (keep-indexed #(when (str/starts-with? %2 "(ns ") %1) lines))
        require-idx (first (keep-indexed #(when (and (> %1 ns-idx)
                                                     (str/includes? %2 "(:require")) %1) lines))]
    (if require-idx
      (let [require-line (nth lines require-idx)
            indent (re-find #"^\s+" require-line)
            new-require (str indent "[com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- >def | ? =>]]")]
        (str/join "\n" (concat (take (inc require-idx) lines)
                               [new-require]
                               (drop (inc require-idx) lines))))
      content)))

(defn convert-defn-to-gdefn [content]
  (-> content
      (str/replace #"\(defn " "(>defn ")
      (str/replace #"\(defn- " "(>defn- ")))

(defn migrate-file [file]
  (println "Migrating:" (.getPath file))
  (let [content (slurp file)
        updated (-> content
                    add-guardrails-require
                    convert-defn-to-gdefn)]
    (when (not= content updated)
      (spit file updated)
      (println "  ✓ Updated"))))

(def files-to-migrate
  ["src/potatoclient/proto.clj"
   "src/potatoclient/ipc.clj"
   "src/potatoclient/logging.clj"
   "src/potatoclient/events/stream.clj"
   "src/potatoclient/ui/control_panel.clj"
   "src/potatoclient/ui/main_frame.clj"
   "src/potatoclient/ui/log_viewer.clj"
   "src/potatoclient/ui/startup_dialog.clj"
   "src/potatoclient/ui/utils.clj"
   "src/potatoclient/runtime.clj"
   "src/potatoclient/core.clj"
   "src/potatoclient/main.clj"])

(println "Starting Guardrails migration...")
(doseq [path files-to-migrate]
  (when-let [file (io/file path)]
    (if (.exists file)
      (migrate-file file)
      (println "  ✗ File not found:" path))))

(println "\nMigration complete!")
(println "\nNOTE: You will need to manually add the proper Guardrails specs to each function.")
(println "Use the patterns from instrumentation.clj as a guide.")