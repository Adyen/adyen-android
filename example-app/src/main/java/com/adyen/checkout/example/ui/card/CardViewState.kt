package com.adyen.checkout.example.ui.card

internal sealed class CardViewState {

    object Loading : CardViewState()

    object ShowComponent : CardViewState()

    object Error : CardViewState()
}
