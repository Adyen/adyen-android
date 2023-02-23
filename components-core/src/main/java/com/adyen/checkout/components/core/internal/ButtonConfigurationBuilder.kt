package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ButtonConfigurationBuilder {

    fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): ButtonConfigurationBuilder
}
