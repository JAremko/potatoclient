(ns potatoclient.specs.proto-generators
  "Generators for proto-map fields"
  (:require
   [clojure.test.check.generators :as gen]
   [pronto.core :as p]))

(defn ping-generator
  "Generator for Ping proto-maps"
  [mapper ping-class]
  (gen/return (p/proto-map mapper ping-class)))

(defn noop-generator
  "Generator for Noop proto-maps"
  [mapper noop-class]
  (gen/return (p/proto-map mapper noop-class)))

(defn frozen-generator
  "Generator for Frozen proto-maps"
  [mapper frozen-class]
  (gen/return (p/proto-map mapper frozen-class)))