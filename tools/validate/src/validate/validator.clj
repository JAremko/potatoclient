(ns validate.validator
  "Core validation functionality for protobuf binary payloads using buf.validate."
  (:require
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [pronto.core :as pronto])
  (:import
   [build.buf.protovalidate Validator ValidationResult]
   [com.google.protobuf Message InvalidProtocolBufferException]
   [java.io ByteArrayInputStream FileInputStream InputStream]
   [java.nio.file Files Path Paths]))

(defn create-validator
  "Create a buf.validate Validator instance."
  []
  (try
    (Validator.)
    (catch Exception e
      (log/error e "Failed to create validator")
      (throw (ex-info "Failed to create buf.validate validator" {:error (.getMessage e)})))))

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
    (let [message-class (Class/forName "ser.JonSharedData$JonGUIState")
          parse-method (.getMethod message-class "parseFrom" (into-array Class [(Class/forName "[B")]))]
      (.invoke parse-method nil (object-array [binary-data])))
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
    (let [message-class (Class/forName "cmd.JonSharedCmd$Root")
          parse-method (.getMethod message-class "parseFrom" (into-array Class [(Class/forName "[B")]))]
      (.invoke parse-method nil (object-array [binary-data])))
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

(defn validate-binary
  "Main validation function that takes binary data and validates it.
   Options:
   - :type - :state, :cmd, or :auto (default)
   - :validator - optional pre-created validator instance"
  [binary-data & {:keys [type validator]
                  :or {type :auto}}]
  (when (nil? binary-data)
    (throw (IllegalArgumentException. "Binary data cannot be nil")))
  (when (zero? (count binary-data))
    (throw (IllegalArgumentException. "Binary data cannot be empty")))
  (let [validator (or validator (create-validator))
        detected-type (if (= type :auto)
                       (auto-detect-message-type binary-data)
                       type)]
    (when-not detected-type
      (throw (ex-info "Could not detect message type and no type specified"
                      {:attempted-types [:state :cmd]})))
    
    (let [message (case detected-type
                   :state (parse-state-message binary-data)
                   :cmd (parse-cmd-message binary-data)
                   (throw (ex-info "Unknown message type" {:type detected-type})))
          validation-result (validate-message message validator)]
      (assoc validation-result
             :message-type detected-type
             :message-size (count binary-data)))))

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

(defn format-validation-result
  "Format validation result for display."
  [result]
  (str "Validation Result:\n"
       "  Message Type: " (:message-type result) "\n"
       "  Message Size: " (:message-size result) " bytes\n"
       "  Valid: " (:valid? result) "\n"
       (when-not (:valid? result)
         (str "  Error: " (or (:error result) (:message result)) "\n"
              (when (seq (:violations result))
                (str "  Violations:\n"
                     (clojure.string/join "\n"
                                        (map #(str "    - Field: " (:field %)
                                                  "\n      Constraint: " (:constraint %)
                                                  "\n      Message: " (:message %))
                                            (:violations result)))))))))