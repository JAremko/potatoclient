#!/bin/bash
# Alternative coverage approach: Pre-compile everything and run tests selectively

set -e

echo "=========================================="
echo "Coverage with Pre-compilation Strategy"
echo "=========================================="

# Ensure we're in the project root
cd "$(dirname "$0")/.."

# Step 1: Full compilation including AOT
echo ""
echo "Step 1: Compiling all code (Java, Kotlin, Clojure)..."
echo "-----------------------------------------------------"
make build

# Step 2: Create a test runner that loads compiled classes
echo ""
echo "Step 2: Creating test runner with compiled classes..."
echo "-----------------------------------------------------"
cat > target/coverage-test-runner.clj << 'EOF'
;; Test runner that ensures compiled classes are loaded
(ns coverage-test-runner
  (:require [clojure.test :as test]
            [clojure.java.io :as io]))

;; Force loading of compiled protobuf classes
(System/setProperty "clojure.compile.path" "target/classes")

;; List of test namespaces that work with cloverage
(def testable-namespaces
  '[potatoclient.proto-constraints-test
    potatoclient.proto-test
    potatoclient.proto-validation-runtime-test
    potatoclient.proto-validation-sanity-test
    potatoclient.proto-validation-test
    potatoclient.required-field-validation-test
    potatoclient.state.comprehensive-generator-test
    potatoclient.state.generator-test
    potatoclient.state-integration-test
    potatoclient.validation-boundary-test
    potatoclient.validation-breakage-test
    potatoclient.websocket-simple-connection-test
    potatoclient.frame-timing-test
    potatoclient.frame-timing-integration-test])

;; Load and run tests
(doseq [ns testable-namespaces]
  (require ns))

(apply test/run-tests testable-namespaces)
EOF

# Step 3: Run cloverage with selective namespace loading
echo ""
echo "Step 3: Running coverage with selective instrumentation..."
echo "---------------------------------------------------------"
mkdir -p target/coverage

# Create a minimal deps configuration for coverage
cat > target/coverage-deps.edn << 'EOF'
{:paths ["src" "test" "target/classes" "resources"]
 :deps {org.clojure/clojure {:mvn/version "1.12.1"}
        cloverage/cloverage {:mvn/version "1.2.4"}}
 :aliases
 {:run-coverage
  {:jvm-opts ["-Dguardrails.enabled=false"
              "-Dclojure.compile.path=target/classes"
              "-cp" "target/classes:src:test:resources"]
   :main-opts ["-m" "cloverage.coverage"
               ;; Only instrument core namespaces without protobuf deps
               "-n" "potatoclient.specs"
               "-n" "potatoclient.theme" 
               "-n" "potatoclient.config"
               "-n" "potatoclient.i18n"
               "-n" "potatoclient.runtime"
               "-n" "potatoclient.logging"
               "-n" "potatoclient.state"
               "-n" "potatoclient.state.config"
               "-n" "potatoclient.state.device"
               "-n" "potatoclient.state.dispatch"
               "-n" "potatoclient.state.schemas"
               "-n" "potatoclient.state.streams"
               "-n" "potatoclient.state.ui"
               "-n" "potatoclient.state.utils"
               "-n" "potatoclient.ui.control-panel"
               "-n" "potatoclient.ui.log-viewer"
               "-n" "potatoclient.ui.main-frame"
               "-n" "potatoclient.ui.startup-dialog"
               "-n" "potatoclient.ui.utils"
               "-n" "potatoclient.events.stream"
               "-n" "potatoclient.core"
               "-n" "potatoclient.main"
               ;; Run the custom test runner
               "-x" "target/coverage-test-runner.clj"
               ;; Output options
               "--text"
               "--html" 
               "--junit"
               "--output" "target/coverage"]}}}
EOF

# Run coverage
clojure -Sdeps-file target/coverage-deps.edn \
        -M:run-coverage \
        2>&1 | tee target/coverage/coverage-precompile.log

# Step 4: Generate summary report
echo ""
echo "Step 4: Generating coverage summary..."
echo "-------------------------------------"

if [ -f "target/coverage/index.html" ]; then
    echo "✓ Coverage report generated successfully!"
    
    # Create enhanced summary
    cat > target/coverage/COVERAGE_SUMMARY.md << 'EOF'
# Coverage Report Summary

## Overview

This coverage report was generated using a pre-compilation strategy to handle
the mixed Clojure/Java/Protobuf codebase.

## Strategy Used

1. **Full compilation** of all Java, Kotlin, and Protobuf classes
2. **Selective instrumentation** of Clojure namespaces without protobuf dependencies  
3. **Custom test runner** to ensure proper classpath ordering

## Excluded Namespaces

The following namespaces were excluded due to protobuf dependencies:
- `potatoclient.cmd.*` - Command namespaces
- `potatoclient.proto` - Protobuf serialization
- `potatoclient.ipc` - IPC messaging  
- `potatoclient.process` - Process management

## Results

EOF
    
    if [ -f "target/coverage/coverage.txt" ]; then
        echo '```' >> target/coverage/COVERAGE_SUMMARY.md
        tail -15 target/coverage/coverage.txt >> target/coverage/COVERAGE_SUMMARY.md
        echo '```' >> target/coverage/COVERAGE_SUMMARY.md
    fi
    
    echo "" >> target/coverage/COVERAGE_SUMMARY.md
    echo "Generated: $(date)" >> target/coverage/COVERAGE_SUMMARY.md
    
    echo ""
    echo "Summary saved to: target/coverage/COVERAGE_SUMMARY.md"
else
    echo "⚠ Coverage report generation failed"
    echo "Check target/coverage/coverage-precompile.log for errors"
fi

# Cleanup
rm -f target/coverage-test-runner.clj
rm -f target/coverage-deps.edn

echo ""
echo "=========================================="
echo "Done!"
echo "=========================================="
echo ""
echo "View coverage report: target/coverage/index.html"