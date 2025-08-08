# CMD-Explorer Implementation Plan

## Preamble - Implementation Strategy and Guidelines

### Overview
CMD-Explorer is a comprehensive testing and validation tool for WebSocket command endpoints. It implements all TypeScript cmd functions with full protobuf validation, generative testing, and buf.validate constraint enforcement.

### Architecture Overview

The tool will use a multimethod-based dispatch system for sending commands, where:
1. Each command function accepts a payload map and constructs the appropriate protobuf message
2. A central dispatcher routes commands based on the payload type (oneof field)
3. All functions are guardrails-protected with Malli specs
4. Validation happens at multiple levels: Malli specs, buf.validate constraints, and protobuf parsing

### Key Technologies and References

#### Proto-Explorer Usage (CRITICAL TOOL)

**Proto-Explorer is MANDATORY for understanding message structures and constraints.**

Every function implementation MUST start with proto-explorer to:
1. Understand exact message structure
2. Discover buf.validate constraints
3. Identify required vs optional fields
4. Understand nested message relationships
5. Find enum values and their constraints

```bash
cd /tools/proto-explorer

# List all cmd message types
./proto-explorer cmd

# Show Root message structure with validation rules  
./proto-explorer cmd Root

# Show specific command structure
./proto-explorer cmd DayCamera.Root
./proto-explorer cmd DayCamera.Zoom

# Always explore nested messages
./proto-explorer cmd RotaryPlatform.Axis
./proto-explorer cmd RotaryPlatform.Azimuth
```

**Never implement a function without first exploring its protobuf structure!**

#### Pronto Documentation
- Main docs: https://raw.githubusercontent.com/JAremko/pronto/refs/heads/master/README.md
- Performance: https://raw.githubusercontent.com/JAremko/pronto/refs/heads/master/doc/performance.md
- Key features: Automatic EDN<->Protobuf conversion, validation support, nested message handling

#### Guardrails with Malli
- Documentation: https://raw.githubusercontent.com/fulcrologic/guardrails/refs/heads/main/README.adoc
- Use `com.fulcrologic.guardrails.malli` namespace for Malli integration
- Every function must have >? and <? annotations for input/output validation

#### Buf.validate Constraints
Java protobuf classes contain validation annotations that we must respect:
- Field constraints (min/max values, string patterns, required fields)
- Oneof constraints (exactly one field must be set)
- Custom CEL expressions for complex validation

### TypeScript Reference Implementation
Location: `/home/jare/git/potatoclient/examples/web/frontend/ts/cmd/`

Key files with command bindings to implement:
- `cmdSender/cmdDayCamera.ts` - Day camera commands (29 functions)
- `cmdSender/cmdHeatCamera.ts` - Heat camera commands (31 functions)
- `cmdSender/cmdRotary.ts` - Rotary platform commands (40 functions)
- `cmdSender/cmdLRF.ts` - Laser rangefinder commands (14 functions)
- `cmdSender/cmdCompass.ts` - Compass commands (11 functions)
- `cmdSender/cmdGps.ts` - GPS commands (5 functions)
- `cmdSender/cmdSystem.ts` - System commands (11 functions)
- `cmdSender/cmdOSD.ts` - OSD commands (8 functions)
- `cmdSender/cmdCamDayGlassHeater.ts` - Glass heater commands (5 functions)
- `cmdSender/cmdCV.ts` - Computer vision commands (8 functions)
- `cmdSender/cmdLRFAlignment.ts` - LRF alignment commands (10 functions)

Note: `cmdSenderShared.ts` contains TypeScript-specific infrastructure (message creation, encoding, WebSocket handling) that will be replaced by our Clojure implementation using Pronto and our WebSocket client.

### Spec-Driven Development with Guardrails

**Key Design Decision**: Unlike TypeScript functions that return void and send messages as side effects, our Clojure functions will:
1. Return the complete CMD protobuf tree as EDN (using Pronto format)
2. Enable testing without side effects
3. Allow inspection and validation of generated messages
4. Separate message construction from sending

**Guardrails Integration**:
- Every function will have a preceding `def` with Malli specs
- Custom generators attached to specs for property-based testing
- Guardrails instrumentation enabled during tool operation
- Direct testing of guardrailed functions with generated inputs

