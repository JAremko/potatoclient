(ns validate.core
  "Main entry point for the validate CLI tool."
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as str]
   [clojure.data.json :as json]
   [clojure.pprint :as pp]
   [validate.validator :as validator])
  (:gen-class))

(def cli-options
  [["-f" "--file FILE" "Path to file to validate (binary or EDN)"
    :validate [#(.exists (java.io.File. %)) "File must exist"]]
   ["-e" "--edn" "Treat input file as EDN format"
    :default false]
   ["-t" "--type TYPE" "Message type: state, cmd, or auto (default: auto)"
    :default :auto
    :parse-fn keyword
    :validate [#(#{:state :cmd :auto} %) "Must be state, cmd, or auto"]]
   ["-o" "--output FORMAT" "Output format: text, json, or edn (default: text)"
    :default :text
    :parse-fn keyword
    :validate [#(#{:text :json :edn} %) "Must be text, json, or edn"]]
   ["-v" "--verbose" "Enable verbose output"
    :default false]
   ["-h" "--help" "Show this help message"]])

(defn usage
  "Generate usage string."
  [options-summary]
  (str/join
   \newline
   ["validate - Dual Protobuf Binary Validation Tool"
    ""
    "This tool runs both buf.validate and Malli validation in parallel,"
    "providing comprehensive structural and semantic validation."
    ""
    "Usage: validate [options]"
    ""
    "Options:"
    options-summary
    ""
    "Examples:"
    "  validate -f output/state_20241208.bin      # Validate binary file"
    "  validate -f state.edn -e                   # Validate EDN file"
    "  validate -f commands.bin -t cmd            # Validate as command type"
    "  validate -f data.edn -e -o json            # EDN input with JSON output"
    "  validate -f state.bin -t state -v          # Verbose binary validation"
    ""
    "Message Types:"
    "  state - Validate as state root message (ser.JonSharedData)"
    "  cmd   - Validate as command root message (cmd.JonSharedCmd)"
    "  auto  - Auto-detect message type (default)"
    ""
    "Output Formats:"
    "  text - Human-readable text output with both validation results (default)"
    "  json - JSON format with nested validation results"
    "  edn  - Clojure EDN format with nested validation results"
    ""
    "Input Formats:"
    "  • Binary protobuf files (.bin, .pb, etc.)"
    "  • EDN files with -e flag (.edn)"
    ""
    "Validation Features:"
    "  • buf.validate: Checks protobuf structural constraints"
    "  • Malli: Checks semantic constraints with Clojure specs"
    "  • EDN validation: Validates EDN directly with Malli, then converts to proto"
    "  • Graceful handling of empty, corrupted, and truncated files"
    "  • Humanized error messages for better debugging"
    "  • Independent results from both validators"]))

(defn error-msg
  "Format error messages."
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  "Validate command line arguments."
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}
      
      errors
      {:exit-message (error-msg errors)}
      
      (not (:file options))
      {:exit-message "Error: File path is required. Use -f or --file option."}
      
      :else
      {:options options})))

(defn format-output
  "Format validation result based on output format."
  [result format verbose?]
  (case format
    :text (if verbose?
            (str (validator/format-validation-result result)
                 "\n\nDetailed Result:\n"
                 (with-out-str (pp/pprint result)))
            (validator/format-validation-result result))
    
    :json (json/write-str result)
    
    :edn (pr-str result)
    
    ;; Default
    (validator/format-validation-result result)))

(defn run-validation
  "Run the validation with the given options."
  [{:keys [file type output verbose edn]}]
  (try
    (when verbose
      (println "Starting validation...")
      (println (str "  File: " file))
      (println (str "  Format: " (if edn "EDN" "Binary")))
      (println (str "  Type: " type))
      (println (str "  Output: " output))
      (println))
    
    (let [result (if edn
                   (validator/validate-edn-file file :type type)
                   (validator/validate-file file :type type))]
      (println (format-output result output verbose))
      
      ;; Return appropriate exit code
      (if (:valid? result) 0 1))
    
    (catch Exception e
      (if verbose
        (do
          (println "Validation failed with error:")
          (println (.getMessage e))
          (println "\nStack trace:")
          (.printStackTrace e))
        (println (str "Error: " (.getMessage e))))
      1)))

(defn -main
  "Main entry point for the CLI."
  [& args]
  (let [{:keys [options exit-message ok?]} (validate-args args)]
    (when exit-message
      (println exit-message)
      (System/exit (if ok? 0 1)))
    
    (let [exit-code (run-validation options)]
      (System/exit exit-code))))