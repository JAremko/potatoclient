# Legacy Code Cleanup List

## Files to Remove

### Legacy Spec Files (src/potatoclient/specs/)
- [ ] `src/potatoclient/specs/cmd/` - entire directory (legacy command specs)
  - [ ] `rotary.clj`
- [ ] `src/potatoclient/specs/data/` - entire directory (legacy data specs)
  - [ ] `camera.clj`
  - [ ] `compass.clj` 
  - [ ] `gps.clj`
  - [ ] `lrf.clj`
  - [ ] `rotary.clj`
  - [ ] `state.clj`
  - [ ] `system.clj`
  - [ ] `time.clj`
  - [ ] `types.clj`
- [ ] `src/potatoclient/specs/proto_type_mapping_nested.clj` - legacy proto mapping
- [ ] `src/potatoclient/specs/transit_messages.clj` - old transit message specs

### Legacy Test Files
- [ ] `test/potatoclient/protobuf_handler_test.clj`
- [ ] `test/potatoclient/transit_handler_test.clj`
- [ ] `test/potatoclient/transit_handlers_working_test.clj`
- [ ] `test/potatoclient/transit_keyword_test.clj`
- [ ] `test/potatoclient/transit_minimal_test.clj`
- [ ] `test/potatoclient/transit_simple_test.clj`
- [ ] `test/potatoclient/transit/keyword_conversion_test.clj`
- [ ] `test/potatoclient/transit/metadata_command_test.clj`
- [ ] `test/potatoclient/transit/kotlin_integration_test.clj` (uses old format)

### Skipped Test Files (already disabled)
- [ ] All `.clj.skip` files can be deleted

## Code to Clean Up in src/potatoclient/specs.clj

### Remove Imports
```clojure
(cmd JonSharedCmd$Root$Builder)
(cmd.RotaryPlatform JonSharedCmdRotary$Axis$Builder JonSharedCmdRotary$Azimuth$Builder JonSharedCmdRotary$Elevation$Builder)
(com.google.protobuf Message Message$Builder)
(ser JonSharedDataTypes$JonGuiDataFxModeDay JonSharedDataTypes$JonGuiDataFxModeHeat ...)
```

### Remove Legacy Specs
- [ ] Lines 556-673: All protobuf instance specs (protobuf-message-builder, rotary-direction-enum, etc.)
- [ ] Any specs that check for protobuf Java class instances

## Reasons for Removal

1. **Replaced by Generated Specs**: Proto-explorer now generates all protobuf-related specs
2. **Old Command Format**: These use the action/params format we've replaced
3. **Manual Protobuf Handling**: We now use static code generation
4. **Redundant Tests**: New comprehensive tests cover all functionality

## What to Keep

- Core domain specs in `specs.clj` (theme-key, domain, locale, etc.)
- UI-related specs (Swing components, etc.)
- Transit message envelope specs
- App-db structure specs
- New test files like `command_roundtrip_test.clj`

## Next Steps

1. Remove all files listed above
2. Clean up imports and legacy specs in `specs.clj`
3. Run tests to ensure nothing breaks
4. Update any remaining references