Example structure:
```clojure
;; Spec definition with custom generator
(def day-camera-zoom-spec
  [:map 
   [:value [:double {:min 0.0 :max 1.0 
                    :gen/gen (gen/double* {:min 0.0 :max 1.0})}]]])

;; Guardrailed function
(>defn day-camera-set-zoom
  [zoom-value]
  [day-camera-zoom-spec => cmd-root-spec]  ; Returns full CMD tree
  {:protocolVersion 1
   :dayCamera {:zoom {:value zoom-value}}})
```

**Shared Specs Directory**:
Create `src/cmd_explorer/specs/shared.clj` for reusable specs:
- Angle specs (azimuth, elevation, bank)
- Range specs (0-1 normalized values)
- Position specs (lat/lon/alt)
- Common enums (localization, modes)
- Full CMD root spec with oneof validation

**Return Value Spec**:
All functions return a complete CMD root message as EDN:
- Must validate against `cmd-root-spec` (using custom oneof spec)
- Exactly one payload field populated
- All required fields present
- Ready for Pronto serialization

### Implementation Flow

1. **Pre-Implementation Research (MANDATORY for EVERY function)**
   - **Read TypeScript source**: Examine the exact implementation in `/examples/web/frontend/ts/cmd/`
   - **Use proto-explorer**: Run `./proto-explorer cmd <MessageType>` to understand:
     - Message structure and field types
     - Nested message requirements
     - Buf.validate constraints on each field
     - Oneof groups and required fields
   - **Document findings**: Note any special constraints or business logic
   
   Example workflow:
   ```bash
   # For implementing day-camera-set-zoom
   1. Read: /examples/web/frontend/ts/cmd/cmdSender/cmdDayCamera.ts:99
   2. Explore: ./proto-explorer cmd DayCamera.Root
   3. Explore: ./proto-explorer cmd DayCamera.Zoom
   4. Note: zoom value constraint is [0.0, 1.0] from buf.validate
   ```

2. **Function Implementation**
   - Define Malli spec with custom generators based on proto constraints
   - Create guardrailed function with >defn
   - Add >? input spec and <? output spec annotations
   - Return complete CMD protobuf tree as EDN matching proto structure
   - No side effects in command construction

3. **Testing Strategy (for EVERY function)**
   - Exercise guardrailed function directly with generated inputs
   - Simple value tests (positive cases)
   - Negative tests (invalid inputs)
   - Boundary tests (min/max values from buf.validate discovered via proto-explorer)
   - Generative tests (minimum 300 runs using spec generators)
   - Validate returned EDN structure matches proto definition

3. **Validation Layers**
   - Input validation via Malli specs
   - Buf.validate constraint checking
   - Protobuf serialization validation
   - Mock WebSocket response validation

4. **Multimethod Dispatch**
   - Dispatch on payload type (oneof field name)
   - Each method constructs appropriate nested message
   - Central send-command function handles common fields

### Custom Oneof Spec
Copy and adapt `/home/jare/git/potatoclient/src/potatoclient/specs/malli_oneof.clj`:
- Rename to `oneof_payload.clj`
- Ensure only one field is populated in oneof messages
- Generate valid oneof values for testing
- Integrate with Pronto's message generation

### Testing Infrastructure

1. **Mock WebSocket Server**
   - Receives binary protobuf messages
   - Deserializes and validates using buf.validate
   - Returns success/error responses
   - Logs all received commands for verification

2. **Generative Testing Requirements**
   - Custom generators respecting buf.validate annotations
   - Minimum 300 test runs per function
   - Property: "valid inputs always produce valid protobuf"
   - Property: "invalid inputs always fail validation"

3. **Validation Checkpoints**
   - Pre-send: Malli spec validation
   - Serialization: Protobuf encoding success
   - Buf.validate: Constraint satisfaction
   - Mock response: Server acceptance

## Implementation Todos

### Phase 1: Project Setup
- [ ] Create deps.edn with dependencies (pronto, malli, guardrails, hato, protobuf, test.check)
- [ ] Copy and adapt malli_oneof.clj as oneof_payload.clj
- [ ] Set up project structure (src/, test/, resources/, src/cmd_explorer/specs/)
- [ ] Create Makefile with test and run targets
- [ ] Copy protobuf classes from main project (like state-explorer)
- [ ] Set up logging configuration
- [ ] Configure guardrails.edn with instrumentation settings
- [ ] Enable guardrails instrumentation in dev/test profiles

