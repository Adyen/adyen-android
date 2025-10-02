// A script to mark all GitHub releases in a repository with a specific tag.
// It can replace existing tags like [DEPRECATED] or [END-OF-LIFE].
// Uses the GitHub REST API and supports a dry run mode.
//
// To get your API token go to Your profile, and create a Fine-grained PAT:
// Resource owner: your-org
// Repository access: your-repo
// Give permission for: Contents R/W
//
// Then you can export the token temporarily in your console (before running):
// export GITHUB_TOKEN=github_pat_xxx
//
// You can run this script by doing:
// DRY_RUN=false MAJOR_VERSION=X TAG="[END-OF-LIFE]" node .github/scripts/deprecate-releases.js

const fetch = require('node-fetch');

// --- Configuration ---
// Load these from environment variables for security.
// DO NOT hardcode your token here.
const GITHUB_TOKEN = process.env.GITHUB_TOKEN;
const REPO_OWNER = 'adyen';
const REPO_NAME = 'adyen-android';
// Dry run set to true by default, set DRY_RUN=false to run for real
const DRY_RUN = process.env.DRY_RUN !== 'false';
// Major version filter - only releases starting with this version will be processed
const MAJOR_VERSION = process.env.MAJOR_VERSION;
// The tag to apply to the release titles, e.g., "[DEPRECATED]"
const TAG = process.env.TAG;

// --------------------

const API_BASE_URL = 'https://api.github.com';
// A list of tags that the script should look for and be able to replace.
const REPLACEABLE_TAGS = ['[DEPRECATED]', '[END-OF-LIFE]'];

/**
 * Fetches all releases from a GitHub repository, handling pagination.
 * @returns {Promise<Array>} A promise that resolves to an array of release objects.
 */
async function fetchAllReleases() {
  let allReleases = [];
  let page = 1;

  console.log('Fetching all releases...');

  while (true) {
    const url = `${API_BASE_URL}/repos/${REPO_OWNER}/${REPO_NAME}/releases?page=${page}&per_page=100`;
    const response = await fetch(url, {
      headers: { 'Authorization': `token ${GITHUB_TOKEN}` }
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch releases: ${response.statusText}`);
    }

    const releasesPage = await response.json();
    if (releasesPage.length === 0) {
      break;
    }

    allReleases = allReleases.concat(releasesPage);
    page++;
  }

  console.log(`Found a total of ${allReleases.length} releases.`);
  return allReleases;
}

/**
 * Updates a single release by adding or replacing a tag in its title.
 * In dry run mode, it only logs the intended action.
 * @param {object} release - The release object from the GitHub API.
 */
async function updateReleaseTag(release) {
  // Filter by major version if specified
  if (MAJOR_VERSION && !release.name.startsWith(MAJOR_VERSION)) {
    console.log(`- Skipping "${release.name}" (not version ${MAJOR_VERSION}.x).`);
    return;
  }

  let baseTitle = release.name;

  // Check if the current title has a replaceable tag and remove it.
  for (const existingTag of REPLACEABLE_TAGS) {
    if (baseTitle.endsWith(existingTag)) {
      baseTitle = baseTitle.replace(existingTag, '').trim();
      break; // Assume only one tag exists
    }
  }

  const newTitle = `${baseTitle} ${TAG}`;

  // If the new title is the same as the old one, no need to update.
  if (release.name === newTitle) {
    console.log(`- Skipping "${release.name}" (already has the correct tag).`);
    return;
  }

  // --- DRY RUN LOGIC ---
  if (DRY_RUN) {
    console.log(`- [DRY RUN] Would update "${release.name}" to "${newTitle}".`);
    return; // Exit before making the API call
  }
  // ---------------------

  const url = `${API_BASE_URL}/repos/${REPO_OWNER}/${REPO_NAME}/releases/${release.id}`;

  console.log(`- Updating "${release.name}"...`);

  const response = await fetch(url, {
    method: 'PATCH',
    headers: {
      'Authorization': `token ${GITHUB_TOKEN}`,
      'Content-Type': 'application/json',
      'Accept': 'application/vnd.github.v3+json'
    },
    body: JSON.stringify({ name: newTitle })
  });

  if (!response.ok) {
    console.error(`  Failed to update "${release.name}": ${response.statusText}`);
  } else {
    console.log(`  Successfully updated to "${newTitle}".`);
  }
}

/**
 * Main function to run the script.
 */
async function main() {
  if (!GITHUB_TOKEN || !REPO_OWNER || !REPO_NAME) {
    console.error('Error: Make sure GITHUB_TOKEN, REPO_OWNER, and REPO_NAME are set as environment variables.');
    process.exit(1);
  }

  if (!TAG) {
    console.error('Error: The TAG environment variable is required. e.g., TAG="[DEPRECATED]"');
    process.exit(1);
  }

  if (DRY_RUN) {
    console.log('*** DRY RUN MODE ENABLED: No actual changes will be made. ***\n');
  }

  if (MAJOR_VERSION) {
    console.log(`*** MAJOR VERSION FILTER: Only releases starting with "${MAJOR_VERSION}" will be processed. ***\n`);
  } else {
    console.log('*** NO VERSION FILTER: All releases will be processed. Set MAJOR_VERSION to filter by version. ***\n');
  }

  console.log(`*** Applying tag: "${TAG}" ***\n`);

  try {
    const releases = await fetchAllReleases();

    if (releases.length === 0) {
      console.log('No releases to update.');
      return;
    }

    console.log('\nStarting release update process...');
    for (const release of releases) {
      await updateReleaseTag(release);
    }
    console.log('\nProcess complete! ✨');

  } catch (error) {
    console.error('An unexpected error occurred:', error.message);
  }
}

main();
