(>defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  [any? => map?]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))