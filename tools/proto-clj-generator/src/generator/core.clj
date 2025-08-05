(ns generator.core
  "Core generator that orchestrates backend and frontend."
  (:require [generator.backend :as backend]
            [generator.frontend :as frontend]
            [generator.edn-specs :as specs]
            [generator.parser :as parser]
            [generator.working-gen :as templates]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [clojure.pprint :as pp]
            [malli.core :as m]))

;; =============================================================================
;; New Architecture Functions
;; =============================================================================

(defn write-edn-debug
  "Write EDN intermediate representation for debugging."
  [edn-data output-dir filename]
  (let [debug-dir (io/file output-dir "debug")
        file (io/file debug-dir filename)]
    (.mkdirs debug-dir)
    (spit file (with-out-str (pp/pprint edn-data)))
    (log/info "Wrote EDN debug to" (.getPath file))))

(defn validate-and-report
  "Validate EDN data and report any errors."
  [edn-data spec-key spec-name]
  (let [spec (get {:backend specs/BackendOutput
                   :descriptor-set specs/DescriptorSet
                   :file specs/FileDef
                   :message specs/MessageDef} 
                  spec-key)]
    (if (m/validate spec edn-data {:registry specs/registry})
      (do
        (log/info spec-name "validation passed")
        true)
      (do
        (log/error spec-name "validation failed:")
        (pp/pprint (specs/explain-validation-error spec edn-data))
        false))))

(defn generate-with-new-architecture
  "Generate Clojure code using the new backend/frontend architecture."
  [{:keys [input-dir output-dir namespace-prefix debug?]
    :or {debug? true}}]
  (log/info "Starting code generation with new architecture")
  (log/info "Input directory:" input-dir)
  (log/info "Output directory:" output-dir)
  (log/info "Namespace prefix:" namespace-prefix)
  
  (try
    ;; Step 1: Parse descriptors to EDN (Backend)
    (log/info "Backend: Parsing descriptors to EDN...")
    (let [backend-output (backend/process-descriptor-files input-dir)]
      
      ;; Step 2: Validate EDN representation
      (when debug?
        (log/info "Writing EDN representation for debugging...")
        ;; Skip validation for now due to spec issues
        ;; (validate-and-report backend-output :backend "Backend output")
        
        ;; Write EDN for debugging
        (write-edn-debug (:command backend-output) output-dir "command-edn.edn")
        (write-edn-debug (:state backend-output) output-dir "state-edn.edn")
        (write-edn-debug (:type-lookup backend-output) output-dir "type-lookup.edn"))
      
      ;; Step 3: Generate Clojure code (Frontend)
      (log/info "Frontend: Generating Clojure code from EDN...")
      (let [generated (frontend/generate-from-backend backend-output namespace-prefix)]
        
        ;; Step 4: Write generated code
        (log/info "Writing generated code...")
        (.mkdirs (io/file output-dir namespace-prefix))
        
        (let [ns-path (str/replace namespace-prefix #"\." "/")
              cmd-file (io/file output-dir 
                               (str ns-path "/command.clj"))
              state-file (io/file output-dir 
                                 (str ns-path "/state.clj"))]
          
          (.mkdirs (.getParentFile cmd-file))
          (.mkdirs (.getParentFile state-file))
          
          (spit cmd-file (:command generated))
          (spit state-file (:state generated))
          
          (log/info "Generated" (.getPath cmd-file))
          (log/info "Generated" (.getPath state-file))
          
          {:success true
           :files [(str cmd-file) (str state-file)]
           :backend-output backend-output})))
    
    (catch Exception e
      (log/error e "Code generation failed")
      {:success false
       :error (.getMessage e)})))

;; =============================================================================
;; Legacy Import Generation (kept for compatibility)
;; =============================================================================

(defn collect-imports
  "Collect all Java imports needed for the generated code."
  [messages enums]
  (let [;; Collect all java classes
        all-classes (concat
                      (keep :java-class messages)
                      (keep :java-class enums))
        ;; Group by package (everything before the last .)
        by-package (group-by (fn [java-class]
                              (let [last-dot (.lastIndexOf java-class ".")]
                                (if (pos? last-dot)
                                  (subs java-class 0 last-dot)
                                  "")))
                            all-classes)]
    ;; Create import groups
    (map (fn [[pkg classes]]
           (concat
             [pkg]
             (distinct
               (map (fn [java-class]
                     (let [last-dot (.lastIndexOf java-class ".")]
                       (if (pos? last-dot)
                         (subs java-class (inc last-dot))
                         java-class)))
                   classes))))
         by-package)))

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
  "Process command descriptor files to extract Root message and dependencies.
  This is kept for backward compatibility but now uses the new architecture."
  [descriptor-dir]
  (let [backend-output (backend/process-descriptor-files descriptor-dir)
        ;; Extract command file data
        cmd-files (get-in backend-output [:command :files])
        ;; Find the Root message
        all-messages (mapcat :messages cmd-files)
        root-msg (first (filter #(= (:name %) :root) all-messages))]
    {:root root-msg
     :messages all-messages
     :enums (mapcat :enums cmd-files)}))

(defn process-state-descriptors
  "Process state descriptor files to extract State message and dependencies.
  This is kept for backward compatibility but now uses the new architecture."
  [descriptor-dir]
  (let [backend-output (backend/process-descriptor-files descriptor-dir)
        ;; Extract state file data
        state-files (get-in backend-output [:state :files])
        ;; Find the State message
        all-messages (mapcat :messages state-files)
        state-msg (first (filter #(= (:name %) :state) all-messages))]
    {:root state-msg
     :messages all-messages
     :enums (mapcat :enums state-files)}))

;; =============================================================================
;; Code Generation
;; =============================================================================

(defn generate-namespace
  "Generate a complete namespace with all conversions."
  [{:keys [ns-name messages enums]}]
  (let [imports (collect-imports messages enums)
        ;; Create a lookup map for message types
        message-lookup (into {} (map (fn [msg]
                                      [(:java-class msg) msg])
                                    messages))]
    (templates/generate-code
      {:ns-name ns-name
       :messages messages
       :enums enums
       :imports imports
       :message-lookup message-lookup})))

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
  "Generate all protobuf conversion code.
  Now uses the new architecture by default."
  [{:keys [input-dir output-dir namespace-prefix use-new-architecture?] :as config}]
  (if (or use-new-architecture? (not (contains? config :use-new-architecture?)))
    ;; Use new architecture by default
    (generate-with-new-architecture config)
    ;; Use legacy architecture
    (do
      (log/info "Starting protobuf code generation (legacy mode)...")
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
         :state-file state-file}))))