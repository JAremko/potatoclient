(ns generator.constraints-test
  "Test constraint extraction and compilation"
  (:require [clojure.test :refer :all]
            [generator.constraints.extractor :as extractor]
            [generator.constraints.compiler :as compiler]
            [generator.backend :as backend]))

(deftest test-constraint-extraction
  (testing "Extract constraints from field with buf.validate options"
    (let [;; Example field with float constraints
          field-with-constraints {:name "temperature"
                                 :type :type-float
                                 :options {"[buf.validate.field]" 
                                          {:float {:gte -50.0
                                                  :lte 150.0}}}}
          result (extractor/extract-and-normalize-constraints field-with-constraints)]
      
      (is (some? result))
      (is (= {:gte -50.0 :lte 150.0} (:field-constraints result)))
      (is (= {:gte -50.0 :lte 150.0} (get-in result [:raw :field])))))
  
  (testing "Extract string constraints with underscore to hyphen conversion"
    (let [field {:name "email"
                 :type :type-string
                 :options {"[buf.validate.field]"
                          {:string {:email true
                                   :min_len 5
                                   :max_len 100}}}}
          result (extractor/extract-and-normalize-constraints field)]
      
      (is (= {:email true :min-len 5 :max-len 100} (:field-constraints result)))))
  
  (testing "Extract repeated field constraints"
    (let [field {:name "items"
                 :type :type-string
                 :label :label-repeated
                 :options {"[buf.validate.field]"
                          {:repeated {:min_items 1
                                     :max_items 10
                                     :unique true}}}}
          result (extractor/extract-and-normalize-constraints field)]
      
      (is (= {:min-items 1 :max-items 10 :unique true} (:repeated-constraints result)))))
  
  (testing "No constraints returns nil"
    (let [field {:name "value" :type :type-int32}
          result (extractor/extract-and-normalize-constraints field)]
      (is (nil? result)))))

(deftest test-constraint-compilation
  (testing "Compile numeric constraints to Malli schema"
    (let [field {:type :type-float
                 :constraints {:field-constraints {:gte -50.0 :lte 150.0}}}
          compiled (compiler/compile-field-constraints field)]
      
      (is (= [[:>= -50.0] [:<= 150.0]] (:schema compiled)))
      (is (= {:min -50.0 :max 150.0} (:generator compiled)))))
  
  (testing "Compile string constraints with properties"
    (let [field {:type :type-string
                 :constraints {:field-constraints {:min-len 5 :max-len 50 :email true}}}
          compiled (compiler/compile-field-constraints field)]
      
      (is (= {:min 5 :max 50} (:props compiled)))
      (is (some #(and (vector? %) (= :re (first %))) (:schema compiled)))
      (is (= {:type :email} (:generator compiled)))))
  
  (testing "Compile enum constraints"
    (let [field {:type :type-enum
                 :constraints {:field-constraints {:in [:foo :bar :baz]}}}
          compiled (compiler/compile-field-constraints field)]
      
      (is (= [[:enum [:foo :bar :baz]]] (:schema compiled)))))
  
  (testing "Compile collection constraints"
    (let [field {:type :type-string
                 :label :label-repeated
                 :constraints {:repeated-constraints {:min-items 1 :max-items 10 :unique true}}}
          compiled (compiler/compile-field-constraints field)]
      
      (is (= {:min 1 :max 10} (:props compiled)))
      (is (some #(and (vector? %) (= :fn (first %))) (:schema compiled))))))

(deftest test-schema-enhancement
  (testing "Enhance base schema with numeric constraints"
    (let [base-schema :double
          compiled {:schema [[:>= 0] [:< 100]]}
          enhanced (compiler/enhance-schema-with-constraints base-schema compiled)]
      
      (is (= [:and :double [:>= 0] [:< 100]] enhanced))))
  
  (testing "Enhance string schema with properties"
    (let [base-schema :string
          compiled {:props {:min 5 :max 50}}
          enhanced (compiler/enhance-schema-with-constraints base-schema compiled)]
      
      (is (= [:string {:min 5 :max 50}] enhanced))))
  
  (testing "Enhance string with properties and constraints"
    (let [base-schema :string
          compiled {:props {:min 5} :schema [[:re #"^[a-z]+$"]]}
          enhanced (compiler/enhance-schema-with-constraints base-schema compiled)]
      
      ;; Should be [:and [:string {:min 5}] [:re #"^[a-z]+$"]]
      (is (vector? enhanced))
      (is (= :and (first enhanced)))
      (is (= [:string {:min 5}] (second enhanced)))
      (let [third-elem (nth enhanced 2)]
        (is (vector? third-elem))
        (is (= :re (first third-elem)))
        (is (= "^[a-z]+$" (.pattern (second third-elem)))))))
  
  (testing "No constraints returns base schema"
    (let [base-schema :int
          enhanced (compiler/enhance-schema-with-constraints base-schema nil)]
      
      (is (= :int enhanced)))))

(deftest test-end-to-end-constraint-application
  (testing "Full pipeline from field to enhanced schema"
    (let [;; Simulate a field from the backend
          field {:name "latitude"
                 :type :type-double
                 :options {"[buf.validate.field]" 
                          {:double {:gte -90.0 :lte 90.0}}}}
          ;; Extract constraints
          extracted-field (assoc field :constraints 
                                (extractor/extract-and-normalize-constraints field))
          ;; Apply to base schema
          base-schema :double
          enhanced (compiler/apply-constraints base-schema extracted-field)]
      
      ;; Debug what we're getting
      (is (some? (:constraints extracted-field)))
      (is (= {:gte -90.0 :lte 90.0} (get-in extracted-field [:constraints :field-constraints])))
      (is (= [:and :double [:>= -90.0] [:<= 90.0]] enhanced)))))