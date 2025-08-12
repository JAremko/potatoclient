#!/usr/bin/env clojure

(ns test-cmd-root-proper
  "Test proper structure for cmd/root using oneof_edn's built-in generator"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; Load cmd specs for realistic test
(require '[potatoclient.specs.cmd.common])

(println "\n=== CMD/Root Structure Options ===\n")

;; OPTION 1: Nested structure (cleanest, but changes message format)
(println "OPTION 1: Nested command field (CLEANEST)")
(println "-------------------------------------------")
(let [nested-spec [:map {:closed true}
                  [:protocol_version [:int {:min 1}]]
                  [:client_type [:enum :ground :web]]
                  [:session_id {:optional true} :int]
                  ;; Commands wrapped in a :command field
                  [:command [:oneof_edn
                            [:ping [:map {:closed true} [:id :int]]]
                            [:echo [:map {:closed true} [:msg :string]]]
                            [:noop [:map {:closed true}]]]]]
  
  (println "\nStructure: Commands are under :command field")
  (println "Example: {:protocol_version 1 :client_type :ground :command {:ping {:id 123}}}")
  
  (println "\nValidation:")
  (let [valid {:protocol_version 1 :client_type :ground :command {:ping {:id 456}}}
        invalid-empty {:protocol_version 1 :client_type :ground}
        invalid-multi {:protocol_version 1 :client_type :ground 
                      :command {:ping {:id 1} :echo {:msg "x"}}}]
    (println "  Valid:" valid "=>" (m/validate nested-spec valid))
    (println "  No command:" invalid-empty "=>" (m/validate nested-spec invalid-empty))
    (println "  Multi command:" invalid-multi "=>" (m/validate nested-spec invalid-multi)))
  
  (println "\nGeneration (automatic, no custom generator needed!):")
  (dotimes [i 5]
    (let [gen (mg/generate nested-spec)
          cmd-type (first (keys (:command gen)))]
      (println (str "  Gen " i ": pv=" (:protocol_version gen) 
                   " type=" (:client_type gen)
                   " cmd=" cmd-type))))
  
  (println "\nPROS:")
  (println "  ✓ Clean, declarative schema")
  (println "  ✓ Generator works automatically")
  (println "  ✓ Clear structure")
  (println "  ✓ oneof_edn handles everything")
  (println "CONS:")
  (println "  ✗ Changes message structure (adds :command wrapper)")
  (println "  ✗ Not backward compatible with flat protobuf"))

;; OPTION 2: Virtual oneof (maintains flat structure)
(println "\n\nOPTION 2: Virtual oneof with transformation (COMPATIBLE)")
(println "----------------------------------------------------------")
(let [;; Internal schema for generation
      internal-spec [:map {:closed true}
                    [:protocol_version [:int {:min 1}]]
                    [:client_type [:enum :ground :web]]
                    [:session_id {:optional true} :int]
                    [:_commands [:oneof_edn
                                [:ping [:map {:closed true} [:id :int]]]
                                [:echo [:map {:closed true} [:msg :string]]]
                                [:noop [:map {:closed true}]]]]]
      
      ;; Flat schema for validation (protobuf style)
      flat-spec [:and
                [:map {:closed true}
                 [:protocol_version [:int {:min 1}]]
                 [:client_type [:enum :ground :web]]
                 [:session_id {:optional true} :int]
                 [:ping {:optional true} [:maybe [:map {:closed true} [:id :int]]]]
                 [:echo {:optional true} [:maybe [:map {:closed true} [:msg :string]]]]
                 [:noop {:optional true} [:maybe [:map {:closed true}]]]]
                [:fn {:error/message "must have exactly one command"}
                 (fn [m]
                   (= 1 (count (filter some? [(m :ping) (m :echo) (m :noop)]))))]]
      
      ;; Generator that flattens the structure
      flat-generator (fn []
                      (let [internal (mg/generate internal-spec)]
                        (merge (dissoc internal :_commands)
                               (:_commands internal))))]
  
  (println "\nStructure: Commands at root level (protobuf-compatible)")
  (println "Example: {:protocol_version 1 :client_type :ground :ping {:id 123}}")
  
  (println "\nValidation (flat structure):")
  (let [valid {:protocol_version 1 :client_type :ground :ping {:id 789}}
        invalid {:protocol_version 1 :client_type :ground :ping {:id 1} :echo {:msg "x"}}]
    (println "  Valid:" valid "=>" (m/validate flat-spec valid))
    (println "  Invalid:" invalid "=>" (m/validate flat-spec invalid)))
  
  (println "\nGeneration (uses oneof_edn internally):")
  (dotimes [i 5]
    (let [gen (flat-generator)]
      (println (str "  Gen " i ": " gen))))
  
  (println "\nPROS:")
  (println "  ✓ Maintains flat protobuf structure")
  (println "  ✓ Backward compatible")
  (println "  ✓ Uses oneof_edn for generation")
  (println "CONS:")
  (println "  ✗ Needs transformation function")
  (println "  ✗ More complex than option 1"))

;; OPTION 3: Direct oneof at root (doesn't work well)
(println "\n\nOPTION 3: Why direct oneof at root doesn't work")
(println "------------------------------------------------")
(println "We CAN'T do this:")
(println "  [:merge")
(println "   [:map {:closed true} [:protocol_version :int] ...]")
(println "   [:oneof_edn [:ping ...] [:echo ...]]]")
(println "\nBecause:")
(println "  • oneof_edn is not a map schema, can't merge directly")
(println "  • Would need to be [:and ...] with custom validator")
(println "  • At that point, might as well use Option 2")

(println "\n\n=== RECOMMENDATION ===")
(println "For cmd/root specifically:")
(println "1. If breaking changes are OK: Use Option 1 (nested :command field)")
(println "2. If must maintain compatibility: Use Option 2 (virtual oneof)")
(println "3. Either way: Let oneof_edn handle the generation logic!")
(println "\nThe key insight: oneof_edn SHOULD generate the one-of behavior,")
(println "we just need to structure our schema to use it properly.")

(System/exit 0)