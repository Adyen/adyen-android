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
- Creating configurations just became easier. Using a DSL you can now create configurations in a more declarative and concise way:
```Kotlin
CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
    shopperLocale = shopperLocale,
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
- Address Lookup functionality for Card Component. 
  - You can enable this feature by setting your address configuration to lookup while building your card configuration as follows:
    ```kotlin
    CheckoutConfiguration(environment = environment, clientKey = clientKey) {
        card {
            setAddressConfiguration(AddressConfiguration.Lookup())
        }
    }
    ```
  - If you're integrating with Drop-in:
    - Implement the mandatory `onAddressLookupQueryChanged(query: String)` callback and optional `onAddressLookupCompletion(lookupAddress: LookupAddress)` callback.
    - Pass the result of these actions by using `AddressLookupDropInServiceResult` class.
  - If you're integrating with standalone `CardComponent`:
    - Set `AddressLookupCallback` via `CardComponent.setAddressLookupCallback(AddressLookupCallback)` function to receive the related events.
    - Pass the result of these actions by calling `CardComponent.setAddressLookupResult(addressLookupResult: AddressLookupResult)`.
    - Delegate back pressed event to `CardComponent` by calling `CardComponent.handleBackPress()` which returns true if the back press is handled by Adyen SDK and false otherwise.
- Add support for Multibanco voucher.
- Permission request is now being delegated to the `ActionComponentCallback`, `SessionComponentCallback` or `ComponentCallback` to handle it and return result through callback.
  - If not handled, a toast will be shown stating that permission is not granted.
- For voucher actions which have no `url` or `downloadUrl`, "Save as image" option will be offered to save the Voucher in `Downloads` folder.
  - Vouchers will save an image to user's phone with the following name format "Payment method type" + "Formatted data and time" (e.g. multibanco-2024-01-09T16_41_10).
- Set your own `AdyenLogger` instance with `AdyenLogger.setLogger`. This gives the ability to intercept logs and handle them in your own way.
- Payment methods:
  - Pay Easy. Payment method type: **econtext_atm**.
  - Convenience Stores Japan. Payment method type: **econtext_stores**
  - Online Banking Japan. Payment method type: **econtext_online**.
  - Seven-Eleven: Payment method type: **econtext_seven_eleven**

## Fixed
- When building `minifyEnabled` and without the `kotlin-parcelize` plugin in your project the build should no longer crash.
- When handling actions you should no longer get `IllegalArgumentException: Unsupported delegate type`.

## Deprecated
- When creating a configuration, the `Builder` constructors with a `Context` are now deprecated. You can omit the `context` parameter.
- The `PermissionException` is deprecated. Handle permissions through `ActionComponentCallback`, `SessionComponentCallback` or `ComponentCallback` callbacks.
- The styles for vouchers have been changed:
  - The `AdyenCheckout.Voucher.Description.Bacs` style will not work anymore. Use `AdyenCheckout.Voucher.Simple.Description` instead.
  - The `AdyenCheckout.Voucher.Description.Boleto` style will not work anymore. Use `AdyenCheckout.Voucher.Full.Description` instead.
  - The `AdyenCheckout.Voucher.ExpirationDateLabel` style will not work anymore. Use `AdyenCheckout.Voucher.InformationFieldLabel` instead.
  - The `AdyenCheckout.Voucher.ExpirationDate` style will not work anymore. Use `AdyenCheckout.Voucher.InformationFieldValue` instead.
  - The `AdyenCheckout.Voucher.ButtonCopyCode` style will not work anymore. Use `AdyenCheckout.Voucher.Button.CopyCode` instead.
  - The `AdyenCheckout.Voucher.ButtonDownloadPdf` styles will not work anymore. Use `AdyenCheckout.Voucher.Button.DownloadPdf` instead.
- `Logger.LogLevel` is deprecated. Use `AdyenLogLevel` instead.
- `AdyenLogger.setLogLevel(logLevel: Int)` is deprecated. Use `AdyenLogger.setLogLevel(level: AdyenLogLevel)` instead.

## Changed
- When creating a configuration, the shopper locale parameter is now optional. If not set, the shopper locale will match the value passed to the API with the sessions flow, or the primary user locale on the device otherwise.
- With the sessions flow, when starting drop-in (with `DropIn.startPayment`) or creating a component (with `YourComponent.PROVIDER.get`), the configuration parameter is now optional.
- When `CheckoutSessionProvider.createSession` to create a `CheckoutSession`, you can pass the `environment` and `clientKey` instead of the whole configuration.
- In drop-in all actions will start in expanded mode
- When using the Google Pay component, it is no longer necessary to manually import the `3ds2` module to handle transactions that require a native 3DS2 challenge. 
- Removing stored payment methods are being handled internally if you are using Sessions integration. You do not need to override `onRemoveStoredPaymentMethod` function anymore.
- If you are using `DropInServiceResult.Error` without specifying an error message, the default has changed from `Error sending payment. Please try again.` to `An unknown error occurred`.
