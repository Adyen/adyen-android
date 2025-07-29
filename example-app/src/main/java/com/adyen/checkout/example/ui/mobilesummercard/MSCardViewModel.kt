/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/7/2025.
 */

package com.adyen.checkout.example.ui.mobilesummercard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.IODispatcher
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getPaymentMethodRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MSCardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage
) : ViewModel(), ComponentCallback<CardComponentState> {

    override fun onSubmit(state: CardComponentState) {
        TODO("Not yet implemented")
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        TODO("Not yet implemented")
    }

    override fun onError(componentError: ComponentError) {
        TODO("Not yet implemented")
    }

    suspend fun fetchPaymentMethods() = withContext(IODispatcher) {
        return@withContext paymentsRepository.getPaymentMethods(
            getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            ),
        )
    }

    fun getCardPaymentMethod(paymentMethodResponse: PaymentMethodsApiResponse?) = paymentMethodResponse
        ?.paymentMethods
        ?.firstOrNull { CardComponent.PROVIDER.isPaymentMethodSupported(it) }
}
