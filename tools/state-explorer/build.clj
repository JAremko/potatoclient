(ns build
  "Build script for state-explorer"
  (:require [clojure.java.io :as io]
            [clojure.tools.build.api :as b]))

(def basis (b/create-basis {:project "deps.edn"}))
(def class-dir "target/classes")

(defn clean
  "Clean build artifacts"
  [_]
  (println "Cleaning build artifacts...")
  (b/delete {:path "target"})
  (println "Clean complete!"))

(defn build
  "Build function - proto classes now come from shared project"
  [_]
  (println "Building state-explorer...")
  (println "Proto classes are provided by potatoclient/shared dependency")
  (println "Build complete!"))