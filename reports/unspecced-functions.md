# Functions Not Using Guardrails Report

*Generated: 2025-07-18 23:40:39*

## Summary

![Functions Not Using Guardrails](https://img.shields.io/badge/Functions%20Not%20Using%20Guardrails-7-orange)

⚠️ Found **7** functions not using Guardrails across **5** namespaces.

## Functions by Namespace

### potatoclient.core

- `-main`

### potatoclient.dev

- `enable-all-dev-settings!`
- `enable-assertions!`
- `enable-verbose-logging!`

### potatoclient.main

- `-main`

### potatoclient.proto

- `create-command`

### potatoclient.reports

- `md-list-item`


## Statistics

| Namespace | Count |
| --- | --- |
| `potatoclient.dev` | 3 |
| `potatoclient.reports` | 1 |
| `potatoclient.proto` | 1 |
| `potatoclient.core` | 1 |
| `potatoclient.main` | 1 |

## Next Steps

1. Convert the functions to use Guardrails `>defn` syntax
2. Add proper function specs like: `[arg-spec => return-spec]`
3. Run `(potatoclient.reports/generate-unspecced-functions-report!)` again to update this report

