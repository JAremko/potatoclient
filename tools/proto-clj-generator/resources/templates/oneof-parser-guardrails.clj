(>defn- ONEOF-PARSE-FN-NAME
  "Parse the oneof payload from PROTO-NAME."
  [^JAVA-CLASS proto]
  [any? => (? map?)]
  (cond
    ONEOF-CASES))