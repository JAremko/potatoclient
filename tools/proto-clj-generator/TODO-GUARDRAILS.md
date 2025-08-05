# Proto-CLJ-Generator TODO: Guardrails & Testing

## 🎯 Primary Goals
1. Fix all remaining namespace/alias issues
2. Pass all tests with proper roundtrip validation
3. Generate guardrails-enabled functions using `>defn`/`>defn-`
4. Integrate Malli specs from shared directory
5. Setup proper clj-kondo configuration for guardrails

## 📋 Tasks

### 1. Fix Immediate Issues
- [ ] Fix namespace mapping in cmd.clj (e.g., `cmd.day.camera` → `cmd.daycamera`)
- [ ] Fix duplicate aliases (two `camera` aliases)
- [ ] Implement actual re-exports in index files (command.clj, state.clj)
- [ ] Update tests to work with new file locations

### 2. Comprehensive Roundtrip Testing
- [ ] Ensure cmd and state root generation tests work
- [ ] Test with Malli specs from `/home/jare/git/potatoclient/shared/specs/`
- [ ] Add buf.validate checks to roundtrip tests
- [ ] Compare input vs output for exact match
- [ ] Use specs from shared dir for validation

### 3. Guardrails Integration

#### Research References
- Main app code for guardrails usage patterns
- `/home/jare/git/potatoclient/tools/guardrails-check/` - tool to verify all functions use guardrails
- Main app Makefile for clj-kondo setup
- Main app `.clj-kondo/` config for guardrails support

#### Implementation
- [ ] Change generated functions from `defn`/`defn-` to `>defn`/`>defn-`
- [ ] Attach Malli specs from `/home/jare/git/potatoclient/shared/specs/`
- [ ] Ensure guardrails active in all builds except release (like main app)
- [ ] Generate proper function specs for:
  - Build functions: `[map? => protobuf-class?]`
  - Parse functions: `[protobuf-class? => map?]`
  - Payload handlers: appropriate specs

### 4. Spec Integration
- [ ] Load specs from `/home/jare/git/potatoclient/shared/specs/custom/`
- [ ] Map proto message types to Malli specs
- [ ] Use same specs for:
  - Guardrails function specs
  - Cmd/state root generation
  - Validation in tests

### 5. clj-kondo Configuration
- [ ] Copy/adapt `.clj-kondo/` config from main app
- [ ] Configure for guardrails macros (`>defn`, `>defn-`)
- [ ] Setup reporting to prevent false positives
- [ ] Ensure generated code passes linting

### 6. Tooling Integration
- [ ] Integrate guardrails-check tool
- [ ] Add Make target to verify all generated functions use guardrails
- [ ] Setup proper error reporting

## 🔍 Key Files to Research

### Main App Files
- `/home/jare/git/potatoclient/.clj-kondo/` - linting config
- `/home/jare/git/potatoclient/Makefile` - build/lint setup
- Examples of `>defn` usage in main app code
- `/home/jare/git/potatoclient/shared/specs/` - Malli specs

### Generator Files to Modify
- `frontend.clj` - Change defn generation to >defn
- `templates/` - Update templates for guardrails
- `core.clj` - Load shared specs
- New: `guardrails.clj` - Spec generation/mapping

## 📊 Success Criteria
1. All tests pass with new structure
2. Roundtrip tests validate perfectly
3. All generated functions use `>defn`/`>defn-`
4. Guardrails-check tool reports 100% coverage
5. clj-kondo reports no false positives
6. Buf.validate checks pass

## 💡 Implementation Notes

### Guardrails Function Pattern
```clojure
(>defn build-some-message
  "Build a SomeMessage protobuf from a map."
  [m]
  [::some-message-spec => protobuf-class?]
  (let [builder ...]
    ...))

(>defn parse-some-message
  "Parse a SomeMessage protobuf to a map."
  [proto]
  [protobuf-class? => ::some-message-spec]
  {...})
```

### Spec Loading
- Load specs at generation time
- Map proto package.MessageName to ::.../message-name specs
- Use specs for both guardrails and validation

### Build Configuration
- Guardrails enabled by default
- Disabled only for release builds
- Same pattern as main app