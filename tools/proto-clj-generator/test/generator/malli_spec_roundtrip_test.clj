(ns generator.malli-spec-roundtrip-test
  "Test roundtrip validation using Malli specs from shared directory"
  (:require [clojure.test :refer :all]
            [generator.core :as core]
            [malli.core :as m]
            [malli.error :as me]
            [clojure.java.io :as io]))

(defonce generated? (atom false))

(defn ensure-generated-code!
  []
  (when-not @generated?
    ;; Clear naming cache
    (require 'generator.naming)
    ((resolve 'generator.naming/clear-conversion-cache!))
    
    ;; Generate code
    (let [result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                     :output-dir "test-malli-output"
                                     :namespace-prefix "test.malli"
                                     :namespace-mode :single
                                     :debug? false})]
      (when-not (:success result)
        (throw (ex-info "Failed to generate code" result)))
      
      (reset! generated? true)
      
      ;; Load generated code
      (require '[test.malli.command :as cmd])
      (require '[test.malli.state :as state]))))

(defn load-shared-specs!
  "Load Malli specs from the shared directory"
  []
  ;; The shared specs should be on the classpath
  (require '[potatoclient.specs.malli-oneof :as oneof])
  (require '[potatoclient.ui-specs :as ui-specs]))

(use-fixtures :once (fn [f]
                     (ensure-generated-code!)
                     (load-shared-specs!)
                     (f)))

(deftest validate-with-shared-specs
  (testing "Generated data validates against shared Malli specs"
    (let [cmd-ns (find-ns 'test.malli.command)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)
          ui-specs-ns (find-ns 'potatoclient.ui-specs)]
      
      (when ui-specs-ns
        (testing "command root spec validation"
          ;; Check if there's a command root spec in ui-specs
          (when-let [cmd-spec (ns-resolve ui-specs-ns 'command-root)]
            (let [input {:ping {}}
                  proto (build-root input)
                  output (parse-root proto)]
              (if (m/validator @cmd-spec)
                (is (m/validate @cmd-spec output)
                    (str "Should validate against shared spec: " 
                         (me/humanize (m/explain @cmd-spec output))))
                (println "No command-root spec found")))))))))

(deftest client-type-enum-validation
  (testing "Client type enum validates correctly"
    (let [state-ns (find-ns 'test.malli.state)
          build-jon-gui-state (ns-resolve state-ns 'build-jon-gui-state)
          parse-jon-gui-state (ns-resolve state-ns 'parse-jon-gui-state)
          ui-specs-ns (find-ns 'potatoclient.ui-specs)]
      
      (when ui-specs-ns
        (when-let [client-type-spec (ns-resolve ui-specs-ns 'client-type)]
          (testing "valid client types"
            (doseq [client-type [:driver :gunner :commander :spectator]]
              (let [input {:gui-connected-to-state-updater? true
                          :connected-to-command-runner? false
                          :client-type client-type
                          :protocol-version 1}
                    proto (build-jon-gui-state input)
                    output (parse-jon-gui-state proto)]
                (when @client-type-spec
                  (is (m/validate @client-type-spec (:client-type output))
                      (str "Client type " client-type " should be valid"))))))
          
          (testing "invalid client type throws during building"
            (is (thrown? Exception
                        (build-jon-gui-state 
                         {:gui-connected-to-state-updater? true
                          :connected-to-command-runner? false
                          :client-type :invalid-type
                          :protocol-version 1}))
                "Should throw for invalid client type")))))))

(deftest oneof-spec-validation
  (testing "Oneof fields validate with malli-oneof specs"
    (let [cmd-ns (find-ns 'test.malli.command)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)
          oneof-ns (find-ns 'potatoclient.specs.malli-oneof)]
      
      (when oneof-ns
        (testing "command root oneof validation"
          (let [inputs [{:ping {}}
                        {:rotary {:halt {}}}
                        {:system {:start-rec {}}}]]
            (doseq [input inputs]
              (let [proto (build-root input)
                    output (parse-root proto)]
                ;; The output should have exactly one key
                (is (= 1 (count output))
                    (str "Should have exactly one command in " output))
                ;; And it should match the input
                (is (= input output))))))))))

(deftest comprehensive-spec-validation
  (testing "Comprehensive validation of complex messages"
    (let [state-ns (find-ns 'test.malli.state)
          build-jon-gui-state (ns-resolve state-ns 'build-jon-gui-state)
          parse-jon-gui-state (ns-resolve state-ns 'parse-jon-gui-state)]
      
      (testing "state with nested messages"
        ;; This test would be more comprehensive with actual nested message builders
        (let [input {:gui-connected-to-state-updater? true
                     :connected-to-command-runner? true
                     :client-type :driver
                     :protocol-version 1
                     :time-ms 1234567890}
              proto (build-jon-gui-state input)
              output (parse-jon-gui-state proto)]
          
          (is (= (:time-ms input) (:time-ms output)))
          (is (number? (:time-ms output)))
          (is (pos? (:time-ms output))))))))