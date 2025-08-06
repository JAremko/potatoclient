(>defn- ONEOF-BUILD-FN-NAME
  "Build the oneof payload for PROTO-NAME."
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
  (case field-key
    ONEOF-CASES
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof "ONEOF-NAME"}))))