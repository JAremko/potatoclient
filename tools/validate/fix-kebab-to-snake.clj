(ns fix-kebab-to-snake
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn kebab->snake [s]
  (str/replace s "-" "_"))

(defn fix-keyword [line]
  ;; Fix keywords that use kebab-case
  (str/replace line #":([a-z]+(?:-[a-z]+)+)" 
               (fn [[_ match]]
                 (str ":" (kebab->snake match)))))

(defn fix-enum [line]
  ;; Fix enum values to uppercase
  (str/replace line #":jon-gui-data-([a-z-]+)" 
               (fn [[_ match]]
                 (str ":JON_GUI_DATA_" (str/upper-case (kebab->snake match))))))

(defn fix-line [line]
  (-> line
      fix-keyword
      fix-enum))

(defn fix-file [filepath]
  (let [content (slurp filepath)
        lines (str/split-lines content)
        fixed-lines (map fix-line lines)
        fixed-content (str/join "\n" fixed-lines)]
    (spit filepath fixed-content)
    (println "Fixed:" filepath)))

;; Fix the test files
(fix-file "/home/jare/git/potatoclient/tools/validate/test/validate/deep_round_trip_test.clj")
(fix-file "/home/jare/git/potatoclient/tools/validate/test/validate/full_round_trip_test.clj")
(fix-file "/home/jare/git/potatoclient/tools/validate/test/validate/specs/state_property_test.clj")

(println "Done!")