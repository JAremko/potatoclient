(ns validate.validator
  "Core validation functionality for protobuf binary payloads using buf.validate and Malli."
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.walk :as walk]
   [clojure.edn :as edn]
   [clojure.tools.logging :as log]
   [pronto.core :as pronto]
   [malli.core :as m]
   [malli.error :as me]
   [malli.registry :as mr]
   [potatoclient.malli.registry :as registry]
   ;; Load specs
   [potatoclient.specs.state-root]
   [potatoclient.specs.cmd.root])
  (:import
   [build.buf.protovalidate Validator ValidationResult]
   [com.google.protobuf Message InvalidProtocolBufferException]
   [java.io ByteArrayInputStream FileInputStream InputStream]
   [java.nio.file Files Path Paths]
   [ser JonSharedData$JonGUIState]
   [cmd JonSharedCmd$Root]))

;; Initialize the Malli registry with oneof-edn schema
(do
  (require '[potatoclient.specs.oneof-edn :as oneof-edn])
  (registry/setup-global-registry!
   (oneof-edn/register_ONeof-edn-schema!)))

;; Cache the validator instance for performance
;; Creating a new validator for each validation adds ~150ms overhead
(def ^:private cached-validator
  (delay
    (try
      (Validator.)
      (catch Exception e
        (log/error e "Failed to create validator")
        (throw (ex-info "Failed to create buf.validate validator" {:error (.getMessage e)}))))))

(defn create-validator
  "Create a buf.validate Validator instance.
   Uses a cached instance for better performance."
  []
  @cached-validator)

(defn read-binary-file
  "Read binary data from a file path."
  [file-path]
  (try
    (let [path (if (instance? Path file-path)
                 file-path
                 (Paths/get file-path (into-array String [])))]
      (Files/readAllBytes path))
    (catch Exception e
      (log/error e "Failed to read file" file-path)
      (throw (ex-info "Failed to read binary file" 
                      {:file file-path
                       :error (.getMessage e)})))))

(defn read-binary-stream
  "Read binary data from an input stream."
  [input-stream]
  (try
    (let [baos (java.io.ByteArrayOutputStream.)]
      (io/copy input-stream baos)
      (.toByteArray baos))
    (catch Exception e
      (log/error e "Failed to read stream")
      (throw (ex-info "Failed to read binary stream" {:error (.getMessage e)})))))

(defn parse-state-message
  "Parse binary data as a state root message (ser.JonSharedData$JonGUIState)."
  [binary-data]
  (try
    ;; Direct static method call instead of reflection
    (ser.JonSharedData$JonGUIState/parseFrom binary-data)
    (catch InvalidProtocolBufferException e
      (log/error e "Invalid protobuf format for state message")
      (throw (ex-info "Invalid state message format" 
                      {:error (.getMessage e)
                       :type :parse-error})))
    (catch Exception e
      (log/error e "Failed to parse state message")
      (throw (ex-info "Failed to parse state message" 
                      {:error (.getMessage e)
                       :type :unknown-error})))))

(defn parse-cmd-message
  "Parse binary data as a command root message (cmd.JonSharedCmd$Root)."
  [binary-data]
  (try
    ;; Direct static method call instead of reflection
    (cmd.JonSharedCmd$Root/parseFrom binary-data)
    (catch InvalidProtocolBufferException e
      (log/error e "Invalid protobuf format for command message")
      (throw (ex-info "Invalid command message format" 
                      {:error (.getMessage e)
                       :type :parse-error})))
    (catch Exception e
      (log/error e "Failed to parse command message")
      (throw (ex-info "Failed to parse command message" 
                      {:error (.getMessage e)
                       :type :unknown-error})))))

(declare validate-message)

(defn auto-detect-message-type
  "Attempt to auto-detect whether a binary is a state or cmd message.
   Uses validation to determine the correct type since protobuf parsing is lenient."
  [binary-data]
  (let [validator (create-validator)]
    ;; Try cmd first (simpler structure)
    (try
      (let [cmd-msg (parse-cmd-message binary-data)
            cmd-result (validate-message cmd-msg validator)]
        (if (:valid? cmd-result)
          :cmd
          ;; Try state if cmd validation fails
          (try
            (let [state-msg (parse-state-message binary-data)
                  state-result (validate-message state-msg validator)]
              (if (:valid? state-result)
                :state
                ;; Both parse but neither validates - return based on fewer violations
                (if (<= (count (:violations cmd-result))
                        (count (:violations state-result)))
                  :cmd
                  :state)))
            (catch Exception _
              ;; State parsing failed, return cmd
              :cmd))))
      (catch Exception _
        ;; Cmd parsing failed, try state
        (try
          (parse-state-message binary-data)
          :state
          (catch Exception _
            ;; Neither worked
            nil))))))

(defn validate-message
  "Validate a protobuf message using buf.validate.
   Returns a map with validation results."
  [message validator]
  (try
    (let [result (.validate validator message)]
      (if (.isSuccess result)
        {:valid? true
         :message "Validation successful"
         :violations []}
        {:valid? false
         :message "Validation failed"
         :violations (map (fn [violation]
                           {:field (try (.getFieldPath violation) 
                                       (catch Exception _ (.toString violation)))
                            :constraint (try (.getConstraintId violation)
                                           (catch Exception _ "unknown"))
                            :message (try (.getMessage violation)
                                        (catch Exception _ (.toString violation)))})
                         (.getViolations result))}))
    (catch build.buf.protovalidate.exceptions.ValidationException e
      {:valid? false
       :message "Validation exception"
       :error (.getMessage e)
       :violations []})
    (catch Exception e
      {:valid? false
       :message "Unexpected error during validation"
       :error (.getMessage e)
       :violations []})))

;; Define mappers for proto->edn conversion
(pronto/defmapper state-mapper [ser.JonSharedData$JonGUIState])
(pronto/defmapper cmd-mapper [cmd.JonSharedCmd$Root])

(defn proto->edn
  "Convert a protobuf message to EDN format for Malli validation.
   Handles errors gracefully."
  [message message-type]
  (try
    ;; The Java proto objects from parsing need to be converted to proto-maps first
    ;; We'll re-parse from bytes to get proper proto-maps
    (let [binary-data (try
                       (.toByteArray message)
                       (catch Exception _ nil))]
      (when binary-data
        (let [proto-map (case message-type
                          :state (pronto/bytes->proto-map state-mapper 
                                                          ser.JonSharedData$JonGUIState
                                                          binary-data)
                          :cmd (pronto/bytes->proto-map cmd-mapper
                                                        cmd.JonSharedCmd$Root
                                                        binary-data)
                          nil)
              ;; proto-map->clj-map returns snake_case, which now matches our specs
              snake-map (when proto-map (pronto/proto-map->clj-map proto-map))]
          ;; Return snake_case directly - specs now expect snake_case
          snake-map)))
    (catch Exception e
      (log/warn e "Failed to convert proto to EDN")
      nil)))

(defn humanize-malli-errors
  "Convert Malli errors to a flat list of violations with proper humanization."
  [errors]
  (letfn [(flatten-errors [errors path]
            (cond
              (map? errors)
              (mapcat (fn [[k v]]
                        (let [new-path (if (= k :malli/error)
                                        path
                                        (conj path (name k)))]
                          (flatten-errors v new-path)))
                      errors)
              
              (vector? errors)
              (map (fn [msg]
                     {:field (if (empty? path) "root" (clojure.string/join "." path))
                      :constraint "malli"
                      :message msg})
                   errors)
              
              (string? errors)
              [{:field (if (empty? path) "root" (clojure.string/join "." path))
                :constraint "malli"
                :message errors}]
              
              :else []))]
    (flatten-errors errors [])))

(defn validate-with-malli
  "Validate EDN data using Malli specs.
   Returns a map with validation results."
  [edn-data message-type]
  (try
    (let [spec-key (case message-type
                     :state :state/root
                     :cmd :cmd/root
                     nil)
          spec (when spec-key (m/schema spec-key))]
      (if spec
        (if (m/validate spec edn-data)
          {:valid? true
           :message "Malli validation successful"
           :violations []}
          (let [explanation (m/explain spec edn-data)
                ;; Use humanize with custom error messages
                humanized (me/humanize explanation
                                      {:errors (merge me/default-errors
                                                     {::mr/missing-key 
                                                      {:error/fn (fn [{:keys [in]} _]
                                                                  (str "missing required field: " (last in)))}
                                                      ::mr/extra-key
                                                      {:error/fn (fn [{:keys [in]} _]
                                                                  (str "unexpected field: " (last in)))}})})
                violations (humanize-malli-errors humanized)]
            {:valid? false
             :message "Malli validation failed"
             :violations violations}))
        {:valid? false
         :message "No Malli spec found for message type"
         :error (str "Missing spec for " message-type)
         :violations []}))
    (catch Exception e
      {:valid? false
       :message "Malli validation error"
       :error (.getMessage e)
       :violations []})))

(defn validate-binary
  "Main validation function that takes binary data and validates it.
   Runs both buf.validate and Malli validation in parallel.
   Options:
   - :type - :state, :cmd, or :auto (default)
   - :validator - optional pre-created validator instance (uses cached by default)"
  [binary-data & {:keys [type validator]
                  :or {type :auto}}]
  ;; Handle nil/empty data gracefully
  (cond
    (nil? binary-data)
    {:valid? false
     :message "Binary data is nil"
     :message-type type
     :message-size 0
     :buf-validate {:valid? false
                     :message "Cannot validate nil data"
                     :violations []}
     :malli {:valid? false
             :message "Cannot validate nil data"
             :violations []}}
    
    (zero? (count binary-data))
    {:valid? false
     :message "Binary data is empty"
     :message-type type
     :message-size 0
     :buf-validate {:valid? false
                     :message "Cannot validate empty data"
                     :violations []}
     :malli {:valid? false
             :message "Cannot validate empty data"
             :violations []}}
    
    :else
    (try
      ;; Use provided validator or cached instance for performance
      (let [validator (or validator (create-validator))
            detected-type (if (= type :auto)
                           (auto-detect-message-type binary-data)
                           type)]
        
        (if-not detected-type
          {:valid? false
           :message "Could not detect or parse message type"
           :message-type nil
           :message-size (count binary-data)
           :buf-validate {:valid? false
                           :message "Failed to parse binary data"
                           :violations []}
           :malli {:valid? false
                   :message "Failed to parse binary data"
                   :violations []}}
          
          ;; Try to parse and validate
          (let [message (try
                         (case detected-type
                           :state (parse-state-message binary-data)
                           :cmd (parse-cmd-message binary-data))
                         (catch Exception e
                           (log/debug e "Failed to parse message")
                           nil))]
            
            (if-not message
              ;; Parse failed - return error result
              {:valid? false
               :message "Failed to parse binary as protobuf message"
               :message-type detected-type
               :message-size (count binary-data)
               :buf-validate {:valid? false
                               :message "Parse error - invalid protobuf format"
                               :violations []}
               :malli {:valid? false
                       :message "Cannot validate unparseable data"
                       :violations []}}
              
              ;; Parse succeeded - run both validations
              (let [;; Run buf.validate
                    buf-result (validate-message message validator)
                    
                    ;; Convert to EDN and run Malli validation
                    edn-data (proto->edn message detected-type)
                    malli-result (if edn-data
                                   (validate-with-malli edn-data detected-type)
                                   {:valid? false
                                    :message "Failed to convert proto to EDN"
                                    :violations []})
                    
                    ;; Overall validity requires both to pass
                    overall-valid? (and (:valid? buf-result) (:valid? malli-result))]
                
                {:valid? overall-valid?
                 :message (cond
                           overall-valid? "Both validations passed"
                           (and (not (:valid? buf-result)) (not (:valid? malli-result))) "Both validations failed"
                           (not (:valid? buf-result)) "buf.validate failed, Malli passed"
                           :else "Malli failed, buf.validate passed")
                 :message-type detected-type
                 :message-size (count binary-data)
                 :buf-validate buf-result
                 :malli malli-result})))))
      
      (catch Exception e
        (log/error e "Unexpected error during validation")
        {:valid? false
         :message (str "Unexpected error: " (.getMessage e))
         :message-type type
         :message-size (count binary-data)
         :buf-validate {:valid? false
                         :message "Validation error"
                         :violations []}
         :malli {:valid? false
                 :message "Validation error"
                 :violations []}}))))

(defn validate-file
  "Validate a binary file.
   Options:
   - :type - :state, :cmd, or :auto (default)
   - :validator - optional pre-created validator instance"
  [file-path & {:keys [type validator]
               :or {type :auto}}]
  (let [binary-data (read-binary-file file-path)]
    (validate-binary binary-data :type type :validator validator)))

(defn validate-stream
  "Validate binary data from an input stream.
   Options:
   - :type - :state, :cmd, or :auto (default)
   - :validator - optional pre-created validator instance"
  [input-stream & {:keys [type validator]
                  :or {type :auto}}]
  (let [binary-data (read-binary-stream input-stream)]
    (validate-binary binary-data :type type :validator validator)))

(defn read-edn-file
  "Read and parse an EDN file."
  [file-path]
  (try
    (let [content (slurp file-path)]
      (edn/read-string content))
    (catch Exception e
      (log/error e "Failed to read EDN file" file-path)
      (throw (ex-info "Failed to read EDN file" 
                      {:file file-path
                       :error (.getMessage e)})))))

(defn edn->proto-binary
  "Convert EDN data to protobuf binary.
   Now expects snake_case EDN format (no conversion needed)."
  [edn-data message-type]
  (try
    ;; EDN data should already be in snake_case format
    ;; Create proto-map from EDN data directly
    (let [proto-map (case message-type
                      :state (pronto/clj-map->proto-map state-mapper 
                                                        ser.JonSharedData$JonGUIState
                                                        edn-data)
                      :cmd (pronto/clj-map->proto-map cmd-mapper
                                                      cmd.JonSharedCmd$Root  
                                                      edn-data)
                      (throw (ex-info "Unknown message type" {:type message-type})))]
      ;; Convert to binary
      (pronto/proto-map->bytes proto-map))
    (catch Exception e
      (log/error e "Failed to convert EDN to proto binary")
      (throw (ex-info "Failed to convert EDN to protobuf" 
                      {:error (.getMessage e)
                       :type message-type})))))

(defn detect-edn-message-type
  "Detect message type from EDN structure.
   State messages have many sub-messages (gps, system, etc).
   Command messages have client_type and one of the command fields."
  [edn-data]
  (cond
    ;; Command has client_type and one of the command fields
    (and (contains? edn-data :client_type)
         (some #(contains? edn-data %)
               [:ping :noop :frozen :gps :compass :lrf :lrf_calib 
                :rotary :osd :system :cv :day_camera :heat_camera :lira
                :day_cam_glass_heater])) :cmd
    
    ;; State has multiple characteristic fields
    (and (contains? edn-data :gps)
         (contains? edn-data :system)
         (contains? edn-data :time)) :state
    
    ;; Default based on presence of certain fields
    (contains? edn-data :client_type) :cmd
    
    ;; Default to nil if can't detect
    :else nil))

(defn validate-edn
  "Validate EDN data using both Malli and buf.validate.
   First validates with Malli directly, then converts to proto
   and validates with buf.validate."
  [edn-data & {:keys [type validator]
               :or {type :auto}}]
  (cond
    (nil? edn-data)
    {:valid? false
     :message "EDN data is nil"
     :message-type type
     :message-size 0
     :buf-validate {:valid? false
                     :message "Cannot validate nil data"
                     :violations []}
     :malli {:valid? false
             :message "Cannot validate nil data"
             :violations []}}
    
    (not (map? edn-data))
    {:valid? false
     :message "EDN data must be a map"
     :message-type type
     :message-size 0
     :buf-validate {:valid? false
                     :message "EDN must be a map"
                     :violations []}
     :malli {:valid? false
             :message "EDN must be a map"
             :violations []}}
    
    :else
    (try
      (let [;; Detect or use specified type
            detected-type (if (= type :auto)
                           (detect-edn-message-type edn-data)
                           type)]
        
        (if-not detected-type
          {:valid? false
           :message "Could not detect message type from EDN structure"
           :message-type nil
           :message-size 0
           :buf-validate {:valid? false
                           :message "Unknown message type"
                           :violations []}
           :malli {:valid? false
                   :message "Unknown message type"
                   :violations []}}
          
          (let [;; First run Malli validation on EDN directly
                malli-result (validate-with-malli edn-data detected-type)
                
                ;; Try to convert to proto binary
                binary (try
                        (edn->proto-binary edn-data detected-type)
                        (catch Exception e
                          (log/debug e "Failed to convert EDN to proto")
                          nil))
                
                ;; If conversion succeeded, validate with buf.validate
                buf-result (if binary
                             (let [val-result (validate-binary binary 
                                                              :type detected-type
                                                              :validator validator)]
                               (:buf-validate val-result))
                             {:valid? false
                              :message "Failed to convert EDN to protobuf"
                              :violations []})
                
                ;; Overall validity requires both to pass
                overall-valid? (and (:valid? malli-result) (:valid? buf-result))]
            
            {:valid? overall-valid?
             :message (cond
                       overall-valid? "Both validations passed"
                       (and (not (:valid? buf-result)) (not (:valid? malli-result))) "Both validations failed"
                       (not (:valid? buf-result)) "buf.validate failed, Malli passed"
                       :else "Malli failed, buf.validate passed")
             :message-type detected-type
             :message-size (if binary (count binary) 0)
             :input-type :edn
             :buf-validate buf-result
             :malli malli-result})))
      
      (catch Exception e
        (log/error e "Unexpected error during EDN validation")
        {:valid? false
         :message (str "Unexpected error: " (.getMessage e))
         :message-type type
         :message-size 0
         :input-type :edn
         :buf-validate {:valid? false
                         :message "Validation error"
                         :violations []}
         :malli {:valid? false
                 :message "Validation error"
                 :violations []}}))))

(defn validate-edn-file
  "Validate an EDN file using both validators.
   Options:
   - :type - :state, :cmd, or :auto (default)
   - :validator - optional pre-created validator instance"
  [file-path & {:keys [type validator]
                :or {type :auto}}]
  (let [edn-data (read-edn-file file-path)]
    (validate-edn edn-data :type type :validator validator)))

(defn format-validation-result
  "Format validation result for display."
  [result]
  (let [buf-result (:buf-validate result)
        malli-result (:malli result)]
    (str "Validation Result:\n"
         (when (:input-type result)
           (str "  Input Type: " (name (:input-type result)) "\n"))
         "  Message Type: " (:message-type result) "\n"
         "  Message Size: " (:message-size result) " bytes\n"
         "  Overall Valid: " (:valid? result) "\n"
         "  Summary: " (:message result) "\n"
         
         ;; Show buf.validate results
         (when buf-result
           (str "\n  buf.validate:\n"
                "    Valid: " (:valid? buf-result) "\n"
                (when-not (:valid? buf-result)
                  (str "    Message: " (or (:error buf-result) (:message buf-result)) "\n"
                       (when (seq (:violations buf-result))
                         (str "    Violations:\n"
                              (str/join "\n"
                                       (map #(str "      - Field: " (:field %)
                                                 "\n        Constraint: " (:constraint %)
                                                 "\n        Message: " (:message %))
                                           (:violations buf-result)))))))))
         
         ;; Show Malli results
         (when malli-result
           (str "\n  Malli:\n"
                "    Valid: " (:valid? malli-result) "\n"
                (when-not (:valid? malli-result)
                  (str "    Message: " (or (:error malli-result) (:message malli-result)) "\n"
                       (when (seq (:violations malli-result))
                         (str "    Violations:\n"
                              (str/join "\n"
                                       (map #(str "      - Field: " (:field %)
                                                 "\n        Message: " (:message %))
                                           (:violations malli-result)))))))))))) ; closing: (str/join, (str "    Violations", (when (seq, (str "    Message", (when-not, (str "\n  Malli", (when malli-result, (str "Validation Result", (let, (defn