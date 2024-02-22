# Example app

## Running the app

1. Duplicate `default.local.gradle` and name it `local.gradle`. Make sure the file is placed in the example-app directory.
2. Replace the predefined values:
    * `MERCHANT_ACCOUNT` your merchant account identifier.
    * `MERCHANT_SERVER_URL` the URL to your server or use https://checkout-test.adyen.com/v71/ directly (check [here](https://docs.adyen.com/api-explorer/Checkout/latest/overview) for the latest version). 
    * `API_KEY_HEADER_NAME` the name of the API key header as expected by the server. Leave empty if your server handles the API key.
    * `CHECKOUT_API_KEY` your API key. Find out how to obtain it [here](https://docs.adyen.com/development-resources/api-credentials/#generate-api-key). Leave empty if your server handles the API key.
    * `CLIENT_KEY` your client key. Find out how to obtain it [here](https://docs.adyen.com/development-resources/client-side-authentication/#get-your-client-key).
    * `SHOPPER_REFERENCE` your reference to uniquely identify this shopper.
3. Sync the project.
4. Run on any device or emulator.

**Warning**: this code is for demonstration purposes only. For actual integration, you should directly connect to our APIs from your server, not from your app as this might expose your credentials.
