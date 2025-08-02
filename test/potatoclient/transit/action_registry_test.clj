(ns potatoclient.transit.action-registry-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]])
  (:import [potatoclient.transit ActionRegistry ActionDefinition]
           [com.cognitect.transit TransitFactory]))

;; Test basic ActionRegistry functionality from Clojure

(deftest test-basic-commands-registered
  (testing "Basic commands are registered"
    (is (ActionRegistry/isKnownAction "ping"))
    (is (ActionRegistry/isKnownAction "noop"))
    (is (ActionRegistry/isKnownAction "frozen"))))

(deftest test-keyword-lookup
  (testing "Can lookup by Transit keywords"
    (let [ping-kw (TransitFactory/keyword "ping")]
      (is (ActionRegistry/isKnownAction ping-kw))
      (is (instance? ActionDefinition (ActionRegistry/getAction ping-kw))))))

(deftest test-unknown-action
  (testing "Unknown actions return false/nil"
    (is (not (ActionRegistry/isKnownAction "unknown-action")))
    (is (nil? (ActionRegistry/getAction "unknown-action")))))

(deftest test-action-properties
  (testing "Action definitions have correct properties"
    (let [ping (ActionRegistry/getAction "ping")]
      (is (= "ping" (.getName ping)))
      (is (= "Heartbeat/keepalive command" (.getDescription ping)))
      (is (.isImplemented ping))
      (is (empty? (.getRequiredParams ping)))
      (is (empty? (.getOptionalParams ping))))))

(deftest test-get-all-actions
  (testing "Can retrieve all registered actions"
    (let [all-names (ActionRegistry/getAllActionNames)
          all-keywords (ActionRegistry/getAllActionKeywords)]
      (is (>= (count all-names) 3))
      (is (>= (count all-keywords) 3))
      (is (contains? all-names "ping"))
      (is (contains? all-names "noop"))
      (is (contains? all-names "frozen")))))

(deftest test-statistics
  (testing "Registry provides statistics"
    (let [stats (ActionRegistry/getStatistics)]
      (is (instance? java.util.Map stats))
      (is (.containsKey stats "total"))
      (is (.containsKey stats "implemented"))
      (is (.containsKey stats "unimplemented"))
      (is (>= (.get stats "total") 3))
      (is (>= (.get stats "implemented") 3)))))

(deftest test-parameter-checking
  (testing "Can check required parameters"
    ;; Basic commands have no required params
    (is (ActionRegistry/hasRequiredParams "ping" {}))
    (is (ActionRegistry/hasRequiredParams "ping" {:extra "param"}))))

(deftest test-from-clojure-keywords
  (testing "Works with Clojure keywords"
    ;; Note: ActionRegistry expects Transit keywords, not Clojure keywords
    ;; This is intentional to maintain clear boundaries
    (let [clj-kw :ping
          transit-kw (TransitFactory/keyword (name clj-kw))]
      ;; ActionRegistry only accepts String or Transit Keyword, not Clojure keyword
      (is (ActionRegistry/isKnownAction transit-kw))
      (is (ActionRegistry/isKnownAction "ping"))))

(deftest test-gps-commands-registered
  (testing "GPS commands are registered with parameters"
    (is (ActionRegistry/isKnownAction "gps-start"))
    (is (ActionRegistry/isKnownAction "gps-stop"))
    (is (ActionRegistry/isKnownAction "gps-set-manual-position"))
    (is (ActionRegistry/isKnownAction "gps-set-use-manual-position"))
    (is (ActionRegistry/isKnownAction "gps-get-meteo"))))

(deftest test-gps-command-parameters
  (testing "GPS commands have correct parameters"
    (let [manual-pos (ActionRegistry/getAction "gps-set-manual-position")
          use-manual (ActionRegistry/getAction "gps-set-use-manual-position")]
      ;; Manual position requires lat/lon/alt
      (is (= 3 (count (.getRequiredParams manual-pos))))
      (is (.isRequired manual-pos (TransitFactory/keyword "latitude")))
      (is (.isRequired manual-pos (TransitFactory/keyword "longitude")))
      (is (.isRequired manual-pos (TransitFactory/keyword "altitude")))
      
      ;; Use manual position requires flag
      (is (= 1 (count (.getRequiredParams use-manual))))
      (is (.isRequired use-manual (TransitFactory/keyword "flag"))))))

(deftest test-parameter-checking-with-gps
  (testing "Parameter checking works for commands with parameters"
    ;; Valid parameters
    (is (ActionRegistry/hasRequiredParams "gps-set-manual-position"
                                          {"latitude" 45.0
                                           "longitude" -122.0
                                           "altitude" 100.0}))
    ;; Missing parameter
    (is (not (ActionRegistry/hasRequiredParams "gps-set-manual-position"
                                                {"latitude" 45.0
                                                 "longitude" -122.0})))
    ;; Extra parameters are ok
    (is (ActionRegistry/hasRequiredParams "gps-set-manual-position"
                                          {"latitude" 45.0
                                           "longitude" -122.0
                                           "altitude" 100.0
                                           "extra" "ignored"})))))