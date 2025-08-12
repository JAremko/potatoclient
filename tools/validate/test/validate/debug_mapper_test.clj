(ns validate.debug-mapper-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [validate.test-harness :as h]
   [pronto.core :as p])
  (:import [cmd JonSharedCmd$Root]))

(deftest test-mapper-loading
  (testing "Can create proto-map from simple command"
    (let [cmd-example {:protocol_version 1
                       :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                       :ping {}}]
      (println "\n=== Testing mapper ===")
      (println "Input:" cmd-example)
      (try
        (let [proto-map (p/clj-map->proto-map h/cmd-mapper JonSharedCmd$Root cmd-example)]
          (println "Created proto-map successfully!")
          (is (some? proto-map)))
        (catch Exception e
          (println "Error:" (.getMessage e))
          (is false (str "Failed to create proto-map: " (.getMessage e))))))))