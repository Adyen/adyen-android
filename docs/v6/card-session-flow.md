# Card component: session flow

Use this guide when your backend starts checkout with `/sessions`.

For shared card configuration, see [card.md](card.md). For shared v6 concepts, see [README.md](README.md).

## When to use session flow

Use session flow when you want the SDK to manage the checkout flow from a `SessionResponse`.

## Example

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
    shopperLocale = Locale.forLanguageTag("en-US"),
) {
    card(
        showCardholderName = true,
        showSecurityCode = true,
    )
    threeDS2(threeDSRequestorAppURL = "https://your-app.example/adyen")
}

lifecycleScope.launch {
    when (val result = Checkout.setup(sessionResponse = sessionResponse, configuration = configuration)) {
        is Checkout.Result.Error -> {
            showError(result.error.message.orEmpty())
        }
        is Checkout.Result.Success -> {
            val controller = CheckoutController(
                target = CheckoutTarget.PaymentMethod(PaymentMethodTypes.SCHEME),
                context = result.checkoutContext,
                callbacks = SessionCheckoutCallbacks(
                    onFinished = {
                        showSuccess()
                    },
                    onError = { error ->
                        showError(error.message.orEmpty())
                    },
                ) {
                    card(
                        onBinChange = OnBinChangeCallback { bin ->
                            println("BIN: $bin")
                        },
                        onBinLookup = OnBinLookupCallback { data ->
                            println("BIN lookup: $data")
                        },
                    )
                },
                coroutineScope = lifecycleScope,
            )
        }
    }
}
```

## Session-controlled card settings

When you use `/sessions`, some card settings are determined by the session instead of component-level `card(...)` parameters:

- `showStorePaymentMethod` cannot be overridden from `CardConfiguration`. Set the server-side `/sessions` request parameter `storePaymentMethodMode` when creating the session. `askForConsent` shows the toggle, while `enabled` and `disabled` hide it.
- `installmentConfiguration` and `showInstallmentAmount` are also determined by the session and cannot be overridden from component-level configuration.

Configure these values in your `/sessions` request and do not rely on component-level values to override them.

Once you have the controller, render it from your `@Composable` UI with `CheckoutPaymentFlow(...)`. Pass a `CheckoutTheme` and optional `CheckoutLocalizationProvider` when you render the flow. See [theme.md](theme.md) and [README.md](README.md#localization).

```kotlin
CheckoutPaymentFlow(
    controller = controller,
    theme = theme,
    localizationProvider = localizationProvider,
)
```

## Complete working example

- [ViewModel setup and callbacks](../../example-app/src/main/java/com/adyen/checkout/example/ui/v6/V6SessionsViewModel.kt)
- [Activity host](../../example-app/src/main/java/com/adyen/checkout/example/ui/v6/V6SessionsActivity.kt)
- [Compose rendering](../../example-app/src/main/java/com/adyen/checkout/example/ui/v6/V6Screen.kt)

## Related docs

- [Card component overview](card.md)
- [Card component: advanced flow](card-advanced-flow.md)
- [Checkout theme](theme.md)
- [Migration notes](../../MIGRATION.md)
