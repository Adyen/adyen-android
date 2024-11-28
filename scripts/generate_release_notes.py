#!/usr/bin/env python

from packaging.version import Version
import json
import os
import re
import requests
import subprocess
import sys

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
    headers = {}
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

def parse_version(version_string) -> Version:
    try:
        return Version(version_string)
    except:
        return Version('0.0.0')

dependency_exclusion_list = []
with open('.github/.release_notes_dependency_exclusion_list') as file:
    dependency_exclusion_list = file.read().splitlines()

def is_dependency_excluded(id: str) -> bool:
    for line in dependency_exclusion_list:
        if id == line:
            return True

    return False

def parse_dependency_update_row(row: str) -> DependencyUpdate:
    parts = row.split('|')

    tag = parts[1]
    id = tag[tag.index('[') + 1:tag.index(']')]

    if is_dependency_excluded(id): return None

    link = tag[tag.index('(') + 1:tag.index(')')]

    version_split = parts[2].split('`')
    old_version = parse_version(version_split[1])
    new_version = parse_version(version_split[3])

    return DependencyUpdate(id, link, new_version, old_version)

def get_dependency_update_content(pr_body: str) -> [DependencyUpdate]:
    updates = []
    should_take = False

    for line in pr_body.splitlines():
        if should_take and not line.startswith('|'):
            should_take = False
            break

        if should_take:
            parsed = parse_dependency_update_row(line)
            if parsed: updates.append(parsed)

        if line == '|---|---|---|---|---|---|': should_take = True

    return updates

def add_or_update_dependency(dependency_updates: dict, dependency: DependencyUpdate):
    previous = dependency_updates.get(dependency.id)
    if previous:
        new_version = dependency.new_version if dependency.new_version > previous.new_version else previous.new_version
        old_version = dependency.old_version if dependency.old_version < previous.old_version else previous.old_version
        updated = DependencyUpdate(dependency.id, dependency.link, new_version, old_version)
        dependency_updates[dependency.id] = updated
    else:
        dependency_updates[dependency.id] = dependency

def format_dependency_table(dependency_updates: dict) -> str:
    table = '| Name | Version |\n|------|---------|'

    for dependency in dependency_updates.values():
        table = table + '\n| [{}]({}) | `{}` -> `{}` |'.format(dependency.id, dependency.link, str(dependency.old_version), str(dependency.new_version))

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

def generate_release_notes_from_prs():
    latest_tag = fetch_latest_release_tag()
    commits = fetch_recent_commits(latest_tag)

    print('Commits between the {} and HEAD\n{}'.format(latest_tag, commits))

    labels = os.getenv('ALLOWED_LABELS').split(',')
    label_contents = {}
    for label in labels:
        label_contents[label] = ''
    dependency_updates = {}

    for commit in commits.splitlines():
        if is_merge_commit(commit):
            pr_number = get_pr_number(commit)
            print('Processing PR #' + pr_number)

            response = fetch_pr(pr_number)
            pr_body = response['body']
            has_dependencies_label = any(label['name'] == 'Dependencies' for label in response['labels'])

            if has_dependencies_label:
                for dependency in get_dependency_update_content(pr_body):
                    add_or_update_dependency(dependency_updates, dependency)
            else:
                for label in labels:
                    label_content = get_label_content(label, pr_body)
                    if label_content:
                        label_contents[label] = label_contents[label] + label_content

    output = combine_contents(label_contents, dependency_updates)

    if output:
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
