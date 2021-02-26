
[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (### Added)
[//]: # ( - New payment method)
[//]: # ( ### Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( ### Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## WARNINGS
- This version has changes in dependency names and package names. Make sure you are using the correct dependency (for components) and re-import classes that were in the `base` package.
- This version now targets Checkout API version [v67](https://docs.adyen.com/online-payments/release-notes#checkout-api-v67).

## Added
- `CardComponent` and `BcmcComponent` will now fetch the public key for card encryption using the `clientKey`
- `CardComponent` will now use Bin Lookup endpoint to more reliably verify the card brand.
- `CardComponent` will now return the Bin and last 4 digits of the card number in the `CardComponentState`
- `DropInService` now has `onPaymentsCallRequested` and `onDetailsCallRequested` that can be used to intercept a request.
 - This allows access to the whole `PaymentComponentState` and to handle asynchronous handling of the API call if necessary.
- `DropIn` result can now be fully handled in `onActivityResult`.
- New code [Documentation page](https://adyen.github.io/adyen-android/)

## Changed
Features:
- MBWay Component removed email field and updated UI for phone number input.

Code:
- Refactored module structure and artifact IDs. Now each payment method has only 1 module with a simplified name.
- Renamed package `com.adyen.checkout.base` to `com.adyen.checkout.components`
- Renamed package `com.adyen.checkout.base.component` to `com.adyen.checkout.components.base`
- Refactored `CallResult` to `DropInServiceResult` as sealed classes.
- CSE module now has all the encryption code and has a simplified API.
 - `CardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)`
- `DropInService` is now a regular bound `Service` instead of a `JobIntentService`
- `ResultHandlerIntent` is now an optional parameter in `startPayment` instead of `DropInConfiguration`
- `clientKey` is now a required parameter for all `Configuration` objects
- `publicKey` cannot be passed directly to the `Configuration` of Card and BCMC anymore, use `clientKey` instead.
- A bunch of internal refactorings and moving classes to Kotlin :rocket:

## Removed
- `SimplifiedDropInService` to not encourage bad practice of passing whole API response to the App.
- AfterPay Component

## Fixed
- Issue where card logo and validation logos would overlap.




