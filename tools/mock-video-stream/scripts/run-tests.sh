#!/bin/bash
# Run tests for mock video stream tool

set -euo pipefail

# Change to tool directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/.."

echo "Running Mock Video Stream Tests"
echo "================================"

# Set up classpath properly
CLASSPATH="src:test:resources:../../shared:../../src/java"

# Run tests with explicit classpath
clojure -Scp "$CLASSPATH" \
        -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.11.1"}
                        org.clojure/data.json {:mvn/version "2.4.0"}
                        metosin/malli {:mvn/version "0.13.0"}
                        com.cognitect/transit-clj {:mvn/version "1.0.333"}
                        com.cognitect/transit-java {:mvn/version "1.0.371"}
                        org.msgpack/msgpack {:mvn/version "0.6.12"}
                        com.taoensso/telemere {:mvn/version "1.0.0-beta16"}
                        io.github.cognitect-labs/test-runner {:git/tag "v0.5.1" :git/sha "dfb30dd"}}}' \
        -M -m cognitect.test-runner