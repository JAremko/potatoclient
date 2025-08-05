(ns test-utils.diff
  "Utilities for showing nice diffs in tests."
  (:require [clojure.test :refer [is]]
            [lambdaisland.deep-diff2 :as ddiff]))

(defn assert-edn-equal
  "Assert that two EDN values are equal, showing a nice diff if not."
  ([expected actual]
   (assert-edn-equal expected actual nil))
  ([expected actual msg]
   (let [equal? (= expected actual)]
     (if equal?
       (is true (str "EDN values are equal" (when msg (str ": " msg))))
       (do
         ;; First show the basic assertion failure
         (is (= expected actual) msg)
         ;; Then show a nice colored diff
         (println "\n=== EDN Diff ===")
         (println "Expected vs Actual:")
         (ddiff/pretty-print (ddiff/minimize (ddiff/diff expected actual)))
         (println "================\n")
         false)))))

(defn show-diff
  "Show a diff between two values without asserting."
  [label expected actual]
  (println (str "\n=== " label " ==="))
  (if (= expected actual)
    (println "Values are equal")
    (ddiff/pretty-print (ddiff/minimize (ddiff/diff expected actual))))
  (println "================\n"))