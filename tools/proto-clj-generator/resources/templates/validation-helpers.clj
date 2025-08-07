;; Validation Helper Functions
;; =============================================================================

{{#fields-with-constraints}}
(defn valid-{{field-name}}?
  "Validate {{field-name}} against its constraints{{#constraint-description}}: {{constraint-description}}{{/constraint-description}}"
  [value]
  (m/validate {{field-spec}} value))

{{/fields-with-constraints}}