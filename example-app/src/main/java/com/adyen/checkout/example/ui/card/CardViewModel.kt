package com.adyen.checkout.example.ui.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.AddressLookupCompletionResult
import com.adyen.checkout.example.repositories.AddressLookupRepository
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class CardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
    private val addressLookupRepository: AddressLookupRepository
) : ViewModel(), ComponentCallback<CardComponentState> {

    private val _cardComponentDataFlow = MutableStateFlow<CardComponentData?>(null)
    val cardComponentDataFlow: Flow<CardComponentData> = _cardComponentDataFlow.filterNotNull()

    private val _cardViewState = MutableStateFlow<CardViewState>(CardViewState.Loading)
    val cardViewState: Flow<CardViewState> = _cardViewState

    private val _events = MutableSharedFlow<CardEvent>()
    val events: Flow<CardEvent> = _events

    init {
        viewModelScope.launch { fetchPaymentMethods() }
        addressLookupRepository.addressLookupOptionsFlow
            .onEach { options ->
                _events.emit(CardEvent.AddressLookup(options))
            }.launchIn(viewModelScope)
    }

    @Suppress("RestrictedApi")
    private suspend fun fetchPaymentMethods() = withContext(DispatcherProvider.IO) {
        val paymentMethodResponse = paymentsRepository.getPaymentMethods(
            getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
            ),
        )

        val cardPaymentMethod = paymentMethodResponse
            ?.paymentMethods
            ?.firstOrNull { CardComponent.PROVIDER.isPaymentMethodSupported(it) }

        if (cardPaymentMethod == null) {
            _cardViewState.emit(CardViewState.Error)
        } else {
            _cardComponentDataFlow.emit(
                CardComponentData(
                    paymentMethod = cardPaymentMethod,
                    callback = this@CardViewModel,
                ),
            )
            _cardViewState.emit(CardViewState.ShowComponent)
        }
    }

    override fun onSubmit(state: CardComponentState) {
        makePayment(state.data)
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        sendPaymentDetails(actionComponentData)
    }

    override fun onError(componentError: ComponentError) {
        onComponentError(componentError)
    }

    fun onAddressLookupQueryChanged(query: String) {
        addressLookupRepository.onQuery(query)
    }

    fun onAddressLookupCompletion(lookupAddress: LookupAddress) {
        viewModelScope.launch {
            when (val lookupResult = addressLookupRepository.onAddressLookupCompleted(lookupAddress)) {
                is AddressLookupCompletionResult.Address -> _events.emit(
                    CardEvent.AddressLookupCompleted(
                        lookupResult.lookupAddress,
                    ),
                )

                is AddressLookupCompletionResult.Error -> _events.emit(
                    CardEvent.AddressLookupError(
                        lookupResult.message,
                    ),
                )
            }
        }
    }

    // no ops
    override fun onStateChanged(state: CardComponentState) = Unit

    private fun makePayment(data: PaymentComponentData<*>) {
        _cardViewState.value = CardViewState.Loading

        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        @Suppress("RestrictedApi")
        viewModelScope.launch(DispatcherProvider.IO) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(CardActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                threeDSMode = keyValueStorage.getThreeDSMode(),
                shopperEmail = keyValueStorage.getShopperEmail(),
            )

            handlePaymentResponse(paymentsRepository.makePaymentsRequest(paymentRequest))
        }
    }

    private suspend fun handlePaymentResponse(json: JSONObject?) {
        json?.let {
            when {
                json.has("action") -> {
                    val action = Action.SERIALIZER.deserialize(json.getJSONObject("action"))
                    handleAction(action)
                }

                else -> _events.emit(CardEvent.PaymentResult("Finished: ${json.optString("resultCode")}"))
            }
        } ?: _events.emit(CardEvent.PaymentResult("Failed"))
    }

    private suspend fun handleAction(action: Action) {
        _events.emit(CardEvent.AdditionalAction(action))
    }

    @Suppress("RestrictedApi")
    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(DispatcherProvider.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(CardEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }
}
