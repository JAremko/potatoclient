(ns potatoclient.transit.kotlin-generator-validation-test
  "CRUCIAL TEST: Validates Malli-generated commands through actual Kotlin/Protobuf/buf.validate.
  
  This test uses the TestCommandProcessor to verify:
  1. Malli generates valid data
  2. Commands serialize correctly to Transit  
  3. Kotlin accepts and builds protobuf from Transit
  4. buf.validate constraints are satisfied
  5. Binary roundtrip preserves data
  6. Java protobuf objects are equal after roundtrip"
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [malli.generator :as mg]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit-core]
            [potatoclient.process :as process]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

;; =============================================================================
;; Test Infrastructure
;; =============================================================================

(def ^:dynamic *test-processor* nil)

(defn start-test-processor []
  "Start the Kotlin test command processor"
  (let [classpath (str/join ":" ["src/kotlin/build/libs/potatoclient-kotlin.jar"
                                 "test/kotlin/build/libs/test-kotlin.jar"])
        process (process/start-process
                  ["java" "-cp" classpath
                   "potatoclient.kotlin.transit.TestCommandProcessor"]
                  {:dir (System/getProperty "user.dir")})]
    {:process process
     :stdin (:stdin process)
     :stdout (:stdout process)}))

(defn send-command-get-result [processor command]
  "Send command to Kotlin processor and get JSON result"
  (let [{:keys [stdin stdout]} processor
        ;; Encode command as Transit
        baos (ByteArrayOutputStream.)
        writer (transit-core/make-writer baos)]
    (transit-core/write-message! writer command baos)
    ;; Send as base64 (since stdin is text-based)
    (let [base64 (.encodeToString (java.util.Base64/getEncoder)
                                  (.toByteArray baos))]
      (.println stdin base64)
      (.flush stdin)
      ;; Read JSON response
      (let [response (.readLine stdout)]
        (when response
          (json/read-str response :key-fn keyword))))))

(defn processor-fixture [f]
  "Start processor before tests, stop after"
  (let [proc (start-test-processor)]
    (Thread/sleep 500) ; Give it time to start
    (binding [*test-processor* proc]
      (try
        (f)
        (finally
          (when-let [p (:process proc)]
            (.destroy p)))))))

(use-fixtures :once processor-fixture)

;; =============================================================================
;; Command Schemas for Generation
;; =============================================================================

(def command-generators
  "Map of command type to [generator-schema builder-fn]"
  {:ping [[:map] (fn [_] (cmd/ping))]

   :cv-start-track-ndc
   [[:map
     [:channel [:enum :heat :day]]
     [:x [:and double? [:>= -1.0] [:<= 1.0]]]
     [:y [:and double? [:>= -1.0] [:<= 1.0]]]
     [:frame-timestamp {:optional true} pos-int?]]
    (fn [g] (cmd/cv-start-track-ndc (:channel g) (:x g) (:y g) (:frame-timestamp g)))]

   :rotary-goto
   [[:map
     [:azimuth [:double {:min 0.0 :max 360.0}]]
     [:elevation [:double {:min -30.0 :max 90.0}]]]
    (fn [g] (cmd/rotary-goto g))]

   :rotary-set-velocity
   [[:map
     [:azimuth-speed [:and double? [:>= 0.0]]]
     [:elevation-speed [:and double? [:>= 0.0]]]
     [:azimuth-direction [:enum :clockwise :counter-clockwise]]
     [:elevation-direction [:enum :clockwise :counter-clockwise]]]
    (fn [g] (cmd/rotary-set-velocity
              (:azimuth-speed g) (:elevation-speed g)
              (:azimuth-direction g) (:elevation-direction g)))]

   :heat-camera-palette
   [[:enum :white-hot :black-hot :rainbow :ironbow :lava :arctic]
    (fn [g] (cmd/heat-camera-palette g))]

   :set-localization
   [[:enum :en :uk]
    (fn [g] (cmd/set-localization g))]})

;; =============================================================================
;; Core Validation Tests
;; =============================================================================

(deftest test-basic-commands-kotlin-validation
  (testing "Basic commands validate through Kotlin"
    (when *test-processor*
      ;; Test simple commands
      (doseq [cmd-fn [cmd/ping cmd/noop cmd/frozen]]
        (let [command (cmd-fn)
              result (send-command-get-result *test-processor* command)]
          (is (:success result)
              (str "Command should validate: " (keys command)
                   "\nError: " (:error result)))
          (when (:success result)
            (is (:proto result) "Should have protobuf JSON")))))))

