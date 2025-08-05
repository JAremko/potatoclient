(ns generator.core
  "Core code generation logic for protobuf conversion."
  (:require [generator.parser :as parser]
            [generator.working-gen :as templates :refer [generate-code]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

;; =============================================================================
;; Import Generation
;; =============================================================================

(defn collect-imports
  "Collect all Java imports needed for the generated code."
  [messages enums]
  (let [;; Get unique Java packages
        message-imports (distinct
                          (keep (fn [msg]
                                  (when-let [java-class (:java-class msg)]
                                    (let [parts (str/split java-class #"\.")]
                                      (vec (butlast parts)))))
                                messages))
        enum-imports (distinct
                       (keep (fn [enum]
                               (when-let [java-class (:java-class enum)]
                                 (let [parts (str/split java-class #"\.")]
                                   (vec (butlast parts)))))
                             enums))
        ;; Combine and dedupe
        all-packages (distinct (concat message-imports enum-imports))]
    ;; Group by package for cleaner imports
    (map (fn [pkg-parts]
           (let [pkg (str/join "." pkg-parts)
                 ;; Find all classes in this package
                 msg-classes (filter #(str/starts-with? (:java-class %) (str pkg "."))
                                   messages)
                 enum-classes (filter #(str/starts-with? (:java-class %) (str pkg "."))
                                    enums)]
             ;; Create import statement [package Class1 Class2 ...]
             (concat
               [pkg]
               (map #(last (str/split (:java-class %) #"\.")) msg-classes)
               (map #(last (str/split (:java-class %) #"\.")) enum-classes))))
         all-packages)))

;; =============================================================================
;; File Processing
;; =============================================================================

(defn process-descriptor-file
  "Process a single descriptor file and extract relevant messages/enums."
  [file-path target-messages target-enums]
  (log/info "Processing descriptor file:" file-path)
  (let [descriptor (parser/parse-single-descriptor file-path)
        ;; Filter to only the messages/enums we care about
        relevant-messages (if target-messages
                           (filter #(contains? target-messages (:name %))
                                  (:messages descriptor))
                           (:messages descriptor))
        relevant-enums (if target-enums
                        (filter #(contains? target-enums (:name %))
                               (:enums descriptor))
                        (:enums descriptor))]
    {:messages relevant-messages
     :enums relevant-enums}))

(defn process-command-descriptors
  "Process command descriptor files to extract Root message and dependencies."
  [descriptor-dir]
  (let [cmd-file (io/file descriptor-dir "jon_shared_cmd.json")
        ;; First get the Root message
        root-desc (process-descriptor-file (.getPath cmd-file) #{"Root"} nil)
        root-msg (first (:messages root-desc))
        ;; Extract oneof fields to know which other files to process
        oneof-fields (when root-msg
                      (mapcat :fields (:oneofs root-msg)))
        ;; Map field types to descriptor files
        type-to-file {"DayCamera.Root" "jon_shared_cmd_day_camera.json"
                      "HeatCamera.Root" "jon_shared_cmd_heat_camera.json"
                      "Gps.Root" "jon_shared_cmd_gps.json"
                      "Compass.Root" "jon_shared_cmd_compass.json"
                      "Lrf.Root" "jon_shared_cmd_lrf.json"
                      "RotaryPlatform.Root" "jon_shared_cmd_rotary.json"
                      "OSD.Root" "jon_shared_cmd_osd.json"
                      "System.Root" "jon_shared_cmd_system.json"
                      "CV.Root" "jon_shared_cmd_cv.json"}
        ;; Process each referenced file
        sub-messages (mapcat
                       (fn [field]
                         (when-let [file-name (get type-to-file 
                                                  (last (str/split (:type-name field) #"\.")))]
                           (let [file-path (io/file descriptor-dir file-name)]
                             (when (.exists file-path)
                               (let [desc (process-descriptor-file (.getPath file-path) nil nil)]
                                 (concat (:messages desc) (:enums desc)))))))
                       oneof-fields)]
    {:root root-msg
     :messages (concat [root-msg] (filter map? sub-messages))
     :enums (filter #(contains? % :values) sub-messages)}))

(defn process-state-descriptors
  "Process state descriptor files."
  [descriptor-dir]
  (let [state-file (io/file descriptor-dir "jon_shared_data.json")
        desc (process-descriptor-file (.getPath state-file) nil nil)]
    desc))

;; =============================================================================
;; Code Generation
;; =============================================================================

(defn generate-namespace
  "Generate a complete namespace with all conversions."
  [{:keys [ns-name messages enums]}]
  (let [imports (collect-imports messages enums)]
    (generate-code
      {:ns-name ns-name
       :messages messages
       :enums enums
       :imports imports})))

(defn write-namespace
  "Write generated namespace to file."
  [output-dir ns-name content]
  (let [;; Convert namespace to file path
        ns-parts (str/split (str ns-name) #"\.")
        file-name (str (last ns-parts) ".clj")
        dir-parts (butlast ns-parts)
        dir-path (apply io/file output-dir dir-parts)
        file-path (io/file dir-path file-name)]
    ;; Create directories if needed
    (.mkdirs dir-path)
    ;; Write file
    (spit file-path content)
    (log/info "Generated" (.getPath file-path))
    file-path))

;; =============================================================================
;; Main Generation Functions
;; =============================================================================

(defn generate-command-converters
  "Generate command conversion namespace."
  [{:keys [input-dir output-dir namespace-prefix]}]
  (log/info "Generating command converters...")
  (let [cmd-data (process-command-descriptors input-dir)
        ns-name (str namespace-prefix ".command")
        content (generate-namespace
                  {:ns-name ns-name
                   :messages (:messages cmd-data)
                   :enums (:enums cmd-data)})]
    (write-namespace output-dir ns-name content)))

(defn generate-state-converters
  "Generate state conversion namespace."
  [{:keys [input-dir output-dir namespace-prefix]}]
  (log/info "Generating state converters...")
  (let [state-data (process-state-descriptors input-dir)
        ns-name (str namespace-prefix ".state")
        content (generate-namespace
                  {:ns-name ns-name
                   :messages (:messages state-data)
                   :enums (:enums state-data)})]
    (write-namespace output-dir ns-name content)))

(defn generate-all
  "Generate all protobuf conversion code."
  [{:keys [input-dir output-dir namespace-prefix] :as config}]
  (log/info "Starting protobuf code generation...")
  (log/info "Input directory:" input-dir)
  (log/info "Output directory:" output-dir)
  (log/info "Namespace prefix:" namespace-prefix)
  
  ;; Validate inputs
  (when-not (.exists (io/file input-dir))
    (throw (ex-info "Input directory does not exist"
                    {:input-dir input-dir})))
  
  ;; Create output directory
  (.mkdirs (io/file output-dir))
  
  ;; Generate converters
  (let [cmd-file (generate-command-converters config)
        state-file (generate-state-converters config)]
    (log/info "Code generation complete!")
    {:command-file cmd-file
     :state-file state-file}))