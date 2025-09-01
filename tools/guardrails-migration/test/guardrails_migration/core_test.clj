(ns guardrails-migration.core-test
  "Comprehensive test suite for Guardrails to Malli migration"
  (:require [clojure.test :refer [deftest testing is are]]
            [guardrails-migration.core :as migration]
            [guardrails-migration.specs :as specs]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]))

;; ============================================================================
;; Test cases for different function patterns
;; ============================================================================

(def test-cases
  "Map of test case name to [input expected-output]"
  {:simple-unary
   ["(>defn simple-fn
       [x]
       [int? => string?]
       (str x))"
    "(defn simple-fn
       {:malli/schema [:=> [:cat :int] :string]}
       [x]
       (str x))"]
   
   :simple-with-docstring
   ["(>defn with-doc
       \"This is a docstring\"
       [x]
       [int? => string?]
       (str x))"
    "(defn with-doc
       \"This is a docstring\"
       {:malli/schema [:=> [:cat :int] :string]}
       [x]
       (str x))"]
   
   :simple-with-attr-map
   ["(>defn with-attrs
       {:some-meta true}
       [x]
       [int? => string?]
       (str x))"
    "(defn with-attrs
       {:some-meta true
        :malli/schema [:=> [:cat :int] :string]}
       [x]
       (str x))"]
   
   :with-doc-and-attrs
   ["(>defn full-single
       \"Docstring here\"
       {:meta-key :value}
       [x y]
       [int? string? => keyword?]
       (keyword (str x y)))"
    "(defn full-single
       \"Docstring here\"
       {:meta-key :value
        :malli/schema [:=> [:cat :int :string] :keyword]}
       [x y]
       (keyword (str x y)))"]
   
   :private-function
   ["(>defn- private-fn
       [x]
       [int? => int?]
       (inc x))"
    "(defn- private-fn
       {:malli/schema [:=> [:cat :int] :int]}
       [x]
       (inc x))"]
   
   :multi-arity
   ["(>defn multi
       ([x]
        [int? => int?]
        (inc x))
       ([x y]
        [int? int? => int?]
        (+ x y)))"
    "(defn multi
       {:malli/schema [:function
                       [:=> [:cat :int] :int]
                       [:=> [:cat :int :int] :int]]}
       ([x]
        (inc x))
       ([x y]
        (+ x y)))"]
   
   :variadic-args
   ["(>defn variadic
       [x & xs]
       [int? [:* int?] => int?]
       (apply + x xs))"
    "(defn variadic
       {:malli/schema [:=> [:cat :int [:* :int]] :int]}
       [x & xs]
       (apply + x xs))"]
   
   :such-that-clause
   ["(>defn with-such-that
       [x]
       [int? | #(> % 0) => int? | #(< % 100)]
       (* x 2))"
    "(defn with-such-that
       {:malli/schema [:=> [:cat :int] :int]}
       [x]
       (* x 2))"]
   
   :nilable-spec
   ["(>defn nilable-arg
       [x]
       [(? int?) => string?]
       (str x))"
    "(defn nilable-arg
       {:malli/schema [:=> [:cat [:maybe :int]] :string]}
       [x]
       (str x))"]
   
   :keyword-specs
   ["(>defn keyword-specs
       [x]
       [::my-spec => ::result-spec]
       (process x))"
    "(defn keyword-specs
       {:malli/schema [:=> [:cat ::my-spec] ::result-spec]}
       [x]
       (process x))"]
   
   :complex-specs
   ["(>defn complex
       [m]
       [[:map [:x int?] [:y string?]] => [:vector keyword?]]
       (mapv keyword (vals m)))"
    "(defn complex
       {:malli/schema [:=> [:cat [:map [:x :int] [:y :string]]] [:vector :keyword]]}
       [m]
       (mapv keyword (vals m)))"]
   
   :multi-arity-mixed
   ["(>defn mixed-multi
       \"Function with mixed arities\"
       ([x]
        [int? => int?]
        x)
       ([x y]
        [int? string? => string?]
        (str x y))
       ([x y z]
        [int? string? keyword? => map?]
        {:x x :y y :z z}))"
    "(defn mixed-multi
       \"Function with mixed arities\"
       {:malli/schema [:function
                       [:=> [:cat :int] :int]
                       [:=> [:cat :int :string] :string]
                       [:=> [:cat :int :string :keyword] :map]]}
       ([x]
        x)
       ([x y]
        (str x y))
       ([x y z]
        {:x x :y y :z z}))"]
   
   :no-gspec
   ["(>defn no-spec
       [x]
       (inc x))"
    "(defn no-spec
       [x]
       (inc x))"]
   
   :empty-body
   ["(>defn empty-body
       [x]
       [int? => nil?])"
    "(defn empty-body
       {:malli/schema [:=> [:cat :int] :nil]}
       [x])"]})

