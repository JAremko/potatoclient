(import '[potatoclient.transit ActionRegistry])
(let [all-commands (sort (ActionRegistry/getAllActionNames))]
  (println "Total commands:" (count all-commands))
  (println "\nAll commands:")
  (doseq [cmd all-commands]
    (println "  " cmd)))