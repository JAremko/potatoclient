(ns generator.global-registry-test
  "Test that custom :oneof schema works with global registry"
  (:require [clojure.test :refer :all]
            [potatoclient.specs.malli-oneof :as oneof]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]))

(deftest test-global-registry-setup
  (testing "Global registry can be set with custom :oneof schema"
    ;; Set up global registry
    (mr/set-default-registry!
      (merge (m/default-schemas)
             (mr/schemas m/default-registry)
             {:oneof oneof/-oneof-schema}))
    
    (testing "Can create :oneof schema without explicit registry"
      (let [schema [:oneof {:a :string :b :int}]]
        (is (some? (m/schema schema)))
        (is (= :oneof (m/type (m/schema schema))))))
    
    (testing "Can validate with :oneof schema using default registry"
      (let [schema [:oneof {:ping [:map [:value :string]]
                           :pong [:map [:value :int]]}]]
        ;; Valid cases
        (is (m/validate schema {:ping {:value "hello"}}))
        (is (m/validate schema {:pong {:value 42}}))
        ;; Invalid cases
        (is (not (m/validate schema {})))
        (is (not (m/validate schema {:ping {:value "hello"} :pong {:value 42}})))))
    
    (testing "Can generate with :oneof schema using default registry"
      (let [schema [:oneof {:a :string :b :int :c :boolean}]
            gen (mg/generator schema)]
        (dotimes [_ 10]
          (let [value (mg/generate gen)]
            (is (m/validate schema value))
            (is (= 1 (count value)))))))))

(deftest test-nested-schemas-with-global-registry
  (testing "Nested schemas work with global :oneof in default registry"
    ;; Ensure registry is set
    (mr/set-default-registry!
      (merge (m/default-schemas)
             (mr/schemas m/default-registry)
             {:oneof oneof/-oneof-schema}))
    
    (let [;; Simulated protobuf command structure
          command-schema [:map
                         [:protocol-version [:and :int [:> 0]]]
                         [:client-type :keyword]
                         [:cmd [:oneof {:ping [:map [:ping :map]]
                                       :rotary [:map [:rotary [:map
                                                              [:goto-ndc [:map
                                                                         [:channel :keyword]
                                                                         [:x :double]
                                                                         [:y :double]]]]]]}]]]
          
          valid-command {:protocol-version 1
                        :client-type :local-network
                        :cmd {:ping {:ping {}}}}
          
          valid-rotary {:protocol-version 1
                       :client-type :local-network
                       :cmd {:rotary {:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}}}}]
      
      (testing "Valid commands pass validation"
        (is (m/validate command-schema valid-command))
        (is (m/validate command-schema valid-rotary)))
      
      (testing "Invalid commands fail validation"
        ;; Missing cmd
        (is (not (m/validate command-schema {:protocol-version 1 :client-type :local})))
        ;; Multiple cmd options
        (is (not (m/validate command-schema 
                            {:protocol-version 1
                             :client-type :local
                             :cmd {:ping {:ping {}} :rotary {:rotary {}}}})))
        ;; Invalid protocol version
        (is (not (m/validate command-schema 
                            {:protocol-version 0  ; Must be > 0
                             :client-type :local
                             :cmd {:ping {:ping {}}}})))))))

(deftest test-registry-in-generated-code-simulation
  (testing "Simulating how generated code would use the global registry"
    ;; Set global registry once
    (mr/set-default-registry!
      (merge (m/default-schemas)
             (mr/schemas m/default-registry)
             {:oneof oneof/-oneof-schema}))
    
    ;; Simulate generated spec definitions
    (def ping-spec [:map [:ping :map]])
    (def rotary-spec [:map [:rotary [:map 
                                     [:goto-ndc [:map
                                               [:channel :keyword]
                                               [:x :double]
                                               [:y :double]]]]]])
    
    (def cmd-oneof-spec [:oneof {:ping ping-spec
                                :rotary rotary-spec}])
    
    (def root-spec [:map
                   [:protocol-version [:and :int [:> 0]]]
                   [:client-type :keyword]
                   [:cmd cmd-oneof-spec]])
    
    ;; Simulate a builder function that would validate
    (defn simulated-build-root [data]
      (when-not (m/validate root-spec data)
        (throw (ex-info "Invalid data" {:errors (m/explain root-spec data)})))
      ;; Would actually build Java protobuf here
      {:built true :data data})
    
    (testing "Builder validates with global registry"
      (is (simulated-build-root {:protocol-version 1
                                :client-type :local
                                :cmd {:ping {:ping {}}}}))
      
      (is (thrown? Exception
                  (simulated-build-root {:protocol-version 1
                                       :client-type :local
                                       :cmd {}})))  ; Empty oneof
      
      (is (thrown? Exception
                  (simulated-build-root {:protocol-version 0  ; Invalid constraint
                                       :client-type :local
                                       :cmd {:ping {:ping {}}}}))))))

(deftest test-generator-with-constraints-and-oneof
  (testing "Generator works with constrained oneof values"
    ;; Ensure registry is set
    (mr/set-default-registry!
      (merge (m/default-schemas)
             (mr/schemas m/default-registry)
             {:oneof oneof/-oneof-schema}))
    
    (let [schema [:oneof {:temp [:and :double [:>= -273.15] [:<= 1000]]
                         :pressure [:and :double [:> 0] [:<= 10000]]}]
          gen (mg/generator schema)]
      
      (testing "Generated values respect constraints"
        (dotimes [_ 20]
          (let [value (mg/generate gen)]
            (is (m/validate schema value))
            (cond
              (contains? value :temp)
              (let [temp (:temp value)]
                (is (>= temp -273.15))
                (is (<= temp 1000)))
              
              (contains? value :pressure)
              (let [pressure (:pressure value)]
                (is (> pressure 0))
                (is (<= pressure 10000))))))))))

(run-tests)