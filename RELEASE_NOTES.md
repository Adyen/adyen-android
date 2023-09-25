[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Fixed
- Fixed Cash App payments being refused because of missing `customerId` during `/payments/details` call.
