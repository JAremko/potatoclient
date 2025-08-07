(require '[generator.main :as main])

(main/generate {:input-dir "../../tools/proto-explorer/output/json-descriptors"
                :output-dir "test-constraint-output"
                :namespace-prefix "potatoclient.proto"
                :guardrails? true})