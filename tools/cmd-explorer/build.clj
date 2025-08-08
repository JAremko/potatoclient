(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]))

(def lib 'cmd-explorer)
(def version "0.1.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"}))

(defn generate-proto [_]
  (println "Generating proto Java sources...")
  (let [result (shell/sh "./scripts/generate-protos.sh" 
                         :dir (System/getProperty "user.dir"))]
    (if (zero? (:exit result))
      (println "Proto generation successful")
      (do
        (println "Proto generation failed:")
        (println (:err result))
        (throw (ex-info "Proto generation failed" {:exit (:exit result)}))))))

(defn compile-proto [_]
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

(defn compile-pronto [_]
  (println "Compiling Pronto Java sources...")
  (let [pronto-src (str (System/getProperty "user.home") 
                       "/.gitlibs/libs/com.appsflyer/pronto/0fb034bc9c943d6a04177b23eb97436f9ca817f7/src/java")]
    (b/javac {:src-dirs [pronto-src]
              :class-dir class-dir
              :basis basis
              :javac-opts ["-source" "11" "-target" "11"]})))

(defn compile-all [_]
  (clean nil)
  (compile-pronto nil)
  (compile-proto nil)
  (println "Compilation complete!"))