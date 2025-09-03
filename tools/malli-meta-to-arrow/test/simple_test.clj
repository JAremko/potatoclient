(ns simple-test
  (:require [meta-to-arrow.simple-transform :as st]))

(def test-input
  "(ns test.ns)

(defn plus
  {:malli/schema [:=> [:cat :int :int] :int]}
  [x y]
  (+ x y))")

(println "Testing simple transformation:")
(let [result (st/transform-file test-input {:require-alias "m"})]
  (println "Count:" (:count result))
  (println "Transformed:")
  (println (:transformed result)))