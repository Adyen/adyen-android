[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Fixed` `Improved` `Changed` `Deprecated` `Removed`)
[//]: # (Example:)
[//]: # (## New)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- Simplified integration for Google Pay:
  - The Component now includes the Google Pay button, and you no longer have to display the button with your own configuration.
  - The Component now does the availability check on initialization.
  - The deprecated `onActivityResult` is no longer needed.

  See the [migration guide](docs/payment-methods/GOOGLE_PAY.md#migrating-to-590) to learn about the changes you have to make to your integration to support Google Pay on this version.

- Drop-in now shows a toolbar on more intermediary screens to let shoppers navigate back easily.

## Changed
- For [native 3D Secure 2](https://docs.adyen.com/online-payments/3d-secure/native-3ds2/?platform=Android&integration=Drop-in&version=latest), when a shopper cancels the payment during the payment flow, the `onAdditionalDetails()` event is now triggered. What this means for your integration depends on whether you already make a `/payments/details` call to handle 3D Secure 2 errors:
  - If yes, you do not need to make any changes to your integration. You can remove the `Cancelled3DS2Exception` handler.
  - If not, update your integration to make a `/payments/details` request to get the details of the canceled transaction.
- Dependency versions:
  | Name                                                                                                                                    | Version        |
  |-----------------------------------------------------------------------------------------------------------------------------------------|----------------|
  | [Android Gradle Plugin](https://developer.android.com/build/releases/past-releases/agp-8-7-0-release-notes#android-gradle-plugin-8.7.3) | **8.7.3**      |
  | [AndroidX Compose BoM](https://developer.android.com/develop/ui/compose/bom/bom-mapping)                                                | **2024.12.01** |
  | [AndroidX ConstraintLayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout#constraintlayout-2.2.0)            | **2.2.0**      |
  | [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.7)                                           | **2.8.7**      |
  | [AndroidX Lifecycle ViewModel Compose](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.7)                         | **2.8.7**      |
  | [KotlinX Coroutines Play Services](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.9.0)                                     | **1.9.0**      |

## Deprecated
- The styles and strings for the Cash App Pay loading indicator. Use the new styles and strings instead.
  | Previous                                                  | Now                                                              |
  |-----------------------------------------------------------|------------------------------------------------------------------|
  | `AdyenCheckout.CashAppPay.ProgressBar`                    | `AdyenCheckout.ProcessingPaymentView.ProgressBar`                |
  | `AdyenCheckout.CashAppPay.WaitingDescriptionTextView`     | `AdyenCheckout.ProcessingPaymentView.WaitingDescriptionTextView` |
  | `cash_app_pay_waiting_text`                               | `checkout_processing_payment` |

## Repository Maintenance
- We are changing the default branch of our SDK repository from `develop` to `main`. If you are using our SDK repository and are working with the `develop` branch, we recommend switching to the `main` branch. The `develop` branch will be removed within the next few weeks.
