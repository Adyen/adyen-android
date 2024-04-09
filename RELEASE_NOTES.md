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
- Localization issues in address lookup functionality.
- Overriding some of the XML styles without specifying a parent style no longer causes a build error.
- Not defining `?android:attr/textColor` in your own theme will no longer crash.
- The build output should no longer contain warnings about multiple substitutions specified in non-positional format in string resources.
- In some edge cases `onAdditionalDetails` was triggered multiple times, this no longer happens.

## Removed
- The functions to get specific configurations from `CheckoutConfiguration` (such as `CheckoutConfiguration.getDropInConfiguration()` or `CheckoutConfiguration.getCardConfiguration()`) are no longer accessible. Pass the `CheckoutConfiguration` object as it is when starting Drop-in or Components.

## Changed
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **8.3.1**                     |
