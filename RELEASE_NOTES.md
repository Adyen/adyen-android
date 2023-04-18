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
* For BLIK, one-click is now supported.
* For 3D Secure 2, the `threeDSRequestorAppURL` can now be overridden from the configuration so that you can now use the [Android App Link](https://developer.android.com/studio/write/app-link-indexing) format (HTTP).

```kotlin
Adyen3DS2Configuration.Builder(locale, environment, clientKey)
    .setThreeDSRequestorURL("https://{your app.com}/adyen3ds2")
    .build()
```
⚠️Because of recent updates to the 3D Secure protocol, we strongly recommend that you provide the threeDSRequestorAppURL parameter as an Android App Link instead of custom link. This requires your app to handle the provided Android App Link. More details on how to handle Android App Link can be found on docs [page](https://docs.adyen.com/online-payments/classic-integrations/api-integration-ecommerce/3d-secure/native-3ds2/android-sdk-integration#handling-android-app-links).

## Fixed
* For BCMC, errors are now correctly highlighted.
