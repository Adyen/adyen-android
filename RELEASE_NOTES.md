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
- Support for the new Asia Pacific South East (APSE) and India live environments. Use these environments with the corresponding APSE or India location-based live endpoints.

## Fixed
- For BACS Direct Debit, the payment agreement text in the payment form now includes the amount. Previously, it always showed the default **above amount** instead of the amount.
