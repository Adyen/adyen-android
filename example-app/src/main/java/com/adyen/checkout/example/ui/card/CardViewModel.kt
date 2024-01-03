package com.adyen.checkout.example.ui.card

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.AddressInputModel
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.example.service.createPaymentRequest
import com.adyen.checkout.example.service.getPaymentMethodRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@OptIn(FlowPreview::class)
@Suppress("TooManyFunctions")
@HiltViewModel
internal class CardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage,
) : ViewModel(), ComponentCallback<CardComponentState> {

    private val _cardComponentDataFlow = MutableStateFlow<CardComponentData?>(null)
    val cardComponentDataFlow: Flow<CardComponentData> = _cardComponentDataFlow.filterNotNull()

    private val _cardViewState = MutableStateFlow<CardViewState>(CardViewState.Loading)
    val cardViewState: Flow<CardViewState> = _cardViewState

    private val addressLookupQueryFlow = MutableStateFlow<String?>(null)

    private val _events = MutableSharedFlow<CardEvent>()
    val events: Flow<CardEvent> = _events

    init {
        viewModelScope.launch { fetchPaymentMethods() }
        addressLookupQueryFlow
            .filterNotNull()
            .debounce(ADDRESS_LOOKUP_QUERY_DEBOUNCE_DURATION)
            .onEach { query ->
                val options = if (query == "empty") {
                    emptyList()
                } else {
                    ADDRESS_LOOKUP_OPTIONS
                }
                // TODO address lookup populate better data
                _events.emit(CardEvent.AddressLookup(options))
            }
            .launchIn(viewModelScope)
    }

    private suspend fun fetchPaymentMethods() = withContext(Dispatchers.IO) {
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
        viewModelScope.launch {
            addressLookupQueryFlow.emit(query)
        }
    }

    fun onAddressLookupCompleted(lookupAddress: LookupAddress) {
        viewModelScope.launch {
            delay(ADDRESS_LOOKUP_COMPLETION_DELAY)
            if (lookupAddress.id == ADDRESS_LOOKUP_ERROR_ITEM_ID) {
                _events.emit(CardEvent.AddressLookupError("Something went wrong."))
            } else {
                _events.emit(
                    CardEvent.AddressLookupCompleted(
                        ADDRESS_LOOKUP_OPTIONS.first { it.id == lookupAddress.id }
                    )
                )
            }
        }
    }

    // no ops
    override fun onStateChanged(state: CardComponentState) = Unit

    private fun makePayment(data: PaymentComponentData<*>) {
        _cardViewState.value = CardViewState.Loading

        val paymentComponentData = PaymentComponentData.SERIALIZER.serialize(data)

        viewModelScope.launch(Dispatchers.IO) {
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentData,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                merchantAccount = keyValueStorage.getMerchantAccount(),
                redirectUrl = savedStateHandle.get<String>(CardActivity.RETURN_URL_EXTRA)
                    ?: error("Return url should be set"),
                isThreeds2Enabled = keyValueStorage.isThreeds2Enabled(),
                isExecuteThreeD = keyValueStorage.isExecuteThreeD(),
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

    private fun sendPaymentDetails(actionComponentData: ActionComponentData) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
            handlePaymentResponse(paymentsRepository.makeDetailsRequest(json))
        }
    }

    private fun onComponentError(error: ComponentError) {
        viewModelScope.launch { _events.emit(CardEvent.PaymentResult("Failed: ${error.errorMessage}")) }
    }

    companion object {
        private const val ADDRESS_LOOKUP_QUERY_DEBOUNCE_DURATION = 300L
        private const val ADDRESS_LOOKUP_COMPLETION_DELAY = 400L
        private const val ADDRESS_LOOKUP_ERROR_ITEM_ID = "3"
        private val ADDRESS_LOOKUP_OPTIONS = listOf(
            LookupAddress(
                id = "1",
                address = AddressInputModel(
                    country = "NL",
                    postalCode = "1234AB",
                    houseNumberOrName = "1HS",
                    street = "Simon Carmiggeltstraat",
                    stateOrProvince = "Noord-Holland",
                    city = "Amsterdam",
                ),
            ),
            LookupAddress(
                id = "2",
                address = AddressInputModel(
                    country = "TR",
                    postalCode = "12345",
                    houseNumberOrName = "1",
                    street = "1. Sokak",
                    stateOrProvince = "Istanbul",
                    city = "Istanbul",
                ),
            ),
            LookupAddress(
                id = ADDRESS_LOOKUP_ERROR_ITEM_ID,
                address = AddressInputModel(
                    country = "",
                    postalCode = "",
                    houseNumberOrName = "",
                    street = "Error option",
                    stateOrProvince = "",
                    city = "",
                ),
            ),
        )
    }
}
