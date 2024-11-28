#!/bin/bash

if [[ -z "$PR_BODY" ]]; then
  echo "PR_BODY is not provided. Please provide it in env list. Exiting..."
  exit 1
fi

if [[ -z "$ALLOWED_RELEASE_NOTES_LABELS" ]]; then
  echo "ALLOWED_RELEASE_NOTES_LABELS is not provided. Please provide it in env list. Exiting..."
  exit 1
fi

# Read allowed labels into an array
IFS=',' read -r -a LABELS <<< "$ALLOWED_RELEASE_NOTES_LABELS"

# Check for valid release notes under a label
check_release_notes() {
  local label=$1
  local header="### $label"

  # Extract the content under the label
  label_content=$(echo "$PR_BODY" | awk -v header="$header" '
    $0 ~ header { capture = 1; next } # Start capturing after the specified header
    capture && /^[#]+[ ]/ { exit } # Stop at lines starting with one or more # followed by a space
    capture { print $0 } # Continue capturing until a stopping condition
  ')

  # Check if the content has at least one non-empty line
  if [[ ! -z $(echo "$label_content" | grep -vE '^[[:space:]]*$') ]]; then
    return 0 # Valid release notes found
  fi

  return 1 # No valid release notes found
}

# Loop through allowed labels and check for valid release notes
for label in "${LABELS[@]}"; do
  if check_release_notes "$label"; then
    echo "Valid release notes found under label: $label"
    exit 0
  fi
done

echo "Error: No valid release notes found in PR body. Please add release notes."
exit 1
