#!/usr/bin/env bb

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[clojure.java.shell :as shell])

(defn compile-kotlin []
  (let [kotlin-files (filter #(.endsWith (.getName %) ".kt")
                             (file-seq (io/file "src/potatoclient/kotlin")))
        kotlin-paths (map #(.getPath %) kotlin-files)
        class-dir "target/classes"
        ;; Simple classpath for now
        cp (str class-dir ":src/potatoclient/java:src:lib/*")]
    (when (seq kotlin-paths)
      (println (str "Compiling " (count kotlin-paths) " Kotlin files..."))
      (let [kotlinc "tools/kotlin-2.2.0/bin/kotlinc"
            result (apply shell/sh 
                         kotlinc
                         "-d" class-dir
                         "-cp" cp
                         "-jvm-target" "17"
                         kotlin-paths)]
        (println "Exit code:" (:exit result))
        (println "\nSTDOUT:")
        (println (:out result))
        (println "\nSTDERR:")
        (println (:err result))))))

(compile-kotlin)