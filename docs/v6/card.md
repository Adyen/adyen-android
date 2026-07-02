# Card component

This guide covers the shared card component API in Android v6 alpha. For flow-specific setup, use:

- [Card component: session flow](card-session-flow.md)
- [Card component: advanced flow](card-advanced-flow.md)

For the shared v6 concepts, see [README.md](README.md).

## Imports

Import the APIs used by the card guides:

```kotlin
import com.adyen.checkout.card.BillingAddressMode
import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.OnBinChangeCallback
import com.adyen.checkout.card.OnBinLookupCallback
import com.adyen.checkout.card.card
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.threeds2.threeDS2
```

## Configure the card component

Register card configuration inside `CheckoutConfiguration`:

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
    amount = Amount(currency = "USD", value = 10_00),
) {
    card(
        billingAddressMode = BillingAddressMode.PostalCode(),
        koreanAuthenticationVisibility = FieldVisibility.SHOW,
        showCardholderName = true,
        showSecurityCode = true,
        showSecurityCodeForStoredCard = true,
        showStorePaymentMethod = true,
        showSupportedCardBrandLogos = true,
        socialSecurityNumberVisibility = FieldVisibility.HIDE,
        supportedCardBrands = listOf(CardBrand("visa"), CardBrand("mc")),
        showCardScanner = true,
    )
}
```

### Public `card(...)` configuration parameters

| API | Purpose |
| --- | --- |
| `billingAddressMode` | Controls billing-address collection for the card flow. |
| `koreanAuthenticationVisibility` | Shows or hides the Korean card authentication field. |
| `showCardholderName` | Shows or hides the cardholder-name field. |
| `showSecurityCode` | Shows or hides the security-code field for regular cards. |
| `showSecurityCodeForStoredCard` | Shows or hides the security-code field for stored cards. |
| `showStorePaymentMethod` | Shows or hides the “store payment method” toggle. |
| `showSupportedCardBrandLogos` | Controls whether supported-card logos are shown. |
| `socialSecurityNumberVisibility` | Shows or hides the CPF/CNPJ field. |
| `supportedCardBrands` | Restricts the supported card brands for the flow. |
| `showCardScanner` | Shows or hides the card scanner entry point. |
| `installmentConfiguration` | Configures installments. |

When you use [session flow](card-session-flow.md), some card settings, including `showStorePaymentMethod`, `installmentConfiguration`, and `showInstallmentAmount`, are determined by the session instead of component-level overrides. For details, see [card-session-flow.md](card-session-flow.md).

## Public billing-address coverage in alpha

Android v6 currently exposes these public billing-address modes:

- `BillingAddressMode.None()`
- `BillingAddressMode.PostalCode()`

This guide intentionally documents only the currently public API surface.

## Card callbacks

Register card-specific callbacks through the checkout callbacks block:

```kotlin
val callbacks = AdvancedCheckoutCallbacks(
    onSubmit = { data -> callPayments(data) },
    onAdditionalDetails = { data -> callDetails(data) },
    onError = { error -> showError(error.message.orEmpty()) },
) {
    card(
        onBinChange = OnBinChangeCallback { bin ->
            println("BIN: $bin")
        },
        onBinLookup = OnBinLookupCallback { data ->
            println("BIN lookup: $data")
        },
    )
}
```

### Public card callbacks

| API | Purpose |
| --- | --- |
| `OnBinChangeCallback` | Receives BIN updates while the shopper types. |
| `OnBinLookupCallback` | Receives the card-brand lookup result for the current input. |

## 3D Secure configuration

3DS2-specific settings are configured with `threeDS2(...)`, not `card(...)`:

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
) {
    card(showCardholderName = true)
    threeDS2(
        threeDSRequestorAppURL = "https://your-app.example/adyen",
    )
}
```

## Flow guides

- Use [card-session-flow.md](card-session-flow.md) when your backend starts checkout with `/sessions`.
- Use [card-advanced-flow.md](card-advanced-flow.md) when your backend starts checkout with `/paymentMethods` and handles `/payments` and `/payments/details`.

## Related docs

- [v6 foundations](README.md)
- [Card component: session flow](card-session-flow.md)
- [Card component: advanced flow](card-advanced-flow.md)
- [Checkout theme](theme.md)
- [Migration notes](../../MIGRATION.md)
