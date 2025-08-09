(require '[clojure.pprint :as pp])
(require 'proto-explorer.json-to-edn)

;; Load test descriptor with validation
(def test-descriptor (proto-explorer.json-to-edn/load-json-descriptor "test-descriptor-with-validation.json"))

;; Get the test message
(def test-msg (get-in test-descriptor [:file 0 :message-type 0]))

(println "Test message fields with validation:")
(println "====================================")
(doseq [field (:field test-msg)]
  (println "\nField:" (:name field) "(" (:type field) ")")
  (when-let [validation (get-in field [:options (keyword "[buf.validate.field]")])]
    (println "  Validation constraints:")
    (pp/pprint validation)))

;; Test the extraction logic
(println "\n\nExtracted field details with validation:")
(println "=========================================")
(let [field-details (mapv (fn [field]
                            (let [base-info {:name (:name field)
                                           :number (:number field)
                                           :type (name (:type field))
                                           :json-name (:json-name field)}
                                  buf-validate (get-in field [:options (keyword "[buf.validate.field]")])]
                              (if buf-validate
                                (assoc base-info :validation buf-validate)
                                base-info)))
                          (:field test-msg))]
  (doseq [field field-details]
    (println (str "\n" (:name field) ":"))
    (if (:validation field)
      (do
        (println "  Type:" (:type field))
        (println "  Validation:")
        (pp/pprint (:validation field)))
      (println "  Type:" (:type field) "(no validation)"))))