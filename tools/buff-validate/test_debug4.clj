(import '[ser JonSharedDataTypes$JonGuiDataClientType])

(println "Available client type values:")
(doseq [ct (JonSharedDataTypes$JonGuiDataClientType/values)]
  (println "  " (.name ct) "=" (.getNumber ct)))