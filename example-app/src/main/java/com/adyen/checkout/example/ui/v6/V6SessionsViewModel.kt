/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.example.ui.v6

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.AdyenCheckout
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class V6SessionsViewModel @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel() {

    // TODO - Replace with checkoutConfigurationProvider once it's updated COSDK-563
    private val configuration = CheckoutConfiguration(
        Environment.TEST,
        BuildConfig.CLIENT_KEY,
    )

    var adyenCheckout by mutableStateOf<AdyenCheckout?>(null)

    init {
        viewModelScope.launch {
            createSession()
        }
    }

    private suspend fun createSession() {
        val session = paymentsRepository.createSession(
            getSessionRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                // TODO - Replace with correct URL once redirects are implemented
                redirectUrl = "test",
                shopperEmail = keyValueStorage.getShopperEmail(),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode()),
                showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
                showRemovePaymentMethodButton = keyValueStorage.isRemoveStoredPaymentMethodEnabled(),
            ),
        ) ?: return

        val result = AdyenCheckout.initialize(
            sessionModel = session,
            checkoutConfiguration = configuration,
            checkoutCallbacks = null,
        )

        adyenCheckout = when (result) {
            is AdyenCheckout.Result.Error -> null
            is AdyenCheckout.Result.Success -> result.adyenCheckout
        }
    }
}
