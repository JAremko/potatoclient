(ns proto-explorer.cli-final
  "Streamlined 2-step CLI for proto exploration"
  (:require [proto-explorer.simple-api :as api]
            [clojure.pprint :as pp]
            [clojure.string :as str]))

(defn search
  "Step 1: Search for protobuf messages by name or Java class.
   Returns list with ready-to-use query strings for step 2."
  [args]
  (if-let [query (first args)]
    (let [limit (if-let [l (second args)]
                  (try (Integer/parseInt l) (catch Exception _ 10))
                  10)
          results (api/search query limit)]
      (println (api/format-search-results results)))
    (println "Usage: proto-explorer search <query> [limit]")))

(defn search-java
  "Search specifically by Java class name"
  [args]
  (if-let [query (first args)]
    (let [limit (if-let [l (second args)]
                  (try (Integer/parseInt l) (catch Exception _ 10))
                  10)
          results (api/search-by-java-class query limit)]
      (println (api/format-search-results results)))
    (println "Usage: proto-explorer search-java <query> [limit]")))

(defn list-messages
  "Step 1 Alternative: List all protobuf messages, optionally filtered by package"
  [args]
  (let [package-filter (first args)
        results (if package-filter
                  (api/list-all-messages package-filter)
                  (api/list-all-messages))]
    (println (api/format-list-results results))))

(defn info
  "Step 2: Get comprehensive information about a protobuf message.
   Use the query string from search results."
  [args]
  (if-let [query (first args)]
    (try
      ;; Handle both full Java class names and simple message names
      (let [java-class (if (str/includes? query "$")
                         query  ; Already a full Java class name
                         ;; Try to find by message name
                         (let [search-results (api/search query 1)
                               matches (:matches search-results)]
                           (if (seq matches)
                             (:java-class (first matches))
                             query)))  ; Fall back to original query
            info (api/get-message-info java-class)]
        (println (api/format-message-info info)))
      (catch Exception e
        (println "Error:" (.getMessage e))
        (println "\nTip: Use the exact query string from search results.")))
    (println "Usage: proto-explorer info <query-from-search>")))

(defn info-edn
  "Get comprehensive information as EDN (for programmatic use)"
  [args]
  (if-let [java-class (first args)]
    (try
      (pp/pprint (api/get-message-info java-class))
      (catch Exception e
        (pp/pprint {:error (.getMessage e)})))
    (pp/pprint {:error "Usage: proto-explorer info-edn <java-class-name>"})))

(defn dispatch-command
  "Dispatch to appropriate command handler"
  [command args]
  (case command
    "search" (search args)
    "search-java" (search-java args)
    "list" (list-messages args)
    "info" (info args)
    "info-edn" (info-edn args)
    ;; Help and default
    ("help" "--help" "-h" nil)
    (println (str "Proto-Explorer - Streamlined 2-Step Workflow\n"
                 "================================================\n\n"
                 "STEP 1: Search or List\n"
                 "  search <query> [limit]       - Search by message name or Java class\n"
                 "  list [package-prefix]        - List all messages\n\n"
                 "STEP 2: Get Details\n"
                 "  info <query-from-step1>      - Get comprehensive message info\n\n"
                 "Alternative commands:\n"
                 "  search-java <query> [limit]  - Search only Java class names\n"
                 "  info-edn <query>             - Get info as EDN (for scripting)\n\n"
                 "Examples:\n"
                 "  proto-explorer search root\n"
                 "  proto-explorer info 'cmd.JonSharedCmd$Root'"))))