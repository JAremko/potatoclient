(ns validate.specs.oneof-edn-test
  "Tests for oneof-edn schema with nil value handling"
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.error :as me]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize the global registry with oneof-edn schema
(defn init-registry! []
  (registry/setup-global-registry!
    (oneof-edn/register_ONeof-edn-schema!)))

(init-registry!)

(deftest oneof-edn-nil-handling-test
  (testing "oneof-edn treats nil values as absent"
    (let [schema [:oneof_edn
                  [:field-a :string]
                  [:field-b :int]
                  [:field-c [:map [:x :int]]]]]
      
      (testing "Valid cases - exactly one non-nil field"
        (is (m/validate schema {:field-a "test"}))
        (is (m/validate schema {:field-b 42}))
        (is (m/validate schema {:field-c {:x 10}})))
      
      (testing "Valid with nil values in other fields (Pronto EDN style)"
        (is (m/validate schema {:field-a "test" :field-b nil :field-c nil}))
        (is (m/validate schema {:field-a nil :field-b 42 :field-c nil}))
        (is (m/validate schema {:field-a nil :field-b nil :field-c {:x 10}})))
      
      (testing "Invalid - all fields are nil"
        (is (not (m/validate schema {:field-a nil :field-b nil :field-c nil}))))
      
      (testing "Invalid - multiple non-nil fields"
        (is (not (m/validate schema {:field-a "test" :field-b 42})))
        (is (not (m/validate schema {:field-a "test" :field-b 42 :field-c nil}))))
      
      (testing "Invalid - no specified fields"
        (is (not (m/validate schema {})))
        (is (not (m/validate schema {:other-field "value"}))))
      
      (testing "Invalid - wrong value type"
        (is (not (m/validate schema {:field-a 123}))) ; expects string
        (is (not (m/validate schema {:field-b "not-int"}))) ; expects int
        (is (not (m/validate schema {:field-c "not-map"}))))))) ; expects map

