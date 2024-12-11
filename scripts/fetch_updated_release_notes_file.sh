#!/bin/bash
#
# Copyright (c) 2024 Adyen N.V.
#
# This file is open source and available under the MIT license. See the LICENSE file for more info.
#
# Created by ozgur on 10/12/2024.
#
function fetch_updated_release_notes_file() {
    if [ "$#" -ne 2 ]; then
        echo "Error: Not enough arguments. Usage: $0 <project_root_directory> <version_name>" >&2
        exit 1
    fi

    directory=$1
    version_name=$2
    notes_file_name="${version_name}.md"

    # Search for matching files using the find command
    file=$(ls -R "$directory" 2>/dev/null | grep -x "$notes_file_name")
    if [[ -z "$file" ]]; then
          echo "Release notes file is not found. Make sure release notes file with name ${notes_file_name} exists in the project root directory." >&2
          exit 1
    fi
    if [[ "${directory: -1}" == "/" ]]; then
        full_path="${directory}${file}"
    else
        full_path="${directory}/${file}"
    fi
    echo "$full_path"
}

fetch_updated_release_notes_file $1 $2
