#!/usr/bin/env bb
;; Babashka script to check test compilation without running tests

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(defn find-test-files []
  (let [test-dir (io/file "test")]
    (when (.exists test-dir)
      (->> (file-seq test-dir)
           (filter #(.endsWith (.getName %) "_test.clj"))
           (map #(.getPath %))))))

(defn check-file-compiles [file-path]
  (println (str "Checking: " file-path))
  (try
    (let [result (shell/sh "clojure" "-M" "-e" 
                          (format "(compile '%s)" 
                                  (-> file-path
                                      (str/replace #"^test/" "")
                                      (str/replace #"\.clj$" "")
                                      (str/replace #"/" ".")
                                      (str/replace #"_" "-"))))]
      (if (zero? (:exit result))
        {:file file-path :status :ok}
        {:file file-path 
         :status :error 
         :message (:err result)}))
    (catch Exception e
      {:file file-path 
       :status :error 
       :message (.getMessage e)})))

(println "\n🔍 Checking test files for compilation errors...\n")

(let [test-files (find-test-files)
      results (map check-file-compiles test-files)
      errors (filter #(= :error (:status %)) results)]
  
  (println (format "\n✅ Files checked: %d" (count test-files)))
  (println (format "✅ Compiled successfully: %d" 
                   (count (filter #(= :ok (:status %)) results))))
  
  (if (empty? errors)
    (do 
      (println "🎉 All test files compile successfully!")
      (System/exit 0))
    (do
      (println (format "\n❌ Compilation errors found: %d\n" (count errors)))
      (doseq [error errors]
        (println (format "File: %s" (:file error)))
        (println (format "Error: %s\n" (:message error))))
      (System/exit 1))))