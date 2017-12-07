# Adyen SDK for Android
Want to add a checkout to your Android app? No matter if your shopper wants to pay with a card (optionally with 3D Secure & One-click), wallet or a local payment method – all can be integrated in the same way, using the Adyen SDK. The Adyen SDK enrypts sensitive card data and sends it directly to Adyen, to keep your PCI scope limited.

This README provides the usage manual for the SDK itself. For the full documentation, including the server side implementation guidelines, refer to https://docs.adyen.com/developers/in-app-integration-guide.

## Installation
To integrate the Adyen SDK into your project, import the **core**, **utils** and **ui** module by adding the following lines to your build.gradle file.

```
compile 'com.adyen.checkout:core:1.14.1'
compile 'com.adyen.checkout:utils:1.14.1'
compile 'com.adyen.checkout:ui:1.14.1'
compile 'com.adyen.checkout:cardscan:1.14.1'
```

> For implementing Custom integration, only the **core** module is required. However, you might also want to include the **utils** module to use Adyen's utility methods such as Luhn check, credit card type detection, etc.

To give you as much flexibility as possible, our Android SDK can be integrated in two ways:

* **Quick integration** – Benefit from the SDK out-of-the-box with a fully optimized UI.
* **Custom integration** – Design your own UI while leveraging the underlying functionality of the SDK.

## Quick integration
The Quick integration of the SDK provides UI components for payment method selection, entering payment details (credit card entry form, iDEAL issuer selection, and so on). To get started, you should create and start a `PaymentRequest`:

```java
PaymentRequest paymentRequest = new PaymentRequest(context, paymentRequestListener);
paymentRequest.start();
```

A `PaymentRequest` needs a context and a `PaymentRequestListener` to be instantiated. The `PaymentRequestListener` requires two methods to be implemented: `onPaymentDataRequested` and `onPaymentResult`.

##### - onPaymentDataRequested
The `onPaymentDataRequested` is called to obtain the payment methods. You have to send the SDK token and payment information to your server. For this, use the `AsyncHttpClient` utility class from the SDK. You are expected to call `callback.completionWithPaymentData(response)` after successfully receiving the response from your server, as this enables the SDK to process the payment.

You can make use of the Adyen test server until you have implemented your own server. See below an example of a request to the Adyen test server:

```java
    @Override
    public void onPaymentDataRequested(@NonNull final PaymentRequest paymentRequest, @NonNull String token, @NonNull final PaymentDataCallback paymentDataCallback) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");

		// Provide below the data to identify your app against the server, implement your own protocol (e.g. OAuth 2.0) to use your own server.
        headers.put("x-demo-server-api-key", MERCHANT_API_SECRET_KEY); // Use the provided MERCHANT_API_SECRET_KEY, or obtain from Customer Area.

        final JSONObject jsonObject = new JSONObject();
        try {
            // You always need to get the token from the SDK, even when using your own server.
            jsonObject.put("token", token);
            // Below is dummy data in a format expected by the Adyen test server. When implementing your own server, the data below becomes free format; you can also decide to add it to the payment creation request while sending it from your own server.
            jsonObject.put("returnUrl", "example-shopping-app://");
            jsonObject.put("countryCode", "NL");
            final JSONObject amount = new JSONObject();
            amount.put("value", "17408");
            amount.put("currency", "USD");
            jsonObject.put("amount", amount);
            jsonObject.put("shopperReference", "example.merchant@adyen.com");
            jsonObject.put("channel", "android");
            jsonObject.put("reference", "test-payment");
        } catch (final JSONException jsonException) {
            Log.e("Unexpected error", "Setup failed");
        }
        AsyncHttpClient.post(MERCHANT_SERVER_URL, headers, jsonObject.toString(), new HttpResponseCallback() { // Use https://checkoutshopper-test.adyen.com/checkoutshopper/demoserver/setup
            @Override
            public void onSuccess(final byte[] response) {
                paymentDataCallback.completionWithPaymentData(response);
            }
            @Override
            public void onFailure(final Throwable e) {
                paymentRequest.cancel();
            }
        });
    }
```


##### - onPaymentResult

The `onPaymentResult` method of the `PaymentRequestListener` is called when the SDK has authorized the payment. To find out whether your payment was successfully processed, call `paymentRequestResult.isProcessed`:

```java
@Override
public void onPaymentResult(@NonNull PaymentRequest paymentRequest, @NonNull PaymentRequestResult paymentRequestResult) {
    if (paymentRequestResult.isProcessed() && (
            paymentRequestResult.getPayment().getPaymentStatus() == Payment.PaymentStatus.AUTHORISED
                    || paymentRequestResult.getPayment().getPaymentStatus() == Payment.PaymentStatus.RECEIVED)) {
        Intent intent  = new Intent(context, SuccessActivity.class);
        startActivity(intent);
        finish();
    } else {
        Intent intent  = new Intent(context, FailureActivity.class);
        startActivity(intent);
        finish();
    }
}
```

