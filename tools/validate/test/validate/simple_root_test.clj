(ns validate.simple-root-test
  "Simplified test for just the root messages."
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   ;; Load all specs
   [potatoclient.specs.common]
   [potatoclient.specs.cmd.root]
   [potatoclient.specs.state.root]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(deftest test-cmd-root-generation
  (testing "Can generate valid cmd/root messages"
    (dotimes [_ 10]
      (let [generated (mg/generate :cmd/root)]
        (is (m/validate :cmd/root generated)
            (str "Generated command should validate: " generated))))))

(deftest test-state-root-generation
  (testing "Can generate valid state/root messages"
    (dotimes [_ 10]
      (let [generated (mg/generate :state/root)]
        (is (m/validate :state/root generated)
            (str "Generated state should validate: " generated))))))