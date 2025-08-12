(ns fix-enum-case
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn fix-enum-case [line]
  ;; Fix all enum values to uppercase
  (-> line
      ;; Fix any :jon_gui_data_* patterns to uppercase
      (str/replace #":jon_gui_data_([a-z_]+)" 
                   (fn [[_ match]]
                     (str ":JON_GUI_DATA_" (str/upper-case match))))
      ;; Fix :jon_gui_* patterns (non-data)
      (str/replace #":jon_gui_([a-z_]+)" 
                   (fn [[_ match]]
                     (str ":JON_GUI_" (str/upper-case match))))))

(defn fix-file [filepath]
  (let [content (slurp filepath)
        lines (str/split-lines content)
        fixed-lines (map fix-enum-case lines)
        fixed-content (str/join "\n" fixed-lines)]
    (spit filepath fixed-content)
    (println "Fixed:" filepath)))

;; Fix all test files
(fix-file "/home/jare/git/potatoclient/tools/validate/test/validate/deep_round_trip_test.clj")
(fix-file "/home/jare/git/potatoclient/tools/validate/test/validate/full_round_trip_test.clj")

(println "Done!")