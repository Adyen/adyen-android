/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/1/2024.
 */

package com.adyen.checkout.ui.core.internal.ui

import com.adyen.checkout.components.core.AddressData
import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.mapToAddressInputModel
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.data.api.TestAddressRepository
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupState
import com.adyen.checkout.ui.core.internal.ui.view.LookupOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultAddressLookupDelegateTest {

    lateinit var defaultAddressLookupDelegate: DefaultAddressLookupDelegate

    lateinit var addressLookupCallback: AddressLookupCallback

    @BeforeEach
    fun setup() {
        AdyenLogger.setLogLevel(Logger.NONE)

        addressLookupCallback = Mockito.mock(AddressLookupCallback::class.java)

        defaultAddressLookupDelegate = DefaultAddressLookupDelegate(
            TestAddressRepository(),
            Locale.US,
        )
    }

    @Test
    fun `when initialized without an address, lookup state should be Initial`() = runTest {
        defaultAddressLookupDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()), AddressInputModel())
        assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Initial)
    }

    @Test
    fun `when initialize called without an address and state is any and query is empty, state should be Initial `() =
        runTest {
            defaultAddressLookupDelegate.initialize(
                CoroutineScope(UnconfinedTestDispatcher()),
                getEmptyMockAddress(),
            )
            // wt used as an example here
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(
                AddressLookupState.SearchResult(
                    "query",
                    getMockList(),
                ),
            )
            defaultAddressLookupDelegate.onAddressQueryChanged("")
            val state = defaultAddressLookupDelegate.addressLookupStateFlow.first()
            assert(state is AddressLookupState.Initial)
        }

    @Test
    fun `when initialize called with an address, lookup state should be form `() = runTest {
        defaultAddressLookupDelegate.initialize(
            CoroutineScope(UnconfinedTestDispatcher()),
            getValidMockAddress(),
        )
        val state = defaultAddressLookupDelegate.addressLookupStateFlow.first()
        assert(state is AddressLookupState.Form)
        assertEquals(getValidMockAddress(), (state as AddressLookupState.Form).selectedAddress)
    }

    @Test
    fun `when initialize called with an address and state is any and query is empty, state should be Form `() =
        runTest {
            defaultAddressLookupDelegate.initialize(
                CoroutineScope(UnconfinedTestDispatcher()),
                getValidMockAddress(),
            )
            // SearchResult used as an example here
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(
                AddressLookupState.SearchResult(
                    "query",
                    getMockList(),
                ),
            )
            defaultAddressLookupDelegate.onAddressQueryChanged("")
            val state = defaultAddressLookupDelegate.addressLookupStateFlow.first()
            assert(state is AddressLookupState.Form)
            assertEquals(getValidMockAddress(), (state as AddressLookupState.Form).selectedAddress)
        }

    @Nested
    @DisplayName("when address lookup state is Initial")
    inner class Initial {

        @BeforeEach
        fun setup() {
            defaultAddressLookupDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()), AddressInputModel())
        }

        @Test
        fun `and a query has been inputted, state should be Loading`() = runTest {
            defaultAddressLookupDelegate.onAddressQueryChanged("test")
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Loading)
        }

        @Test
        fun `and manual entry mode is selected, state should be Form`() = runTest {
            defaultAddressLookupDelegate.onManualEntryModeSelected()
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Form)
        }

        @Test
        fun `and search result event emitted, state should remain the same`() = runTest {
            defaultAddressLookupDelegate.updateAddressLookupOptions(emptyList())
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Initial)
        }
    }

    @Nested
    @DisplayName("when address lookup state is Loading")
    inner class Loading {
        @BeforeEach
        fun setup() {
            defaultAddressLookupDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()), AddressInputModel())
        }

        @Test
        fun `and a result is passed, state should be Loading`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.Loading)
            val lookupOptions = listOf(LookupAddress("id", makeAddressData(postalCode = "1234AB")))
            defaultAddressLookupDelegate.updateAddressLookupOptions(lookupOptions)
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.SearchResult)
        }

        @Test
        fun `and an empty result is passed, state should be Loading`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.Loading)
            defaultAddressLookupDelegate.updateAddressLookupOptions(emptyList())
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Error)
        }
    }

    @Nested
    @DisplayName("when address lookup state is Form")
    inner class Form {
        @BeforeEach
        fun setup() {
            defaultAddressLookupDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()), AddressInputModel())
        }

        @Test
        fun `and a query is inputted, state should be Loading`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.Form(null))
            defaultAddressLookupDelegate.onAddressQueryChanged("query")
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Loading)
        }

        @Test
        fun `and manual mode event has been triggered, state should remain the same`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.Form(null))
            defaultAddressLookupDelegate.onManualEntryModeSelected()
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Form)
        }
    }

    @Nested
    @DisplayName("when address lookup state is SearchResult")
    inner class SearchResult {
        @BeforeEach
        fun setup() {
            defaultAddressLookupDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()), AddressInputModel())
        }

        @Test
        fun `and a query is inputted, state should be Loading`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(
                AddressLookupState.SearchResult("query", getMockList()),
            )
            defaultAddressLookupDelegate.onAddressQueryChanged("query")
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Loading)
        }

        @Test
        fun `and an option is selected and no completion call has to be made, state should be Form`() = runTest {
            whenever(addressLookupCallback.onLookupCompletion(getMockList()[0].lookupAddress)).thenReturn(
                false,
            )
            defaultAddressLookupDelegate.setAddressLookupCallback(addressLookupCallback)

            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(
                AddressLookupState.SearchResult("query", getMockList()),
            )
            defaultAddressLookupDelegate.onAddressLookupCompletion(getMockList()[0].lookupAddress)
            val state = defaultAddressLookupDelegate.addressLookupStateFlow.first()
            assert(state is AddressLookupState.Form)
            assertEquals(
                /* expected = */
                getMockList()[0].lookupAddress.address.mapToAddressInputModel(),
                /* actual = */
                (state as AddressLookupState.Form).selectedAddress,
            )
        }

        @Test
        fun `and an option is selected and a completion call has to be made, state should be SearchResult with the item loading`() =
            runTest {
                whenever(addressLookupCallback.onLookupCompletion(getMockList()[0].lookupAddress)).thenReturn(
                    true,
                )
                defaultAddressLookupDelegate.setAddressLookupCallback(addressLookupCallback)

                defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(
                    AddressLookupState.SearchResult("query", getMockList()),
                )
                defaultAddressLookupDelegate.onAddressLookupCompletion(getMockList()[0].lookupAddress)
                val state = defaultAddressLookupDelegate.addressLookupStateFlow.first()
                assert(state is AddressLookupState.SearchResult)
                assert((state as AddressLookupState.SearchResult).options.first().isLoading)
            }

        @Test
        fun `and an option is selected and a completion call has been made, state should be Form`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(
                AddressLookupState.SearchResult("query", getMockList(true)),
            )
            defaultAddressLookupDelegate.setAddressLookupResult(
                AddressLookupResult.Completed(getMockList()[0].lookupAddress),
            )
            val state = defaultAddressLookupDelegate.addressLookupStateFlow.first()
            assert(state is AddressLookupState.Form)
            assertEquals(
                /* expected = */
                getMockList()[0].lookupAddress.address.mapToAddressInputModel(),
                /* actual = */
                (state as AddressLookupState.Form).selectedAddress,
            )
        }

        @Test
        fun `and an option is selected and a completion call has resulted in error, state should be SearchResult`() =
            runTest {
                defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(
                    AddressLookupState.SearchResult("query", getMockList(true)),
                )
                defaultAddressLookupDelegate.setAddressLookupResult(
                    AddressLookupResult.Error(),
                )
                val state = defaultAddressLookupDelegate.addressLookupStateFlow.first()
                assert(state is AddressLookupState.SearchResult)

                val expectedList = getMockList(false)
                assertEquals(
                    /* expected = */
                    false,
                    /* actual = */
                    (state as AddressLookupState.SearchResult).options[0].isLoading,
                )
                assertEquals(expectedList, state.options)
            }
    }

    @Nested
    @DisplayName("when address lookup state is Error")
    inner class Error {
        @BeforeEach
        fun setup() {
            defaultAddressLookupDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()), AddressInputModel())
        }

        @Test
        fun `and a query is inputted, state should be Loading`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.Error("query"))
            defaultAddressLookupDelegate.onAddressQueryChanged("query")
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Loading)
        }

        @Test
        fun `and manual entry mode is selected, state should be Form`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.Error("query"))
            defaultAddressLookupDelegate.onManualEntryModeSelected()
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Form)
        }

        @Test
        fun `and an option selected event is triggered, state should remain the same`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.Error("query"))
            defaultAddressLookupDelegate.setAddressLookupResult(
                AddressLookupResult.Completed(
                    LookupAddress(
                        "id",
                        makeAddressData(),
                    ),
                ),
            )
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Error)
        }
    }

    @Nested
    @DisplayName("when address lookup state is InvalidUI ")
    inner class InvalidUI {
        @BeforeEach
        fun setup() {
            defaultAddressLookupDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()), AddressInputModel())
        }

        @Test
        fun `and a query is inputted, state should be Loading`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.InvalidUI)
            defaultAddressLookupDelegate.onAddressQueryChanged("query")
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.Loading)
        }

        @Test
        fun `and an invalidUI event has been emitted, state should be the same`() = runTest {
            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(AddressLookupState.InvalidUI)
            // make sure output data is invalid
            defaultAddressLookupDelegate.updateAddressInputData {
                this.resetAll()
            }
            defaultAddressLookupDelegate.submitAddress()
            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.InvalidUI)
        }
    }

    @Nested
    @DisplayName("submit is called")
    inner class Submit {
        @BeforeEach
        fun setup() {
            defaultAddressLookupDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()), AddressInputModel())
        }

        @Test
        fun `and address is valid, submit address flow must emit an event`() = runTest {
            defaultAddressLookupDelegate.updateAddressInputData {
                this.set(getValidMockAddress())
            }

            defaultAddressLookupDelegate.submitAddress()

            assertEquals(getValidMockAddress(), defaultAddressLookupDelegate.addressLookupSubmitFlow.first())
        }

        @Test
        fun `and address is not valid and the state is Form, state should be invalid`() = runTest {
            defaultAddressLookupDelegate.updateAddressInputData {
                this.set(getEmptyMockAddress())
            }

            defaultAddressLookupDelegate.mutableAddressLookupStateFlow.tryEmit(
                AddressLookupState.Form(
                    getEmptyMockAddress(),
                ),
            )
            defaultAddressLookupDelegate.submitAddress()

            assert(defaultAddressLookupDelegate.addressLookupStateFlow.first() is AddressLookupState.InvalidUI)
        }
    }

    private fun getMockList(loading: Boolean = false) = listOf(
        LookupOption(
            lookupAddress = LookupAddress(
                id = "id_1",
                address = makeAddressData(
                    street = "street_1",
                ),
            ),
            isLoading = loading,
        ),
        LookupOption(
            lookupAddress = LookupAddress(
                id = "id_2",
                address = makeAddressData(
                    street = "street_2",
                ),
            ),
        ),
    )

    @Suppress("LongParameterList")
    private fun makeAddressData(
        street: String = "street",
        city: String = "city",
        country: String = "country",
        postalCode: String = "postalCode",
        stateOrProvince: String = "stateOrProvince",
        houseNumberOrName: String = "houseNumberOrName",
        apartmentSuite: String = "apartmentSuite"
    ) = AddressData(
        postalCode = postalCode,
        street = street,
        stateOrProvince = stateOrProvince,
        houseNumberOrName = houseNumberOrName,
        apartmentSuite = apartmentSuite,
        city = city,
        country = country,
    )

    private fun getValidMockAddress() = AddressInputModel(
        street = "street",
        city = "city",
        country = "country",
        postalCode = "postalCode",
        stateOrProvince = "stateOrProvince",
        houseNumberOrName = "houseNumberOrName",
    )

    private fun getEmptyMockAddress() = AddressInputModel(
        street = "",
        city = "",
        country = "",
        postalCode = "",
        stateOrProvince = "",
        houseNumberOrName = "",
    )
}
