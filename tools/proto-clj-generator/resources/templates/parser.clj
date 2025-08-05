(defn PARSE-FN-NAME
  "Parse a PROTO-NAME protobuf message to a map."
  [^JAVA-CLASS proto]
  (cond-> {}
    REGULAR-FIELDS
    ONEOF-PAYLOAD))