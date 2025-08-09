(require '[clojure.java.io :as io]
         '[clojure.pprint :as pp])

(require 'proto-explorer.json-to-edn)

(def descriptor-path "../../examples/protogen/output/json-descriptors/descriptor-set.json")

(defn find-message [descriptor package-name message-name]
  (some (fn [file]
          (when (= (:package file) package-name)
            (some #(when (= (:name %) message-name) %)
                  (:message-type file))))
        (:file descriptor)))

(let [descriptor (proto-explorer.json-to-edn/load-json-descriptor descriptor-path)
      msg (find-message descriptor "ser" "JonGuiDataGps")]
  (println "Message found:" (:name msg))
  (println "\nFields with options:")
  (doseq [field (:field msg)]
    (when-let [options (:options field)]
      (println (str "\nField: " (:name field) " (type: " (:type field) ")"))
      (pp/pprint options))))