### Phase 2: Core Infrastructure
- [ ] Create shared specs directory (src/cmd_explorer/specs/shared.clj)
- [ ] Define cmd-root-spec with oneof validation
- [ ] Define common reusable specs (angles, ranges, positions, enums)
- [ ] Attach custom generators to all specs
- [ ] Implement WebSocket client module (use state-explorer as reference)
- [ ] Create mock WebSocket server for testing
- [ ] Implement protobuf message builder utilities
- [ ] Create multimethod dispatcher for command routing
- [ ] Implement base command function with common fields
- [ ] Set up buf.validate constraint extraction from proto classes
- [ ] Create test harness for exercising guardrailed functions

### Phase 3: Core Command Functions
- [ ] Implement send-cmd-ping (standalone ping command)
- [ ] Implement send-cmd-frozen (standalone frozen command)
- [ ] Add comprehensive tests for core commands

### Implementation Checklist Template

For EACH function, complete ALL steps:
- [ ] Read TypeScript source at specified line
- [ ] Run proto-explorer on relevant message types
- [ ] Document constraints from buf.validate
- [ ] Define Malli spec with generators
- [ ] Implement guardrailed function
- [ ] Add unit tests (positive, negative, boundary)
- [ ] Add generative tests (300+ runs)
- [ ] Test with mock server
- [ ] Verify binary compatibility

### Phase 4: Day Camera Commands (cmdDayCamera.ts - 29 functions)
- [ ] day-camera-set-infrared-filter (line 5)
  - [ ] Read TS source
  - [ ] Proto-explore: DayCamera.Root, DayCamera.InfraRedFilter
  - [ ] Document: boolean field, no special constraints
  - [ ] Implement with specs
  - [ ] Test thoroughly
- [ ] day-camera-set-iris (line 12)
  - [ ] Read TS source
  - [ ] Proto-explore: DayCamera.Root, DayCamera.Iris
  - [ ] Document: numeric constraints
  - [ ] Implement with specs
  - [ ] Test thoroughly
- [ ] day-camera-set-auto-iris (line 19)
- [ ] day-camera-take-photo (line 26)
- [ ] day-camera-start (line 37)
- [ ] day-camera-stop (line 44)
- [ ] day-camera-set-focus (line 51)
- [ ] day-camera-move-focus (line 59)
- [ ] day-camera-halt-focus (line 67)
- [ ] day-camera-offset-focus (line 75)
- [ ] day-camera-reset-focus (line 83)
- [ ] day-camera-save-focus-to-table (line 91)
- [ ] day-camera-set-zoom (line 99)
- [ ] day-camera-move-zoom (line 107)
- [ ] day-camera-halt-zoom (line 115)
- [ ] day-camera-offset-zoom (line 123)
- [ ] day-camera-reset-zoom (line 131)
- [ ] day-camera-save-zoom-to-table (line 139)
- [ ] day-camera-set-zoom-table-value (line 147)
- [ ] day-camera-set-digital-zoom-level (line 155)
- [ ] day-camera-next-zoom-table-pos (line 163)
- [ ] day-camera-prev-zoom-table-pos (line 171)
- [ ] day-camera-get-meteo (line 179)
- [ ] day-camera-set-fx-mode (line 186)
- [ ] day-camera-next-fx-mode (line 193)
- [ ] day-camera-prev-fx-mode (line 200)
- [ ] day-camera-set-clahe-level (line 207)
- [ ] day-camera-shift-clahe-level (line 214)
- [ ] Add comprehensive tests for all day camera functions