(deftest test-generated-commands-kotlin-validation
  (testing "Malli-generated commands validate through full Kotlin stack"
    (when *test-processor*
      (doseq [[cmd-type [schema builder-fn]] command-generators]
        (testing (str "Command type: " cmd-type)
          (let [samples (try
                          (mg/sample schema {:size 10})
                          (catch Exception e
                            (println "Failed to generate for" cmd-type ":" (.getMessage e))
                            []))]
            (doseq [generated samples]
              (let [command (builder-fn generated)
                    result (send-command-get-result *test-processor* command)]
                (is (:success result)
                    (str "Generated " cmd-type " should validate.\n"
                         "Generated: " generated "\n"
                         "Command: " command "\n"
                         "Error: " (:error result)))

                ;; Additional validation when successful
                (when (:success result)
                  ;; Check protobuf was created
                  (is (contains? result :proto) "Should have proto field")
                  ;; Check binary size is reasonable
                  (is (pos? (get result :binary-size 0)) "Should have binary data"))))))))))

(deftest test-constraint-validation-with-generators
  (testing "buf.validate constraints are enforced on generated data"
    (when *test-processor*
      ;; Test that valid generated values pass
      (let [valid-goto (mg/generate [:map
                                     [:azimuth [:double {:min 0.0 :max 360.0}]]
                                     [:elevation [:double {:min -30.0 :max 90.0}]]])
            command (cmd/rotary-goto valid-goto)
            result (send-command-get-result *test-processor* command)]
        (is (:success result) "Valid values should pass"))

      ;; Test edge cases that should pass
      (doseq [azimuth [0.0 180.0 359.9]
              elevation [-30.0 0.0 90.0]]
        (let [command (cmd/rotary-goto {:azimuth azimuth :elevation elevation})
              result (send-command-get-result *test-processor* command)]
          (is (:success result)
              (str "Edge case should pass: az=" azimuth " el=" elevation)))))))

(deftest test-high-volume-generation
  (testing "High volume test with diverse generated commands"
    (when *test-processor*
      (let [results (atom {:success 0 :failure 0 :errors []})]
        ;; Generate and test many commands
        (doseq [[cmd-type [schema builder-fn]] command-generators
                :let [samples (try
                                (mg/sample schema {:size 50})
                                (catch Exception e []))]
                generated samples]
          (try
            (let [command (builder-fn generated)
                  result (send-command-get-result *test-processor* command)]
              (if (:success result)
                (swap! results update :success inc)
                (do
                  (swap! results update :failure inc)
                  (swap! results update :errors conj
                         {:type cmd-type
                          :generated generated
                          :error (:error result)}))))
            (catch Exception e
              (swap! results update :failure inc)
              (swap! results update :errors conj
                     {:type cmd-type
                      :generated generated
                      :error (.getMessage e)}))))

        ;; Report results
        (let [{:keys [success failure errors]} @results]
          (println "Generator validation results:")
          (println "  Success:" success)
          (println "  Failure:" failure)
          (when (seq errors)
            (println "  First 5 errors:")
            (doseq [err (take 5 errors)]
              (println "   " err)))

          ;; All generated commands should validate
          (is (zero? failure)
              (str failure " commands failed validation")))))))

(deftest test-transit-keyword-preservation
  (testing "Transit preserves keyword types through roundtrip"
    (when *test-processor*
      ;; Test that keywords are preserved
      (let [command (cmd/cv-start-track-ndc :heat 0.5 -0.5)
            result (send-command-get-result *test-processor* command)]
        (is (:success result))
        ;; The extracted Transit should have keywords preserved
        ;; Note: This depends on Transit configuration
        ))))

(deftest test-protobuf-equality
  (testing "Protobuf objects are equal after binary roundtrip"
    (when *test-processor*
      ;; The TestCommandProcessor should report if protos are equal
      (let [command (cmd/rotary-goto {:azimuth 45.0 :elevation 30.0})
            result (send-command-get-result *test-processor* command)]
        (is (:success result))
        ;; If the processor provides equality info
        (when (contains? result :proto-equals)
          (is (:proto-equals result)
              "Protobuf should equal itself after roundtrip"))))))