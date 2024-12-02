#!/bin/bash

# Fetch latest release tag
fetch_latest_release_tag() {
    LATEST_TAG=$(git describe --tags --abbrev=0)
    echo "$LATEST_TAG"
}

# Fetch recent commits since the last release tag
fetch_recent_commits() {
    LATEST_TAG=$1

    COMMITS=$(git log --oneline "$LATEST_TAG"..HEAD)
    echo -e "$COMMITS"
}

# Generate release notes from PRs
generate_release_notes_from_prs() {
    LATEST_TAG="$(fetch_latest_release_tag)"
    COMMITS="$(fetch_recent_commits "$LATEST_TAG")"
    echo -e "Commits between the $LATEST_TAG and HEAD\n$COMMITS"

    # Initialize variables to hold content for each label
    IFS=',' read -r -a LABELS <<< "$ALLOWED_LABELS"
    declare -A LABEL_CONTENTS
    for LABEL in "${LABELS[@]}"; do
        LABEL_CONTENTS["$LABEL"]=""
    done

    # Iterate over each commit to find PR numbers, fetch PR body and extract release notes
    while IFS= read -r COMMIT; do
        if [[ $COMMIT =~ Merge\ pull\ request\ \#([0-9]+) ]]; then
            PR_NUMBER="${BASH_REMATCH[1]}"
            echo -e "Processing PR #$PR_NUMBER"

            # Fetch PR content and extract body
            API_URL="https://api.github.com/repos/$GITHUB_REPO/pulls/$PR_NUMBER"
            PR_RESPONSE=$(curl -s -H "Authorization: token $GITHUB_TOKEN" "$API_URL")
            PR_BODY=$(echo "$PR_RESPONSE" | jq -r '.body')

            # Extract release notes for each label
            for LABEL in "${LABELS[@]}"; do
                HEADER="### $LABEL"
                LABEL_CONTENT=$(echo "$PR_BODY" | awk -v header="$HEADER" '
                    $0 ~ header { capture = 1; next } # Start capturing after the specified header
                    capture && /^[#]+[ ]/ { exit } # Stop at lines starting with one or more # followed by a space
                    capture { print $0 } # Continue capturing until a stopping condition
                ')
                if [ -n "$LABEL_CONTENT" ]; then
                    LABEL_CONTENT=$(echo "$LABEL_CONTENT" | sed -e :a -e '/[^[:blank:]]/,$!d; /^[[:space:]]*$/{ $d; N; ba' -e '}') # Remove new lines at the beginning and at the end
                    LABEL_CONTENTS["$LABEL"]="${LABEL_CONTENTS[$LABEL]}$LABEL_CONTENT\n"
                    echo -e "Generated notes for $LABEL:\n$LABEL_CONTENT"
                fi
            done
        fi
    done <<< "$COMMITS"

    # Combine notes by labels
    OUTPUT=""
    for LABEL in "${LABELS[@]}"; do
        if [ -n "${LABEL_CONTENTS[$LABEL]}" ]; then
            OUTPUT="${OUTPUT}### $LABEL\n${LABEL_CONTENTS[$LABEL]}\n"
        fi
    done

    # Save release notes in a file
    if [ -n "$OUTPUT" ]; then
        printf "Generated release notes:\n$OUTPUT"
        printf "$OUTPUT" >> "$RELEASE_NOTES_FILE_NAME"
        printf "$OUTPUT" >> "$GITHUB_STEP_SUMMARY"
    fi
}

# Main Execution
generate_release_notes() {
    if [ -z "$ALLOWED_LABELS" ]; then
        echo "ALLOWED_LABELS is not provided. Please provide it in env list. Exiting..."
        exit 1
    fi

    if [ -z "$GITHUB_TOKEN" ]; then
        echo "GITHUB_TOKEN is not provided. Please provide it in env list. Exiting..."
        exit 1
    fi

    if [ -z "$GITHUB_REPO" ]; then
        echo "GITHUB_REPO is not provided. Please provide it in env list. Exiting..."
        exit 1
    fi

    if [ -z "$RELEASE_NOTES_FILE_NAME" ]; then
        echo "Release notes file name is not provided. Please provide it in the arguments of the script. Exiting..."
        exit 1
    fi

    generate_release_notes_from_prs
}

RELEASE_NOTES_FILE_NAME=$1

generate_release_notes
