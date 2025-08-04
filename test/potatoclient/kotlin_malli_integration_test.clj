(ns potatoclient.kotlin-malli-integration-test
  "Integration test that validates Malli-generated commands through actual Kotlin/Protobuf/buf.validate.
  
  This is the CRUCIAL test requested by the user to verify:
  1. Clojure sends Malli-generated payloads
  2. Kotlin successfully validates them
  3. Serializes to protobuf 
  4. Deserializes correctly
  5. Java representation matches (via equals/hash)"
  (:require [clojure.test :refer [deftest testing is]]
            [malli.generator :as mg]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.kotlin.integration-test-utils :as test-utils]
            [clojure.data.json :as json]))

;; Command generators with precise Malli schemas
(def command-generators
  {:rotary-goto
   {:schema [:map
             [:azimuth [:double {:min 0.0 :max 360.0}]]
             [:elevation [:double {:min -30.0 :max 90.0}]]]
    :builder (fn [data] (cmd/rotary-goto data))}

   :cv-start-track
   {:schema [:map
             [:channel [:enum :heat :day]]
             [:x [:and double? [:>= -1.0] [:<= 1.0]]]
             [:y [:and double? [:>= -1.0] [:<= 1.0]]]
             [:frame-timestamp {:optional true} pos-int?]]
    :builder (fn [data] (cmd/cv-start-track-ndc
                          (:channel data)
                          (:x data)
                          (:y data)
                          (:frame-timestamp data)))}

   :heat-palette
   {:schema [:enum :white-hot :black-hot :rainbow :ironbow :lava :arctic]
    :builder (fn [data] (cmd/heat-camera-palette data))}

   :localization
   {:schema [:enum :en :uk]
    :builder (fn [data] (cmd/set-localization data))}})

(deftest test-malli-kotlin-validation-roundtrip
  (testing "Malli-generated commands pass full Kotlin validation"
    (doseq [[cmd-type {:keys [schema builder]}] command-generators]
      (testing (str "Command type: " cmd-type)
        ;; Generate samples
        (let [samples (mg/sample schema {:size 20})]
          (doseq [generated samples]
            (testing (str "Generated: " generated)
              ;; Build command
              (let [command (builder generated)
                    ;; Send to Kotlin for validation
                    result (test-utils/validate-command-with-kotlin command)]

                ;; Core assertions
                (is (:success result)
                    (str "Command should validate.\n"
                         "Generated: " generated "\n"
                         "Command: " command "\n"
                         "Error: " (:error result)))

                ;; When successful, verify additional properties
                (when (:success result)
                  ;; Proto was created
                  (is (:proto-json result) "Should have protobuf JSON")

                  ;; Binary size is reasonable
                  (is (pos? (:binary-size result 0)) "Should have binary data")

                  ;; Java equals works after roundtrip
                  (is (:proto-equals result)
                      "Protobuf should equal itself after binary roundtrip")

                  ;; Hash codes match
                  (is (= (:original-hash result) (:roundtrip-hash result))
                      "Hash codes should match after roundtrip"))))))))))

(deftest test-constraint-edge-cases
  (testing "Edge case values near constraint boundaries"
    ;; Test azimuth boundaries
    (doseq [azimuth [0.0 0.0001 179.9999 359.9999]]
      (let [command (cmd/rotary-goto {:azimuth azimuth :elevation 0.0})
            result (test-utils/validate-command-with-kotlin command)]
        (is (:success result)
            (str "Azimuth " azimuth " should be valid"))))

    ;; Test elevation boundaries  
    (doseq [elevation [-30.0 -29.9999 0.0 89.9999 90.0]]
      (let [command (cmd/rotary-goto {:azimuth 180.0 :elevation elevation})
            result (test-utils/validate-command-with-kotlin command)]
        (is (:success result)
            (str "Elevation " elevation " should be valid"))))

    ;; Test NDC boundaries
    (doseq [coord [-1.0 -0.9999 0.0 0.9999 1.0]]
      (let [command (cmd/cv-start-track-ndc :heat coord coord)
            result (test-utils/validate-command-with-kotlin command)]
        (is (:success result)
            (str "NDC coordinate " coord " should be valid"))))))

(deftest test-invalid-values-rejected
  (testing "Invalid values are properly rejected by buf.validate"
    ;; Test invalid azimuth
    (let [command {:rotary {:goto {:azimuth 400.0 :elevation 45.0}}}
          result (test-utils/validate-command-with-kotlin command)]
      (is (not (:success result)) "Azimuth > 360 should fail")
      (is (re-find #"azimuth" (:error result "")) "Error should mention azimuth"))

    ;; Test invalid elevation
    (let [command {:rotary {:goto {:azimuth 180.0 :elevation -45.0}}}
          result (test-utils/validate-command-with-kotlin command)]
      (is (not (:success result)) "Elevation < -30 should fail")
      (is (re-find #"elevation" (:error result "")) "Error should mention elevation"))))

(deftest test-high-volume-malli-generation
  (testing "High volume test with 1000 generated commands"
    (let [results (atom {:success 0 :failure 0 :errors []})]
      ;; Generate and test many commands
      (doseq [[cmd-type {:keys [schema builder]}] command-generators
              :let [samples (mg/sample schema {:size 250})]]
        (doseq [generated samples]
          (try
            (let [command (builder generated)
                  result (test-utils/validate-command-with-kotlin command)]
              (if (:success result)
                (swap! results update :success inc)
                (do
                  (swap! results update :failure inc)
                  (when (< (count (:errors @results)) 10) ; Keep first 10 errors
                    (swap! results update :errors conj
                           {:type cmd-type
                            :generated generated
                            :error (:error result)})))))
            (catch Exception e
              (swap! results update :failure inc)
              (when (< (count (:errors @results)) 10)
                (swap! results update :errors conj
                       {:type cmd-type
                        :generated generated
                        :error (.getMessage e)}))))))

      ;; Report results
      (let [{:keys [success failure errors]} @results]
        (println "\nHigh volume test results:")
        (println "  Success:" success)
        (println "  Failure:" failure)
        (when (seq errors)
          (println "  Sample errors:")
          (doseq [err errors]
            (println "   " err)))

        ;; Assert all should pass
        (is (zero? failure)
            (str failure " commands failed validation"))))))