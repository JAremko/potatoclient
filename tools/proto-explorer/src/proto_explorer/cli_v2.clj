(ns proto-explorer.cli-v2
  "CLI v2 - Clean 2-step interface using descriptor-based search"
  (:require [proto-explorer.unified-api-v2 :as api]
            [clojure.string :as str]
            [clojure.pprint :as pp]))

(defn search
  "Fuzzy search for protobuf messages - returns descriptor names and Java classes"
  [args]
  (if-let [query (first args)]
    (let [results (api/fuzzy-search query)]
      (println (api/format-search-results results)))
    (println "Usage: proto-explorer search <query>")))

(defn info
  "Get comprehensive information about a protobuf message.
   Can use either descriptor name (e.g., 'cmd.Root') or Java class (e.g., 'cmd.JonSharedCmd$Root')"
  [args]
  (if-let [identifier (first args)]
    (let [;; Determine if this is a Java class name or descriptor name
          is-java-class? (and (str/includes? identifier "$")
                             (re-matches #"^(cmd|ser)\..+" identifier))
          info (if is-java-class?
                 (api/get-info-by-java-class identifier)
                 (api/get-info-by-descriptor identifier))]
      (if (:error info)
        (println "Error:" (:error info))
        (println (api/format-info info))))
    (println "Usage: proto-explorer info <descriptor-name-or-java-class>")))

(defn info-edn
  "Get comprehensive information as EDN (for programmatic use)"
  [args]
  (if-let [identifier (first args)]
    (let [is-java-class? (and (str/includes? identifier "$")
                             (re-matches #"^(cmd|ser)\..+" identifier))
          info (if is-java-class?
                 (api/get-info-by-java-class identifier)
                 (api/get-info-by-descriptor identifier))]
      (pp/pprint info))
    (pp/pprint {:error "Usage: proto-explorer info-edn <descriptor-name-or-java-class>"})))

(defn dispatch-command
  "Dispatch to appropriate command handler"
  [command args]
  (case command
    "search" (search args)
    "info" (info args)
    "info-edn" (info-edn args)
    (println (str "Unknown command: " command "\n"
                 "Available commands:\n"
                 "  search <query>         - Fuzzy search for protobuf messages\n"
                 "  info <identifier>      - Get info (use descriptor name or Java class)\n"
                 "  info-edn <identifier>  - Get info as EDN"))))