(ns test-actual-patterns
  "Test migration with actual patterns from the codebase"
  (:require [guardrails-migration.simple :as s]
            [clojure.string :as str]))

;; Test namespace cleaning
(def test-ns-form
  '(ns potatoclient.config
     "Configuration management"
     (:require [clojure.edn :as edn]
               [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
               [malli.core :as m])))

(def expected-ns-form
  '(ns potatoclient.config
     "Configuration management"
     (:require [clojure.edn :as edn]
               [malli.core :as m])))

(defn test-ns-cleaning []
  (println "Testing namespace form cleaning:")
  (let [result (s/clean-ns-form test-ns-form)]
    (println "Input:" (pr-str test-ns-form))
    (println "Output:" (pr-str result))
    (println "Expected:" (pr-str expected-ns-form))
    (println "Pass:" (= result expected-ns-form))
    (println "---")))

;; Test cases from actual codebase patterns
(def test-cases
  [;; Simple predicates
   '(>defn foo [x] [int? => string?] (str x))
   '(>defn foo [x] [string? => any?] x)
   '(>defn foo [] [=> any?] 42)
   
   ;; Nilable with (?)
   '(>defn foo [x] [(? string?) => any?] x)
   '(>defn foo [x y] [string? (? int?) => string?] (str x y))
   
   ;; Qualified keywords
   '(>defn foo [x] [:potatoclient.ui-specs/theme-key => any?] x)
   '(>defn foo [x] [:potatoclient.ui-specs/stream-key => any?] x)
   
   ;; Collection specs
   '(>defn foo [x] [[:sequential ifn?] => nil?] nil)
   '(>defn foo [x] [[:sequential string?] => string?] (first x))
   
   ;; Map specs
   '(>defn foo [x] [[:map [:version string?] [:timestamp string?]] => any?] x)
   
   ;; Function schemas with instance checks (already Malli)
   '(>defn foo [x] [[:fn {:error/message "must be a JFrame"} 
                     #(instance? JFrame %)] => nil?] nil)
   
   ;; Maybe with complex specs
   '(>defn foo [x] [[:maybe string?] => any?] x)
   '(>defn foo [x] [[:maybe [:fn {:error/message "test"} #(pos? %)]] => any?] x)
   
   ;; Variadic
   '(>defn foo [x & xs] [string? [:* any?] => string?] (str x))
   
   ;; Multi-arity
   '(>defn foo 
     ([x] [int? => int?] x)
     ([x y] [int? int? => int?] (+ x y)))
   
   ;; Private functions
   '(>defn- private-foo [x] [any? => nil?] nil)
   
   ;; With docstring
   '(>defn foo "Does something" [x] [any? => any?] x)
   
   ;; With metadata
   '(>defn foo {:author "test"} [x] [any? => any?] x)
   
   ;; Complex nested collections
   '(>defn foo [x] [[:map-of keyword? [:vector string?]] => any?] x)
   
   ;; Instance checks (already Malli)
   '(>defn foo [x] [[:fn #(instance? java.io.File %)] => nil?] nil)
   
   ;; Multiple variadics (edge case)
   '(>defn foo [x y & zs] [int? string? [:* keyword?] => any?] x)
   
   ;; Empty gspec (no validation)
   '(>defn foo [x] [] x)])

(defn test-all []
  (println "Testing actual patterns from codebase:")
  (println)
  (doseq [form test-cases]
    (println "Input:" (pr-str form))
    (let [result (s/transform-form form)]
      (println "Output:" (pr-str result))
      (println))
    (println "---")))

;; Run tests
(test-ns-cleaning)
(println)
(test-all)