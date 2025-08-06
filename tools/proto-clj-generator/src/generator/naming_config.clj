(ns generator.naming-config
  "Configuration for naming conventions and transformations.
  This provides a flexible, data-driven approach to namespace generation."
  (:require [clojure.string :as str]
            [camel-snake-kebab.core :as csk]))

(def default-config
  "Default naming convention configuration.
  Can be overridden via configuration file or environment."
  {:namespace-prefix "potatoclient.proto"
   
   ;; Rules for transforming packages to namespaces
   :package-rules
   [{:name "Command packages"
     :pattern #"^cmd\.(.+)$"
     :transform (fn [match]
                 (str "cmd." (csk/->kebab-case (second match))))}
    
    {:name "Serialization root package"
     :pattern #"^ser$"
     :transform (constantly "ser.types")}
    
    {:name "Serialization sub-packages"
     :pattern #"^ser\.(.+)$"
     :transform (fn [match]
                 (str "ser." (csk/->kebab-case (second match))))}
    
    {:name "Default transformation"
     :pattern #"(.+)"
     :transform (fn [match]
                 (-> (second match)
                     (str/replace #"\." "-")
                     str/lower-case))}]
   
   ;; Rules for generating namespace aliases
   :alias-rules
   [{:name "Remove common prefixes"
     :pattern #"^jon_shared_(.+)\.proto$"
     :transform (fn [match]
                 (-> (second match)
                     (str/replace #"^(cmd_|data_)" "")
                     csk/->kebab-case
                     keyword))}
    
    {:name "Default alias from filename"
     :pattern #"^(.+)\.proto$"
     :transform (fn [match]
                 (-> (second match)
                     csk/->kebab-case
                     keyword))}]
   
   ;; Type name transformations
   :type-transforms
   {:enum-values-suffix "-values"
    :enum-keywords-suffix "-keywords"
    :builder-prefix "build-"
    :parser-prefix "parse-"}})

(defn apply-rules
  "Apply a list of pattern rules to a value.
  Returns the result of the first matching rule's transform function."
  [rules value]
  (loop [remaining rules]
    (when-let [{:keys [pattern transform]} (first remaining)]
      (if-let [match (re-matches pattern value)]
        (transform match)
        (recur (rest remaining))))))

(defn package->namespace-suffix
  "Transform a package name to a namespace suffix using configured rules."
  ([package] (package->namespace-suffix default-config package))
  ([config package]
   (apply-rules (:package-rules config) package)))

(defn filename->alias
  "Generate a namespace alias from a filename using configured rules."
  ([filename] (filename->alias default-config filename))
  ([config filename]
   (apply-rules (:alias-rules config) filename)))

(defn get-type-suffix
  "Get the suffix for a type transformation."
  ([transform-key] (get-type-suffix default-config transform-key))
  ([config transform-key]
   (get-in config [:type-transforms transform-key])))

(defn load-config
  "Load naming configuration from a file or environment.
  Returns merged configuration with defaults."
  [config-source]
  (cond
    (map? config-source)
    (merge-with merge default-config config-source)
    
    (string? config-source)
    ;; TODO: Load from file
    (merge-with merge default-config (read-string (slurp config-source)))
    
    :else
    default-config))

(defn validate-config
  "Validate a naming configuration for required fields and structure."
  [config]
  (and (map? config)
       (string? (:namespace-prefix config))
       (sequential? (:package-rules config))
       (every? #(and (contains? % :pattern)
                    (contains? % :transform)
                    (instance? java.util.regex.Pattern (:pattern %))
                    (fn? (:transform %)))
               (:package-rules config))))