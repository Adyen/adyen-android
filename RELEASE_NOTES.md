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
- The `:action` module no longer comes with the `:3ds2` and `:wechatpay` modules by default. If you are using standalone components and want to handle these actions you have to add the dependencies for these modules like below. The `:card` and `:bcmc` modules do come with `:3ds2`, so it's not needed to add extra dependencies there.
```Groovy
implementation 'com.adyen.checkout:3ds2:{your version}'
implementation 'com.adyen.checkout:wechatpay:{your version}'
```

## New
- Payment methods:
  - [Boleto Bancário](https://docs.adyen.com/payment-methods/boleto-bancario). Payment method type: *boletobancario*.
