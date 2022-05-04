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
- Full billing address form in the card component. Use `CardConfiguration.Builder.setAddressConfiguration` to enable and configure.

## Deprecated
- `Environment.LIVE`. Replace with one of our explicit live environments, same one as your back end. You can find that value in your Customer Area.
- `CardConfiguration.Builder.setAddressVisibility`. Replace with `CardConfiguration.Builder.setAddressConfiguration`.

## Fixed
- Soft keyboard navigation (next) not working in some cases in card component.
- Localization issues with standalone components and custom locales.
- Crash in GooglePay availability check.
- Proguard rules. If you had to manually set any Proguard rules related to Drop-in or Components you can remove them.
- Minor visual issues with removing stored payment methods.