# Claude AI Assistant Context

## Current Architecture Status

### Refactored Clojure UI (December 2024)
The Clojure codebase has been significantly simplified to prepare for future Kotlin integration:

**Current State:**
- **UI-Only Focus**: Simplified to provide only UI components (startup dialog, main frame)
- **State Management**: New `state.clj` namespace with UI-only state atom
- **Stream Controls**: Stream toggle buttons are present but noop - ready for Kotlin integration
- **Removed Components**: All IPC, transit communication, and process management code removed
- **Preserved Components**: Java/Kotlin code intact for future integration

**Key Changes:**
- Moved app state atom to dedicated `potatoclient.state` namespace
- Fixed all namespace/file naming conventions (underscores in filenames)
- Removed `ipc.clj`, `process.clj`, and transit communication directories
- Stream control buttons remain in UI but perform no operations (logging only)
- Configuration and theme management remain functional

**Next Steps:**
- Integrate Kotlin video stream processes
- Connect stream controls to Kotlin backend
- Implement new IPC protocol between Clojure UI and Kotlin streams


## Development Tools

### I18n Translation Checker
**Location**: `tools/i18n-checker/`

A tool for checking translation completeness and consistency across locales.

**Features:**
- Finds all `i18n/tr` calls in the codebase using rewrite-clj for accurate parsing
- Identifies missing translation keys (used in code but not defined)
- Identifies unused translation keys (defined but not used in code)
- Checks consistency between locales (keys missing in some locales)
- Generates stub entries for missing keys

**Usage:**
```bash
cd tools/i18n-checker
./check.sh              # Generate report
./check.sh --stubs en   # Generate English stub entries
./check.sh --help       # Show help
```

**Runtime Detection (Dev Mode):**
When running in development mode (`make dev`), the i18n system automatically:
- Displays missing keys as `[MISSING: :key-name]` in the UI
- Logs warnings for missing keys with locale information
- Validates translation consistency on startup
- Only reports each missing key once to avoid log spam

## Important: Use Specialized Agents

### Proto-Class Explorer Agent
**ALWAYS use the `proto-class-explorer` agent when you need information about:**
- Protobuf message definitions
- Java class representations of proto messages
- buf.validate constraints on proto fields
- Proto field types and structures
- Mapping between proto messages and their Java implementations

**How to use:**
Provide full context when invoking:
```
Task: proto-class-explorer agent
Prompt: "Find information about [message/class name] in /home/jare/git/potatoclient/tools/proto-explorer"
```

The agent provides:
- Comprehensive proto message details
- Java class mappings (e.g., `cmd.Root` → `cmd.JonSharedCmd$Root`)
- Pronto EDN representations for Clojure integration
- Field information with buf.validate constraints
- Automatic handling of the 2-step proto-explorer workflow using Makefile commands

**Technical Details:**
- Tool location: `/home/jare/git/potatoclient/tools/proto-explorer`
- Uses Makefile commands: `make proto-search`, `make proto-list`, `make proto-info`
- Descriptor files: `output/json-descriptors/`
- Automatically compiles proto classes when needed

### Test Runner Analyzer Agent
**ALWAYS use the `test-runner-analyzer` agent for:**
- Running tests in the codebase
- Getting test execution reports with failure details
- Validating code changes through testing

**How to use:**
Instead of directly running test commands, use:
```
Task: test-runner-analyzer agent
Prompt: "Run tests in [working directory] using command: [test command]"
```
Example: "Run tests in /home/jare/git/potatoclient using command: lein test"

The agent provides:
- Test execution with filtered results
- Failure reporting (test name, error message, location)
- Pass/fail summary statistics

**Important Notes:**
- The agent ONLY runs tests and reports results - it does not fix tests or investigate root causes
- When reporting failures, the agent includes this directive: "Per project Testing Philosophy: Fix the failing code to make tests pass. NEVER modify, disable, or delete the tests themselves."


## Shared Module

The `shared` module (`./shared/README.md`) is the foundational library providing:
- **Protocol Buffer definitions** for all system communication (commands and state messages)
- **Malli specifications** that mirror protobuf constraints with runtime validation
- **Serialization utilities** for EDN ↔ Protobuf binary conversion
- **Single source of truth** for data structures, enums, and protocol compliance

This module ensures type safety and protocol consistency across all PotatoClient subsystems. All other modules depend on it for message definitions and validation.

## Project Principles

### Core Development Philosophy
**No Legacy, No Backward Compatibility, No Versioning**
- We are in pre-alpha stage
- Focus on building a robust, clean foundation
- Make breaking changes when needed for better architecture
- No deprecated code or compatibility layers

