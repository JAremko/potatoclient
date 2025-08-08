(ns build
  "Build script for buff-validate - compiles necessary protobuf files"
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(defn ensure-dir
  "Ensure directory exists"
  [path]
  (let [dir (io/file path)]
    (when-not (.exists dir)
      (.mkdirs dir)
      (println "Created directory:" path))))

(def basis (b/create-basis {:project "deps.edn"}))
(def class-dir "target/classes")

(defn generate-proto
  "Generate Java sources from proto files using protogen"
  [_]
  (println "Generating proto Java sources...")
  (let [result (shell/sh "./scripts/generate-protos.sh" 
                         :dir (System/getProperty "user.dir"))]
    (if (zero? (:exit result))
      (println "Proto generation successful")
      (do
        (println "Proto generation failed:")
        (println (:err result))
        (throw (ex-info "Proto generation failed" {:exit (:exit result)}))))))

(defn compile-proto
  "Compile generated Java sources"
  [_]
  (println "Compiling proto Java sources...")
  (let [java-src-dir "src/java"]
    (if (.exists (io/file java-src-dir))
      (b/javac {:src-dirs [java-src-dir]
                :class-dir class-dir
                :basis basis
                :javac-opts ["-source" "11" "-target" "11"]})
      (do
        (println "Warning: No Java sources found in src/java/")
        (println "Run 'clojure -T:build generate-proto' first")))))

(defn compile-pronto
  "Compile Pronto Java sources"
  [_]
  (println "Compiling Pronto Java sources...")
  (let [pronto-src (str (System/getProperty "user.home") 
                       "/.gitlibs/libs/com.appsflyer/pronto/0fb034bc9c943d6a04177b23eb97436f9ca817f7/src/java")]
    (b/javac {:src-dirs [pronto-src]
              :class-dir class-dir
              :basis basis
              :javac-opts ["-source" "11" "-target" "11"]})))

(defn compile-all
  "Compile all Java sources (Pronto + proto)"
  [_]
  (ensure-dir "target/classes")
  (compile-pronto nil)
  (compile-proto nil)
  (println "All compilation complete!"))

(defn build
  "Main build function - generates and compiles proto files"
  [_]
  (println "Building buff-validate...")
  (generate-proto nil)
  (compile-all nil)
  (println "Build complete!"))

(defn clean
  "Clean all build artifacts"
  [_]
  (println "Cleaning build artifacts...")
  (b/delete {:path "target"})
  (b/delete {:path ".cpcache"})
  (println "Clean complete!"))