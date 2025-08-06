(ns generator.main
  "CLI entry point for the proto-clj-generator."
  (:require [generator.core :as core]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]))

(defn validate-config
  "Validate the configuration map."
  [{:keys [input-dir output-dir namespace-prefix] :as config}]
  (cond
    (not input-dir)
    (throw (ex-info "Missing required parameter: input-dir" config))
    
    (not output-dir)
    (throw (ex-info "Missing required parameter: output-dir" config))
    
    (not namespace-prefix)
    (throw (ex-info "Missing required parameter: namespace-prefix" config))
    
    (not (.exists (io/file input-dir)))
    (throw (ex-info "Input directory does not exist"
                    {:input-dir input-dir}))
    
    :else config))

(defn generate
  "Main generation function called by deps.edn alias.
  
  Expected config:
  {:input-dir \"path/to/json-descriptors\"
   :output-dir \"path/to/output\"
   :namespace-prefix \"potatoclient.proto\"}"
  [config]
  (log/info "Proto-CLJ Generator starting...")
  (try
    (let [validated-config (validate-config config)
          result (core/generate-all validated-config)]
      (log/info "Generation completed successfully!")
      (when (:files result)
        (log/info "Generated files:")
        (doseq [f (:files result)]
          (log/info "  " f)))
      result)
    (catch Exception e
      (log/error e "Generation failed!")
      (throw e))))

(defn -main
  "Command-line entry point."
  [& args]
  (cond
    (and (>= (count args) 3) (<= (count args) 5))
    (let [[input-dir output-dir ns-prefix & opts] args
          namespace-split? (some #{"--namespace-split"} opts)
          guardrails? (some #{"--guardrails"} opts)
          config {:input-dir input-dir
                  :output-dir output-dir
                  :namespace-prefix ns-prefix
                  :namespace-split? namespace-split?
                  :guardrails? guardrails?}]
      (generate config)
      (System/exit 0))
    
    :else
    (do
      (println "Usage: clojure -M:gen <input-dir> <output-dir> <namespace-prefix> [--namespace-split] [--guardrails]")
      (println "Example: clojure -M:gen ../../tools/proto-explorer/output/json-descriptors generated potatoclient.proto")
      (println "         clojure -M:gen ../../tools/proto-explorer/output/json-descriptors generated-ns potatoclient.proto --namespace-split --guardrails")
      (System/exit 1))))