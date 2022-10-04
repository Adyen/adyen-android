[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Added
- `Online banking Poland` Component

## Changed
- Update 3DS2 SDK version to `2.2.8`

## Fixed
- Gracefully terminate drop in without crashing when coming back from a redirect after drop-in was closed
- Updating 3DS2 SDK version fixed a crash on Android 13
