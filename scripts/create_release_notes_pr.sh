#!/bin/bash

# Configure Git
configure_git() {
    echo "Configuring Git..."
    git config --global user.name "$GITHUB_ACTOR"
    git config --global user.email "${GITHUB_ACTOR}@users.noreply.github.com"
}

# Check out the base branch and create a new branch
prepare_branches() {
    echo "Checking out base branch '$BASE_BRANCH'..."
    git fetch origin "$BASE_BRANCH"
    git checkout -f "$BASE_BRANCH"

    echo "Creating and switching to branch '$RELEASE_NOTES_BRANCH'..."
    git checkout -b "$RELEASE_NOTES_BRANCH"
}

# Commit the release notes file
commit_release_notes() {
    echo "Adding release notes file '$RELEASE_NOTES_FILE_NAME' to the commit..."
    git add "$RELEASE_NOTES_FILE_NAME"

    echo "Committing changes..."
    git commit -m "Add release notes for $VERSION_NAME release"
    git push origin "$RELEASE_NOTES_BRANCH"
}

# Create a pull request via the GitHub API
create_pr() {
    echo "Creating pull request via GitHub API..."

    API_URL="https://api.github.com/repos/$GITHUB_REPO/pulls"
    PR_TITLE="Release $VERSION_NAME"
    PR_BODY=$(cat <<EOF
      ## Description
      This PR adds release notes for the $VERSION_NAME release. Merging it will update the release notes and publish the draft GitHub release.

      ## Checklist before merging
      - [ ] Release is tested
      - [ ] Release notes are approved by the Docs team
      - [ ] Release is published to Maven Central
EOF
    )

    # Remove leading spaces
    PR_BODY="${PR_BODY//      /}"

    JSON_PAYLOAD=$(jq -n \
      --arg title "$PR_TITLE" \
      --arg head "$RELEASE_NOTES_BRANCH" \
      --arg base "$BASE_BRANCH" \
      --arg body "$PR_BODY" \
      '{
          title: $title,
          head: $head,
          base: $base,
          body: $body
      }')

    curl -s -X POST -H "Authorization: token $GITHUB_TOKEN" \
        -H "Content-Type: application/json" \
        -d "$JSON_PAYLOAD" "$API_URL" > /dev/null || {
          echo "Error: Failed to create a pull request."; exit 1;
        }

    echo "Pull request created successfully."
}

# Main Execution
create_release_notes_pr() {
    if [ -z "$VERSION_NAME" ]; then
        echo "VERSION_NAME is not provided. Please provide it in env list. Exiting..."
        exit 1
    fi

    if [ -z "$BASE_BRANCH" ]; then
        echo "BASE_BRANCH is not provided. Please provide it in env list. Exiting..."
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

    configure_git
    prepare_branches
    commit_release_notes
    create_pr
}

RELEASE_NOTES_FILE_NAME=$1
RELEASE_NOTES_BRANCH="release-notes-$VERSION_NAME"

create_release_notes_pr
