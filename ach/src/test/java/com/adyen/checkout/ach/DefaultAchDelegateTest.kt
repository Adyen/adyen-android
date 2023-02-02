/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 31/1/2023.
 */

package com.adyen.checkout.ach

import com.adyen.checkout.ach.testrepository.TestAddressRepository
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.api.model.AddressItem
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.AddressRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.test.TestPublicKeyRepository
import com.adyen.checkout.components.ui.AddressFormUIState
import com.adyen.checkout.components.ui.AddressInputModel
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.util.AddressFormUtils
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.GenericEncrypter
import com.adyen.checkout.cse.test.TestGenericEncrypter
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.testFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultAchDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
) {

    private lateinit var publicKeyRepository: TestPublicKeyRepository
    private lateinit var addressRepository: TestAddressRepository
    private lateinit var genericEncrypter: TestGenericEncrypter
    private lateinit var delegate: AchDelegate

    @BeforeEach
    fun setUp() {
        publicKeyRepository = TestPublicKeyRepository()
        addressRepository = TestAddressRepository()
        genericEncrypter = TestGenericEncrypter()
        delegate = createAchDelegate()
    }

    @Test
    fun `when fetching the public key fails, then an error is propagated`() = runTest {
        publicKeyRepository.shouldReturnError = true
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        val exception = delegate.exceptionFlow.first()
        assertEquals(publicKeyRepository.errorResult.exceptionOrNull(), exception.cause)
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {

        @Test
        fun `address configuration is none, then countries and states should not be fetched`() = runTest {
            delegate = createAchDelegate(configuration = getAchConfigurationBuilder().apply {
                setAddressConfiguration(AddressConfiguration.None)
            }.build())

            val countriesFlow = mutableListOf<List<AddressItem>>()
            val statesFlow = mutableListOf<List<AddressItem>>()
            val jobCountries = testFlow(addressRepository.countriesFlow, countriesFlow)
            val jobStateFlow = testFlow(addressRepository.statesFlow, statesFlow)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            runCurrent()
            assertEquals(countriesFlow.size, 0)
            assertEquals(statesFlow.size, 0)

            jobCountries.cancel()
            jobStateFlow.cancel()
        }

        @Test
        fun `address configuration is full address and shopper local is US, then countries and states should be emitted`() =
            runTest {
                val countriesFlow = mutableListOf<List<AddressItem>>()
                val statesFlow = mutableListOf<List<AddressItem>>()
                val jobCountries = testFlow(addressRepository.countriesFlow, countriesFlow)
                val jobStateFlow = testFlow(addressRepository.statesFlow, statesFlow)
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                runCurrent()

                assertEquals(TestAddressRepository.COUNTRIES, countriesFlow.firstOrNull())
                assertEquals(TestAddressRepository.STATES, statesFlow.firstOrNull())

                jobCountries.cancel()
                jobStateFlow.cancel()
            }

        @Test
        fun `when address configuration is NONE, addressUIState in outputdata must be NONE`() = runTest {
            val configuration =
                getAchConfigurationBuilder().setAddressConfiguration(AddressConfiguration.None).build()
            delegate = createAchDelegate(configuration = configuration)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputData = delegate.outputDataFlow.first()

            assertEquals(outputData.addressUIState, AddressFormUIState.NONE)
        }

        @Test
        fun `when address configuration is FullAddress, addressUIState in outputdata must be FullAddress`() = runTest {
            val configuration =
                getAchConfigurationBuilder().setAddressConfiguration(AddressConfiguration.FullAddress()).build()
            delegate = createAchDelegate(configuration = configuration)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val outputData = delegate.outputDataFlow.first()

            assertEquals(outputData.addressUIState, AddressFormUIState.FULL_ADDRESS)
        }

        @Test
        fun `when the address is changed, addressOutputDataFlow should be notified with the same data`() = runTest {
            val configuration =
                getAchConfigurationBuilder().setAddressConfiguration(AddressConfiguration.FullAddress()).build()
            val componentParams = AchComponentParamsMapper(null).mapToParams(configuration)
            val countryOptions = AddressFormUtils.initializeCountryOptions(
                shopperLocale = componentParams.shopperLocale,
                addressParams = componentParams.addressParams,
                countryList = TestAddressRepository.COUNTRIES
            )

            val expectedCountries = AddressFormUtils.markAddressListItemSelected(
                list = countryOptions,
                code = componentParams.shopperLocale.country,
            )
            val addressInputModel =
                AddressInputModel(
                    postalCode = "34220",
                    street = "Street Name",
                    stateOrProvince = "province",
                    houseNumberOrName = "44",
                    apartmentSuite = "aparment",
                    city = "Istanbul",
                    country = "Turkey"
                )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData { address = addressInputModel }
            val addressOutputData = delegate.addressOutputDataFlow.first()

            with(addressOutputData) {
                assertEquals(addressInputModel.postalCode, postalCode.value)
                assertEquals(addressInputModel.street, street.value)
                assertEquals(addressInputModel.stateOrProvince, stateOrProvince.value)
                assertEquals(addressInputModel.houseNumberOrName, houseNumberOrName.value)
                assertEquals(addressInputModel.apartmentSuite, apartmentSuite.value)
                assertEquals(addressInputModel.city, city.value)
                assertEquals(addressInputModel.country, country.value)
                assertEquals(expectedCountries, countryOptions)
                assertEquals(stateOptions, AddressFormUtils.initializeStateOptions(TestAddressRepository.STATES))
            }
        }

        @Test
        fun `when bank account number is valid, bankAccountNumber in outputdata must be valid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.updateInputData { bankAccountNumber = TEST_BANK_ACCOUNT_NUMBER }
            val outputData = delegate.outputDataFlow.first()
            assertEquals(outputData.bankAccountNumber.validation, Validation.Valid)
        }

        @Test
        fun `when bank account number is not valid, bankAccountNumber in outputdata must be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.updateInputData { bankAccountNumber = "" }
            val outputData = delegate.outputDataFlow.first()
            assertEquals(
                outputData.bankAccountNumber.validation, Validation.Invalid(
                    reason = R.string.checkout_ach_bank_account_number_invalid
                )
            )
        }

        @Test
        fun `when bank bankLocationId number is valid, bankLocationId in outputdata must be valid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.updateInputData { bankLocationId = TEST_BANK_BANK_LOCATION_ID }
            val outputData = delegate.outputDataFlow.first()
            assertEquals(outputData.bankLocationId.validation, Validation.Valid)
        }

        @Test
        fun `when bankLocationId is not valid, bankLocationId in outputdata must be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.updateInputData { bankLocationId = "" }
            val outputData = delegate.outputDataFlow.first()
            assertEquals(
                outputData.bankLocationId.validation, Validation.Invalid(
                    reason = R.string.checkout_ach_bank_account_location_invalid
                )
            )
        }

        @Test
        fun `when bank ownerName number is valid, ownerName in outputdata must be valid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.updateInputData { ownerName = TEST_OWNER_NAME }
            val outputData = delegate.outputDataFlow.first()
            assertEquals(outputData.ownerName.validation, Validation.Valid)
        }

        @Test
        fun `when ownerName is not valid, ownerName in outputdata must be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.updateInputData { ownerName = "" }
            val outputData = delegate.outputDataFlow.first()
            assertEquals(
                outputData.ownerName.validation, Validation.Invalid(
                    reason = R.string.checkout_ach_bank_account_holder_name_invalid
                )
            )
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {
        @Test
        fun `component is not initialized, then component state should not be ready`() = runTest {
            val componentState = delegate.componentStateFlow.first()
            assertFalse(componentState.isReady)
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData {
                bankLocationId = TEST_BANK_BANK_LOCATION_ID
                bankAccountNumber = TEST_BANK_ACCOUNT_NUMBER
                ownerName = TEST_OWNER_NAME
                address = AddressInputModel(
                    postalCode = "34220",
                    street = "Street Name",
                    stateOrProvince = "province",
                    houseNumberOrName = "44",
                    apartmentSuite = "aparment",
                    city = "Istanbul",
                    country = "Turkey"
                )
            }

            val componentState = delegate.componentStateFlow.first()

            assertTrue(componentState.isReady)
            assertTrue(componentState.isInputValid)
        }

        @Test
        fun `when bankLocationId is invalid, then component state should be invalid`() = runTest {
            val configuration =
                getAchConfigurationBuilder().setAddressConfiguration(AddressConfiguration.None).build()
            delegate = createAchDelegate(configuration = configuration)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData {
                bankLocationId = ""
                bankAccountNumber = TEST_BANK_ACCOUNT_NUMBER
                ownerName = TEST_OWNER_NAME
                address = AddressInputModel()
            }

            val componentState = delegate.componentStateFlow.first()

            assertTrue(componentState.isReady)
            assertFalse(componentState.isInputValid)
        }

        @Test
        fun `when bankAccountNumber is invalid, then component state should be invalid`() = runTest {
            val configuration =
                getAchConfigurationBuilder().setAddressConfiguration(AddressConfiguration.None).build()
            delegate = createAchDelegate(configuration = configuration)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData {
                bankLocationId = TEST_BANK_BANK_LOCATION_ID
                bankAccountNumber = ""
                ownerName = TEST_OWNER_NAME
                address = AddressInputModel()
            }

            val componentState = delegate.componentStateFlow.first()

            assertTrue(componentState.isReady)
            assertFalse(componentState.isInputValid)
        }

        @Test
        fun `when ownerName is invalid, then component state should be invalid`() = runTest {
            val configuration =
                getAchConfigurationBuilder().setAddressConfiguration(AddressConfiguration.None).build()
            delegate = createAchDelegate(configuration = configuration)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData {
                bankLocationId = TEST_BANK_BANK_LOCATION_ID
                bankAccountNumber = TEST_BANK_ACCOUNT_NUMBER
                ownerName = ""
                address = AddressInputModel()
            }

            val componentState = delegate.componentStateFlow.first()

            assertTrue(componentState.isReady)
            assertFalse(componentState.isInputValid)
        }

        @Test
        fun `when address is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData {
                bankLocationId = TEST_BANK_BANK_LOCATION_ID
                bankAccountNumber = TEST_BANK_ACCOUNT_NUMBER
                ownerName = TEST_OWNER_NAME
                address = AddressInputModel()
            }

            val componentState = delegate.componentStateFlow.first()

            assertTrue(componentState.isReady)
            assertFalse(componentState.isInputValid)
        }

        @Test
        fun `when all fields in outputdata are valid, then component state should be valid`() = runTest {
            val configuration =
                getAchConfigurationBuilder().setAddressConfiguration(AddressConfiguration.None).build()
            delegate = createAchDelegate(configuration = configuration)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData {
                bankLocationId = TEST_BANK_BANK_LOCATION_ID
                bankAccountNumber = TEST_BANK_ACCOUNT_NUMBER
                ownerName = ""
                address = AddressInputModel()
            }

            val componentState = delegate.componentStateFlow.first()

            assertTrue(componentState.isReady)
            assertFalse(componentState.isInputValid)
        }
    }

    @Nested
    inner class SubmitButtonVisibilityTest {
        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createAchDelegate(
                configuration = getAchConfigurationBuilder()
                    .setSubmitButtonVisible(false)
                    .build()
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createAchDelegate(
                configuration = getAchConfigurationBuilder()
                    .setSubmitButtonVisible(true)
                    .build()
            )

            assertTrue(delegate.shouldShowSubmitButton())
        }
    }

    private fun createAchDelegate(
        paymentMethod: PaymentMethod = PaymentMethod(),
        analyticsRepository: AnalyticsRepository = this.analyticsRepository,
        publicKeyRepository: PublicKeyRepository = this.publicKeyRepository,
        addressRepository: AddressRepository = this.addressRepository,
        genericEncrypter: GenericEncrypter = this.genericEncrypter,
        configuration: AchConfiguration = getAchConfigurationBuilder().build(),
    ) = DefaultAchDelegate(
        observerRepository = PaymentObserverRepository(),
        paymentMethod = paymentMethod,
        analyticsRepository = analyticsRepository,
        publicKeyRepository = publicKeyRepository,
        addressRepository = addressRepository,
        submitHandler = SubmitHandler(),
        genericEncrypter = genericEncrypter,
        componentParams = AchComponentParamsMapper(null).mapToParams(configuration)
    )

    private fun getAchConfigurationBuilder() = AchConfiguration.Builder(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_BANK_ACCOUNT_NUMBER = "123456"
        private const val TEST_BANK_BANK_LOCATION_ID = "123456789"
        private const val TEST_OWNER_NAME = "Joseph"
    }
}
