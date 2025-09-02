(ns potatoclient.malli.registry-test
  "Tests for the Malli registry management"
  (:require
    [clojure.test :refer [deftest testing is]]
    [matcher-combinators.test] ;; extends clojure.test's `is` macro
    [matcher-combinators.matchers :as matchers]
    [malli.core :as m]
    [malli.registry :as mr]
    [potatoclient.malli.registry :as registry]
    [potatoclient.malli.oneof :as oneof]))

(deftest setup-global-registry
  (testing "Global registry setup"
    (testing "Automatically includes oneof schema"
      (registry/setup-global-registry!)

      ;; Verify oneof is available without explicit registration
      (is (m/validate [:oneof [:a :int] [:b :string]] {:a 1}))
      (is (not (m/validate [:oneof [:a :int] [:b :string]] {:a 1 :b "two"}))))

    (testing "Can add custom schemas to registry"
      (registry/setup-global-registry! {:my/custom [:map [:id :int]]})

      ;; Both oneof and custom schemas should work
      (is (m/validate :my/custom {:id 123}))
      (is (m/validate [:oneof [:x :int]] {:x 42})))))

(deftest register-spec
  (testing "Registering individual specs"
    (registry/setup-global-registry!)

    (testing "Can register a simple spec"
      (registry/register-spec! :test/simple [:map [:name :string]])
      (is (m/validate :test/simple {:name "test"}))
      (is (not (m/validate :test/simple {:name 123}))))

    (testing "Can register a spec with references"
      (registry/register-spec! :test/address
                               [:map
                                [:street :string]
                                [:city :string]
                                [:zip :string]])
      (registry/register-spec! :test/person
                               [:map
                                [:name :string]
                                [:age :int]
                                [:address :test/address]])

      (is (m/validate :test/person
                      {:name "John"
                       :age 30
                       :address {:street "123 Main St"
                                 :city "Boston"
                                 :zip "02101"}}))

      (is (not (m/validate :test/person
                           {:name "John"
                            :age 30
                            :address {:street "123 Main St"}}))))  ; missing fields

    (testing "Can override existing spec"
      (registry/register-spec! :test/simple [:map [:id :int]])
      (is (m/validate :test/simple {:id 1}))
      (is (not (m/validate :test/simple {:name "test"}))))))  ; old schema no longer valid

(deftest get-registry
  (testing "Getting current mutable registry"
    (registry/setup-global-registry!)
    (registry/register-spec! :test/example [:map [:value :any]])

    (let [current-registry (registry/get-registry)]
      (is (map? current-registry))
      ;; The get-registry function only returns the mutable portion
      ;; oneof is in the composite registry but not the mutable part
      (is (match? {:test/example any?} current-registry))

      ;; Verify the spec works via the global registry
      (is (m/validate :test/example {:value "test"})))))

(deftest registry-with-oneof
  (testing "Registry works with oneof schemas"
    (registry/setup-global-registry!)

    (testing "Can register specs using oneof"
      (registry/register-spec! :test/command
                               [:oneof
                                [:create [:map [:name :string]]]
                                [:update [:map [:id :int] [:name :string]]]
                                [:delete [:map [:id :int]]]])

      (is (m/validate :test/command {:create {:name "test"}}))
      (is (m/validate :test/command {:update {:id 1 :name "updated"}}))
      (is (m/validate :test/command {:delete {:id 1}}))
      (is (not (m/validate :test/command {})))
      (is (not (m/validate :test/command {:create {:name "a"} :delete {:id 1}}))))

    (testing "Can use oneof in nested specs"
      (registry/register-spec! :test/request
                               [:map
                                [:id :uuid]
                                [:command :test/command]
                                [:timestamp :int]])

      (is (m/validate :test/request
                      {:id #uuid "550e8400-e29b-41d4-a716-446655440000"
                       :command {:create {:name "test"}}
                       :timestamp 1234567890}))

      ;; Using matcher-combinators for partial matching
      (is (match? {:id uuid?
                   :command {:create {:name string?}}
                   :timestamp int?}
                  {:id #uuid "550e8400-e29b-41d4-a716-446655440000"
                   :command {:create {:name "test"}}
                   :timestamp 1234567890})))))

(deftest registry-composition
  (testing "Registry can be composed from multiple sources"
    (let [base-registry {:base/spec [:map [:x :int]]}
          extended-registry {:extended/spec [:map [:y :string]]}]
      (registry/setup-global-registry!
        (merge base-registry extended-registry))

      ;; oneof is always included automatically
      (is (m/validate :base/spec {:x 1}))
      (is (m/validate :extended/spec {:y "test"}))
      (is (m/validate [:oneof [:a :base/spec] [:b :extended/spec]]
                      {:a {:x 1}})))))

(deftest registry-thread-safety
  (testing "Registry operations are thread-safe"
    (registry/setup-global-registry!)

    ;; Register specs from multiple threads
    (let [futures (for [i (range 10)]
                    (future
                      (registry/register-spec!
                        (keyword "test" (str "spec-" i))
                        [:map [(keyword (str "field-" i)) :int]])))]

      ;; Wait for all to complete
      (doseq [f futures] @f)

      ;; Verify all specs were registered
      (doseq [i (range 10)]
        (let [spec-name (keyword "test" (str "spec-" i))
              field-name (keyword (str "field-" i))]
          (is (m/validate spec-name {field-name i})))))))