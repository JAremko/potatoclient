(ns generator.type-resolution
  "Utilities for resolving type references across namespaces."
  (:require [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [generator.proto-registry :as registry]))

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
        enum-info (get type-lookup (keyword enum-type-ref))
        enum-name (if enum-info
                   (str (name (:name enum-info)) "-values")
                   ;; Fallback: extract from type ref
                   (str (-> enum-type-ref
                           (str/replace #"^\." "")
                           (str/split #"\.")
                           last
                           csk/->kebab-case)
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

(defn file->namespace-suffix
  "Convert a proto filename and package to namespace suffix.
  Uses the proto registry for metadata-driven resolution."
  [filename package]
  (let [full-ns (registry/filename->namespace filename)]
    (if full-ns
      ;; Remove the namespace prefix to get just the suffix
      (let [prefix (:namespace-prefix (registry/get-naming-config))]
        (str/replace full-ns (str prefix ".") ""))
      ;; Fallback to package-based if not in registry
      (-> package
         str/lower-case
         (str/replace #"_" "-"))))

(defn resolve-enum-reference-with-aliases
  "Resolve an enum reference using the ns-alias-map to determine correct qualification.
  Returns {:name 'enum-name' :qualified? true/false :ns-alias 'alias'}"
  [enum-type-ref current-package type-lookup ns-alias-map current-filename]
  (let [enum-info (get type-lookup (keyword enum-type-ref))
        ;; First try registry-based resolution
        registry-result (when current-filename
                         (registry/resolve-enum-with-metadata enum-type-ref current-filename))]
    (if registry-result
      registry-result
      ;; Fallback to existing logic
      (let [enum-name (if enum-info
                       (str (name (:name enum-info)) "-values")
                       ;; Fallback: extract from type ref
                       (str (-> enum-type-ref
                               (str/replace #"^\." "")
                               (str/split #"\.")
                               last
                               csk/->kebab-case)
                           "-values"))]
        (if enum-info
          ;; We have the enum info, check if it's in a different namespace
          (let [enum-filename (:filename enum-info)
                enum-package (:package enum-info)
                enum-ns-suffix (file->namespace-suffix enum-filename enum-package)
                enum-full-ns (str "potatoclient.proto." enum-ns-suffix)
                ;; Check if we have an alias for this namespace
                ns-alias (get ns-alias-map enum-full-ns)]
            (if ns-alias
              {:name enum-name
               :qualified? true
               :ns-alias ns-alias}
              ;; No alias means it's in the current namespace
              {:name enum-name
               :qualified? false}))
          ;; Fallback to old logic if no enum info
          (resolve-enum-reference enum-type-ref current-package type-lookup))))))

(defn resolve-enum-keyword-map-with-aliases
  "Resolve an enum keyword map reference using the ns-alias-map.
  Similar to resolve-enum-reference-with-aliases but for the -keywords map."
  [enum-type-ref current-package type-lookup ns-alias-map current-filename]
  (let [result (resolve-enum-reference-with-aliases enum-type-ref current-package type-lookup ns-alias-map current-filename)]
    (update result :name str/replace #"-values$" "-keywords")))