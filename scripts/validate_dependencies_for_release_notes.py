#!/usr/bin/env python

import subprocess
import sys
import toml

def fetch_changed_dependencies() -> dict:
    toml_file_path = 'gradle/libs.versions.toml'

    # Fetch the base commit of the PR using the merge base
    base_commit = subprocess.check_output(
        ['git', 'merge-base', 'origin/v5', 'HEAD']
    ).decode(sys.stdout.encoding).strip()

    # Fetch the version of the file before the PR changes
    old_toml_file = subprocess.check_output(
        ['git', 'show', base_commit + ':' + toml_file_path]
    ).decode(sys.stdout.encoding).strip()
    old_versions = toml.loads(old_toml_file)

    # Load the current version of the file after the PR changes
    with open(toml_file_path) as file:
        new_versions = toml.load(file)

    # Combine libraries and plugins for both old and new versions
    old_dependencies = {**old_versions['libraries'], **old_versions['plugins']}
    new_dependencies = {**new_versions['libraries'], **new_versions['plugins']}

    # Identify newly added dependencies
    added_dependencies = {}

    for dependency_name, dependency_data in new_dependencies.items():
        # Check if the dependency is not in the old dependencies
        if dependency_name not in old_dependencies:
            # Add the new dependency to the dictionary
            added_dependencies[dependency_name] = dependency_data

    return added_dependencies

def validate_new_dependencies(added_dependencies):
    with open('.github/release_notes_dependency_list.toml') as file:
        dependency_list = toml.load(file)

    for value in added_dependencies.values():
        # Determine the identifier for matching
        if 'group' in value and 'name' in value:
            id = value['group'] + ':' + value['name']
        elif 'module' in value:
            id = value['module']
        else:
            id = value['id']

        # Check against included and excluded lists
        if id not in dependency_list['excluded'] and id not in dependency_list['included']:
            raise Exception(
                f"❌ Dependency not recognized: {id}\n"
                "Please add it to either the 'included' or 'excluded' lists in `.github/release_notes_dependency_list.toml`."
            )

def main():
    try:
        # Check for changed dependencies in the PR
        added_dependencies = fetch_changed_dependencies()

        if added_dependencies:
            validate_new_dependencies(added_dependencies)
            print("✅ All new dependencies are properly listed.")
        else:
            print("✅ No new dependencies added in this PR.")

    except Exception as e:
        print(f"❌ Error: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()
