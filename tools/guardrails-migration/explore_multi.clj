(ns explore-multi
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]
            [clojure.string :as str]))

(defn explore-multi-arity
  "Explore multi-arity function structure"
  []
  (let [form-str "(>defn multi
  ([x]
   [int? => int?]
   (* x 2))
  ([x y]
   [int? int? => int?]
   (+ x y)))"
        zloc (z/of-string form-str)]
    
    (println "Original form:")
    (println form-str)
    (println "\n========================================")
    
    ;; Navigate to each arity
    (println "Navigation:")
    (let [name-loc (-> zloc z/down z/right)]
      (println "At name:" (z/sexpr name-loc))
      
      (let [first-arity (z/right name-loc)]
        (println "First arity is list?" (z/list? first-arity))
        (println "First arity:" (z/string first-arity))
        
        ;; Go into first arity
        (let [inside-first (z/down first-arity)]
          (println "  Inside first arity:" (z/tag inside-first) "=>" (z/sexpr inside-first))
          
          (let [first-gspec (z/right inside-first)]
            (println "  Gspec in first:" (z/sexpr first-gspec))
            
            ;; Remove gspec from first arity
            (let [no-gspec (z/remove first-gspec)]
              (println "  After removing gspec, at:" (z/tag no-gspec))
              (println "  Parent after remove:" (z/string (z/up no-gspec))))))
        
        (let [second-arity (z/right first-arity)]
          (println "\nSecond arity:" (z/string second-arity))
          
          ;; Process second arity
          (let [inside-second (z/down second-arity)
                second-gspec (z/right inside-second)]
            (println "  Gspec in second:" (z/sexpr second-gspec)))))))
  
  ;; Now try to process all arities
  (println "\n\nProcessing all arities:")
  (let [form-str "(>defn multi
  ([x]
   [int? => int?]
   (* x 2))
  ([x y]
   [int? int? => int?]
   (+ x y)))"
        zloc (z/of-string form-str)
        gspecs (atom [])]
    
    ;; Find all gspecs and collect them
    (let [processed (z/prewalk
                     zloc
                     ;; Find gspec vectors in arity lists
                     (fn [loc]
                       (and (z/vector? loc)
                            (str/includes? (z/string loc) "=>")
                            ;; Make sure parent is a list (arity)
                            (z/list? (z/up loc))))
                     ;; Collect and remove
                     (fn [loc]
                       (swap! gspecs conj (z/sexpr loc))
                       (z/remove loc)))]
      
      (println "Collected gspecs:" @gspecs)
      (println "\nAfter removing all gspecs:")
      (println (z/string processed))
      
      ;; Build function schema
      (println "\nBuilding function schema:")
      (let [schemas (map (fn [gspec]
                          (let [arrow-idx (.indexOf gspec '=>)
                                args (subvec gspec 0 arrow-idx)
                                ret (get gspec (inc arrow-idx))]
                            [:=> (into [:cat] args) ret]))
                        @gspecs)
            function-schema (if (= 1 (count schemas))
                             (first schemas)
                             (into [:function] schemas))]
        (println "Schema:" function-schema)
        
        ;; Now add the metadata
        (println "\nAdding metadata:")
        (let [with-meta (z/subedit-> processed
                                     z/down ; to >defn
                                     z/right ; to name
                                     (z/insert-right {:malli/schema function-schema}))]
          (println (z/string with-meta))))))
  
  ;; Test with docstring and metadata
  (println "\n\n========================================")
  (println "Multi-arity with docstring and metadata:")
  (let [form-str "(>defn complex
  \"Does multiple things\"
  {:author \"me\"}
  ([x]
   [int? => int?]
   x)
  ([x y]
   [int? int? => int?]
   (+ x y)))"
        zloc (z/of-string form-str)]
    
    (println form-str)
    
    ;; Check what comes after name
    (let [name-loc (-> zloc z/down z/right)
          after-name (z/right name-loc)
          after-after (z/right after-name)]
      (println "\nAfter name:" (z/tag after-name) "=>" 
               (if (string? (z/sexpr after-name)) "docstring" (z/sexpr after-name)))
      (println "After that:" (z/tag after-after) "=>" 
               (if (map? (z/sexpr after-after)) "metadata" (z/sexpr after-after))))))