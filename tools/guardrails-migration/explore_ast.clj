(ns explore-ast
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
                    (str " => " (pr-str (n/sexpr node))))))
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

;; Let's explore different patterns
(println "\n\n=== EXPLORING DIFFERENT >DEFN PATTERNS ===\n")

;; 1. Simple function
(explore-form "(>defn foo [x] [int? => string?] (str x))")

;; 2. Function with docstring
(explore-form "(>defn bar
  \"Does something\"
  [x]
  [int? => string?]
  (str x))")

;; 3. Function with metadata
(explore-form "(>defn baz
  {:author \"me\"}
  [x]
  [int? => string?]
  (str x))")

;; 4. Function with docstring AND metadata
(explore-form "(>defn qux
  \"Does something\"
  {:author \"me\"}
  [x]
  [int? => string?]
  (str x))")

;; 5. Multi-arity
(explore-form "(>defn multi
  ([x]
   [int? => int?]
   (* x 2))
  ([x y]
   [int? int? => int?]
   (+ x y)))")

;; 6. Private function
(explore-form "(>defn- private-fn
  [x]
  [any? => nil?]
  nil)")

;; 7. No args function with docstring
(explore-form "(>defn no-args
  \"A function with no arguments\"
  []
  [=> string?]
  \"hello\")")

;; 8. Maybe spec
(explore-form "(>defn with-maybe
  [x y]
  [int? (? string?) => string?]
  (str x y))")