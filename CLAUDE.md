# Claude AI Assistant Context

## Important: Use Specialized Agents

### Proto-Class Explorer Agent
**ALWAYS use the `proto-class-explorer` agent when you need information about:**
- Protobuf message definitions
- Java class representations of proto messages
- buf.validate constraints on proto fields
- Proto field types and structures
- Mapping between proto messages and their Java implementations

**How to use:**
Instead of directly using proto-explorer commands or searching for proto/Java files, use:
```
Task: proto-class-explorer agent
Prompt: "Find information about [message/class name]"
```

The agent provides:
- Comprehensive proto message details
- Java class mappings (e.g., `cmd.Root` → `cmd.JonSharedCmd$Root`)
- Pronto EDN representations for Clojure integration
- Field information with buf.validate constraints
- Automatic handling of the 2-step proto-explorer workflow

### Test Runner Analyzer Agent
**ALWAYS use the `test-runner-analyzer` agent for:**
- Running any tests in the codebase
- Analyzing test failures
- Getting comprehensive test execution reports
- Validating code changes through testing

**How to use:**
Instead of directly running test commands, use:
```
Task: test-runner-analyzer agent
Prompt: "Run tests for [module/feature]" or "Run all tests and analyze failures"
```

The agent provides:
- Automatic test discovery and execution
- Detailed failure analysis
- Test coverage information
- Suggestions for fixing failures

### Guardrails Scanner Agent
**ALWAYS use the `guardrails-scanner` agent for:**
- Finding Clojure functions using `defn`/`defn-` instead of Guardrails' `>defn`/`>defn-`
- Checking Guardrails adoption statistics across the codebase
- Identifying namespaces with unspecced functions
- Searching for specific function patterns that lack Guardrails

**How to use:**
Instead of manually searching for functions without Guardrails, use:
```
Task: guardrails-scanner agent
Prompt: "Find all functions not using Guardrails" or "Check which namespaces have unspecced functions"
```

The agent provides:
- List of functions using raw `defn` that should use `>defn`
- Statistics on Guardrails adoption percentage
- Namespace-grouped reports of unspecced functions
- Recommendations to convert functions to use Guardrails

**Note**: This tool specifically checks for Guardrails library usage (runtime validation), not general security vulnerabilities or error handling.

## Project Principles

### Core Development Philosophy
**No Legacy, No Backward Compatibility, No Versioning**
- We are in pre-alpha stage
- Focus on building a robust, clean foundation
- Make breaking changes when needed for better architecture
- No deprecated code or compatibility layers

### Code Quality Standards
**All Functions Must Have:**
1. **Guardrails** - Input validation and error handling
2. **Malli Specs** - Type specifications for all functions
3. **Comprehensive Tests** - Every function must be tested

### Testing Philosophy
**NO Test Deletion or Disabling - WE MAKE THEM PASS**
- Never comment out failing tests
- Never delete tests that are inconvenient
- Fix the code, not the test
- If a test is failing, it's highlighting a real issue that needs resolution

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