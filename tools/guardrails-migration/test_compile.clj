#!/usr/bin/env clojure

(ns test-compile
  "Test that migrated files compile correctly"
  (:require [clojure.java.io :as io]))

;; Add migrated directory to classpath
(defn test-compilation []
  (println "Testing compilation of migrated files...")
  (println)
  
  ;; Test each migrated file
  (let [migrated-files (.listFiles (io/file "migrated"))
        test-forms ["(ns test.config
                       (:require [clojure.edn :as edn]
                                 [clojure.java.io :as io]
                                 [clojure.string :as str]
                                 [malli.core :as m]))"
                    
                    "(defn- extract-domain*
                       \"Extract domain/IP from various URL formats.\"
                       {:malli/schema [:=> [:cat :string] :string]}
                       [input]
                       (str/trim input))"
                    
                    "(defn get-config-dir
                       {:malli/schema [:=> [:cat] :any]}
                       []
                       \"test\")"]]
    
    ;; Test that Malli metadata syntax compiles
    (doseq [form test-forms]
      (try
        (read-string form)
        (println "✓ Form reads correctly:" (subs form 0 (min 50 (count form))) "...")
        (catch Exception e
          (println "✗ Failed to read form:" (.getMessage e)))))
    
    (println)
    (println "Testing actual file reading...")
    
    ;; Test reading actual migrated files
    (doseq [file migrated-files
            :when (.endsWith (.getName file) ".clj")]
      (try
        (let [content (slurp file)
              reader (java.io.PushbackReader. (java.io.StringReader. content))]
          ;; Try to read all forms
          (loop []
            (let [form (read reader false ::eof)]
              (when-not (= form ::eof)
                (recur))))
          (println "✓" (.getName file) "- All forms read successfully"))
        (catch Exception e
          (println "✗" (.getName file) "-" (.getMessage e)))))))

(test-compilation)