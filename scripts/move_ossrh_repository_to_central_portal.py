import os
import sys
import requests
import urllib.parse

# --- Configuration ---
SONATYPE_API_BASE_URL = "https://ossrh-staging-api.central.sonatype.com/manual"
SEARCH_REPOSITORIES_URL = f"{SONATYPE_API_BASE_URL}/search/repositories"
UPLOAD_REPO_BASE_URL = f"{SONATYPE_API_BASE_URL}/upload/repository" # Key will be appended
TIMEOUT_SECONDS = 600 # This should be adjusted after the API stops throwing 401 error

def _get_env_variables():
    # Retrieves Sonatype username, password, and publishing_type from environment variables.
    # Exits with an error if they are not set.
    username = os.environ.get("SONATYPE_USERNAME")
    password = os.environ.get("SONATYPE_PASSWORD")
    publishing_type = os.environ.get("PUBLISHING_TYPE")

    required = {
        "SONATYPE_USERNAME": username,
        "SONATYPE_PASSWORD": password,
        "PUBLISHING_TYPE": publishing_type
    }
    for name, value in required.items():
        if not value:
            print(f"Error: Environment variable {name} must be set.")
            sys.exit(1)

    auth = (username, password)
    return auth, publishing_type

def _make_api_request(method, url, auth, params=None, headers=None, expected_success_codes=None):
    # Makes an HTTP request and handles common errors.
    # Returns the response object on success, None on failure or request exception.
    if headers is None:
        headers = {}
    headers.setdefault("Accept", "application/json") # Default Accept header

    if expected_success_codes is None:
        if method.upper() == "POST":
            pass
        else: # Default for GET, DELETE etc.
            expected_success_codes = [200]

    print(f"Making {method} request to: {url} with params: {params if params else 'None'}")
    try:
        response = requests.request(method, url, auth=auth, params=params, headers=headers, timeout=TIMEOUT_SECONDS)

        print(f"Response Status: {response.status_code}")
        # Optionally print response text for debugging, can be verbose
        # print(f"Response Text: {response.text[:500]}...") # Print first 500 chars

        # If expected_success_codes is provided, use it for validation
        if expected_success_codes and response.status_code not in expected_success_codes:
            print(f"Error: API request to {url} failed with status {response.status_code}.")
            print(f"Response: {response.text}")
            return None # Indicate failure for unexpected status

        # If no expected_success_codes, or if it's in the list, return the response
        return response

    except requests.exceptions.RequestException as e:
        print(f"Error: Request to {url} failed due to an exception: {e}")
        return None

def _get_single_open_repository_key(auth):
    # Fetches open repositories and ensures there is exactly one.
    # Returns the key of the single open repository, or None if conditions are not met.
    params = {"state": "open", "ip": "any"}
    print("Searching for open repositories...")
    # For GET, we expect a 200
    response = _make_api_request("GET", SEARCH_REPOSITORIES_URL, auth, params=params, expected_success_codes=[200])

    if response is None: # Network or request exception, or non-200 status
        return None

    try:
        data = response.json()
        repositories = data.get("repositories", [])

        if not isinstance(repositories, list):
            print(f"Error: Expected 'repositories' to be a list, got {type(repositories)}.")
            print(f"Response data: {data}")
            return None

        if len(repositories) == 0:
            print("Error: No open repositories found.")
            return None
        elif len(repositories) > 1:
            print(f"Error: Expected 1 open repository, but found {len(repositories)}.")
            for i, repo in enumerate(repositories):
                print(f"  Repo {i+1}: Key = {repo.get('key')}, State = {repo.get('state')}")
            return None
        else: # Exactly one repository
            repo_key = repositories[0].get("key")
            if not repo_key:
                print("Error: Found one repository, but it has no 'key'.")
                print(f"Repository data: {repositories[0]}")
                return None
            print(f"Found one open repository with key: {repo_key}")
            return repo_key

    except ValueError: # Includes JSONDecodeError
        print(f"Error: Could not decode JSON response from repository search.")
        print(f"Response text: {response.text}")
        return None

def _upload_repository_by_key(auth, repo_key, publishing_type_value):
    # Makes the POST request to upload/promote the repository by its key.
    # Includes publishing_type as a query parameter.
    # Returns True if successful (2xx), False otherwise.
    if not repo_key:
        print("Error: Repository key is missing for upload.")
        return False

    encoded_repo_key = urllib.parse.quote(repo_key, safe='')
    # Construct URL with query parameter for publishing_type
    upload_url = f"{UPLOAD_REPO_BASE_URL}/{encoded_repo_key}?publishing_type={urllib.parse.quote(publishing_type_value)}"

    print(f"Attempting to publish repository with key: {repo_key} and publishing_type: {publishing_type_value}")
    # This POST request typically does not require a body, parameters are in URL.
    # We don't set expected_success_codes here, will check manually.
    response = _make_api_request("POST", upload_url, auth)

    if response is None: # Network or request exception
        return False

    print("-------------------- UPLOAD RESPONSE START --------------------")
    print(response.text)
    print("-------------------- UPLOAD RESPONSE END ----------------------")
    print(f"Upload HTTP Status Code: {response.status_code}")

    # Success conditions: 2xx status codes
    if 200 <= response.status_code < 300:
        print(f"Repository publish command for key '{repo_key}' processed: Status {response.status_code}. Considered successful.")
        return True
    else:
        print(f"Error: Failed to publish repository with key '{repo_key}'. Status: {response.status_code}.")
        return False

def main():
    # Main function to orchestrate the API calls.
    print("Starting Sonatype repository publish process...")
    auth_credentials, publishing_type = _get_env_variables()

    repository_key = _get_single_open_repository_key(auth_credentials)

    if repository_key is None:
        # Error messages are printed within _get_single_open_repository_key
        sys.exit(1)

    if _upload_repository_by_key(auth_credentials, repository_key, publishing_type):
        print("Repository has been published successfully.")
        sys.exit(0)
    else:
        # Error messages are printed within _upload_repository_by_key
        sys.exit(1)

if __name__ == "__main__":
    main()
