[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Fixed` `Improved` `Changed` `Deprecated` `Removed`)
[//]: # (Example:)
[//]: # (## New)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- Payment method:
  - Pay by Bank US. Payment method type: **paybybank_AIS_DD**.

## Fixed
- For cards, the address lookup functionality no longer crashes if the shopper presses back when the postal code field is in focus.
- For Drop-in, fixed an issue where the error dialog showed loading state in some edge cases.

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Android Gradle Plugin](https://developer.android.com/build/releases/gradle-plugin#android-gradle-plugin-8.7.1)                    | **8.7.1**                     |
  | [AndroidX Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.9.3)            | **1.9.3**                     | 
  | [AndroidX Annotation](https://developer.android.com/jetpack/androidx/releases/annotation#1.9.1)        | **1.9.1**                     |
  | [AndroidX Autofill](https://developer.android.com/jetpack/androidx/releases/autofill#1.3.0-beta01)     | **1.3.0-beta01**              |
  | [AndroidX Compose Activity](https://developer.android.com/jetpack/androidx/releases/activity#1.9.3)    | **1.9.3**                     |
  | [AndroidX Compose BOM](https://developer.android.com/develop/ui/compose/bom/bom-mapping)               | **2024.10.00**                |
  | [AndroidX Fragment](https://developer.android.com/jetpack/androidx/releases/fragment#1.8.5)            | **1.8.5**                     |
  | [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.6)          | **2.8.6**                     | 
  | [AndroidX Lifecycle ViewModel Compose](https://developer.android.com/jetpack/androidx/releases/lifecycle#2.8.6)  | **2.8.6**                     |
