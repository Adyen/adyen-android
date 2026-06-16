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
        showStorePaymentMethod = true,
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
                        onBinValue = OnBinValueCallback { bin ->
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
