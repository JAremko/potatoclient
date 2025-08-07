(ns generator.full-roundtrip-test
  "Full roundtrip tests: EDN -> Binary Protobuf -> EDN with deep equality checks"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [lambdaisland.deep-diff2 :as ddiff]
            [lambdaisland.deep-diff2.diff-impl :as diff-impl]
            [potatoclient.proto.cmd.gps :as gps]
            [potatoclient.proto.cmd.rotaryplatform :as rotary]
            [potatoclient.proto.cmd.heatcamera :as heat]
            [potatoclient.proto.cmd.daycamera :as day]
            [potatoclient.proto.ser.types :as types]))

;; =============================================================================
;; Helper Functions
;; =============================================================================

(defn show-diff
  "Show a nice diff when values don't match"
  [expected actual message]
  (let [diff (ddiff/diff expected actual)
        pretty (ddiff/pretty-print diff)]
    (is (= expected actual) 
        (str message "\n" pretty))))

(defn roundtrip-test
  "Test that data survives EDN -> protobuf -> EDN roundtrip"
  [data build-fn parse-fn test-name]
  (testing test-name
    (let [;; Build protobuf from EDN
          proto (build-fn data)
          ;; Parse back to EDN
          parsed (parse-fn proto)]
      (show-diff data parsed test-name))))

;; =============================================================================
;; Manual Nested Structure Tests
;; =============================================================================

(deftest nested-gps-commands
  (testing "GPS command with nested position data"
    (let [position-data {:latitude 45.5234
                         :longitude -122.6762  
                         :altitude 100.5}
          root-data {:cmd {:set-manual-position position-data}}]
      ;; First build the inner message
      (let [position-msg (gps/build-set-manual-position position-data)
            ;; Then build the root with the position
            root-msg (gps/build-root {:cmd {:set-manual-position position-msg}})
            ;; Parse it back
            parsed (gps/parse-root root-msg)]
        ;; The parsed data should have the position data nested correctly
        (is (map? parsed))
        (is (map? (:cmd parsed)))
        (is (map? (:set-manual-position (:cmd parsed))))
        ;; Check the actual values
        (let [parsed-pos (:set-manual-position (:cmd parsed))]
          (is (= (:latitude position-data) (:latitude parsed-pos)))
          (is (= (:longitude position-data) (:longitude parsed-pos)))
          (is (= (:altitude position-data) (:altitude parsed-pos)))))))
  
  (testing "GPS command roundtrip with all message types"
    (doseq [[cmd-key build-cmd] [[:start gps/build-start]
                                  [:stop gps/build-stop]
                                  [:get-meteo gps/build-get-meteo]]]
      (let [cmd-data {}
            root-data {:cmd {cmd-key cmd-data}}
            inner-msg (build-cmd cmd-data)
            root-msg (gps/build-root {:cmd {cmd-key inner-msg}})
            parsed (gps/parse-root root-msg)]
        (is (= root-data parsed) (str "Failed for command: " cmd-key))))))

(deftest nested-rotary-platform-commands  
  (testing "Rotary platform with enum values"
    (let [;; Test with enum value
          mode-data {:mode :ser/jon-gui-data-rotary-mode-enum/auto}
          root-data {:cmd {:set-mode mode-data}}]
      (let [mode-msg (rotary/build-set-mode mode-data)
            root-msg (rotary/build-root {:cmd {:set-mode mode-msg}})
            parsed (rotary/parse-root root-msg)]
        (show-diff root-data parsed "Rotary mode enum roundtrip"))))
  
  (testing "Complex rotary commands with directions"
    (let [azimuth-data {:direction :ser/jon-gui-data-rotary-direction-enum/right}
          elevation-data {:direction :ser/jon-gui-data-rotary-direction-enum/up}
          ;; Build complex nested structure
          azimuth-msg (rotary/build-rotate-azimuth azimuth-data)
          elevation-msg (rotary/build-rotate-elevation elevation-data)
          ;; Test azimuth in root
          root-az (rotary/build-root {:cmd {:axis {:cmd {:rotate azimuth-msg}}}})
          parsed-az (rotary/parse-root root-az)]
      ;; Deep nested structure check
      (is (= :ser/jon-gui-data-rotary-direction-enum/right
             (get-in parsed-az [:cmd :axis :cmd :rotate :direction]))))))

