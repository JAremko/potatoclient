(ns debug-zipper
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]))

(def test-input 
  "(ns test.ns)
   
(defn plus
  {:malli/schema [:=> [:cat :int :int] :int]}
  [x y]
  (+ x y))")

(println "Input:")
(println test-input)
(println)

(let [zloc (z/of-string test-input)]
  (println "Walking through forms:")
  (loop [loc zloc
         count 0]
    (when (and loc (not (z/end? loc)) (< count 20))
      (when (z/list? loc)
        (let [first-elem (z/down loc)]
          (when first-elem
            (println "Found list with first element:" (z/sexpr first-elem))
            (when (#{'defn 'defn-} (z/sexpr first-elem))
              (println "  -> It's a defn!")
              (let [name-loc (z/right first-elem)
                    name (z/sexpr name-loc)
                    after-name (z/right name-loc)]
                (println "  -> Name:" name)
                (println "  -> After name:" (z/sexpr after-name))
                (when (map? (z/sexpr after-name))
                  (println "  -> Has metadata map:" (z/sexpr after-name))))))))
      (recur (z/next loc) (inc count)))))