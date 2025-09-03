(ns test-working
  (:require [meta-to-arrow.working-transform :as transform]))

(def test-input 
  "(ns test.ns)
   
(defn plus
  {:malli/schema [:=> [:cat :int :int] :int]}
  [x y]
  (+ x y))

(defn minus
  \"Subtracts numbers\"
  {:malli/schema [:=> [:cat :int :int] :int]
   :other :metadata}
  [x y]
  (- x y))")

(println "Testing working transformation:")
(println "Input:")
(println test-input)
(println)

(let [result (transform/transform-file test-input {})]
  (println "Result:")
  (println "Count:" (:count result))
  (println "Names:" (:additions result))
  (println)
  (println "Transformed:")
  (println (:transformed result)))