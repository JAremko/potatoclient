(ns potatoclient.transit.action-registry-integration-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str])
  (:import [potatoclient.transit ActionRegistry ActionDefinition]
           [com.cognitect.transit TransitFactory]))

(deftest test-command-count
  (testing "Registry has expected number of commands"
    (let [all-commands (ActionRegistry/getAllActionNames)]
      ;; We've registered: 3 basic + 5 GPS + 35 rotary + 13 system + 9 CV = 65 commands
      (is (>= (count all-commands) 65)
          (str "Expected at least 65 commands, got " (count all-commands))))))

(deftest test-rotary-commands
  (testing "Important rotary commands are registered"
    ;; Test the most important rotary command
    (let [rotate-ndc (ActionRegistry/getAction "rotary-rotate-to-ndc")]
      (is (not (nil? rotate-ndc)))
      (is (= "rotary-rotate-to-ndc" (.getName rotate-ndc)))
      (is (= 3 (count (.getRequiredParams rotate-ndc))))
      (is (.isRequired rotate-ndc (TransitFactory/keyword "channel")))
      (is (.isRequired rotate-ndc (TransitFactory/keyword "x")))
      (is (.isRequired rotate-ndc (TransitFactory/keyword "y"))))
    
    ;; Test scanning commands
    (is (ActionRegistry/isKnownAction "rotary-scan-start"))
    (is (ActionRegistry/isKnownAction "rotary-scan-stop"))
    (is (ActionRegistry/isKnownAction "rotary-scan-add-node"))
    
    ;; Test complex scan node command
    (let [scan-add (ActionRegistry/getAction "rotary-scan-add-node")]
      (is (= 7 (count (.getRequiredParams scan-add))))
      (is (.isRequired scan-add (TransitFactory/keyword "index")))
      (is (.isRequired scan-add (TransitFactory/keyword "azimuth")))
      (is (.isRequired scan-add (TransitFactory/keyword "elevation"))))))

(deftest test-cv-commands
  (testing "CV tracking commands are registered"
    ;; Test the important CV tracking command
    (let [start-track (ActionRegistry/getAction "cv-start-track-ndc")]
      (is (not (nil? start-track)))
      (is (= 4 (count (.getRequiredParams start-track))))
      (is (.isRequired start-track (TransitFactory/keyword "channel")))
      (is (.isRequired start-track (TransitFactory/keyword "x")))
      (is (.isRequired start-track (TransitFactory/keyword "y")))
      (is (.isRequired start-track (TransitFactory/keyword "frame-time"))))
    
    ;; Test other CV commands
    (is (ActionRegistry/isKnownAction "cv-stop-track"))
    (is (ActionRegistry/isKnownAction "cv-vampire-mode-enable"))
    (is (ActionRegistry/isKnownAction "cv-stabilization-mode-enable"))))

(deftest test-system-commands
  (testing "System commands are registered"
    (is (ActionRegistry/isKnownAction "system-start-all"))
    (is (ActionRegistry/isKnownAction "system-stop-all"))
    (is (ActionRegistry/isKnownAction "system-reboot"))
    (is (ActionRegistry/isKnownAction "system-start-rec"))
    (is (ActionRegistry/isKnownAction "system-stop-rec"))
    
    ;; Test localization command with parameter
    (let [set-loc (ActionRegistry/getAction "system-set-localization")]
      (is (= 1 (count (.getRequiredParams set-loc))))
      (is (.isRequired set-loc (TransitFactory/keyword "loc"))))))

(deftest test-command-naming-convention
  (testing "All commands follow kebab-case naming convention"
    (let [all-names (ActionRegistry/getAllActionNames)]
      (doseq [name all-names]
        (is (re-matches #"^[a-z]+(-[a-z]+)*$" name)
            (str "Command name not kebab-case: " name))))))

(deftest test-parameter-naming-convention
  (testing "All parameters follow kebab-case naming convention"
    (let [all-names (ActionRegistry/getAllActionNames)]
      (doseq [name all-names]
        (let [action (ActionRegistry/getAction name)
              all-params (concat (seq (.getRequiredParams action))
                                 (seq (.getOptionalParams action)))]
          (doseq [param all-params]
            (let [param-name (.getName param)]
              (is (re-matches #"^[a-z]+(-[a-z]+)*$" param-name)
                  (str "Parameter not kebab-case: " param-name 
                       " in command " name)))))))))

(deftest test-no-duplicate-commands
  (testing "No duplicate command names"
    (let [all-names (ActionRegistry/getAllActionNames)
          name-list (vec all-names)]
      (is (= (count name-list) (count (set name-list)))
          "Found duplicate command names"))))

(deftest test-statistics
  (testing "Registry statistics are reasonable"
    (let [stats (ActionRegistry/getStatistics)
          total (.get stats "total")
          implemented (.get stats "implemented")
          unimplemented (.get stats "unimplemented")]
      (is (>= total 65))
      (is (>= implemented 0))
      (is (>= unimplemented 0))
      (is (= total (+ implemented unimplemented))))))

(deftest test-command-descriptions
  (testing "All commands have descriptions"
    (let [all-names (ActionRegistry/getAllActionNames)]
      (doseq [name all-names]
        (let [action (ActionRegistry/getAction name)
              description (.getDescription action)]
          (is (not (str/blank? description))
              (str "Command missing description: " name)))))))