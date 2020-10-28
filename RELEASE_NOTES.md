
[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (### Added)
[//]: # ( - New payment method)
[//]: # ( ### Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( ### Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

### Added:
- Updated to AndroidX libraries instead of the old Support Libs.
- Support for Kotlin in all modules except CSE.
- Target Java 8
- Spotbugs in place of Findbugs

### Changed:
- Min API to 21 to reduce scope of security concerns.
- Target API to 30
- Updated to Gradle 6.6.1
- Updated 3DS2 SDK to version 2.2.0 with default protocol as still 2.1.0
- Updated a bunch of libraries, including Google Pay and WeChatPay dependencies.
- Replaced deprecated LocalBroadcast with Kotlin Flow as an experiment for communicating between DropService and DropInActivity.
- Renamed example.local.gradle with default.local.gradle for CI builds and example app without values.
