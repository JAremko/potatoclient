(ns sample.input
  "Sample input file for testing the migration tool"
  (:require [clojure.string :as str]
            [malli.core :as m]))

;; Simple function with metadata schema
(defn add-numbers
  "Adds two numbers together"
  [x y]
  (+ x y))
(m/=> add-numbers [:=> [:cat :int :int] :int])

;; Private function with metadata
(defn- validate-input
  {:private true
   :doc "Internal validation"}
  [input]
  (and (string? input)
       (not (str/blank? input))))
(m/=> validate-input [:=> [:cat :string] :boolean])

;; Function with complex schema
(defn process-data
  [input & [verbose?]]
  (when verbose?
    (println "Processing:" (:id input)))
  (if (seq (:data input))
    [:ok "processed"]
    nil))
(m/=> process-data [:=> 
                    [:cat 
                     [:map 
                      [:id :uuid]
                      [:data [:vector :any]]
                      [:config [:? [:map-of :keyword :any]]]]
                     [:? :boolean]]
                    [:or 
                     [:tuple :keyword :string]
                     :nil]])

;; Multi-arity function
(defn flexible
  "A flexible function with multiple arities"
  {:since "1.0"}
  ([] nil)
  ([x] x)
  ([x y] (+ x y)))
(m/=> flexible [:function
                [:=> [:cat] :nil]
                [:=> [:cat :int] :int]
                [:=> [:cat :int :int] :int]])

;; Function without malli schema (should not be touched)
(defn regular-function
  "Just a regular function"
  {:some-other :metadata}
  [x]
  (* x 2))

;; Function with only malli schema in metadata
(defn minimal
  [k]
  (name k))
(m/=> minimal [:=> [:cat :keyword] :string])