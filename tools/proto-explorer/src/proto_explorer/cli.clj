(ns proto-explorer.cli
  "Babashka CLI interface for proto-explorer"
  (:require [proto-explorer.generated-specs :as specs]
            [proto-explorer.test-data-generator :as tdg]
            [proto-explorer.java-class-mapper :as mapper]
            [proto-explorer.nested-class-mapper :as nested-mapper]
            [proto-explorer.keyword-tree-mapper :as keyword-mapper]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

;; Initialize specs on namespace load  
(try
  (specs/load-all-specs! "../../shared/specs/protobuf")
  (catch Exception e
    ;; Silent fail - specs will be loaded on demand
    nil))

;; Output format control
(def ^:dynamic *output-format* :json)

(defn- keyword->string [x]
  "Convert keywords to strings for JSON serialization, preserving namespaces"
  (cond
    (keyword? x) (if (namespace x)
                   (str (namespace x) "/" (name x))  ; Preserve namespace without colon
                   (name x))                         ; Simple keyword
    (map? x) (reduce-kv (fn [m k v]
                         (assoc m (keyword->string k) (keyword->string v)))
                       {}
                       x)
    (sequential? x) (mapv keyword->string x)
    :else x))

(defn- output-data
  "Output data in the configured format (JSON or EDN)"
  [data]
  (case *output-format*
    :json (println (json/generate-string (keyword->string data) {:pretty true}))
    :edn (pp/pprint data)
    ;; Default to JSON
    (println (json/generate-string (keyword->string data) {:pretty true}))))

(defn find-specs
  "Find specs by name with fuzzy matching"
  [args]
  (if-let [pattern (first args)]
    (let [matches (specs/find-specs pattern)
          results (mapv (fn [[k v]]
                         {:spec k
                          :namespace (namespace k)
                          :name (name k)})
                       matches)]
      (output-data {:found (count results)
                    :specs results}))
    (output-data {:error "Usage: bb proto-explorer find <pattern>"})))

(defn get-spec
  "Get spec for exact message"
  [args]
  (if-let [spec-name (first args)]
    (let [spec-key (if (str/starts-with? spec-name ":")
                     (edn/read-string spec-name)
                     (keyword spec-name))]
      (if-let [spec (specs/get-spec spec-key)]
        (output-data {:spec spec-key
                     :definition spec})
        (output-data {:error (str "Spec not found: " spec-key)})))
    (output-data {:error "Usage: bb proto-explorer spec <spec-name>"})))

(defn generate-example
  "Generate example data for a spec"
  [args]
  (if-let [spec-name (first args)]
    (let [spec-key (if (str/starts-with? spec-name ":")
                     (edn/read-string spec-name)
                     (keyword spec-name))]
      (if (specs/get-spec spec-key)
        (try
          (let [example (tdg/generate-data spec-key)]
            (output-data {:spec spec-key
                         :example example}))
          (catch Exception e
            (output-data {:error (str "Failed to generate example: " (.getMessage e))})))
        (output-data {:error (str "Spec not found: " spec-key)})))
    (output-data {:error "Usage: bb proto-explorer example <spec-name>"})))

(defn generate-examples
  "Generate multiple examples for a spec"
  [args]
  (let [spec-name (first args)
        n (if (second args)
            (try (Integer/parseInt (second args))
                 (catch Exception _ 3))
            3)]
    (if spec-name
      (let [spec-key (if (str/starts-with? spec-name ":")
                       (edn/read-string spec-name)
                       (keyword spec-name))]
        (if (specs/get-spec spec-key)
          (try
            (let [examples (tdg/generate-examples spec-key n)]
              (output-data {:spec spec-key
                           :count n
                           :examples examples}))
            (catch Exception e
              (output-data {:error (str "Failed to generate examples: " (.getMessage e))})))
          (output-data {:error (str "Spec not found: " spec-key)})))
      (output-data {:error "Usage: bb proto-explorer examples <spec-name> [count]"}))))

(defn list-messages
  "List all messages, optionally filtered by package"
  [args]
  (let [package-filter (first args)
        all-specs (keys @specs/spec-registry)
        filtered (if package-filter
                   (filter #(= package-filter (namespace %)) all-specs)
                   all-specs)
        by-package (group-by namespace filtered)]
    (output-data {:total (count filtered)
                 :packages (keys by-package)
                 :by-package (reduce-kv (fn [acc pkg specs]
                                         (assoc acc pkg (mapv name specs)))
                                       {}
                                       by-package)})))

(defn show-stats
  "Show statistics about loaded specs"
  [_args]
  (let [all-specs @specs/spec-registry
        packages (group-by namespace (keys all-specs))]
    (output-data {:total-specs (count all-specs)
                 :total-packages (count packages)
                 :by-package (reduce-kv (fn [acc pkg specs]
                                         (assoc acc pkg (count specs)))
                                       {}
                                       packages)})))

(defn batch-process
  "Process batch queries from stdin"
  [_args]
  (let [queries (try
                  (edn/read *in*)
                  (catch Exception e
                    (output-data {:error (str "Failed to read EDN from stdin: " (.getMessage e))})
                    (System/exit 1)))]
    (if (sequential? queries)
      (let [results (mapv (fn [query]
                           (case (:op query)
                             :find (let [matches (specs/find-specs (:pattern query))]
                                    {:op :find
                                     :pattern (:pattern query)
                                     :results (mapv first matches)})
                             :spec (let [spec (specs/get-spec (:spec query))]
                                    {:op :spec
                                     :spec (:spec query)
                                     :definition spec})
                             :example (try
                                       {:op :example
                                        :spec (:spec query)
                                        :example (tdg/generate-data (:spec query))}
                                       (catch Exception e
                                         {:op :example
                                          :spec (:spec query)
                                          :error (.getMessage e)}))
                             {:error (str "Unknown operation: " (:op query))}))
                         queries)]
        (output-data {:batch-results results}))
      (output-data {:error "Batch input must be a sequence of queries"}))))

(defn- call-java-via-uberjar
  "Call the proto-explorer uberjar for Java reflection tasks."
  [command class-name]
  (try
    (let [jar-path "target/proto-explorer.jar"
          ;; Check if jar exists
          _ (when-not (.exists (io/file jar-path))
              (throw (ex-info "Uberjar not found. Run 'make uberjar' first." {:jar jar-path})))
          {:keys [out err exit]} (shell/sh "java" "-cp" 
                                               (str jar-path ":../../target/classes")
                                               "proto_explorer.main" command class-name)
          result (str/trim out)]
      (cond
        (not= exit 0)
        {:error (str "Command failed with exit code " exit)
         :stderr err}
        
        (str/blank? result)
        {:error "No output from uberjar"
         :stderr err}
        
        :else
        (let [lines (str/split-lines result)
              ;; Skip "Loading" lines and find the actual output
              edn-lines (remove #(or (str/starts-with? % "Loading")
                                    (str/starts-with? % "Loaded"))
                               lines)
              edn-str (str/join "\n" edn-lines)]
          (if (str/blank? edn-str)
            {:error "No EDN output found"
             :raw-output result}
            (try
              (edn/read-string edn-str)
              (catch Exception e
                ;; If not EDN, return as-is
                {:output edn-str
                 :parse-error (.getMessage e)}))))))
    (catch Exception e
      {:error (str "Failed to call uberjar: " (.getMessage e))})))

(defn java-class-info
  "Get Java class info for a protobuf message (via JVM - SLOW!)"
  [args]
  (if-let [class-name (first args)]
    (let [result (call-java-via-uberjar "java-class" class-name)
          with-warning (assoc result :warning "This command starts a JVM process and may be slow!")]
      (output-data with-warning))
    (output-data {:error "Usage: bb java-class <class-name>"})))

(defn java-field-mapping
  "Get proto field to Java method mapping (via JVM - SLOW!)"
  [args]
  (if-let [class-name (first args)]
    (let [result (call-java-via-uberjar "java-fields" class-name)
          with-warning (assoc result :warning "This command starts a JVM process and may be slow!")]
      (output-data with-warning))
    (output-data {:error "Usage: bb java-fields <class-name>"})))

(defn java-builder-info
  "Get Java builder info for a protobuf message (via JVM - SLOW!)"
  [args]
  (if-let [class-name (first args)]
    (let [result (call-java-via-uberjar "java-builder" class-name)
          with-warning (assoc result :warning "This command starts a JVM process and may be slow!")]
      (output-data with-warning))
    (output-data {:error "Usage: bb java-builder <class-name>"})))

(defn generate-proto-type-mapping
  "Generate proto type mapping file from descriptors"
  [args]
  (let [descriptor-dir (or (first args) "output/json-descriptors")
        output-file (or (second args) "../../shared/specs/protobuf/proto_type_mapping.clj")]
    (try
      (mapper/generate-mapping-file descriptor-dir output-file)
      (output-data {:success true
                   :output-file output-file
                   :message (str "Generated proto type mapping at " output-file)})
      (catch Exception e
        (output-data {:error (str "Failed to generate mapping: " (.getMessage e))})))))

(defn generate-nested-proto-mapping
  "Generate context-aware nested proto type mapping file from descriptors"
  [args]
  (let [descriptor-dir (or (first args) "output/json-descriptors")
        output-file (or (second args) "../../shared/specs/protobuf/proto_type_mapping_nested_data.clj")]
    (try
      (let [result (nested-mapper/generate-nested-mapping-file descriptor-dir output-file)]
        (output-data {:success true
                      :output-file output-file
                      :class-count (:class-count result)
                      :leaf-count (:leaf-count result)
                      :total-mappings (:total-mappings result)
                      :message (str "Generated nested proto mapping with "
                                    (:class-count result) " parent classes and "
                                    (:total-mappings result) " total mappings")}))
      (catch Exception e
        (output-data {:error (str "Failed to generate nested mapping: " (.getMessage e))})))))


(defn generate-keyword-tree-cmd
  "Generate keyword-based proto tree structure for commands"
  [args]
  (let [descriptor-dir (or (first args) "output/json-descriptors")
        output-file (or (second args) "../../shared/specs/protobuf/proto_keyword_tree_cmd.clj")]
    (try
      (let [result (keyword-mapper/generate-keyword-tree-file descriptor-dir output-file :command)]
        (output-data {:success true
                      :proto-type "command"
                      :output-file output-file
                      :root-class (:root-class result)
                      :root-count (:root-count result)
                      :total-nodes (:total-nodes result)
                      :message (str "Generated command keyword tree with "
                                    (:root-count result) " root keywords and "
                                    (:total-nodes result) " total nodes")}))
      (catch Exception e
        (output-data {:error (str "Failed to generate command keyword tree: " (.getMessage e))})))))

(defn generate-keyword-tree-state
  "Generate keyword-based proto tree structure for state"
  [args]
  (let [descriptor-dir (or (first args) "output/json-descriptors")
        output-file (or (second args) "../../shared/specs/protobuf/proto_keyword_tree_state.clj")]
    (try
      (let [result (keyword-mapper/generate-keyword-tree-file descriptor-dir output-file :state)]
        (output-data {:success true
                      :proto-type "state"
                      :output-file output-file
                      :root-class (:root-class result)
                      :root-count (:root-count result)
                      :total-nodes (:total-nodes result)
                      :message (str "Generated state keyword tree with "
                                    (:root-count result) " root keywords and "
                                    (:total-nodes result) " total nodes")}))
      (catch Exception e
        (output-data {:error (str "Failed to generate state keyword tree: " (.getMessage e))})))))