#!/usr/bin/env python

import json
import os
import re
import requests
import subprocess
import sys
import toml

# You can test this script locally with this command:
# ALLOWED_LABELS="Breaking changes,New,Fixed,Improved,Changed,Removed,Deprecated" GITHUB_TOKEN="" GITHUB_REPO="Adyen/adyen-android" python ./scripts/generate_release_notes.py test_output.txt

class DependencyUpdate:

    def __init__(self, id, link, new_version, old_version):
        self.id = id
        self.link = link
        self.new_version = new_version
        self.old_version = old_version

def fetch_latest_release_tag() -> str:
    return subprocess.check_output(['git', 'describe', '--tags', '--abbrev=0']
        ).decode(sys.stdout.encoding
        ).strip()

def fetch_recent_commits(latest_tag: str):
    return subprocess.check_output(['git', 'log', '--oneline', latest_tag + '..HEAD']
        ).decode(sys.stdout.encoding
        ).strip()

def is_merge_commit(commit: str) -> bool:
    # Check if the commit message starts with a certain message after the hash
    return commit.startswith('Merge pull request #', commit.index(' ') + 1)

def get_pr_number(commit: str) -> str:
    start = commit.index("#") + 1
    end = commit.index(" ", start)
    return commit[start:end]

def fetch_pr(number: str) -> dict:
    url = 'https://api.github.com/repos/{}/pulls/{}'.format(os.getenv('GITHUB_REPO'), number)
    headers = {'Authorization': 'token ' + os.getenv('GITHUB_TOKEN')}
    return requests.get(url, headers=headers).json()

def get_label_content(label: str, pr_body: str) -> str:
    if not pr_body: return ''

    header = '### ' + label
    should_take = False

    content = ''

    for line in pr_body.splitlines():
        if should_take and re.search(r'^[#]+[ ]', line) is not None:
            should_take = False
            break

        if should_take: content = content + line + '\n'

        if line == header: should_take = True

    return content

def format_dependency_table(dependency_updates: [DependencyUpdate]) -> str:
    table = '| Name | Version |\n|------|---------|'

    for dependency in dependency_updates:
        table = table + '\n| {} | `{}` -> `{}` |'.format(dependency.link, dependency.old_version, dependency.new_version)

    return table

def combine_contents(label_contents, dependency_updates) -> str:
    output = ''
    for label,value in label_contents.items():
        if value or dependency_updates and label == 'Changed':
            output = '{}### {}\n'.format(output, label)

        if value: output = '{}{}\n'.format(output, value)

        if label == 'Changed' and dependency_updates:
            output = output + '- Dependency versions:\n\n' + format_dependency_table(dependency_updates)

    if output: output = output.strip()

    return output

def generate_dependency_updates(latest_tag: str) -> [DependencyUpdate]:
    updates = []
    toml_file_path = 'gradle/libs.versions.toml'

    old_toml_file = subprocess.check_output(['git', 'show', latest_tag + ':' + toml_file_path]
        ).decode(sys.stdout.encoding
        ).strip()
    old_versions = toml.loads(old_toml_file)

    with open(toml_file_path) as file:
        new_versions = toml.load(file)

    all_versions = {**old_versions['libraries'], **new_versions['libraries'], **old_versions['plugins'], **new_versions['plugins']}

    with open('.github/release_notes_dependency_list.toml') as file:
        dependency_list = toml.load(file)

    for value in all_versions.values():
        if 'group' in value and 'name' in value:
            id = value['group'] + ':' + value['name']
        elif 'module' in value:
            id = value['module']
        else:
            id = value['id']

        if id not in dependency_list['excluded'] and id not in dependency_list['included']:
            raise Exception('Dependency not recognized: ' + id)

        if id in dependency_list['excluded']:
            continue

        if 'version' in value:
            version_ref = value['version']['ref']
        else:
            # If there is no explicit version defined the version probably comes from a BoM and it's safe to skip
            continue

        link = dependency_list['included'][id]
        new_version = new_versions['versions'].get(version_ref, None)
        old_version = old_versions['versions'].get(version_ref, None)

        # If the versions isn't updated we can ignore the dependency
        if new_version == old_version:
            continue

        dependency_update = DependencyUpdate(id, link, new_version, old_version)
        updates.append(dependency_update)

    return updates

def generate_release_notes_from_prs():
    latest_tag = fetch_latest_release_tag()
    commits = fetch_recent_commits(latest_tag)

    print('Commits between the {} and HEAD\n{}'.format(latest_tag, commits))

    labels = os.getenv('ALLOWED_LABELS').split(',')
    label_contents = {}
    for label in labels:
        label_contents[label] = ''

    for commit in commits.splitlines():
        if is_merge_commit(commit):
            pr_number = get_pr_number(commit)
            print('Processing PR #' + pr_number)

            response = fetch_pr(pr_number)
            pr_body = response['body']

            for label in labels:
                label_content = get_label_content(label, pr_body)
                if label_content:
                    label_contents[label] = label_contents[label] + label_content

    dependency_updates = generate_dependency_updates(latest_tag)

    output = combine_contents(label_contents, dependency_updates)

    print('Generated release notes:\n' + output)
    with open(sys.argv[1], 'w+') as f:
        f.write(output)
    if os.getenv('GITHUB_STEP_SUMMARY'): os.environ[os.getenv('GITHUB_STEP_SUMMARY')] = output

def main():
    red = '\033[31m'

    if os.getenv('ALLOWED_LABELS') is None:
        print(red + 'ALLOWED_LABELS is not provided. Please provide it in env list. Exiting...')
        sys.exit(1)

    if os.getenv('GITHUB_TOKEN') is None:
        print(red + 'GITHUB_TOKEN is not provided. Please provide it in env list. Exiting...')
        sys.exit(1)

    if os.getenv('GITHUB_REPO') is None:
        print(red + 'GITHUB_REPO is not provided. Please provide it in env list. Exiting...')
        sys.exit(1)

    generate_release_notes_from_prs()

if __name__ == '__main__':
    main()
