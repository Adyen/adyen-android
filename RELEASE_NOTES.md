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
- You can now use [Adyen Test Cards Android](https://github.com/Adyen/adyen-testcards-android) to prefill test payment method information, to test your integration more quickly.
- For Twint:
  - You can now [store payment details](/docs/payment-methods/TWINT.md#optional-configurations) and [pay with stored payment details](/docs/payment-methods/TWINT.md#stored-twint-payments).
> [!WARNING]
> For Twint Components integrations, you must now use [`TwintComponent`](/docs/payment-methods/TWINT.md) instead of `InstantPaymentComponent`.
- For French meal vouchers, the following payment method types are now available:
    - Up. Payment method type: **mealVoucher_FR_groupeup**.
    - Natixis. Payment method type: **mealVoucher_FR_natixis**.
    - Sodexo. Payment method type: **mealVoucher_FR_sodexo**.  
    - Learn to [configure French meal vouchers](/docs/payment-methods/FRENCH_MEAL_VOUCHER.md).
- For [API-only integrations with encrypted card details](https://docs.adyen.com/payment-methods/cards/custom-card-integration/?tab=candroid_3), you can now use the following classes to validate corresponding fields:

  | Class                       | Description                        |
  |-----------------------------|------------------------------------|
  | `CardNumberValidator`       | Validates the card number field.   |
  | `CardExpiryDateValidator`   | Validates the expiry date field.   |
  | `CardSecurityCodeValidator` | Validates the security code field. |

- Support for the following locales:

  | Locale     | Values    |
  |------------|-----------|
  | Catalan    | **ca-ES** |
  | Icelandic  | **is-IS** |
  | Bulgarian  | **bg-BG** |
  | Estonian   | **et-EE** |
  | Latvian    | **lv-LV** |
  | Lithuanian | **lt-lT** |

## Fixed
- When parsing JSON objects with explicit null values, JSON deserialization no longer returns the coerced `null` string.

## Improved
- For UPI Intent, if the shopper selects the **Continue** button without selecting an UPI option, an error message now shows.
- For Drop-in, in the navigation bar, the accessibility of the **Back/Close** button is improved.

## Changed
- For Drop-in, headers of preselected stored payment screen and payment methods list screen are updated.
- When you set `analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE)`, only [Drop-in/Components analytics](https://docs.adyen.com/online-payments/analytics-and-data-tracking#data-we-are-collecting) are not sent to Adyen.
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Adyen 3DS2](https://github.com/Adyen/adyen-3ds2-android/releases/tag/2.2.21)                          | **2.2.21**                    |
  | [Cash App Pay](https://github.com/cashapp/cash-app-pay-android-sdk/releases/tag/v2.5.0)                | **2.5.0**                     |
  | [Android Gradle Plugin](https://developer.android.com/build/releases/past-releases/agp-8-5-0-release-notes#android-gradle-plugin-8.5.1)                          | **8.5.1**                    |
  | [AndroidX Fragment](https://developer.android.com/jetpack/androidx/releases/fragment#1.8.3)            | **1.8.3**                     |
  | [AndroidX Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.9.2)            | **1.9.2**                     |
  | [AndroidX Compose Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.9.2)    | **1.9.2**                     |
  | [AndroidX Compose BOM](https://developer.android.com/develop/ui/compose/bom/bom-mapping)               | **2024.06.00**                |
  | [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.3)          | **2.8.3**                     |
  | [AndroidX Lifecycle ViewModel Compose](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.3)  | **2.8.3**                     |
  | [AndroidX AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat#1.7.0)          | **1.7.0**                     |

## Deprecated
- The style for payment method list headers. Use the new style instead.

  | Previous                                   | Now                                        |
  |--------------------------------------------|--------------------------------------------|
  | `AdyenCheckout.TextAppearance.HeaderTitle` | `AdyenCheckout.TextAppearance.HeaderLabel` |

- The `com.adyen.checkout.instant.ActionHandlingMethod` method. Use the new method instead.

  | Previous                                          | Now                                                       |
  |---------------------------------------------------|-----------------------------------------------------------|
  | `com.adyen.checkout.instant.ActionHandlingMethod` | `com.adyen.checkout.components.core.ActionHandlingMethod` |
