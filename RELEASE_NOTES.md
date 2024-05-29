[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Improved` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## New)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- For external redirects, you can now [customize the colors of the toolbar and navigation bar](docs/UI_CUSTOMIZATION.md#styling-custom-tabs) displayed in [Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs).
- TWINT is now supported with a native flow, and you no longer need to redirect shoppers through the browser. To use the redirect flow, set the following configuration:
```kotlin
CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
    ..
) {
    // Optionally pass the payment method type to only configure it for the specific payment method.
    instantPayment(PaymentMethodTypes.TWINT) {
        setActionHandlingMethod(ActionHandlingMethod.PREFER_WEB)
    }
}
```

## Fixed
- Fixed some memory leaks.
- In case of a debug build, Drop-in no longer overrides the log level.
- For cards, when a shopper does not select an address, the address lookup function now displays a validation error.
- Actions no longer crash when your app uses obfuscation.
- When handling a 3D Secure 2 challenge using Checkout API v66 or earlier, Drop-in no longer throws an error.
- If the app process unexpectedly terminates when handling actions, the state is now restored and you can proceed with the payment flow.
- For `/sessions`, fixed an issue where the `setEnableRemovingStoredPaymentMethods` flag in the [Drop-in configuration](https://docs.adyen.com/online-payments/build-your-integration/sessions-flow/?platform=Android&integration=Drop-in#3-optional-add-a-configuration-object) was ignored.

## Changed
- The phone number input field in the payment form now shows ISO codes instead of flags.
- The UI elements that were previously labelled **Country** are now **Country/Region**.
- Dependency versions:
  | Name                                                                                                         | Version                       |
  |--------------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Adyen 3DS2](https://github.com/Adyen/adyen-3ds2-android/releases/tag/2.2.18)                                | **2.2.18**                    |
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                          | **8.3.2**                     |
  | [AndroidX Browser](https://developer.android.com/jetpack/androidx/releases/browser#1.8.0)                    | **1.8.0**                     |
  | [AndroidX Compose Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.9.0)          | **1.9.0**                     |
  | [AndroidX Compose BoM](https://developer.android.com/develop/ui/compose/bom/bom-mapping)                     | **2024.04.01**                |
  | [AndroidX Compose Compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler#1.5.12) | **1.5.12**                    |
  | [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.7.0)                | **2.7.0**                     |
  | [Google Pay](https://developers.google.com/pay/api/android/support/release-notes#feb-24)                     | **19.3.0**                    |
  | [Google Pay Compose Button](https://github.com/google-pay/compose-pay-button/releases/tag/v1.0.0)            | **1.0.0**                     |
  | [Kotlin](https://github.com/JetBrains/kotlin/releases/tag/v1.9.24)                                           | **1.9.24**                    |
  | [Kotlin coroutines](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.8.0)                         | **1.8.0**                     |
