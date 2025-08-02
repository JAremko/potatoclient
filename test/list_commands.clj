(ns list-commands
  (:import [potatoclient.transit ActionRegistry]))

(def all-commands (sort (ActionRegistry/getAllActionNames)))
(println "Total:" (count all-commands))
(println "\nAll commands:")
(doseq [cmd all-commands]
  (println cmd))