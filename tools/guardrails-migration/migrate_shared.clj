(require '[guardrails-migration.zipper-clean :as m])
(require '[clojure.java.io :as io])
(import 'java.io.File)

(defn find-clj-files [dir]
  (filter #(and (.isFile %)
                (.endsWith (.getName %) ".clj")
                (not (.contains (.getPath %) "guardrails-migration")))
          (file-seq (File. dir))))

(def all-clj-files 
  (concat (find-clj-files "../../src")
          (find-clj-files "../../shared/src")))

(println (str "Found " (count all-clj-files) " Clojure files"))

(doseq [file all-clj-files]
  (let [path (.getPath file)
        content (slurp path)]
    (when (or (.contains content ">defn")
              (.contains content ">defn-")
              (.contains content "(>"))
      (print (str "Migrating: " path " ... "))
      (flush)
      (try
        (m/migrate-file path path)
        (println "SUCCESS")
        (catch Exception e
          (println (str "FAILED: " (.getMessage e))))))))