(deftest oneof-edn-generator-test
  (testing "Generator produces exactly one field"
    (let [schema [:oneof_edn
                  [:field-a :string]
                  [:field-b :int]
                  [:field-c :boolean]]]
      
      (testing "Generated values have exactly one key"
        (dotimes [_ 20]
          (let [generated (mg/generate schema)]
            (is (= 1 (count generated))
                (str "Generated map should have exactly 1 key, got: " generated))
            (is (contains? #{:field-a :field-b :field-c} (first (keys generated)))
                (str "Generated key should be one of the specified fields, got: " (first (keys generated))))
            (is (some? (first (vals generated)))
                (str "Generated value should not be nil, got: " generated))))))))

(deftest oneof-edn-complex-schema-test
  (testing "oneof-edn with complex nested schemas"
    (let [schema [:oneof_edn
                  [:command [:map {:closed true}
                            [:type [:enum :ping :pong]]
                            [:id :int]]]
                  [:query [:map {:closed true}
                          [:sql :string]
                          [:params [:vector :any]]]]
                  [:event [:map {:closed true}
                          [:name :keyword]
                          [:data :any]]]]]
      
      (testing "Valid complex values"
        (is (m/validate schema {:command {:type :ping :id 1}}))
        (is (m/validate schema {:query {:sql "SELECT * FROM users" :params []}}))
        (is (m/validate schema {:event {:name :user-login :data {:user-id 123}}})))
      
      (testing "Valid with nil in other branches (Pronto style)"
        (is (m/validate schema {:command {:type :ping :id 1} :query nil :event nil}))
        (is (m/validate schema {:command nil :query {:sql "SELECT 1" :params []} :event nil})))
      
      (testing "Invalid complex values"
        (is (not (m/validate schema {:command {:type :invalid}}))) ; missing :id
        (is (not (m/validate schema {:query {:sql 123}}))) ; wrong type for sql
        (is (not (m/validate schema {:event {:name "not-keyword"}}))) ; wrong type for name
        (is (not (m/validate schema {:command {:type :ping :id 1} 
                                     :query {:sql "SELECT 1" :params []}}))) ; two non-nil fields
        ))))

(deftest oneof-edn-error-messages-test
  (testing "Validation correctly identifies errors"
    (let [schema [:oneof_edn
                  [:option-a :string]
                  [:option-b :int]]]
      
      (testing "Correctly rejects invalid values"
        (is (not (m/validate schema {}))
            "Empty map should be invalid")
        (is (not (m/validate schema {:option-a nil :option-b nil}))
            "Map with all nil values should be invalid")
        (is (not (m/validate schema {:option-a "test" :option-b 42}))
            "Map with multiple non-nil values should be invalid")
        (is (not (m/validate schema "not-a-map"))
            "Non-map value should be invalid")
        (is (not (m/validate schema nil))
            "Nil should be invalid")
        (is (not (m/validate schema [])) 
            "Vector should be invalid"))
      
      (testing "Correctly accepts valid values"
        (is (m/validate schema {:option-a "test"}))
        (is (m/validate schema {:option-b 42}))
        (is (m/validate schema {:option-a "test" :option-b nil}))
        (is (m/validate schema {:option-a nil :option-b 42}))))))

(deftest oneof-edn-closed-behavior-test
  (testing "oneof-edn acts as a closed map - rejects extra keys"
    (let [schema [:oneof_edn
                  [:field-a :string]
                  [:field-b :int]
                  [:field-c :boolean]]]
      
      (testing "Rejects extra keys even with nil values"
        (is (not (m/validate schema {:field-a "test" :extra-key nil}))
            "Should reject extra key with nil value")
        (is (not (m/validate schema {:field-b 42 :unknown nil}))
            "Should reject unknown key with nil value")
        (is (not (m/validate schema {:field-c true :field-d nil}))
            "Should reject field-d which is not in schema"))
      
      (testing "Rejects extra keys with non-nil values"
        (is (not (m/validate schema {:field-a "test" :extra-key "value"}))
            "Should reject extra key with value")
        (is (not (m/validate schema {:field-b 42 :unknown 123}))
            "Should reject unknown key with value"))
      
      (testing "Rejects multiple extra keys"
        (is (not (m/validate schema {:field-a "test" :extra1 nil :extra2 nil}))
            "Should reject multiple extra keys with nil")
        (is (not (m/validate schema {:field-b 42 :x 1 :y 2 :z 3}))
            "Should reject multiple extra keys with values"))
      
      (testing "Validation correctly rejects extra keys"
        (let [value {:field-a "test" :extra-key nil}
              valid? (m/validate schema value)]
          (is (not valid?) "Should reject map with extra key even if nil"))
        
        (let [value {:field-b 42 :unknown "value" :another "key"}
              valid? (m/validate schema value)]
          (is (not valid?) "Should reject map with multiple extra keys")))
      
      (testing "Still accepts valid data without extra keys"
        (is (m/validate schema {:field-a "test"}))
        (is (m/validate schema {:field-b 42}))
        (is (m/validate schema {:field-c false}))
        (is (m/validate schema {:field-a "test" :field-b nil :field-c nil}))))))

(deftest oneof-edn-closed-complex-test
  (testing "Closed behavior with complex nested schemas"
    (let [schema [:oneof_edn
                  [:command [:map {:closed true}
                            [:type [:enum :ping :pong]]
                            [:id :int]]]
                  [:query [:map {:closed true}
                          [:sql :string]
                          [:params [:vector :any]]]]]
      
      (testing "Rejects extra keys at oneof level"
        (is (not (m/validate schema {:command {:type :ping :id 1} :extra nil}))
            "Should reject extra key at oneof level")
        (is (not (m/validate schema {:query {:sql "SELECT 1" :params []} :unknown-field nil}))
            "Should reject unknown field at oneof level"))
      
      (testing "Nested maps are still closed"
        (is (not (m/validate schema {:command {:type :ping :id 1 :extra "field"}}))
            "Nested map should still enforce closed constraint")
        (is (not (m/validate schema {:query {:sql "SELECT 1" :params [] :limit 10}}))
            "Query map should reject extra :limit field"))
      
      (testing "Accepts valid data"
        (is (m/validate schema {:command {:type :ping :id 1}}))
        (is (m/validate schema {:query {:sql "SELECT 1" :params []}}))
        (is (m/validate schema {:command {:type :ping :id 1} :query nil})))))

(deftest oneof-edn-pronto-compatibility-test
  (testing "Compatible with Pronto EDN where all fields exist but inactive ones are nil"
    (let [schema [:oneof_edn
                  [:ping [:map {:closed true} [:id :int]]]
                  [:rotary [:map {:closed true} [:angle :double]]]
                  [:cv [:map {:closed true} [:command :string]]]]
          ;; Simulating Pronto EDN with all fields present
          pronto-style-ping {:ping {:id 123} :rotary nil :cv nil}
          pronto-style-rotary {:ping nil :rotary {:angle 45.5} :cv nil}
          pronto-style-cv {:ping nil :rotary nil :cv {:command "capture"}}]
      
      (testing "All Pronto-style maps validate correctly"
        (is (m/validate schema pronto-style-ping))
        (is (m/validate schema pronto-style-rotary))
        (is (m/validate schema pronto-style-cv)))
      
      (testing "Regular maps (without nil fields) also work"
        (is (m/validate schema {:ping {:id 123}}))
        (is (m/validate schema {:rotary {:angle 45.5}}))
        (is (m/validate schema {:cv {:command "capture"}})))
      
      (testing "Mixed style works (some nil fields present)"
        (is (m/validate schema {:ping {:id 123} :rotary nil}))
        (is (m/validate schema {:rotary {:angle 45.5} :cv nil}))
        (is (m/validate schema {:cv {:command "capture"} :ping nil})))
      
      (testing "Rejects extra fields even in Pronto style"
        (is (not (m/validate schema {:ping {:id 123} :rotary nil :cv nil :extra nil}))
            "Should reject extra field even with nil")
        (is (not (m/validate schema {:ping {:id 123} :unknown-cmd nil}))
            "Should reject unknown command field")))))

(deftest oneof-edn-integration-with-maps-test
  (testing "Integration with regular map schemas using :merge"
    (let [base-spec [:map {:closed true}
                     [:protocol_version :int]
                     [:client_type :keyword]]
          oneof-spec [:oneof_edn
                      [:ping [:map [:id :int]]]
                      [:cmd [:map [:action :string]]]]
          ;; Test that oneof can be used alongside regular map validation
          combined-validator (fn [value]
                              (and (m/validate base-spec value)
                                   (m/validate oneof-spec value)))]
      
      (testing "Combined validation works"
        (is (combined-validator {:protocol_version 1
                                 :client_type :local
                                 :ping {:id 123}}))
        (is (combined-validator {:protocol_version 2
                                 :client_type :remote
                                 :cmd {:action "execute"}})))
      
      (testing "Fails when base validation fails"
        (is (not (combined-validator {:ping {:id 123}}))) ; missing required fields
        (is (not (combined-validator {:protocol_version "not-int"
                                      :client_type :local
                                      :ping {:id 123}}))))
      
      (testing "Fails when oneof validation fails"
        (is (not (combined-validator {:protocol_version 1
                                      :client_type :local}))) ; no command
        (is (not (combined-validator {:protocol_version 1
                                      :client_type :local
                                      :ping {:id 123}
                                      :cmd {:action "test"}}))))))) ; multiple commands

(deftest oneof-edn-generator-coverage-test
  (testing "Generator covers all alternatives"
    (let [schema [:oneof_edn
                  [:a :string]
                  [:b :int]
                  [:c :boolean]]
          samples (repeatedly 100 #(mg/generate schema))
          keys-generated (set (mapcat keys samples))]
      
      (testing "All alternatives are generated"
        (is (contains? keys-generated :a) "Should generate :a alternative")
        (is (contains? keys-generated :b) "Should generate :b alternative")
        (is (contains? keys-generated :c) "Should generate :c alternative"))
      
      (testing "Each generated value is valid"
        (doseq [sample samples]
          (is (m/validate schema sample)
              (str "Generated value should be valid: " sample)))))))