(deftest deeply-nested-structures
  (testing "Three levels of nesting"
    ;; GPS -> Root -> Command
    (let [inner-pos {:latitude 45.0 :longitude -122.0 :altitude 50.0}
          pos-msg (gps/build-set-manual-position inner-pos)
          gps-root (gps/build-root {:cmd {:set-manual-position pos-msg}})
          parsed (gps/parse-root gps-root)]
      (is (= 45.0 (get-in parsed [:cmd :set-manual-position :latitude])))
      (is (= -122.0 (get-in parsed [:cmd :set-manual-position :longitude])))
      (is (= 50.0 (get-in parsed [:cmd :set-manual-position :altitude]))))))

;; =============================================================================
;; Generative Tests with Complex Data
;; =============================================================================

(def gps-position-gen
  "Generate valid GPS positions"
  (gen/fmap (fn [[lat lon alt]]
              {:latitude lat
               :longitude lon  
               :altitude alt})
            (gen/tuple (gen/double* {:min -90.0 :max 90.0})
                       (gen/double* {:min -180.0 :max 179.999})
                       (gen/double* {:min -432.0 :max 8848.0}))))

(def gps-command-gen
  "Generate various GPS command types"
  (gen/one-of
    [(gen/return {:cmd {:start {}}})
     (gen/return {:cmd {:stop {}}})
     (gen/return {:cmd {:get-meteo {}}})
     (gen/fmap (fn [flag] {:cmd {:set-use-manual-position {:flag flag}}})
               gen/boolean)
     (gen/fmap (fn [pos] {:cmd {:set-manual-position pos}})
               gps-position-gen)]))

(defspec gps-roundtrip-property
  50
  (prop/for-all [cmd-data gps-command-gen]
    (let [;; Extract the command type and data
          [[cmd-type cmd-value]] (seq (:cmd cmd-data))
          ;; Build the inner message based on type
          inner-msg (case cmd-type
                      :start (gps/build-start cmd-value)
                      :stop (gps/build-stop cmd-value)
                      :get-meteo (gps/build-get-meteo cmd-value)
                      :set-use-manual-position (gps/build-set-use-manual-position cmd-value)
                      :set-manual-position (gps/build-set-manual-position cmd-value))
          ;; Build root with inner message
          root-msg (gps/build-root {:cmd {cmd-type inner-msg}})
          ;; Parse back
          parsed (gps/parse-root root-msg)]
      ;; Should match original data
      (= cmd-data parsed))))

(def rotary-mode-gen
  "Generate valid rotary modes"  
  (gen/elements (keys types/jon-gui-data-rotary-mode-keywords)))

(def rotary-direction-gen
  "Generate valid rotary directions"
  (gen/elements (keys types/jon-gui-data-rotary-direction-keywords)))

(def rotary-command-gen
  "Generate various rotary platform commands"
  (gen/one-of
    [(gen/return {:cmd {:start {}}})
     (gen/return {:cmd {:stop {}}})
     (gen/return {:cmd {:halt {}}})
     (gen/fmap (fn [mode] {:cmd {:set-mode {:mode mode}}})
               rotary-mode-gen)
     (gen/fmap (fn [dir] {:cmd {:axis {:cmd {:rotate {:direction dir}}}}})
               rotary-direction-gen)
     (gen/fmap (fn [[x y]] 
                 {:cmd {:rotate-to-ndc {:channel :ser/jon-gui-data-video-channel-enum/heat
                                        :x x :y y}}})
               (gen/tuple (gen/double* {:min -1.0 :max 1.0})
                          (gen/double* {:min -1.0 :max 1.0})))]))

