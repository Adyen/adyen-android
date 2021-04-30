[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (### Added)
[//]: # ( - New payment method)
[//]: # ( ### Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( ### Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Changed
- Updated Material Components dependency to 1.3.0.
- Provided default `GooglePayConfiguration` and `CardConfiguration` in Drop-in. It's not required to manually set these configurations in `DropInConfiguration.Builder` anymore.
- The default Google Pay environment will automatically follow the Adyen environment. It will be initialized as `ENVIRONMENT_TEST` when using Adyen's `TEST` environment, otherwise it will be set to `ENVIRONMENT_PRODUCTION`.
- The `merchantAccount` parameter in `GooglePayConfiguration.Builder` is now optional. You can remove it from the builder constructor, or use `GooglePayConfiguration.Builder.setMerchantAccount` if you need to pass it manually.
