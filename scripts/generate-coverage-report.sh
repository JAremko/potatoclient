#!/bin/bash
# Generate comprehensive coverage reports for Clojure, Java, and Kotlin

set -e

echo "=========================================="
echo "Generating Code Coverage Reports"
echo "=========================================="

# Ensure we're in the project root
cd "$(dirname "$0")/.."

# Create coverage output directory
mkdir -p target/coverage-reports

# 1. Setup JaCoCo for Java/Kotlin coverage
echo ""
echo "Setting up JaCoCo..."
./scripts/setup-jacoco.sh

# 2. Ensure Java/Kotlin classes are compiled
echo ""
echo "Ensuring Java/Kotlin classes are compiled..."
echo "------------------------------------------"
# Check if classes exist, compile if not
if [ ! -d "target/classes" ] || [ -z "$(ls -A target/classes 2>/dev/null)" ]; then
    echo "Classes not found, compiling..."
    make compile-kotlin compile-java-proto
else
    echo "Classes already compiled"
fi

# 3. Run Clojure tests with coverage
echo ""
echo "Running Clojure tests with coverage..."
echo "------------------------------------------"
clojure -M:test-coverage 2>&1 | tee target/coverage-reports/clojure-test.log || true

# 4. Run Java/Kotlin tests with JaCoCo
echo ""
echo "Running Java/Kotlin tests with JaCoCo..."
echo "------------------------------------------"

# Run tests with JaCoCo agent
java -javaagent:target/jacocoagent.jar=destfile=target/jacoco.exec \
     -cp "$(clojure -Spath):target/classes:target/test-classes" \
     clojure.main -m cognitect.test-runner 2>&1 | tee target/coverage-reports/java-kotlin-test.log || true

# 5. Generate JaCoCo reports
if [ -f target/jacoco.exec ]; then
    echo ""
    echo "Generating JaCoCo HTML report..."
    java -jar target/jacococli.jar report target/jacoco.exec \
         --classfiles target/classes \
         --sourcefiles src/potatoclient/kotlin \
         --sourcefiles src/potatoclient/java \
         --html target/coverage-reports/jacoco-html \
         --xml target/coverage-reports/jacoco.xml \
         --csv target/coverage-reports/jacoco.csv
fi

# 6. Generate combined coverage summary
echo ""
echo "Generating coverage summary..."
cat > target/coverage-reports/coverage-summary.txt << EOF
========================================
Code Coverage Summary Report
Generated: $(date)
========================================

## Clojure Coverage (via Cloverage)
EOF

if [ -f target/coverage/index.html ]; then
    echo "HTML Report: target/coverage/index.html" >> target/coverage-reports/coverage-summary.txt
    # Extract coverage percentage from cloverage output
    grep -A5 "Forms" target/coverage-reports/clojure-test.log >> target/coverage-reports/coverage-summary.txt 2>/dev/null || true
else
    echo "No Clojure coverage data found" >> target/coverage-reports/coverage-summary.txt
fi

cat >> target/coverage-reports/coverage-summary.txt << EOF

## Java/Kotlin Coverage (via JaCoCo)
EOF

if [ -f target/coverage-reports/jacoco.csv ]; then
    echo "HTML Report: target/coverage-reports/jacoco-html/index.html" >> target/coverage-reports/coverage-summary.txt
    echo "" >> target/coverage-reports/coverage-summary.txt
    # Parse JaCoCo CSV for summary
    awk -F',' 'NR>1 {missed+=$4; covered+=$5} END {if(missed+covered>0) printf "Line Coverage: %.2f%% (%d/%d)\n", covered/(missed+covered)*100, covered, missed+covered}' \
        target/coverage-reports/jacoco.csv >> target/coverage-reports/coverage-summary.txt 2>/dev/null || true
else
    echo "No Java/Kotlin coverage data found" >> target/coverage-reports/coverage-summary.txt
fi

# 7. List uncovered functions
echo ""
echo "Analyzing uncovered code..."
./scripts/analyze-uncovered-code.sh

echo ""
echo "=========================================="
echo "Coverage reports generated successfully!"
echo "=========================================="
echo ""
echo "View reports at:"
echo "  - Clojure: target/coverage/index.html"
echo "  - Java/Kotlin: target/coverage-reports/jacoco-html/index.html"
echo "  - Summary: target/coverage-reports/coverage-summary.txt"
echo "  - Uncovered functions: target/coverage-reports/uncovered-functions.txt"