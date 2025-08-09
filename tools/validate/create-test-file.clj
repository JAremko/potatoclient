(require '[validate.test-harness :as h])
(require '[clojure.java.io :as io])

(let [state-data (h/valid-state-bytes)
      cmd-data (h/valid-command-ping-bytes)]
  (io/copy state-data (io/file "test-state.bin"))
  (io/copy cmd-data (io/file "test-cmd.bin"))
  (println "Created test-state.bin with" (count state-data) "bytes")
  (println "Created test-cmd.bin with" (count cmd-data) "bytes"))

(System/exit 0)