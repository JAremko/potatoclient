(ns build
  "Build script for shared project.
   Handles proto compilation and Java class compilation."
  (:require
   [clojure.tools.build.api :as b]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(def lib 'potatoclient/shared)
(def version "0.1.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"}))

(defn copy-proto-classes
  "Copy pre-compiled proto classes from tools if available."
  [_]
  (let [sources ["../tools/state-explorer/target/classes"
                 "../tools/validate/target/classes"]
        target (io/file class-dir)]
    
    ;; Create target directory
    (.mkdirs target)
    
    (if-let [source (->> sources
                        (map io/file)
                        (filter #(.exists %))
                        first)]
      (do
        (println (format "Copying proto classes from %s" source))
        ;; Copy ser and cmd directories
        (doseq [subdir ["ser" "cmd"]]
          (let [src (io/file source subdir)
                dst (io/file target subdir)]
            (when (.exists src)
              (.mkdirs (.getParentFile dst))
              (b/copy-dir {:src-dirs [(.getPath src)]
                          :target-dir class-dir}))))
        (println "Proto classes copied successfully"))
      (println "Warning: No pre-compiled proto classes found"))))

(defn compile-pronto
  "Compile Pronto Java sources."
  [_]
  (let [home (System/getProperty "user.home")
        pronto-path (str home "/.gitlibs/libs/com.appsflyer/pronto/0fb034bc9c943d6a04177b23eb97436f9ca817f7")
        pronto-java-src (str pronto-path "/src/java")]
    (println "Compiling Pronto Java sources...")
    (b/javac {:src-dirs [pronto-java-src]
              :class-dir class-dir
              :basis basis
              :javac-opts ["-source" "11" "-target" "11"]})))

(defn compile-clj
  "Compile Clojure sources."
  [_]
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir}))

(defn compile-all
  "Compile everything needed for tests."
  [_]
  (clean nil)
  (copy-proto-classes nil)
  (compile-pronto nil)
  (compile-clj nil))

(defn run-tests
  "Run tests."
  [_]
  (compile-all nil)
  (b/process {:command-args ["clojure" "-M:test"]}))