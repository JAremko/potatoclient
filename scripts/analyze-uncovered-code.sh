#!/bin/bash
# Analyze coverage reports to find uncovered functions and code blocks

set -e

OUTPUT_FILE="target/coverage-reports/uncovered-functions.txt"
mkdir -p target/coverage-reports

echo "========================================" > "$OUTPUT_FILE"
echo "Uncovered Functions and Code Blocks" >> "$OUTPUT_FILE"
echo "Generated: $(date)" >> "$OUTPUT_FILE"
echo "========================================" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# 1. Analyze Clojure coverage
if [ -d target/coverage ]; then
    echo "## Clojure Uncovered Functions" >> "$OUTPUT_FILE"
    echo "--------------------------------" >> "$OUTPUT_FILE"
    
    # Parse Cloverage's text output for uncovered forms
    if [ -f target/coverage-reports/clojure-test.log ]; then
        # Extract uncovered namespaces and functions
        awk '/NOT COVERED:/{flag=1; next} /^$/{flag=0} flag' target/coverage-reports/clojure-test.log >> "$OUTPUT_FILE" 2>/dev/null || true
    fi
    
    # Parse coverage data from HTML (more reliable)
    if command -v python3 &> /dev/null; then
        python3 << 'EOF' >> "$OUTPUT_FILE" 2>/dev/null || true
import os
import re
from pathlib import Path

coverage_dir = Path("target/coverage")
if coverage_dir.exists():
    # Find all coverage HTML files
    for html_file in coverage_dir.glob("*.html"):
        if html_file.name != "index.html":
            with open(html_file, 'r') as f:
                content = f.read()
                # Extract namespace from filename
                ns = html_file.stem.replace('_', '.')
                
                # Find uncovered functions (simplified parsing)
                uncovered = re.findall(r'class="not-covered"[^>]*>([^<]+)</span>', content)
                if uncovered:
                    print(f"\nNamespace: {ns}")
                    for func in uncovered[:10]:  # Limit to first 10
                        if func.strip() and not func.startswith('('):
                            print(f"  - {func}")
EOF
    fi
    echo "" >> "$OUTPUT_FILE"
fi

# 2. Analyze Java/Kotlin coverage from JaCoCo
if [ -f target/coverage-reports/jacoco.xml ]; then
    echo "## Java/Kotlin Uncovered Functions" >> "$OUTPUT_FILE"
    echo "-----------------------------------" >> "$OUTPUT_FILE"
    
    # Parse JaCoCo XML for uncovered methods
    if command -v python3 &> /dev/null; then
        python3 << 'EOF' >> "$OUTPUT_FILE" 2>/dev/null || true
import xml.etree.ElementTree as ET
from pathlib import Path

jacoco_xml = Path("target/coverage-reports/jacoco.xml")
if jacoco_xml.exists():
    tree = ET.parse(jacoco_xml)
    root = tree.getroot()
    
    # Find all methods with 0% coverage
    for package in root.findall(".//package"):
        pkg_name = package.get('name', '').replace('/', '.')
        
        for class_elem in package.findall("class"):
            class_name = class_elem.get('name', '').split('/')[-1]
            
            for method in class_elem.findall("method"):
                method_name = method.get('name', '')
                method_desc = method.get('desc', '')
                
                # Check if method has 0 coverage
                counters = method.findall("counter")
                covered = False
                for counter in counters:
                    if counter.get('type') == 'LINE' and int(counter.get('covered', 0)) > 0:
                        covered = True
                        break
                
                if not covered and method_name not in ['<init>', '<clinit>']:
                    print(f"\n{pkg_name}.{class_name}")
                    print(f"  - {method_name}{method_desc}")
EOF
    else
        # Fallback: Use grep on XML
        echo "Parsing JaCoCo XML with grep..." >> "$OUTPUT_FILE"
        grep -B2 'covered="0"' target/coverage-reports/jacoco.xml | grep 'name=' | head -20 >> "$OUTPUT_FILE" 2>/dev/null || true
    fi
    echo "" >> "$OUTPUT_FILE"
fi

# 3. Summary statistics
echo "" >> "$OUTPUT_FILE"
echo "## Coverage Statistics Summary" >> "$OUTPUT_FILE"
echo "------------------------------" >> "$OUTPUT_FILE"

# Clojure stats
if [ -f target/coverage-reports/clojure-test.log ]; then
    echo "" >> "$OUTPUT_FILE"
    echo "### Clojure Coverage:" >> "$OUTPUT_FILE"
    grep -E "(Forms|Lines|Branches)" target/coverage-reports/clojure-test.log | tail -3 >> "$OUTPUT_FILE" 2>/dev/null || true
fi

# Java/Kotlin stats
if [ -f target/coverage-reports/jacoco.csv ]; then
    echo "" >> "$OUTPUT_FILE"
    echo "### Java/Kotlin Coverage:" >> "$OUTPUT_FILE"
    awk -F',' 'NR>1 {
        missed_inst+=$4; covered_inst+=$5
        missed_branch+=$6; covered_branch+=$7
        missed_line+=$8; covered_line+=$9
        missed_method+=$12; covered_method+=$13
    } END {
        if(missed_inst+covered_inst>0) printf "Instructions: %.2f%% (%d/%d)\n", covered_inst/(missed_inst+covered_inst)*100, covered_inst, missed_inst+covered_inst
        if(missed_branch+covered_branch>0) printf "Branches: %.2f%% (%d/%d)\n", covered_branch/(missed_branch+covered_branch)*100, covered_branch, missed_branch+covered_branch
        if(missed_line+covered_line>0) printf "Lines: %.2f%% (%d/%d)\n", covered_line/(missed_line+covered_line)*100, covered_line, missed_line+covered_line
        if(missed_method+covered_method>0) printf "Methods: %.2f%% (%d/%d)\n", covered_method/(missed_method+covered_method)*100, covered_method, missed_method+covered_method
    }' target/coverage-reports/jacoco.csv >> "$OUTPUT_FILE" 2>/dev/null || true
fi

echo "" >> "$OUTPUT_FILE"
echo "Report generated at: $OUTPUT_FILE"