/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.exception.CheckoutError
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.getSessionRequest
import com.adyen.checkout.example.service.getSettingsInstallmentOptionsMode
import com.adyen.checkout.example.ui.compose.UIText
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import com.adyen.checkout.threeds2.internal.ui.mapToUiCustomization
import com.adyen.checkout.threeds2.threeDS2
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class V6SessionsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val uiThemeRepository: UIThemeRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel() {

    // TODO - Replace with checkoutConfigurationProvider once it's updated COSDK-563
    private val configuration = CheckoutConfiguration(
        Environment.TEST,
        BuildConfig.CLIENT_KEY,
        configurationBlock = {
            threeDS2 {
                val checkoutTheme = uiThemeRepository.getCheckoutTheme(context)
                uiCustomization = checkoutTheme.mapToUiCustomization()
            }
        },
    )

    val checkoutController = CheckoutController()

    var uiState by mutableStateOf<V6UiState>(V6UiState.Loading)

    init {
        viewModelScope.launch {
            createSession()
        }
    }

    private suspend fun createSession() {
        val sessionResponse = paymentsRepository.createSession(
            getSessionRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                redirectUrl = savedStateHandle.get<String>(V6SessionsActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                shopperEmail = keyValueStorage.getShopperEmail(),
                installmentOptions = getSettingsInstallmentOptionsMode(keyValueStorage.getInstallmentOptionsMode()),
                showInstallmentAmount = keyValueStorage.isInstallmentAmountShown(),
                showRemovePaymentMethodButton = keyValueStorage.isRemoveStoredPaymentMethodEnabled(),
            ),
        ) ?: return

        val result = Checkout.setup(
            sessionResponse = sessionResponse,
            configuration = configuration,
        )

        uiState = when (result) {
            is Checkout.Result.Error -> V6UiState.Error(UIText.String(result.errorReason))
            is Checkout.Result.Success -> V6UiState.Component(
                checkoutContext = result.checkoutContext,
                checkoutCallbacks = CheckoutCallbacks(
                    onError = ::onError,
                ),
                paymentMethods = result.checkoutContext.getPaymentMethods(),
            )
        }
    }

    private fun onError(error: CheckoutError) {
        Log.d(TAG, "onError: ${error.message}")
    }

    fun handleIntent(intent: Intent) {
        checkoutController.handleIntent(intent)
    }

    companion object {
        private val TAG = getLogTag()
    }
}
