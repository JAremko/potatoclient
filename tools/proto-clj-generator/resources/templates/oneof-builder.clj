(defn BUILD-FN-NAME-PAYLOAD
  "Set the oneof payload field."
  [builder [field-key field-value]]
  (case field-key
    CASE-CLAUSES
    (throw (ex-info "Unknown payload field" {:field field-key}))))