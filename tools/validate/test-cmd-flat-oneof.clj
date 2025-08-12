#!/usr/bin/env clojure

(ns test-cmd-flat-oneof
  "Test using oneof_edn with flat protobuf-style structure"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.util :as mu]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== Testing Flat Protobuf-Style with oneof_edn ===\n")

;; Approach 1: Commands at root level (protobuf style)
;; but validated as oneof
(println "Approach 1: Flat structure with :and validation")
(let [base-map [:map {:closed true}
                [:protocol_version [:int {:min 1}]]
                [:client_type [:enum :ground :web]]
                ;; Commands as optional fields
                [:ping {:optional true} [:maybe [:map {:closed true} [:id :int]]]]
                [:echo {:optional true} [:maybe [:map {:closed true} [:msg :string]]]]
                [:noop {:optional true} [:maybe [:map {:closed true}]]]]
      
      ;; Add oneof validation
      flat-with-validation [:and
                           base-map
                           [:fn {:error/message "must have exactly one command"}
                            (fn [m]
                              (let [cmds [:ping :echo :noop]
                                    non-nil (filter #(some? (get m %)) cmds)]
                                (= 1 (count non-nil))))]]
      
      ;; Can't easily generate for :and with :fn, so custom generator
      flat-with-generator [:and 
                          {:gen/gen (fn [_]
                                     (mg/generator
                                      [:map {:closed true}
                                       [:protocol_version [:int {:min 1}]]
                                       [:client_type [:enum :ground :web]]
                                       [:command [:oneof_edn
                                                 [:ping [:map {:closed true} [:id :int]]]
                                                 [:echo [:map {:closed true} [:msg :string]]]
                                                 [:noop [:map {:closed true}]]]]]))}
                          flat-with-validation]]
  
  (println "  Valid examples:")
  (doseq [v [{:protocol_version 1 :client_type :ground :ping {:id 123}}
            {:protocol_version 2 :client_type :web :echo {:msg "hello"}}
            {:protocol_version 1 :client_type :ground :noop {}}]]
    (println (str "    " v " => " (m/validate flat-with-validation v))))
  
  (println "\n  Invalid examples:")
  (doseq [v [{:protocol_version 1 :client_type :ground}
            {:protocol_version 1 :client_type :ground :ping {:id 1} :echo {:msg "x"}}]]
    (println (str "    " v " => " (m/validate flat-with-validation v)))))

;; Approach 2: Create a "projection" schema that converts between
;; flat and nested representations
(println "\n\nApproach 2: Transform between flat and nested")
(let [;; Internal schema with nested command
      nested-schema [:map {:closed true}
                    [:protocol_version [:int {:min 1}]]
                    [:client_type [:enum :ground :web]]
                    [:command [:oneof_edn
                              [:ping [:map {:closed true} [:id :int]]]
                              [:echo [:map {:closed true} [:msg :string]]]
                              [:noop [:map {:closed true}]]]]]
      
      ;; Functions to convert between representations
      flat->nested (fn [flat]
                    (let [base {:protocol_version (:protocol_version flat)
                               :client_type (:client_type flat)}
                          cmd (cond
                               (:ping flat) {:ping (:ping flat)}
                               (:echo flat) {:echo (:echo flat)}
                               (:noop flat) {:noop (:noop flat)})]
                      (assoc base :command cmd)))
      
      nested->flat (fn [nested]
                    (let [base {:protocol_version (:protocol_version nested)
                               :client_type (:client_type nested)}
                          cmd (:command nested)]
                      (merge base cmd)))]
  
  (println "  Testing transformations:")
  (let [flat-msg {:protocol_version 1 :client_type :ground :ping {:id 456}}
        nested (flat->nested flat-msg)
        back-to-flat (nested->flat nested)]
    (println (str "    Flat: " flat-msg))
    (println (str "    -> Nested: " nested))
    (println (str "    -> Back to flat: " back-to-flat))
    (println (str "    Nested valid? " (m/validate nested-schema nested))))
  
  (println "\n  Generating and flattening:")
  (dotimes [i 3]
    (let [nested (mg/generate nested-schema)
          flat (nested->flat nested)]
      (println (str "    Gen " i ": " flat)))))

;; Approach 3: Best of both - use oneof_edn as a virtual field
(println "\n\nApproach 3: Virtual oneof field (RECOMMENDED)")
(let [;; Define the spec with all fields but group commands conceptually
      cmd-root-spec [:and
                    ;; Base map with all fields
                    [:map {:closed true}
                     [:protocol_version [:int {:min 1}]]
                     [:client_type [:enum :ground :web]]
                     ;; Commands as optional
                     [:ping {:optional true} [:maybe [:map {:closed true} [:id :int]]]]
                     [:echo {:optional true} [:maybe [:map {:closed true} [:msg :string]]]]
                     [:noop {:optional true} [:maybe [:map {:closed true}]]]]
                    ;; Oneof validator as a virtual constraint
                    [:fn {:error/message "must have exactly one command (ping, echo, or noop)"}
                     (fn [m]
                       (= 1 (count (filter some? [(m :ping) (m :echo) (m :noop)]))))]]
      
      ;; Generator that understands the oneof constraint
      spec-with-gen (m/schema
                     cmd-root-spec
                     {:registry (m/default-schemas)
                      :gen/gen (fn [_schema _options]
                                (mg/generator
                                 [:map {:closed true}
                                  [:protocol_version [:int {:min 1}]]
                                  [:client_type [:enum :ground :web]]
                                  [:_commands [:oneof_edn
                                              [:ping [:map {:closed true} [:id :int]]]
                                              [:echo [:map {:closed true} [:msg :string]]]
                                              [:noop [:map {:closed true}]]]]]
                                 {:gen/fmap (fn [m]
                                             (merge
                                              (select-keys m [:protocol_version :client_type])
                                              (:_commands m)))}))})]
  
  (println "  This approach:")
  (println "    • Keeps flat structure (protobuf-compatible)")
  (println "    • Validates oneof constraint")  
  (println "    • Can generate valid examples")
  (println "    • Works with existing code")
  
  (println "\n  Validation:")
  (doseq [v [{:protocol_version 1 :client_type :ground :ping {:id 789}}
            {:protocol_version 2 :client_type :web :echo {:msg "test"}}]]
    (println (str "    " v " => " (m/validate spec-with-gen v))))
  
  (println "\n  Generation:")
  (dotimes [i 5]
    (let [gen (mg/generate spec-with-gen)]
      (println (str "    Gen " i ": " gen)))))

(println "\n\n=== SUMMARY ===")
(println "For cmd/root with flat protobuf structure:")
(println "1. Keep the flat map structure to match protobuf")
(println "2. Use :and with :fn validator for oneof constraint")
(println "3. Use custom generator that internally uses oneof_edn")
(println "4. This maintains compatibility while getting validation + generation")

(System/exit 0)