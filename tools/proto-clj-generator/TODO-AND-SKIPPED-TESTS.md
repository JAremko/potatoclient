# TODO and Skipped Tests Report

This file documents all remaining TODOs and disabled/skipped tests that need to be addressed per Phase 0 requirements.

## PARAMOUNT PRINCIPLE: Full Roundtrip Validation with Boundary Checks

**This is non-negotiable and must be implemented and passing:**

The proto-clj-generator MUST have a comprehensive test that validates the entire data pipeline:

1. **Generate test data** from Malli specs (including boundary conditions for all constraints)
2. **Validate with Malli specs** - ensure generated data passes spec validation
3. **Convert to Java protobuf** - successfully build Java objects
4. **Validate with buf.validate** - ensure Java objects pass protobuf validation
5. **Serialize to binary** - convert to wire format
6. **Parse back from binary** - deserialize to Java objects
7. **Validate again** - ensure parsed data still passes validation
8. **Convert to EDN** - parse back to Clojure data
9. **Compare with original** - ensure roundtrip preserves data exactly

### Requirements:
- Test MUST cover both `cmd` and `state` message roots
- Test MUST include boundary conditions (min/max values, empty/full collections, etc.)
- Test MUST include negative testing (invalid data should fail at appropriate stages)
- Test CANNOT be skipped or disabled
- Test MUST run as part of regular test suite

### Current Status:
- **full_roundtrip_validation_test.clj.disabled** exists but is disabled
- **full_roundtrip_validation_test.clj** created with custom :oneof support
- **Basic oneof tests** created and passing in test_oneof.clj
- Next: Fix registry issues to make :oneof work in generated code

## TODOs in Source Code

### 1. naming_config.clj:96
**Location**: `/src/generator/naming_config.clj`
**TODO**: Load from file
```clojure
(string? config-source)
;; TODO: Load from file
(merge-with merge default-config (read-string (slurp config-source)))
```
**Action Required**: Implement file loading functionality for naming configuration
**Priority**: Medium

### 2. spec_gen.clj:142
**Location**: `/src/generator/spec_gen.clj`
**TODO**: Switch to custom :oneof spec once we debug the registry issues
```clojure
;; For now, use :altn which is proven to work
;; TODO: Switch to custom :oneof spec once we debug the registry issues
field-specs (into {}
```
**Action Required**: Debug registry issues and switch from :altn to custom :oneof spec
**Priority**: High (affects spec generation quality)
**Progress**: Basic :oneof tests passing, need to fix registry propagation in generated code

### 3. edn_test.clj:156
**Location**: `/test/generator/edn_test.clj`
**TODO**: Implement resolve-builder-name function in frontend
```clojure
;; TODO: Implement resolve-builder-name function in frontend
#_(deftest builder-name-resolution-test
```
**Action Required**: Implement resolve-builder-name function and enable test
**Priority**: Medium

### 4. dependency_graph.clj:152 (COMPLETED)
**Location**: `/src/generator/dependency_graph.clj`
**Status**: ✅ COMPLETED - analyze-type-references function has been implemented
**Action**: None - can be removed from TODO list

## Skipped/Disabled Test Files

### Files with .skip extension:

1. **comprehensive_roundtrip_ns_test.clj.skip**
   - Full path: `/test/generator/comprehensive_roundtrip_ns_test.clj.skip`
   - **Purpose**: Tests namespace-split generated code roundtrip
   - **Dependencies**: Requires proto-explorer output and Java imports (cmd.JonSharedCmd)
   - **Why Disabled**: Likely disabled due to namespace split changes or proto-explorer dependency
   - **Action Required**: 
     - Verify if namespace splitting is still a supported feature
     - Update import paths if needed
     - Re-enable if feature is still active
   
2. **malli_spec_roundtrip_test.clj.skip**
   - Full path: `/test/generator/malli_spec_roundtrip_test.clj.skip`
   - **Purpose**: Tests roundtrip validation using Malli specs from shared directory
   - **Dependencies**: Requires shared specs from potatoclient.specs.malli-oneof
   - **Why Disabled**: Likely disabled due to missing shared specs or oneof implementation issues
   - **Action Required**: 
     - Verify shared specs availability
     - Update to use new :altn approach instead of custom :oneof
     - Re-enable after fixing oneof registry issues

### Files with .disabled extension:

1. **buf_validate_roundtrip_test.clj.disabled**
   - Full path: `/test/generator/buf_validate_roundtrip_test.clj.disabled`
   - **Purpose**: Unknown - need to examine file
   - **Action Required**: Examine and determine status
   
2. **buf_validate_test.clj.disabled**
   - Full path: `/test/generator/buf_validate_test.clj.disabled`
   - **Purpose**: Tests buf.validate constraints enforcement
   - **Dependencies**: Java imports for buf.protovalidate.Validator
   - **Why Disabled**: Requires Java protobuf validator library that may not be available
   - **Action Required**: 
     - Since we now generate Malli constraints, this test may be obsolete
     - Consider removing or converting to test Malli validation instead
   
