(require '[proto-explorer.json-to-edn :as json-edn])
(require '[proto-explorer.spec-generator :as spec-gen])
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
(println "- message-type count:" (count (:message-type cmd-descriptor)))

;; Look at the first message
(when-let [first-msg (first (:message-type cmd-descriptor))]
  (println "\nFirst message:")
  (println "- name:" (:name first-msg))
  (println "- field count:" (count (:field first-msg)))
  (println "- oneof count:" (count (:oneof-decl first-msg))))

;; Test spec generation
(println "\nGenerating specs...")
(def specs (spec-gen/generate-specs descriptor-set))

(println "\nGenerated packages:" (keys specs))

;; Look at cmd specs
(when-let [cmd-specs (get specs "cmd")]
  (println "\nCmd message specs:" (keys cmd-specs))
  
  ;; Print the Root spec
  (when-let [root-spec (:root cmd-specs)]
    (println "\nRoot spec structure (truncated):")
    (let [spec-str (with-out-str (pp/pprint root-spec))
          lines (clojure.string/split-lines spec-str)]
      (doseq [line (take 50 lines)]
        (println line)))))

(println "\nDone!")