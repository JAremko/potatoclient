#!/bin/bash
# Download JaCoCo agent for Java/Kotlin code coverage

set -e

JACOCO_VERSION="0.8.13"
JACOCO_DIR="target"
JACOCO_AGENT="$JACOCO_DIR/jacocoagent.jar"
JACOCO_CLI="$JACOCO_DIR/jacococli.jar"

echo "Setting up JaCoCo $JACOCO_VERSION for code coverage..."

# Create target directory if it doesn't exist
mkdir -p "$JACOCO_DIR"

# Download JaCoCo agent if not present
if [ ! -f "$JACOCO_AGENT" ]; then
    echo "Downloading JaCoCo agent..."
    curl -L "https://search.maven.org/remotecontent?filepath=org/jacoco/org.jacoco.agent/$JACOCO_VERSION/org.jacoco.agent-$JACOCO_VERSION-runtime.jar" \
         -o "$JACOCO_AGENT"
    echo "JaCoCo agent downloaded to $JACOCO_AGENT"
else
    echo "JaCoCo agent already exists at $JACOCO_AGENT"
fi

# Download JaCoCo CLI for report generation
if [ ! -f "$JACOCO_CLI" ]; then
    echo "Downloading JaCoCo CLI..."
    curl -L "https://search.maven.org/remotecontent?filepath=org/jacoco/org.jacoco.cli/$JACOCO_VERSION/org.jacoco.cli-$JACOCO_VERSION-nodeps.jar" \
         -o "$JACOCO_CLI"
    echo "JaCoCo CLI downloaded to $JACOCO_CLI"
else
    echo "JaCoCo CLI already exists at $JACOCO_CLI"
fi

echo "JaCoCo setup complete!"