(defn BUILD-FN-NAME
  "Build a PROTO-NAME protobuf message from a map."
  [m]
  (let [builder (JAVA-CLASS/newBuilder)]
    REGULAR-FIELDS
    ONEOF-PAYLOAD
    (.build builder)))