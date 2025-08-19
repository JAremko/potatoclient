(ns potatoclient.oneof-merge-test
  "Test merging regular map schemas with oneof schemas"
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [malli.core :as m]
   [malli.util :as mu]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.malli.oneof :as oneof]
   [clojure.test.check.generators :as gen]))

;; Setup registry with oneof support
(registry/setup-global-registry!)

(deftest test-oneof-merge
  (testing "Merging a regular map with a oneof schema"
    
    (testing "Simple merge of map with oneof"
      (let [;; Regular fields
            base-schema [:map {:closed true}
                         [:protocol_version [:int {:min 1}]]
                         [:session_id [:int {:min 1}]]
                         [:important :boolean]
                         [:from_cv_subsystem :boolean]
                         [:client_type :keyword]]
            
            ;; Oneof fields
            payload-schema [:oneof
                            [:ping [:map [:message :string]]]
                            [:noop [:map]]
                            [:system [:map [:command :keyword]]]]
            
            ;; Try merging them
            merged (mu/merge base-schema payload-schema)]
        
        (println "\n=== Simple merge test ===")
        (println "Base schema:" (m/form base-schema))
        (println "Payload schema:" (m/form payload-schema))
        (println "Merged result:" (m/form merged))
        
        ;; Generate a sample
        (let [sample (mg/generate merged)]
          (println "Generated sample:" sample)
          (is (m/validate merged sample)))))
    
    (testing "Merge with map containing oneof as field"
      (let [;; Map with oneof as a field
            schema-with-oneof [:map {:closed true}
                               [:protocol_version [:int {:min 1}]]
                               [:session_id [:int {:min 1}]]
                               [:payload [:oneof
                                          [:ping [:map]]
                                          [:noop [:map]]]]]
            
            sample {:protocol_version 1
                    :session_id 1
                    :payload {:ping {} :noop nil}}]
        
        (println "\n=== Map with oneof field ===")
        (println "Schema:" (m/form schema-with-oneof))
        (println "Sample:" sample)
        (println "Valid?" (m/validate schema-with-oneof sample))
        (is (m/validate schema-with-oneof sample))))
    
    (testing "Flattened oneof approach (what we need for Pronto)"
      (let [;; Try to create a schema where oneof fields are at root level
            base-fields [:map {:closed false}  ; Note: not closed to allow additional fields
                         [:protocol_version [:int {:min 1}]]
                         [:session_id [:int {:min 1}]]
                         [:important :boolean]
                         [:from_cv_subsystem :boolean]
                         [:client_type :keyword]]
            
            ;; Create individual optional fields
            oneof-fields [:map {:closed false}
                          [:ping {:optional true} [:map]]
                          [:noop {:optional true} [:map]]
                          [:system {:optional true} [:map [:command :keyword]]]]
            
            ;; Merge them
            merged (mu/merge base-fields oneof-fields)
            
            ;; Add constraint that exactly one oneof field must be present
            constrained [:and
                         merged
                         [:fn {:error/message "Exactly one payload field must be present"}
                          (fn [m]
                            (let [oneof-keys [:ping :noop :system]
                                  present (filter #(contains? m %) oneof-keys)]
                              (= 1 (count present))))]]
            
            ;; Test samples
            valid-sample {:protocol_version 1
                          :session_id 1
                          :important false
                          :from_cv_subsystem false
                          :client_type :LOCAL_NETWORK
                          :ping {}}
            
            invalid-sample-none {:protocol_version 1
                                  :session_id 1
                                  :important false
                                  :from_cv_subsystem false
                                  :client_type :LOCAL_NETWORK}
            
            invalid-sample-multiple {:protocol_version 1
                                      :session_id 1
                                      :important false
                                      :from_cv_subsystem false
                                      :client_type :LOCAL_NETWORK
                                      :ping {}
                                      :noop {}}]
        
        (println "\n=== Flattened oneof approach ===")
        (println "Merged schema:" (m/form merged))
        (println "Constrained schema:" (m/form constrained))
        (println "\nValid sample:" valid-sample)
        (println "Validates?" (m/validate constrained valid-sample))
        (is (m/validate constrained valid-sample))
        ;; Also test with matcher-combinators
        (is (match? {:protocol_version pos-int?
                     :session_id pos-int?
                     :important boolean?
                     :from_cv_subsystem boolean?
                     :client_type keyword?
                     :ping map?}
                    valid-sample))
        
        (println "\nInvalid sample (no oneof):" invalid-sample-none)
        (println "Validates?" (m/validate constrained invalid-sample-none))
        (is (not (m/validate constrained invalid-sample-none)))
        
        (println "\nInvalid sample (multiple):" invalid-sample-multiple)
        (println "Validates?" (m/validate constrained invalid-sample-multiple))
        (is (not (m/validate constrained invalid-sample-multiple)))))
    
    (testing "Custom oneof that generates flattened structure"
      ;; What if we modify the oneof generator to produce a flattened structure?
      (let [base-schema [:map {:closed true}
                         [:protocol_version [:int {:min 1}]]
                         [:session_id [:int {:min 1}]]
                         [:important :boolean]
                         [:from_cv_subsystem :boolean]
                         [:client_type :keyword]
                         ;; Add oneof fields as optional
                         [:ping {:optional true} [:map]]
                         [:noop {:optional true} [:map]]
                         [:system {:optional true} [:map [:command :keyword]]]]
            
            ;; Custom generator that ensures exactly one oneof field
            custom-gen (gen/bind
                        (gen/elements [:ping :noop :system])
                        (fn [active-field]
                          (gen/fmap
                           (fn [[base-vals field-val]]
                             (assoc base-vals active-field field-val))
                           (gen/tuple
                            ;; Generate base fields
                            (mg/generator [:map
                                           [:protocol_version [:int {:min 1}]]
                                           [:session_id [:int {:min 1}]]
                                           [:important :boolean]
                                           [:from_cv_subsystem :boolean]
                                           [:client_type :keyword]])
                            ;; Generate value for active field
                            (case active-field
                              :ping (mg/generator [:map])
                              :noop (mg/generator [:map])
                              :system (mg/generator [:map [:command :keyword]]))))))]
        
        (println "\n=== Custom generator approach ===")
        (println "Schema:" (m/form base-schema))
        (let [samples (gen/sample custom-gen 3)]
          (doseq [sample samples]
            (println "Generated:" sample)
            (println "Valid?" (m/validate base-schema sample))
            (is (m/validate base-schema sample))
            ;; Test with matcher-combinators - exactly one of the oneof fields should be present
            (is (match? (matchers/all-of
                         {:protocol_version pos-int?
                          :session_id pos-int?
                          :important boolean?
                          :from_cv_subsystem boolean?
                          :client_type keyword?}
                         #(= 1 (count (filter (partial contains? %) [:ping :noop :system]))))
                        sample))))))
    ))