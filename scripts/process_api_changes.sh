#!/bin/bash

#
# Copyright (c) 2024 Adyen N.V.
#
# This file is open source and available under the MIT license. See the LICENSE file for more info.
#
# Created by oscars on 14/6/2024.
#

# Input is the raw `git diff` output for `.api` files produced by the workflow
# (see .github/workflows/validate_public_api.yml).
input_file="api_changes.txt"
output_file="api_changes.md"

if [ ! -s "$input_file" ]
then
  echo "# ✅ No public API changes" > "$output_file"
  exit 0
fi

echo "# 🚫 Public API changes" > "$output_file"

# The input is one or more consecutive git-diff sections, one per changed file.
# Split it into per-file sections, render each as its own fenced diff block
# with a heading derived from the module name (parent folder of the .api file).
awk '
  /^diff --git / {
    # Close previous block if any.
    if (in_block) {
      print "```"
      in_block = 0
    }

    # Extract the b-path (post-image) to derive the module name.
    # Line format: `diff --git a/<path> b/<path>`
    match($0, / b\/[^ ]+/)
    b_path = substr($0, RSTART + 3, RLENGTH - 3)

    # Module name = segment before `/api/` (e.g. `ui-core/api/ui-core.api` → `ui-core`).
    module = b_path
    sub(/\/api\/.*/, "", module)

    print ""
    print "## " module
    print "```diff"
    in_block = 1
    skip_header = 1
    next
  }
  # Skip the remaining git-diff header lines (`index ...`, `new file mode ...`,
  # `deleted file mode ...`, `--- a/...`, `+++ b/...`). These duplicate info
  # already shown in the `## <module>` heading. Passthrough begins at the first
  # `@@` hunk header.
  skip_header && !/^@@/ { next }
  /^@@/ { skip_header = 0 }
  { if (in_block) print }
  END {
    if (in_block) print "```"
  }
' "$input_file" >> "$output_file"

echo "" >> "$output_file"
echo "If these changes are intentional run \`./gradlew apiDump\` and commit the changes." >> "$output_file"
