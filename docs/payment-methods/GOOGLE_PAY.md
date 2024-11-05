# Google Pay
On this page, you can find additional configuration and a migration guide for Google Pay.

## Drop-in
### Sessions
The integration works out of the box for sessions implementation. Check out the integration guide [here](https://docs.adyen.com/online-payments/build-your-integration/sessions-flow/?platform=Android&integration=Drop-in).

### Advanced
There is no additional configuration required. Follow the [Advanced flow integration guide](https://docs.adyen.com/online-payments/build-your-integration/advanced-flow/?platform=Android&integration=Drop-in).

## Components
Use the following module and component names:
- To import the module use `googlepay`.

```groovy
implementation "com.adyen.checkout:googlepay:YOUR_VERSION"
```

- To launch and show the Component use `GooglePayComponent`.

```kotlin
val component = GooglePayComponent.PROVIDER.get(
    activity = activity, // or fragment = fragment
    checkoutSession = checkoutSession, // Should be passed only for sessions
    paymentMethod = paymentMethod,
    configuration = checkoutConfiguration,
    componentCallback = callback,
)
```

### Sessions
Make sure to follow the Android Components integration guide for sessions integration [here](https://docs.adyen.com/online-payments/build-your-integration/sessions-flow?platform=Android&integration=Components).

### Advanced
Make sure to follow the Android Components integration guide for advanced integration [here](https://docs.adyen.com/online-payments/build-your-integration/advanced-flow/?platform=Android&integration=Components).

## Optional configurations

```kotlin
CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
    …
) {
    googlePay {
        setSubmitButtonVisible(true)
        setMerchantAccount("YOUR_MERCHANT_ACCOUNT")
        setGooglePayEnvironment(WalletConstants.ENVIRONMENT_TEST)
        setMerchantInfo(…)
        setCountryCode("US")
        setAllowedAuthMethods(listOf(AllowedAuthMethods.PAN_ONLY))
        setAllowedCardNetworks(listOf("AMEX", "MASTERCARD"))
        setAllowPrepaidCards(false)
        setAllowCreditCards(true)
        setAssuranceDetailsRequired(false)
        setEmailRequired(true)
        setExistingPaymentMethodRequired(false)
        setShippingAddressRequired(true)
        setShippingAddressParameters(…)
        setBillingAddressRequired(true)
        setBillingAddressParameters(…)
        setTotalPriceStatus("FINAL")
        setGooglePayButtonStyling(…)
    }
}
```

| Method                             | Description                                                                                                                                       |
|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `setSubmitButtonVisible`           | Set to `true` to display the Google Pay button in the component. The default value is `false`.                                                    |
| `setMerchantAccount`               | Sets the merchant account to be put in the payment token from Google to Adyen.                                                                    |
| `setGooglePayEnvironment`          | Sets the environment to be used by Google Pay.                                                                                                    |
| `setMerchantInfo`                  | Sets the information about the merchant requesting the payment.                                                                                   |
| `setCountryCode`                   | Sets the ISO 3166-1 alpha-2 country code where the transaction is processed.                                                                      |
| `setAllowedAuthMethods`            | Sets the supported authentication methods.                                                                                                        |
| `setAllowedCardNetworks`           | Sets the allowed card networks. The allowed networks are automatically configured based on your account settings, but you can override them here. |
| `setAllowPrepaidCards`             | Set to `true` if you support prepaid cards.                                                                                                       |
| `setAllowCreditCards`              | Set to `true` if you support credit cards.                                                                                                        |
| `setAssuranceDetailsRequired`      | Set to `true` if you want to request assurance details.                                                                                           |
| `setEmailRequired`                 | Set to `true` if an email address is required.                                                                                                    |
| `setExistingPaymentMethodRequired` | Set to `true` if an existing payment method is required.                                                                                          |
| `setShippingAddressRequired`       | Set to `true` if a shipping address is required.                                                                                                  |
| `setShippingAddressParameters`     | Allows to configure the shipping address parameters.                                                                                              |
| `setBillingAddressRequired`        | Set to `true` if a billing address is required.                                                                                                   |
| `setBillingAddressParameters`      | Allows to configure the billing address parameters.                                                                                               |
| `setTotalPriceStatus`              | Sets the status of the total price used.                                                                                                          |
| `setGooglePayButtonStyling`        | Allows to configure the styling of the Google Pay button.                                                                                         |

## Migrating to 5.8.0+
It is not necessary to migrate, but 5.8.0 introduced a simplified integration for Google Pay. This new integration among others gets rid of the deprecated `onActivityResult` and includes the Google Pay button. Follow the steps below to migrate from previous 5.x.x versions to 5.8.0:

### 1. Remove deprecated Activity Result code

Add `AdyenComponentView` to your layout and attach the component to it.
```xml
<com.adyen.checkout.ui.core.AdyenComponentView
        android:id="@+id/componentView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```
```kotlin
// Attach the component to the view
binding.componentView.attach(googlePayComponent, lifecycleOwner)

// Or if you use Jetpack Compose
AdyenComponent(googlePayComponent)
```

Now you no longer need activity result related code, so you can clean it up. For example you can remove:
```kotlin
// This function can be deleted
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googlePayComponent.handleActivityResult(resultCode, data)
}
```

### 2. Display Google Pay button

If you want to keep displaying a button yourself, then you have to replace the call to `googlePayComponent.startGooglePayScreen(…)` with `googlePayComponent.submit()`.

To let the component display the Google Pay button inside the `AdyenComponentView` remove your own button and adjust your configuration:
```kotlin
CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
    …
) {
    googlePay {
        setSubmitButtonVisible(true)
        setGooglePayButtonStyling(…) // Optionally style the button
    }
}
```

The `com.google.pay.button:compose-pay-button` dependency can now also be removed from your `build.gradle`.

### 3. Google Pay availability check

The `GooglePayComponent` now does the availability check on initialization and will return a `GooglePayUnavailableException` in `onError`. You no longer need to manually call `GooglePayComponent.PROVIDER.isAvailable(…)`.
