[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## WARNING
Make sure you explicitly set your LIVE environment on release builds when initializing Drop-in; `DropInConfiguration` is now initialized with `Environment.TEST` by default instead of `Environment.EUROPE`.

## Fixed
- Use `ApplicationInfo.FLAG_DEBUGGABLE` instead of `BuildConfig.DEBUG` to figure out whether it's a debug or release build.
- `NoClassDefFoundError` crash with Card component, if 3DS2 is not included in the project.
- Handle Google Pay cancellation and failure callbacks on initialisation.

## Changed
- Update 3DS2 SDK to version `2.2.5`.

## Added
- Card component shows a more specific error message when the user enters a card belonging to an unsupported brand.
- `DropInConfiguration.Builder.setSkipListWhenSinglePaymentMethod` to allow skipping payment methods screen when single payment method exists. This only applies to payment methods that require a component (user input). Redirect payment methods, SDK payment methods, and so on will not be skipped.
- Provide `fundingSource` when present in payment methods response.
- Support for installments in card component.
- Use `SavedStateHandle` in all components and providers.
- `Environment.LIVE`, identical to the `Environment.EUROPE`.