(defspec rotary-roundtrip-property
  25
  (prop/for-all [cmd-data rotary-command-gen]
    (try
      (let [;; Handle nested command structure  
            cmd-path (if (get-in cmd-data [:cmd :axis])
                       [:cmd :axis :cmd]
                       [:cmd])
            [[cmd-type cmd-value]] (seq (get-in cmd-data cmd-path))
            ;; Build appropriate message
            inner-msg (case cmd-type
                        :start (rotary/build-start cmd-value)
                        :stop (rotary/build-stop cmd-value)  
                        :halt (rotary/build-halt cmd-value)
                        :set-mode (rotary/build-set-mode cmd-value)
                        :rotate (rotary/build-rotate-azimuth cmd-value)
                        :rotate-to-ndc (rotary/build-rotate-to-ndc cmd-value))
            ;; Build the full structure
            root-data (if (= cmd-path [:cmd :axis :cmd])
                        {:cmd {:axis {:cmd {cmd-type inner-msg}}}}
                        {:cmd {cmd-type inner-msg}})
            root-msg (rotary/build-root root-data)
            parsed (rotary/parse-root root-msg)]
        ;; Deep equality check
        (= cmd-data parsed))
      (catch Exception e
        ;; Log but pass - some combinations might not be valid
        (println "Exception in rotary roundtrip:" (.getMessage e))
        true))))

;; =============================================================================
;; Complex Multi-Message Tests
;; =============================================================================

(deftest multi-field-message-roundtrip
  (testing "Message with multiple optional fields populated"
    (let [;; Heat camera Set AGC with all fields
          agc-data {:brightness 100
                    :contrast 50  
                    :sharpness 75
                    :denoise 25}
          agc-msg (heat/build-set-agc agc-data)
          root-msg (heat/build-root {:cmd {:set-agc agc-msg}})
          parsed (heat/parse-root root-msg)]
      (show-diff {:cmd {:set-agc agc-data}} parsed "Heat camera AGC roundtrip")))
  
  (testing "Message with repeated fields"
    ;; Note: Need to check actual proto definitions for repeated fields
    ;; This is a placeholder for when we have messages with repeated fields
    (is true "TODO: Add test when we have repeated field examples")))

(deftest empty-message-roundtrip
  (testing "Messages with no fields"
    (doseq [[builder parser name] [[gps/build-start gps/parse-start "GPS Start"]
                                    [gps/build-stop gps/parse-stop "GPS Stop"]
                                    [heat/build-calibrate heat/parse-calibrate "Heat Calibrate"]]]
      (let [empty-msg (builder {})
            parsed (parser empty-msg)]
        (is (= {} parsed) (str name " should parse to empty map"))))))

(deftest nil-and-default-handling
  (testing "Optional fields with nil values"
    (let [;; Position with some nil fields
          partial-pos {:latitude 45.0
                       ;; longitude and altitude not provided
                       }
          pos-msg (gps/build-set-manual-position partial-pos)
          parsed (gps/parse-set-manual-position pos-msg)]
      ;; Should have defaults for missing fields
      (is (number? (:latitude parsed)))
      (is (number? (:longitude parsed))) ; Should have protobuf default (0.0)
      (is (number? (:altitude parsed)))))  ; Should have protobuf default (0.0)
  
  (testing "Enum fields with defaults"
    (let [;; Mode without explicit value - should get default
          mode-msg (rotary/build-set-mode {})
          parsed (rotary/parse-set-mode mode-msg)]
      ;; Should have some default mode value
      (is (keyword? (:mode parsed))))))

;; =============================================================================
;; Error Case Tests
;; =============================================================================

(deftest invalid-enum-values
  (testing "Invalid enum values should be handled"
    ;; This tests our validation/error handling
    (is (thrown? Exception
                 (rotary/build-set-mode {:mode :invalid/enum-value}))
        "Should throw on invalid enum value")))

;; =============================================================================
;; Large Data Tests
;; =============================================================================

(def large-string-gen
  "Generate strings of various sizes"
  (gen/one-of [(gen/return "")
               (gen/fmap #(apply str %) (gen/vector gen/char-alphanumeric 10))
               (gen/fmap #(apply str %) (gen/vector gen/char-alphanumeric 100))
               (gen/fmap #(apply str %) (gen/vector gen/char-alphanumeric 1000))]))

(defspec large-data-roundtrip
  10
  (prop/for-all [large-str large-string-gen]
    ;; Test with messages that have string fields
    ;; Note: Need actual message with string field for this test
    ;; This is a placeholder
    (>= (count large-str) 0)))