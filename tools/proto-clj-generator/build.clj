(ns build
  "Build tasks for proto-clj-generator"
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def lib 'potatoclient/proto-clj-generator)
(def version "0.1.0")
(def class-dir "target/classes")
(def java-src-dir "src/java")

(defn clean [_]
  (b/delete {:path "target"}))

(defn generate-proto-java
  "Generate Java protobuf sources using the main project's infrastructure"
  [_]
  (println "Generating Java protobuf sources...")
  (let [result (b/process {:command-args ["./scripts/generate-proto-java.sh"]})]
    (when-not (zero? (:exit result))
      (throw (ex-info "Failed to generate Java sources" result))))
  (println "Java source generation complete"))

(defn compile-java-proto
  "Compile Java protobuf classes"
  [_]
  (println "Compiling Java protobuf classes...")
  (b/delete {:path class-dir})
  ;; First ensure we have the Java sources
  (when-not (.exists (io/file java-src-dir))
    (println "Java sources not found, generating...")
    (generate-proto-java nil))
  ;; Compile the Java sources
  (b/javac {:src-dirs [java-src-dir]
            :class-dir class-dir
            :basis (b/create-basis {:project "deps.edn"})
            :javac-opts ["-source" "11" "-target" "11"]})
  (println "Java protobuf compilation complete"))

(defn compile-all
  "Compile all sources"
  [_]
  (compile-java-proto nil))

(defn test
  "Run tests with compiled proto classes"
  [_]
  (compile-all nil)
  (println "Running tests...")
  (b/process {:command-args ["clojure" "-X:test"]}))

(defn build
  "Build the project"
  [_]
  (clean nil)
  (compile-all nil)
  (println "Build complete"))