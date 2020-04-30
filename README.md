# Adyen Components for Android

Adyen Components for Android allows you to accept in-app payments by providing you with the building blocks you need to create a checkout experience.

For an overview of how you can integrate with Adyen on Android check out the [Documentation Website][docs.android]

<br/>

![DropIn Preview](https://docs-admin.is.adyen.com/user/pages/docs/01.checkout/07.android/01.drop-in/dropin-android.jpg)

<br/>

## Installation

The Components are available through [jcenter][dl], you only need to add the Gradle dependency.

### Import with Gradle

Import the Component module for the Payment Method you want to use by adding it to your `build.gradle` file.
For example, for the Drop-in solution you should add:
```groovy
implementation "com.adyen.checkout:drop-in:3.6.5"
```
For a Credit Card component you should add:
```groovy
implementation "com.adyen.checkout:card-ui:3.6.5"
```

## Drop-in

The Drop-in is the implementation that handles the presentation of all available payment methods and the subsequent entry of a customer's payment details. It is initialized with the response of [`/paymentMethods`][apiExplorer.paymentMethods], and provides everything you need to make an API call to [`/payments`][apiExplorer.payments] and [`/payments/details`][apiExplorer.paymentsDetails].

### Usage

The Drop-in requires the response of the `/paymentMethods` endpoint to be initialized. To pass the response to Drop-in, decode the response to the `PaymentMethodsApiResponse` class.

You can provide the raw JSONObject to the `SERIALIZER` object to deserialize the data.
```kotlin
val paymentMethodsApiResponse = PaymentMethodsApiResponse.SERIALIZER.deserialize(jsonObject)
```

The Drop-in relies on you to implement the calls to your server.
When calling [`/payments`][apiExplorer.payments] or [`/payments/details`][apiExplorer.paymentsDetails] is required, it will trigger an intent to the `DropInService` which you need to extend.
The data comes as a `JSONObject` that you can use to compose your final `/payments` call on your back end.
After the call, you return a `CallResult` with a type and message, each type expects a certain message.
- ACTION - If the result contains an `action` object, return it in the message to continue the payment flow.
- FINISHED - If there is no `action` the payment flow is finished, the message will be passed along as the result.
- ERROR - If an error happened during the connection.
 
```kotlin
class YourDropInService : DropInService() {
    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        // make /payments call with the component data
        return CallResult(CallResult.ResultType.ACTION, "action JSON object")
    }
    
    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        // make /payments/details call with the component data
        return CallResult(CallResult.ResultType.FINISHED, "Success")
    }
}
```

Don't forget to also add the service your manifest.
```xml
<service
    android:name=".YourDropInService"
    android:permission="android.permission.BIND_JOB_SERVICE"/>
```

Some payment methods need additional configuration. For example, to enable the card form, the Drop-in needs a public key from the Customer Area to be used for encryption. These payment method specific configuration parameters can be set in the `DropInConfiguration`:

```kotlin
val dropInConfiguration = DropInConfiguration.Builder(this@MainActivity,
resultIntent, YourDropInService::class.java)
    .addCardConfiguration(cardConfiguration)
    .build()
```

After serializing the payment methods and creating the configuration, the Drop-in is ready to be initialized. Just call the `.startPayment()` method, the final result sent on the `CallResult` will be added to your `resultIntent` to start your Activity.

```kotlin
DropIn.startPayment(this@YourActivity, paymentMethodsApiResponse, dropInConfiguration)
```

## Components

In order to have more flexibility over the checkout flow, you can use our Components to present each payment method individually in your own Activity.

To do that you need the data of that specific payment method parsed to the `PaymentMethod` class, and to create the configuration object.

```kotlin
val cardConfiguration =
    CardConfiguration.Builder(Locale.getDefault(), resources.displayMetrics, Environment.TEST, "<publicKey>")
    .build()
        
val cardComponent = CardComponent.PROVIDER.get(this@YourActivity, paymentMethod, cardConfiguration)
```

Then you need to add the Component View to your layout.
```xml
<com.adyen.checkout.card.CardView 
        android:layout_width="wrap_content" 
        android:layout_height="wrap_content"/>
```

Then, after the component is initialized, you can attach it to the view to start getting user data.
```kotlin
cardView.attach(cardComponent, this@YourActivity)
```

From this moment you will start receiving updates when the user inputs data. When the data is valid, you can send it to the `/payments` endpoint.
```kotlin
cardComponent.observe(this@MainActivity, Observer { 
    if (it?.isValid == true) {
        // you can now use it.data to send your payments/ request
    }
})
```

## ProGuard

If you use ProGuard or R8, the following rules should be enough to maintain all expected functionality.
Please let us know if you find any issues.

```
-keep class com.adyen.checkout.base.model.** { *; }
-keep class com.adyen.threeds2.** { *; }
-keepclassmembers public class * implements com.adyen.checkout.base.PaymentComponent {
   public <init>(...);
}
```

## See also

* [Android Documentation][docs.android]

* [Adyen Checkout Documentation][docs.checkout]

* [API Reference](https://docs.adyen.com/checkout/api-only/)

## License

This repository is open source and available under the MIT license. For more information, see the LICENSE file.

[docs.checkout]: https://docs.adyen.com/checkout/
[docs.android]: https://docs.adyen.com/checkout/android/
[dl]: https://jcenter.bintray.com/com/adyen/checkout/
[apiExplorer.paymentMethods]: https://docs.adyen.com/api-explorer/#/PaymentSetupAndVerificationService/v46/paymentMethods
[apiExplorer.payments]: https://docs.adyen.com/api-explorer/#/PaymentSetupAndVerificationService/v46/payments
[apiExplorer.paymentsDetails]: https://docs.adyen.com/api-explorer/#/PaymentSetupAndVerificationService/v46/paymentsDetails
