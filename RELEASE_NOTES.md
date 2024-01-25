[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Breaking changes` `New` `Added` `Improved` `Changed` `Deprecated` `Removed` `Fixed`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # (## Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- Add support for Multibanco voucher.
- Permission request is now being delegated to the `ActionComponentCallback`, `SessionComponentCallback` or `ComponentCallback` to handle it and return result through callback.
- For voucher actions which have no `url` or `downloadUrl`, "Save as image" option will be offered to save the Voucher in `Downloads` folder.
  - Vouchers will save an image to user's phone with the following name format "Payment method type" + "Formatted data and time" (e.g. multibanco-2024-01-09T16_41_10).

## Deprecated
- The `PermissionException` is deprecated. Handle permissions through `ActionComponentCallback`, `SessionComponentCallback` or `ComponentCallback` callbacks.
- The styles for vouchers have been changed:
  - The `AdyenCheckout.Voucher.Description.Bacs` style will not work anymore. Use `AdyenCheckout.Voucher.Simple.Description` instead.
  - The `AdyenCheckout.Voucher.Description.Boleto` style will not work anymore. Use `AdyenCheckout.Voucher.Full.Description` instead.
  - The `AdyenCheckout.Voucher.ExpirationDateLabel` style will not work anymore. Use `AdyenCheckout.Voucher.InformationFieldLabel` instead.
  - The `AdyenCheckout.Voucher.ExpirationDate` style will not work anymore. Use `AdyenCheckout.Voucher.InformationFieldValue` instead.
  - The `AdyenCheckout.Voucher.ButtonCopyCode` and `AdyenCheckout.Voucher.ButtonDownloadPdf` styles will not work anymore. Use `AdyenCheckout.Voucher.ActionButton` instead.

## Changed
- In drop-in all actions will start in expanded mode
