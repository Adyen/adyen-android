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
- Gift Card flow in Drop-in.
- New required Gift Card related methods in `DropInService`:
  - `checkBalance` to make the `/paymentMethods/balance` API call and `sendBalanceResult` to return the result.
  - `createOrder` to make the `/orders` API call and `sendOrderResult` to return the result.
  - `cancelOrder` to make the `/orders/cancel` API call. Result can be returned with `sendResult`.
- `DropInServiceResult.Update` required for the Gift Card flow. Updates drop-in with a new list of payment methods and optionally an order.
- Gift Card Component.
- RTL support.
- Arabic string resource translations.
- Pass a custom `Bundle` to `DropInService` using `DropInConfiguration.Builder.setAdditionalDataForDropInService`. Retrieve this bundle by calling `DropInService.getAdditionalData`.
- The default Google Pay environment will automatically follow the Adyen environment even when calling `GooglePayConfiguration.Builder.setEnvironment` separately. You can still call `GooglePayConfiguration.Builder.setGooglePayEnvironment` to override this default behaviour.
- Ability to remove stored payment methods. Override `DropInService.removeStoredPaymentMethod` and use `sendRecurringResult` to return the result.

## Changed
- Updated Adyen 3DS2 SDK version to `2.2.6`.

## Fixed
- Redirects in Android 11. [Privacy changes for package visibility in Android 11](https://developer.android.com/about/versions/11/privacy/package-visibility) requires a fix for [handling redirects using Custom Tabs](https://developers.google.com/web/updates/2020/07/custom-tabs-android-11).
- Crash when the provided shopper `Locale` doesn't have an `isO3Country`.
- Crash in the Component screen when resuming drop-in after the activity has been destroyed in the background.
- CVC and expiry date fields in Card Component not being validated when empty.
- Various missing translations.

## Removed
- Drop-in being dismissed when tapping anywhere outside of the bottom sheet.