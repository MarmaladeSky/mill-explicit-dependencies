#!/usr/bin/env bash
set -euo pipefail

if [ $# -ne 1 ]; then
  echo "Usage: $0 <new-version>"
  exit 1
fi

NEW=$1
ROOT="$(cd "$(dirname "$0")" && pwd)"

# For SNAPSHOT versions, strip the suffix for fields that only accept a bare version
if [[ "$NEW" == *-SNAPSHOT ]]; then
  BASE_VER="${NEW%-SNAPSHOT}"
else
  BASE_VER="$NEW"
fi

# build.mill line 19: def millVersion — bare version (no -SNAPSHOT)
sed -i "s|def millVersion = \"[^\"]*\"|def millVersion = \"${BASE_VER}\"|" "$ROOT/build.mill"

# build.mill line 21: def publishVersion — always full version
sed -i "s|def publishVersion = \"[^\"]*\"|def publishVersion = \"${NEW}\"|" "$ROOT/build.mill"

# integration-test/build.mill line 1: mill-version — bare version (no -SNAPSHOT)
# Uses # as delimiter because the line contains | characters
sed -i "s#//| mill-version: [^[:space:]]*#//| mill-version: ${BASE_VER}#" "$ROOT/integration-test/build.mill"

# integration-test/build.mill line 3: plugin dependency — always full version
# Uses # as delimiter because the line contains | characters
sed -i "s#digital\.junkie::mill-explicit-dependencies:[^\"]*\"#digital.junkie::mill-explicit-dependencies:${NEW}\"#" "$ROOT/integration-test/build.mill"

if [[ "$NEW" != *-SNAPSHOT ]]; then
  # Badge label
  sed -i "s|mill_explicit_dependencies-[^-]*-green|mill_explicit_dependencies-${NEW}-green|" "$ROOT/README.md"

  # Maven Central link
  sed -i "s|mill-explicit-dependencies_3/[^)]*|mill-explicit-dependencies_3/${NEW}|" "$ROOT/README.md"
fi

echo "Version updated to ${NEW}"
