#!/usr/bin/env bash
#
# Convenience launcher for the development Minecraft client (NeoForge 1.21.10)
# with this mod loaded from source.
#
#   ./run-client.sh
#
# First run downloads game assets (a few minutes); later runs are quick.
# It uses an offline dev account, so use it for singleplayer testing.
#
set -euo pipefail
cd "$(dirname "$0")"

# Ensure JAVA_HOME points at a JDK 21 if it isn't already set.
if [ -z "${JAVA_HOME:-}" ]; then
  if [ -d /opt/homebrew/opt/openjdk@21 ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk@21
  elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
  fi
fi
echo "Using JAVA_HOME=${JAVA_HOME:-<unset>}"

# Prefer the locally extracted Gradle (the wrapper's distribution download is
# blocked in some networks); fall back to the Gradle wrapper otherwise.
if [ -x .gradle-dist/gradle-8.12/bin/gradle ]; then
  GRADLE=.gradle-dist/gradle-8.12/bin/gradle
else
  GRADLE=./gradlew
fi

echo "Launching Minecraft client (Ctrl+C in this terminal to stop)..."
exec "$GRADLE" runClient "$@"
