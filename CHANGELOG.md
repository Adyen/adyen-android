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
## [Unreleased]
### Changed
- Update 3DS2 SDK to version `2.1.0-rc08`
- Added `FLAG_SECURE` to window when `CardView` is present.
- Card Data validation now accepts up to 30 years in the future.
### Fixed
- Fix and use `LocaleUtil.fromLanguageTag()` to parse user locale for localization. 
- Fix typo in CUP regex validation.

## [3.6.4] - 2020-02-21
### Changed
- Update 3DS2 SDK to version `2.1.0-rc07`
### Fixed
- Warning with string with placeholders without specified order.

## [3.6.3] - 2020-02-11
### Added
- Now Drop-in and Components will forcefully try to get localized strings from the ShopperLocale set in the configuration object.
### Changed
- `CardComponent` initialization will now try to check the supported brands from the `PaymentMethod` object if no custom array is provided.
### Fixed
- Fix issue with 19 digit card number
- Added missing translated strings.
- Fix synchronization issue that could trigger an OOB exception in `CardType` detection.
- Added proper return type for some Configuration Builder methods.

## [3.6.2] - 2020-01-20
### Added
- New translation string files for supported locales.
- ComponentView classes now have `highlightValidationErrors()` method.
- Added `Cancelled3DS2Exception` related to [Identify 3DS cancellation](https://github.com/Adyen/adyen-android/issues/159) request.
- Support American Express card number format.
### Changed
- Always enable pay button and highlight validation errors on Drop-in.

## [3.6.1] - 2019-12-20
### Fixed
- Catch SDKRuntimeException and InvalidInputException on 3DS2 Component.
- Avoid concurrency issue on `mFilteredSupportedCards` in `CardComponent`.

## [3.6.0] - 2019-12-17
### Added
- Added AfterPay payment method
- Drop-in now also returns the `Amount` that started the payment flow if it was passed on the `DropInConfiguration`.
- Added `CallResult.ResultType.ERROR_WITH_MESSAGE` to specify when a custom error message should be shown in DropIn. 
### Changed
- Errors now show in a Dialog instead of a Toast.
- Updated `GooglePayPaymentMethod` Model object to reflect new API parameters with added card network parameter.

## [3.5.1] - 2019-11-04
### Added
- `BinValue` and `CardType` included to CardComponentState 
- New `SimplifiedDropInService` is an extension of the `DropInService` that handles the raw `payments/` response without having to deal with `CallResult`
- Payment methods with all details being optional will now show in the Drop-in list. 
- You can now pass an `Activity` as the `Context` when starting the DropIn and and check for cancelling on the `onActivityResult` with `DropIn.DROP_IN_REQUEST_CODE`
### Changed
- CardNumber input type changed to `InputType.TYPE_CLASS_NUMBER`
### Deprecated
- `recurringDetailReference` deprecated, use storedPaymentMethodId instead - API changes.

## [3.5.0] - 2019-10-21
### Added
- WeChatPay SDK payment method.
### Changed
- Renamed `RecurringDetail` to `StoredPaymentMethod` to match the object array names on the [documentation](https://docs.adyen.com/api-explorer/#/PaymentSetupAndVerificationService/v50/paymentMethods__section_resParams).
- Added the fields `holderName` and `shopperEmail` that were missing.
- Precise CVC check in CardComponent for Amex card type.
### Deprecated
- Created new deprecated `RecurringDetail` to maintain backwards compatibility for now.
 
## [3.4.1] - 2019-10-07
### Added
- MolpayComponents can how handle multiple MolPay PaymentMethods (molpay_ebanking_fpx_MY, molpay_ebanking_TH, molpay_ebanking_VN)
- You can check which payment methods a Component can handle by calling `getSupportedPaymentMethodTypes()`
- Added option to show payment Amount on the DropIn pay button.
### Changed
- Deprecated `getPaymentMethodType()` in favor of `getSupportedPaymentMethodTypes()`.
- Merge Loading Activity into DropIn Activity to have single Activity for DropIn component.
- Configuration interface removed in favor of base class plus minor code improvements.
### Fixed
- Add margin left to CardListAdapter's layout.
- Fixed typo on package `com.adyen.checkout.core.exeption` to `com.adyen.checkout.core.exception`

## [3.4.0] - 2019-09-23
### Added
- Created standard style pattern for customizing XML layouts of the Components.
- Created BCMC component.
- Add option in Setting screen to enable 3ds2.
### Changed
- In CardConfiguration BCMC card type gonna be exclude from supported card type list.

## [3.3.1] - 2019-09-13
### Fixed
- There was issue with ConstraintLayout's Flow related to [ClassNotFoundException androidx.constraintlayout.widget.helper.Flow](https://github.com/Adyen/adyen-android/issues/109) fixed
- Created method to save the state of an ActionComponent when the Activity gets destroyed to persist the `paymentData`.
- Fixed non Ecommerce stored payment methods being shown as a regular payment method.

## [3.3.0] - 2019-09-11
### Added
- Created SepaComponent
- Created Card Component view specifically for DropIn to have more space to show supported card types in screen.
- DropIn will try to parse and use card brands from payment method response if there were no SupportedCard type in CardConfiguration
### Changed
- Component `observe()` will now notify the observer every time the user changes the input even though content and validation might not have changed.
- Refactored validation structure for fields
- CardType class moved from package `com.adyen.checkout.card.model` to package `com.adyen.checkout.card.data`
- CardType's regex updated
### Fixed
- Issue with image sometimes not loading
- Catching unexpected exception on Card encryption. 

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