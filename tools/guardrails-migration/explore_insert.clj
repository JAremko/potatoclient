(ns explore-insert
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]
            [clojure.string :as str]))

(defn test-insertion
  "Test different insertion strategies"
  [form-str description]
  (println "\n========================================")
  (println description)
  (println "Original:" form-str)
  (println "========================================")
  
  ;; Strategy 1: Find name, insert right
  (println "\nStrategy 1: Insert after name")
  (try
    (let [zloc (z/of-string form-str)
          result (z/subedit-> zloc
                             z/down ; to >defn
                             z/right ; to name
                             (z/insert-right {:malli/schema [:=> [:cat :int] :string]}))]
      (println "Success:" (z/string result)))
    (catch Exception e
      (println "Failed:" (.getMessage e))))
  
  ;; Strategy 2: Check what's after name first
  (println "\nStrategy 2: Check position before insert")
  (let [zloc (z/of-string form-str)
        name-loc (-> zloc z/down z/right)
        after-name (z/right name-loc)]
    (println "After name is:" (z/tag after-name) "=>" 
             (cond
               (string? (z/sexpr after-name)) "docstring"
               (map? (z/sexpr after-name)) "metadata"
               (vector? (z/sexpr after-name)) "args"
               :else (z/sexpr after-name)))
    
    ;; Decide where to insert based on what's there
    (let [insert-point (cond
                        ;; If docstring, insert after it
                        (string? (z/sexpr after-name))
                        after-name
                        
                        ;; If existing metadata, we need to merge
                        (map? (z/sexpr after-name))
                        :merge-needed
                        
                        ;; Otherwise insert after name
                        :else
                        name-loc)]
      (if (= :merge-needed insert-point)
        (println "Need to merge with existing metadata")
        (try
          (let [result (z/subedit-> zloc
                                    z/down ; to >defn
                                    z/right ; to name
                                    (z/find-value z/right (z/sexpr insert-point))
                                    (z/insert-right {:malli/schema [:=> [:cat :int] :string]}))]
            (println "Inserted after" (z/sexpr insert-point) ":" (z/string result)))
          (catch Exception e
            (println "Failed to insert:" (.getMessage e)))))))
  
  ;; Strategy 3: Edit existing metadata
  (when (str/includes? form-str "{")
    (println "\nStrategy 3: Edit existing metadata")
    (let [zloc (z/of-string form-str)
          meta-loc (z/find zloc z/next z/map?)]
      (when meta-loc
        (let [edited (z/edit meta-loc assoc :malli/schema [:=> [:cat :int] :string])]
          (println "Edited metadata:" (z/string (z/up edited))))))))

;; Test cases
(test-insertion
 "(>defn foo [x] [int? => string?] (str x))"
 "Simple function")

(test-insertion
 "(>defn bar \"Does something\" [x] [int? => string?] (str x))"
 "Function with docstring")

(test-insertion
 "(>defn baz {:author \"me\"} [x] [int? => string?] (str x))"
 "Function with metadata")

(test-insertion
 "(>defn qux \"Doc\" {:author \"me\"} [x] [int? => string?] (str x))"
 "Function with both")