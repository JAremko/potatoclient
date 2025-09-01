#!/bin/bash
# I18n checker script

cd "$(dirname "$0")"

echo "Running I18n Translation Checker..."
echo ""

clojure -M -m i18n-checker.core "$@"