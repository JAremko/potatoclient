(ns explore-target
  (:require [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]
            [clojure.pprint :as pp]))

(defn explore-node
  "Print detailed info about a node and its children"
  [node indent]
  (let [prefix (apply str (repeat indent "  "))]
    (println (str prefix "Tag: " (n/tag node) 
                  (when (= :token (n/tag node))
                    (str " => " (pr-str (n/sexpr node))))
                  (when (= :string (n/tag node))
                    " => \"...\"")
                  (when (= :map (n/tag node))
                    " => {...}")
                  (when (= :vector (n/tag node))
                    (str " => [...]"))))
    (when (n/inner? node)
      (doseq [child (n/children node)]
        (explore-node child (inc indent))))))

(defn explore-form
  "Explore a single form"
  [form-str]
  (println "\n========================================")
  (println "Form:" form-str)
  (println "========================================")
  (let [parsed (p/parse-string form-str)]
    (explore-node parsed 0)
    (println "\nSexpr:" (pr-str (n/sexpr parsed)))))

;; Let's explore target patterns (what we want to create)
(println "\n\n=== EXPLORING TARGET DEFN PATTERNS ===\n")

;; 1. Simple function
(explore-form "(defn foo {:malli/schema [:=> [:cat :int] :string]} [x] (str x))")

;; 2. Function with docstring
(explore-form "(defn bar
  \"Does something\"
  {:malli/schema [:=> [:cat :int] :string]}
  [x]
  (str x))")

;; 3. Function with metadata (merged)
(explore-form "(defn baz
  {:author \"me\"
   :malli/schema [:=> [:cat :int] :string]}
  [x]
  (str x))")

;; 4. Function with docstring AND metadata
(explore-form "(defn qux
  \"Does something\"
  {:author \"me\"
   :malli/schema [:=> [:cat :int] :string]}
  [x]
  (str x))")

;; 5. Multi-arity
(explore-form "(defn multi
  {:malli/schema [:function
                  [:=> [:cat :int] :int]
                  [:=> [:cat :int :int] :int]]}
  ([x]
   (* x 2))
  ([x y]
   (+ x y)))")

;; 6. Private function  
(explore-form "(defn- private-fn
  {:malli/schema [:=> [:cat :any] :nil]}
  [x]
  nil)")

;; 7. No args function with docstring
(explore-form "(defn no-args
  \"A function with no arguments\"
  {:malli/schema [:=> [:cat] :string]}
  []
  \"hello\")")

;; 8. Maybe spec
(explore-form "(defn with-maybe
  {:malli/schema [:=> [:cat :int [:maybe :string]] :string]}
  [x y]
  (str x y))")