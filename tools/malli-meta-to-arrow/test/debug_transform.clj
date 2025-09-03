(ns debug-transform
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]))

(def test-input 
  "(defn plus
  {:malli/schema [:=> [:cat :int :int] :int]}
  [x y]
  (+ x y))")

(println "Input:")
(println test-input)
(println)

(defn transform-single-defn
  "Debug version with logging"
  [zloc require-alias]
  (println "In transform-single-defn")
  (let [fn-name-loc (-> zloc z/down z/right)
        fn-name (z/sexpr fn-name-loc)
        _ (println "  Function name:" fn-name)
        after-name (z/right fn-name-loc)
        _ (println "  After name node:" (z/node after-name))
        _ (println "  After name sexpr:" (z/sexpr after-name))
        has-docstring? (and after-name (string? (z/sexpr after-name)))
        _ (println "  Has docstring?" has-docstring?)
        after-doc (if has-docstring? 
                    (z/right after-name) 
                    after-name)
        has-metadata? (and after-doc (map? (z/sexpr after-doc)))
        _ (println "  Has metadata?" has-metadata?)
        metadata (when has-metadata? (z/sexpr after-doc))
        _ (println "  Metadata:" metadata)
        schema (when metadata (:malli/schema metadata))
        _ (println "  Schema:" schema)]
    
    (if schema
      (do
        (println "  -> Found schema, transforming!")
        {:transformed? true
         :name fn-name
         :schema schema})
      (do
        (println "  -> No schema found")
        {:transformed? false}))))

(let [zloc (z/of-string test-input)]
  (println "Testing transformation:")
  (let [result (transform-single-defn zloc "m")]
    (println)
    (println "Result:" result)))