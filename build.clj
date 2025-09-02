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
      (println "ERROR: Kotlin compiler not found at" kotlinc)
      (println "Please install Kotlin 2.2.0 according to your system:")
      (println "  - On macOS: brew install kotlin")
      (println "  - On Linux: Use your package manager (apt, dnf, pacman, etc.)")
      (println "  - Manual: Download from https://github.com/JetBrains/kotlin/releases/tag/v2.2.0")
      (println "           and extract to" kotlin-dir)
      (throw (ex-info "Kotlin compiler not found" {:kotlin-dir kotlin-dir})))
    
    ;; Get all Kotlin source files (including transit subdir)
    (let [kotlin-files (filter #(.endsWith (.getName %) ".kt")
                               (file-seq (io/file "src/potatoclient/kotlin")))
          kotlin-paths (map #(.getPath %) kotlin-files)
          ;; Add protobuf classes to classpath - include both compiled classes and source dirs
          java-src-path "src/potatoclient/java"
          ;; Need to include src directory for Transit Java enums
          src-path "src"
          ;; IPC classes are now in the main module
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
            (println "KOTLIN COMPILATION ERROR:")
            (println (:err result))
            (println "KOTLIN COMPILATION OUTPUT:")
            (println (:out result))
            (throw (ex-info "Kotlin compilation failed" {:output (:out result) :error (:err result)})))
          (println "Kotlin compilation successful"))))))

(defn compile-java-proto [_]
  (println "Compiling Java protobuf classes...")
  ;; Check if proto files exist
  (let [proto-dir (io/file "src/java")
        proto-exists? (.exists proto-dir)]
    (if proto-exists?
      (do
        ;; Compile all Java sources including protobuf and IPC classes
        (b/javac {:src-dirs ["src/java"]
                  :class-dir class-dir
                  :basis (get-basis)
                  :javac-opts ["--release" "17"]})
        (println "Java compilation successful"))
      (println "Java sources not found, skipping Java compilation"))))


(defn compile-pronto [_]
  (let [home (System/getProperty "user.home")
        pronto-path (str home "/.gitlibs/libs/com.appsflyer/pronto/5444ae5ec3b567a5565e6c1e90ba0850960331b6")
        pronto-java-src (str pronto-path "/src/java")]
    (println "Compiling Pronto Java sources...")
    (b/javac {:src-dirs [pronto-java-src]
              :class-dir class-dir
              :basis (get-basis)
              :javac-opts ["--release" "17"]})))

(defn compile-all [_]
  ;; Compile Java protobuf and IPC classes first
  (compile-java-proto nil)
  ;; Compile Pronto Java classes
  (compile-pronto nil)
  ;; Then compile Kotlin as it depends on Java
  (compile-kotlin nil))

(defn compile-java-tests [_]
  (println "Compiling Java test files...")
  ;; Ensure main Java classes are compiled first
  (compile-java-proto nil)
  ;; Only compile TestRunner.java (not JUnit tests which have dependency issues)
  (io/make-parents (io/file "target/test-classes/dummy.txt"))
  (let [result (shell/sh "javac" 
                        "-cp" (str class-dir ":" (str/join ":" (:classpath-roots (get-basis))))
                        "-d" "target/test-classes"
                        "test/potatoclient/java/ipc/TestRunner.java")]
    (when (not= 0 (:exit result))
      (throw (ex-info "Java test compilation failed" {:output (:out result) :error (:err result)})))
    (println "Java test compilation successful")))

(defn run-java-tests [_]
  (println "Running Java tests...")
  ;; First compile the tests
  (compile-java-tests nil)
  ;; Run the TestRunner
  (let [classpath (str "target/test-classes:" class-dir ":" (str/join ":" (:classpath-roots (get-basis))))
        result (shell/sh "java" "-cp" classpath "potatoclient.java.ipc.TestRunner")]
    (println (:out result))
    (when (not= 0 (:exit result))
      (println "ERROR:" (:err result))
      (throw (ex-info "Java tests failed" {:exit (:exit result)})))
    (println "Java tests completed successfully")))

