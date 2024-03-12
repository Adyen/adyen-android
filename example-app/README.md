# Example app

The `example-app` module is used for development and testing purposes. It should not be used as a template for your own integration. Check out the [docs](https://docs.adyen.com/online-payments/build-your-integration/) for best practices on integration.

## Running the app

Steps to run the example app:
1. Build a server that acts as a proxy between the app and the Adyen Checkout API.
    * Your server should mirror the necessary endpoints for your flow (for example [/sessions](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) for the sessions flow and [/paymentMethods](https://docs.adyen.com/api-explorer/Checkout/latest/post/paymentMethods), [/payments](https://docs.adyen.com/api-explorer/Checkout/latest/post/payments) and [/payments/details](https://docs.adyen.com/api-explorer/Checkout/latest/post/payments/details) for the advanced flow).
    * The API key should be managed on the server.
2. Duplicate `example.local.gradle` and name it `local.gradle`. Make sure the file is placed in the `example-app` directory.
3. Replace the predefined values:
    * `MERCHANT_SERVER_URL`: the URL to your server.
    * `CLIENT_KEY`: your client key. Find out how to obtain it [here](https://docs.adyen.com/development-resources/client-side-authentication/#get-your-client-key).
    * `MERCHANT_ACCOUNT`: your merchant account identifier.
    * `AUTHORIZATION_HEADER_NAME`: the name of the authorization header as expected by your server. You can use an empty string if this is not applicable for you.
    * `AUTHORIZATION_HEADER_VALUE`: the value for the authorization header. You can use an empty string if this is not applicable for you.
4. Sync the project.
5. Run on any device or emulator.

> [!WARNING]
> In case you don't have your own server you can connect to the Adyen Checkout API directly for testing purposes only. Be aware this could potentially leak your credentials, the market-ready application must never connect to Adyen API directly.

To connect to the Adyen Checkout API directly you can use the following values:
* `MERCHANT_SERVER_URL`: `https://checkout-test.adyen.com/{VERSION}/` (check [here](https://docs.adyen.com/api-explorer/Checkout/latest/overview) for the latest version).
* `AUTHORIZATION_HEADER_NAME`: `x-api-key`.
* `AUTHORIZATION_HEADER_VALUE`: your API key.
