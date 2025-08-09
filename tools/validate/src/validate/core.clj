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
  [["-f" "--file FILE" "Path to binary file to validate"
    :validate [#(.exists (java.io.File. %)) "File must exist"]]
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
   ["validate - Protobuf Binary Validation Tool"
    ""
    "Usage: validate [options]"
    ""
    "Options:"
    options-summary
    ""
    "Examples:"
    "  validate -f output/state_20241208.bin"
    "  validate -f commands.bin -t cmd"
    "  validate -f data.bin -o json"
    "  validate -f state.bin -t state -v"
    ""
    "Message Types:"
    "  state - Validate as state root message (ser.JonSharedData)"
    "  cmd   - Validate as command root message (cmd.JonSharedCmd)"
    "  auto  - Auto-detect message type (default)"
    ""
    "Output Formats:"
    "  text - Human-readable text output (default)"
    "  json - JSON format for programmatic processing"
    "  edn  - Clojure EDN format"]))

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
  [{:keys [file type output verbose]}]
  (try
    (when verbose
      (println "Starting validation...")
      (println (str "  File: " file))
      (println (str "  Type: " type))
      (println (str "  Output: " output))
      (println))
    
    (let [result (validator/validate-file file :type type)]
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