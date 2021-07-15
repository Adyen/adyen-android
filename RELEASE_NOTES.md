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

## Fixed
- Logos not loading when multiple icons in the same screen have the same logo.
