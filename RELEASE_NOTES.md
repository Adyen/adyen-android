[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

⚠️ This is an alpha release. Don't use it to accept payments in your live environment.

## Breaking changes
- All classes in `com.adyen.checkout.action` are now in `com.adyen.checkout.action.core`. If you import the classes, you must update import statements.
- For Components integrations, each payment component no longer handles 3D Secure 2 and WeChat Pay actions. To handle the actions, you must add dependencies for each action:
    ```Groovy
    implementation 'com.adyen.checkout:3ds2:YOUR_VERSION'
    implementation 'com.adyen.checkout:wechatpay:YOUR_VERSION'
    ```
    Exceptions: `CardComponent` and `BcmcComponent` can handle the 3D Secure 2 action. They don't require the additional dependencies.

## New
- Payment method: [Boleto Bancario](https://docs.adyen.com/payment-methods/boleto-bancario). Payment method type: **boletobancario**.
- [Jetpack Compose](https://developer.android.com/jetpack/compose) compatibility.
    - For Drop-in, use the `drop-in-compose` module.
    - For Components, use the `components-compose` module.
- For cards, the `brand` attribute is now included in the `paymentMethod` object for all cards. Previously, it was just included for co-branded ones.
- You can now safely exclude unnecessary third-party dependencies. Do this by excluding the Adyen Checkout module that includes the third-party dependency. For example:
    ```Groovy
    implementation('com.adyen.checkout:drop-in:YOUR_VERSION') {
        exclude group: 'com.adyen.checkout', module: '3ds2'
        exclude group: 'com.adyen.checkout', module: 'wechatpay'
    }
    ```
    Make sure that you don't include a payment method that corresponds to the module that you exclude.

- For Google Pay, new configurations in `GooglePayConfiguration`:
  | Function                      | Description                               |
  |-------------------------------|-------------------------------------------|
  | `setAllowCreditCards`         | Specify if you allow credit cards.        |
  | `setAssuranceDetailsRequired` | Specify if you require assurance details. |

## Improved
- Email input validation.

## Fixed
- `@RestrictTo` annotations no longer cause false [lint check warnings](https://developer.android.com/studio/write/lint).

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **8.0.2** (requires Java 17)  |
  | [Kotlin Gradle plugin](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.android)                 | **1.8.22**                    |
  | [AndroidX Fragment](https://developer.android.com/jetpack/androidx/releases/fragment)                  | **1.6.0**                     |
  | [Material Design](https://m2.material.io/)                                                             | **1.9.0**                     |
  | [Google Pay](https://developers.google.com/pay/api/android/support/release-notes#jun-22)               | **19.2.0**                    |
