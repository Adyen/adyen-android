[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Breaking Changes

- For Drop in, you can no longer get the result using `onActivityResult()`. Drop-in now uses the [Activity Result API](https://developer.android.com/training/basics/intents/result) instead.
- For Components, you can no longer use `requiresView()` for action Component providers.
- Restructured packages and moved classes. If you're upgrading, you only need to re-import the them because most classes names haven't changed.
- All public classes that should not be directly used are now marked as internal.
- You now must configure `environment`. The default value is no longer **TEST**.
- Build configuration: [`compileSdkVersion` and `targetSdkVersion`](https://developer.android.com/about/versions/11/setup-sdk#update-build): **33**.
- Dependency versions:
  | Name                                                                                                   | Version    |
  |--------------------------------------------------------------------------------------------------------|------------|
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **7.4.2**  |
  | [Kotlin Gradle plugin](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android)                 | **1.8.21** |
  | [Appcompat](https://developer.android.com/jetpack/androidx/releases/appcompat)                         | **1.6.1**  |
  | [Kotlin coroutines](https://kotlinlang.org/docs/coroutines-overview.html)                              | **1.6.4**  |
  | [AndroidX Fragment](https://developer.android.com/jetpack/androidx/releases/fragment)                  | **1.5.7**  |
  | [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)                | **2.5.1**  |
  | [AndroidX Recyclerview](https://developer.android.com/jetpack/androidx/releases/recyclerview)          | **1.3.0**  |
  | [AndroidX Constraintlayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout)  | **2.1.4**  |
  | [Material Design](https://m2.material.io/)                                                             | **1.8.0**  |

## Removed
- `requiresConfiguration()` in action Component providers. For all Components, configuration is optional.
- `CardConfiguration.Builder.setAddressVisibility()`. Use `CardConfiguration.Builder.setAddressConfiguration()` instead.
- `Environment.LIVE`. Use the same live environment as your backend instead. You can find that value in your Customer Area.
- `saveState()` and `restoreState()` in action components. The component will automatically handle the state now.
- `DropInServiceResult.Action` constructor from JSON string. Use the constructor with the `Action` and `Action.SERIALIZER` instead.

## New
- Sessions flow using the single `/sessions` request is now supported.
- For Components:
    - Payment method Components now handle actions. You no longer need a payment Component and action Components for a payment method with additional actions.
    - The `GenericActionComponent` that can handle all action types. You no longer need to implement separate Components for redirects and 3D Secure 2 authentication, for example.
    - A **Pay** button that you can configure to be hidden.
    - The `submit()` method that can be used to add your own pay/submit button.
    - You can now add `amount` to the configuration to show it on the pay/submit button.
    - The `onSubmit()` event that gets emitted when the shopper pays.
- When the shopper is redirected back from an external app or website, an intermediate view with a loading spinner and a **Cancel** button now shows. The shopper can select to cancel the redirect back to your app.
- Localisation for the Portuguese (Portugal) language.
- Payment methods:
    - [ACH Direct Debit](https://docs.adyen.com/payment-methods/ach-direct-debit). [Payment method type](https://docs.adyen.com/payment-methods/payment-method-types): **ach**.
    - [DuitNow](https://docs.adyen.com/payment-methods/duitnow). Payment method type: **duitnow**.
    - [Open banking](https://docs.adyen.com/payment-methods/open-banking). Payment method type: **paybybank**.
    - [Online banking Czech Republic](https://docs.adyen.com/payment-methods/online-banking-czech-republic). Payment method type: **onlineBanking_CZ**.
    - [Online banking Slovakia](https://docs.adyen.com/payment-methods/online-banking-slovakia). Payment method type: **onlineBanking_SK**.
    - [Pay Now](https://docs.adyen.com/payment-methods/paynow). Payment method type: **paynow**.
    - [PromptPay](https://docs.adyen.com/payment-methods/promptpay). Payment method type: **promptpay**.
    - [UPI](https://docs.adyen.com/payment-methods/upi):
        - UPI Collect: The shopper pays by entering their virtual payment address (VPA). Payment method type: **upi_collect**.
        - UPI QR: The shopper pays by scanning a QR code. Payment method type: **upi_qr**.
- Express payment methods like PayPal and Klarna. These payment methods don't require the shopper to enter their payment details before they pay. Use `InstantPaymentComponent`.

## Changed
- For cards:
    - The supported brand logo icons now show below the card number input field.
    - US Debit brand logo icons no longer show.
- For Drop-in, values set in `DropInConfiguration` now override conflicting configurations for individual payment methods.
- For Google Pay, you can now set `GooglePayConfiguration.merchantAccount` to override the `gatewayMerchantId` configured in your Customer Area. For Advanced flow, this is the `paymentMethod.configuration.gatewayMerchantId` parameter in the `/paymentMethods` response.
- For Components, when a payment method doesn't require input from the shopper, the Component that launches automatically returns the `onSubmit()` callback. For example, for the stored cards without a CVC input field.
- For gift cards and partial payments, you must now implement `onBalanceCheck()` and `onRequestOrder()` to launch payment methods with an order and make [partial payments](https://docs.adyen.com/online-payments/partial-payments).

## Improved
- You can now instantiate more than one instance of the same Component within the same lifecycle. Passing the `key` parameter to the Component provider `get()` method. For example, you can show cards and stored cards on the same screen.
- For Components, you no longer need to handle duplicate events such as submit callbacks or errors with because they're only emitted once. [Flows](https://developer.android.com/kotlin/flow) are now used instead of [LiveData](https://developer.android.com/topic/libraries/architecture/livedata).
- More UI theme customization options like dark mode.
- The expiry date input field now has more specific validation rules and error messages.
- The email address input field now has more specific validation rules.

## Fixed
- The redirect flow on Android 11.
