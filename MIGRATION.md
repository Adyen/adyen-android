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

    adyen3DS2 {
        setThreeDSRequestorAppURL("https://your-app.example/adyen")
    }
}

when (val result = CheckoutSessionProvider.createSession(sessionModel, checkoutConfiguration)) {
    is CheckoutSessionResult.Success -> {
        val checkoutSession = result.checkoutSession
        // Pass checkoutSession to your payment-method-specific integration.
    }
    is CheckoutSessionResult.Error -> showError()
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
            val sessionsContext = result.checkoutContext
            // Use sessionsContext with your payment-method-specific integration.
        }
    }
}
```

For a complete card example, see [docs/v6/card-session-flow.md](docs/v6/card-session-flow.md).

#### Advanced flow

##### Before (v5)

```kotlin
class CardActivity : AppCompatActivity() {
    private val viewModel: ExampleViewModel by viewModels()

    private fun setupCardView(
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
    ) {
        val cardComponent = CardComponent.PROVIDER.get(
            activity = this,
            paymentMethod = paymentMethod,
            checkoutConfiguration = checkoutConfiguration,
            callback = viewModel,
        )

        binding.cardView.attach(cardComponent, this)
    }
}

class ExampleViewModel : ViewModel(), ComponentCallback<CardComponentState> {
    override fun onSubmit(state: CardComponentState) {
        makePayment(state.data)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        showError(componentError.errorMessage)
    }
}
```

##### After (v6)

```kotlin
class CardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            when (val result = Checkout.setup(paymentMethods = paymentMethods, configuration = configuration)) {
                is Checkout.Result.Error -> showError(result.error.message.orEmpty())
                is Checkout.Result.Success -> {
                    setContent {
                        CheckoutScreen(
                            controller = createCheckoutController(result.checkoutContext),
                            theme = theme,
                            localizationProvider = localizationProvider,
                        )
                    }
                }
            }
        }
    }

    private fun createCheckoutController(checkoutContext: CheckoutContext.Advanced): CheckoutController {
        return CheckoutController(
            target = CheckoutTarget.PaymentMethod(PaymentMethodTypes.SCHEME),
            context = checkoutContext,
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
            ) {
                card()
            },
            coroutineScope = lifecycleScope,
        )
    }
}

@Composable
private fun CheckoutScreen(
    controller: CheckoutController,
    theme: CheckoutTheme,
    localizationProvider: CheckoutLocalizationProvider?,
) {
    CheckoutPaymentFlow(
        controller = controller,
        theme = theme,
        localizationProvider = localizationProvider,
    )
}
```

For a complete card example, see [docs/v6/card-advanced-flow.md](docs/v6/card-advanced-flow.md).

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
val checkoutConfiguration = CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
) {
    card {
        setHolderNameRequired(true)
        setShowStorePaymentField(true)
        setHideCvcStoredCard(false)
        setAddressConfiguration(AddressConfiguration.PostalCode())
    }
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

- `ComponentCallback<CardComponentState>.onSubmit(...)` moves to `AdvancedCheckoutCallbacks(onSubmit = { ... })`.
- `ComponentCallback<CardComponentState>.onAdditionalDetails(...)` moves to `AdvancedCheckoutCallbacks(onAdditionalDetails = { ... })`.
- Card-specific callbacks such as BIN events are registered inside the `card(...)` block on your checkout callbacks.
- For end-to-end callback examples, see [docs/v6/card-session-flow.md](docs/v6/card-session-flow.md) and [docs/v6/card-advanced-flow.md](docs/v6/card-advanced-flow.md).

#### Rendering migration

##### Before (v5)

```kotlin
val cardComponent = CardComponent.PROVIDER.get(
    owner = activity,
    paymentMethod = paymentMethod,
    checkoutConfiguration = checkoutConfiguration,
    callback = callback,
)

binding.cardView.attach(cardComponent, activity)
```

##### After (v6)

Rendering now happens from your Compose UI layer:

```kotlin
@Composable
fun CheckoutScreen(
    controller: CheckoutController,
    theme: CheckoutTheme,
    localizationProvider: CheckoutLocalizationProvider?,
) {
    CheckoutPaymentFlow(
        controller = controller,
        theme = theme,
        localizationProvider = localizationProvider,
    )
}
```

#### Theme and localization migration

- Theme customization moves from legacy XML-oriented guidance to `CheckoutTheme` passed to `CheckoutPaymentFlow(...)`.
- Locale selection still starts from `shopperLocale` on `CheckoutConfiguration`.
- Targeted string overrides move to `CheckoutLocalizationProvider` or `StringResourceLocalizationProvider` passed to `CheckoutPaymentFlow(...)`.

#### Package migration guide

| v5 style | v6 style |
| --- | --- |
| `com.adyen.checkout.components.core.CheckoutConfiguration` | `com.adyen.checkout.core.components.CheckoutConfiguration` |
| `com.adyen.checkout.card.*` | `com.adyen.checkout.card.*` (package unchanged) |
| `CheckoutSessionProvider.createSession(...)` | `Checkout.setup(sessionResponse = ..., configuration = ...)` |
| `ComponentCallback<CardComponentState>` | `AdvancedCheckoutCallbacks` |
| `SessionComponentCallback<CardComponentState>` | `SessionCheckoutCallbacks` |
