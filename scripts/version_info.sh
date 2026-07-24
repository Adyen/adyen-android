#!/bin/bash

# Validates a version name and derives release information from it.
#
# Usage: version_info.sh <version_name>
#
# Output: one `key=value` line per value on stdout, so callers can parse each value by key.
# The keys are:
#   version_name  The validated version name (e.g. `6.1.0` or `6.1.0-alpha.1`).
#   prerelease    `true` if the version name is a pre-release, `false` otherwise.
function version_info() {
    local version_name="$1"

    if [[ -z "$version_name" ]]; then
        echo "Error: no version name provided. Usage: version_info.sh <version_name>." >&2
        exit 1
    fi

    local version_name_regex="^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{1,2}(-(alpha|beta|rc)\.[0-9]{1,2})?$"

    if [[ ! ${version_name} =~ ${version_name_regex} ]]; then
        echo "Error: invalid version name: $version_name. Please make sure that the name follows this pattern: $version_name_regex ." >&2
        exit 1
    fi

    # A version name is a pre-release when it contains an alpha, beta or rc suffix.
    local prerelease="false"
    if [[ "$version_name" == *alpha* || "$version_name" == *beta* || "$version_name" == *rc* ]]; then
        prerelease="true"
    fi

    echo "version_name=$version_name"
    echo "prerelease=$prerelease"
}

version_info "$1"
