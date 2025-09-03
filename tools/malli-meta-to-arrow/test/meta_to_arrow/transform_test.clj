(ns meta-to-arrow.transform-test
  (:require [clojure.test :refer [deftest testing is]]
            [meta-to-arrow.transform :as transform]
            [clojure.string :as str]))

;; ============================================================================
;; Test Cases
;; ============================================================================

(deftest simple-defn-with-metadata
  (testing "Simple defn with :malli/schema metadata"
    (let [input "(ns test.ns)
                 
                 (defn plus
                   {:malli/schema [:=> [:cat :int :int] :int]}
                   [x y]
                   (+ x y))"
          
          expected "(ns test.ns
  (:require [malli.core :as m]))
                 
                 (defn plus
                   [x y]
                   (+ x y))
(m/=> plus [:=> [:cat :int :int] :int])"
          
          result (transform/transform-file input {})]
      
      (is (= 1 (:count result)))
      (is (str/includes? (:transformed result) "(m/=> plus"))
      (is (str/includes? (:transformed result) "(:require [malli.core :as m])")))))

(deftest defn-with-docstring-and-metadata
  (testing "defn with docstring and metadata"
    (let [input "(ns test.ns)
                 
                 (defn multiply
                   \"Multiplies two numbers\"
                   {:malli/schema [:=> [:cat :number :number] :number]
                    :other-meta \"value\"}
                   [x y]
                   (* x y))"
          
          result (transform/transform-file input {})]
      
      (is (= 1 (:count result)))
      (is (str/includes? (:transformed result) "(m/=> multiply"))
      (is (str/includes? (:transformed result) "\"Multiplies two numbers\""))
      (is (str/includes? (:transformed result) ":other-meta \"value\""))
      (is (not (str/includes? (:transformed result) ":malli/schema"))))))

(deftest defn-private-with-metadata
  (testing "defn- with metadata"
    (let [input "(ns test.ns)
                 
                 (defn- helper
                   {:malli/schema [:=> [:cat :string] :keyword]}
                   [s]
                   (keyword s))"
          
          result (transform/transform-file input {})]
      
      (is (= 1 (:count result)))
      (is (str/includes? (:transformed result) "(defn- helper"))
      (is (str/includes? (:transformed result) "(m/=> helper")))))

(deftest multiple-defns-with-metadata
  (testing "Multiple defns with metadata in same file"
    (let [input "(ns test.ns)
                 
                 (defn first-fn
                   {:malli/schema [:=> [:cat :int] :int]}
                   [x]
                   (inc x))
                 
                 (defn second-fn
                   {:malli/schema [:=> [:cat :string] :keyword]}
                   [s]
                   (keyword s))
                 
                 (defn third-fn
                   ;; No malli schema
                   [x y]
                   (+ x y))"
          
          result (transform/transform-file input {})]
      
      (is (= 2 (:count result)))
      (is (str/includes? (:transformed result) "(m/=> first-fn"))
      (is (str/includes? (:transformed result) "(m/=> second-fn"))
      (is (not (str/includes? (:transformed result) "(m/=> third-fn"))))))

(deftest preserve-existing-malli-require
  (testing "Preserve existing malli.core require with different alias"
    (let [input "(ns test.ns
                   (:require [malli.core :as malli]))
                 
                 (defn plus
                   {:malli/schema [:=> [:cat :int :int] :int]}
                   [x y]
                   (+ x y))"
          
          result (transform/transform-file input {:require-alias "malli"})]
      
      (is (= 1 (:count result)))
      (is (str/includes? (:transformed result) "(malli/=> plus"))
      ;; Should not add duplicate require
      (is (= 1 (count (re-seq #"\[malli\.core" (:transformed result))))))))

(deftest complex-schema-preservation
  (testing "Complex nested schema is preserved correctly"
    (let [input "(ns test.ns)
                 
                 (defn process
                   {:malli/schema [:=> 
                                   [:cat 
                                    [:map 
                                     [:id :uuid]
                                     [:name :string]
                                     [:tags [:vector :keyword]]]
                                    [:? :boolean]]
                                   [:or :nil [:map [:status :keyword]]]]}
                   [data & [verbose?]]
                   {:status :ok})"
          
          result (transform/transform-file input {})]
      
      (is (= 1 (:count result)))
      (is (str/includes? (:transformed result) "[:map [:status :keyword]]")))))

(deftest metadata-only-malli-schema
  (testing "Metadata with only :malli/schema gets removed entirely"
    (let [input "(ns test.ns)
                 
                 (defn simple
                   {:malli/schema [:=> [:cat] :any]}
                   []
                   :done)"
          
          result (transform/transform-file input {})]
      
      (is (= 1 (:count result)))
      ;; The metadata map should be completely removed
      (is (not (re-find #"\{.*\}" (:transformed result))))
      (is (str/includes? (:transformed result) "(m/=> simple")))))

(deftest custom-require-alias
  (testing "Using custom require alias"
    (let [input "(ns test.ns)
                 
                 (defn calc
                   {:malli/schema [:=> [:cat :int :int] :int]}
                   [a b]
                   (+ a b))"
          
          result (transform/transform-file input {:require-alias "mal"})]
      
      (is (str/includes? (:transformed result) "[malli.core :as mal]"))
      (is (str/includes? (:transformed result) "(mal/=> calc")))))

(deftest preserve-formatting
  (testing "Preserve indentation and spacing"
    (let [input "(ns test.ns)

                 ;; A helper function
                 (defn helper
                   \"Does something helpful\"
                   {:malli/schema [:=> [:cat :int] :int]
                    :private true}
                   [x]
                   ;; Implementation
                   (let [result (* x 2)]
                     result))"
          
          result (transform/transform-file input {})]
      
      (is (str/includes? (:transformed result) ";; A helper function"))
      (is (str/includes? (:transformed result) ";; Implementation"))
      (is (str/includes? (:transformed result) "\"Does something helpful\"")))))

(deftest multi-arity-function-schema
  (testing "Multi-arity function schema"
    (let [input "(ns test.ns)
                 
                 (defn multi
                   {:malli/schema [:function
                                   [:=> [:cat :int] :int]
                                   [:=> [:cat :int :int] :int]]}
                   ([x] (inc x))
                   ([x y] (+ x y)))"
          
          result (transform/transform-file input {})]
      
      (is (= 1 (:count result)))
      (is (str/includes? (:transformed result) "[:function"))
      (is (str/includes? (:transformed result) "(m/=> multi")))))

(deftest no-ns-form
  (testing "File without ns form"
    (let [input "(defn standalone
                   {:malli/schema [:=> [:cat] :nil]}
                   []
                   nil)"
          
          result (transform/transform-file input {})]
      
      ;; Should still transform but without adding require
      (is (= 1 (:count result)))
      (is (str/includes? (:transformed result) "(m/=> standalone"))
      (is (not (str/includes? (:transformed result) ":require"))))))

(deftest empty-file
  (testing "Empty file returns unchanged"
    (let [input ""
          result (transform/transform-file input {})]
      
      (is (= 0 (:count result)))
      (is (= "" (:transformed result))))))

(deftest file-with-no-schemas
  (testing "File with defns but no malli schemas"
    (let [input "(ns test.ns)
                 
                 (defn regular-fn [x] x)
                 (defn another-fn [x y] (+ x y))"
          
          result (transform/transform-file input {})]
      
      (is (= 0 (:count result)))
      ;; Should not add require if no transformations
      (is (not (str/includes? (:transformed result) "malli.core"))))))

;; Run tests with: clojure -M:test