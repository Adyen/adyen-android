[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Improved` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## New)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- For Google Pay with the advanced flow, `onSubmit` now returns `threeDS2SdkVersion` inside the `paymentMethod` object. Forward this field to `/payments` to ensure the native 3DS2 flow is triggered properly.

## Fixed
- When using components or when using drop-in and excluding a module, R8 will no longer fail with `[CIRCULAR REFERENCE: com.android.tools.r8.utils.b: Missing class...`
