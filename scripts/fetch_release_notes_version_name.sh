#!/bin/bash
#
# Copyright (c) 2024 Adyen N.V.
#
# This file is open source and available under the MIT license. See the LICENSE file for more info.
#
# Created by ozgur on 10/12/2024.
#
function fetch_release_notes_version_name() {
    if [ "$#" -ne 1 ]; then
        echo "Usage: $0 <merge_commit_message>" >&2
        exit 1
    fi
    commit_message=$1

    # The release notes branch is named "release-notes-<version>", e.g.
    # "Merge pull request #2782 from Adyen/release-notes-5.19.0".
    # Capture the version token right after "release-notes-" so trailing text
    # (multi-line commit bodies, descriptions) is not included.
    if [[ "$commit_message" =~ release-notes-([0-9a-zA-Z.-]+) ]]; then
        version_name="${BASH_REMATCH[1]}"
    else
        echo "No part starting with 'release-notes-' found in the string." >&2
        exit 1
    fi

    echo "$version_name"
}

fetch_release_notes_version_name "$1"
