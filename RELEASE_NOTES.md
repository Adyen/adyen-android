[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Fixed` `Improved` `Changed` `Deprecated` `Removed`)
[//]: # (Example:)
[//]: # (## New)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Fixed
- Address data not being correctly saved to PaymentComponentData in Lookup mode.
- Manually edited fields in address form in Lookup mode no longer lose their state on starting Lookup mode. 
