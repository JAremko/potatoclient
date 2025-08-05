(>defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  [map? => any?]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))