#!/bin/bash

function release_version() {
    local branch_name=$(git branch --show-current)

    if [[ $branch_name != release/* ]]; then
      echo "Error: invalid branch name. Branch name should start with \"release/\"."
      exit 1
    fi

    local version_name="${branch_name#*release/}"
    local version_name_regex="^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{1,2}(-(alpha|beta|rc)[0-9]{2}){0,1}$"

    if [[ ! ${version_name} =~ ${version_name_regex} ]]; then
        echo "Error: invalid version name: $version_name. Please make sure that the name follows this pattern: $version_name_regex ."
        exit 1
    fi

    echo "$version_name"
}

release_version
