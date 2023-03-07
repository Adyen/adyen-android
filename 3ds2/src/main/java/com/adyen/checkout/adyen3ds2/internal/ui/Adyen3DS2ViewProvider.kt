/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/9/2022.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import android.content.Context
import android.util.AttributeSet
import com.adyen.checkout.adyen3ds2.internal.ui.view.DelegatedAuthenticationView
import com.adyen.checkout.adyen3ds2.internal.ui.view.DelegatedAuthenticationRegistrationView
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvider
import com.adyen.checkout.ui.core.internal.ui.view.PaymentInProgressView

internal object Adyen3DS2ViewProvider : ViewProvider {

    override fun getView(
        viewType: ComponentViewType,
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): ComponentView = when (viewType) {
        is Adyen3DS2ComponentViewType -> {
            when (viewType) {
                Adyen3DS2ComponentViewType.DELEGATED_AUTHENTICATION_REGISTRATION ->
                    DelegatedAuthenticationRegistrationView(
                        context,
                        attrs,
                        defStyleAttr
                    )
                Adyen3DS2ComponentViewType.DELEGATED_AUTHENTICATION -> DelegatedAuthenticationView(
                    context,
                    attrs,
                    defStyleAttr
                )
                Adyen3DS2ComponentViewType.REDIRECT -> PaymentInProgressView(context, attrs, defStyleAttr)
            }
        }
        else -> throw IllegalStateException("Unsupported view type")
    }
}

internal enum class Adyen3DS2ComponentViewType : ComponentViewType {
    DELEGATED_AUTHENTICATION_REGISTRATION, DELEGATED_AUTHENTICATION, REDIRECT;

    override val viewProvider: ViewProvider = Adyen3DS2ViewProvider
}
