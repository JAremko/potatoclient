(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as io]))

(def lib 'cmd-explorer)
(def version "0.1.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"}))

(defn compile-java [_]
  (println "Compiling Java sources...")
  (b/javac {:src-dirs ["src/java"]
            :class-dir class-dir
            :basis basis
            :javac-opts ["-source" "11" "-target" "11"]}))

(defn compile-pronto [{:keys []}]
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
  (println "Compilation complete!"))