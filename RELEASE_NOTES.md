[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
* Payment method: Cash App Pay.

## Fixed
- For Google Pay, serializing the shipping address parameters no longer causes an error.
- For WeChat Pay on Android 11, an API restriction no longer causes an error.

## Removed
* For 3D Secure 2, the `threeDSRequestorAppURL` will not have a default value anymore. You can set it manually using:

```kotlin
Adyen3DS2Configuration.Builder(locale, environment, clientKey)
    .setThreeDSRequestorURL("https://{your app.com}/adyen3ds2")
    .build()
```