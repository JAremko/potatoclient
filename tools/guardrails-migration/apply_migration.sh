#!/bin/bash

# Script to backup original files and apply migration

BACKUP_DIR="backup-$(date +%Y%m%d-%H%M%S)"
MIGRATED_DIR="migrated"
PROJECT_ROOT="../.."

echo "Creating backup directory: $BACKUP_DIR"
mkdir -p "$BACKUP_DIR"

# Files to migrate with their correct paths
declare -a FILES=(
    "src/potatoclient/config.clj:config.clj"
    "src/potatoclient/state.clj:state.clj"
    "src/potatoclient/theme.clj:theme.clj"
    "src/potatoclient/ui/startup_dialog.clj:startup_dialog.clj"
    "src/potatoclient/ui/main_frame.clj:main_frame.clj"
    "src/potatoclient/logging.clj:logging.clj"
    "src/potatoclient/i18n.clj:i18n.clj"
    "src/potatoclient/main.clj:main.clj"
    "src/potatoclient/dev_instrumentation.clj:dev_instrumentation.clj"
    "src/potatoclient/ui_specs.clj:ui_specs.clj"
    "shared/src/potatoclient/malli/registry.clj:registry.clj"
)

echo "Backing up original files..."
for file_mapping in "${FILES[@]}"; do
    IFS=':' read -r original_path migrated_name <<< "$file_mapping"
    full_path="$PROJECT_ROOT/$original_path"
    
    if [ -f "$full_path" ]; then
        cp "$full_path" "$BACKUP_DIR/$(basename $original_path)"
        echo "  ✓ Backed up: $original_path"
    else
        echo "  ✗ Not found: $original_path"
    fi
done

echo ""
echo "Applying migration..."
for file_mapping in "${FILES[@]}"; do
    IFS=':' read -r original_path migrated_name <<< "$file_mapping"
    full_path="$PROJECT_ROOT/$original_path"
    migrated_file="$MIGRATED_DIR/$migrated_name"
    
    if [ -f "$migrated_file" ]; then
        cp "$migrated_file" "$full_path"
        echo "  ✓ Replaced: $original_path"
    else
        echo "  ✗ Migrated file not found: $migrated_name"
    fi
done

echo ""
echo "Migration complete!"
echo "Backup saved in: $BACKUP_DIR"
echo ""
echo "Next steps:"
echo "1. Run 'make test' in the project root to verify everything works"
echo "2. If tests fail, restore from backup: cp $BACKUP_DIR/* [original locations]"
echo "3. Commit the changes if tests pass"