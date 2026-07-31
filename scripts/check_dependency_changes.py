#!/usr/bin/env python

import os
import subprocess
import sys
import difflib

# You can test this script locally with this command:
# FIRST_DEPENDENCY_LIST="FIRST_DEPENDENCY_LIST" SECOND_DEPENDENCY_LIST="SECOND_DEPENDENCY_LIST" python ./scripts/check_dependency_changes.py test_output.txt

def compare_dependency_list(first_deps, second_deps):
    dependency_diff = diff_strings(first_deps, second_deps)
    output = generate_github_comment(dependency_diff)

    with open(sys.argv[1], 'w+') as f:
        f.write(output)
    if os.getenv('GITHUB_STEP_SUMMARY'): os.environ[os.getenv('GITHUB_STEP_SUMMARY')] = output

def diff_strings(first: str, second: str) -> str:
    diff = difflib.unified_diff(first.splitlines(keepends=True), second.splitlines(keepends=True))
    return ''.join(diff)

def generate_github_comment(dependency_diff: str) -> str:
    if not dependency_diff:
        return ""

    return '''\
The following dependencies have been modified in this PR:
```diff
{diff}
```
To check the affected modules run the `aggregateDependencyLists` gradle task with `includeModules=true`.
    '''.format(diff=dependency_diff)

def main():
    red = '\033[31m'

    first_deps = os.getenv('FIRST_DEPENDENCY_LIST')
    if first_deps is None:
        print(red + 'FIRST_DEPENDENCY_LIST is not provided. Please provide it in env list. Exiting...')
        sys.exit(1)

    second_deps = os.getenv('SECOND_DEPENDENCY_LIST')
    if second_deps is None:
        print(red + 'SECOND_DEPENDENCY_LIST is not provided. Please provide it in env list. Exiting...')
        sys.exit(1)

    compare_dependency_list(first_deps, second_deps)

if __name__ == '__main__':
    main()
