(ns build
  "Build script for cmd-explorer"
  (:require [clojure.java.io :as io]
            [clojure.tools.build.api :as b]))

(def lib 'cmd-explorer)
(def version "0.1.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean
  "Clean build artifacts"
  [_]
  (println "Cleaning build artifacts...")
  (b/delete {:path "target"})
  (println "Clean complete!"))

(defn build
  "Build function - proto classes now come from shared project"
  [_]
  (println "Building cmd-explorer...")
  (println "Proto classes are provided by potatoclient/shared dependency")
  (println "Build complete!"))