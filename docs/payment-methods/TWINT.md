# Twint
On this page, you can find additional configuration for adding Twint to your integration.

## Drop-in
### Sessions
The integration works out of the box for sessions implementation. Check out the integration guide [here](https://docs.adyen.com/online-payments/build-your-integration/sessions-flow/?platform=Android&integration=Drop-in).

### Advanced
There is no additional configuration required. Follow the [Advanced flow integration guide](https://docs.adyen.com/online-payments/build-your-integration/advanced-flow/?platform=Android&integration=Drop-in).

## Components
Use the following module and component names:
- To import the module use `twint`.

```Groovy
implementation "com.adyen.checkout:twint:YOUR_VERSION"
```

- To launch and show the Component use `TwintComponent`.

```Kotlin
val component = TwintComponent.PROVIDER.get(
    activity = activity, // or fragment = fragment
    checkoutSession = checkoutSession, // Should be passed only for sessions
    paymentMethod = paymentMethod,
    configuration = checkoutConfiguration,
    componentCallback = callback,
)
```

### Sessions
Make sure to follow the Android Components integration guide for sessions integration [here](https://docs.adyen.com/online-payments/build-your-integration/sessions-flow?platform=Android&integration=Components).

### Advanced
Make sure to follow the Android Components integration guide for advanced integration [here](https://docs.adyen.com/online-payments/build-your-integration/advanced-flow/?platform=Android&integration=Components).

## Optional configurations

```Kotlin
CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
    ..
) {
    twint {
        setShowStorePaymentField(true)
        setActionHandlingMethod(ActionHandlingMethod.PREFER_NATIVE)
    }
}
```

`setShowStorePaymentField` allows you to specify if a switch is shown that enables shoppers to store their details for faster future payments.
`setActionHandlingMethod` allows you to specify how the action returned from the `/payments` call will be handled. `ActionHandlingMethod.PREFER_NATIVE` will try to use a native flow and `ActionHandlingMethod.PREFER_WEB` will try to use a web/browser based flow.

## Stored Twint payments
### Sessions
- Include the [`storedPaymentMethodMode`](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions#request-storePaymentMethodMode) and [`shopperReference`](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions#request-storePaymentMethodMode) parameter in your [`/sessions`](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) request.
- The API response will contain a list of the shopper's stored payment methods.
- For drop-in, pass the API response as explained in [the integration guide](https://docs.adyen.com/online-payments/build-your-integration/sessions-flow/?platform=Android&integration=Drop-in) and it will show the stored payment methods. 
- For components, get the Twint `StoredPaymentMethod` from the list and pass it when creating the `TwintComponent`:
```Kotlin
val storedPaymentMethods = checkoutSession.sessionSetupResponse.paymentMethodsApiResponse?.storedPaymentMethods
val storedPaymentMethod = storedPaymentMethods?.filter { it.type == PaymentMethodTypes.TWINT }

val component = TwintComponent.PROVIDER.get(
    activity = activity, // or fragment = fragment
    checkoutSession = checkoutSession, // Should be passed only for sessions
    storedPaymentMethod = storedPaymentMethod,
    configuration = checkoutConfiguration,
    componentCallback = callback,
)
```

### Advanced
- When a shopper chooses to pay, they will be provided with the option to store their payment details. To remove the option to store payment details, add `setShowStorePaymentField(false)` when configuring Twint.
- The `PaymentComponentState` will contain the shopper's choice in `data.storePaymentMethod`. 
- Include [`storePaymentMethod`](https://docs.adyen.com/api-explorer/Checkout/latest/post/payments#request-storePaymentMethod) and [`shopperReference`](https://docs.adyen.com/api-explorer/Checkout/latest/post/payments#request-shopperReference) in the [`/payments`](https://docs.adyen.com/api-explorer/Checkout/latest/post/payments) request.
- Include the `shopperReference` in the [`/paymentMethods`](https://docs.adyen.com/api-explorer/Checkout/latest/post/paymentMethods) request and the response will contain a list of the shopper's stored payment methods.
- For drop-in, pass the API response as explained in [the integration guide](https://docs.adyen.com/online-payments/build-your-integration/advanced-flow/?platform=Android&integration=Drop-in) and it will show the stored payment methods.
- For components, get the Twint `StoredPaymentMethod` from the list and pass it when creating the `TwintComponent`:
```Kotlin
val storedPaymentMethods = paymentMethodsApiResponse?.storedPaymentMethods
val storedPaymentMethod = storedPaymentMethods?.filter { it.type == PaymentMethodTypes.TWINT }

val component = TwintComponent.PROVIDER.get(
    activity = activity, // or fragment = fragment
    storedPaymentMethod = storedPaymentMethod,
    configuration = checkoutConfiguration,
    componentCallback = callback,
)
```
