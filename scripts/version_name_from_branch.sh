#!/bin/bash

# Extracts the release version name from the current release branch.
#
# The release branch convention is `release/<version_name>` (e.g. `release/6.1.0`).
#
# Output: the extracted version name on stdout.
function version_name_from_branch() {
    local branch_name="${GITHUB_REF_NAME:-$(git branch --show-current)}"

    if [[ $branch_name != release/* ]]; then
        echo "Error: invalid branch name. Branch name should start with \"release/\"." >&2
        exit 1
    fi

    echo "${branch_name#*release/}"
}

version_name_from_branch
