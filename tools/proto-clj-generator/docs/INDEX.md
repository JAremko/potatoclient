# Proto-CLJ-Generator Documentation Index

## Architecture & Design
- [Multi-Pass IR Generation](MULTI-PASS-IR-GENERATION.md) - Detailed explanation of the IR transformation pipeline
- [Dependency Resolution Design](DEPENDENCY-RESOLUTION-DESIGN.md) - Cross-namespace type resolution system

## User Guides
- [Examples](examples.md) - Code examples for common use cases
- [Guardrails Usage](guardrails-usage.md) - How to use guardrails in generated code
- [Malli Integration](malli-integration.md) - Working with Malli specs

## Development History
- [Project Summary](PROJECT-SUMMARY.md) - Overall project evolution
- [Phase 4 Summary](PHASE4-SUMMARY.md) - Integrated code generation implementation
- [Phase 5 Summary](PHASE5-SUMMARY.md) - Testing and validation implementation
- [Bug Fixes Summary](BUG-FIXES-SUMMARY.md) - Major bugs found and fixed

## API Documentation

### Core Modules
- `generator.backend` - JSON descriptor parsing and basic IR generation
- `generator.deps` - Dependency resolution and IR enrichment
- `generator.specs` - Malli specifications for all data structures
- `generator.frontend` - Code generation for single-file mode
- `generator.frontend-namespaced` - Code generation for namespace-separated mode

### Constraint Modules
- `generator.constraints.extractor` - Extract buf.validate rules from JSON
- `generator.constraints.compiler` - Compile constraints to Malli schemas
- `generator.validation-helpers` - Runtime validation helper generation

### Support Modules
- `generator.spec-gen` - Malli spec generation from IR
- `generator.type-resolution` - Type reference resolution
- `generator.naming` - Proto to Clojure naming conversions

## Testing
- Comprehensive test suite with >30 test files
- Property-based testing with Malli generators
- Full roundtrip validation tests
- Constraint boundary testing

## Contributing
See the main [README](../README.md) for contribution guidelines and code quality standards.