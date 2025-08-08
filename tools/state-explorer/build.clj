(ns build
  "Build script for state-explorer - compiles necessary protobuf files"
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(defn ensure-dir
  "Ensure directory exists"
  [path]
  (let [dir (io/file path)]
    (when-not (.exists dir)
      (.mkdirs dir)
      (println "Created directory:" path))))

(defn copy-proto-files
  "Copy proto files from protogen directory"
  []
  (println "Copying proto files...")
  (ensure-dir "proto")
  
  ;; Copy only the state-related proto file we need
  (let [proto-files ["jon_shared_data.proto"
                     "jon_shared_data_camera_day.proto"
                     "jon_shared_data_camera_heat.proto"
                     "jon_shared_data_rotary.proto"
                     "jon_shared_data_compass.proto"
                     "jon_shared_data_gps.proto"
                     "jon_shared_data_lrf.proto"
                     "jon_shared_data_types.proto"
                     "jon_shared_data_time.proto"
                     "jon_shared_data_system.proto"
                     "jon_shared_data_actual_space_time.proto"
                     "jon_shared_data_rec_osd.proto"
                     "jon_shared_data_day_cam_glass_heater.proto"
                     "jon_shared_data_compass_calibration.proto"]
        source-dir "../../examples/protogen/proto"
        target-dir "proto"]
    (doseq [file proto-files]
      (let [source (io/file source-dir file)
            target (io/file target-dir file)]
        (when (.exists source)
          (io/copy source target)
          (println "  Copied" file))))))

(defn download-protoc
  "Download protoc if not available"
  []
  (let [protoc-path "bin/protoc"]
    (if (.exists (io/file protoc-path))
      (println "protoc already available")
      (do
        (println "Downloading protoc...")
        (ensure-dir "bin")
        ;; Download protoc directly
        (let [os (System/getProperty "os.name")
              arch (System/getProperty "os.arch")
              platform (cond
                        (.contains os "Mac") "osx-x86_64"
                        (.contains os "Linux") "linux-x86_64"
                        :else "linux-x86_64")
              url (str "https://github.com/protocolbuffers/protobuf/releases/download/v29.5/"
                      "protoc-29.5-" platform ".zip")
              result (shell/sh "bash" "-c"
                              (str "cd /tmp && "
                                   "wget -q " url " && "
                                   "unzip -q protoc-29.5-" platform ".zip -d protoc-tmp && "
                                   "cp protoc-tmp/bin/protoc " (System/getProperty "user.dir") "/bin/ && "
                                   "chmod +x " (System/getProperty "user.dir") "/bin/protoc && "
                                   "rm -rf protoc-tmp protoc-29.5-" platform ".zip"))]
          (if (zero? (:exit result))
            (println "protoc downloaded successfully")
            (do
              (println "Failed to download protoc, trying to use system protoc...")
              ;; Try to use system protoc if available
              (let [which-result (shell/sh "which" "protoc")]
                (when (zero? (:exit which-result))
                  (shell/sh "ln" "-sf" (str/trim (:out which-result)) "bin/protoc")
                  (println "Using system protoc")))))))))

(defn compile-proto-files
  "Compile proto files to Java"
  []
  (println "Compiling proto files...")
  (ensure-dir "target/classes")
  
  (let [proto-files (filter #(.endsWith (.getName %) ".proto") 
                            (file-seq (io/file "proto")))
        protoc-cmd ["bin/protoc"
                   "--java_out=target/classes"
                   "--proto_path=proto"]]
    
    (doseq [proto-file proto-files]
      (let [cmd (conj protoc-cmd (.getPath proto-file))
            result (apply shell/sh cmd)]
        (if (zero? (:exit result))
          (println "  Compiled" (.getName proto-file))
          (println "  Failed to compile" (.getName proto-file) ":" (:err result)))))))

(defn compile-java-files
  "Compile generated Java files"
  []
  (println "Compiling Java classes...")
  (let [java-files (filter #(.endsWith (.getName %) ".java")
                          (file-seq (io/file "target/classes")))
        class-path (str/join ":" ["target/classes"
                                  (System/getProperty "java.class.path")])]
    
    (when (seq java-files)
      (let [files (map #(.getPath %) java-files)
            cmd (concat ["javac" "-cp" class-path "-d" "target/classes"] files)
            result (apply shell/sh cmd)]
        (if (zero? (:exit result))
          (println "Java compilation successful")
          (println "Java compilation failed:" (:err result)))))))

(defn setup-pronto
  "Set up Pronto dependency"
  []
  (println "Setting up Pronto...")
  ;; For now, we'll skip Pronto and use basic proto parsing
  ;; In the future, we can compile Pronto here
  (println "Pronto setup skipped (will use basic proto parsing)"))

(defn build
  "Main build function"
  []
  (println "Building state-explorer...")
  (copy-proto-files)
  (download-protoc)
  (compile-proto-files)
  (compile-java-files)
  (setup-pronto)
  (println "Build complete!"))

(defn clean
  "Clean build artifacts"
  []
  (println "Cleaning build artifacts...")
  (let [dirs ["target" "proto" "bin"]]
    (doseq [dir dirs]
      (when (.exists (io/file dir))
        (shell/sh "rm" "-rf" dir)
        (println "  Removed" dir))))
  (println "Clean complete!"))

;; Main entry point
(defn -main [& args]
  (case (first args)
    "clean" (clean)
    "build" (build)
    (build)))