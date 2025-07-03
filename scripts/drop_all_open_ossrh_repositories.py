#!/usr/bin/env python3
import os
import sys
import requests
import urllib.parse

# --- Configuration ---
SONATYPE_API_BASE_URL = "https://ossrh-staging-api.central.sonatype.com/manual"
SEARCH_REPOSITORIES_URL = f"{SONATYPE_API_BASE_URL}/search/repositories"
DROP_REPOSITORY_BASE_URL = f"{SONATYPE_API_BASE_URL}/drop/repository" # Key will be appended
TIMEOUT_SECONDS = 60

def _get_auth_credentials():
    # Retrieves Sonatype username and password from environment variables.
    # Exits with an error if they are not set.
    username = os.environ.get("SONATYPE_CENTRAL_PORTAL_USERNAME")
    password = os.environ.get("SONATYPE_CENTRAL_PORTAL_PASSWORD")

    if not username or not password:
        print("Error: SONATYPE_CENTRAL_PORTAL_USERNAME and SONATYPE_CENTRAL_PORTAL_PASSWORD environment variables must be set.")
        sys.exit(1)
    return username, password

def _make_api_request(method, url, auth, params=None, headers=None, expected_success_codes=None):
    # Makes an HTTP request and handles common errors.
    # Returns the response object on success, None on failure or request exception.
    if headers is None:
        headers = {}
    headers.setdefault("Accept", "application/json") # Default Accept header

    if expected_success_codes is None:
        expected_success_codes = [200]

    print(f"Making {method} request to: {url}")
    try:
        response = requests.request(method=method, url=url, auth=auth, params=params, headers=headers, timeout=TIMEOUT_SECONDS)

        print(f"Response Status: {response.status_code}")
        # Optionally print response text for debugging, can be verbose
        # print(f"Response Text: {response.text[:500]}...") # Print first 500 chars

        if response.status_code in expected_success_codes:
            return response
        else:
            print(f"Error: API request to {url} failed with status {response.status_code}.")
            print(f"Response: {response.text}")
            return None

    except requests.exceptions.RequestException as e:
        print(f"Error: Request to {url} failed due to an exception: {e}")
        return None

def _list_open_repositories(auth):
    # Fetches a list of 'open' repository keys.
    # Returns a list of keys, or None if the request fails.
    params = {"state": "open", "ip": "any"}
    response = _make_api_request("GET", SEARCH_REPOSITORIES_URL, auth, params=params)

    if response:
        try:
            data = response.json()
            # Ensure 'repositories' key exists and is a list before extracting keys
            repositories_data = data.get("repositories", [])
            if not isinstance(repositories_data, list):
                print(f"Error: Expected 'repositories' to be a list, but got: {type(repositories_data)}")
                return None

            repo_keys = [repo.get("key") for repo in repositories_data if repo.get("key")]
            print(f"Found {len(repo_keys)} open repositories.")
            return repo_keys
        except ValueError: # Includes JSONDecodeError
            print(f"Error: Could not decode JSON response from {SEARCH_REPOSITORIES_URL}")
            print(f"Response text: {response.text}")
            return None
    return None

def _drop_repository(auth, repo_key):
    # Drops a single repository by its key.
    # Returns True if successful or repository already gone (404), False otherwise.
    if not repo_key:
        print("Error: Cannot drop repository with an empty key.")
        return False

    # The key can contain slashes which need to be URL encoded for the path segment
    encoded_repo_key = urllib.parse.quote(repo_key, safe='')
    drop_url = f"{DROP_REPOSITORY_BASE_URL}/{encoded_repo_key}"

    # Successful drop could be 200, 202, 204. 404 means it's already gone (which is success for us).
    success_codes = [200, 202, 204, 404]
    response = _make_api_request("DELETE", drop_url, auth, expected_success_codes=success_codes)

    if response:
        if response.status_code == 404:
            print(f"Repository {repo_key} not found (or already dropped). Considered success.")
        else:
            print(f"Successfully dropped repository {repo_key} (or ensured it was dropped).")
        return True
    return False

def main():
    # Main function to list and drop open repositories.
    print("Starting process to drop all open Sonatype staging repositories...")
    sonatype_username, sonatype_password = _get_auth_credentials()
    auth = (sonatype_username, sonatype_password)

    open_repo_keys = _list_open_repositories(auth)

    if open_repo_keys is None:
        print("Failed to retrieve list of open repositories. Exiting.")
        sys.exit(1)

    if not open_repo_keys:
        print("No open repositories found. Nothing to drop. Exiting successfully.")
        sys.exit(0)

    print(f"Attempting to drop {len(open_repo_keys)} open repositories...")
    all_dropped_successfully = True
    for key in open_repo_keys:
        print(f"--- Processing repository key: {key} ---")
        if not _drop_repository(auth, key):
            all_dropped_successfully = False
            print(f"Failed to drop repository with key: {key}")
        print("--- Finished processing repository key ---")


    if all_dropped_successfully:
        print("All identified open repositories have been successfully processed (dropped or confirmed gone).")
        sys.exit(0)
    else:
        print("One or more repositories could not be dropped. Please check logs.")
        sys.exit(1)

if __name__ == "__main__":
    main()
