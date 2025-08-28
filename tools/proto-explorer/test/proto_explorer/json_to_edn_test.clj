(ns proto-explorer.json-to-edn-test
  (:require [clojure.test :refer [deftest testing is]]
            [proto-explorer.json-to-edn :as json-to-edn]))

(deftest normalize-key-test
  (testing "Case preservation in normalize-key"
    (is (= :message_type (json-to-edn/normalize-key "message_type"))
        "Should preserve snake_case")
    (is (= :messageType (json-to-edn/normalize-key "messageType"))
        "Should preserve camelCase")
    (is (= :TYPE_STRING (json-to-edn/normalize-key "TYPE_STRING"))
        "Should preserve UPPER_SNAKE_CASE")))