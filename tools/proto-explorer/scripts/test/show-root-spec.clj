(require '[proto-explorer.json-to-edn :as json-edn])
(require '[proto-explorer.spec-generator :as spec-gen])
(require '[clojure.pprint :as pp])

;; Load and generate
(def descriptor-set (json-edn/load-json-descriptor "output/json-descriptors/jon_shared_cmd.json"))
(def specs (spec-gen/generate-specs descriptor-set))
(def cmd-specs (get specs "cmd"))
(def root-spec (:root cmd-specs))

(println "Root spec:")
(pp/pprint (take 10 (drop 1 root-spec))) ; Skip :map and show first 10 fields