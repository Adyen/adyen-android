[//]: <> (A changelog is a file which contains a curated, chronologically ordered list of notable changes for each version of a project.)
[//]: <> (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: <> (Example:)
[//]: <> (## [0.0.6] - 2014-12-12)
[//]: <> (### Added)
[//]: <> ( - New payment method)
[//]: <> ( ### Changed)
[//]: <> ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: <> ( ### Deprecated)
[//]: <> ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)
[//]: <>
[//]: <>
[//]: <> (Add changes that not released yet into `Unreleased` section)
[//]: <> (Comment `Unreleased` section if there are no changes)
[//]: <> (## [Unreleased])
## [3.2.1] - 2019-08-29
### Added
- Created Settings screen on Example app to facilitate testing instead of hard coding values.
### Changed
- All configurations are now `Parcelable`
- Authentication3DS2Exception moved from package `com.adyen.checkout.adyen3ds2` to `com.adyen.checkout.adyen3ds2.exception`
- Update 3DS2 to version `2.1.0-rc04`
### Fixed
- Issue related to [DropIn singleton configuration](https://github.com/Adyen/adyen-android/issues/89) fixed
- Check 3DS2 Transaction to avoid NPE related to [wrong flow implementation](https://github.com/Adyen/adyen-android/issues/101)
- Do not show payment methods that are not Ecommerce on shopper interaction.
- Issue related to [Adyen 3DS2](https://github.com/Adyen/adyen-android/issues/102) fixed
### Deprecated
- All configuration's public constructor were deprecated, instead you can use Builder class of each configuration.
- `DropInConfiguration.Builder(context, serviceClass)` deprecated because now you need to pass `resultHandlerIntent` to builder's constructor.
```kotlin
DropInConfiguration.Builder(context, resultHandlerIntent, serviceClass)
```
- `DropIn.INSTANCE` singleton deprecated, instead you can use the static method directly. 
```kotlin
// Be aware that `dropInConfiguration` need to be initiate with new constructor mentioned above
DropIn.startPayment(context, paymentMethodsApiResponse, dropInConfiguration)
```