(ns cmd-explorer.pronto-malli
  "Integration layer between Pronto proto-maps and Malli specs.
   Provides utilities for validating, generating, and converting between
   Pronto proto-maps and Malli specs."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [pronto.core :as p]
   [cmd-explorer.registry :as registry]
   [clojure.test.check.generators :as gen]))

;; ============================================================================
;; Core Validation Functions
;; ============================================================================

(defn validate-proto-map
  "Validate a Pronto proto-map against a Malli spec.
   Returns true if valid, false otherwise.
   
   The spec should be designed to validate proto-map structure:
   - All fields are always present with defaults
   - Scalar fields are never nil
   - Message fields can be nil when unset
   - Oneof constraints must be checked with p/which-one-of"
  [spec proto-map]
  (m/validate spec proto-map))

(defn explain-proto-map
  "Explain validation errors for a proto-map against a spec.
   Returns nil if valid, otherwise returns explanation data."
  [spec proto-map]
  (m/explain spec proto-map))

(defn humanize-proto-map-errors
  "Get human-readable error messages for proto-map validation failures."
  [spec proto-map]
  (when-let [explanation (explain-proto-map spec proto-map)]
    (me/humanize explanation)))

;; ============================================================================
;; Proto-Map Generation
;; ============================================================================

(defn generate-proto-map
  "Generate a Pronto proto-map from a Malli spec.
   
   Parameters:
   - mapper: The Pronto mapper containing the proto class
   - proto-class: The Java protobuf class
   - spec: A Malli spec describing the proto-map structure
   - options: Optional generation options
   
   Returns a valid Pronto proto-map instance."
  [mapper proto-class spec & [{:keys [size seed] :as options}]]
  (let [gen-options (cond-> {}
                      size (assoc :size size)
                      seed (assoc :seed seed))
        ;; Generate a plain map from the spec
        generated-map (if (seq gen-options)
                        (mg/generate spec gen-options)
                        (mg/generate spec))]
    ;; Convert the plain map to a proto-map
    (if (map? generated-map)
      ;; Create empty proto-map and merge the generated map
      (let [empty-proto (p/proto-map mapper proto-class)]
        (merge empty-proto generated-map))
      generated-map)))

(defn generate-valid-proto-map
  "Generate a proto-map that is guaranteed to pass validation.
   Tries multiple times if necessary to ensure validity.
   
   Parameters:
   - mapper: The Pronto mapper
   - proto-class: The Java protobuf class  
   - spec: The Malli spec
   - max-attempts: Maximum generation attempts (default 100)
   
   Returns a valid proto-map or throws if unable to generate."
  [mapper proto-class spec & [{:keys [max-attempts] :or {max-attempts 100}}]]
  (loop [attempts 0]
    (if (>= attempts max-attempts)
      (throw (ex-info "Unable to generate valid proto-map"
                      {:spec spec
                       :proto-class proto-class
                       :attempts max-attempts}))
      (let [proto-map (generate-proto-map mapper proto-class spec)]
        (if (validate-proto-map spec proto-map)
          proto-map
          (recur (inc attempts)))))))

;; ============================================================================
;; Spec Adaptation for Proto-Maps
;; ============================================================================

(defn adapt-spec-for-proto-map
  "Adapt a regular Malli spec to work with Pronto proto-maps.
   
   This handles the differences between plain maps and proto-maps:
   - Proto-maps have all fields present with defaults
   - Scalar fields are never nil (have zero values)
   - Message fields can be nil
   - Oneof fields need special handling
   
   Returns an adapted spec suitable for proto-map validation."
  [spec proto-class]
  ;; For now, return the spec as-is
  ;; In the future, we can add transformations here
  spec)

;; ============================================================================
;; Oneof Validation Helpers
;; ============================================================================

