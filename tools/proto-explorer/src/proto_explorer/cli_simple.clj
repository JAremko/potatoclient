(ns proto-explorer.cli-simple
  "Simplified CLI for proto-explorer with fuzzy search and unified info"
  (:require [proto-explorer.unified-api :as api]
            [clojure.pprint :as pp]))

(defn search
  "Fuzzy search for protobuf message classes"
  [args]
  (if-let [query (first args)]
    (let [results (api/fuzzy-search-classes query)]
      (println (api/format-search-results results)))
    (println "Usage: proto-explorer search <query>")))

(defn info
  "Get comprehensive information about a protobuf message class"
  [args]
  (if-let [class-name (first args)]
    (let [class-info (api/get-complete-class-info class-name)]
      (println (api/format-class-info class-info)))
    (println "Usage: proto-explorer info <full-class-name>")))

(defn info-edn
  "Get comprehensive information as EDN (for programmatic use)"
  [args]
  (if-let [class-name (first args)]
    (let [class-info (api/get-complete-class-info class-name)]
      (pp/pprint class-info))
    (pp/pprint {:error "Usage: proto-explorer info-edn <full-class-name>"})))

(defn dispatch-command
  "Dispatch to appropriate command handler"
  [command args]
  (case command
    "search" (search args)
    "info" (info args)
    "info-edn" (info-edn args)
    (println (str "Unknown command: " command "\n"
                 "Available commands:\n"
                 "  search <query>        - Fuzzy search for protobuf messages\n"
                 "  info <class-name>     - Get comprehensive info about a class\n"
                 "  info-edn <class-name> - Get info as EDN"))))