[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Improved` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Added
- We added [a guide](https://github.com/Adyen/adyen-android/blob/develop/docs/UI_CUSTOMIZATION.md) on UI customization of the Android SDK.

## Improved
- For encryption now JSON Web Encryption (JWE) with RSA OAEP 256 and AES GCM 256 is used. You don't need to make any changes to your integration.

## Fixed
- For drop-in with sessions, error dialogs will no longer display user unfriendly messages.
- Overriding some of the XML styles without specifying a parent style no longer causes a build error.
- The Await and QR Code action components will no longer be stuck in a loading state for a long time after the payment is finalized. 

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Kotlin](https://kotlinlang.org/docs/releases.html#release-details)                                    | **1.9.21**                    |
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **8.2.0**                     |
  | [AndroidX Compose compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler)  | **1.5.7**                     |
  | [AndroidX Compose Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.8.1)    | **1.8.1**                     |
  | [AndroidX Browser](https://developer.android.com/jetpack/androidx/releases/browser#1.7.0)              | **1.7.0**                     |
