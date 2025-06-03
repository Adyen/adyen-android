import os
import sys
import requests

# Configuration
TIMEOUT_SECONDS = 60
BASE_API_URL = "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/"

def _get_configuration():
    # Retrieves and validates necessary environment variables and constructs the API URL and auth.
    # Exits if any required environment variable is missing.
    namespace = os.environ.get("NAMESPACE")
    publishing_type = os.environ.get("PUBLISHING_TYPE")
    sonatype_username = os.environ.get("SONATYPE_USERNAME")
    sonatype_password = os.environ.get("SONATYPE_PASSWORD")

    required_vars = {
        "NAMESPACE": namespace,
        "PUBLISHING_TYPE": publishing_type,
        "SONATYPE_USERNAME": sonatype_username,
        "SONATYPE_PASSWORD": sonatype_password
    }

    for var_name, value in required_vars.items():
        if value is None:
            print(f"Error: Environment variable {var_name} is not set.")
            sys.exit(1)

    api_url = f"{BASE_API_URL}{namespace}?publishing_type={publishing_type}"
    auth = (sonatype_username, sonatype_password)

    return api_url, auth

def _make_api_request(url, auth_tuple, headers_dict):
    # Makes the POST request to the Sonatype API.
    # Returns the response object or None if a request exception occurs.
    print(f"Making API call to: {url}")

    try:
        response = requests.post(url, headers=headers_dict, auth=auth_tuple, timeout=TIMEOUT_SECONDS)
        return response
    except requests.exceptions.RequestException as e:
        print(f"Error: Request failed due to an exception: {e}")
        return None

def _handle_response_and_exit(response):
    # Handles the API response, prints details, and exits with appropriate status code.
    if response is None:
        # This means a requests.exceptions.RequestException occurred and was printed earlier
        sys.exit(1)

    response_body = response.text
    http_status_code = response.status_code

    print("-------------------- RESPONSE START --------------------")
    print(response_body)
    print("-------------------- RESPONSE END ----------------------")
    print(f"HTTP Status Code: {http_status_code}")

    if http_status_code >= 400:
        print(f"Error: Server responded with HTTP status {http_status_code}.")
        sys.exit(1)
    else:
        print(f"Request finished successfully (HTTP Status: {http_status_code}).")
        # Successful exit (status code 0) is implicit if not exited above
        # but we can be explicit for clarity if preferred:
        # sys.exit(0)

def main():
    # Main function to orchestrate the API call.
    api_url, auth_credentials = _get_configuration()

    headers = {
        "Accept": "application/json"
    }

    response = _make_api_request(api_url, auth_credentials, headers)
    _handle_response_and_exit(response)

if __name__ == "__main__":
    main()
