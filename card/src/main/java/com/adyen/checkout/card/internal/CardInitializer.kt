/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import com.adyen.checkout.card.internal.ui.CardComponent
import com.adyen.checkout.card.internal.ui.CardFactory
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.core.components.internal.PaymentMethodFactory
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.components.internal.StoredPaymentMethodFactory
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

@Keep
internal class CardInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        // TODO - Remove cast
        PaymentMethodProvider.register(
            PaymentMethodTypes.SCHEME,
            CardFactory() as PaymentMethodFactory<CardPaymentComponentState, CardComponent>,
        )
        PaymentMethodProvider.register(
            PaymentMethodTypes.SCHEME,
            CardFactory() as StoredPaymentMethodFactory<CardPaymentComponentState, CardComponent>,
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
