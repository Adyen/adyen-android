#!/bin/bash

# Script expects GITHUB_ACCESS_TOKEN var to be set on the environment.
function trigger_action() {
  local REPO_OWNER="Adyen"
  local REPO_NAME="adyen-android"
  local BRANCH="v4"
  local WORKFLOW_ID="publish_release.yml"

  echo "- Trigger Action"
  echo "Repo owner: ${REPO_OWNER}"
  echo "Repo name: ${REPO_NAME}"
  echo "Workflow: ${WORKFLOW_ID}"
  echo "Branch: ${BRANCH}"

  echo ""
  echo "Running..."

  curl \
    -X POST \
    -H "Accept: application/vnd.github.v4+json" \
    -u ":${GITHUB_ACCESS_TOKEN}" \
    https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/actions/workflows/${WORKFLOW_ID}/dispatches \
    -d "{\"ref\":\"${BRANCH}\"}"

    echo ""
    echo "Done!"

}

trigger_action