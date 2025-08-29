(ns explore-zipper
  (:require [rewrite-clj.zip :as z]
            [clojure.string :as str]))

(defn explore-navigation
  "Navigate through a form with zipper and show what we find"
  [form-str description]
  (println "\n========================================")
  (println description)
  (println "Form:" form-str)
  (println "========================================")
  (let [zloc (z/of-string form-str)]
    
    ;; Start at the list
    (println "At root:" (z/tag zloc) "=>" (z/string zloc))
    
    ;; Go down to first element (>defn)
    (let [loc1 (z/down zloc)]
      (println "Down to first:" (z/tag loc1) "=>" (z/sexpr loc1))
      
      ;; Go right to function name
      (let [loc2 (z/right loc1)]
        (println "Right to name:" (z/tag loc2) "=>" (z/sexpr loc2))
        
        ;; Go right to next element
        (let [loc3 (z/right loc2)]
          (println "Right again:" (z/tag loc3) "=>" 
                   (if (string? (z/sexpr loc3))
                     "\"...\""
                     (z/sexpr loc3)))
          
          ;; Continue navigating
          (loop [loc loc3
                 n 1]
            (when (and loc (not (z/end? loc)) (< n 10))
              (let [next-loc (z/right loc)]
                (when next-loc
                  (println (str "Right #" n ":") (z/tag next-loc) "=>"
                           (cond
                             (string? (z/sexpr next-loc)) "\"...\""
                             (z/vector? next-loc) "[...]"
                             (z/map? next-loc) "{...}"
                             (z/list? next-loc) "(...)"
                             :else (z/sexpr next-loc)))
                  (recur next-loc (inc n))))))))))
  
  ;; Now let's find the gspec
  (println "\nFinding gspec:")
  (let [zloc (z/of-string form-str)
        gspec-loc (z/find zloc z/next 
                         #(and (z/vector? %)
                               (str/includes? (z/string %) "=>")))]
    (if gspec-loc
      (do
        (println "Found gspec:" (z/sexpr gspec-loc))
        (println "After removing:")
        (let [removed (z/remove gspec-loc)]
          (if (z/end? removed)
            (println "At end after remove")
            (println "Now at:" (z/tag removed) "=>" (z/sexpr removed))))
        
        ;; Try subedit
        (println "\nUsing subedit to remove gspec:")
        (let [edited (z/subedit-> zloc
                                  (z/find z/next #(and (z/vector? %)
                                                      (str/includes? (z/string %) "=>")))
                                  z/remove)]
          (println "Result:" (z/string edited))))
      (println "No gspec found"))))

;; Test different patterns
(explore-navigation 
 "(>defn foo [x] [int? => string?] (str x))"
 "1. Simple function")

(explore-navigation
 "(>defn bar
  \"Does something\"
  [x]
  [int? => string?]
  (str x))"
 "2. Function with docstring")

(explore-navigation
 "(>defn baz
  {:author \"me\"}
  [x]
  [int? => string?]
  (str x))"
 "3. Function with metadata")

(explore-navigation
 "(>defn multi
  ([x]
   [int? => int?]
   (* x 2))
  ([x y]
   [int? int? => int?]
   (+ x y)))"
 "4. Multi-arity")