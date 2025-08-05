(defn PARSE-FN-NAME-PAYLOAD
  "Parse the oneof payload from a PROTO-NAME."
  [^JAVA-CLASS proto]
  (case (.GET-ONEOF-CASE proto)
    CASE-CLAUSES
    ;; Default case - no payload set
    {}))