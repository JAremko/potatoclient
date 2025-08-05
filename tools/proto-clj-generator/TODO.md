# Proto-CLJ-Generator TODO

## 🎯 Primary Goal: Robust Dependency Graph for Namespace Generation

### Current Issues
1. Generated namespaces don't have proper `:require` statements for dependencies
2. No dependency analysis before generation
3. Re-exports mention "backward compatibility" instead of testing support
4. Tests need to work with the new split-file architecture

### Dependency Structure (from proto analysis)
```
jon_shared_data_types.proto (Foundation - shared enums/types)
    ↑
    ├── Most command files (cmd_system, cmd_cv, cmd_rotary, etc.)
    ├── Most state files (data_camera_heat, data_rotary, etc.)
    ├── jon_shared_cmd.proto (aggregates all commands)
    └── jon_shared_data.proto (aggregates all state)
```

## 📋 Tasks

### 1. Implement Dependency Graph Builder
- [ ] Create `generator.dependency-graph` namespace
- [ ] Parse proto imports from JSON descriptors or EDN
- [ ] Build directed acyclic graph (DAG) of dependencies
- [ ] Detect circular dependencies (error if found)
- [ ] Generate topological sort for compilation order

### 2. Update Backend to Preserve Import Information
- [ ] Modify `backend.clj` to capture proto import statements
- [ ] Add `:imports` key to file EDN structure
- [ ] Map proto imports to Clojure namespace requires

### 3. Enhance Frontend Namespace Generation
- [ ] Generate proper `:require` statements based on dependency graph
- [ ] Handle cross-namespace type references
- [ ] Example: `cmd.rotary` needs to require `ser.types` for enums
- [ ] Ensure compilation order follows dependency graph

### 4. Fix Re-export Strategy
- [ ] Change re-export comments from "backward compatibility" to "testing support"
- [ ] Generate actual re-export functions (not just comments)
- [ ] Consider using `potemkin/import-vars` or manual def/defn forwarding
- [ ] Make index files (`command.clj`, `state.clj`) actually useful for tests

### 5. Update Test Infrastructure
- [ ] Update existing tests to work with split namespaces
- [ ] Add compilation test that ensures all generated files compile
- [ ] Use compilation success as dependency graph validation
- [ ] Update `comprehensive_roundtrip_test.clj` for new structure

### 6. Consider Implementation Strategies

#### Option A: Smart Dependency Resolution (Preferred)
- Analyze actual type usage in messages
- Generate minimal required imports
- Use Clojure's compiler to validate

#### Option B: Brute Force + Pruning (Backup)
- Import everything everywhere initially
- Use clj-kondo or similar to detect unused
- Use rewrite-clj to remove unused imports
- (Acknowledging this is "kinda smelly")

## 🏗️ Implementation Order

1. **Phase 1**: Dependency Graph Infrastructure
   - Build graph from proto imports
   - Add import tracking to backend

2. **Phase 2**: Smart Namespace Generation
   - Generate requires based on graph
   - Handle type references correctly

3. **Phase 3**: Testing & Validation
   - Update test suite
   - Add compilation tests
   - Validate dependency correctness

4. **Phase 4**: Re-export Implementation
   - Implement proper re-exports
   - Update index files for testing

## 🔍 Key Files to Modify

- `backend.clj` - Add import tracking
- `frontend-namespaced.clj` - Add require generation
- New: `dependency-graph.clj` - Graph builder
- `comprehensive_roundtrip_test.clj` - Update for namespaces
- `core.clj` - Integrate dependency analysis

## 📊 Success Criteria

1. All generated files compile without errors
2. Cross-namespace type references work correctly
3. Tests pass with new namespace structure
4. No manual intervention needed for dependencies
5. Clear dependency visualization available

## 💡 Notes

- `jon_shared_data_types.proto` is the foundation - maps to `ser.types` namespace
- Command files heavily depend on state types (enums)
- The main aggregators (`jon_shared_cmd.proto`, `jon_shared_data.proto`) import everything
- Consider generating a dependency graph visualization (graphviz?)