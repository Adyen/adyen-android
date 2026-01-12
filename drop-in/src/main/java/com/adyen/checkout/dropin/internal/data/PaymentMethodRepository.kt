/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal interface PaymentMethodRepository {

    val regulars: List<PaymentMethod>

    val favorites: Flow<List<StoredPaymentMethod>>

    fun removeFavorite(id: String)
}

internal class DefaultPaymentMethodRepository(
    paymentMethodsApiResponse: PaymentMethodsApiResponse,
) : PaymentMethodRepository {

    override val regulars: List<PaymentMethod> = paymentMethodsApiResponse.paymentMethods.orEmpty()

    private val _favorites = MutableStateFlow(paymentMethodsApiResponse.storedPaymentMethods.orEmpty())
    override val favorites: Flow<List<StoredPaymentMethod>> = _favorites.asStateFlow()

    override fun removeFavorite(id: String) {
        // TODO - Implement network call and only remove locally if successful
        _favorites.value = _favorites.value.filterNot { it.id == id }
    }
}
