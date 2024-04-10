[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Improved` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Fixed
- For Drop-in and Components, when `?android:attr/textColor` is not defined in your own theme, the Card Component no longer crashes.
- The `onAdditionalDetails` event is now triggered only once. Previously, the event was triggered multiple times in some edge cases.
- The build output no longer contains warnings about multiple substitutions specified in non-positional format in string resources.
- For the Card Component, we fixed localization issues that occurred when using the Address Lookup functionality.
- Overriding some of the XML styles without specifying a parent style no longer causes a build error.

## Removed
- You can no longer use functions like `CheckoutConfiguration.getCardConfiguration()` or `CheckoutConfiguration.getDropInConfiguration()` to get configurations from the  `CheckoutConfiguration` object. When starting Drop-in or Components, pass the full `CheckoutConfiguration` object.

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **8.3.1**                     |
