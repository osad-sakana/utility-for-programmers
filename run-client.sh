#!/usr/bin/env bash
#
# Convenience launcher for the development Minecraft client (Fabric 26.2)
# with this mod loaded from source.
#
#   ./run-client.sh
#
# First run downloads game assets (a few minutes); later runs are quick.
# It uses an offline dev account, so use it for singleplayer testing.
#
set -euo pipefail
cd "$(dirname "$0")"

# If JAVA_HOME isn't already set, try to point it at a JDK 25 install. This is
# only needed to launch the Gradle daemon itself; the actual compile/run
# toolchain (also JDK 25) is auto-provisioned separately by Gradle's foojay
# resolver, so an unset JAVA_HOME here is not fatal — Gradle falls back to
# whatever `java` is on PATH.
if [ -z "${JAVA_HOME:-}" ]; then
  if [ -d /opt/homebrew/opt/openjdk@25 ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk@25
  elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
    found="$(/usr/libexec/java_home -v 25 2>/dev/null || true)"
    if [ -n "$found" ]; then
      export JAVA_HOME="$found"
    fi
  fi
fi
if [ -n "${JAVA_HOME:-}" ]; then
  echo "Using JAVA_HOME=$JAVA_HOME"
else
  echo "No JDK 25 found to set JAVA_HOME; falling back to PATH's java (Gradle will auto-provision a JDK 25 toolchain for the build itself)."
fi

# Always use the Gradle wrapper: Fabric Loom requires Gradle >= 9.5 (see
# gradle/wrapper/gradle-wrapper.properties), so an older locally-extracted
# Gradle (e.g. a pre-Fabric-migration .gradle-dist/ leftover) will not work.
GRADLE=./gradlew

echo "Launching Minecraft client (Ctrl+C in this terminal to stop)..."
exec "$GRADLE" runClient "$@"
