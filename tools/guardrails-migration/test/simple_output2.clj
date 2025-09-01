(ns sample.file "Sample file for testing migration" (:require [clojure.string :as str] [malli.core :as m]))

(defn extract-domain "Extract domain from URL" #:malli{:schema [:=> [:cat :string] :string]} [url] (let [cleaned (str/trim url)] (if (str/includes? cleaned "://") (second (str/split cleaned #"://")) cleaned)))

(defn- private-helper #:malli{:schema [:=> [:cat :int :int] :int]} [x y] (+ x y))

(defn multi-arity #:malli{:schema [:function [:=> [:cat :string] :string] [:=> [:cat :string :string] :string]]} ([x] (str/upper-case x)) ([x y] (str x " " y)))

(defn with-maybe #:malli{:schema [:=> [:cat :string [:maybe :int]] :string]} [required optional] (if optional (str required "-" optional) required))

(defn regular-function "This should stay unchanged" [x] (println x))