# Shared Specifications Library

This directory contains shared Malli specifications and utilities used across multiple PotatoClient projects:
- `cmd-explorer`: Command exploration and validation tool
- `state-explorer`: State management and exploration tool  
- Main application: The primary PotatoClient application

## Structure

```
shared/src/potatoclient/
├── malli/
│   ├── pronto.clj        # Pronto-Malli integration layer
│   └── registry.clj       # Global Malli registry management
├── specs/
│   ├── cmd_root.clj      # JonCommand root message specs
│   ├── common.clj        # Common reusable specs (angles, ranges, positions, enums)
│   ├── oneof_pronto.clj  # Custom oneof schema for Pronto proto-maps
│   └── proto_generators.clj # Proto-map field generators
└── validation/
    └── (future validation utilities)
```

## Usage

Projects access shared specs via symlinks:
- `/tools/cmd-explorer/src/potatoclient` → `/shared/src/potatoclient`
- `/tools/state-explorer/src/potatoclient` → `/shared/src/potatoclient`
- `/src/potatoclient_shared` → `/shared/src/potatoclient`

## Key Features

- **Common Specs**: Reusable specifications for angles, ranges, positions, enums
- **Proto Validation**: Custom oneof-pronto schema for validating Pronto proto-maps
- **Performance Optimized**: Includes macros and utilities for optimized proto-map operations
- **Global Registry**: Centralized Malli registry management for spec reuse

## Testing

Each project should initialize the shared registry in its test harness:

```clojure
(ns your-project.test-harness
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-pronto :as oneof]))

;; Initialize registry with oneof-pronto schema
(registry/setup-global-registry! (oneof/register-oneof-pronto-schema!))

;; Load specs (they auto-register)
(require '[potatoclient.specs.common])
(require '[potatoclient.specs.cmd-root])
```