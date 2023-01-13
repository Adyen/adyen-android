#!/bin/bash

#
# Copyright (c) 2023 Adyen N.V.
#
# This file is open source and available under the MIT license. See the LICENSE file for more info.
#
# Created by oscars on 13/1/2023.
#

function trigger_action() {
  local REPO_OWNER="Adyen"
  local REPO_NAME="adyen-android"
  # Insert your branch name
  local BRANCH=""
  local WORKFLOW_ID="generate_dependency_graph.yml"
  # You can get or generate one at https://github.com/settings/tokens
  local PERSONAL_ACCESS_TOKEN=""

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
    -u ":${PERSONAL_ACCESS_TOKEN}" \
    https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/actions/workflows/${WORKFLOW_ID}/dispatches \
    -d "{\"ref\":\"${BRANCH}\"}"

    echo ""
    echo "Done!"

}

trigger_action
