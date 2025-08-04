# TODO: Documentation Verification Pass

This is a second-pass verification to ensure our documentation restructuring was complete and accurate.

## Phase 1: Verify Documentation Coverage

- [ ] **Verify all source files have been considered**
  - [ ] Check all `.clj` files for inline documentation that should be extracted
  - [ ] Check all `.kt` files for important implementation details
  - [ ] Check `resources/` for configuration files that need documentation
  - [ ] Check `scripts/` for any tools or utilities needing docs

- [ ] **Verify all tools are documented**
  - [ ] Check `tools/` directory for any undocumented tools
  - [ ] Verify proto-explorer documentation is complete
  - [ ] Verify mock-video-stream documentation is complete  
  - [ ] Verify guardrails-check documentation is complete
  - [ ] Check for any Babashka scripts needing documentation

- [ ] **Verify build system documentation**
  - [ ] Check Makefile for any undocumented targets
  - [ ] Verify deps.edn aliases are documented
  - [ ] Check for any build scripts in `scripts/`
  - [ ] Verify CI/CD setup is documented

## Phase 2: Cross-Reference and Accuracy

- [ ] **Verify all cross-references work**
  - [ ] Check all internal links in documentation
  - [ ] Verify file paths in examples are correct
  - [ ] Check that code examples actually work
  - [ ] Verify command examples match current implementation

- [ ] **Verify technical accuracy**
  - [ ] Re-check claim about 23 functions without Guardrails
  - [ ] Verify all protobuf commands listed are implemented
  - [ ] Check that Transit protocol examples are current
  - [ ] Verify video stream specifications (resolution, FPS)

- [ ] **Check for outdated information**
  - [ ] Search for version numbers that might be outdated
  - [ ] Look for references to removed features
  - [ ] Check for old TODO items in code comments
  - [ ] Verify all external links still work

## Phase 3: Completeness Check

- [ ] **Verify nothing was lost in migration**
  - [ ] Compare old .claude/ content with new docs/ content
  - [ ] Check that all important details were preserved
  - [ ] Verify no unique information was accidentally dropped
  - [ ] Ensure all code examples were migrated

- [ ] **Check for missing documentation**
  - [ ] Document any configuration files in resources/
  - [ ] Document any important scripts
  - [ ] Check for undocumented public APIs
  - [ ] Verify all user-facing features are documented

- [ ] **Verify consistency**
  - [ ] Check that terminology is consistent across docs
  - [ ] Verify formatting is consistent
  - [ ] Check that examples follow same patterns
  - [ ] Ensure all docs have proper headers/footers

## Phase 4: Integration and Testing

- [ ] **Test all documented procedures**
  - [ ] Test getting started guide from scratch
  - [ ] Test adding a new language following the guide
  - [ ] Test adding a new theme following the guide
  - [ ] Test debugging subprocess following the guide

- [ ] **Verify tool documentation**
  - [ ] Run all proto-explorer examples
  - [ ] Test mock-video-stream scenarios
  - [ ] Run guardrails-check and verify output

- [ ] **Check build documentation**
  - [ ] Verify all make targets work as documented
  - [ ] Test release build process
  - [ ] Verify platform-specific builds

## Phase 5: Final Review

- [ ] **Review main entry points**
  - [ ] README.md - clear and welcoming?
  - [ ] CLAUDE.md - comprehensive for AI context?
  - [ ] docs/README.md - good navigation?
  - [ ] Getting started guide - smooth onboarding?

- [ ] **Check for clarity**
  - [ ] Are complex concepts well explained?
  - [ ] Are there enough examples?
  - [ ] Is the navigation intuitive?
  - [ ] Are error scenarios covered?

- [ ] **Verify principles are captured**
  - [ ] "No legacy support" principle clear?
  - [ ] "Clean architecture" explained?
  - [ ] "Keywords everywhere" documented?
  - [ ] Performance considerations noted?

## Phase 6: Cleanup and Archive

- [ ] **Final cleanup tasks**
  - [ ] Remove any temporary verification files
  - [ ] Delete any backup copies made during migration
  - [ ] Clean up any test artifacts
  - [ ] Update .gitignore if needed

- [ ] **Create final summary**
  - [ ] Document what was found and fixed
  - [ ] Note any remaining concerns
  - [ ] Create changelog of documentation updates
  - [ ] Update documentation version/date

- [ ] **Archive this TODO**
  - [ ] Move this file to docs/archive/
  - [ ] Mark completion date
  - [ ] Add summary of findings
  - [ ] Close any related issues

## Notes

- Focus on accuracy over perfection
- Document any findings in a summary file
- If new issues are found, create separate TODOs
- Remember: we don't care about legacy/versioning

## Completion Checklist

- [ ] All phases completed
- [ ] Summary document created
- [ ] Any new TODOs created for findings
- [ ] This TODO moved to archive
- [ ] Team notified of completion

---

**Started**: [Date]  
**Target**: [Date]  
**Completed**: [Date]