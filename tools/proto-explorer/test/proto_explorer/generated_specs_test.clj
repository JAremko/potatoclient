(ns proto-explorer.generated-specs-test
  (:require [clojure.test :refer :all]
            [proto-explorer.generated-specs :as gs]
            [clojure.java.io :as io]))

(deftest test-kebab-snake-conversion
  (testing "kebab->snake conversion"
    (is (= "protocol_version" (gs/kebab->snake "protocol-version")))
    (is (= "heat_camera" (gs/kebab->snake "heat-camera")))
    (is (= "simple" (gs/kebab->snake "simple")))))

(deftest test-load-spec-file
  (testing "Load a spec file"
    ;; Create a temporary spec file for testing
    (let [temp-file (io/file "test-spec.clj")
          spec-content "(ns test.specs \"Test specs\" (:require [malli.core]))

(def TestMessage \"Schema for TestMessage\" [:map [:field-one [:maybe :string]] [:field-two [:maybe :int]]])

(def TestEnum \"Schema for TestEnum\" [:enum :VALUE-ONE :VALUE-TWO])

"]
      
      (try
        (spit temp-file spec-content)
        (let [specs (gs/load-spec-file temp-file)]
          (is (map? specs))
          (is (contains? specs :test.specs/TestMessage))
          (is (contains? specs :test.specs/TestEnum))
          (is (vector? (:test.specs/TestMessage specs)))
          (is (= :map (first (:test.specs/TestMessage specs)))))
        (finally
          (.delete temp-file)))))
  
  (testing "Load a spec file with hyphenated namespace"
    ;; Test regression for hyphenated namespaces like cmd.Lrf-calib
    (let [temp-file (io/file "test-hyphen-spec.clj")
          spec-content "(ns test.specs.cmd.Lrf-calib \"Test specs with hyphen\" (:require [malli.core]))

(def root \"Schema for root\" [:map [:channel [:oneof {:day [:map [:day :cmd/offsets]], :heat [:map [:heat :cmd/offsets]]}]]])

(def offsets \"Schema for offsets\" [:map [:cmd [:oneof {:set [:map [:set :cmd/set-offsets]]}]]])

"]
      
      (try
        (spit temp-file spec-content)
        (let [specs (gs/load-spec-file temp-file)]
          (is (map? specs))
          (is (contains? specs :test.specs.cmd.Lrf-calib/root))
          (is (contains? specs :test.specs.cmd.Lrf-calib/offsets))
          (is (vector? (:test.specs.cmd.Lrf-calib/root specs)))
          (is (= :map (first (:test.specs.cmd.Lrf-calib/root specs)))))
        (finally
          (.delete temp-file))))))

(deftest test-load-all-specs
  (testing "Load all specs from directory"
    (let [spec-dir "../../shared/specs/protobuf"
          specs (gs/load-all-specs! spec-dir)]
      (is (map? specs))
      (is (pos? (count specs)))
      ;; Check for known specs (with proper case)
      (is (or (contains? specs :potatoclient.specs.cmd/root)
              (contains? specs :cmd/root)))
      (is (or (contains? specs :potatoclient.specs.ser/jon-gui-state)
              (contains? specs :ser/jon-gui-state))))))

(deftest test-get-spec
  (testing "Get spec by key"
    (gs/load-all-specs! "../../shared/specs/protobuf")
    
    (let [spec (gs/get-spec :cmd/Ping)]
      (is (vector? spec))
      (is (= :map (first spec))))))

(deftest test-find-specs
  (testing "Find specs by pattern"
    (gs/load-all-specs! "../../shared/specs/protobuf")
    
    (let [results (gs/find-specs "camera")]
      (is (seq results))
      (is (every? (fn [[k _]] 
                   (re-find #"(?i)camera" (str k))) 
                 results)))))

(deftest test-spec->field-info
  (testing "Extract field info from spec"
    (let [spec [:map 
                [:protocol-version [:maybe :int]]
                [:session-id [:maybe :int]]
                [:important [:maybe :boolean]]]
          fields (gs/spec->field-info spec)]
      
      (is (= 3 (count fields)))
      (is (some #(= "protocol-version" (:name %)) fields))
      (is (some #(= "protocol_version" (:proto-name %)) fields))
      (is (every? :optional? fields)))))

(deftest test-spec->message-info
  (testing "Extract message info from spec"
    (let [spec-entry [:cmd/TestMessage 
                      [:map [:test-field [:maybe :string]]]]
          info (gs/spec->message-info spec-entry)]
      
      (is (= "TestMessage" (:name info)))
      (is (= "cmd" (:namespace info)))
      (is (= ":cmd/TestMessage" (:full-name info)))
      (is (seq (:fields info))))))

(deftest test-find-message
  (testing "Find message by name"
    (gs/load-all-specs! "../../shared/specs/protobuf")
    
    (let [message (gs/find-message "ping")]
      (is (map? message))
      (is (= "ping" (:name message))))))

(deftest test-list-messages
  (testing "List all messages"
    (gs/load-all-specs! "../../shared/specs/protobuf")
    
    (let [messages (gs/list-messages)]
      (is (seq messages))
      (is (every? #(contains? % :name) messages))
      (is (every? #(contains? % :namespace) messages))
      (is (every? #(contains? % :full-name) messages)))))

(deftest test-validate-data
  (testing "Validate data against spec"
    (gs/load-all-specs! "../../shared/specs/protobuf")
    
    (testing "Valid data"
      (is (gs/validate-data :potatoclient.specs.cmd/ping {})))
    
    (testing "Invalid spec key"
      (is (thrown? Exception 
                  (gs/validate-data :does/not-exist {}))))))

(deftest test-explain-errors
  (testing "Explain validation errors"
    (gs/load-all-specs! "../../shared/specs/protobuf")
    
    (testing "Invalid data - skipping cmd/Root due to circular references"
      ;; cmd/Root has circular references that cause stack overflow
      ;; Use a spec with required fields instead
      (let [explanation (gs/explain-errors :ser/jon-gui-data-rotary 
                                         {:azimuth 400})]  ; Invalid: should be < 360
        (is (some? explanation))))
    
    (testing "Invalid spec key"
      (is (thrown? Exception 
                  (gs/explain-errors :does/not-exist {}))))))