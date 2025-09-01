(ns sample.file
  "Sample file for testing migration"
  (:require [clojure.string :as str]
            [malli.core :as m]))

(defn extract-domain
  "Extract domain from URL"
  [url]
  (let [cleaned (str/trim url)]
    (if (str/includes? cleaned "://")
      (second (str/split cleaned #"://"))
      cleaned)))

(defn- private-helper
  [x y]
  (+ x y))

(defn multi-arity
  ([x]
   [string? => string?]
   (str/upper-case x))
  ([x y]
   [string? string? => string?]
   (str x " " y)))

(defn with-maybe
  [required optional]
  (if optional
    (str required "-" optional)
    required))

(defn regular-function
  "This should stay unchanged"
  [x]
  (println x))