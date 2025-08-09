(ns proto-explorer.validation-extractor
  "Extract buf.validate constraints from compiled protobuf Java classes."
  (:require [clojure.java.shell :as shell]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn get-validation-info
  "Get validation information for a protobuf message class.
  Returns a map with field validation constraints extracted from the Java class."
  [class-name]
  (let [result (shell/sh "java" 
                         "-cp" (System/getProperty "java.class.path")
                         "proto_explorer.JavaReflectionHelper"
                         "validation-info"
                         class-name)]
    (if (zero? (:exit result))
      (try
        (edn/read-string (:out result))
        (catch Exception e
          {:error (str "Failed to parse result: " (.getMessage e))
           :raw-output (:out result)}))
      {:error (str "Command failed with exit code " (:exit result))
       :stderr (:err result)})))

(defn parse-validation-options
  "Parse the validation options string to extract constraints.
  The options string contains buf.validate field constraints."
  [options-str]
  (when (and options-str (str/includes? options-str "buf.validate"))
    ;; Extract validation constraints from the options string
    ;; This is a simplified parser - you may need to enhance it based on actual format
    (cond
      (str/includes? options-str "gte:") 
      (let [gte-match (re-find #"gte:\s*([-\d.]+)" options-str)
            lte-match (re-find #"lte:\s*([-\d.]+)" options-str)
            gt-match (re-find #"gt:\s*([-\d.]+)" options-str)
            lt-match (re-find #"lt:\s*([-\d.]+)" options-str)]
        (cond-> {}
          gte-match (assoc :gte (parse-double (second gte-match)))
          lte-match (assoc :lte (parse-double (second lte-match)))
          gt-match (assoc :gt (parse-double (second gt-match)))
          lt-match (assoc :lt (parse-double (second lt-match)))))
      
      (str/includes? options-str "min_len:")
      (let [min-match (re-find #"min_len:\s*(\d+)" options-str)
            max-match (re-find #"max_len:\s*(\d+)" options-str)]
        (cond-> {}
          min-match (assoc :min-len (parse-long (second min-match)))
          max-match (assoc :max-len (parse-long (second max-match)))))
      
      (str/includes? options-str "required: true")
      {:required true}
      
      :else
      {:raw options-str})))

(defn enhance-field-with-validation
  "Enhance field information with parsed validation constraints."
  [field]
  (if-let [options (:options field)]
    (assoc field :validation (parse-validation-options options))
    field))