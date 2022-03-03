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
- Gift Card Component.
- RTL support.
- `DropInConfiguration.Builder.setAdditionalDataForDropInService(Bundle)` function to add additional data to be used later in the drop-in. In order to fetch the additional data `DropInService.getAdditionalData()` needs to be called.
- While building a `GooglePayConfiguration` whenever `GooglePayConfiguration.setEnvironment(Environment)` gets called, `GooglePayConfiguration.mBuilderGooglePayEnvironment` will be updated with the corresponding `WalletConstant` (i.e. `Environment.TEST` corresponds to `WalletConstant.ENVIRONMENT_TEST`. And `Environment.LIVE`, `Environment.EUROPE`, `Environment.UNITED_STATES`, `Environment.AUSTRALIA` correspond to `WalletConstant.ENVIRONMENT_LIVE`.). However if you call `GooglePayConfiguration.setGooglePayEnvironment(int)` manually while building the configuration, `GooglePayConfiguration.mBuilderGooglePayEnvironment` won't be updated automatically when `GooglePayConfiguration.setEnvironment(Environment)` gets called.
- Ability to remove stored payment methods.
- Ability to reload the GooglePayComponent with a different configuration in the same drop-in session (e.g. in the gift card flow).

## Changed
- Adyen 3DS2 SDK version to 2.2.6.

## Fixed
- Redirects for Android 11.
- Crash when locale doesn't have ISO3 country.
- Crash happening in Card Component when coming back from background after activity has been destroyed by OS.
- CVC and expiry date in Card Component not being validated when empty.

## Removed
- Canceling drop-in when shopper clicks outside the bottom sheet.