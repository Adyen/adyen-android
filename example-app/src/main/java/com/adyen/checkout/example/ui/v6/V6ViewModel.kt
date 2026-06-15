/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.BinLookupData
import com.adyen.checkout.card.card
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.AdvancedCheckoutResult
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import com.adyen.checkout.example.ui.compose.ResultState
import com.adyen.checkout.example.ui.compose.UIText
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
internal class V6ViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val checkoutConfigurationProvider: CheckoutConfigurationProvider,
) : ViewModel() {

    private lateinit var checkoutContext: CheckoutContext.Advanced

    var uiState by mutableStateOf<V6UiState>(V6UiState.Loading)

    init {
        viewModelScope.launch {
            fetchPaymentMethods()
        }
    }

    private suspend fun fetchPaymentMethods() {
        val paymentMethods = paymentsRepository.getPaymentMethods(
            getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getOldAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            ),
        )

        if (paymentMethods == null) {
            val message = "Payment methods are null."
            Log.d(TAG, message)
            uiState = V6UiState.Error(UIText.String(message))
            return
        }

        val result = Checkout.setup(
            paymentMethods = paymentMethods,
            configuration = checkoutConfigurationProvider.checkoutConfig,
        )

        uiState = when (result) {
            is Checkout.Result.Error -> V6UiState.Error(UIText.String(result.error.message.orEmpty()))
            is Checkout.Result.Success -> {
                checkoutContext = result.checkoutContext
                val paymentMethods = checkoutContext.getPaymentMethods()
                V6UiState.Component(
                    paymentMethods = paymentMethods,
                    storedPaymentMethods = checkoutContext.getStoredPaymentMethods(),
                    selectedPaymentMethod = paymentMethods.first(),
                    checkoutController = createCheckoutController(
                        paymentMethod = paymentMethods.first(),
                        checkoutContext = result.checkoutContext,
                    ),
                )
            }
        }
    }

    private fun onBinValue(binValue: String) {
        Log.d(TAG, "Bin value received: $binValue")
    }

    private fun onBinLookup(binLookupData: BinLookupData) {
        Log.d(TAG, "Bin Lookup Data received: $binLookupData")
    }

    private suspend fun onSubmit(data: PaymentComponentData<*>): SubmitResult {
        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)
        val paymentRequest = createPaymentRequest(
            paymentComponentData = paymentComponentData,
            shopperReference = keyValueStorage.getShopperReference(),
            amount = keyValueStorage.getOldAmount(),
            countryCode = keyValueStorage.getCountry(),
            merchantAccount = keyValueStorage.getMerchantAccount(),
            redirectUrl = savedStateHandle.get<String>(V6Activity.RETURN_URL_EXTRA)
                ?: error("Return url should be set"),
            threeDSMode = keyValueStorage.getThreeDSMode(),
            shopperEmail = keyValueStorage.getShopperEmail(),
        )
        val response = paymentsRepository.makePaymentsRequest(paymentRequest)
        return handleSubmitResponse(response)
    }

    private suspend fun onAdditionalDetails(data: ActionComponentData): AdditionalDetailsResult {
        val request = ActionComponentData.SERIALIZER.serialize(data)
        val response = paymentsRepository.makeDetailsRequest(request)
        return handleAdditionalDetailsResponse(response)
    }

    private fun handleSubmitResponse(json: JSONObject?): SubmitResult {
        return when {
            json == null -> {
                Log.e(TAG, "Empty payments response — terminating with Completion(\"Error\").")
                uiState = V6UiState.Final(ResultState.get(RESULT_CODE_ERROR))
                SubmitResult.Completion(RESULT_CODE_ERROR)
            }

            json.has("action") -> {
                val action = Action.SERIALIZER.deserialize(json.getJSONObject("action"))
                SubmitResult.Action(action)
            }

            else -> {
                val resultCode = json.optString("resultCode")
                SubmitResult.Completion(resultCode)
            }
        }
    }

    private fun handleAdditionalDetailsResponse(json: JSONObject?): AdditionalDetailsResult {
        if (json == null) {
            Log.e(TAG, "Empty payments/details response — terminating with Completion(\"Error\").")
            uiState = V6UiState.Final(ResultState.get(RESULT_CODE_ERROR))
            return AdditionalDetailsResult.Completion(RESULT_CODE_ERROR)
        }
        val resultCode = json.optString("resultCode")
        return AdditionalDetailsResult.Completion(resultCode)
    }

    private fun onFailure(error: CheckoutError) {
        uiState = V6UiState.Error(UIText.String(error.message.orEmpty()))
    }

    private fun onComplete(result: AdvancedCheckoutResult) {
        uiState = V6UiState.Final(ResultState.get(result.resultCode.value))
    }

    @Suppress("unused")
    fun handleIntent(intent: Intent) {
        // TODO - Check if the controller should handle the intent or if we can do this inside a component
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethodResponse) {
        val newState = (uiState as? V6UiState.Component)?.copy(
            selectedPaymentMethod = paymentMethod,
            checkoutController = createCheckoutController(
                paymentMethod = paymentMethod,
                checkoutContext = checkoutContext,
            ),
        )

        if (newState != null) {
            uiState = newState
        }
    }

    private fun createCheckoutController(
        paymentMethod: PaymentMethodResponse,
        checkoutContext: CheckoutContext.Advanced,
    ): CheckoutController {
        val target = if (paymentMethod is StoredPaymentMethod) {
            CheckoutTarget.StoredPaymentMethod(paymentMethod.id)
        } else {
            CheckoutTarget.PaymentMethod(paymentMethod.type)
        }

        return CheckoutController(
            target = target,
            context = checkoutContext,
            callbacks = AdvancedCheckoutCallbacks(
                onSubmit = ::onSubmit,
                onAdditionalDetails = ::onAdditionalDetails,
                onFailure = ::onFailure,
                onComplete = ::onComplete,
            ) {
                card(
                    onBinValue = ::onBinValue,
                    onBinLookup = ::onBinLookup,
                )
            },
            coroutineScope = viewModelScope,
        )
    }

    companion object {
        private val TAG = getLogTag()
        private const val RESULT_CODE_ERROR = "Error"
    }
}
