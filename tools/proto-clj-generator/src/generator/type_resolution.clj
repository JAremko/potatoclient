(ns generator.type-resolution
  "Utilities for resolving type references across namespaces."
  (:require [clojure.string :as str]
            [potatoclient.proto.conversion :as conv]))

(defn type-ref->package
  "Extract package from a type reference.
  e.g. '.ser.JonGuiDataRotaryMode' -> 'ser'"
  [type-ref]
  (when type-ref
    (let [clean-ref (str/replace type-ref #"^\." "")
          parts (str/split clean-ref #"\.")
          ;; Last part is the type name, everything before is the package
          package-parts (butlast parts)]
      (when (seq package-parts)
        (str/join "." package-parts)))))

(defn resolve-enum-reference
  "Resolve an enum reference to either a local or qualified name.
  Returns {:name 'enum-name' :qualified? true/false :ns-alias 'alias'}"
  [enum-type-ref current-package type-lookup]
  (let [enum-package (type-ref->package enum-type-ref)
        enum-info (get type-lookup enum-type-ref)
        enum-name (if-let [enum-def (or (:definition enum-info) enum-info)]
                   (str (name (:name enum-def)) "-values")
                   ;; Fallback: extract from type ref
                   (str (-> enum-type-ref
                           (str/replace #"^\." "")
                           (str/split #"\.")
                           last
                           conv/string->keyword
                           name)
                       "-values"))]
    (if (or (nil? enum-package) 
            (= enum-package current-package))
      ;; Same package, no qualification needed
      {:name enum-name
       :qualified? false}
      ;; Different package, needs qualification
      ;; For "ser" package, we need special handling since all types are in ser.clj
      {:name enum-name
       :qualified? true
       :ns-alias (cond
                   ;; ser.* types are all in ser.types namespace
                   (str/starts-with? enum-package "ser")
                   "types"  ;; matches the :as types in require
                   
                   ;; For other packages, use last segment
                   :else
                   (last (str/split enum-package #"\\.")))})))

(defn resolve-enum-keyword-map
  "Resolve an enum keyword map reference.
  Similar to resolve-enum-reference but for the -keywords map."
  [enum-type-ref current-package type-lookup]
  (let [result (resolve-enum-reference enum-type-ref current-package type-lookup)]
    (update result :name str/replace #"-values$" "-keywords")))

(defn qualified-enum-ref
  "Generate a qualified or unqualified enum reference string."
  [{:keys [name qualified? ns-alias]}]
  (if qualified?
    (str ns-alias "/" name)
    name))