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

    # Extract the branch name
    release_notes_branch_name=$(echo "$commit_message" | grep -oE 'release-notes.*')

    # Check if "release-notes" was found
    if [[ -z "$release_notes_branch_name" ]]; then
        echo "No part starting with 'release-notes' found in the string." >&2
        exit 1
    fi

    # Regex for version (starts with a version number like 1.2.3.md)
    version_name_regex="[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{1,2}(-(alpha|beta|rc)[0-9]{2})?"

    # Extract the version name from the "release-notes" part
    version_name=$(echo "$release_notes_branch_name" | grep -oE "$version_name_regex")

    # Check if version name was found
    if [[ -z "$version_name" ]]; then
        echo "Version cannot be extracted." >&2
        exit 1
    fi

    echo "$version_name"
}

fetch_release_notes_version_name "$1"
