package com.adyen.checkout.example.ui.card

import com.adyen.checkout.components.model.payments.response.Action

internal sealed class CardAction {

    data class Redirect(val action: Action) : CardAction()
    data class ThreeDS2(val action: Action) : CardAction()
    object Unsupported : CardAction()
}
