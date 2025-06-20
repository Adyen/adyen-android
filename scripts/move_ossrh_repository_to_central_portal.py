#!/usr/bin/env python3
import os
import sys
import requests
import urllib.parse
from requests.exceptions import ReadTimeout

# --- Configuration ---
SONATYPE_API_BASE_URL = "https://ossrh-staging-api.central.sonatype.com/manual"
SEARCH_REPOSITORIES_URL = f"{SONATYPE_API_BASE_URL}/search/repositories"
UPLOAD_REPO_BASE_URL = f"{SONATYPE_API_BASE_URL}/upload/repository"
TIMEOUT_SECONDS = 120

def _get_env_variables():
    # Retrieves Sonatype username, password, and publishing_type from environment variables.
    # Exits with an error if they are not set.
    username = os.environ.get("SONATYPE_CENTRAL_PORTAL_USERNAME")
    password = os.environ.get("SONATYPE_CENTRAL_PORTAL_PASSWORD")
    publishing_type = os.environ.get("PUBLISHING_TYPE")

    required = {
        "SONATYPE_CENTRAL_PORTAL_USERNAME": username,
        "SONATYPE_CENTRAL_PORTAL_PASSWORD": password,
        "PUBLISHING_TYPE": publishing_type,
    }
    for name, value in required.items():
        if not value:
            print(f"Error: Environment variable {name} must be set.")
            sys.exit(1)

    auth = (username, password)
    return auth, publishing_type

def _make_api_request(method, url, auth, params=None, headers=None, success_on_timeout=False):
    # Makes an HTTP request and handles common errors.
    # Returns the response object on success, a special string "TIMEOUT_SUCCESS" if a timeout is treated as success,
    # or None on other failures or request exceptions.
    if headers is None:
        headers = {}
    headers.setdefault("Accept", "application/json")

    print(f"Making {method} request to: {url} with timeout {TIMEOUT_SECONDS}s")
    try:
        response = requests.request(
            method, url, auth=auth, params=params, headers=headers, timeout=TIMEOUT_SECONDS
        )

        print(f"Response Status: {response.status_code}")
        # Check for expected HTTP success codes
        if 200 <= response.status_code < 300:
            return response  # HTTP success
        else:
            print(f"Error: API request to {url} failed with status {response.status_code}.")
            print(f"Response: {response.text}")
            return None  # Indicate failure for unexpected HTTP status

    except ReadTimeout:
        if success_on_timeout:
            print(f"Info: Request to {url} timed out after {TIMEOUT_SECONDS} seconds. Treating as success.")
            return "TIMEOUT_SUCCESS"  # Special indicator for this specific success case
        else:
            print(f"Error: Request to {url} failed due to a ReadTimeout.")
            return None
    except requests.exceptions.RequestException as e:
        print(f"Error: Request to {url} failed due to an exception: {e}")
        return None

def _get_single_open_repository_key(auth):
    # Fetches open repositories and ensures there is exactly one.
    # Returns the key of the single open repository, or None if conditions are not met.
    params = {"state": "open", "ip": "any"}
    print("Searching for open repositories...")
    response = _make_api_request("GET", SEARCH_REPOSITORIES_URL, auth, params=params)

    if response is None:
        return None

    try:
        data = response.json()
        repositories = data.get("repositories", [])

        if not isinstance(repositories, list):
            print(f"Error: Expected 'repositories' to be a list, got {type(repositories)}.")
            return None

        if len(repositories) == 1:
            repo_key = repositories[0].get("key")
            if not repo_key:
                print("Error: Found one repository, but it has no 'key'.")
                return None
            print(f"Found one open repository with key: {repo_key}")
            return repo_key
        else:
            print(f"Error: Expected 1 open repository, but found {len(repositories)}.")
            return None

    except ValueError:  # Includes JSONDecodeError
        print(f"Error: Could not decode JSON response from repository search.")
        print(f"Response text: {response.text}")
        return None

def _upload_repository_by_key(auth, repo_key, publishing_type_value):
    # Makes the POST request to upload/promote the repository by its key.
    # Returns True if successful (HTTP 2xx or timeout), False otherwise.
    if not repo_key:
        print("Error: Repository key is missing for upload.")
        return False

    encoded_repo_key = urllib.parse.quote(repo_key, safe="")
    upload_url = f"{UPLOAD_REPO_BASE_URL}/{encoded_repo_key}?publishing_type={urllib.parse.quote(publishing_type_value)}"

    print(f"Attempting to publish repository with key: {repo_key}")

    # Call the API and enable success on timeout for this specific step
    response = _make_api_request(
        "POST", upload_url, auth, success_on_timeout=True
    )

    # A normal response object or our special string means success
    if response is not None:
        print(f"Repository publish command for key '{repo_key}' processed successfully.")
        return True
    else:
        print(f"Error: Failed to publish repository with key '{repo_key}'. Check logs above.")
        return False

def main():
    # Main function to orchestrate the API calls.
    print("Starting Sonatype repository publish process...")
    auth_credentials, publishing_type = _get_env_variables()

    repository_key = _get_single_open_repository_key(auth_credentials)
    if repository_key is None:
        sys.exit(1)

    if _upload_repository_by_key(auth_credentials, repository_key, publishing_type):
        print("Repository has been published successfully.")
        sys.exit(0)
    else:
        sys.exit(1)

if __name__ == "__main__":
    main()
