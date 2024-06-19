[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Improved` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## New)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- UPI now supports `upi_intent` payment apps.
- The new iDEAL payment flow where the shopper is redirected to the iDEAL payment page to select their bank and authorize the payment.

## Changed
- Drop-in navigation improvements:
  - Top navigation has been added
  - Dragging gesture has been disabled which caused Drop-in to dismiss
  - Going back from actions dismisses Drop-in

## Deprecated
- For `IdealComponent`:
  - `isConfirmationRequired()` can be removed.
  - `submit()` can be removed.
- When configuring iDEAL:
  - `setViewType`, `setHideIssuerLogos` and `setSubmitButtonVisible` can be removed.