### Phase 5: Heat Camera Commands (cmdHeatCamera.ts - 31 functions)
- [ ] heat-camera-take-photo (line 5)
- [ ] heat-camera-set-agc (line 16)
- [ ] heat-camera-set-filter (line 23)
- [ ] heat-camera-set-zoom-table-value (line 30)
- [ ] heat-camera-set-digital-zoom-level (line 38)
- [ ] string-to-heat-camera-agc-mode (line 45)
- [ ] string-to-heat-camera-filter (line 58)
- [ ] heat-camera-calibrate (line 71)
- [ ] heat-camera-start (line 78)
- [ ] heat-camera-stop (line 85)
- [ ] heat-camera-zoom-in (line 92)
- [ ] heat-camera-zoom-out (line 99)
- [ ] heat-camera-zoom-stop (line 106)
- [ ] heat-camera-set-auto-focus-on (line 120)
- [ ] heat-camera-set-auto-focus-off (line 125)
- [ ] heat-camera-focus-stop (line 130)
- [ ] heat-camera-focus-in (line 137)
- [ ] heat-camera-focus-out (line 144)
- [ ] heat-camera-focus-step-plus (line 151)
- [ ] heat-camera-focus-step-minus (line 158)
- [ ] heat-camera-next-zoom-table-pos (line 165)
- [ ] heat-camera-prev-zoom-table-pos (line 173)
- [ ] heat-camera-reset-zoom (line 181)
- [ ] heat-camera-save-zoom-to-table (line 188)
- [ ] heat-camera-get-meteo (line 195)
- [ ] heat-camera-enable-dde (line 202)
- [ ] heat-camera-disable-dde (line 209)
- [ ] heat-camera-set-dde-level (line 216)
- [ ] heat-camera-shift-dde-level (line 223)
- [ ] heat-camera-set-fx-mode (line 230)
- [ ] heat-camera-next-fx-mode (line 237)
- [ ] heat-camera-prev-fx-mode (line 244)
- [ ] heat-camera-set-clahe-level (line 251)
- [ ] heat-camera-shift-clahe-level (line 258)
- [ ] Add comprehensive tests for all heat camera functions

### Phase 6: Rotary Platform Commands (cmdRotary.ts - 40 functions)
- [ ] Implement all 40 rotary platform functions
- [ ] Add comprehensive tests for each function
- [ ] Validate azimuth/elevation constraints
- [ ] Test velocity and position commands

### Phase 7: LRF Commands (cmdLRF.ts - 14 functions)
- [ ] lrf-start (line 5)
- [ ] lrf-stop (line 12)
- [ ] lrf-new-session (line 19)
- [ ] lrf-scan-on (line 26)
- [ ] lrf-refine-on (line 33)
- [ ] lrf-refine-off (line 40)
- [ ] lrf-scan-off (line 47)
- [ ] lrf-measure (line 54)
- [ ] lrf-enable-fog-mode (line 69)
- [ ] lrf-disable-fog-mode (line 76)
- [ ] lrf-target-designator-off (line 83)
- [ ] lrf-target-designator-on-mode-a (line 90)
- [ ] lrf-target-designator-on-mode-b (line 97)
- [ ] lrf-get-meteo (line 104)
- [ ] Add comprehensive tests for all LRF functions

### Phase 8: Compass Commands (cmdCompass.ts - 11 functions)
- [ ] compass-start (line 4)
- [ ] compass-get-meteo (line 11)
- [ ] compass-stop (line 19)
- [ ] compass-set-magnetic-declination (line 26)
- [ ] compass-set-offset-angle-azimuth (line 33)
- [ ] compass-set-offset-angle-elevation (line 40)
- [ ] compass-calibrate-long-start (line 47)
- [ ] compass-calibrate-short-start (line 54)
- [ ] compass-calibrate-next (line 61)
- [ ] compass-calibrate-cancel (line 68)
- [ ] compass-set-use-rotary-position (line 75)
- [ ] Add comprehensive tests for all compass functions

### Phase 9: GPS Commands (cmdGps.ts - 5 functions)
- [ ] gps-start (line 4)
- [ ] gps-stop (line 11)
- [ ] gps-set-manual-position (line 18)
- [ ] gps-set-use-manual-position (line 30)
- [ ] gps-get-meteo (line 38)
- [ ] Add comprehensive tests for all GPS functions

