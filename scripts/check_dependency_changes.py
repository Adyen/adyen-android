#!/usr/bin/env python

import os
import subprocess
import sys
import difflib

# You can test this script locally with this command:
# BASE_REF="BASE_REFERENCE" HEAD_REF="HEAD_REFERENCE" python ./scripts/check_dependency_changes.py test_output.txt

def compare_dependency_list(base_ref: str, head_ref: str):
    base_dependency_list = get_dependency_list(base_ref)
    head_dependency_list = get_dependency_list(head_ref)

    dependency_diff = diff_strings(base_dependency_list, head_dependency_list)
    output = generate_github_comment(dependency_diff)

    with open(sys.argv[1], 'w+') as f:
        f.write(output)
    if os.getenv('GITHUB_STEP_SUMMARY'): os.environ[os.getenv('GITHUB_STEP_SUMMARY')] = output

def get_dependency_list(git_ref: str) -> str:
    subprocess.Popen(["git", "checkout", "-f", git_ref])
    # This is needed to avoid having the "downloading" wrapper progress text printed inside the output of the task
    subprocess.Popen(["./gradlew", "wrapper", "-q"])
    return subprocess.check_output(["./gradlew", "dependencyList", "-q", "--no-configuration-cache"]
        ).decode(sys.stdout.encoding
        ).strip()

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
To check the affected modules run the `dependencyList` gradle task with `includeModules=true`.
    '''.format(diff=dependency_diff)

def main():
    red = '\033[31m'

    base_ref = os.getenv('BASE_REF')
    if base_ref is None:
        print(red + 'BASE_REF is not provided. Please provide it in env list. Exiting...')
        sys.exit(1)

    head_ref = os.getenv('HEAD_REF')
    if head_ref is None:
        print(red + 'HEAD_REF is not provided. Please provide it in env list. Exiting...')
        sys.exit(1)

    compare_dependency_list(base_ref, head_ref)

if __name__ == '__main__':
    main()
