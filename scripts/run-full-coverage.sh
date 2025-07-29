#!/bin/bash
# Run full coverage with proper setup for mixed Clojure/Java/Protobuf codebase

set -e

echo "=========================================="
echo "Full Coverage with Mixed Codebase Support"
echo "=========================================="

# Ensure we're in the project root
cd "$(dirname "$0")/.."

# Step 1: Ensure all Java/Kotlin/Protobuf classes are compiled
echo ""
echo "Step 1: Ensuring all classes are compiled..."
echo "------------------------------------------"
if [ ! -d "target/classes" ] || [ -z "$(ls -A target/classes 2>/dev/null)" ]; then
    echo "Building project to ensure all classes are available..."
    make build
else
    echo "Classes already compiled"
fi

# Step 2: Create a custom deps.edn alias that includes compiled classes first
echo ""
echo "Step 2: Creating custom coverage configuration..."
echo "------------------------------------------"
cat > coverage-deps.edn << 'EOF'
{:aliases
 {:coverage-full
  {:extra-paths ["test" "target/classes" "src/potatoclient/java"]
   :extra-deps {cloverage/cloverage {:mvn/version "1.2.4"}
                io.github.cognitect-labs/test-runner {:git/url "https://github.com/cognitect-labs/test-runner"
                                                      :sha "dfb30dd6605cb6c0efc275e1df1736f6e90d4d73"}}
   :jvm-opts ["-Dguardrails.enabled=true"
              "-Dclojure.compile.path=target/classes"]
   :main-opts ["-m" "cloverage.coverage"
               "-p" "src"
               "-s" "test"
               ;; Exclude problematic namespaces
               "--ns-exclude-regex" ".*instrumentation.*"
               "--ns-exclude-regex" ".*\\.java\\..*"
               "--ns-exclude-regex" ".*\\.kotlin\\..*"
               ;; Exclude protobuf-dependent namespaces that cause ClassNotFoundException
               "--ns-exclude-regex" "potatoclient\\.cmd\\..*"
               "--ns-exclude-regex" "potatoclient\\.proto"
               "--ns-exclude-regex" "potatoclient\\.ipc"
               "--ns-exclude-regex" "potatoclient\\.process"
               ;; Exclude problematic function calls
               "--exclude-call" "clojure.core/import"
               "--exclude-call" "clojure.core/gen-class"
               ;; Output options
               "--codecov"
               "--junit"
               "--text"
               "--html"
               "--output" "target/coverage"]}}}
EOF

# Step 3: Run coverage with the custom configuration
echo ""
echo "Step 3: Running coverage..."
echo "------------------------------------------"
mkdir -p target/coverage

# Use the custom deps.edn with proper classpath ordering
clojure -Sdeps "$(cat coverage-deps.edn)" \
        -M:coverage-full \
        2>&1 | tee target/coverage/cloverage-full.log

# Step 4: Analyze results
echo ""
echo "Step 4: Analyzing coverage results..."
echo "------------------------------------------"

# Check if HTML report was generated
if [ -f "target/coverage/index.html" ]; then
    echo "✓ Coverage report generated successfully!"
    echo ""
    # Extract coverage summary
    if [ -f "target/coverage/coverage.txt" ]; then
        echo "Coverage Summary:"
        echo "-----------------"
        tail -10 target/coverage/coverage.txt
    fi
else
    echo "⚠ Coverage report generation failed"
    echo "Check target/coverage/cloverage-full.log for errors"
fi

# Clean up temporary file
rm -f coverage-deps.edn

echo ""
echo "=========================================="
echo "Coverage Analysis Complete"
echo "=========================================="
echo ""
echo "Reports available at:"
echo "  HTML: target/coverage/index.html"
echo "  Text: target/coverage/coverage.txt"
echo "  Log:  target/coverage/cloverage-full.log"
echo ""
echo "Note: Some namespaces are excluded due to protobuf dependencies."
echo "To see which namespaces were instrumented, check the log file."