(ns proto-explorer.cli-final
  "Final simplified CLI - 2-step process with descriptor-set.json"
  (:require [proto-explorer.simple-api :as api]
            [clojure.pprint :as pp]))

(defn search
  "Fuzzy search for protobuf messages"
  [args]
  (if-let [query (first args)]
    (let [limit (if-let [l (second args)]
                  (try (Integer/parseInt l) (catch Exception _ 10))
                  10)
          results (api/search-messages query limit)]
      (println (api/format-search-results results)))
    (println "Usage: proto-explorer search <query> [limit]")))

(defn list-messages
  "List all protobuf messages, optionally filtered by package"
  [args]
  (let [package-filter (first args)
        results (if package-filter
                  (api/list-all-messages package-filter)
                  (api/list-all-messages))]
    (println (api/format-list-results results))))

(defn info
  "Get comprehensive information about a protobuf message by Java class name"
  [args]
  (if-let [java-class (first args)]
    (try
      (let [info (api/get-message-info java-class)]
        (println (api/format-message-info info)))
      (catch Exception e
        (println "Error:" (.getMessage e))))
    (println "Usage: proto-explorer info <java-class-name>")))

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
    "list" (list-messages args)
    "info" (info args)
    "info-edn" (info-edn args)
    (println (str "Unknown command: " command "\n"
                 "Available commands:\n"
                 "  search <query> [limit]  - Fuzzy search for protobuf messages\n"
                 "  list [package-prefix]   - List all messages (optionally filtered)\n"
                 "  info <java-class>       - Get info by Java class name\n"
                 "  info-edn <java-class>   - Get info as EDN"))))