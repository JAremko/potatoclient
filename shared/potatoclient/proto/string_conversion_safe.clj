(ns potatoclient.proto.string-conversion-safe
  "String conversion with collision detection for safety.
  This version tracks all conversions and throws on collisions."
  (:require [clojure.string :as str]
            [potatoclient.proto.string-conversion :as conv]))

;; =============================================================================
;; Collision Tracking
;; =============================================================================

(defonce ^:private forward-cache
  "Maps [function-name input] -> output"
  (atom {}))

(defonce ^:private reverse-cache
  "Maps [function-name output] -> input"
  (atom {}))

(defn clear-caches!
  "Clear all conversion caches. Useful for testing."
  []
  (reset! forward-cache {})
  (reset! reverse-cache {}))

(defn- check-and-cache!
  "Check for collisions and cache the conversion.
  Throws if output already exists with different input."
  [fn-name input output]
  (when (and input output)
    (let [forward-key [fn-name input]
          reverse-key [fn-name output]]
      ;; Check forward cache first
      (if-let [cached (get @forward-cache forward-key)]
        cached  ; Return cached value
        ;; New conversion - check for reverse collision
        (let [existing-input (get @reverse-cache reverse-key)]
          (when (and existing-input (not= existing-input input))
            (throw (ex-info "String conversion collision detected!"
                            {:function fn-name
                             :input input
                             :output output
                             :existing-input existing-input
                             :message (str "Both '" existing-input "' and '" input 
                                          "' convert to '" output "' in " fn-name)})))
          ;; Safe to cache
          (swap! forward-cache assoc forward-key output)
          (swap! reverse-cache assoc reverse-key input)
          output)))))

;; =============================================================================
;; Safe Conversion Functions
;; =============================================================================

(defn ->kebab-case
  "Convert to kebab-case with collision detection"
  [s]
  (when s
    (let [result (conv/->kebab-case s)]
      (check-and-cache! "->kebab-case" s result))))

(defn ->kebab-case-keyword
  "Convert to kebab-case keyword with collision detection"
  [s]
  (when s
    (let [result (conv/->kebab-case-keyword s)]
      (check-and-cache! "->kebab-case-keyword" s result))))

(defn ->PascalCase
  "Convert to PascalCase with collision detection"
  [s]
  (when s
    (let [result (conv/->PascalCase s)]
      (check-and-cache! "->PascalCase" s result))))

(defn ->snake_case
  "Convert to snake_case with collision detection"
  [s]
  (when s
    (let [result (conv/->snake_case s)]
      (check-and-cache! "->snake_case" s result))))

(defn proto-name->clj-name
  "Convert proto name to Clojure name with collision detection"
  [s]
  (when s
    (let [result (conv/proto-name->clj-name s)]
      (check-and-cache! "proto-name->clj-name" s result))))

(defn clj-name->proto-name
  "Convert Clojure name to proto name with collision detection"
  [k]
  (when k
    (let [result (conv/clj-name->proto-name k)]
      (check-and-cache! "clj-name->proto-name" k result))))

(defn json-key->clj-key
  "Convert JSON key to Clojure key with collision detection"
  [k]
  (when k
    (let [result (conv/json-key->clj-key k)]
      (check-and-cache! "json-key->clj-key" k result))))

;; Method name functions don't need collision detection as they're prefixed

(def getter-method-name conv/getter-method-name)
(def setter-method-name conv/setter-method-name)
(def has-method-name conv/has-method-name)
(def add-method-name conv/add-method-name)
(def add-all-method-name conv/add-all-method-name)

;; =============================================================================
;; Collision Reporting
;; =============================================================================

(defn get-all-conversions
  "Get all recorded conversions for analysis"
  []
  {:forward @forward-cache
   :reverse @reverse-cache})

(defn find-potential-collisions
  "Find all outputs that could have multiple inputs"
  []
  (let [reverse @reverse-cache]
    (->> (group-by first (keys reverse))
         (map (fn [[fn-name keys]]
                [fn-name (->> keys
                             (map (fn [k] [(second k) (get reverse k)]))
                             (into {}))]))
         (into {}))))

(defn validate-no-collisions!
  "Validate that no collisions have been detected.
  Useful for running after a test suite."
  []
  (let [collisions (find-potential-collisions)]
    (when (some #(> (count (second %)) 1) collisions)
      (throw (ex-info "Collisions detected in string conversions!"
                      {:collisions collisions})))))