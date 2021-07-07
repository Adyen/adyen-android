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
- 3 new methods to `ActionComponentProvider`: `canHandleAction`, `requiresView` and `getSupportedActionTypes`.
- `Adyen3DS2Component`, `QRCodeComponent`, `RedirectComponent` and `WeChatPayActionComponent` now implement `IntentHandlingComponent` and will have a `handleIntent` method. This method replaces `handleRedirectResponse` in `RedirectComponent` and `handleResultIntent` in `WeChatPayActionComponent`.  
- `QRCodeComponent` will now redirect QR Code actions that should work as a redirect on Android (e.g. `bcmc_mobile`). Use the `handleIntent` method to handle the result of the redirect.
- `Adyen3DS2Component` now supports the new 3DS2 frictionless flow (requires API v67). In some cases the component will now make a redirect, use the `handleIntent` method to handle the result of the redirect.
- For certain card types, the CVC field will be marked as optional. The `CardComponent` performs a BIN lookup call which uses the card number to try and identify the corresponding card type and  whether the CVC is required or not.
- Support new `"googlepay"` txVariant.
- `TotalPriceStatus` to the `GooglePayConfiguration`.

## Fixed
- Update Google Pay logo. 
- Pay button not working when paying with a stored card with holder name required. 
- If `GooglePayConfiguration` is not provided manually to Drop-in, Google Pay will use the amount specified in `DropInConfiguration`.
