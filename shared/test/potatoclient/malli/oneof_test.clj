(ns potatoclient.malli.oneof-test
  "Tests for the oneof Malli schema"
  (:require
    [clojure.test :refer [deftest testing is are use-fixtures]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [malli.core :as m]
    [malli.error :as me]
    [malli.generator :as mg]
    [malli.transform :as mt]
    [potatoclient.malli.oneof :as oneof]
    [potatoclient.malli.registry :as registry]))

;; Set up global registry before all tests
(use-fixtures :once
  (fn [f]
    (registry/setup-global-registry!)
    (f)))

(deftest oneof-schema-registration
  (testing "Schema can be registered and used"
    (let [test-registry (merge (m/default-schemas)
                               (oneof/register-oneof-schema! {}))]
      (is (contains? test-registry :oneof))
      (is (m/schema [:oneof [:a :int] [:b :string]] {:registry test-registry})))

    (testing "Schema is automatically available via global registry"
      ;; Global registry was already set up by fixture
      ;; No explicit registry needed
      (is (m/validate [:oneof [:a :int] [:b :string]] {:a 1})))))

(deftest oneof-validation
  (testing "Basic validation"
    (let [schema [:oneof
                  [:ping [:map [:data :string]]]
                  [:pong [:map [:timestamp :int]]]
                  [:status :boolean]]]

      (testing "Valid cases - exactly one non-nil field"
        (are [value] (m/validate schema value)
          {:ping {:data "hello"}}
          {:ping {:data "hello"} :pong nil}
          {:ping {:data "hello"} :pong nil :status nil}
          {:pong {:timestamp 12345}}
          {:status true}
          {:status false}))

      (testing "Invalid cases - no non-nil fields"
        (are [value] (not (m/validate schema value))
          {}
          {:ping nil}
          {:ping nil :pong nil}
          {:ping nil :pong nil :status nil}))

      (testing "Invalid cases - multiple non-nil fields"
        (are [value] (not (m/validate schema value))
          {:ping {:data "hello"} :pong {:timestamp 123}}
          {:ping {:data "hello"} :status true}
          {:pong {:timestamp 123} :status false}
          {:ping {:data "hello"} :pong {:timestamp 123} :status true}))

      (testing "Invalid cases - extra keys"
        (are [value] (not (m/validate schema value))
          {:ping {:data "hello"} :extra "key"}
          {:unknown "field"}
          {:ping {:data "hello"} :unknown "field"}))

      (testing "Invalid cases - wrong value type"
        (are [value] (not (m/validate schema value))
          {:ping "not a map"}
          {:status "not a boolean"}
          {:ping {:data 123}}  ; wrong field type
          {:pong {:timestamp "not an int"}}))

      (testing "Invalid cases - not a map"
        (are [value] (not (m/validate schema value))
          nil
          []
          "string"
          123)))))

(deftest oneof-with-complex-schemas
  (testing "Works with complex nested schemas"
    (let [schema [:oneof
                  [:create [:map
                            [:id :uuid]
                            [:name :string]
                            [:tags [:vector :keyword]]]]
                  [:update [:map
                            [:id :uuid]
                            [:changes [:map-of :keyword :any]]]]
                  [:delete [:map [:id :uuid]]]]]

      (testing "Valid complex cases"
        (are [value] (m/validate schema value)
          {:create {:id #uuid "550e8400-e29b-41d4-a716-446655440000"
                    :name "test"
                    :tags [:tag1 :tag2]}}
          {:update {:id #uuid "550e8400-e29b-41d4-a716-446655440000"
                    :changes {:name "new-name" :status :active}}}
          {:delete {:id #uuid "550e8400-e29b-41d4-a716-446655440000"}}))

      (testing "Invalid complex cases"
        (are [value] (not (m/validate schema value))
          {:create {:id "not-a-uuid" :name "test" :tags []}}
          {:update {:id #uuid "550e8400-e29b-41d4-a716-446655440000"}}  ; missing changes
          {:delete {}}  ; missing id
          {:create {:id #uuid "550e8400-e29b-41d4-a716-446655440000"
                    :name "test"
                    :tags ["not" "keywords"]}})))))

(deftest oneof-explainer
  (testing "Provides helpful error messages"
    (let [schema [:oneof [:a :int] [:b :string]]
          explain (fn [value] (m/explain schema value))]

      (testing "Error for non-map"
        (let [errors (:errors (explain "not-a-map"))]
          (is (= 1 (count errors)))
          (is (match? {:message "should be a map"} (first errors)))))

      (testing "Error for extra keys"
        (let [errors (:errors (explain {:a 1 :extra "key"}))]
          (is (= 1 (count errors)))
          (is (match? {:message #"unexpected keys"} (first errors)))))

      (testing "Error for no non-nil fields"
        (let [errors (:errors (explain {:a nil :b nil}))]
          (is (= 1 (count errors)))
          (is (match? {:message #"exactly one non-nil field"} (first errors)))))

      (testing "Error for multiple non-nil fields"
        (let [errors (:errors (explain {:a 1 :b "two"}))]
          (is (= 1 (count errors)))
          (is (match? {:message #"multiple non-nil fields"} (first errors))))))))

(deftest oneof-generator
  (testing "Can generate valid values"
    (let [schema [:oneof
                  [:x :int]
                  [:y :string]
                  [:z :boolean]]
          generator (mg/generator schema)]

      (testing "Generated values are valid"
        (doseq [value (mg/sample generator 20)]
          (is (m/validate schema value)
              (str "Generated value should be valid: " value))

          (testing "Has exactly one non-nil field"
            (let [non-nil-count (->> [:x :y :z]
                                     (map #(get value %))
                                     (filter some?)
                                     count)]
              (is (= 1 non-nil-count)
                  (str "Should have exactly 1 non-nil field: " value)))))))))

(deftest oneof-parsing
  (testing "Parser correctly transforms values"
    (let [schema [:oneof
                  [:int :int]
                  [:bool :boolean]]
          parser (m/parser schema)]

      (testing "Parses valid values without transformation"
        (is (= {:int 42} (parser {:int 42})))
        (is (= {:bool true} (parser {:bool true}))))

      (testing "Returns nil when invalid"
        (is (nil? (parser {:int 42 :bool true})))  ; multiple non-nil
        (is (nil? (parser {})))  ; no non-nil
        (is (nil? (parser {:unknown "key"})))))))  ; extra key

(deftest oneof-encoding
  (testing "Encoder correctly transforms values"
    (let [schema [:oneof
                  [:timestamp :int]
                  [:name :string]]
          encoder (m/encoder schema mt/transformer)]

      (testing "Encodes valid values without transformation"
        (is (= {:timestamp 12345} (encoder {:timestamp 12345})))
        (is (= {:name "test"} (encoder {:name "test"}))))

      (testing "Returns value unchanged when invalid"
        (is (= {:timestamp 123 :name "both"} (encoder {:timestamp 123 :name "both"})))))))

(deftest oneof-form
  (testing "Preserves form correctly"
    (let [schema [:oneof [:a :int] [:b :string]]
          compiled (m/schema schema)]
      (is (= [:oneof [:a :int] [:b :string]] (m/form compiled))))))

(deftest oneof-compatibility
  (testing "Compatible with nil values in pronto-style maps"
    (let [schema [:oneof
                  [:command [:map [:type :keyword]]]
                  [:query [:map [:sql :string]]]
                  [:event [:map [:name :string]]]]]

      (testing "Validates pronto-style maps with explicit nils"
        (is (m/validate schema
                        {:command {:type :create}
                         :query nil
                         :event nil}))

        (is (m/validate schema
                        {:command nil
                         :query {:sql "SELECT * FROM users"}
                         :event nil})))

      (testing "Validates maps with missing keys (implicit nil)"
        (is (m/validate schema
                        {:command {:type :create}}))

        (is (m/validate schema
                        {:query {:sql "SELECT * FROM users"}}))))))

(deftest oneof-error-humanization
  (testing "Error humanization should work correctly with oneof schemas"
    (let [schema [:oneof
                  [:protocol_version {:base true} [:int {:min 1}]]
                  [:session_id {:base true} [:int {:min 1}]]
                  [:client_type {:base true} [:enum :LOCAL :REMOTE :CLOUD]]
                  [:ping [:map {:closed true}]]
                  [:noop [:map {:closed true}]]
                  [:system [:map [:restart :boolean]]]]]

      (testing "Humanize errors for invalid base field values"
        (let [invalid-data {:protocol_version 0  ; Invalid: must be > 0
                            :session_id 123
                            :client_type :LOCAL
                            :ping {}}
              explanation (m/explain schema invalid-data)]
          (is explanation "Should produce an explanation")
          ;; This was failing with :malli.core/invalid-schema
          (is (map? (me/humanize explanation))
              "Should be able to humanize the explanation")
          (is (match? {:protocol_version any?} (me/humanize explanation)))))

      (testing "Humanize errors for missing oneof field"
        (let [invalid-data {:protocol_version 1
                            :session_id 123
                            :client_type :LOCAL}
              explanation (m/explain schema invalid-data)]
          (is explanation "Should produce an explanation")
          (is (me/humanize explanation)
              "Should be able to humanize the explanation")))

      (testing "Humanize errors for multiple oneof fields"
        (let [invalid-data {:protocol_version 1
                            :session_id 123
                            :client_type :LOCAL
                            :ping {}
                            :noop {}}  ; Two oneof fields - invalid
              explanation (m/explain schema invalid-data)]
          (is explanation "Should produce an explanation")
          (is (me/humanize explanation)
              "Should be able to humanize the explanation")))

      (testing "Humanize errors for nested validation failures"
        (let [schema [:oneof
                      [:cmd [:map [:type [:enum :create :update :delete]]]]
                      [:query [:map [:sql :string] [:limit [:int {:min 1}]]]]]
              invalid-data {:query {:sql "SELECT *" :limit 0}}  ; limit too small
              explanation (m/explain schema invalid-data)]
          (is explanation "Should produce an explanation")
          (let [humanized (me/humanize explanation)]
            (is humanized "Should be able to humanize the explanation")
            (is (or (get-in humanized [:query :limit])
                    (get humanized :query))
                "Should have error for nested field")))))))