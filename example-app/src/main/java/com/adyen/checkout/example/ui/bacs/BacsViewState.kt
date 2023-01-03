package com.adyen.checkout.example.ui.bacs

import androidx.annotation.StringRes

internal sealed class BacsViewState {

    object Loading : BacsViewState()

    object ShowComponent : BacsViewState()

    data class Error(@StringRes val message: Int) : BacsViewState()
}
