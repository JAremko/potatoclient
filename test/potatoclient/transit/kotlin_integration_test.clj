(ns potatoclient.transit.kotlin-integration-test
  "Integration tests that verify the complete Transit → Kotlin → Protobuf flow
  by running actual Kotlin subprocesses and comparing results."
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [potatoclient.transit.commands :as cmd]
            [potatoclient.transit.core :as transit]
            [potatoclient.transit.subprocess-launcher :as launcher]
            [proto-explorer.generated-specs :as specs]
            [proto-explorer.test-data-generator :as gen]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.core.async :as async :refer [<! >! go chan timeout alts!]]
            [taoensso.telemere :as t])
  (:import [java.io File]))

;; =============================================================================
;; Test Subprocess Management
;; =============================================================================

(def ^:dynamic *test-subprocess* nil)
(def ^:dynamic *test-channel* nil)

(defn start-test-subprocess
  "Start the Kotlin test command processor subprocess."
  []
  (let [java-cmd (or (System/getenv "JAVA_HOME")
                     "java")
        classpath (str "target/classes" File/pathSeparator "target/test-classes")
        main-class "potatoclient.kotlin.transit.TestCommandProcessorKt"
        process-builder (ProcessBuilder. 
                         [java-cmd "-cp" classpath main-class])
        _ (.redirectError process-builder ProcessBuilder$Redirect/INHERIT)
        process (.start process-builder)
        in-chan (chan 100)
        out-chan (chan 100)]
    
    ;; Start Transit communication
    (launcher/start-transit-communication process in-chan out-chan)
    
    {:process process
     :in-chan in-chan
     :out-chan out-chan}))

(defn stop-test-subprocess
  "Stop the test subprocess."
  [subprocess]
  (when subprocess
    ;; Send shutdown command
    (>!! (:in-chan subprocess)
         {:msg-type "control"
          :msg-id (str (random-uuid))
          :timestamp (System/currentTimeMillis)
          :payload {:action "shutdown"}})
    
    ;; Wait for process to exit
    (Thread/sleep 100)
    
    ;; Force kill if needed
    (when (.isAlive (:process subprocess))
      (.destroyForcibly (:process subprocess)))
    
    ;; Close channels
    (async/close! (:in-chan subprocess))
    (async/close! (:out-chan subprocess))))

(defn subprocess-fixture
  "Test fixture that starts/stops subprocess."
  [f]
  (let [subprocess (start-test-subprocess)]
    (binding [*test-subprocess* subprocess
              *test-channel* (:out-chan subprocess)]
      (try
        (f)
        (finally
          (stop-test-subprocess subprocess))))))

(use-fixtures :once subprocess-fixture)

;; =============================================================================
;; Test Helpers
;; =============================================================================

(defn send-command
  "Send a command to the test subprocess and wait for response."
  [action params timeout-ms]
  (let [msg-id (str (random-uuid))
        message {:msg-type "command"
                 :msg-id msg-id
                 :timestamp (System/currentTimeMillis)
                 :payload {:action action
                           :params params}}]
    
    ;; Send command
    (>!! (:in-chan *test-subprocess*) message)
    
    ;; Wait for response
    (go
      (let [timeout-ch (timeout timeout-ms)
            [response ch] (alts! [*test-channel* timeout-ch])]
        (if (= ch timeout-ch)
          {:error "Timeout waiting for response"}
          response)))))

(defn get-proto-json
  "Extract protobuf JSON from subprocess response."
  [response]
  (when-let [json-str (get-in response [:payload :json])]
    (json/read-str json-str :key-fn keyword)))

