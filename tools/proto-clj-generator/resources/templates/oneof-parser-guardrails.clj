(>defn- ONEOF-PARSE-FN-NAME
  "Parse the oneof payload from PROTO-NAME."
  [^JAVA-CLASS proto]
  [#(instance? JAVA-CLASS %) => (? map?)]
  (cond
    ONEOF-CASES))