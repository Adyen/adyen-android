# Google Pay component

This guide covers the Google Pay component in Android v6 alpha, using the session flow.

For the shared v6 concepts, see [README.md](README.md).

## Imports

Import the APIs used by this guide:

```kotlin
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.googlepay.GooglePayAllowedPaymentMethods
import com.adyen.checkout.googlepay.GooglePayButtonAppearance
import com.adyen.checkout.googlepay.GooglePayButtonType
import com.adyen.checkout.googlepay.googlePay
```

## Minimal setup

Google Pay works without any Google Pay configuration. The amount comes from the session, and the merchant account comes with the Google Pay payment method in the session.

Create the controller in a `ViewModel`, and pass its `viewModelScope` and `SavedStateHandle` so the checkout flow can be restored after process death:

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
)

viewModelScope.launch {
    when (val result = Checkout.setup(sessionResponse = sessionResponse, configuration = configuration)) {
        is Checkout.Result.Error -> {
            showError(result.error.message.orEmpty())
        }
        is Checkout.Result.Success -> {
            val controller = CheckoutController(
                target = CheckoutTarget.PaymentMethod(PaymentMethodTypes.GOOGLE_PAY),
                context = result.checkoutContext,
                callbacks = SessionCheckoutCallbacks(
                    onAction = {
                        showActionScreen()
                    },
                    onComplete = { checkoutResult ->
                        showSuccess(checkoutResult.resultCode.value)
                    },
                    onFailure = { error ->
                        showError(error.message.orEmpty())
                    },
                ),
                coroutineScope = viewModelScope,
                savedStateHandle = savedStateHandle,
            )
        }
    }
}
```

Render the controller with `CheckoutPaymentMethod(...)` and `CheckoutAction(...)`, as described in [README.md](README.md#rendering-the-compose-flow).

Google Pay can only be used when Google Play services are available and Google reports that the shopper is ready to pay. If not, the component reports an error through `onFailure`. To check this before showing Google Pay in your own UI, call `Checkout.isPaymentMethodAvailable(PaymentMethodTypes.GOOGLE_PAY, checkoutContext)` from a coroutine. See [README.md](README.md#available-payment-methods).

If the session doesn't contain a merchant account for Google Pay and you don't pass `merchantAccount`, `CheckoutController(...)` throws a `CheckoutException` with the `INVALID_CONFIGURATION` error code.

## Optional configuration

Register `googlePay(...)` inside `CheckoutConfiguration` only to change a default:

```kotlin
val configuration = CheckoutConfiguration(
    environment = Environment.TEST,
    clientKey = clientKey,
) {
    googlePay(
        countryCode = "NL",
        allowedPaymentMethods = GooglePayAllowedPaymentMethods(
            card = GooglePayAllowedPaymentMethods.Card(
                allowPrepaidCards = false,
            ),
        ),
        appearance = GooglePayButtonAppearance(type = GooglePayButtonType.PAY),
    )
}
```

### Defaults

| API | When not set |
| --- | --- |
| `merchantAccount` | Taken from the Google Pay payment method in the session. |
| `googlePayEnvironment` | Google Pay's test environment for `Environment.TEST`, production for live environments. |
| `totalPriceStatus` | `"FINAL"`. |
| `countryCode` | Not sent. See [Country code](#country-code). |
| `merchantInfo` | Only SDK information is sent, no merchant name. |
| `allowedPaymentMethods` | A single card payment method with the defaults below. |
| `isEmailRequired` | `false`. |
| `isExistingPaymentMethodRequired` | `false`. |
| `isShippingAddressRequired` | `false`. |
| `shippingAddressParameters` | Not sent. |
| `checkoutOption` | Not sent. Google Pay decides the button text in the payment sheet. |
| `appearance` | A "Buy with Google Pay" button with a 100 dp corner radius. Its theme follows the checkout theme: a light button on a dark background, a dark button otherwise. |

For `GooglePayAllowedPaymentMethods.Card`:

| API | When not set |
| --- | --- |
| `allowedAuthMethods` | `PAN_ONLY` and `CRYPTOGRAM_3DS`. |
| `allowedCardNetworks` | The card brands of the Google Pay payment method in the session, or all supported networks if there are none. |
| `allowPrepaidCards` | `true`. |
| `allowCreditCards` | Not sent. Google Pay allows credit cards by default. |
| `issuerCountryCodes` | No restriction. |
| `assuranceDetailsRequired` | Not sent. Google Pay doesn't request assurance details by default. |
| `billingAddressRequired` | `false`. |
| `billingAddressParameters` | Not sent. |

The KDoc of `googlePay(...)` and `GooglePayAllowedPaymentMethods` describes each parameter in detail.

## Country code

If you process transactions in the European Economic Area (EEA) or another country with Strong Customer Authentication (SCA), set `countryCode` to the country of your acquirer bank. This is not the shopper's country.

Without it, the request to Google Pay is still valid, but issuers may decline the payment or the liability shift may not apply. The SDK doesn't know your acquirer's country, so it leaves the field out rather than guessing. Ask your Adyen contact if you are not sure which country to use.

## Google Pay button

The component shows the Google Pay button, and tapping it opens the Google Pay payment sheet. If you set `showSubmitButton = false` on `CheckoutConfiguration`, the button is hidden and you open the payment sheet yourself by calling `controller.submit()`. This only works once Google Pay is available: until the availability check passes, or if it fails, `submit()` does nothing.

## Before you go live

- Enable Google Pay for your merchant account in the Adyen Customer Area.
- Use a live `Environment`. Google Pay then switches to production automatically.
- If you process transactions in the EEA or another SCA country, set `countryCode`. See [Country code](#country-code).

## Related docs

- [v6 foundations](README.md)
- [Checkout theme](theme.md)
- [Migration notes](../../MIGRATION.md)