3. **full_roundtrip_validation_test.clj.disabled**
   - Full path: `/test/generator/full_roundtrip_validation_test.clj.disabled`
   - **Purpose**: Comprehensive end-to-end roundtrip tests with full validation chain
   - **Dependencies**: 
     - Malli oneof custom schema
     - buf.validate Java validator
     - potatoclient.specs.cmd and potatoclient.specs.ser
   - **Why Disabled**: Multiple dependencies including custom :oneof schema and Java validator
   - **Action Required**: 
     - Convert to use :altn instead of custom :oneof
     - Remove buf.validate dependency (use Malli validation instead)
     - Update or remove if too complex
   
4. **namespace_separation_test.clj.disabled**
   - Full path: `/test/generator/namespace_separation_test.clj.disabled`
   - **Purpose**: Tests namespace separation functionality
   - **Dependencies**: generator.core-namespaced and generator.frontend-namespaced
   - **Why Disabled**: References non-existent namespaced versions of core modules
   - **Action Required**: 
     - Determine if namespace separation is still a supported feature
     - Remove if obsolete, or update to use current architecture

## Phase 0 Cleanup Tasks

Based on TODO-GUARDRAILS.md requirements:

### Non-Guardrails Templates
- **Action Required**: Remove all templates that don't use guardrails
- **Location**: `/resources/templates/` directory contains both guardrails and non-guardrails versions
- **Templates to Remove**:
  - `builder.clj` (keep `builder-guardrails.clj`)
  - `namespace.clj` (keep `namespace-guardrails.clj`)
  - `namespace-with-requires.clj` (keep `namespace-with-requires-guardrails.clj`)
  - `namespace-with-specs.clj` (keep `namespace-with-specs-guardrails.clj`)
  - `oneof-builder.clj` (keep `oneof-builder-guardrails.clj`)
  - `oneof-parser.clj` (keep `oneof-parser-guardrails.clj`)
  - `parser.clj` (keep `parser-guardrails.clj`)
- **Templates to Keep**:
  - All `-guardrails.clj` versions
  - `field-getter.clj`, `field-setter.clj`, `field-getter-repeated.clj`, `field-setter-repeated.clj` (simple, no need for guardrails)
  - `malli-spec.clj` (spec definition, not a function)
  - `validation-helpers.clj` (new, already uses guardrails)

### Commented-Out Code
- **Action Required**: Remove all commented-out code blocks
- **Status**: Partially complete - found some in edn_test.clj

### Test Organization
- **Action Required**: Ensure all tests are properly organized and enabled
- **Status**: Multiple disabled tests need review

## Summary Statistics

- **Active TODOs**: 3 (excluding the completed dependency_graph.clj TODO)
- **Skipped Tests**: 2 files
- **Disabled Tests**: 4 files
- **Total Test Files Needing Attention**: 6

## Recommended Action Plan

1. **High Priority**:
   - Enable or remove the 6 disabled/skipped test files
   - Complete the custom :oneof spec switch (spec_gen.clj)
   
2. **Medium Priority**:
   - Implement config file loading (naming_config.clj)
   - Implement resolve-builder-name function (edn_test.clj)
   
3. **Low Priority**:
   - Clean up any remaining commented-out code
   - Remove non-guardrails templates if any exist

## Progress Update

### Completed
- ✅ Created comprehensive TODO and skipped tests documentation
- ✅ Created basic oneof spec tests (test_oneof.clj) - all passing
- ✅ Created full roundtrip validation test file with custom :oneof support
- ✅ Verified custom :oneof spec works correctly in isolation

### In Progress
- 🔄 Debugging registry issues for custom :oneof spec in generated code
- 🔄 Working on enabling full roundtrip validation test

### Next Steps
1. Fix registry propagation to make :oneof work in generated code
2. Complete the full roundtrip validation test
3. Remove non-guardrails templates

## Priority Action Plan

### IMMEDIATE (Must Do First):
1. **Enable Full Roundtrip Validation Test**
   - Update `full_roundtrip_validation_test.clj.disabled`
   - Remove custom :oneof dependency (use :altn)
   - Ensure it covers all requirements from PARAMOUNT PRINCIPLE
   - This test MUST pass before any other work continues

### HIGH PRIORITY:
1. **Remove Non-Guardrails Templates** (7 files to delete)
2. **Fix/Remove Disabled Tests** (6 test files need attention)
3. **Complete Custom :oneof Spec Switch** (spec_gen.clj TODO)

### MEDIUM PRIORITY:
1. **Implement Config File Loading** (naming_config.clj TODO)
2. **Implement resolve-builder-name** (edn_test.clj TODO)

### LOW PRIORITY:
1. **Remove Commented-Out Code**
2. **General Code Cleanup**

## Next Steps

1. Start with the IMMEDIATE priority - enable and fix the full roundtrip validation test
2. Remove the 7 non-guardrails template files
3. Review and fix/remove the 6 disabled test files
4. Complete remaining TODOs in priority order
5. Ensure all tests pass before marking Phase 0 complete