(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'proto-explorer/proto-explorer)
(def version "0.1.0")
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s.jar" (name lib) version))

;; clean target
(defn clean [_]
  (b/delete {:path "target"}))

(defn copy-specs [_]
  "Copy spec files to class dir"
  (println "Copying spec files...")
  ;; Copy protobuf specs
  (b/copy-dir {:src-dirs ["../../shared/specs/protobuf"]
               :target-dir (str class-dir "/potatoclient/specs")})
  (println "Spec files copied successfully"))

;; build uberjar
(defn uberjar [_]
  (clean nil)
  ;; Copy source and resources
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  ;; Copy external specs
  (copy-specs nil)
  
  ;; Create basis without external paths
  (let [basis (b/create-basis {:project "deps.edn"})]
    ;; Compile Java files
    (b/javac {:src-dirs ["src"]
              :class-dir class-dir
              :basis basis})
    
    ;; Compile Clojure
    (b/compile-clj {:basis basis
                    :src-dirs ["src"]
                    :class-dir class-dir
                    :ns-compile '[proto-explorer.main]})
    
    (b/uber {:class-dir class-dir
             :uber-file uber-file
             :basis basis
             :main 'proto-explorer.main}))
  
  (println "Uberjar created:" uber-file))