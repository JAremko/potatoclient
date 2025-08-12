(require '[proto-explorer.json-to-edn :as json-edn])
;; Note: spec-generator module doesn't exist anymore
;; (require '[proto-explorer.spec-generator :as spec-gen])
(require '[clojure.pprint :as pp])

;; Load a simple command file
(println "Loading jon_shared_cmd.json...")
(def descriptor-set (json-edn/load-json-descriptor "output/json-descriptors/jon_shared_cmd.json"))
(def cmd-descriptor (first (:file descriptor-set)))

;; Check the structure
(println "\nFile structure:")
(println "- name:" (:name cmd-descriptor))
(println "- package:" (:package cmd-descriptor)) 
(println "- dependencies:" (:dependency cmd-descriptor))
(println "- message-type count:" (count (:messageType cmd-descriptor)))

;; Look at the first message
(when-let [first-msg (first (:messageType cmd-descriptor))]
  (println "\nFirst message:")
  (println "- name:" (:name first-msg))
  (println "- field count:" (count (:field first-msg)))
  (println "- oneof count:" (count (:oneofDecl first-msg))))

;; Test spec generation - SKIPPED (spec-generator module doesn't exist)
;; (println "\nGenerating specs...")
;; (def specs (spec-gen/generate-specs descriptor-set))

(println "\nDone!")