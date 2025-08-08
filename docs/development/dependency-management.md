# Dependency Management

This guide covers how to manage and update dependencies in PotatoClient.

## Quick Start

```bash
# Check for outdated dependencies
make deps-outdated

# Interactively upgrade dependencies
make deps-upgrade

# Upgrade all dependencies (use with caution)
make deps-upgrade-all
```

## Overview

PotatoClient uses [antq](https://github.com/liquidz/antq) for dependency management. This tool:
- Scans `deps.edn` for outdated dependencies
- Shows available updates in a clear table format
- Supports interactive and batch upgrades
- Downloads new dependencies automatically

## Commands

### Check for Outdated Dependencies

```bash
make deps-outdated
```

This command:
- Scans all dependencies in `deps.edn`
- Shows which dependencies have newer versions available
- Displays current vs latest versions in a table
- Does not modify any files

### Interactive Upgrade

```bash
make deps-upgrade
```

This command:
- Shows each outdated dependency one by one
- Lets you choose which to upgrade (y/n for each)
- Automatically updates `deps.edn`
- Downloads the new dependencies

### Batch Upgrade All

```bash
make deps-upgrade-all
```

⚠️ **Use with caution!** This command:
- Upgrades ALL outdated dependencies at once
- Requires confirmation before proceeding
- May introduce breaking changes
- Always test thoroughly after using

## Best Practices

### Before Upgrading

1. **Check current state**: Ensure all tests pass before upgrading
   ```bash
   make test
   ```

2. **Review changes**: Check what will be upgraded
   ```bash
   make deps-outdated
   ```

3. **Commit current state**: Always commit before major upgrades
   ```bash
   git add -A && git commit -m "Before dependency upgrade"
   ```

### During Upgrade

1. **Use interactive mode** for production projects:
   ```bash
   make deps-upgrade
   ```

2. **Upgrade incrementally**: Update related dependencies together
   - Protobuf libraries (protobuf-java, protobuf-java-util)
   - Kotlin libraries (kotlin-stdlib, kotlin-reflect, kotlinx-coroutines)
   - Jackson libraries (jackson-core, jackson-databind)

3. **Check compatibility**: Some dependencies must match versions:
   - `protobuf-java` and `protobuf-java-util` should use the same version
   - Kotlin libraries should generally use the same major version

### After Upgrading

1. **Clean and rebuild**:
   ```bash
   make clean
   make build
   ```

2. **Run tests**:
   ```bash
   make test
   ```

3. **Test proto-explorer** (if protobuf was updated):
   ```bash
   make proto-search QUERY=root
   ```

4. **Test development mode**:
   ```bash
   make dev
   ```

## Important Dependencies

### Core Dependencies

These require extra care when upgrading:

- **Clojure**: Currently 1.12.1
- **Protobuf**: 4.31.1 (protobuf-java and protobuf-java-util must match)
- **Pronto**: From GitHub, SHA-pinned for stability
- **GStreamer**: 1.4.0 (system-dependent, test video playback after upgrading)

### Version Constraints

Some dependencies have specific requirements:

- **Java**: Exactly version 17 (not 17+)
- **Kotlin**: All Kotlin libs should use the same major version
- **Transit**: transit-clj and transit-java work together

## Troubleshooting

### Dependency Resolution Errors

If you see "Could not find artifact" errors:

1. Check if the version exists on Maven Central
2. Try a slightly older version
3. Check for typos in version numbers

### Build Failures After Upgrade

If the build fails after upgrading:

1. Check for breaking changes in the library's changelog
2. Review deprecation warnings
3. Consider reverting specific problematic upgrades

### Proto-Explorer Issues

If proto-explorer stops working after upgrades:

1. Ensure protobuf versions match between main project and proto-explorer
2. Regenerate proto files if needed: `make proto`
3. Check protovalidate compatibility

## Manual Dependency Management

If you need to check or update dependencies manually:

```bash
# Direct antq usage
clojure -M:outdated

# Check specific dependency versions
clojure -X:deps find-versions :lib org.clojure/clojure

# Force download of dependencies
clojure -P
```

## CI/CD Considerations

For automated environments:

- Use `make deps-outdated` in CI to detect available updates
- Never use `make deps-upgrade-all` in CI without manual review
- Consider pinning critical dependencies for production stability
- Set up notifications for security updates

## Related Documentation

- [Getting Started](getting-started.md)
- [Code Standards](code-standards.md)
- [Proto Explorer](../tools/proto-explorer.md)