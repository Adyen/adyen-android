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
- Dual-branded card flow: shoppers can now select their card's brand when two brands are detected.
- Ability to store BCMC cards.
- Ability to stop observing components.
- 3DS2 SDK version in `CardComponent`'s output.
- Read list of supported card networks for Google Pay from the payment methods API response.

## Fixed
- Crash caused by having stored payment methods none of which is Ecommerce.
- Google Pay Component will not include `TotalPrice` in its output, if `TotalPriceStatus` is set to `NOT_CURRENTLY_KNOWN`.
- Issue in Drop-in when multiple payment methods have the same type.
- Missing default BCMC configuration in Drop-in.
