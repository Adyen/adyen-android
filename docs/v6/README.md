# Adyen Android v6 alpha

This guide covers the v6 checkout entry points and the shared concepts used by the Android v6 alpha documentation set.

For card-specific configuration and flow guides, see [card.md](card.md), [card-session-flow.md](card-session-flow.md), and [card-advanced-flow.md](card-advanced-flow.md). For Compose theme customization, see [theme.md](theme.md). For migration notes, see [../../MIGRATION.md](../../MIGRATION.md).

## Alpha status

This documentation set describes the current public Android v6 alpha API.

- The documented flow is Compose-first.
- The documented scope is limited to the currently public API surface.
- Drop-in is out of scope for this alpha guide set.
- Card billing address coverage is limited to the public modes that exist today.

## Overview

The public integration surface is centered around four concepts:

- `Checkout.setup(...)` initializes the checkout flow.
- `CheckoutConfiguration` is the shared configuration container.
- `CheckoutController(...)` binds a checkout target to callbacks for either sessions or advanced flow.
- `CheckoutPaymentFlow(...)` renders the Compose UI for the active checkout controller.

Depending on the flow, `Checkout.setup(...)` returns either `CheckoutContext.Sessions` or `CheckoutContext.Advanced`.

## Imports

Import the APIs used by these examples:

```kotlin
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.card.BillingAddressMode
import com.adyen.checkout.card.card
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.StringResourceLocalizationProvider
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.components.AnalyticsConfiguration
import com.adyen.checkout.core.components.AnalyticsLevel
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutPaymentFlow
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.threeds2.threeDS2
import com.adyen.checkout.ui.theme.CheckoutTheme
import java.util.Locale
import kotlinx.coroutines.launch
```

## CheckoutConfiguration

`CheckoutConfiguration` is the checkout-wide container for:

- environment and client key
- optional shopper locale, amount, analytics, and submit-button behavior
- payment-method configuration registered through the builder block
- action configuration such as 3D Secure 2

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
    shopperLocale = Locale.forLanguageTag("en-US"),
    amount = Amount(currency = "USD", value = 10_00),
    analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.ALL),
    showSubmitButton = true,
) {
    card(
        billingAddressMode = BillingAddressMode.PostalCode(),
        showCardholderName = true,
        showSecurityCode = true,
        showSecurityCodeForStoredCard = true,
        showStorePaymentMethod = true,
    )

    threeDS2(
        threeDSRequestorAppURL = "https://your-app.example/adyen",
    )
}
```

Use the builder block to register payment-method and action configuration that should apply to the flow.

In session flow, `showStorePaymentMethod` and `installmentConfiguration` for cards are controlled by the `/sessions` response. Their effective values come from `enableStoreDetails`, `installmentOptions`, and `showInstallmentAmount`. Configure them in your session request and do not rely on component-level values to override them.

## Session flow

Use the session flow when your backend starts checkout with `/sessions`.
The example below reuses the shared `configuration` from [CheckoutConfiguration](#checkoutconfiguration).

```kotlin
lifecycleScope.launch {
    when (val result = Checkout.setup(sessionResponse = sessionResponse, configuration = configuration)) {
        is Checkout.Result.Error -> {
            showError(result.error.message.orEmpty())
        }
        is Checkout.Result.Success -> {
            val sessionsContext = result.checkoutContext
            // Pass sessionsContext to your payment-method-specific integration.
        }
    }
}
```

For a complete card example that creates `CheckoutController(...)` and renders `CheckoutPaymentFlow(...)`, see [card-session-flow.md](card-session-flow.md).

## Advanced flow

Use the advanced flow when your backend starts checkout with `/paymentMethods` and handles `/payments` and `/payments/details`.

```kotlin
lifecycleScope.launch {
    when (val result = Checkout.setup(paymentMethods = paymentMethods, configuration = configuration)) {
        is Checkout.Result.Error -> {
            showError(result.error.message.orEmpty())
        }
        is Checkout.Result.Success -> {
            val advancedContext = result.checkoutContext
            // Pass advancedContext to your payment-method-specific integration.
        }
    }
}
```

For a complete card example that wires `AdvancedCheckoutCallbacks(...)` and renders `CheckoutPaymentFlow(...)`, see [card-advanced-flow.md](card-advanced-flow.md).

## Rendering the Compose flow

Render the public Compose UI with `CheckoutPaymentFlow(...)` from your `@Composable` UI:

```kotlin
CheckoutPaymentFlow(
    controller = controller,
    theme = theme,
    localizationProvider = localizationProvider,
)
```

## Theme

Use `CheckoutTheme` to customize the Compose checkout UI. For the full guide, see [theme.md](theme.md).

## Localization

Android v6 uses two separate concepts for localization:

- `shopperLocale` on `CheckoutConfiguration` sets the shopper locale used by the checkout flow.
- `CheckoutLocalizationProvider` lets you override specific checkout strings at render time.

Use `StringResourceLocalizationProvider` when you want targeted string overrides backed by your app resources:

```kotlin
val localizationProvider = StringResourceLocalizationProvider(
    mapOf(
        CheckoutLocalizationKey.CARD_NUMBER to R.string.checkout_card_number_custom,
        CheckoutLocalizationKey.CARD_SECURITY_CODE to R.string.checkout_card_security_code_custom,
    ),
)

CheckoutPaymentFlow(
    controller = controller,
    theme = theme,
    localizationProvider = localizationProvider,
)
```

Use `shopperLocale` when you want the checkout flow to follow a specific shopper locale. Use a localization provider when you want to override selected strings without replacing the full locale handling.
You can also override checkout string resources through XML. For more information, see [Overriding string resources](../UI_CUSTOMIZATION.md#overriding-string-resources).

## Next steps

- [Card component overview](card.md)
- [Card component: session flow](card-session-flow.md)
- [Card component: advanced flow](card-advanced-flow.md)
- [Checkout theme](theme.md)
- [Migration notes](../../MIGRATION.md)
