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
- Fixed various memory leaks.
- Drop-in no longer overrides the log level in case of debug builds.
- Address Lookup not displaying validation error on Card Component when no address has been selected.
- Actions no longer crash when your app uses obfuscation.
- Drop-in no longer throws an error while handling a 3DS2 challenge on API 66 and below.

## Changed
- Flags are replaced by ISO codes in the phone number inputs (affected payment methods: MB Way, Pay Easy, Convenience Stores Japan, Online Banking Japan and Seven-Eleven).
- Strings containing "country" are changed to "country/region".
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Adyen 3DS2](https://github.com/Adyen/adyen-3ds2-android/releases/tag/2.2.18)                          | **2.2.18**                    |
