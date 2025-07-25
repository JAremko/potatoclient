(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]))

(def lib 'potatoclient/potatoclient)
(def version (str/trim (slurp "VERSION")))
(def class-dir "target/classes")
(def uber-file (format "target/%s-%s.jar" (name lib) version))

(defn- get-basis 
  "Create basis, optionally including the class-dir if it exists (for CI builds)"
  []
  (let [base-basis (b/create-basis {:project "deps.edn"})
        class-dir-file (io/file class-dir)
        class-dir-exists? (.exists class-dir-file)]
    (println (str "get-basis: class-dir exists? " class-dir-exists? " at " (.getAbsolutePath class-dir-file)))
    (if class-dir-exists?
      (let [abs-path (.getAbsolutePath class-dir-file)
            updated-basis (update base-basis :classpath-roots conj abs-path)]
        (println (str "get-basis: added " abs-path " to classpath"))
        (println (str "get-basis: classpath-roots now: " (:classpath-roots updated-basis)))
        updated-basis)
      base-basis)))

(defn clean [_]
  (b/delete {:path "target"}))

(defn compile-kotlin [_]
  (println "Compiling Kotlin sources...")
  ;; First ensure the kotlin compiler is available
  (let [kotlin-dir "tools/kotlin-2.2.0"
        kotlinc (str kotlin-dir "/bin/kotlinc")
        kotlinc-exists? (.exists (io/file kotlinc))]
    (when-not kotlinc-exists?
      (println "Kotlin compiler not found. Running setup script...")
      (let [setup-result (shell/sh "./scripts/setup-kotlin.sh")]
        (when (not= 0 (:exit setup-result))
          (throw (ex-info "Failed to set up Kotlin" {:output (:out setup-result) :error (:err setup-result)})))))
    
    ;; Get all Kotlin source files
    (let [kotlin-files (filter #(.endsWith (.getName %) ".kt")
                               (file-seq (io/file "src/potatoclient/kotlin")))
          kotlin-paths (map #(.getPath %) kotlin-files)]
      (when (seq kotlin-paths)
        (println (str "Compiling " (count kotlin-paths) " Kotlin files..."))
        (let [result (apply shell/sh 
                           kotlinc
                           "-d" class-dir
                           "-cp" (str/join ":" (:classpath-roots (get-basis)))
                           "-jvm-target" "17"
                           kotlin-paths)]
          (when (not= 0 (:exit result))
            (throw (ex-info "Kotlin compilation failed" {:output (:out result) :error (:err result)})))
          (println "Kotlin compilation successful"))))))

(defn compile-java-proto [_]
  (println "Compiling Java protobuf classes...")
  (b/javac {:src-dirs ["src/potatoclient/java"]
            :class-dir class-dir
            :basis (get-basis)
            :javac-opts ["--release" "17"]})
  (println "Java protobuf compilation successful"))

(defn compile-all [_]
  ;; Compile Kotlin first as Java may depend on Kotlin classes
  (compile-kotlin nil)
  (compile-java-proto nil))

(defn compile-clj [_]
  (b/compile-clj {:basis (get-basis)
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :compile-opts {:elide-meta [:doc :file :line :added]
                                 :direct-linking true}}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (compile-kotlin nil)
  (compile-java-proto nil)
  (compile-clj nil)
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis (get-basis)
           :main 'potatoclient.main}))

(defn release [_]
  "Build a release version with instrumentation disabled and full optimizations."
  (clean nil)
  ;; Generate proto files first
  (println "Generating proto files...")
  (let [result (shell/sh "make" "proto")]
    (when (not= 0 (:exit result))
      (throw (ex-info "Proto generation failed" {:output (:out result) :error (:err result)}))))
  ;; Set environment variable for release build
  (System/setProperty "POTATOCLIENT_RELEASE" "true")
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  ;; Create release marker file
  (spit (io/file class-dir "RELEASE") "true")
  (compile-kotlin nil)
  (compile-java-proto nil)
  ;; Compile with release optimizations
  (b/compile-clj {:basis (get-basis)
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :compile-opts {:elide-meta [:doc :file :line :added]
                                 :direct-linking true}})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis (get-basis)
           :main 'potatoclient.main}))
