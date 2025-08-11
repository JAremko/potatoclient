(ns validate.test-validator
  "Test-specific validator that can validate any protobuf message type,
   not just root messages. This allows testing sub-messages independently."
  (:require
   [clojure.tools.logging :as log]
   [pronto.core :as p])
  (:import
   [build.buf.protovalidate Validator ValidationResult]
   [com.google.protobuf Message InvalidProtocolBufferException]))

;; ============================================================================
;; CORE VALIDATION
;; ============================================================================

(def ^:private cached-validator
  "Cached validator instance for performance"
  (delay
    (try
      (Validator.)
      (catch Exception e
        (log/error e "Failed to create validator")
        (throw (ex-info "Failed to create buf.validate validator" {:error (.getMessage e)}))))))

(defn get-validator
  "Get or create a buf.validate Validator instance."
  []
  @cached-validator)

(defn validate-message
  "Validate any protobuf message using buf.validate.
   This is the core validation function that works with any message type."
  [^Message message]
  (try
    (let [validator (get-validator)
          result (.validate validator message)]
      (if (.isSuccess result)
        {:valid? true
         :message "Validation successful"
         :violations []}
        {:valid? false
         :message "Validation failed"
         :violations (mapv (fn [violation]
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

;; ============================================================================
;; PROTO-MAP VALIDATION
;; ============================================================================

(defn validate-proto-map
  "Validate a Pronto proto-map directly.
   Extracts the underlying protobuf message and validates it."
  [proto-map]
  (if (p/proto-map? proto-map)
    (let [proto-message (p/proto-map->proto proto-map)]
      (validate-message proto-message))
    (throw (IllegalArgumentException. "Input must be a proto-map"))))

;; ============================================================================
;; BINARY VALIDATION WITH CUSTOM PARSER
;; ============================================================================

(defn parse-as
  "Parse binary data as a specific protobuf class.
   This allows parsing any message type, not just roots."
  [binary-data ^Class message-class]
  (try
    (let [parse-method (.getMethod message-class "parseFrom" 
                                  (into-array Class [(Class/forName "[B")]))]
      (.invoke parse-method nil (object-array [binary-data])))
    (catch InvalidProtocolBufferException e
      (throw (ex-info "Invalid protobuf format" 
                      {:class message-class
                       :error (.getMessage e)
                       :type :parse-error})))
    (catch Exception e
      (throw (ex-info "Failed to parse message" 
                      {:class message-class
                       :error (.getMessage e)
                       :type :unknown-error})))))

(defn validate-binary-as
  "Validate binary data as a specific message type.
   Allows testing any message type by providing its class."
  [binary-data ^Class message-class]
  (let [message (parse-as binary-data message-class)
        result (validate-message message)]
    (assoc result 
           :message-class (.getName message-class)
           :message-size (count binary-data))))

;; ============================================================================
;; HIERARCHICAL TESTING UTILITIES
;; ============================================================================

(defn validate-hierarchy
  "Validate a message and all its sub-messages hierarchically.
   Returns a map with validation results for each level."
  [proto-map path]
  (let [self-result (validate-proto-map proto-map)
        sub-results (atom {})]
    
    ;; Validate each field that is also a message
    (doseq [[k v] proto-map
            :when (p/proto-map? v)]
      (let [sub-path (conj path k)
            sub-result (validate-hierarchy v sub-path)]
        (swap! sub-results assoc k sub-result)))
    
    {:path path
     :valid? (:valid? self-result)
     :violations (:violations self-result)
     :sub-messages @sub-results}))

(defn all-valid?
  "Check if a hierarchical validation result is completely valid."
  [hierarchy-result]
  (and (:valid? hierarchy-result)
       (every? all-valid? (vals (:sub-messages hierarchy-result)))))

(defn find-violations
  "Extract all violations from a hierarchical validation result."
  [hierarchy-result]
  (concat
   (when-not (:valid? hierarchy-result)
     (map #(assoc % :path (:path hierarchy-result)) 
          (:violations hierarchy-result)))
   (mapcat find-violations (vals (:sub-messages hierarchy-result)))))

;; ============================================================================
;; TEST HELPERS FOR SPECIFIC MESSAGE TYPES
;; ============================================================================

(defn validate-gps-submessage
  "Validate a GPS sub-message directly."
  [gps-proto-map]
  (validate-proto-map gps-proto-map))

(defn validate-system-submessage
  "Validate a System sub-message directly."
  [system-proto-map]
  (validate-proto-map system-proto-map))

(defn validate-state-with-submessages
  "Validate a complete state message and report on each sub-message."
  [state-proto-map]
  (let [hierarchy (validate-hierarchy state-proto-map [:root])]
    {:overall-valid? (all-valid? hierarchy)
     :root-valid? (:valid? hierarchy)
     :sub-message-results (reduce (fn [acc [k v]]
                                    (assoc acc k (:valid? v)))
                                  {}
                                  (:sub-messages hierarchy))
     :all-violations (find-violations hierarchy)}))

;; ============================================================================
;; BOTTOM-UP TESTING STRATEGY
;; ============================================================================

(defn test-bottom-up
  "Test a message hierarchy from bottom to top.
   1. Test leaf messages (no sub-messages)
   2. Test their parents
   3. Continue up to root
   Returns a detailed test report."
  [root-proto-map]
  (let [results (atom [])]
    
    ;; Helper to test at each level
    (letfn [(test-level [proto-map path level]
              (let [result (validate-proto-map proto-map)
                    test-entry {:path path
                               :level level
                               :valid? (:valid? result)
                               :violations (:violations result)}]
                (swap! results conj test-entry)
                
                ;; Test sub-messages first (bottom-up)
                (doseq [[k v] proto-map
                        :when (p/proto-map? v)]
                  (test-level v (conj path k) (inc level)))))]
      
      ;; Start from root
      (test-level root-proto-map [:root] 0))
    
    ;; Organize results by level (bottom-up)
    (let [by-level (group-by :level @results)
          max-level (apply max (keys by-level))]
      {:test-order (for [level (range max-level -1 -1)]
                    {:level level
                     :tests (get by-level level [])})
       :all-valid? (every? :valid? @results)
       :summary {:total (count @results)
                :passed (count (filter :valid? @results))
                :failed (count (remove :valid? @results))}})))

;; ============================================================================
;; USAGE EXAMPLES
;; ============================================================================

(comment
  ;; Example: Validate a GPS sub-message directly
  (require '[validate.test-harness :as h])
  
  ;; Create a GPS proto-map
  (def gps-proto (p/proto-map h/state-mapper ser.JonSharedDataGps$JonGuiDataGps
                              :latitude 45.5
                              :longitude -122.6
                              :altitude 100.0
                              :fix_type :JON_GUI_DATA_GPS_FIX_TYPE_3D
                              :manual_latitude 0.0
                              :manual_longitude 0.0))
  
  ;; Validate it directly
  (validate-proto-map gps-proto)
  ;; => {:valid? true, :message "Validation successful", :violations []}
  
  ;; Example: Test bottom-up
  (def state (h/valid-state))
  (def test-report (test-bottom-up state))
  (println (:summary test-report))
  ;; => {:total 15, :passed 15, :failed 0}
  )