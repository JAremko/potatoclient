(ns regenerate-edn
  (:require [clojure.java.io :as io]
            [state-explorer.proto-to-edn :as p2e]
            [clojure.pprint :as pprint])
  (:import [ser JonSharedData$JonGUIState]))

(defn regenerate-edn-file [bin-file]
  (let [edn-file (clojure.string/replace bin-file #"\.bin$" ".edn")]
    (println "Processing" bin-file "->>" edn-file)
    (try
      ;; Read the binary file
      (let [binary-data (with-open [is (io/input-stream bin-file)]
                          (let [baos (java.io.ByteArrayOutputStream.)]
                            (io/copy is baos)
                            (.toByteArray baos)))
            ;; Parse as protobuf
            proto-msg (JonSharedData$JonGUIState/parseFrom binary-data)
            ;; Convert to EDN with snake_case
            edn-data (p2e/proto-message->map proto-msg)]
        
        ;; Write to EDN file
        (spit edn-file (with-out-str (pprint/pprint edn-data)))
        (println "  ✓ Generated" edn-file))
      (catch Exception e
        (println "  ✗ Failed:" (.getMessage e))))))

;; Regenerate all EDN files in output directory
(doseq [file (file-seq (io/file "output"))]
  (when (and (.isFile file)
             (.endsWith (.getName file) ".bin"))
    (regenerate-edn-file (.getPath file))))