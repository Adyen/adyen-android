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

    private var countryOptions: List<AddressListItem> = emptyList()
    private var stateOptions: List<AddressListItem> = emptyList()

    override fun initialize(coroutineScope: CoroutineScope) {
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
            }
            .launchIn(coroutineScope)
    }

    override fun onAddressQueryChanged(query: String) {
        addressLookupEventChannel.trySend(AddressLookupEvent.Query(query))
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

    override fun updateAddressLookupOptions(options: List<LookupAddress>) {
        addressLookupEventChannel.trySend(AddressLookupEvent.SearchResult(options))
    }

    override fun setAddressLookupResult(lookupAddress: LookupAddress) {
        addressLookupEventChannel.trySend(AddressLookupEvent.OptionSelected(lookupAddress, false))
    }

    override fun setAddressLookupCallback(addressLookupCallback: AddressLookupCallback) {
        this.addressLookupCallback = addressLookupCallback
    }

    private fun makeAddressLookupState(
        event: AddressLookupEvent,
        addressLookupOptions: List<LookupAddress>,
    ): AddressLookupState {
        return when (event) {
            is AddressLookupEvent.Query -> {
                addressLookupInputData.query = event.query
                AddressLookupState.Loading
            }

            AddressLookupEvent.ClearQuery -> {
                AddressLookupState.Initial
            }

            AddressLookupEvent.Manual -> {
                if (currentAddressLookupState is AddressLookupState.Initial ||
                    currentAddressLookupState is AddressLookupState.Error
                ) {
                    AddressLookupState.Form(null)
                } else {
                    currentAddressLookupState
                }
            }

            is AddressLookupEvent.SearchResult -> {
                if (currentAddressLookupState is AddressLookupState.Loading) {
                    if (event.addressLookupOptions.isEmpty()) {
                        AddressLookupState.Error
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

            is AddressLookupEvent.OptionSelected -> {
                if (currentAddressLookupState is AddressLookupState.SearchResult) {
                    if (event.loading) {
                        AddressLookupState.SearchResult(
                            addressLookupInputData.query,
                            addressLookupOptions.map {
                                LookupOption(
                                    lookupAddress = it,
                                    isLoading = it == event.lookupAddress,
                                )
                            },
                        )
                    } else {
                        AddressLookupState.Form(event.lookupAddress.address)
                    }
                } else {
                    currentAddressLookupState
                }
            }
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
