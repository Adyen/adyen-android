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
- BACS Direct Debit Component.

## Fixed
- Google Pay Environment being set incorrectly to `WalletConstants.ENVIRONMENT_PRODUCTION`.

## Deprecated
- `DropInServiceResult.Action(actionJSON: String)` is deprecated in favor of `DropInServiceResult.Action(action: com.adyen.checkout.components.model.payments.response.Action)`. Use `com.adyen.checkout.components.model.payments.response.Action.SERIALIZER` to serialize your JSON response string.