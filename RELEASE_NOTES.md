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
- `@RestrictTo` annotations no longer cause false errors with Android Studio and Lint.
- Using the layout inspector or having view attribute inspection enabled in the developer options no longer causes a crash when viewing a payment method.
- Implementing the `:action` module no longer gives a duplicate class error caused by a duplicate namespace.
- For Drop-in, dismissing the gift card payment method no longer prevents further interaction.

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [AndroidX Compose BoM](https://developer.android.com/jetpack/compose/bom/bom-mapping)                  | **2023.09.01**                |
