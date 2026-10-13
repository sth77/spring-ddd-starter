#!/usr/bin/env bash
#
# Safety check of the hygen-it profile: the profile reverts src/ with `git checkout` and `git clean` once it is
# done, which would take any uncommitted work along with the generated code.

set -euo pipefail

if [ -n "$(git status --porcelain src/)" ]; then
    echo "ERROR: Uncommitted changes detected in src/. The hygen-it profile will clean src/ after tests." \
        "Please commit or stash your changes first." >&2
    exit 1
fi
