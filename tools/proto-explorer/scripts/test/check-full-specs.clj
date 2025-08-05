(require '[proto-explorer.json-to-edn :as json-edn])
(require '[proto-explorer.spec-generator :as spec-gen])
(require '[clojure.pprint :as pp])

;; Load and generate
(def descriptor-set (json-edn/load-json-descriptor "output/json-descriptors/jon_shared_cmd.json"))
(def specs (spec-gen/generate-specs descriptor-set))

(println "Generated packages:" (keys specs))
(println "\nCmd package specs:")
(def cmd-specs (get specs "cmd"))
(println "Keys:" (keys cmd-specs))

;; The issue might be case sensitivity
(println "\nChecking case variations:")
(println ":root ->" (:root cmd-specs))
(println ":Root ->" (:Root cmd-specs))

;; Print first few fields of Root spec
(when-let [root-spec (:Root cmd-specs)]
  (println "\nRoot spec (first 5 fields):")
  (doseq [[k v] (take 6 (drop 1 root-spec))]
    (println "  " k "->" (pr-str v))))