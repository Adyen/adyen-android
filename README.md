# Adyen SDK for Android
Want to add checkout to your Android app? No matter if your shopper wants to pay with a card (optionally with 3D Secure & One-click), wallet or a local payment method – all can be integrated in the same way, using the Adyen SDK. The Adyen SDK encrypts sensitive card data and sends it directly to Adyen in order to keep your PCI scope limited.

This README provides the usage manual for the SDK itself. For the full documentation, including the server side implementation guidelines, refer to the [Android SDK Guide](https://docs.adyen.com/developers/checkout/android-sdk).

To give you as much flexibility as possible, our Android SDK can be integrated in two ways:

* **Quick integration** – Benefit from a fully optimized out-of-the-box UI with the SDK.
* **Custom integration** – Design your own UI while leveraging the underlying functionality of the SDK.

## Quick integration
![Credit Card](https://user-images.githubusercontent.com/8339684/42883150-0aeec504-8a9b-11e8-9a23-426ce4771481.gif)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
![One-Click](https://user-images.githubusercontent.com/8339684/42883151-0badfece-8a9b-11e8-94d9-41320e757b01.gif)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

#### Installation
Import the quick integration modules by adding these lines to your build.gradle file.

```groovy
final checkoutVersion = "2.2.0"
implementation "com.adyen.checkout:ui:${checkoutVersion}"
implementation "com.adyen.checkout:nfc:${checkoutVersion}" // Optional; Integrates NFC card reader in card UI
implementation "com.adyen.checkout:wechatpay:${checkoutVersion}" // Optional; Integrates support for WeChat Pay
```

#### Getting started
The Quick integration of the SDK provides UI components for payment method selection, entering payment method details (credit card entry form, iDEAL issuer selection, etc.). To get started, use the `CheckoutController` class to start the payment:

```java
CheckoutController.startPayment(/*Activity*/ this, new CheckoutSetupParametersHandler() {
    @Override
    public void onRequestPaymentSession(@NonNull CheckoutSetupParameters checkoutSetupParameters) {
        // TODO: Forward to your own server and request the payment session from Adyen with the given CheckoutSetupParameters.
    }

    @Override
    public void onError(@NonNull CheckoutException checkoutException) {
        // TODO: Handle error.
    }
});
```

Send the `CheckoutSetupParameters` to your own server, which then needs to forward this data, among some other parameters, to the Adyen Checkout API. See the [API Explorer](https://docs.adyen.com/api-explorer/#/PaymentSetupAndVerificationService/v32/paymentSession) for more details.

##### - Generating StartPaymentParameters
After receiving the payment session data from your own server, use the `CheckoutController` to handle the payment session response:

```java
String encodedPaymentSession = ...;
CheckoutController.handlePaymentSessionResponse(/*Activity*/ this, encodedPaymentSession, new StartPaymentParametersHandler() {
    @Override
    public void onStartPaymentParameters(@NonNull StartPaymentParameters startPaymentParameters) {
        // TODO: Start the desired checkout process.
    }

    @Override
    public void onError(@NonNull CheckoutException checkoutException) {
        // TODO: Handle error.
    }
});
```

##### - Starting the desired checkout process
With the `StartPaymentParameters`, you can now start the checkout process:
```java
PaymentMethodHandler paymentMethodHandler = CheckoutController.getCheckoutHandler(startPaymentParameters);
paymentMethodHandler.handlePaymentMethodDetails(/* Activity */ this, REQUEST_CODE_CHECKOUT);
```

##### - Handling onActivityResult(int, int, Intent)
After the payment has been processed, you will receive the result in your calling Activity:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_CHECKOUT) {
        if (resultCode == PaymentMethodHandler.RESULT_CODE_OK) {
            PaymentResult paymentResult = PaymentMethodHandler.Util.getPaymentResult(data);
            // Handle PaymentResult.
        } else {
            CheckoutException checkoutException = PaymentMethodHandler.Util.getCheckoutException(data);

            if (resultCode == PaymentMethodHandler.RESULT_CODE_CANCELED) {
                // Handle cancellation and optional CheckoutException.
            } else {
                // Handle CheckoutException.
            }
        }
    }
}
```


#### Configuration

##### - Changing the Theme
By default, we extend your existing `AppTheme` in our quick integration. If that does not give you the results that you would like to have, you can override the theme by adding a new theme to your `styles.xml` file:

```xml
<style name="AppTheme.Checkout" parent="Theme.AppCompat[...]">
    <item name="colorPrimary">@color/checkoutColorPrimary</item>
    <item name="colorPrimaryDark">@color/checkoutColorPrimaryDark</item>
    <item name="colorAccent">@color/checkoutColorAccent</item>
</style>
```

> The default AppTheme (or the AppTheme.Checkout) must have an AppCompat Theme as parent.

##### - Changing the orientation
You can set the screen orientation by overriding the appropriate resource:
```xml
<resources>
    <integer name="checkout_screenOrientation">1</integer>
</resources>
```
The number corresponds to `ActivityInfo.ScreenOrientation`:
- landscape: 0 (`ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE`)
- portrait: 1 (`ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`)
- etc.

##### - Changing the font
By default, we use the font that is declared in the theme that is used for checkout (so either `AppTheme` or `AppTheme.Checkout`). You can override it by providing a new font resource:
```xml
<style name="AppTheme" parent="Theme.AppCompat[...]">
    [...]
    <item name="android:fontFamily">@font/montserrat</item>
    <item name="fontFamily">@font/montserrat</item>
</style>
```


## Custom integration
#### Installation
Import the following modules by adding these line to your `build.gradle` file.
```groovy
final checkoutVersion = "2.2.0"
implementation "com.adyen.checkout:core:${checkoutVersion}"
implementation "com.adyen.checkout:core-card:${checkoutVersion}" // Optional; Required for processing card payments.
implementation "com.adyen.checkout:nfc:${checkoutVersion}" // Optional; Enables reading of card information with the device"s NFC chip.
implementation "com.adyen.checkout:util:${checkoutVersion}" // Optional; Collection of utility classes.
```

#### Getting started
It is possible to have more control over the payment flow — presenting your own UI for specific payment methods, filtering a list of payment methods, or implementing your own unique checkout experience. To get started, use the `PaymentController` class to start the payment:

```java
PaymentController.startPayment(/*Activity*/ this, new PaymentSetupParametersHandler() {
    @Override
    public void onRequestPaymentSession(@NonNull PaymentSetupParameters paymentSetupParameters) {
        // TODO: Forward to your own server and request the payment session from Adyen with the given PaymentSetupParameters.
    }

    @Override
    public void onError(@NonNull CheckoutException checkoutException) {
        // TODO: Handle error.
    }
});
```

Send the `PaymentSetupParameters` to your own server, which then needs to forward this data, among some other parameters, to the Adyen Checkout API. See the [API Explorer](https://docs.adyen.com/api-explorer/#/PaymentSetupAndVerificationService/v32/paymentSession) for more details.

##### - Create a PaymentSession
After receiving the Base64 encoded payment session data from your own server, use the `PaymentController` to handle the payment session response:

```java
String encodedPaymentSession = ...;
PaymentController.handlePaymentSessionResponse(/*Activity*/ this, encodedPaymentSession, new StartPaymentParametersHandler() {
    @Override
    public void onPaymentInitialized(@NonNull StartPaymentParameters startPaymentParameters) {
        PaymentReference paymentReference = startPaymentParamters.getPaymentReference();
        // TODO: Use the PaymentReference to retrieve a PaymentHandler (see next section).
    }

    @Override
    public void onError(@NonNull CheckoutException checkoutException) {
        // TODO: Handle error.
    }
});
```

With the `PaymentReference` you can retrieve an instance of a `PaymentHandler`. Here you can attach the desired Observers and Handlers in the scope of the current Activity (Observers and Handlers will automatically be removed when the `Activity` is destroyed):

> `PaymentReference` is `Parcelable`, so you can pass it along to another `Activity`. 

```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    PaymentReference paymentReference = getIntent().getParcelableExtra("EXTRA_PAYMENT_REFERENCE");
    mPaymentHandler = paymentReference.getPaymentHandler(/*Activity*/ this);
    
    // Observe data
    mPaymentHandler.getNetworkingStateObservable().observe(/*Activity*/ this, new Observer<NetworkingState>() {
        @Override
        public void onChanged(@NonNull NetworkingState networkingState) {
            // TODO: Handle NetworkingState.
        }
    });
    mPaymentHandler.getPaymentSessionObservable().observe(/*Activity*/ this, new Observer<PaymentSession>() {
        @Override
        public void onChanged(@NonNull PaymentSession paymentSession) {
            // TODO: Handle PaymentSession change, i.e. refresh your list of PaymentMethods.
        }
    });
    mPaymentHandler.getPaymentResultObservable().observe(/*Activity*/ this, new Observer<PaymentResult>() {
        @Override
        public void onChanged(@NonNull PaymentResult paymentResult) {
            // TODO: Handle PaymentResult.
        }
    });
    
    // Handle data
    mPaymentHandler.setRedirectHandler(/*Activity*/ this, new RedirectHandler() {
        @Override
        public void onRedirectRequired(@NonNull RedirectDetails redirectDetails) {
            // TODO: Handle RedirectDetails.
        }
    });
    mPaymentHandler.setAdditionalDetailsHandler(/*Activity*/ this, new AdditionalDetailsHandler() {
        @Override
        public void onAdditionalDetailsRequired(@NonNull AdditionalDetails additionalDetails) {
            // TODO: Handle AdditionalDetails.
        }
    });
    mPaymentHandler.setErrorHandler(/*Activity*/ this, new ErrorHandler() {
        @Override
        public void onError(@NonNull CheckoutException error) {
            // TODO: Handle CheckoutException.
        }
    });
}
```

In order to make a payment, select a `PaymentMethod` and retrieve the according `PaymentMethodDetails` from the shopper.

```java
PaymentMethod paymentMethod = ...; // The user selected PaymentMethod.
PaymentMethodDetails paymentMethodDetails = ...; // The user entered payment method details, e.g. `CardDetails` for card payments. You can check what's required and optional to submit by looking at `PaymentMethod.getInputDetails()`.
mPaymentHandler.initiatePayment(paymentMethod, paymentMethodDetails);
```


## Proguard Rules
If you are using ProGuard add the following options:
```proguard
#### Adyen Checkout ####
-keep class com.adyen.checkout.core.** { *; }
-dontwarn com.adyen.checkout.nfc.**
-dontwarn com.adyen.checkout.googlepay.**
-dontwarn com.adyen.checkout.wechatpay.**
```


## Example App - Quick Start
Run `bash <(curl -s https://raw.githubusercontent.com/Adyen/adyen-android/master/setup.sh)`

## See also
 * [Android SDK Guide](https://docs.adyen.com/developers/checkout/android-sdk)

## License
This repository is open source and available under the MIT license. For more information, see the LICENSE file.
