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
- For cards, in `AddressConfiguration.PostalCode` mode, you can now specify if the postal code field required.
- For BCMC, you can now specify if the card holder name field is required.
- After the card brand is detected and the shopper enters the full card number in the card number input field, focus automatically moves to the next input field.

## Changed
- Upgraded the 3D Secure 2 SDK version to v2.2.10.
- For a card number to be valid, its minimum required length is now 12 digits. Previously, the minimum was 8 digits.
- For cards, if you currently set the postal code input field in the AddressConfiguration as `.setAddressConfiguration(AddressConfiguration.PostalCode)`, you must update it to `.setAddressConfiguration(AddressConfiguration.PostalCode())`.

## Fixed
- Configuration changes no longer dismiss Drop-in. Previously, some configuration changes dismissed Drop-in.
- Drop-in can now be initialized with only stored payment methods. Previously, no payment methods were shown if only stored payment methods were available.