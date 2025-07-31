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
    (if class-dir-exists?
      (let [abs-path (.getAbsolutePath class-dir-file)
            updated-basis (update base-basis :classpath-roots conj abs-path)]
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
    
    ;; Get all Kotlin source files (including transit subdir)
    (let [kotlin-files (filter #(.endsWith (.getName %) ".kt")
                               (file-seq (io/file "src/potatoclient/kotlin")))
          kotlin-paths (map #(.getPath %) kotlin-files)
          ;; Add protobuf classes to classpath - include both compiled classes and source dirs
          java-src-path "src/potatoclient/java"
          ;; Need to include src directory for Transit Java enums
          src-path "src"
          classpath-with-proto (str class-dir ":" java-src-path ":" src-path ":" (str/join ":" (:classpath-roots (get-basis))))]
      (when (seq kotlin-paths)
        (println (str "Compiling " (count kotlin-paths) " Kotlin files..."))
        ;; kotlinc needs to be redefined here since it's out of scope from the outer let
        (let [kotlinc (str "tools/kotlin-2.2.0/bin/kotlinc")
              result (apply shell/sh 
                           kotlinc
                           "-d" class-dir
                           "-cp" classpath-with-proto
                           "-jvm-target" "17"
                           kotlin-paths)]
          (when (not= 0 (:exit result))
            (throw (ex-info "Kotlin compilation failed" {:output (:out result) :error (:err result)})))
          (println "Kotlin compilation successful"))))))

(defn compile-java-proto [_]
  (println "Compiling Java protobuf classes...")
  ;; Only compile the generated protobuf files, not old WebSocketManager
  (b/javac {:src-dirs ["src/potatoclient/java/cmd" "src/potatoclient/java/ser"]
            :class-dir class-dir
            :basis (get-basis)
            :javac-opts ["--release" "17"]})
  (println "Java protobuf compilation successful"))

(defn compile-java-enums [_]
  (println "Compiling Java enum classes...")
  ;; Compile the Transit Java enums
  (b/javac {:src-dirs ["src/potatoclient/transit"]
            :class-dir class-dir
            :basis (get-basis)
            :javac-opts ["--release" "17"]})
  (println "Java enum compilation successful"))

(defn compile-all [_]
  ;; Compile Java protobuf first as Kotlin Transit classes depend on protobuf
  (compile-java-proto nil)
  ;; Compile Java enums that both Clojure and Kotlin use
  (compile-java-enums nil)
  (compile-kotlin nil))

(defn compile-kotlin-tests [_]
  (println "Compiling Kotlin test files...")
  (let [kotlin-dir "tools/kotlin-2.2.0"
        kotlinc (str kotlin-dir "/bin/kotlinc")
        kotlinc-exists? (.exists (io/file kotlinc))]
    (if kotlinc-exists?
      (let [kotlin-test-files (filter #(.endsWith (.getName %) ".kt")
                                     (file-seq (io/file "test/kotlin")))
            kotlin-test-paths (map #(.getPath %) kotlin-test-files)
            test-class-dir "target/test-classes"
            ;; Include main classes, protobuf, JUnit and Kotlin test dependencies
            classpath-with-deps (str class-dir ":" 
                                   test-class-dir ":"
                                   "src/potatoclient/java" ":"
                                   (str/join ":" (:classpath-roots (get-basis))))]
        (when (seq kotlin-test-paths)
          (println (str "Compiling " (count kotlin-test-paths) " Kotlin test files..."))
          (io/make-parents (io/file test-class-dir "dummy.txt"))
          (let [result (apply shell/sh 
                             kotlinc
                             "-d" test-class-dir
                             "-cp" classpath-with-deps
                             "-jvm-target" "17"
                             kotlin-test-paths)]
            (when (not= 0 (:exit result))
              (throw (ex-info "Kotlin test compilation failed" {:output (:out result) :error (:err result)})))
            (println "Kotlin test compilation successful"))))
      (println "WARNING: Kotlin compiler not found. Skipping Kotlin test compilation."))))

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
  ;; Use compile-all to ensure correct order
  (compile-all nil)
  (compile-clj nil)
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis (get-basis)
           :main 'potatoclient.main}))

(defn release [_]
  "Build a release version with instrumentation disabled and full optimizations."
  (clean nil)
  ;; Generate proto files first (skip if they already exist)
  (let [proto-dir (io/file "src/potatoclient/java/cmd")
        proto-exists? (.exists proto-dir)]
    (if proto-exists?
      (println "Proto files already exist, skipping generation...")
      (do
        (println "Generating proto files...")
        (let [result (shell/sh "make" "proto")]
          (when (not= 0 (:exit result))
            (throw (ex-info "Proto generation failed" {:output (:out result) :error (:err result)})))))))
  ;; Set environment variable for release build
  (System/setProperty "POTATOCLIENT_RELEASE" "true")
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  ;; Create release marker file
  (spit (io/file class-dir "RELEASE") "true")
  ;; Compile in correct order: Java proto first, then enums, then Kotlin
  (compile-java-proto nil)
  (compile-java-enums nil)
  (compile-kotlin nil)
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
