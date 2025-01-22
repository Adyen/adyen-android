#!/bin/bash

#
# Copyright (c) 2025 Adyen N.V.
#
# This file is open source and available under the MIT license. See the LICENSE file for more info.
#
# Created by ozgur on 21/1/2025.
#
function generate_pr_label() {
  if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <branch_name>" >&2
    exit 1
  fi
  branch_name=$1

  # Check if the string starts with a specific prefix
  if [[ "$branch_name" == feature/* ]]; then
    label="Feature"
  elif [[ "$branch_name" == fix/* ]]; then
    label="Fix"
  elif [[ "$branch_name" == chore/* ]]; then
    label="Chore"
  else
    echo "Branch name is not valid. It should start with feature/, fix/, chore/ or renovate/" >&2
    exit 1
  fi

  echo "$label"
}

generate_pr_label $1
