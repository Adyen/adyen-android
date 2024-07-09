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
- For Google Pay on Advanced flow, `onSubmit` now returns`threeDS2SdkVersion` in the `paymentMethod` object that you must pass in your [`/payments`](https://docs.adyen.com/api-explorer/Checkout/71/post/payments) request to correctly trigger the 3D Secure 2 flow.

## Fixed
- On Android API versions 21 to 25, the `NoSuchMethodError` no longer occurs during the 3D Secure 2 challenge flow.
- When [using R8 to shrink your code](https://developer.android.com/build/shrink-code), `CIRCULAR REFERENCE: com.android.tools.r8.utils.b: Missing class...` errors no longer occur.

## Changed
- Dependency versions:
  | Name                                                                                                         | Version                       |
  |--------------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Adyen 3DS2](https://github.com/Adyen/adyen-3ds2-android/releases/tag/2.2.19)                                | **2.2.19**                    |
  | [Kotlin](https://github.com/JetBrains/kotlin/releases/tag/v1.9.24)                                           | **1.9.24**                    |
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                          | **8.4.1**                     |
  | [AndroidX Compose Compiler](https://developer.android.com/jetpack/androidx/releases/compose-compiler#1.5.14) | **1.5.14**                    |
  | [Kotlin coroutines](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.8.1)                         | **1.8.1**                     |
  | [AndroidX Fragment](https://developer.android.com/jetpack/androidx/releases/fragment#1.7.1)                  | **1.7.1**                     |
