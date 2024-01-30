[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Improved` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- We added a [UI customization guide](docs/UI_CUSTOMIZATION.md), which explains how to customize the styles and string resources.

## Improved
- The integration now uses JSON Web Encryption (JWE) with RSA OAEP 256 and AES GCM 256 for encryption. You do not need to make any changes to your integration.

## Fixed
- For Drop-in, error dialogs no longer display user unfriendly messages when using the Sessions flow.
- Overriding some of the XML styles without specifying a parent style no longer causes a build error.
- The Await and QR Code action components no longer get stuck in a loading state after the payment is completed.

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Kotlin](https://kotlinlang.org/docs/releases.html#release-details)                                    | **1.9.21**                    |
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **8.2.0**                     |
  | [AndroidX Compose compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler)  | **1.5.7**                     |
  | [AndroidX Compose Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.8.1)    | **1.8.1**                     |
  | [AndroidX Browser](https://developer.android.com/jetpack/androidx/releases/browser#1.7.0)              | **1.7.0**                     |
