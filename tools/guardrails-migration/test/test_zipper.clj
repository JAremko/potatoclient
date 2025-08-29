(ns test-zipper
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]
            [clojure.string :as str]))

;; Test extracting and removing gspec
(def sample "(>defn foo [x] [int? => string?] (str x))")

(defn explore []
  (let [zloc (z/of-string sample)]
    (println "Original:" (z/string zloc))
    (println "Sexpr:" (z/sexpr zloc))
    
    ;; Navigate to parts
    (let [defn-sym (z/down zloc)
          fn-name (z/right defn-sym)
          args (z/right fn-name)
          gspec (z/right args)
          body (z/right gspec)]
      (println "defn-sym:" (z/string defn-sym) "=>" (z/sexpr defn-sym))
      (println "fn-name:" (z/string fn-name) "=>" (z/sexpr fn-name))
      (println "args:" (z/string args) "=>" (z/sexpr args))
      (println "gspec:" (z/string gspec) "=>" (z/sexpr gspec))
      (println "body:" (z/string body) "=>" (z/sexpr body))
      
      ;; Try to transform
      (println "\nTransforming:")
      
      ;; 1. Change >defn to defn
      (let [zloc1 (-> zloc z/down (z/replace 'defn) z/up)]
        (println "After changing >defn:" (z/string zloc1))
        
        ;; 2. Step by step transformation
        (println "\nStep by step:")
        (let [step1 (-> zloc1 z/down z/right z/right z/right)
              _ (println "At gspec:" (z/string step1))
              step2 (z/remove step1)
              _ (println "After remove, at:" (if (z/end? step2) "END" (z/string step2)))
              ;; After remove, we should be at the next node (str x)
              ;; But we need to check if we're at the end
              step3 (if (z/end? step2)
                     ;; If at end, go left
                     (-> zloc1 z/down z/right z/right) ; back to [x]
                     (-> step2 z/left z/left)) ; from (str x) to [x] to foo
              _ (println "Navigated to:" (z/string step3))
              step4 (z/insert-right step3 {:malli/schema [:=> [:cat :int] :string]})
              final (z/root step4)]
          (println "Final:" (n/string final)))))))

(explore)