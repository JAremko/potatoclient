# I18n Translation Checker

A comprehensive tool for verifying translation completeness and consistency across all locales in the PotatoClient project.

## Purpose

Maintaining complete and consistent translations is critical for international users. This tool:
- Finds all `i18n/tr` calls in the codebase using AST parsing
- Identifies missing translation keys that will cause runtime errors
- Detects unused translation keys that can be cleaned up
- Verifies consistency across all supported locales
- Generates stub entries for missing translations

## Why Translation Completeness Matters

**Missing translations cause runtime errors** that display `[MISSING: :key-name]` in the UI. This tool helps:
- Prevent user-facing translation errors
- Maintain consistency across locales
- Clean up unused translations
- Streamline the translation workflow

## Usage

### Quick Start
```bash
# From project root
cd tools/i18n-checker

# Generate comprehensive report
./check.sh

# Generate stub entries for missing keys
./check.sh --stubs en   # English stubs
./check.sh --stubs uk   # Ukrainian stubs

# Show help
./check.sh --help
```

### Integration with Development Workflow
```bash
# After adding new UI text
./check.sh

# Before releasing a new version
./check.sh && ./check.sh --stubs uk

# As part of CI pipeline
./check.sh || exit 1
```

## How It Works

1. **AST-Based Analysis**: Uses `rewrite-clj` for accurate code parsing
2. **Comprehensive Scanning**: 
   - Finds all `i18n/tr` and `tr` calls in .clj and .cljc files
   - Extracts translation keys from source code
   - Loads all translation files from resources/i18n/
3. **Set Operations**: 
   - Compares used keys vs defined keys
   - Identifies missing and unused keys
   - Checks consistency across locales
4. **Stub Generation**: Creates template entries for missing translations

## Output Format

### Success Case
```
✅ I18N TRANSLATION CHECK PASSED
================================
Keys used in code: 142
Keys defined: 142
All locales complete: en, uk
No missing or unused keys detected!
```

### Issues Detected
```
📊 I18N TRANSLATION REPORT
==========================
✅ Keys in code: 156
📝 Keys defined: 148

❌ MISSING TRANSLATIONS (8)
--------------------------------
These keys are used but not defined:
- :settings/new-option
- :dialog/confirm-delete
- :error/network-timeout
- :status/processing
- :menu/export-data
[... 3 more]

⚠️ UNUSED KEYS (12)
------------------------
These keys are defined but never used:
- :old/deprecated-message
- :temp/debug-label
[... showing first 10 of 12]

❌ CONSISTENCY ISSUES
--------------------
uk is missing 5 keys:
- :settings/new-option
- :dialog/confirm-delete
[... showing first 5]

⚠️ Action needed: Add 8 missing translations and review 12 unused keys
```

## Generating Translation Stubs

When missing keys are detected, use the `--stubs` option to generate template entries:

```bash
./check.sh --stubs uk
```

Output:
```clojure
;; Add these to resources/i18n/uk.edn:
{:settings/new-option "TODO: translate"
 :dialog/confirm-delete "TODO: translate"
 :error/network-timeout "TODO: translate"
 :status/processing "TODO: translate"
 :menu/export-data "TODO: translate"}
```

## Supported Locales

| Locale | Code | File Path |
|--------|------|-----------|
| English | `en` | `resources/i18n/en.edn` |
| Ukrainian | `uk` | `resources/i18n/uk.edn` |

## Runtime Detection

In development mode (`make dev`), missing translations are automatically detected and displayed in the UI as:
```
[MISSING: :key-name]
```

This provides immediate feedback during development, but the checker tool ensures completeness before deployment.

## Best Practices

1. **Run after adding UI text** - Catch missing translations immediately
2. **Generate stubs early** - Provide translators with a complete list
3. **Review unused keys periodically** - Keep translation files clean
4. **Include in CI pipeline** - Prevent incomplete translations from reaching production
5. **Use descriptive key names** - Make translation intent clear

## Implementation Details

- **Language**: Clojure with rewrite-clj for AST parsing
- **Accuracy**: Finds both namespace-qualified and unqualified `tr` calls
- **Performance**: Efficient set operations for large codebases
- **Flexibility**: Easily extendable for new locales

## Common Issues and Solutions

### Issue: New locale not detected
**Solution**: Add the new locale file to `resources/i18n/` and ensure it follows the EDN format.

### Issue: Dynamic keys not detected
**Solution**: The tool only detects literal keyword arguments to `tr`. Avoid dynamic key generation.

### Issue: False positives for conditional translations
**Solution**: All translation keys should be defined even if conditionally used.

## File Structure

```
tools/i18n-checker/
├── check.sh           # Main execution script
├── deps.edn          # Dependencies (rewrite-clj)
├── README.md         # This file
└── src/
    └── i18n_checker/
        └── core.clj  # Core implementation
```

## Exit Codes

- **0**: All translations complete and consistent
- **1**: Missing or consistency issues detected
