#!/bin/bash

#
# Copyright (c) 2024 Adyen N.V.
#
# This file is open source and available under the MIT license. See the LICENSE file for more info.
#
# Created by oscars on 14/6/2024.
#

# Reads `git diff` output for `.api` files from stdin and renders a Markdown
# report to `api_changes.md`. Intended to be piped from the workflow:
#
#   git diff --no-color -- '**/*.api' | bash scripts/process_api_changes.sh
output_file="api_changes.md"

diff_output="$(cat)"

if [ -z "$diff_output" ]
then
  echo "# ✅ No public API changes" > "$output_file"
  exit 0
fi

{
  echo "# 🚫 Public API changes"
  # Split the concatenated git-diff sections (one per changed file) into
  # per-module fenced diff blocks, headed by the module name.
  printf '%s\n' "$diff_output" | awk '
    /^diff --git / {
      if (in_block) {
        print "```"
        in_block = 0
      }

      # Derive the module name from the b-path. Match both ` b/...` and
      # ` "b/..."` (git quotes paths that contain spaces), and only call
      # `substr` on a successful match.
      if (match($0, / "?b\//)) {
        b_path = substr($0, RSTART + RLENGTH)
        gsub(/"/, "", b_path)

        # Strip `/api/<name>.api` to leave just the module folder (e.g.
        # `ui-core/api/ui-core.api` → `ui-core`). Fall back to dropping the
        # `.api` suffix for paths without a `/api/` segment.
        module = b_path
        if (!sub(/\/api\/.*/, "", module)) {
          sub(/\.api$/, "", module)
        }
        if (module == "") module = "root"

        print ""
        print "## " module
        print "```diff"
        in_block = 1
        skip_header = 1
      }
      next
    }
    # Skip the remaining git-diff header lines (`index ...`, `new file mode`,
    # `deleted file mode`, `--- a/...`, `+++ b/...`); passthrough starts at
    # the first `@@` hunk header.
    skip_header && !/^@@/ { next }
    /^@@/ { skip_header = 0 }
    { if (in_block) print }
    END {
      if (in_block) print "```"
    }
  '
  echo ""
  echo 'If these changes are intentional run `./gradlew apiDump` and commit the changes.'
} > "$output_file"
