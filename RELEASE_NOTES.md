[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## New
- For 3D Secure 2 transactions, when a challenge is unsuccessful because of error or timeout, the details are propagated in an object instead of returning an error. You can make a `/payments/details` request from your server to submit these details.

## Changed
- `compileSdkVersion` and `targetSdkVersion`: 33.

## Fixed
- For cards, when a detected card brand doesn't require a security code (CVC), the **CVC** field on the payment form no longer shows a validation error.
- For dual-branded cards, if the shopper doesn't select a detected brand, the `paymentMethod` object no longer contains a brand when submitting the payment.
- After the [`AwaitComponent`](https://github.com/Adyen/adyen-android/blob/4.13.0/await/src/main/java/com/adyen/checkout/await/AwaitComponent.java) handles an action, it no longer causes a crash when resuming your app from the background.