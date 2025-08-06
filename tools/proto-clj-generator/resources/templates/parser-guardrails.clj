(>defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  [#(instance? JAVA-CLASS %) => SPEC-NAME]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))