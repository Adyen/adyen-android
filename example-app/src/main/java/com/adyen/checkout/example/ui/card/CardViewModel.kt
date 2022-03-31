package com.adyen.checkout.example.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.model.getStringOrNull
import com.adyen.checkout.example.data.api.model.AdditionalData
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
internal class CardViewModel @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage
) : ViewModel() {

    private val _cardViewState = MutableStateFlow<CardViewState>(CardViewState.Loading)
    val cardViewState: Flow<CardViewState> = _cardViewState

    private val _paymentResult = MutableSharedFlow<String>()
    val paymentResult: Flow<String> = _paymentResult

    private val _additionalAction = MutableSharedFlow<CardAction>()
    val additionalAction: Flow<CardAction> = _additionalAction

    private var cardComponentState: CardComponentState? = null

    init {
        viewModelScope.launch { _cardViewState.emit(fetchPaymentMethods()) }
    }

    private suspend fun fetchPaymentMethods(): CardViewState = withContext(Dispatchers.IO) {
        val paymentMethod = paymentsRepository.getPaymentMethods(getPaymentMethodRequest(keyValueStorage))
            ?.paymentMethods
            ?.firstOrNull { it.type == "scheme" }

        if (paymentMethod == null) CardViewState.Error
        else CardViewState.ShowComponent(paymentMethod)
    }

    fun onCardComponentState(state: CardComponentState?) {
        cardComponentState = state
    }

    fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _paymentResult.emit("Failed: ${error.errorMessage}") }
    }

    fun onPayClick() {
        val state = cardComponentState
        when {
            state == null -> _cardViewState.tryEmit(CardViewState.Error)
            state.isValid -> makePayment(state.data)
            else -> _cardViewState.tryEmit(CardViewState.Invalid)
        }
    }

    private fun makePayment(data: PaymentComponentData<*>) {
        _cardViewState.value = CardViewState.Loading

        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(Dispatchers.IO) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData,
                keyValueStorage.getShopperReference(),
                keyValueStorage.getAmount(),
                keyValueStorage.getCountry(),
                keyValueStorage.getMerchantAccount(),
                // Should be set correctly for additional actions
                "",
                AdditionalData(
                    allow3DS2 = keyValueStorage.isThreeds2Enable().toString(),
                    executeThreeD = keyValueStorage.isExecuteThreeD().toString()
                )
            )

            val requestBody = paymentRequest.toString().toRequestBody("application/json".toMediaType())
            handlePaymentResponse(paymentsRepository.paymentsRequestAsync(requestBody))
        }
    }

    private suspend fun handlePaymentResponse(responseBody: ResponseBody?) {
        responseBody?.let {
            @Suppress("BlockingMethodInNonBlockingContext")
            val json = JSONObject(it.string())

            when {
                json.has("action") -> {
                    val action = Action.SERIALIZER.deserialize(json.getJSONObject("action"))
                    handleAction(action)
                }
                else -> _paymentResult.emit("Success: ${json.getStringOrNull("resultCode")}")
            }
        } ?: _paymentResult.emit("Failed")
    }

    private suspend fun handleAction(action: Action) {
        val cardAction = when (action.type) {
            "redirect" -> CardAction.Redirect(action)
            "threeDS2" -> CardAction.ThreeDS2(action)
            else -> CardAction.Unsupported
        }

        _additionalAction.emit(cardAction)
    }

    fun onActionComponentData(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            handlePaymentResponse(paymentsRepository.detailsRequestAsync(requestBody))
        }
    }
}
