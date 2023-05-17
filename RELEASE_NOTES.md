[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

## Breaking Changes
- Dependency versions:
  | Name                                                                                                   | Version                       |
  |--------------------------------------------------------------------------------------------------------|-------------------------------|
  | [Android Gradle plugin](https://developer.android.com/build/releases/gradle-plugin)                    | **8.0.1** (requires Java 17)  |
- All classes in `com.adyen.checkout.action` are moved to `com.adyen.checkout.action.core`. Please update your imports if applicable.
- Standalone payment components no longer come with the 3DS2 and WeChat Pay actions. If you are using standalone components and want to handle these actions you have to add the dependencies like below. The `CardComponent` and `BcmcComponent` are able to handle 3DS2 actions, so it's not needed to add extra dependencies.
```Groovy
implementation 'com.adyen.checkout:3ds2:{your version}'
implementation 'com.adyen.checkout:wechatpay:{your version}'
```

## New
- Payment methods:
  - [Boleto Bancário](https://docs.adyen.com/payment-methods/boleto-bancario). Payment method type: *boletobancario*.
