package com.adyen.checkout.example.ui.bacs

internal sealed class BacsViewState {

    object Loading : BacsViewState()

    object ShowComponent : BacsViewState()

    object Error : BacsViewState()
}
