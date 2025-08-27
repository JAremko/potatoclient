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
  (b/delete {:path "target"})
  (b/delete {:path "src/java"}))

(defn compile-proto-sources
  "Compile proto Java source files to bytecode.
   Assumes proto sources are already generated in src/java."
  [_]
  (let [proto-src-dir (io/file "src/java")]
    (if (.exists proto-src-dir)
      (do
        (println "Compiling proto Java sources to bytecode...")
        (b/javac {:src-dirs ["src/java"]
                  :class-dir class-dir
                  :basis basis
                  :javac-opts ["-source" "17" "-target" "17"]})
        (println "Proto classes compiled successfully"))
      (println "Warning: No proto sources found in src/java"))))

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
              :javac-opts ["-source" "17" "-target" "17"]})))

(defn compile-clj
  "Compile Clojure sources."
  [_]
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :java-opts ["-Dguardrails.enabled=true" "-Dguardrails.throw=true"]}))

(defn compile-all
  "Compile everything needed for tests."
  [_]
  (compile-proto-sources nil)
  (compile-pronto nil)
  (compile-clj nil))

(defn run-tests
  "Run tests."
  [_]
  (compile-all nil)
  (b/process {:command-args ["clojure" "-M:test"]}))
