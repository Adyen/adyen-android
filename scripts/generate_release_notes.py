#!/usr/bin/env python

import json
import os
import re
import requests
import subprocess
import sys

# You can test this script locally with this command:
# ALLOWED_LABELS="Breaking changes,New,Fixed,Improved,Changed,Removed,Deprecated" GITHUB_TOKEN="" GITHUB_REPO="Adyen/adyen-android" python ./scripts/generate_release_notes.py test_output.txt

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

def combine_contents(label_contents) -> str:
    output = ''
    for label,value in label_contents.items():
        if value: output = '{}### {}\n{}\n'.format(output, label, value)

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

    for commit in commits.splitlines():
        if is_merge_commit(commit):
            pr_number = get_pr_number(commit)
            print('Processing PR #' + pr_number)

            response = fetch_pr(pr_number)
            pr_body = response['body']
            has_dependencies_label = any(label['name'] == 'Dependencies' for label in response['labels'])

            if has_dependencies_label:
                print('TODO')
            else:
                for label in labels:
                    label_content = get_label_content(label, pr_body)
                    if label_content:
                        label_contents[label] = label_contents[label] + label_content
                        print('Generated notes for {}:\n{}'.format(label, label_content))

    output = combine_contents(label_contents)

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
