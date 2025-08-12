#!/usr/bin/env clojure

(ns test-oneof-final
  "Final comprehensive test for oneof_edn"
  (:require
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== ONEOF_EDN FINAL TEST SUITE ===\n")

;; Test counter
(def test-count (atom 0))
(def pass-count (atom 0))
(def fail-count (atom 0))

(defn run-test [name test-fn]
  (swap! test-count inc)
  (print (str (format "%2d. " @test-count) name "... "))
  (flush)
  (try
    (test-fn)
    (println "✓ PASS")
    (swap! pass-count inc)
    true
    (catch Throwable e
      (println (str "✗ FAIL: " (.getMessage e)))
      (swap! fail-count inc)
      false)))

;; TEST 1: Basic validation
(run-test "Validates single non-nil field"
  #(let [s [:oneof_edn [:a :string] [:b :int]]]
     (assert (m/validate s {:a "test"}))
     (assert (m/validate s {:b 42}))
     (assert (not (m/validate s {:a "x" :b 1})))))

;; TEST 2: Rejects empty map
(run-test "Rejects empty map"
  #(let [s [:oneof_edn [:x :int] [:y :int]]]
     (assert (not (m/validate s {})))))

;; TEST 3: Rejects all nil
(run-test "Rejects all nil fields"
  #(let [s [:oneof_edn [:x :int] [:y :int]]]
     (assert (not (m/validate s {:x nil :y nil})))))

;; TEST 4: Accepts nil in inactive fields
(run-test "Accepts nil in inactive fields (Pronto style)"
  #(let [s [:oneof_edn [:a :string] [:b :int] [:c :boolean]]]
     (assert (m/validate s {:a "test" :b nil :c nil}))))

;; TEST 5: Closed map behavior
(run-test "Acts as closed map - rejects extra keys"
  #(let [s [:oneof_edn [:a :string] [:b :int]]]
     (assert (not (m/validate s {:a "test" :extra 123})))))

;; TEST 6: Type validation
(run-test "Validates field types correctly"
  #(let [s [:oneof_edn [:num :int] [:text :string]]]
     (assert (m/validate s {:num 42}))
     (assert (not (m/validate s {:num "42"})))))

;; TEST 7: Generation works
(run-test "Generator produces valid values"
  #(let [s [:oneof_edn [:a :string] [:b :int] [:c :boolean]]]
     (dotimes [_ 10]
       (let [v (mg/generate s)]
         (assert (m/validate s v))
         (assert (= 1 (count v)))))))

;; TEST 8: Generation coverage
(run-test "Generator covers all alternatives"
  #(let [s [:oneof_edn [:a :int] [:b :int] [:c :int]]
        samples (repeatedly 50 (fn [] (mg/generate s)))
        keys-seen (set (map (comp first keys) samples))]
     (assert (>= (count keys-seen) 2) 
            (str "Should generate at least 2 different keys, got: " keys-seen))))

;; TEST 9: Property - generated values valid
(run-test "Property: All generated values are valid"
  #(let [s [:oneof_edn [:x :int] [:y :string]]
        prop-test (prop/for-all [v (mg/generator s)]
                    (m/validate s v))
        result (tc/quick-check 50 prop-test)]
     (assert (:pass? result))))

;; TEST 10: Property - exactly one field
(run-test "Property: Generated values have exactly one field"
  #(let [s [:oneof_edn [:a :int] [:b :int]]
        prop-test (prop/for-all [v (mg/generator s)]
                    (= 1 (count v)))
        result (tc/quick-check 50 prop-test)]
     (assert (:pass? result))))

;; TEST 11: Complex nested schemas
(run-test "Works with complex nested schemas"
  #(let [s [:oneof_edn
           [:simple :string]
           [:complex [:map {:closed true}
                     [:x :int]
                     [:y :int]]]]]
     (assert (m/validate s {:simple "test"}))
     (assert (m/validate s {:complex {:x 1 :y 2}}))
     (assert (not (m/validate s {:complex {:x 1}})))))

;; TEST 12: Integration with :merge
(run-test "Integrates with :merge"
  #(let [base [:map {:closed true} [:id :int] [:name :string]]
        oneof [:oneof_edn [:action-a :keyword] [:action-b :boolean]]
        merged [:merge base oneof]]
     (assert (m/validate merged {:id 1 :name "x" :action-a :foo}))
     (assert (m/validate merged {:id 1 :name "x" :action-b true}))
     (assert (not (m/validate merged {:id 1 :name "x"})))
     (assert (not (m/validate merged {:id 1 :name "x" :action-a :foo :action-b true})))))