Keep in mind that `isProcessed` does not mean that the payment succeeded. Therefore, you are supposed to check the payment result. If `isProcessed` returns `false`, you can get the `Throwable` via `paymentRequestResult.getError` method.

## Custom integration

It is possible to have more control over the payment flow — presenting your own UI for specific payment methods, filtering a list of payment methods, or implementing your own unique checkout experience.

For more control, implement the `PaymentRequestDetailsListener` and configure your `PaymentRequest` object accordingly. Note that you also have to implement the methods from the Quick integration.

```java
final PaymentRequest paymentRequest = new PaymentRequest(context, paymentRequestListener, paymentRequestDetailsListener);
paymentRequest.start();
```

##### - onPaymentMethodSelectionRequired

This method gives you two lists of payment methods available for your payment request.  The `recurringMethods` list contains `One-Click` enabled payment methods. Other payment options are present in the `otherMethods` list.

```java
@Override
public void onPaymentMethodSelectionRequired(@NonNull final PaymentRequest paymentRequest, final List<PaymentMethod> recurringMethods, @NonNull final List<PaymentMethod> otherMethods, @NonNull final PaymentMethodCallback callback) {
    // Initialise UI for displaying provided payment methods. The selected method should be notified via PaymentMethodCallback
}
```

##### onRedirectRequired

This method is called only when a payment method, which requires redirection, is selected. If the UI is not handled by SDK, this method is called and then the merchant application is expected to open the provided link. When the operation on this page is completed (e.g. a shopper completed the payment), the application is opened via a `URI`. This URI has to be returned to the SDK via `UriCallback`.

```java
@Override
public void onRedirectRequired(@NonNull final PaymentRequest paymentRequest, final String redirectUrl, @NonNull final UriCallback returnUriCallback) {
    // Open the given redirectUrl using Chrome custom tabs.
    uriCallback = returnUriCallback;
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    CustomTabsIntent customTabsIntent = builder.build();
    customTabsIntent.launchUrl(context, Uri.parse(redirectUrl));
}
```
Note that the launch mode of your activity must be `singleTask` in order to be able to handle redirections. When the operation on the redirected web page is completed, your activity will be opened via a deep link. In this case your activity should override the `onNewIntent` method, and when the new intent is received, it should notify the SDK via `UriCallback`.

##### onPaymentDetailsRequired

This method is called only if payment details are required. For example, if Credit Card is selected as a payment method, then the credit card number, CVC and other details are required. In this case, if the UI is not handled by SDK, the merchant application will be notified via this method. The merchant application is supposed to display a UI for collecting this information, and then it should notify the SDK via `PaymentDetailsCallback`.

```java
@Override
public void onPaymentDetailsRequired(@NonNull final PaymentRequest paymentRequest, @NonNull final Collection<InputDetail> inputDetails, @NonNull final PaymentDetailsCallback callback) {
  // For different payment methods different UI might be required. It is suggested to check selected method via paymentRequest.getPaymentMethod() and display UI accordingly.
  // When all payment details are retrieved, SDK should be notified via PaymentDetailsCallback.
  if (PaymentMethod.Type.CARD.equals(paymentRequest.getPaymentMethod().getType())) {
     Card card = new Card();
     card.setNumber("4111111111111111");
     card.setCardHolderName("checkout shopper");
     card.setCvc("737");
     card.setExpiryMonth("10");
     card.setExpiryYear("2020");
     card.setGenerationTime(new Date());

     try {
       //Create PaymentDetails object from the inputDetails and fill them with the shopper input.
       //Then call callback.completionWithPaymentDetails.
       CreditCardPaymentDetails creditCardPaymentDetails = new CreditCardPaymentDetails(inputDetails);
       creditCardPaymentDetails.fillCardToken(card.serialize(paymentRequest.getPublicKey()));
       creditCardPaymentDetails.fillStoreDetails(true);
       callback.completionWithPaymentDetails(creditCardPaymentDetails);
     } catch (EncrypterException e) {
       e.printStackTrace();
     }
  }
}
```

## Examples

For your convenience, we included the following demo modules into this repository:

* **checkoutdemo** – A functioning demo of the Checkout SDK using the Quick integration.

* **defaultApp** – Also uses the Quick integration, but allows to configure parameters for setting up the payment request.

* **customuiapplication** – An example implementation of the Custom integration where the application fully handles the UI.

* **customwithcheckoutui** – An experimental module where the Custom integration is used with UI elements (fragments, views) from the Checkout UI.


## See also

 * [Complete Documentation](https://docs.adyen.com/developers/in-app-integration?platform=inapp-android)

 * [SDK Reference](https://adyen.github.io/adyen-android/)


## License

This repository is open source and available under the MIT license. For more information, see the LICENSE file.
