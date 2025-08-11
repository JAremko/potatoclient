(ns regenerate-with-pronto
  (:require [clojure.java.io :as io]
            [pronto.core :as p]
            [clojure.pprint :as pprint])
  (:import [ser JonSharedData$JonGUIState]))

;; Define mapper for state messages  
(p/defmapper state-mapper [ser.JonSharedData$JonGUIState])

(defn regenerate-edn-file [bin-file]
  (let [edn-file (clojure.string/replace bin-file #"\.bin$" ".edn")]
    (println "Processing" bin-file "->>" edn-file)
    (try
      ;; Read the binary file
      (let [binary-data (with-open [is (io/input-stream bin-file)]
                          (let [baos (java.io.ByteArrayOutputStream.)]
                            (io/copy is baos)
                            (.toByteArray baos)))
            ;; Parse as protobuf using Pronto
            proto-map (p/bytes->proto-map state-mapper ser.JonSharedData$JonGUIState binary-data)
            ;; Convert to EDN using Pronto's native format (snake_case)
            edn-data (p/proto-map->clj-map proto-map)]
        
        ;; Write to EDN file
        (spit edn-file (with-out-str (pprint/pprint edn-data)))
        (println "  ✓ Generated" edn-file)
        
        ;; Show a sample of the structure
        (when (= bin-file "output/1754664759800.bin")
          (println "\nSample structure (first few fields):")
          (println "  Keys:" (take 5 (keys edn-data)))
          (when-let [rotary (:rotary edn-data)]
            (when-let [scan-node (:current_scan_node rotary)]
              (println "  Scan node fields:" (keys scan-node))))))
      (catch Exception e
        (println "  ✗ Failed:" (.getMessage e))))))

;; Regenerate all EDN files in output directory
(doseq [file (file-seq (io/file "output"))]
  (when (and (.isFile file)
             (.endsWith (.getName file) ".bin"))
    (regenerate-edn-file (.getPath file))))