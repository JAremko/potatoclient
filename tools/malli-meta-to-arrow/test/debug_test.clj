(ns debug-test
  (:require [meta-to-arrow.final-transform :as transform]))

(def simple-input 
  "(ns test.ns)
   
   (defn plus
     {:malli/schema [:=> [:cat :int :int] :int]}
     [x y]
     (+ x y))")

(println "Testing simple transformation:")
(println "Input:")
(println simple-input)
(println)

(let [result (transform/transform-file simple-input {})]
  (println "Result:")
  (println "Count:" (:count result))
  (println "Transformed:")
  (println (:transformed result))
  (println "Additions:" (:additions result)))