(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def lib 'potatoclient/potatoclient)
(def version (str/trim (slurp "VERSION")))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn compile-java [_]
  (b/javac {:src-dirs ["src/potatoclient/java"]
            :class-dir class-dir
            :basis basis
            :javac-opts ["--release" "17"]}))

(defn compile-clj [_]
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :compile-opts {:elide-meta [:doc :file :line :added]
                                 :direct-linking true}}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (compile-java nil)
  (compile-clj nil)
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'potatoclient.main}))

(defn release [_]
  "Build a release version with instrumentation disabled and full optimizations."
  (clean nil)
  ;; Set environment variable for release build
  (System/setProperty "POTATOCLIENT_RELEASE" "true")
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (compile-java nil)
  ;; Compile with release optimizations
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :compile-opts {:elide-meta [:doc :file :line :added]
                                 :direct-linking true}})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'potatoclient.main}))
