package com.adyen.checkout.example.ui.giftcard

internal sealed class GiftCardViewState {

    object Loading : GiftCardViewState()

    object ShowComponent : GiftCardViewState()

    object Error : GiftCardViewState()
}
