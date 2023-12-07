[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- The [BcmcComponent](https://adyen.github.io/adyen-android/bcmc/com.adyen.checkout.bcmc/-bcmc-component/index.html) now supports co-badged Bancontact cards and card brand detection.
  - The [BcmcComponentState](https://adyen.github.io/adyen-android/bcmc/com.adyen.checkout.bcmc/-bcmc-component-state/index.html) now contains 3 extra fields: `cardBrand`, `binValue` and `lastFourDigits`.
- You can now override payment method names in Drop-in by using [DropInConfiguration.Builder.overridePaymentMethodName(type, name)](https://adyen.github.io/adyen-android/drop-in/com.adyen.checkout.dropin/-drop-in-configuration/-builder/override-payment-method-name.html).
- For stored cards, Drop-in now shows the card name (for example **Visa** or **Mastercard**) instead of **Credit Card**.
- Now it is possible to show installment amounts for card payments using [InstallmentConfiguration.showInstallmentAmount](https://adyen.github.io/adyen-android/card/com.adyen.checkout.card/-installment-configuration/show-installment-amount.html) in [CardConfiguration.Builder.setInstallmentConfigurations()](https://adyen.github.io/adyen-android/card/com.adyen.checkout.card/-card-configuration/-builder/set-installment-configurations.html).
- For gift cards, you can now hide the PIN text field by setting [GiftCardConfiguration.Builder.setPinRequired()](https://adyen.github.io/adyen-android/giftcard/com.adyen.checkout.giftcard/-gift-card-configuration/-builder/set-pin-required.html) to **false**.
- For Google Pay:
  - When initializing the [Google Pay button](https://docs.adyen.com/payment-methods/google-pay/android-component/#2-show-the-google-pay-button), you can now use [GooglePayComponent.getGooglePayButtonParameters()](https://adyen.github.io/adyen-android/googlepay/com.adyen.checkout.googlepay/-google-pay-component/get-google-pay-button-parameters.html) to get the `allowedPaymentMethods` attribute.
  - You can now use [AllowedAuthMethods](https://adyen.github.io/adyen-android/googlepay/com.adyen.checkout.googlepay/-allowed-auth-methods/index.html) and [AllowedCardNetworks](https://adyen.github.io/adyen-android/googlepay/com.adyen.checkout.googlepay/-allowed-card-networks/index.html) to easily access to the possible values for [GooglePayConfiguration.Builder.setAllowedAuthMethods()](https://adyen.github.io/adyen-android/googlepay/com.adyen.checkout.googlepay/-google-pay-configuration/-builder/set-allowed-auth-methods.html) and [GooglePayConfiguration.Builder.setAllowedCardNetworks()](https://adyen.github.io/adyen-android/googlepay/com.adyen.checkout.googlepay/-google-pay-configuration/-builder/set-allowed-card-networks.html).

## Fixed
- Fixed a bug where components would not be displayed in Jetpack Compose lazy lists.

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [AndroidX Compose Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.8.0)    | **1.8.0**                     |
  | [Material Design](https://m2.material.io/)                                                             | **1.10.0**                    |
  | [Gradle](https://docs.gradle.org/8.4/release-notes.html)                                               | **8.4**                       |
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **8.1.2**                     |
  | [AndroidX Compose BoM](https://developer.android.com/jetpack/compose/bom/bom-mapping)                  | **2023.10.01**                |
  | [AndroidX Recyclerview](https://developer.android.com/jetpack/androidx/releases/recyclerview#1.3.2)    | **1.3.2**                     |
  | [AndroidX Fragment](https://developer.android.com/jetpack/androidx/releases/fragment#1.6.2)            | **1.6.2**                     |
