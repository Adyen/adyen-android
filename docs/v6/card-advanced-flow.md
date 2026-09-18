# Card component: advanced flow

Use this guide when your backend starts checkout with `/paymentMethods` and handles `/payments` and `/payments/details`.

For shared card configuration, see [card.md](card.md). For shared v6 concepts, see [README.md](README.md).

## When to use advanced flow

Use advanced flow when your backend manages payment submission and additional details explicitly.

## Example

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
    shopperLocale = Locale.forLanguageTag("en-US"),
    amount = Amount(currency = "USD", value = 10_00),
) {
    card(
        billingAddressMode = BillingAddressMode.PostalCode(),
        showCardholderName = true,
        showSecurityCode = true,
        showStorePaymentMethod = true,
    )
    authentication(threeDSRequestorAppURL = "https://your-app.example/adyen")
}

lifecycleScope.launch {
    when (val result = Checkout.setup(paymentMethods = paymentMethods, configuration = configuration)) {
        is Checkout.Result.Error -> {
            showError(result.error.message.orEmpty())
        }
        is Checkout.Result.Success -> {
            val controller = CheckoutController(
                target = CheckoutTarget.PaymentMethod(PaymentMethodTypes.SCHEME),
                context = result.checkoutContext,
                callbacks = AdvancedCheckoutCallbacks(
                    onSubmit = { data ->
                        callPayments(data)
                    },
                    onAction = {
                        isHandlingAction = true
                    },
                    onAdditionalDetails = { data ->
                        callDetails(data)
                    },
                    onFailure = { error ->
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

`callPayments(...)` should return `SubmitResult`, and `callDetails(...)` should return `AdditionalDetailsResult`. Once you have the controller, render it from your `@Composable` UI, switching to the action once `onAction` has set `isHandlingAction`:

```kotlin
if (isHandlingAction) {
    CheckoutAction(
        controller = controller,
        theme = theme,
        localizationProvider = localizationProvider,
    )
} else {
    CheckoutPaymentMethod(
        controller = controller,
        theme = theme,
        localizationProvider = localizationProvider,
    )
}
```

See [README.md](README.md#rendering-the-compose-flow) for the full rendering contract.

## Complete working example

- [ViewModel setup and callbacks](../../example-app/src/main/java/com/adyen/checkout/example/ui/v6/V6ViewModel.kt)
- [Activity host](../../example-app/src/main/java/com/adyen/checkout/example/ui/v6/V6Activity.kt)
- [Compose rendering](../../example-app/src/main/java/com/adyen/checkout/example/ui/v6/V6Screen.kt)

## Related docs

- [Card component overview](card.md)
- [Card component: session flow](card-session-flow.md)
- [Checkout theme](theme.md)
- [Migration notes](../../MIGRATION.md)