;; ============================================================================
;; Test helper functions
;; ============================================================================

(defn normalize-whitespace
  "Normalize whitespace for comparison"
  [s]
  (-> s
      (clojure.string/replace #"\s+" " ")
      (clojure.string/trim)))

(defn forms-equal?
  "Compare two code forms ignoring whitespace differences"
  [form1 form2]
  (= (normalize-whitespace form1)
     (normalize-whitespace form2)))

;; ============================================================================
;; Main test suite
;; ============================================================================

(deftest test-migration-all-cases
  (doseq [[case-name [input expected]] test-cases]
    (testing (str "Case: " (name case-name))
      (let [result (migration/migrate-form input)]
        (is (forms-equal? result expected)
            (str "Failed for case: " (name case-name)
                 "\nExpected: " expected
                 "\nGot: " result))))))

(deftest test-specs-validation
  (testing "Guardrails form validation"
    (is (specs/valid-guardrails-form? 
         '(>defn foo [x] [int? => string?] (str x))))
    (is (not (specs/valid-guardrails-form?
              '(defn foo [x] [int? => string?] (str x))))))
  
  (testing "Output form validation"
    (is (specs/valid-output-form?
         '(defn foo {:malli/schema [:=> [:cat :int] :string]} [x] (str x))))
    (is (not (specs/valid-output-form?
              '(>defn foo [x] [int? => string?] (str x)))))))

(deftest test-gspec-parsing
  (testing "Parse simple gspec"
    (let [gspec '[int? => string?]]
      (is (= {:args '[int?] :ret 'string?}
             (migration/parse-gspec gspec)))))
  
  (testing "Parse gspec with multiple args"
    (let [gspec '[int? string? keyword? => map?]]
      (is (= {:args '[int? string? keyword?] :ret 'map?}
             (migration/parse-gspec gspec)))))
  
  (testing "Parse gspec with such-that"
    (let [gspec '[int? | #(> % 0) => string?]]
      (is (= {:args '[int?] 
              :arg-preds '[#(> % 0)]
              :ret 'string?}
             (migration/parse-gspec gspec)))))
  
  (testing "Parse variadic gspec"
    (let [gspec '[int? [:* int?] => int?]]
      (is (= {:args '[int? [:* int?]] :ret 'int?}
             (migration/parse-gspec gspec)))))
  
  (testing "Parse nilable spec"
    (let [gspec '[(? int?) => string?]]
      (is (= {:args '[[:maybe int?]] :ret 'string?}
             (migration/parse-gspec gspec))))))

(deftest test-file-migration
  (testing "Migrate entire file"
    (let [input-file "test/resources/sample_guardrails.clj"
          output-file "/tmp/migrated.clj"
          _ (spit input-file
                  "(ns sample.ns
                     (:require [com.fulcrologic.guardrails.malli.core :refer [>defn =>]]))
                   
                   (>defn foo [x] [int? => string?] (str x))
                   
                   (defn regular [x] (inc x))
                   
                   (>defn- bar [x y] [int? string? => keyword?] (keyword y))")
          result (migration/migrate-file input-file output-file)]
      (is (= :success (:status result)))
      (let [output (slurp output-file)]
        (is (not (re-find #">defn" output)))
        (is (re-find #":malli/schema" output))))))

;; Run tests with: clojure -M:test