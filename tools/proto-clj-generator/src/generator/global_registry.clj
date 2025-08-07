(ns generator.global-registry
  "Global registry setup for Malli schemas used throughout the generator"
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [generator.edn-specs :as edn-specs]
            [generator.specs :as specs]
            [generator.specs-with-generators :as specs-gen]
            [generator.specs-with-generators-v2 :as specs-gen-v2]))

;; =============================================================================
;; Registry Setup
;; =============================================================================

(defn create-global-registry
  "Create a composite registry with all schemas used in the project"
  []
  (mr/composite-registry
    ;; Base Malli schemas
    (m/default-schemas)
    ;; Custom :oneof schema
    {:oneof oneof/-oneof-schema}
    ;; EDN specs
    edn-specs/registry
    ;; Generator specs
    specs/registry
    ;; Specs with generators v1
    specs-gen/all-specs
    ;; Specs with generators v2
    specs-gen-v2/all-specs))

(defn init!
  "Initialize the global default registry"
  []
  (mr/set-default-registry! (create-global-registry)))

;; Initialize on namespace load
(init!)

;; =============================================================================
;; Helper Functions
;; =============================================================================

(defn validate
  "Validate data against a schema using the global registry"
  [schema data]
  (m/validate schema data))

(defn explain
  "Explain validation errors using the global registry"
  [schema data]
  (m/explain schema data))

(defn generate
  "Generate data from a schema using the global registry"
  [schema]
  (require '[malli.generator :as mg])
  ((resolve 'mg/generate) ((resolve 'mg/generator) schema)))

(defn schema
  "Create a schema instance using the global registry"
  [schema-form]
  (m/schema schema-form))