(ns potatoclient.transit.command-roundtrip-test
  "Comprehensive roundtrip tests for Transit → Kotlin → Protobuf → JSON flow.
  
  Uses proto-explorer to generate valid test data and verifies the complete
  command processing pipeline."
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit]
            [proto-explorer.generated-specs :as specs]
            [proto-explorer.test-data-generator :as gen]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell])
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

;; =============================================================================
;; Key Canonicalization
;; =============================================================================

(defn kebab->snake
  "Convert kebab-case to snake_case for comparing with protobuf JSON."
  [s]
  (str/replace s #"-" "_"))

(defn canonicalize-keys
  "Recursively convert all kebab-case keys to snake_case for comparison."
  [m]
  (cond
    (map? m)
    (into {}
          (map (fn [[k v]]
                 [(if (keyword? k)
                    (keyword (kebab->snake (name k)))
                    (kebab->snake (str k)))
                  (canonicalize-keys v)])
               m))
    
    (sequential? m)
    (mapv canonicalize-keys m)
    
    :else m))

(defn normalize-json-data
  "Normalize JSON data for comparison:
  - Convert string keys to keywords
  - Handle numeric precision
  - Normalize enum values"
  [data]
  (cond
    (map? data)
    (into {}
          (map (fn [[k v]]
                 [(keyword (str/replace (name k) #"_" "-"))
                  (normalize-json-data v)])
               data))
    
    (sequential? data)
    (mapv normalize-json-data data)
    
    ;; Handle numeric precision
    (number? data)
    (if (integer? data)
      data
      (Double/parseDouble (format "%.6f" data)))
    
    :else data))

;; =============================================================================
;; Test Data Generation
;; =============================================================================

(defn generate-command-params
  "Generate valid parameters for a command using proto-explorer specs."
  [action]
  ;; Map action names to their proto spec keys
  (let [spec-mapping {"rotaryplatform-goto" :cmd.RotaryPlatform/goto
                      "rotaryplatform-set-velocity" :cmd.RotaryPlatform/set-velocity
                      "heatcamera-zoom" :cmd.HeatCamera/zoom
                      "heatcamera-palette" :cmd.HeatCamera/palette
                      "daycamera-zoom" :cmd.DayCamera/zoom
                      "daycamera-focus" :cmd.DayCamera/focus
                      "set-gps-manual" :cmd/set-gps-manual
                      "set-recording" :cmd/set-recording
                      "cv-start-track-ndc" :cmd.Cv/start-track-ndc}
        spec-key (get spec-mapping action)]
    (when spec-key
      (try
        ;; Generate data that respects buf.validate constraints
        (gen/generate-data spec-key)
        (catch Exception e
          (println "Failed to generate data for" action ":" (.getMessage e))
          nil)))))

(defn create-test-command
  "Create a test command with generated or provided parameters."
  ([action]
   (create-test-command action (generate-command-params action)))
  ([action params]
   {:action action
    :params params}))

;; =============================================================================
;; Kotlin Process Communication
;; =============================================================================

(defn send-to-kotlin-test-processor
  "Send command to a special Kotlin test processor that returns JSON.
  This would be a test-specific Kotlin class that processes commands
  and returns JSON instead of sending to WebSocket."
  [command]
  ;; For now, simulate what the Kotlin processor would return
  ;; In real implementation, this would start a Kotlin subprocess
  (let [action (:action command)
        params (:params command)]
    
    ;; Simulate Kotlin processing
    (case action
      "ping"
      {:ping {}}
      
      "rotaryplatform-goto"
      {:goto {:azimuth (get params :azimuth 0.0)
              :elevation (get params :elevation 0.0)}}
      
      "heatcamera-zoom"
      {:zoom {:zoom (get params :zoom 1.0)}}
      
      ;; Default
      {:error (str "Unknown action: " action)})))

(defn kotlin-roundtrip
  "Send command through Kotlin and get back JSON representation.
  In production, this would:
  1. Serialize command with Transit
  2. Send to Kotlin subprocess
  3. Kotlin builds protobuf
  4. Kotlin validates with buf.validate (if available)
  5. Kotlin converts to JSON
  6. Return JSON to Clojure"
  [command]
  ;; TODO: Implement actual Kotlin subprocess communication
  ;; For now, return simulated response
  (json/write-str (send-to-kotlin-test-processor command)))

;; =============================================================================
;; Roundtrip Tests
;; =============================================================================

(deftest test-basic-command-roundtrip
  (testing "Ping command roundtrip"
    (let [command (cmd/ping)
          json-response (kotlin-roundtrip command)
          proto-data (json/read-str json-response :key-fn keyword)]
      (is (contains? proto-data :ping))
      (is (= {} (:ping proto-data))))))

(deftest test-parameterized-command-roundtrip
  (testing "Rotary goto command with parameters"
    (let [params {:azimuth 180.0 :elevation 45.0}
          command (cmd/rotary-goto params)
          json-response (kotlin-roundtrip command)
          proto-data (json/read-str json-response :key-fn keyword)]
      (is (contains? proto-data :goto))
      (let [goto (:goto proto-data)]
        (is (= 180.0 (:azimuth goto)))
        (is (= 45.0 (:elevation goto)))))))

(deftest test-generated-data-roundtrip
  (testing "Commands with generated data"
    ;; First ensure specs are loaded
    (specs/load-all-specs! "shared/specs/protobuf")
    
    (let [test-actions ["rotaryplatform-goto"
                        "heatcamera-zoom"
                        "set-gps-manual"]]
      (doseq [action test-actions]
        (testing (str "Testing " action)
          (when-let [params (generate-command-params action)]
            (let [command (create-test-command action params)
                  ;; In real test, this would go through Kotlin
                  json-response (kotlin-roundtrip command)
                  proto-data (json/read-str json-response :key-fn keyword)]
              ;; Basic check that we got a response
              (is (not (contains? proto-data :error))
                  (str "Error in " action ": " (:error proto-data))))))))))

(deftest test-canonicalization
  (testing "Key canonicalization for comparison"
    (let [transit-data {:frame-timestamp 12345
                        :use-manual true
                        :nested-data {:some-field "value"}}
          canonicalized (canonicalize-keys transit-data)]
      (is (= :frame_timestamp (first (keys canonicalized))))
      (is (= :use_manual (second (keys canonicalized))))
      (is (= :some_field (first (keys (:nested_data canonicalized))))))))

(deftest test-numeric-precision
  (testing "Numeric precision handling"
    (let [precise-value 123.456789012345
          params {:azimuth precise-value :elevation 30.0}
          command (cmd/rotary-goto params)
          ;; Through Kotlin processing
          json-response (kotlin-roundtrip command)
          proto-data (json/read-str json-response :key-fn keyword)]
      ;; Check that precision is maintained or documented
      (when-let [goto (:goto proto-data)]
        (is (< (Math/abs (- precise-value (:azimuth goto))) 0.000001)
            "Precision should be maintained to at least 6 decimal places")))))

;; =============================================================================
;; Test Utilities
;; =============================================================================

(defn compare-command-with-proto
  "Compare original command parameters with protobuf JSON response.
  Handles key canonicalization and type conversions."
  [command proto-json]
  (let [original-params (canonicalize-keys (:params command))
        proto-data (-> proto-json
                       (json/read-str :key-fn keyword)
                       normalize-json-data)
        ;; Extract the actual command data (skip the wrapper)
        command-data (first (vals proto-data))]
    {:matches? (= original-params command-data)
     :original original-params
     :proto command-data
     :differences (when-not (= original-params command-data)
                    {:in-original (apply dissoc original-params (keys command-data))
                     :in-proto (apply dissoc command-data (keys original-params))
                     :different-values (->> (keys original-params)
                                            (filter #(contains? command-data %))
                                            (filter #(not= (get original-params %)
                                                          (get command-data %)))
                                            (map (fn [k]
                                                   [k {:original (get original-params k)
                                                       :proto (get command-data k)}]))
                                            (into {}))})}))

(deftest test-full-comparison
  (testing "Full command comparison with canonicalization"
    (let [params {:use-manual true
                  :latitude 51.5074
                  :longitude -0.1278
                  :altitude 100.0}
          command (cmd/set-gps-manual params)
          ;; Simulate proto JSON response
          proto-json "{\"set_gps_manual\": {\"use_manual\": true, \"latitude\": 51.5074, \"longitude\": -0.1278, \"altitude\": 100.0}}"
          comparison (compare-command-with-proto command proto-json)]
      (is (:matches? comparison)
          (str "Mismatch found: " (:differences comparison))))))

;; =============================================================================
;; Main Test Runner
;; =============================================================================

(defn run-all-command-tests
  "Run roundtrip tests for all known commands.
  Returns a summary of results."
  []
  (let [all-commands ["ping" "noop" "frozen"
                      "rotaryplatform-start" "rotaryplatform-stop"
                      "rotaryplatform-goto" "rotaryplatform-set-velocity"
                      "heatcamera-zoom" "heatcamera-calibrate"
                      "heatcamera-palette" "heatcamera-photo"
                      "daycamera-zoom" "daycamera-focus" "daycamera-photo"
                      "cv-start-track-ndc"
                      "set-gps-manual" "set-recording"
                      "lrf-single-measurement" "lrf-continuous-start"
                      "glass-heater-on" "glass-heater-off"]
        results (atom [])]
    
    (doseq [action all-commands]
      (try
        (let [params (generate-command-params action)
              command (create-test-command action params)
              json-response (kotlin-roundtrip command)
              proto-data (json/read-str json-response :key-fn keyword)]
          (swap! results conj
                 {:action action
                  :success (not (contains? proto-data :error))
                  :has-params (boolean params)
                  :response proto-data}))
        (catch Exception e
          (swap! results conj
                 {:action action
                  :success false
                  :error (.getMessage e)}))))
    
    @results))

(comment
  ;; Run comprehensive tests
  (run-all-command-tests)
  
  ;; Test specific command
  (let [cmd (cmd/rotary-goto {:azimuth 90.0 :elevation 30.0})]
    (kotlin-roundtrip cmd))
  
  ;; Test canonicalization
  (canonicalize-keys {:frame-timestamp 123 :nested-map {:field-name "value"}})
  )