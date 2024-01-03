/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/12/2023.
 */

package com.adyen.checkout.ui.core.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.AddressInputModel
import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupEvent
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupInputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupState
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.view.LookupOption
import com.adyen.checkout.ui.core.internal.util.AddressFormUtils
import com.adyen.checkout.ui.core.internal.util.AddressValidationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultAddressLookupDelegate :
    AddressLookupDelegate,
    AddressDelegate {

    override val addressDelegate: AddressDelegate = this

    private var addressLookupCallback: AddressLookupCallback? = null

    private val addressLookupInputData = AddressLookupInputData()

    private val _addressLookupStateFlow = MutableStateFlow<AddressLookupState>(AddressLookupState.Initial)
    override val addressLookupStateFlow: Flow<AddressLookupState> = _addressLookupStateFlow

    private var currentAddressLookupOptions: List<LookupAddress> = emptyList()

    private val currentAddressLookupState
        get() = _addressLookupStateFlow.value

    override val addressLookupEventChannel = bufferedChannel<AddressLookupEvent>()
    private val addressLookupEventFlow: Flow<AddressLookupEvent> = addressLookupEventChannel.receiveAsFlow()

    override val addressOutputData: AddressOutputData
        get() = _addressOutputDataFlow.value

    private val _addressOutputDataFlow = MutableStateFlow(
        AddressValidationUtils.validateAddressInput(
            addressLookupInputData.selectedAddress,
            AddressFormUIState.LOOKUP,
            emptyList(),
            emptyList(),
            false,
        ),
    )
    override val addressOutputDataFlow: Flow<AddressOutputData> = _addressOutputDataFlow

    private val submitAddressChannel = bufferedChannel<AddressInputModel>()
    override val addressLookupSubmitFlow: Flow<AddressInputModel> = submitAddressChannel.receiveAsFlow()

    private val addressLookupErrorPopupChannel = bufferedChannel<String?>()
    override val addressLookupErrorPopupFlow: Flow<String?> = addressLookupErrorPopupChannel.receiveAsFlow()

    private var countryOptions: List<AddressListItem> = emptyList()
    private var stateOptions: List<AddressListItem> = emptyList()

    override fun initialize(coroutineScope: CoroutineScope, addressInputModel: AddressInputModel) {
        addressLookupEventFlow
            .onEach { addressLookupEvent ->
                val addressLookupOptions =
                    if (addressLookupEvent is AddressLookupEvent.SearchResult) {
                        addressLookupEvent.addressLookupOptions
                    } else {
                        currentAddressLookupOptions
                    }
                _addressLookupStateFlow.emit(
                    makeAddressLookupState(
                        event = addressLookupEvent,
                        addressLookupOptions = addressLookupOptions,
                    ),
                )

                if (addressLookupEvent is AddressLookupEvent.ErrorResult) {
                    addressLookupErrorPopupChannel.trySend(addressLookupEvent.message)
                }
            }
            .launchIn(coroutineScope)

        addressLookupEventChannel.trySend(AddressLookupEvent.Initialize(addressInputModel))
    }

    override fun onAddressQueryChanged(query: String) {
        if (query.isEmpty()) {
            addressLookupEventChannel.trySend(AddressLookupEvent.ClearQuery)
        } else {
            addressLookupEventChannel.trySend(AddressLookupEvent.Query(query))
        }
        addressLookupCallback?.onQueryChanged(query)
    }

    override fun onAddressLookupCompleted(lookupAddress: LookupAddress): Boolean {
        val isLoading = addressLookupCallback?.onLookupCompleted(lookupAddress) ?: false
        addressLookupEventChannel.trySend(
            AddressLookupEvent.OptionSelected(
                lookupAddress,
                isLoading,
            ),
        )
        return isLoading
    }

    override fun onManualEntryModeSelected() {
        addressLookupEventChannel.trySend(AddressLookupEvent.Manual)
    }

    override fun submitAddress() {
        if (addressDelegate.addressOutputData.isValid) {
            submitAddressChannel.trySend(addressLookupInputData.selectedAddress)
        } else {
            addressLookupEventChannel.trySend(AddressLookupEvent.InvalidUI)
        }
    }

    override fun updateAddressLookupOptions(options: List<LookupAddress>) {
        addressLookupEventChannel.trySend(AddressLookupEvent.SearchResult(options))
    }

    override fun setAddressLookupResult(addressLookupResult: AddressLookupResult) {
        when (addressLookupResult) {
            is AddressLookupResult.Error -> {
                addressLookupEventChannel.trySend(
                    AddressLookupEvent.ErrorResult(addressLookupResult.message),
                )
            }

            is AddressLookupResult.Completed -> {
                addressLookupEventChannel.trySend(
                    AddressLookupEvent.OptionSelected(
                        addressLookupResult.lookupAddress,
                        false,
                    ),
                )
            }
        }
    }

    override fun setAddressLookupCallback(addressLookupCallback: AddressLookupCallback) {
        this.addressLookupCallback = addressLookupCallback
    }

    private fun makeAddressLookupState(
        event: AddressLookupEvent,
        addressLookupOptions: List<LookupAddress>,
    ): AddressLookupState {
        return when (event) {
            is AddressLookupEvent.Initialize -> handleInitializeEvent(event)
            is AddressLookupEvent.Query -> handleQueryEvent(event)
            AddressLookupEvent.ClearQuery -> handleClearQueryEvent()
            AddressLookupEvent.Manual -> handleManualEvent()
            is AddressLookupEvent.SearchResult -> handleSearchResultEvent(event)
            is AddressLookupEvent.OptionSelected -> handleOptionSelectedEvent(event, addressLookupOptions)
            is AddressLookupEvent.InvalidUI -> handleInvalidUIEvent()
            is AddressLookupEvent.ErrorResult -> handleErrorEvent()
        }
    }

    private fun handleInitializeEvent(event: AddressLookupEvent.Initialize): AddressLookupState {
        return if (event.address.isEmpty) {
            AddressLookupState.Initial
        } else {
            AddressLookupState.Form(event.address)
        }
    }

    private fun handleQueryEvent(event: AddressLookupEvent.Query): AddressLookupState {
        addressLookupInputData.query = event.query
        return AddressLookupState.Loading
    }

    private fun handleClearQueryEvent(): AddressLookupState {
        return if (!addressLookupInputData.selectedAddress.isEmpty) {
            AddressLookupState.Form(addressLookupInputData.selectedAddress)
        } else {
            AddressLookupState.Initial
        }
    }

    private fun handleManualEvent(): AddressLookupState {
        return if (currentAddressLookupState is AddressLookupState.Initial ||
            currentAddressLookupState is AddressLookupState.Error ||
            currentAddressLookupState is AddressLookupState.SearchResult
        ) {
            AddressLookupState.Form(null)
        } else {
            currentAddressLookupState
        }
    }

    private fun handleSearchResultEvent(event: AddressLookupEvent.SearchResult): AddressLookupState {
        return if (currentAddressLookupState is AddressLookupState.Loading) {
            if (event.addressLookupOptions.isEmpty()) {
                AddressLookupState.Error(addressLookupInputData.query)
            } else {
                currentAddressLookupOptions = event.addressLookupOptions
                AddressLookupState.SearchResult(
                    addressLookupInputData.query,
                    event.addressLookupOptions.map {
                        LookupOption(lookupAddress = it, isLoading = false)
                    },
                )
            }
        } else {
            currentAddressLookupState
        }
    }

    private fun handleOptionSelectedEvent(
        event: AddressLookupEvent.OptionSelected,
        addressLookupOptions: List<LookupAddress>
    ): AddressLookupState {
        return if (currentAddressLookupState is AddressLookupState.SearchResult) {
            if (event.loading) {
                AddressLookupState.SearchResult(
                    addressLookupInputData.query,
                    addressLookupOptions.map {
                        LookupOption(lookupAddress = it, isLoading = it == event.lookupAddress)
                    },
                )
            } else {
                AddressLookupState.Form(event.lookupAddress.address)
            }
        } else {
            currentAddressLookupState
        }
    }

    private fun handleInvalidUIEvent(): AddressLookupState {
        return if (currentAddressLookupState is AddressLookupState.Form ||
            currentAddressLookupState is AddressLookupState.SearchResult
        ) {
            AddressLookupState.InvalidUI
        } else {
            currentAddressLookupState
        }
    }

    private fun handleErrorEvent(): AddressLookupState {
        return if (currentAddressLookupState is AddressLookupState.SearchResult) {
            AddressLookupState.SearchResult(
                (currentAddressLookupState as? AddressLookupState.SearchResult)?.query.orEmpty(),
                (currentAddressLookupState as? AddressLookupState.SearchResult)
                    ?.options
                    ?.map { it.copy(isLoading = false) }
                    .orEmpty(),
            )
        } else {
            currentAddressLookupState
        }
    }

    override fun updateAddressInputData(update: AddressInputModel.() -> Unit) {
        addressLookupInputData.selectedAddress.update()
        countryOptions = AddressFormUtils.markAddressListItemSelected(
            list = countryOptions,
            code = addressLookupInputData.selectedAddress.country,
        )
        stateOptions = AddressFormUtils.markAddressListItemSelected(
            list = stateOptions,
            code = addressLookupInputData.selectedAddress.stateOrProvince,
        )
        _addressOutputDataFlow.tryEmit(
            AddressValidationUtils.validateAddressInput(
                addressInputModel = addressLookupInputData.selectedAddress,
                addressFormUIState = AddressFormUIState.LOOKUP,
                countryOptions = countryOptions,
                stateOptions = stateOptions,
                isOptional = false,
            ),
        )
    }

    override fun updateCountryOptions(countryOptions: List<AddressListItem>) {
        this.countryOptions = countryOptions
    }

    override fun updateStateOptions(stateOptions: List<AddressListItem>) {
        this.stateOptions = stateOptions
    }
}
