package com.adyen.checkout.example.ui.card

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

internal sealed class CardViewState {

    object Loading : CardViewState()

    data class ShowComponent(val paymentMethod: PaymentMethod) : CardViewState()

    object Invalid : CardViewState()

    object Error : CardViewState()
}