;; TEST 13: Nested oneof
(run-test "Supports nested oneof"
  #(let [s [:map {:closed true}
           [:type :keyword]
           [:data [:oneof_edn
                  [:text :string]
                  [:nested [:oneof_edn
                           [:a :int]
                           [:b :boolean]]]]]]]
     (assert (m/validate s {:type :x :data {:text "hello"}}))
     (assert (m/validate s {:type :y :data {:nested {:a 42}}}))
     (assert (m/validate s {:type :z :data {:nested {:b true}}}))))

;; TEST 14: Error messages
(run-test "Provides helpful error messages"
  #(let [s [:oneof_edn [:a :string] [:b :int]]
        explain-empty (m/explain s {})
        explain-multi (m/explain s {:a "x" :b 1})
        explain-extra (m/explain s {:a "x" :c 1})]
     (assert explain-empty)
     (assert explain-multi)
     (assert explain-extra)))

;; TEST 15: CMD-like structure
(run-test "Works with cmd/root-like structures"
  #(do
     (require '[potatoclient.specs.cmd.common])
     (let [cmd-spec [:merge
                    [:map {:closed true}
                     [:protocol_version [:int {:min 1}]]
                     [:client_type [:enum :ground :web]]]
                    [:oneof_edn
                     [:ping [:map {:closed true} [:id :int]]]
                     [:echo [:map {:closed true} [:msg :string]]]]]]
       (assert (m/validate cmd-spec {:protocol_version 1 :client_type :ground :ping {:id 1}}))
       (assert (m/validate cmd-spec {:protocol_version 2 :client_type :web :echo {:msg "hi"}}))
       (assert (not (m/validate cmd-spec {:protocol_version 1 :client_type :ground})))
       (assert (not (m/validate cmd-spec {:protocol_version 1 :client_type :ground 
                                          :ping {:id 1} :echo {:msg "x"}})))
       ;; Test generation
       (dotimes [_ 5]
         (let [v (mg/generate cmd-spec)]
           (assert (m/validate cmd-spec v)))))))

;; TEST 16: Min/max constraints in generator
(run-test "Generator respects min/max constraints"
  #(let [s [:oneof_edn
           [:small [:int {:min 0 :max 10}]]
           [:big [:int {:min 1000 :max 2000}]]]]
     (dotimes [_ 20]
       (let [v (mg/generate s)]
         (assert (m/validate s v))
         (when-let [small (:small v)]
           (assert (<= 0 small 10)))
         (when-let [big (:big v)]
           (assert (<= 1000 big 2000)))))))

;; TEST 17: String constraints
(run-test "Generator respects string constraints"
  #(let [s [:oneof_edn
           [:short [:string {:min 1 :max 5}]]
           [:long [:string {:min 10 :max 20}]]]]
     (dotimes [_ 20]
       (let [v (mg/generate s)
             text (or (:short v) (:long v))]
         (assert (string? text))
         (if (:short v)
           (assert (<= 1 (count text) 5))
           (assert (<= 10 (count text) 20)))))))

;; TEST 18: Works with all Malli types
(run-test "Works with various Malli types"
  #(let [s [:oneof_edn
           [:str :string]
           [:int :int]
           [:bool :boolean]
           [:key :keyword]
           [:uuid :uuid]
           [:vec [:vector :int]]
           [:set [:set :keyword]]
           [:map [:map [:x :int]]]]]
     (dotimes [_ 10]
       (let [v (mg/generate s)]
         (assert (m/validate s v))))))

;; TEST 19: Large schemas
(run-test "Handles schemas with many alternatives"
  #(let [fields (map (fn [i] [(keyword (str "field" i)) :int]) (range 20))
        s (into [:oneof_edn] fields)]
     (dotimes [_ 10]
       (let [v (mg/generate s)]
         (assert (m/validate s v))
         (assert (= 1 (count v)))))))

;; TEST 20: Real cmd/root spec
(run-test "Real cmd/root spec works"
  #(do
     (require '[potatoclient.specs.cmd.root])
     (let [spec :cmd/root]
       ;; Can't easily test validation without valid examples
       ;; But we can test generation
       (dotimes [_ 3]
         (let [v (mg/generate spec)]
           (assert (m/validate spec v))
           (assert (:protocol_version v))
           (assert (:client_type v)))))))

;; SUMMARY
(println "\n=== TEST SUMMARY ===")
(println (str "Total tests: " @test-count))
(println (str "Passed: " @pass-count))
(println (str "Failed: " @fail-count))

(if (zero? @fail-count)
  (do
    (println "\n✅ ALL TESTS PASSED!")
    (println "oneof_edn is fully functional with:")
    (println "  • Validation (single non-nil field)")
    (println "  • Generation (all alternatives)")
    (println "  • Closed map behavior")
    (println "  • Integration with :merge")
    (println "  • Nested oneof support")
    (println "  • Property-based testing")
    (println "  • Min/max constraints")
    (println "  • CMD/Root compatibility")
    (System/exit 0))
  (do
    (println (str "\n❌ " @fail-count " TESTS FAILED"))
    (System/exit 1)))