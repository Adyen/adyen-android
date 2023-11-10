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
- You can now override payment method names in drop in by using `DropInConfiguration.Builder.overridePaymentMethodName(type, name)`
