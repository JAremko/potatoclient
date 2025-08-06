(ns proto-explorer.cli-bb
  "Babashka-compatible CLI interface for proto-explorer"
  (:require [proto-explorer.json-to-edn :as json-edn]
            [clj-fuzzy.metrics :as fuzzy]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

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

(defn find-messages
  "Find messages by name with fuzzy matching"
  [args]
  (if-let [pattern (first args)]
    (let [json-dir "output/json-descriptors"
          files (file-seq (io/file json-dir))
          json-files (filter #(and (.isFile %) (.endsWith (.getName %) ".json")) files)
          all-messages (atom [])
          pattern-lower (str/lower-case pattern)]
      (doseq [file json-files]
        (try
          (let [data (json-edn/load-json-descriptor (.getPath file))
                files (:file data [])]
            (doseq [file-desc files]
              (when-let [messages (:message-type file-desc)]
                (doseq [msg messages]
                  (let [msg-name (:name msg)
                        msg-name-lower (str/lower-case msg-name)]
                    (when (or (str/includes? msg-name-lower pattern-lower)
                             (< (fuzzy/levenshtein msg-name-lower pattern-lower) 5))  ; Allow fuzzy matches
                      (swap! all-messages conj {:name msg-name
                                              :file (.getName file)
                                              :proto-file (:name file-desc)})))))))
          (catch Exception e
            ;; Skip files that fail to parse
            nil)))
      (let [sorted-results (->> @all-messages
                              (sort-by :name)
                              (take 50))]
        (output-data {:found (count sorted-results)
                     :messages sorted-results})))
    (output-data {:error "Usage: bb find <pattern>"})))

(defn list-messages
  "List all messages, optionally filtered by package"
  [args]
  (let [package-filter (first args)
        json-dir "output/json-descriptors"
        files (file-seq (io/file json-dir))
        json-files (filter #(and (.isFile %) (.endsWith (.getName %) ".json")) files)
        all-messages (atom [])]
    (doseq [file json-files]
      (when (or (nil? package-filter)
                (str/includes? (.getName file) package-filter))
        (try
          (let [data (json-edn/load-json-descriptor (.getPath file))
                files (:file data [])]
            (doseq [file-desc files]
              (when-let [messages (:message-type file-desc)]
                (doseq [msg messages]
                  (swap! all-messages conj {:name (:name msg)
                                          :file (.getName file)
                                          :proto-file (:name file-desc)})))))
          (catch Exception e
            ;; Skip files that fail to parse
            nil))))
    (output-data {:total (count @all-messages)
                  :messages (sort-by :name @all-messages)})))

(defn java-class-info
  "Get Java class info for a protobuf message (requires JVM)"
  [args]
  (if-let [message-name (first args)]
    (let [result (shell/sh "clojure" "-M:run" "java-class" message-name)]
      (if (= 0 (:exit result))
        (println (:out result))
        (output-data {:error (:err result)})))
    (output-data {:error "Usage: bb java-class <message-name>"})))

(defn java-field-mapping
  "Get proto field to Java method mapping (requires JVM)"
  [args]
  (if-let [message-name (first args)]
    (let [result (shell/sh "clojure" "-M:run" "java-fields" message-name)]
      (if (= 0 (:exit result))
        (println (:out result))
        (output-data {:error (:err result)})))
    (output-data {:error "Usage: bb java-fields <message-name>"})))

(defn java-builder-info
  "Get Java builder info for a protobuf message (requires JVM)"
  [args]
  (if-let [message-name (first args)]
    (let [result (shell/sh "clojure" "-M:run" "java-builder" message-name)]
      (if (= 0 (:exit result))
        (println (:out result))
        (output-data {:error (:err result)})))
    (output-data {:error "Usage: bb java-builder <message-name>"})))

(defn batch-process
  "Process batch queries from stdin"
  [args]
  (doseq [line (line-seq (io/reader *in*))]
    (let [query (json/parse-string line true)]
      (case (:type query)
        "find" (find-messages [(:pattern query)])
        "list" (list-messages (when (:package query) [(:package query)]))
        (output-data {:error (str "Unknown query type: " (:type query))})))))