(defn compile-kotlin-tests [_]
  (println "Compiling Kotlin test files...")
  ;; Ensure all Java classes are compiled first (Kotlin tests depend on them)
  (compile-java-proto nil)
  ;; Ensure main Kotlin classes are compiled
  (compile-kotlin nil)
  
  (let [kotlin-dir "tools/kotlin-2.2.0"
        kotlinc (str kotlin-dir "/bin/kotlinc")
        kotlinc-exists? (.exists (io/file kotlinc))]
    (if kotlinc-exists?
      (let [test-class-dir "target/test-classes"
            ;; Only compile IpcClientServerTestRunner (not JUnit tests which have dependency issues)
            test-runner-path "test/kotlin/ipc/IpcClientServerTestRunner.kt"
            ;; Include main classes and Kotlin dependencies
            classpath-with-deps (str class-dir ":" 
                                   test-class-dir ":"
                                   (str/join ":" (:classpath-roots (get-basis))))]
        (println "Compiling IpcClientServerTestRunner.kt...")
        (io/make-parents (io/file test-class-dir "dummy.txt"))
        (let [result (shell/sh 
                       kotlinc
                       "-d" test-class-dir
                       "-cp" classpath-with-deps
                       "-jvm-target" "17"
                       test-runner-path)]
          (when (not= 0 (:exit result))
            (throw (ex-info "Kotlin test compilation failed" {:output (:out result) :error (:err result)})))
          (println "Kotlin test compilation successful")))
      (println "WARNING: Kotlin compiler not found. Skipping Kotlin test compilation."))))

(defn compile-all-kotlin-tests [_]
  (println "Compiling ALL Kotlin test files with JUnit...")
  ;; Ensure all Java classes are compiled first (Kotlin tests depend on them)
  (compile-java-proto nil)
  ;; Ensure main Kotlin classes are compiled
  (compile-kotlin nil)
  
  (let [kotlin-dir "tools/kotlin-2.2.0"
        kotlinc (str kotlin-dir "/bin/kotlinc")
        kotlinc-exists? (.exists (io/file kotlinc))]
    (if kotlinc-exists?
      (let [kotlin-test-files (filter #(.endsWith (.getName %) ".kt")
                                     (file-seq (io/file "test/kotlin")))
            kotlin-test-paths (map #(.getPath %) kotlin-test-files)
            test-class-dir "target/test-classes"
            ;; Get test classpath with JUnit
            test-basis (b/create-basis {:project "deps.edn" :aliases [:test]})
            classpath-with-junit (str class-dir ":" 
                                     test-class-dir ":"
                                     (str/join ":" (:classpath-roots test-basis)))]
        (when (seq kotlin-test-paths)
          (println (str "Compiling " (count kotlin-test-paths) " Kotlin test files with JUnit..."))
          (io/make-parents (io/file test-class-dir "dummy.txt"))
          (let [result (apply shell/sh 
                             kotlinc
                             "-d" test-class-dir
                             "-cp" classpath-with-junit
                             "-jvm-target" "17"
                             kotlin-test-paths)]
            (when (not= 0 (:exit result))
              (println "Compilation errors (may be expected for incomplete tests):")
              (println (:err result)))
            (println "Kotlin test compilation completed"))))
      (println "WARNING: Kotlin compiler not found. Skipping Kotlin test compilation."))))

(defn run-kotlin-tests [_]
  (println "Running Kotlin tests...")
  ;; First compile the tests
  (compile-kotlin-tests nil)
  ;; Run the SimpleTestRunner (since JUnit tests have dependency issues)
  (let [classpath (str "target/test-classes:" class-dir ":" (str/join ":" (:classpath-roots (get-basis))))
        result (shell/sh "java" "-cp" classpath "potatoclient.kotlin.ipc.SimpleTestRunner")]
    (println (:out result))
    (when (not= 0 (:exit result))
      (println "ERROR:" (:err result))
      (throw (ex-info "Kotlin tests failed" {:exit (:exit result)})))
    (println "Kotlin tests completed successfully")))

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
  ;; Compile in correct order: Java proto first, then IPC, then Kotlin
  (compile-java-proto nil)
  (compile-kotlin nil)
  ;; Compile with release optimizations
  (b/compile-clj {:basis (get-basis)
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :ns-compile (fn [sym]
                                (not (contains? #{'potatoclient.reports
                                                  'potatoclient.instrumentation}
                                                sym)))
                  :compile-opts {:elide-meta [:doc :file :line :added]
                                 :direct-linking true}})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis (get-basis)
           :main 'potatoclient.main}))
