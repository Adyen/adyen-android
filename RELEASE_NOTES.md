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
- You can now override payment method names in drop in by using `DropInConfiguration.Builder.overridePaymentMethodName(type, name)`.
- For stored cards, Drop-in will show the card name ("Visa", "Mastercard"...), instead of "Credit Card".
- Now it is possible to show installment amounts for card payments using `InstallmentConfiguration.showInstallmentAmount` in `CardConfiguration.Builder.setInstallmentConfigurations()`.
- For gift cards, you can now hide the PIN text field using `GiftCardConfiguration.Builder.setPinRequired()`.
- When initializing the [Google Pay button](https://docs.adyen.com/payment-methods/google-pay/android-component/#2-show-the-google-pay-button), you can now use `GooglePayComponent.getGooglePayButtonParameters()` to get the `allowedPaymentMethods` attribute.
- For Google Pay, you can now use `AllowedAuthMethods` and `AllowedCardNetworks` to easily access to the possible values for `GooglePayConfiguration.Builder.setAllowedAuthMethods()` and `GooglePayConfiguration.Builder.setAllowedCardNetworks()`.

## Fixed
- Fixed the bug which would not show components in Compose lazy lists.
