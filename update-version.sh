#!/usr/bin/env bash
set -euo pipefail

if [ $# -ne 1 ]; then
  echo "Usage: $0 <new-version>"
  exit 1
fi

NEW=$1
ROOT="$(cd "$(dirname "$0")" && pwd)"

# build.mill — publishVersion (always)
sed -i "s|def publishVersion = \"[^\"]*\"|def publishVersion = \"${NEW}\"|" "$ROOT/build.mill"

if [[ "$NEW" != *-SNAPSHOT ]]; then
  # Badge label
  sed -i "s|mill_explicit_dependencies-[^-]*-green|mill_explicit_dependencies-${NEW}-green|" "$ROOT/README.md"

  # Maven Central link
  sed -i "s|mill-explicit-dependencies_3/[^)]*|mill-explicit-dependencies_3/${NEW}|" "$ROOT/README.md"

  # Usage snippet
  sed -i "s|mill-explicit-dependencies:[^\"]*\"|mill-explicit-dependencies:${NEW}\"|" "$ROOT/README.md"
fi

echo "Version updated to ${NEW}"
