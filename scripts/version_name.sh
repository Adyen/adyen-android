#!/bin/bash

function release_version() {
    local build_file="${GITHUB_WORKSPACE}/gradle/libs.versions.toml"
    local version_name_key="version-name"
    local version_name_regex="^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{1,2}(-(alpha|beta|rc)[0-9]{2}){0,1}$"

    local version=$(sed -n "s/.*${version_name_key}[[:space:]]*=[[:space:]]*[\"\']\(.*\)[\"\'].*/\1/p" ${build_file})

    if [[ ! ${version} =~ ${version_name_regex} ]]; then
        echo "Error: invalid version name [$version], please validate that [$version_name_key] at [$build_file] follows regex $version_name_regex ."
        exit 1
    fi

    echo "$version"
}

release_version
