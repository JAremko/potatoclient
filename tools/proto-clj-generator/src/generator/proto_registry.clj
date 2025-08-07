(ns generator.proto-registry
  "Centralized registry for proto file metadata and namespace resolution.
  Provides a metadata-driven approach to avoid hardcoded patterns."
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [potatoclient.proto.conversion :as conv]
            [generator.naming-config :as naming]))

(defonce ^:private registry (atom {}))

(defn- extract-file-metadata
  "Extract metadata from a JSON descriptor file entry."
  [file-entry]
  {:filename (:name file-entry)
   :package (:package file-entry)
   :dependencies (vec (:dependency file-entry []))
   :message-types (mapv :name (:messageType file-entry []))
   :enum-types (mapv :name (:enumType file-entry []))})

(defn- build-type-index
  "Build an index of all types (messages and enums) with their file metadata."
  [file-metadata]
  (let [filename (:filename file-metadata)
        package (:package file-metadata)]
    (concat
     ;; Index message types
     (for [msg-type (:message-types file-metadata)]
       {:type :message
        :name msg-type
        :qualified-name (str "." package "." msg-type)
        :filename filename
        :package package})
     ;; Index enum types
     (for [enum-type (:enum-types file-metadata)]
       {:type :enum
        :name enum-type
        :qualified-name (str "." package "." enum-type)
        :filename filename
        :package package}))))

(defn load-descriptors
  "Load all JSON descriptors from a directory and build the registry."
  [descriptor-dir]
  (let [files (->> (io/file descriptor-dir)
                   file-seq
                   (filter #(.endsWith (.getName %) ".json"))
                   (remove #(= (.getName %) "descriptor-set.json")))]
    (doseq [file files]
      (let [content (json/parse-string (slurp file) true)
            file-entries (get content :file [])]
        (doseq [file-entry file-entries]
          (let [metadata (extract-file-metadata file-entry)
                types (build-type-index metadata)]
            ;; Store file metadata
            (swap! registry assoc-in [:files (:filename metadata)] metadata)
            ;; Store type index
            (doseq [type-info types]
              (swap! registry assoc-in [:types (:qualified-name type-info)] type-info))))))))

(defn get-naming-config
  "Get the naming convention configuration."
  []
  (or (:naming-config @registry)
      naming/default-config))

(defn set-naming-config
  "Set custom naming convention configuration."
  [config]
  (swap! registry assoc :naming-config config))

(defn package->namespace
  "Convert a protobuf package to a Clojure namespace using configured rules."
  [package]
  (let [config (get-naming-config)
        suffix (naming/package->namespace-suffix config package)]
    (str (:namespace-prefix config) "." suffix)))

(defn filename->namespace
  "Convert a proto filename to a namespace.
  Uses the file's package from the registry instead of hardcoded patterns."
  [filename]
  (if-let [metadata (get-in @registry [:files filename])]
    (package->namespace (:package metadata))
    ;; Fallback for files not in registry
    (let [package (-> filename
                     (str/replace #"\.proto$" "")
                     (str/replace #"^jon_shared_" "")
                     (str/replace #"_" "."))]
      (package->namespace package))))

(defn get-type-info
  "Get information about a type from the registry."
  [qualified-type-name]
  (get-in @registry [:types qualified-type-name]))

(defn get-file-metadata
  "Get metadata for a proto file."
  [filename]
  (get-in @registry [:files filename]))

(defn resolve-type-namespace
  "Resolve the namespace for a given type reference.
  Returns the full Clojure namespace where the type is defined."
  [type-ref]
  (when-let [type-info (get-type-info type-ref)]
    (filename->namespace (:filename type-info))))

(defn build-ns-alias-map
  "Build a map of namespaces to their aliases based on dependencies.
  Uses configured alias rules instead of hardcoded patterns."
  [current-file]
  (when-let [metadata (get-file-metadata current-file)]
    (let [current-ns (filename->namespace current-file)
          deps (:dependencies metadata)
          config (get-naming-config)]
      (into {}
            (for [dep deps
                  :let [dep-metadata (get-file-metadata dep)
                        dep-ns (filename->namespace dep)]
                  :when (and dep-metadata (not= dep-ns current-ns))]
              [dep-ns (naming/filename->alias config dep)])))))

(defn resolve-enum-with-metadata
  "Resolve an enum reference using registry metadata.
  This replaces the hardcoded enum resolution logic."
  [enum-type-ref current-file]
  (let [enum-info (get-type-info enum-type-ref)
        current-metadata (get-file-metadata current-file)
        config (get-naming-config)
        base-name (if enum-info
                   (conv/->kebab-case (:name enum-info))
                   (-> enum-type-ref
                       (str/replace #"^\." "")
                       (str/split #"\.")
                       last
                       conv/->kebab-case))
        enum-name (str base-name (naming/get-type-suffix config :enum-values-suffix))]
    (if (and enum-info current-metadata)
      (let [enum-ns (resolve-type-namespace enum-type-ref)
            current-ns (filename->namespace current-file)
            ns-alias-map (build-ns-alias-map current-file)]
        (if (= enum-ns current-ns)
          {:name enum-name
           :qualified? false}
          {:name enum-name
           :qualified? true
           :ns-alias (get ns-alias-map enum-ns (last (str/split enum-ns #"\.")))}))
      ;; Fallback
      {:name enum-name
       :qualified? false})))

(defn clear-registry
  "Clear the registry (mainly for testing)."
  []
  (reset! registry {}))

(defn get-all-files
  "Get all registered proto files."
  []
  (keys (:files @registry)))

(defn get-all-types
  "Get all registered types."
  []
  (vals (:types @registry)))