(defn canonicalize-for-comparison
  "Canonicalize data for comparison between Transit and Protobuf.
  Handles key conversions and type normalizations."
  [data]
  (letfn [(kebab->snake [s]
            (str/replace s #"-" "_"))
          (process-value [v]
            (cond
              (map? v) (canonicalize-for-comparison v)
              (sequential? v) (mapv process-value v)
              (keyword? v) (name v)
              (float? v) (Double/parseDouble (format "%.6f" v))
              :else v))]
    (cond
      (map? data)
      (into {}
            (map (fn [[k v]]
                   [(if (keyword? k)
                      (keyword (kebab->snake (name k)))
                      (kebab->snake (str k)))
                    (process-value v)])
                 data))
      
      (sequential? data)
      (mapv canonicalize-for-comparison data)
      
      :else data)))

;; =============================================================================
;; Integration Tests
;; =============================================================================

(deftest test-subprocess-communication
  (testing "Basic subprocess communication"
    (go
      (let [response (<! (send-command "ping" nil 1000))]
        (is (= "response" (:msg-type response)))
        (is (contains? (:payload response) :json))
        
        (let [proto-result (get-proto-json response)]
          (is (:success proto-result))
          (is (contains? (:proto proto-result) :ping)))))))

(deftest test-parameterized-commands
  (testing "Commands with parameters"
    (go
      (let [params {:azimuth 123.45 :elevation -15.0}
            response (<! (send-command "rotaryplatform-goto" params 1000))
            proto-result (get-proto-json response)]
        
        (is (:success proto-result))
        
        (let [proto-data (:proto proto-result)
              goto-data (:goto proto-data)]
          (is (= 123.45 (:azimuth goto-data)))
          (is (= -15.0 (:elevation goto-data))))))))

(deftest test-all-command-types
  (testing "All command types from transit/commands.clj"
    ;; Load specs
    (specs/load-all-specs! "shared/specs/protobuf")
    
    (let [test-cases
          [{:fn #(cmd/ping) :action "ping"}
           {:fn #(cmd/rotary-goto {:azimuth 180.0 :elevation 45.0})
            :action "rotaryplatform-goto"}
           {:fn #(cmd/heat-camera-zoom 4.0)
            :action "heatcamera-zoom"}
           {:fn #(cmd/set-gps-manual {:use-manual true
                                      :latitude 51.5
                                      :longitude -0.1
                                      :altitude 100.0})
            :action "set-gps-manual"}
           {:fn #(cmd/cv-start-track-ndc :heat 0.5 -0.25 12345)
            :action "cv-start-track-ndc"}]]
      
      (doseq [{:keys [fn action]} test-cases]
        (testing (str "Command: " action)
          (go
            (let [command (fn)
                  params (:params command)
                  response (<! (send-command action params 2000))
                  proto-result (get-proto-json response)]
              
              (is (:success proto-result)
                  (str "Failed: " (:error proto-result)))
              
              (when (:success proto-result)
                ;; Compare canonicalized versions
                (let [original (canonicalize-for-comparison params)
                      proto-params (-> proto-result
                                       :proto
                                       vals
                                       first
                                       canonicalize-for-comparison)]
                  ;; For commands with params, verify roundtrip
                  (when params
                    (is (= original proto-params)
                        (str "Mismatch:\nOriginal: " original
                             "\nProto: " proto-params))))))))))))

(deftest test-error-handling
  (testing "Error handling for invalid commands"
    (go
      (let [response (<! (send-command "invalid-command" {} 1000))
            proto-result (get-proto-json response)]
        
        (is (false? (:success proto-result)))
        (is (str/includes? (:error proto-result) "Unknown"))))))

(deftest test-missing-required-params
  (testing "Missing required parameters"
    (go
      ;; rotaryplatform-goto requires both azimuth and elevation
      (let [response (<! (send-command "rotaryplatform-goto" 
                                       {:azimuth 45.0} ; missing elevation
                                       1000))
            proto-result (get-proto-json response)]
        
        (is (false? (:success proto-result)))
        (is (str/includes? (:error proto-result) "elevation"))))))

(deftest test-generated-data-roundtrip
  (testing "Roundtrip with proto-explorer generated data"
    (specs/load-all-specs! "shared/specs/protobuf")
    
    (let [spec-mappings
          {"rotaryplatform-goto" :cmd.RotaryPlatform/goto
           "heatcamera-zoom" :cmd.HeatCamera/zoom
           "set-gps-manual" :cmd/set-gps-manual}]
      
      (doseq [[action spec-key] spec-mappings]
        (testing (str "Generated data for " action)
          (go
            (when-let [generated-data (try
                                        (gen/generate-data spec-key)
                                        (catch Exception e
                                          (t/log! :warn 
                                                  "Failed to generate data"
                                                  {:action action
                                                   :error (.getMessage e)})
                                          nil))]
              (let [response (<! (send-command action generated-data 2000))
                    proto-result (get-proto-json response)]
                
                (is (:success proto-result)
                    (str "Failed with generated data: " (:error proto-result)))))))))))

(deftest test-enum-handling
  (testing "Enum value conversion"
    (go
      (let [params {:palette "white-hot"}
            response (<! (send-command "heatcamera-palette" params 1000))
            proto-result (get-proto-json response)]
        
        (is (:success proto-result))
        
        ;; Check enum was converted to uppercase
        (let [palette-data (get-in proto-result [:proto :palette :palette])]
          (is (= "WHITE_HOT" palette-data)))))))

(deftest test-optional-fields
  (testing "Optional field handling"
    (go
      ;; Test with optional field present
      (let [params-with {:mode "manual" :distance 10.0}
            response1 (<! (send-command "daycamera-focus" params-with 1000))
            proto1 (get-proto-json response1)]
        
        (is (:success proto1))
        (is (= 10.0 (get-in proto1 [:proto :focus :distance]))))
      
      ;; Test with optional field absent
      (let [params-without {:mode "auto"}
            response2 (<! (send-command "daycamera-focus" params-without 1000))
            proto2 (get-proto-json response2)]
        
        (is (:success proto2))
        (is (not (contains? (get-in proto2 [:proto :focus]) :distance)))))))

;; =============================================================================
;; Performance Tests
;; =============================================================================

(deftest test-performance-metrics
  (testing "Command processing performance"
    (go
      (let [iterations 100
            start-time (System/currentTimeMillis)]
        
        ;; Send many commands
        (dotimes [i iterations]
          (let [params {:azimuth (* i 3.6) :elevation 30.0}
                response (<! (send-command "rotaryplatform-goto" params 100))]
            (is (get-in response [:payload :json]))))
        
        (let [end-time (System/currentTimeMillis)
              total-time (- end-time start-time)
              avg-time (/ total-time iterations)]
          
          (println (str "\nPerformance: " iterations " commands in " total-time "ms"))
          (println (str "Average: " avg-time "ms per command"))
          
          ;; Assert reasonable performance
          (is (< avg-time 50) "Average command time should be < 50ms"))))))

(comment
  ;; Run individual tests
  (run-tests)
  
  ;; Start subprocess manually for debugging
  (def sub (start-test-subprocess))
  
  ;; Send test command
  (go
    (>! (:in-chan sub)
        {:msg-type "command"
         :msg-id "test-1"
         :timestamp (System/currentTimeMillis)
         :payload {:action "ping"}})
    
    (println (<! (:out-chan sub))))
  
  ;; Stop subprocess
  (stop-test-subprocess sub)
  )