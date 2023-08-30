[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

⚠️ This is a beta release. Don't use it to accept payments in your live environment.

For guidance on integrating with this version, have a look at the [integration guide](https://docs.adyen.com/online-payments/build-your-integration/android-5-0-0-beta-release/).

## Breaking changes
- For Drop-in, you can now configure if you show a dialog to dismiss Drop-in with either a finished state or error state.
    - `DropInServiceResult.Error` now requires the `ErrorDialog` parameter.
    - `DropInServiceResult.Finished` now has an optional `FinishedDialog` parameter.
- Analytics feature turned on by default. Find out [what we track and how you can configure it](https://docs.adyen.com/online-payments/analytics-and-data-tracking).
    - The method for setting analytics configuration has changed:

  | v5.0.0-beta01                                                               | Earlier versions                                   |
  |-----------------------------------------------------------------------------|----------------------------------------------------|
  | `setAnalyticsConfiguration(analyticsConfiguration: AnalyticsConfiguration)` | `setAnalyticsEnabled(isAnalyticsEnabled: Boolean)` |

- `PaymentMethodDetails` and its subclasses now have the `checkoutAttemptId` field.
- You can no longer manually instantiate the `Environment` class and the `baseUrl` field has been removed.

## New
- You can now safely exclude any payment method from Drop-in. Do this by excluding the Adyen Checkout module that includes the payment method. For example:
    ```Groovy
    implementation('com.adyen.checkout:drop-in:5.0.0-beta01') {
        exclude group: 'com.adyen.checkout', module: 'card'
        exclude group: 'com.adyen.checkout', module: 'ideal'
    }
    ```
- For cards:
    - The [BIN value callback](https://github.com/Adyen/adyen-android/blob/4fe3ddbfdfa29c702037bcd7d1bf48f4fb965a4d/card/src/main/java/com/adyen/checkout/card/CardComponent.kt#L98) is invoked while the shopper inputs their card number. The callback uses up to the first 8 digits.
    - The [BIN lookup callback](https://github.com/Adyen/adyen-android/blob/4fe3ddbfdfa29c702037bcd7d1bf48f4fb965a4d/card/src/main/java/com/adyen/checkout/card/CardComponent.kt#L108) is invoked when brands are detected on the card.
- When the shopper is redirected to another app or browser, [a new callback](https://github.com/Adyen/adyen-android/blob/a1d5b55ad048581e86eccd8f3f6c95b6c8e6a7eb/action-core/src/main/java/com/adyen/checkout/action/core/internal/ActionHandlingComponent.kt#L48C8-L48C8) is invoked.
- For Drop-in, you can now navigate the shopper back to the payment methods list, for example to load new payment methods. To do this, use [DropInServiceResult.ToPaymentMethodsList](https://github.com/Adyen/adyen-android/blob/a1d5b55ad048581e86eccd8f3f6c95b6c8e6a7eb/drop-in/src/main/java/com/adyen/checkout/dropin/DropInServiceResult.kt#L96).

## Fixed
- QR code payment methods no longer crash in some cases.
- Rotating a device during the redirect flow no longer causes a crash.

## Changed
- The `compileSdkVersion` and `targetSdkVersion` are now set to 34 (Android 14).
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Adyen 3DS2](https://github.com/Adyen/adyen-3ds2-android)                                              | **2.2.15**                    |
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **8.1.1**                     |
  | [AndroidX Browser](https://developer.android.com/jetpack/androidx/releases/browser#1.6.0)              | **1.6.0**                     |
  | [AndroidX Compose BoM](https://developer.android.com/jetpack/compose/bom/bom-mapping)                  | **2023.08.00**                |
  | [AndroidX Compose compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler)  | **1.5.3**                     |
  | [AndroidX Fragment](https://developer.android.com/jetpack/androidx/releases/fragment#1.6.1)            | **1.6.1**                     |
  | [AndroidX Recyclerview](https://developer.android.com/jetpack/androidx/releases/recyclerview#1.3.1)    | **1.3.1**                     |
  | [Cash App Pay](https://github.com/cashapp/cash-app-pay-android-sdk)                                    | **2.3.0**                     |
  | [Kotlin Gradle plugin](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android)                 | **1.9.10**                    |
  | [WeChat Pay](https://developers.weixin.qq.com/doc/oplatform/en/Mobile_App/Access_Guide/Android.html)   | **6.8.0**                     |
