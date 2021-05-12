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
- Support for Pix payment method.
- `QRCodeComponent` to handle action type `qrCode` from payment method `pix`.
    - Support for other payment methods with a qrCode action will be added in the future.
- Support for returning `returnUrlQueryString` from redirect URL for some redirect payment methods like Swish.

## Changed
- New releases are now published to [Maven Central](https://repo1.maven.org/maven2/com/adyen/checkout/)
- A `Configuration` object is now required when initializing any component. Action Components did not require it previously.
- Provided default `GooglePayConfiguration` and `CardConfiguration` in Drop-in. It's not required to manually set these configurations in `DropInConfiguration.Builder` anymore.
- The default Google Pay environment will automatically follow the Adyen environment. It will be initialized as `ENVIRONMENT_TEST` when using Adyen's `TEST` environment, otherwise it will be set to `ENVIRONMENT_PRODUCTION`.
- The `merchantAccount` parameter in `GooglePayConfiguration.Builder` is now optional. You can remove it from the builder constructor, or use `GooglePayConfiguration.Builder.setMerchantAccount` if you need to pass it manually.
- Updated 3DS2 SDK to version 2.2.2
- Updated Material Components dependency to 1.3.0.

## Fixed
- Passing `threeDSRequestorAppURL` to the SDK in the 3DS2 Component only when protocol version is 2.2.0 or higher since this is not expected in 2.1.0
- Style in TextInputLayout where in some scenarios text color would be too light and hard to see.

## Removed
- `WeChatPayComponent` since it didn't have any function. Instead you can simply check if the App is available by calling: `WeChatPayUtils.isAvailable(applicationContext)`