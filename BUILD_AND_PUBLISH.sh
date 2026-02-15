#!/bin/bash

# SeleniumIQ Build and Publish Script
# Version: 1.0.7
# This script builds the plugin and publishes it to JetBrains Marketplace

set -e  # Exit on error

echo "=================================================="
echo "SeleniumIQ v1.0.7 - Build and Publish"
echo "=================================================="

# Check if PUBLISH_TOKEN is set
if [ -z "$PUBLISH_TOKEN" ]; then
    echo "ERROR: PUBLISH_TOKEN environment variable is not set"
    echo "Usage: export PUBLISH_TOKEN='your-token' && ./BUILD_AND_PUBLISH.sh"
    exit 1
fi

echo ""
echo "Step 1: Clean previous builds..."
./gradlew clean

echo ""
echo "Step 2: Building plugin..."
./gradlew buildPlugin

echo ""
echo "Step 3: Verifying build..."
if [ -f "build/distributions/selenium-iq-plugin-1.0.7.zip" ]; then
    echo "✓ Plugin built successfully!"
    ls -lh build/distributions/selenium-iq-plugin-1.0.7.zip
else
    echo "✗ Build failed - plugin ZIP not found"
    exit 1
fi

echo ""
echo "Step 4: Running plugin verifier..."
./gradlew runPluginVerifier

echo ""
echo "Step 5: Publishing to JetBrains Marketplace..."
./gradlew publishPlugin

echo ""
echo "=================================================="
echo "✓ Build and publish completed successfully!"
echo "=================================================="
echo ""
echo "Plugin published to JetBrains Marketplace"
echo "It may take 1-2 hours for the update to appear"
echo ""
echo "Check status at: https://plugins.jetbrains.com/plugin/XXXX-seleniumiq"
echo "=================================================="
