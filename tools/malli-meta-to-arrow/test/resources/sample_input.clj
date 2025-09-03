(ns sample.input
  "Sample input file for testing the migration tool"
  (:require [clojure.string :as str]))

;; Simple function with metadata schema
(defn add-numbers
  "Adds two numbers together"
  {:malli/schema [:=> [:cat :int :int] :int]}
  [x y]
  (+ x y))

;; Private function with metadata
(defn- validate-input
  {:malli/schema [:=> [:cat :string] :boolean]
   :private true
   :doc "Internal validation"}
  [input]
  (and (string? input)
       (not (str/blank? input))))

;; Function with complex schema
(defn process-data
  {:malli/schema [:=> 
                  [:cat 
                   [:map 
                    [:id :uuid]
                    [:data [:vector :any]]
                    [:config [:? [:map-of :keyword :any]]]]
                   [:? :boolean]]
                  [:or 
                   [:tuple :keyword :string]
                   :nil]]}
  [input & [verbose?]]
  (when verbose?
    (println "Processing:" (:id input)))
  (if (seq (:data input))
    [:ok "processed"]
    nil))

;; Multi-arity function
(defn flexible
  "A flexible function with multiple arities"
  {:malli/schema [:function
                  [:=> [:cat] :nil]
                  [:=> [:cat :int] :int]
                  [:=> [:cat :int :int] :int]]
   :since "1.0"}
  ([] nil)
  ([x] x)
  ([x y] (+ x y)))

;; Function without malli schema (should not be touched)
(defn regular-function
  "Just a regular function"
  {:some-other :metadata}
  [x]
  (* x 2))

;; Function with only malli schema in metadata
(defn minimal
  {:malli/schema [:=> [:cat :keyword] :string]}
  [k]
  (name k))