### Code Quality Standards
**ALL Functions MUST Have Malli Metadata:**
1. **Strict Malli Schemas** - MANDATORY for every single function
   - Every `defn` and `defn-` MUST have `:malli/schema` metadata
   - Use and expand specs from our shared registry (`potatoclient.malli.registry`)
   - Reuse common specs: `:cmd/root`, `:state/root`, `:nat-int`, `:angle-degrees`, etc.
   - Create new registry entries for repeated patterns - don't duplicate specs
   - Use Malli schemas (`:int`, `:double`, `:map`) never predicates (`int?`, `double?`)
   
   Example:
   ```clojure
   (defn process-command
     "Process a command and return result"
     {:malli/schema [:=> [:cat :cmd/root] [:map [:status :keyword] [:result any?]]]}
     [cmd]
     ...)
   ```

2. **Comprehensive Tests** - Every function must be tested
3. **Documentation** - Clear docstrings explaining purpose and usage

### Generative Testing with mi/check
With Malli schemas:
- Functions with `:malli/schema` metadata can be tested automatically
- Use `(mi/collect! {:ns ['namespace.name]})` to gather schemas
- Use `(mi/check)` for automatic generative testing of all collected functions
- This finds edge cases and constraint violations automatically
- No need to write manual `defspec` tests - schemas drive the testing

**Malli Map Specs Must Be Closed:**
- All map specs must use `{:closed true}` to reject extra keys
- This ensures strict validation and prevents unintended data from passing through
- Use `malli.util/closed-schema` when working with existing schemas

### Testing Philosophy
**NO Test Deletion or Disabling - WE MAKE THEM PASS**
- Never comment out failing tests
- Never delete tests that are inconvenient
- Fix the code, not the test
- If a test is failing, it's highlighting a real issue that needs resolution

## Development Tools

### Clj-kondo Type Config Generation
The project can automatically generate clj-kondo type configurations from Malli function schemas:

**Make Targets:**
- `make kondo-configs` - Generate type configs for entire project (root + shared modules)
- `make kondo-configs-shared` - Generate type configs for shared module only
- `cd shared && make kondo-configs` - Alternative for shared module

**How it works:**
1. Auto-discovers all Clojure namespaces in src directories
2. Loads Malli registry and UI specs
3. Collects function schemas from `:malli/schema` metadata
4. Generates clj-kondo type information for static analysis
5. Writes to `.clj-kondo/metosin/malli-types-clj/config.edn`

**Benefits:**
- Arity checking based on your function specs
- Type checking for function arguments and return values
- Better IDE integration with accurate type hints
- Catches type mismatches at edit time

Run `make kondo-configs` after adding new Malli schemas to keep type checking up to date.

## Development Guidelines

### When Working with Protobuf
1. Use `proto-class-explorer` agent for discovery and information
2. Ensure all proto changes are reflected in Java classes
3. Validate constraints are properly defined in proto files
4. Test proto serialization/deserialization thoroughly

### When Running Tests
1. Always use `test-runner-analyzer` agent
2. Run tests after every significant change
3. Ensure all tests pass before considering work complete
4. Add new tests for new functionality

### Code Organization
- Keep code modular and composable
- Prefer pure functions where possible
- Use meaningful names that reflect intent
- Document complex logic inline when necessary

## IPC Protocol Architecture

### Transit Keywords - The Foundation
**CRITICAL: Always use Transit Keywords for IPC communication**
- **Kotlin/Java side**: Use `com.cognitect.transit.Keyword` via `TransitFactory.keyword("name")`
- **Clojure side**: Automatically converts Transit Keywords to Clojure keywords
- **Never use strings** for message types, actions, or enum values - always use Transit Keywords
- This eliminates type conversion issues and ensures protocol consistency

### Implementation Notes

**Kotlin Side (`TransitKeys.kt`):**
- All message keys are pre-created Transit Keywords
- Use `TransitKeys.MSG_TYPE`, `TransitKeys.EVENT`, etc.
- Never use string literals for keys or enum values

**Clojure Side:**
- Transit Keywords automatically convert to Clojure keywords
- No manual conversion needed - `:msg-type`, `:event`, etc. work directly
- Use `case` or `cond` with keywords for message dispatch

**Key Benefits:**
1. Type safety - Transit Keywords are strongly typed
2. No string conversion bugs
3. Cleaner code on both sides
4. Better performance (no string parsing)
5. IDE autocomplete support
