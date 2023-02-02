[//]: # (This file will be used for the release notes on GitHub when publishing.)
[//]: # (Types of changes: `Added` `Changed` `Deprecated` `Removed` `Fixed` `Security`)
[//]: # (Example:)
[//]: # (## Added)
[//]: # ( - New payment method)
[//]: # (## Changed)
[//]: # ( - DropIn service's package changed from `com.adyen.dropin` to `com.adyen.dropin.services`)
[//]: # ( # Deprecated)
[//]: # ( - Configurations public constructor are deprecated, please use each Configuration's builder to make a Configuration object)

# New
* We added `CardBrand`, along with `CardType`, to use when adding new card brands through `CardConfiguration`.
* In `CardConfiguration` when adding supported card types you can now add new supported card types which are not part of `CardType`.
  example:

```kotlin
CardConfiguration.Builder([SHOPPER_LOCALE], [ENVIRONMENT], [CLIENT_KEY])
.setSupportedCardTypes(CardBrand(txVariant = "[CARD_BRAND1]"), CardBrand(txVariant = "[CARD_BRAND2]"))
.build()
```

## Fixed
* Undefined supported card types now works correctly for card component/drop-in.
  `CardType.UNKNOWN` was not working correctly when there were supported card brands coming from `/paymentMethods`, under `scheme` payment method,
  which are not part of `CardType` predefined brands, so we had to add `CardBrand` to make these undefined supported card types work.

## Deprecated
* The `CardType.UNKNOWN` property. Use `CardBrand(txVariant = "[CARD_BRAND]")` instead.
* The `CardType.setTxVariant()` method. No longer needed as it was used with `CardType.UNKNOWN`