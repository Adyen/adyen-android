# Migration Notes

## 6.0.0-alpha01

See also:

- [docs/v6/README.md](docs/v6/README.md)
- [docs/v6/card.md](docs/v6/card.md)
- [docs/v6/theme.md](docs/v6/theme.md)

### Core objects

#### Sessions flow

##### Before (v5)

```kotlin
val checkoutConfiguration = CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
    shopperLocale = shopperLocale,
) {
    card {
        setHolderNameRequired(true)
    }
}

when (val result = CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)) {
    is CheckoutSessionResult.Success -> {
        val checkoutSession = result.checkoutSession
        // Build the payment component with the session and payment method.
    }
    is CheckoutSessionResult.Error -> {
        showError()
    }
}
```

##### After (v6)

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
    shopperLocale = shopperLocale,
) {
    card(showCardholderName = true)
    threeDS2(threeDSRequestorAppURL = "https://your-app.example/adyen")
}

lifecycleScope.launch {
    when (val result = Checkout.setup(sessionResponse = sessionResponse, configuration = configuration)) {
        is Checkout.Result.Error -> showError(result.error.message.orEmpty())
        is Checkout.Result.Success -> {
            val controller = CheckoutController(
                target = CheckoutTarget.PaymentMethod(PaymentMethodTypes.SCHEME),
                context = result.checkoutContext,
                callbacks = SessionCheckoutCallbacks(
                    onFinished = { showSuccess() },
                    onError = { error -> showError(error.message.orEmpty()) },
                ),
                coroutineScope = lifecycleScope,
            )
        }
    }
}
```

#### Advanced flow

##### Before (v5)

```kotlin
class ExampleViewModel : ComponentCallback<CardComponentState> {
    override fun onSubmit(state: CardComponentState) {
        makePayment(state.data)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }
}
```

##### After (v6)

```kotlin
lifecycleScope.launch {
    when (val result = Checkout.setup(paymentMethods = paymentMethods, configuration = configuration)) {
        is Checkout.Result.Error -> showError(result.error.message.orEmpty())
        is Checkout.Result.Success -> {
            val controller = CheckoutController(
                target = CheckoutTarget.PaymentMethod(PaymentMethodTypes.SCHEME),
                context = result.checkoutContext,
                callbacks = AdvancedCheckoutCallbacks(
                    onSubmit = { data ->
                        callPayments(data)
                    },
                    onAdditionalDetails = { data ->
                        callDetails(data)
                    },
                    onError = { error ->
                        showError(error.message.orEmpty())
                    },
                ),
                coroutineScope = lifecycleScope,
            )
        }
    }
}
```

#### Summary

- `Checkout.setup(...)` replaces `CheckoutSessionProvider.createSession(...)` and the legacy component-first setup as the main public v6 flow entry point.
- `CheckoutConfiguration` now refers to the new `com.adyen.checkout.core.components.CheckoutConfiguration` type.
- `CheckoutContext.Sessions` and `CheckoutContext.Advanced` represent the initialized flow state.
- `CheckoutController(...)` binds a checkout target and callbacks for rendering.
- `CheckoutPaymentFlow(...)` becomes the main Compose rendering entry point.

### Card component

#### Configuration object

##### Before (v5)

```kotlin
checkoutConfiguration.card {
    setHolderNameRequired(true)
    setShowStorePaymentField(true)
    setHideCvcStoredCard(false)
    setAddressConfiguration(AddressConfiguration.PostalCode())
}
```

##### After (v6)

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
) {
    card(
        billingAddressMode = BillingAddressMode.PostalCode(),
        showCardholderName = true,
        showSecurityCode = true,
        showSecurityCodeForStoredCard = true,
        showStorePaymentMethod = true,
    )
}
```

#### Callback migration

##### Before (v5)

```kotlin
class ExampleViewModel : ComponentCallback<CardComponentState> {
    override fun onSubmit(state: CardComponentState) {
        makePayment(state.data)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }
}
```

##### After (v6)

```kotlin
val callbacks = AdvancedCheckoutCallbacks(
    onSubmit = { data -> callPayments(data) },
    onAdditionalDetails = { data -> callDetails(data) },
    onError = { error -> showError(error.message.orEmpty()) },
) {
    card(
        onBinValue = OnBinValueCallback { bin ->
            println("BIN: $bin")
        },
        onBinLookup = OnBinLookupCallback { data ->
            println("BIN lookup: $data")
        },
    )
}
```

#### Rendering migration

##### Before (v5)

```kotlin
val cardComponent = CardComponent.PROVIDER.get(
    owner = activity,
    paymentMethod = paymentMethod,
    checkoutConfiguration = checkoutConfiguration,
    callback = callback,
)
```

##### After (v6)

```kotlin
CheckoutPaymentFlow(
    controller = controller,
    theme = theme,
    localizationProvider = localizationProvider,
)
```

#### Theme and localization migration

- Theme customization moves from legacy XML-oriented guidance to `CheckoutTheme` passed to `CheckoutPaymentFlow(...)`.
- Locale selection still starts from `shopperLocale` on `CheckoutConfiguration`.
- Targeted string overrides move to `CheckoutLocalizationProvider` or `StringResourceLocalizationProvider` passed to `CheckoutPaymentFlow(...)`.

#### Package migration guide

| v5 style | v6 style |
| --- | --- |
| `com.adyen.checkout.components.core.CheckoutConfiguration` | `com.adyen.checkout.core.components.CheckoutConfiguration` |
| `com.adyen.checkout.card.*` | `com.adyen.checkout.card.*` |
| `CheckoutSessionProvider.createSession(...)` | `Checkout.setup(sessionResponse = ..., configuration = ...)` |
| `ComponentCallback<CardComponentState>` | `AdvancedCheckoutCallbacks` |
| `SessionComponentCallback<CardComponentState>` | `SessionCheckoutCallbacks` |
