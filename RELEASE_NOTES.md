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
- `QRCodeComponent` will now redirect qr code actions that should work as a redirect on Android (e.g. `bcmc_mobile`).
- `TotalPriceStatus` to the `GooglePayConfiguration`.

## Changed
- `WeChatPayActionComponent` is now an `IntentHandlingComponent`, the method `handleResultIntent` is renamed to `handleIntent`.
