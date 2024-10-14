# Gift card
On this page, you can find additional configuration for adding Gift cards to your integration.

## Drop-in
### Sessions
The integration works out of the box for sessions implementation. Check out the integration guide [here](https://docs.adyen.com/online-payments/build-your-integration/sessions-flow/?platform=Android&integration=Drop-in).

### Advanced
Follow the [Advanced flow integration guide](https://docs.adyen.com/online-payments/build-your-integration/advanced-flow/?platform=Android&integration=Drop-in) and make sure the Drop-in is configured correctly and supports [partial payments](https://docs.adyen.com/online-payments/partial-payments/).

To configure Drop-in to create and cancel orders, implement the following methods in your `DropInService`:

| Method                                                             | Explanation                                                                                                                                                                                                        |
|--------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `onBalanceCheck(paymentComponentState: PaymentComponentState<*>)`  | Called when the shopper pays with a gift card. Make a [/paymentMethods/balance](https://docs.adyen.com/api-explorer/Checkout/latest/post/paymentMethods/balance) request.                                          |
| `onOrderRequest()`                                                 | Called when the gift card balance is less than the transaction amount. Make an [/orders](https://docs.adyen.com/api-explorer/Checkout/latest/post/orders) request with the amount of the total transaction amount. |
| `onOrderCancel(order: Order, shouldUpdatePaymentMethods: Boolean)` | Called when the shopper cancels the gift card transaction. Make an [/orders/cancel](https://docs.adyen.com/api-explorer/#/CheckoutService/latest/post/orders/cancel) request.                                      |

The following example shows how to configure Drop-in for gift cards:
```Kotlin
override fun onBalanceCheck(paymentComponentState: PaymentComponentState<*>) {
    // Make a POST /paymentMethods/balance request
    if (isSuccessfulResponse()) {
        val balanceResult = BalanceResult.SERIALIZER.deserialize(jsonResponse)
        val dropInServiceResult = BalanceDropInServiceResult.Balance(balanceResult)
        sendBalanceResult(dropInServiceResult)
    } else {
        val dropInServiceResult = BalanceDropInServiceResult.Error(..)
        sendBalanceResult(dropInServiceResult)
    }
}

override fun onOrderRequest() {
    // Make a POST /orders request
    if (isSuccessfulResponse()) {
        val orderResponse = OrderResponse.SERIALIZER.deserialize(jsonResponse)
        val dropInServiceResult = OrderDropInServiceResult.OrderCreated(orderResponse)
        sendOrderResult(dropInServiceResult)
    } else {
        val dropInServiceResult = OrderDropInServiceResult.Error(..)
        sendOrderResult(dropInServiceResult)
    }
}

override fun onOrderCancel(order: Order, shouldUpdatePaymentMethods: Boolean) {
    val orderJson = OrderRequest.SERIALIZER.serialize(order)
    // Make a POST /orders/cancel request
    if (isSuccessfulResponse()) {
        if (shouldUpdatePaymentMethods) {
            // shouldUpdatePaymentMethods is true when the shopper manually removes their gift cards and cancels the order
            // The total reverts to the original amount and you might need to fetch your payment methods and update Drop-in with the new list of payment methods
            val paymentMethods = fetchPaymentMethods() // Fetch the payment methods here
            val dropInServiceResult = DropInServiceResult.Update(paymentMethods, null) // Update the payment methods
        } else {
            // shouldUpdatePaymentMethods is false when Drop-in is closed while the order is in progress
            // If this happens, there is no need to make any further calls.
        }
    } else {
        val dropInServiceResult = DropInServiceResult.Error(..)
        sendResult(dropInServiceResult)
    }
}
```

## Components
Use the following module and component names:
- To import the module use `giftcard`.

```Groovy
implementation "com.adyen.checkout:giftcard:YOUR_VERSION"
```

- To launch and show the Component use `GiftCardComponent`.

```Kotlin
val component = GiftCardComponent.PROVIDER.get(
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

The `componentCallback` which is passed to the `GiftCardComponent.PROVIDER` requires the following methods to be implemented:

| Method                                                            | Explanation                                                                                                                                                                                                                                                                                                   |
|-------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `onRequestOrder()`                                                | In this method you should make a network call to the [/orders](https://docs.adyen.com/api-explorer/Checkout/latest/post/orders) endpoint of the Checkout API through your server. This method is called when the user is trying to pay a part of the amount using a partial payment method.                   |
| `onBalanceCheck(paymentComponentState: PaymentComponentState<*>)` | In this method you should make a network call to the [/paymentMethods/balance](https://docs.adyen.com/api-explorer/Checkout/latest/post/paymentMethods/balance) endpoint of the Checkout API through your server. This method is called right after the user enters their gift card details and submits them. |

The following example shows how to implement the `GiftCardComponentCallback`:

```Kotlin
override fun onRequestOrder() {
    // Make a POST /orders request
    if (isSuccessfulResponse()) {
        val orderResponse = OrderResponse.SERIALIZER.deserialize(jsonResponse)
        giftCardComponent.resolveOrderResponse(event.order)
    }
}

override fun onBalanceCheck(paymentComponentState: PaymentComponentState<*>) {
    // Make a POST /paymentMethods/balance request
    if (isSuccessfulResponse()) {
        val balanceResult = BalanceResult.SERIALIZER.deserialize(jsonResponse)
        giftCardComponent.resolveBalanceResult(event.balanceResult)
    }
}
```

## Optional configurations

```Kotlin
CheckoutConfiguration(
    environment = environment,
    clientKey = clientKey,
    ..
) {
    giftCard {
        setPinRequired(true)
    }
}
```

`setPinRequired` allows you to specify if the Pin field should be hidden from the Component and not requested by the shopper. Note that this might have implications for the transaction.
