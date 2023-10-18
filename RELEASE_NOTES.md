[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

For guidance on integrating with this version, have a look at the [integration guide](https://docs.adyen.com/online-payments/build-your-integration/?platform=Android).

If your integration uses Android v4.13.3 and earlier, and you're upgrading it to use v5.0.0, you can follow the [migration guide](https://docs.adyen.com/online-payments/build-your-integration/migrate-to-android-5-0-0/).

These are the changes between the beta and stable release. For the full release notes that include all the changes from v4.13.3, see the [release notes in our Docs](https://docs.adyen.com/online-payments/release-notes/?version=5.0.0&integration_type=android).

## Breaking changes
- `Amount.EMPTY` is removed. Make sure you pass amounts with a valid value and currency.

## Fixed
- `@RestrictTo` annotations no longer cause false errors with Android Studio Hedgehog (Beta).
- The Drop-in bottom sheet will no longer shift position on the screen when launching some flows like redirect and 3D Secure 2.

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Google Pay](https://developers.google.com/pay/api/android/support/release-notes#sept-14)              | **19.2.1**                    |
  | [AndroidX Compose BoM](https://developer.android.com/jetpack/compose/bom/bom-mapping)                  | **2023.09.00**                |
