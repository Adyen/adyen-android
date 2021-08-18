[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Added
- `showPreselectedStoredPaymentMethod` flag to `DropInConfiguration` to allow choosing to skip to the payment method selection screen.
- Any `Configuration` will check if the `clientKey` matches the `environment` it will be used on.
- Postal Code input field to Card Component.
- KCP Authentication input field support to Card Component.
- Social Security Number (CPF/CNPJ) input field support to Card Component.
- Android 12 support.

## Changed
- Adyen 3DS2 SDK version to 2.2.4.

## Removed
- Switching to next input field automatically after card number has been filled in.

## Fixed
- Logos not loading when multiple icons in the same screen have the same logo.
- Crashes when returning from a redirect, after drop-in activity is killed in the background.
- Card number validation changed for cards where the number doesn't pass the Luhn check but is still valid.