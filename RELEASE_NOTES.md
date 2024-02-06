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
- Address Lookup functionality for Card Component. You can enable this feature by setting your address configuration to lookup while building your card configuration as follows
```kotlin
CheckoutConfiguration(
    shopperLocale = shopperLocale,
    environment = environment,
    clientKey = clientKey,
    amount = amount,
    analyticsConfiguration = createAnalyticsConfiguration(),
) {
    card {
        setAddressConfiguration(AddressConfiguration.Lookup())
    }
}
```
- Add support for Multibanco voucher.
- Permission request is now being delegated to the `ActionComponentCallback`, `SessionComponentCallback` or `ComponentCallback` to handle it and return result through callback.
  - If not handled, a toast will be shown stating that permission is not granted.
- For voucher actions which have no `url` or `downloadUrl`, "Save as image" option will be offered to save the Voucher in `Downloads` folder.
  - Vouchers will save an image to user's phone with the following name format "Payment method type" + "Formatted data and time" (e.g. multibanco-2024-01-09T16_41_10).
- Creating configurations just became easier. Using a DSL you can now create configurations in a more declarative and concise way:
```Kotlin
CheckoutConfiguration(
    shopperLocale = shopperLocale,
    environment = environment,
    clientKey = clientKey,
    amount = amount,
    analyticsConfiguration = createAnalyticsConfiguration(),
) {
    dropIn {
        setEnableRemovingStoredPaymentMethods(true)
    }
    
    card {
        setHolderNameRequired(true)
        setShopperReference("...")
    }

    adyen3DS2 {
        setThreeDSRequestorAppURL("...")
    }
}
```

## Deprecated
- The `PermissionException` is deprecated. Handle permissions through `ActionComponentCallback`, `SessionComponentCallback` or `ComponentCallback` callbacks.
- The styles for vouchers have been changed:
  - The `AdyenCheckout.Voucher.Description.Bacs` style will not work anymore. Use `AdyenCheckout.Voucher.Simple.Description` instead.
  - The `AdyenCheckout.Voucher.Description.Boleto` style will not work anymore. Use `AdyenCheckout.Voucher.Full.Description` instead.
  - The `AdyenCheckout.Voucher.ExpirationDateLabel` style will not work anymore. Use `AdyenCheckout.Voucher.InformationFieldLabel` instead.
  - The `AdyenCheckout.Voucher.ExpirationDate` style will not work anymore. Use `AdyenCheckout.Voucher.InformationFieldValue` instead.
  - The `AdyenCheckout.Voucher.ButtonCopyCode` style will not work anymore. Use `AdyenCheckout.Voucher.Button.CopyCode` instead.
  - The `AdyenCheckout.Voucher.ButtonDownloadPdf` styles will not work anymore. Use `AdyenCheckout.Voucher.Button.DownloadPdf` instead.

## Changed
- In drop-in all actions will start in expanded mode