(defn create-oneof-validator
  "Create a validator function for a oneof field in a proto-map.
   
   Parameters:
   - oneof-name: The name of the oneof field (as keyword)
   - field-specs: A map of field-name -> spec for each possible field
   
   Returns a function that validates the oneof constraint."
  [oneof-name field-specs]
  (fn [proto-map]
    (let [active-field (p/which-one-of proto-map oneof-name)]
      (if (nil? active-field)
        false ;; No field is set, invalid for required oneof
        (if-let [field-spec (get field-specs active-field)]
          ;; Validate the active field's value
          (let [field-value (get proto-map active-field)]
            (m/validate field-spec field-value))
          ;; Unknown field set (shouldn't happen)
          false)))))

(defn create-oneof-spec
  "Create a Malli spec for a oneof field.
   
   Parameters:
   - oneof-name: The name of the oneof (as keyword)
   - field-specs: Map of field-name -> spec
   - proto-class: The Java protobuf class
   - mapper: The Pronto mapper
   
   Returns a Malli spec that validates the oneof constraint."
  [oneof-name field-specs proto-class mapper]
  [:fn
   {:error/message (str "Exactly one field in " oneof-name " must be set")}
   (create-oneof-validator oneof-name field-specs)])

;; ============================================================================
;; Batch Validation
;; ============================================================================

(defn validate-proto-maps
  "Validate multiple proto-maps against a spec.
   Returns a map with :valid and :invalid keys containing the categorized maps."
  [spec proto-maps]
  (reduce (fn [acc proto-map]
            (if (validate-proto-map spec proto-map)
              (update acc :valid conj proto-map)
              (update acc :invalid conj proto-map)))
          {:valid [] :invalid []}
          proto-maps))

(defn validate-with-report
  "Validate a proto-map and return a detailed report.
   
   Returns a map with:
   - :valid? - boolean indicating if validation passed
   - :proto-map - the original proto-map
   - :errors - human-readable error messages (if invalid)
   - :explanation - full explanation data (if invalid)"
  [spec proto-map]
  (if (validate-proto-map spec proto-map)
    {:valid? true
     :proto-map proto-map}
    {:valid? false
     :proto-map proto-map
     :errors (humanize-proto-map-errors spec proto-map)
     :explanation (explain-proto-map spec proto-map)}))

;; ============================================================================
;; Round-Trip Testing
;; ============================================================================

(defn test-round-trip
  "Test that a proto-map can round-trip through serialization.
   
   Parameters:
   - proto-map: The proto-map to test
   - mapper: The Pronto mapper
   - proto-class: The Java protobuf class
   
   Returns a map with:
   - :success? - whether the round-trip succeeded
   - :original - the original proto-map
   - :serialized - the byte array
   - :restored - the restored proto-map
   - :errors - any errors that occurred"
  [proto-map mapper proto-class]
  (try
    (let [bytes (p/proto-map->bytes proto-map)
          restored (p/bytes->proto-map mapper proto-class bytes)]
      {:success? true
       :original proto-map
       :serialized bytes
       :restored restored})
    (catch Exception e
      {:success? false
       :original proto-map
       :errors (.getMessage e)})))

(defn validate-round-trip
  "Validate that a generated proto-map can round-trip and still be valid.
   
   Returns true if the proto-map can serialize, deserialize, and still
   pass validation against the spec."
  [spec proto-map mapper proto-class]
  (let [result (test-round-trip proto-map mapper proto-class)]
    (and (:success? result)
         (validate-proto-map spec (:restored result)))))

;; ============================================================================
;; Generator Testing
;; ============================================================================

(defn test-generator
  "Test a proto-map generator by generating and validating multiple samples.
   
   Parameters:
   - mapper: The Pronto mapper
   - proto-class: The Java protobuf class
   - spec: The Malli spec
   - n: Number of samples to generate (default 100)
   
   Returns a map with statistics about the generation:
   - :total - total samples generated
   - :valid - number that passed validation
   - :invalid - number that failed validation
   - :round-trip-success - number that survived serialization round-trip
   - :errors - any validation errors encountered"
  [mapper proto-class spec & [{:keys [n] :or {n 100}}]]
  (let [results (for [i (range n)]
                  (try
                    (let [proto-map (generate-proto-map mapper proto-class spec)
                          valid? (validate-proto-map spec proto-map)
                          round-trip? (when valid?
                                       (validate-round-trip spec proto-map mapper proto-class))]
                      {:valid? valid?
                       :round-trip? round-trip?})
                    (catch Exception e
                      {:error (.getMessage e)})))]
    {:total n
     :valid (count (filter :valid? results))
     :invalid (count (remove :valid? results))
     :round-trip-success (count (filter :round-trip? results))
     :errors (vec (keep :error results))}))

;; ============================================================================
;; Performance Utilities
;; ============================================================================

(defn create-optimized-proto-map
  "Create a proto-map with optimized performance using Pronto best practices.
   
   Parameters:
   - mapper: The Pronto mapper
   - proto-class: The Java protobuf class
   - field-values: A map of field names to values
   
   Creates an empty proto-map and merges all values at once."
  [mapper proto-class field-values]
  (let [empty-proto (p/proto-map mapper proto-class)]
    (merge empty-proto field-values)))

(defn batch-update-proto-map
  "Update multiple fields in a proto-map efficiently using p-> macro.
   
   Parameters:
   - proto-map: The proto-map to update
   - updates: A map of field names to new values
   
   Returns the updated proto-map."
  [proto-map updates]
  (p/p-> proto-map
         (as-> pm
               (reduce (fn [pm [k v]]
                        (assoc pm k v))
                      pm
                      updates))))

(defn with-type-hints
  "Apply type hints to a proto-map for performance-critical code.
   
   Parameters:
   - proto-map: The proto-map to hint
   - proto-class: The Java protobuf class
   - mapper: The Pronto mapper
   - f: Function to execute with the hinted proto-map
   
   Returns the result of calling f with the hinted proto-map."
  [proto-map proto-class mapper f]
  ;; For now, just call the function without hints
  ;; p/with-hints is a complex macro that needs special handling
  (f proto-map))