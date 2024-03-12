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
- A new way to create a configuration using DSL to be more declarative and concise:
```Kotlin
CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
    shopperLocale = shopperLocale,
    amount = amount,
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

- For the Card Component, you can use the new [Address Lookup functionality](docs/ADDRESS_LOOKUP.md). 
- For voucher actions: when the `url` or `downloadUrl` is not included, the shopper has the option to select **Save as image** and save the voucher to the device's `Downloads` folder.
- You can now set your own `AdyenLogger` instance with `AdyenLogger.setLogger`. This gives the ability to intercept logs and handle them in your own way.
- [Instructions](example-app/README.md) to use the testing app in the repository. You can follow `How to migrate` section [here](https://github.com/Adyen/adyen-android/pull/1505).
- Payment methods:
  - Multibanco. Payment method type: **multibanco**.
  - Pay Easy. Payment method type: **econtext_atm**.
  - Convenience Stores Japan. Payment method type: **econtext_stores**
  - Online Banking Japan. Payment method type: **econtext_online**.
  - Seven-Eleven: Payment method type: **econtext_seven_eleven**

## Fixed
- When building `minifyEnabled` without the `kotlin-parcelize` plugin in your project, the build should no longer crash.
- When handling actions, you no longer get the `IllegalArgumentException: Unsupported delegate type` error that causes a crash.

## Deprecated
- When creating a configuration, the `Builder` constructors with a `Context` is deprecated. You can now omit the `context` parameter.
- `PermissionException`. Handle permissions through `ActionComponentCallback`, `SessionComponentCallback`, or `ComponentCallback` callbacks instead.
- The styles for vouchers have been changed:
    - | Previous (v5.2.0 or earlier)                | Now (v5.3.0)                                  |
      |---------------------------------------------|-----------------------------------------------|
      | `AdyenCheckout.Voucher.Description.Bacs`    | `AdyenCheckout.Voucher.Simple.Description`    |
      | `AdyenCheckout.Voucher.Description.Boleto`  | `AdyenCheckout.Voucher.Full.Description`      |
      | `AdyenCheckout.Voucher.ExpirationDateLabel` | `AdyenCheckout.Voucher.InformationFieldLabel` |
      | `AdyenCheckout.Voucher.ExpirationDate`      | `AdyenCheckout.Voucher.InformationFieldValue` |
      | `AdyenCheckout.Voucher.ButtonCopyCode`      | `AdyenCheckout.Voucher.Button.CopyCode`       |
      | `AdyenCheckout.Voucher.ButtonDownloadPdf`   | `AdyenCheckout.Voucher.Button.DownloadPdf`    |
- Logger.LogLevel has been deprecated.
    - | Previous (v5.2.0 or earlier)             | Now (v5.3.0)                                    |
      |------------------------------------------|-------------------------------------------------|
      | `Logger.LogLevel`                        | `AdyenLogLevel`                                 |
      | `AdyenLogger.setLogLevel(logLevel: Int)` | `AdyenLogger.setLogLevel(level: AdyenLogLevel)` |

## Changed
- When creating a configuration, the `shopperLocale` parameter is now optional.
    - Sessions flow: when you don't set it, the shopper locale is set to the value included in the `/sessions` request.
    - Advanced flow: when you don't set it, the shopper local is set to the primary user locale on the device.
- For Drop-in, all actions now start in expanded mode.
- For the Google Pay Component, you no longer need to manually import the `3ds2` module to handle transactions that require Native 3D Secure 2 challenge.
- If you use `DropInServiceResult.Error` without specifying an error message, the default has changed from `Error sending payment. Please try again.` to `An unknown error occurred`.
- For the Sessions flow:
    - When starting Drop-in (with `DropIn.startPayment`) or creating a Component (with `YourComponent.PROVIDER.get`), the `configuration` parameter is now optional.
    - When using `CheckoutSessionProvider.createSession` to create a `CheckoutSession`, you can pass only `environment` and `clientKey` instead of the whole configuration.
    - Removing stored payment methods is now handled internally. You no longer need to override the `onRemoveStoredPaymentMethod` function.
