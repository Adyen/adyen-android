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
* The `CardComponentState.binValue` now reports 8 digit bins in case of card numbers with 16 or more digits.
* The new `CardBrand` class can be used to define unknown card brands. This can be used along with `CardType`.
* When adding new card brands through `CardConfiguration`, you can now use the new `CardBrand`to add brands that are not already defined in `CardType`.
  For example:

```kotlin
CardConfiguration.Builder([SHOPPER_LOCALE], [ENVIRONMENT], [CLIENT_KEY])
.setSupportedCardTypes(CardBrand(txVariant = "[CARD_BRAND1]"), CardBrand(txVariant = "[CARD_BRAND2]"))
.build()
```

## Changed
* Upgraded the 3D Secure 2 SDK version to v2.2.11.
* Upgraded `compileSdkVersion` and `targetSdkVersion` to 32.
* Upgraded Kotlin version to 1.6.21.
* Upgraded Kotlin coroutines version to 1.6.1.
* Upgraded Fragment version to 1.5.5.
* Upgraded AppCompat version to 1.5.1.

## Fixed
* For cards, you can now add unknown card types that aren't defined in `CardType`. Previously,
  `CardType.UNKNOWN` was not working correctly.
* There is no longer a conflict with the 3D Secure 2 SDK that causes a runtime exception. This fixes the [known issue in v4.9.0.](https://github.com/Adyen/adyen-android/releases/tag/4.9.0)

## Deprecated
* The `CardType.UNKNOWN` property. Use `CardBrand(txVariant = "[CARD_BRAND]")` instead.
* The `CardType.setTxVariant()` method. No longer needed as it was used with `CardType.UNKNOWN`.