### Phase 10: System Commands (cmdSystem.ts - 11 functions)
- [ ] system-reboot (line 5)
- [ ] system-power-off (line 12)
- [ ] system-reset-configs (line 19)
- [ ] system-start-all (line 26)
- [ ] system-stop-all (line 33)
- [ ] system-mark-rec-important (line 40)
- [ ] system-unmark-rec-important (line 47)
- [ ] system-set-localization (line 54)
- [ ] system-enter-transport (line 61)
- [ ] system-enable-geodesic-mode (line 68)
- [ ] system-disable-geodesic-mode (line 75)
- [ ] Add comprehensive tests for all system functions

### Phase 11: OSD Commands (cmdOSD.ts - 8 functions)
- [ ] osd-show-default-screen (line 5)
- [ ] osd-show-lrf-measure-screen (line 12)
- [ ] osd-show-lrf-result-screen (line 19)
- [ ] osd-show-lrf-result-simplified-screen (line 26)
- [ ] osd-disable-day-osd (line 33)
- [ ] osd-disable-heat-osd (line 40)
- [ ] osd-enable-day-osd (line 47)
- [ ] osd-enable-heat-osd (line 54)
- [ ] Add comprehensive tests for all OSD functions

### Phase 12: Glass Heater Commands (cmdCamDayGlassHeater.ts - 5 functions)
- [ ] day-camera-glass-heater-start (line 4)
- [ ] day-camera-glass-heater-stop (line 12)
- [ ] day-camera-glass-heater-turn-on (line 20)
- [ ] day-camera-glass-heater-turn-off (line 28)
- [ ] day-camera-glass-heater-get-meteo (line 36)
- [ ] Add comprehensive tests for all glass heater functions

### Phase 13: CV Commands (cmdCV.ts - 8 functions)
- [ ] Implement all 8 CV commands
- [ ] Add comprehensive tests for each function
- [ ] Validate CV-specific constraints

### Phase 14: LRF Alignment Commands (cmdLRFAlignment.ts - 10 functions)
- [ ] Implement all 10 LRF alignment commands
- [ ] Add comprehensive tests for each function
- [ ] Validate alignment-specific constraints

### Phase 15: Integration Testing
- [ ] Create end-to-end test suite
- [ ] Test command sequences
- [ ] Test error handling and recovery
- [ ] Performance testing with multiple commands
- [ ] Validate against real WebSocket endpoint

### Phase 16: CLI Interface
- [ ] Create main entry point with command selection
- [ ] Add interactive REPL mode
- [ ] Implement batch command execution
- [ ] Add command history and replay
- [ ] Create help system with examples

### Phase 17: Documentation
- [ ] Write comprehensive README
- [ ] Document all functions with examples
- [ ] Create validation rules reference
- [ ] Add troubleshooting guide
- [ ] Generate API documentation

### Phase 18: Performance Optimization
- [ ] Profile command serialization
- [ ] Optimize protobuf encoding
- [ ] Implement command batching
- [ ] Add connection pooling
- [ ] Benchmark against TypeScript implementation

## Validation Stages

### Stage 1: Input Validation
- Malli spec checking on function entry
- Type coercion where appropriate
- Range validation from buf.validate
- Required field checking

### Stage 2: Message Construction
- Proper oneof field selection
- Nested message building
- Default value handling
- Field presence validation

### Stage 3: Serialization
- Protobuf encoding success
- Binary size validation
- Round-trip testing (encode/decode)
- Wire format verification

### Stage 4: Constraint Validation
- Buf.validate rule checking
- CEL expression evaluation
- Cross-field validation
- Business logic constraints

### Stage 5: Transport Validation
- WebSocket connection success
- Message delivery confirmation
- Response parsing
- Error handling

## Success Criteria

1. **100% Function Coverage**: All TypeScript command binding functions implemented (excluding infrastructure from cmdSenderShared.ts)
2. **Comprehensive Testing**: >300 generative tests per function
3. **Full Validation**: All buf.validate constraints enforced
4. **Type Safety**: Malli specs for all inputs/outputs
5. **Error Handling**: Graceful failure with clear messages
6. **Performance**: <10ms per command serialization
7. **Documentation**: Every function documented with examples
8. **Compatibility**: Binary-compatible with TypeScript implementation

## Notes

- Use proto-explorer extensively to understand message shapes
- Reference state-explorer for WebSocket implementation patterns
- Keep functions signatures similar to TypeScript for consistency
- Prioritize validation and testing over features
- Focus on developer experience with clear error messages