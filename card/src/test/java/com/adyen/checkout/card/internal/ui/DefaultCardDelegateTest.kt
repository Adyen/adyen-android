/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/8/2022.
 */

package com.adyen.checkout.card.internal.ui

import androidx.annotation.StringRes
import app.cash.turbine.test
import app.cash.turbine.testIn
import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.R
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.data.api.DetectCardTypeRepository
import com.adyen.checkout.card.internal.data.api.TestDetectCardTypeRepository
import com.adyen.checkout.card.internal.data.api.TestDetectedCardType
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate.Companion.BIN_VALUE_EXTENDED_LENGTH
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate.Companion.BIN_VALUE_LENGTH
import com.adyen.checkout.card.internal.ui.model.AddressFieldPolicyParams
import com.adyen.checkout.card.internal.ui.model.CardComponentParamsMapper
import com.adyen.checkout.card.internal.ui.model.CardListItem
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.ui.model.ExpiryDate
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.ui.model.InstallmentOption
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.card.internal.util.DetectedCardTypesUtils
import com.adyen.checkout.card.internal.util.InstallmentUtils
import com.adyen.checkout.components.analytics.AnalyticsRepository
import com.adyen.checkout.components.model.AddressListItem
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.repository.AddressRepository
import com.adyen.checkout.components.repository.PaymentObserverRepository
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.test.TestAddressRepository
import com.adyen.checkout.components.test.TestPublicKeyRepository
import com.adyen.checkout.components.ui.AddressFormUIState
import com.adyen.checkout.components.ui.AddressInputModel
import com.adyen.checkout.components.ui.AddressOutputData
import com.adyen.checkout.components.ui.AddressParams
import com.adyen.checkout.components.ui.ComponentMode
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.SubmitHandler
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.util.AddressFormUtils
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.internal.BaseCardEncrypter
import com.adyen.checkout.cse.internal.BaseGenericEncrypter
import com.adyen.checkout.cse.internal.test.TestCardEncrypter
import com.adyen.checkout.cse.internal.test.TestGenericEncrypter
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultCardDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val submitHandler: SubmitHandler<CardComponentState>
) {

    private lateinit var cardEncrypter: TestCardEncrypter
    private lateinit var genericEncrypter: TestGenericEncrypter
    private lateinit var publicKeyRepository: TestPublicKeyRepository
    private lateinit var addressRepository: TestAddressRepository
    private lateinit var detectCardTypeRepository: TestDetectCardTypeRepository
    private lateinit var delegate: DefaultCardDelegate

    @BeforeEach
    fun before() {
        cardEncrypter = TestCardEncrypter()
        genericEncrypter = TestGenericEncrypter()
        publicKeyRepository = TestPublicKeyRepository()
        addressRepository = TestAddressRepository()
        detectCardTypeRepository = TestDetectCardTypeRepository()
        delegate = createCardDelegate()
    }

    @Test
    fun `when component is not initialized, then an error is propagated`() = runTest {
        publicKeyRepository.shouldReturnError = true

        delegate.exceptionFlow.test {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val exception = expectMostRecentItem()

            assertEquals(publicKeyRepository.errorResult.exceptionOrNull(), exception.cause)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("when input data changes and")
    inner class InputDataChangedTest {
        @Test
        fun `address configuration is none, then countries and states should not be fetched`() = runTest {
            val countriesFlow = addressRepository.countriesFlow.testIn(this)
            val statesFlow = addressRepository.statesFlow.testIn(this)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            countriesFlow.expectNoEvents()
            statesFlow.expectNoEvents()

            countriesFlow.cancelAndIgnoreRemainingEvents()
            statesFlow.cancelAndIgnoreRemainingEvents()
        }

        @Test
        fun `address configuration is postal code, then countries and states should not be fetched`() = runTest {
            val countriesFlow = addressRepository.countriesFlow.testIn(this)
            val statesFlow = addressRepository.statesFlow.testIn(this)
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setAddressConfiguration(AddressConfiguration.PostalCode())
                    .build()
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            countriesFlow.expectNoEvents()
            statesFlow.expectNoEvents()

            countriesFlow.cancelAndIgnoreRemainingEvents()
            statesFlow.cancelAndIgnoreRemainingEvents()
        }

        @Test
        fun `address repository returns error, then countries should be emitted empty`() = runTest {
            val countriesFlow = addressRepository.countriesFlow.testIn(this)
            val statesFlow = addressRepository.statesFlow.testIn(this)

            addressRepository.shouldReturnError = true
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setAddressConfiguration(AddressConfiguration.FullAddress())
                    .build()
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertTrue(countriesFlow.awaitItem().isEmpty())
            statesFlow.expectNoEvents()

            countriesFlow.cancelAndIgnoreRemainingEvents()
            statesFlow.cancelAndIgnoreRemainingEvents()
        }

        @Test
        fun `address configuration is full address with default country, then countries and states should be emitted`() =
            runTest {
                val countriesFlow = addressRepository.countriesFlow.testIn(this)
                val statesFlow = addressRepository.statesFlow.testIn(this)

                delegate = createCardDelegate(
                    configuration = getDefaultCardConfigurationBuilder()
                        .setAddressConfiguration(AddressConfiguration.FullAddress(defaultCountryCode = "NL"))
                        .build()
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                assertEquals(TestAddressRepository.COUNTRIES, countriesFlow.awaitItem())
                assertEquals(TestAddressRepository.STATES, statesFlow.awaitItem())

                countriesFlow.cancelAndIgnoreRemainingEvents()
                statesFlow.cancelAndIgnoreRemainingEvents()
            }

        @Test
        fun `address configuration is full address without default country, then only countries should be emitted`() =
            runTest {
                val countriesFlow = addressRepository.countriesFlow.testIn(this)
                val statesFlow = addressRepository.statesFlow.testIn(this)

                delegate = createCardDelegate(
                    configuration = getDefaultCardConfigurationBuilder(shopperLocale = Locale.CANADA)
                        .setAddressConfiguration(AddressConfiguration.FullAddress())
                        .build()
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                assertEquals(TestAddressRepository.COUNTRIES, countriesFlow.awaitItem())
                statesFlow.expectNoEvents()

                countriesFlow.cancelAndIgnoreRemainingEvents()
                statesFlow.cancelAndIgnoreRemainingEvents()
            }

        @Test
        fun `When the address is changed, addressOutputDataFlow should be notified with the same data`() = runTest {
            val addressConfiguration = AddressConfiguration.FullAddress()
            val addressParams = AddressParams.FullAddress(addressFieldPolicy = AddressFieldPolicyParams.Required)
            val countryOptions = AddressFormUtils.initializeCountryOptions(
                shopperLocale = delegate.componentParams.shopperLocale,
                addressParams = addressParams,
                countryList = TestAddressRepository.COUNTRIES
            )

            val expectedCountries = AddressFormUtils.markAddressListItemSelected(
                list = countryOptions,
                code = delegate.componentParams.shopperLocale.country,
            )

            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setAddressConfiguration(addressConfiguration)
                    .build()
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

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

            delegate.addressOutputDataFlow.test {
                delegate.updateInputData {
                    address = addressInputModel
                }
                with(expectMostRecentItem()) {
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
        }

        @Test
        fun `detect card type repository returns error, then output data should not have detected cards`() = runTest {
            detectCardTypeRepository.detectionResult = TestDetectedCardType.ERROR
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                with(expectMostRecentItem()) {
                    assertTrue(detectedCardTypes.isEmpty())
                }
            }
        }

        @Test
        fun `When a card brand is detected, isCardListVisible should be false`() = runTest {
            val supportedCardBrands = listOf(CardBrand(cardType = CardType.VISA))
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                    .build()
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateComponentState(createOutputData())
                delegate.updateInputData { /* Empty to trigger an update */ }
                with(expectMostRecentItem()) {
                    assertFalse(isCardListVisible)
                }
            }
        }

        @Test
        fun `When a card brand is not detected, isCardListVisible should be true`() = runTest {
            val supportedCardBrands = listOf(CardBrand(cardType = CardType.VISA))
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                    .build()
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            detectCardTypeRepository.detectionResult = TestDetectedCardType.EMPTY

            delegate.outputDataFlow.test {
                delegate.updateComponentState(createOutputData(detectedCardTypes = emptyList()))
                delegate.updateInputData { /* Empty to trigger an update */ }
                with(expectMostRecentItem()) {
                    assertTrue(isCardListVisible)
                }
            }
        }

        @Test
        fun `When the supported card list is empty, isCardListVisible should be true`() = runTest {
            val supportedCardBrands = emptyList<CardBrand>()
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                    .build()
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            detectCardTypeRepository.detectionResult = TestDetectedCardType.EMPTY

            delegate.outputDataFlow.test {
                delegate.updateComponentState(createOutputData(detectedCardTypes = emptyList()))
                delegate.updateInputData { /* Empty to trigger an update */ }
                with(expectMostRecentItem()) {
                    assertTrue(isCardListVisible)
                }
            }
        }

        @Test
        fun `detect card type repository returns supported cards, then output data should contain them`() = runTest {
            val supportedCardBrands = listOf(
                CardBrand(cardType = CardType.VISA),
                CardBrand(cardType = CardType.MASTERCARD),
                CardBrand(cardType = CardType.AMERICAN_EXPRESS)
            )
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                    .build()
            )
            detectCardTypeRepository.detectionResult = TestDetectedCardType.DETECTED_LOCALLY

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                val expectedDetectedCardTypes = detectCardTypeRepository.getDetectedCardTypesLocal(supportedCardBrands)

                with(expectMostRecentItem()) {
                    assertEquals(expectedDetectedCardTypes, detectedCardTypes)
                    assertFalse(isDualBranded)
                }
            }
        }

        @Test
        fun `detect card type repository returns unsupported cards, then output data should filter them`() = runTest {
            val supportedCardTypes = listOf(
                CardBrand(cardType = CardType.VISA),
                CardBrand(cardType = CardType.AMERICAN_EXPRESS)
            )
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardTypes.toTypedArray())
                    .build()
            )
            detectCardTypeRepository.detectionResult = TestDetectedCardType.FETCHED_FROM_NETWORK

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                val expectedDetectedCardTypes =
                    detectCardTypeRepository.getDetectedCardTypesNetwork(supportedCardTypes).filter { it.isSupported }

                with(expectMostRecentItem()) {
                    assertEquals(expectedDetectedCardTypes, detectedCardTypes)
                    assertFalse(isDualBranded)
                }
            }
        }

        @Test
        fun `detect card type repository returns dual branded cards, then output data should be good`() = runTest {
            val supportedCardBrands = listOf(
                CardBrand(cardType = CardType.BCMC),
                CardBrand(cardType = CardType.MAESTRO)
            )
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                    .build()
            )
            detectCardTypeRepository.detectionResult = TestDetectedCardType.DUAL_BRANDED

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                val invalidLuhnCardNumber = "192382023091310912"

                delegate.updateInputData {
                    cardNumber = invalidLuhnCardNumber
                    selectedCardIndex = 1
                }

                val expectedDetectedCardTypes = DetectedCardTypesUtils.filterDetectedCardTypes(
                    detectedCardTypes = detectCardTypeRepository.getDetectedCardTypesDualBranded(supportedCardBrands),
                    selectedCardIndex = 1,
                )

                val selectedCard =
                    requireNotNull(DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(expectedDetectedCardTypes))

                assertFalse(selectedCard.enableLuhnCheck)
                assertEquals(Brand.FieldPolicy.HIDDEN, selectedCard.expiryDatePolicy)
                assertEquals(Brand.FieldPolicy.OPTIONAL, selectedCard.cvcPolicy)

                with(expectMostRecentItem()) {
                    assertEquals(expectedDetectedCardTypes, detectedCardTypes)
                    assertEquals(FieldState(invalidLuhnCardNumber, Validation.Valid), cardNumberState)
                    assertTrue(expiryDateState.validation is Validation.Valid)
                    assertTrue(securityCodeState.validation is Validation.Valid)
                    assertEquals(InputFieldUIState.OPTIONAL, cvcUIState)
                    assertEquals(InputFieldUIState.OPTIONAL, expiryDateUIState)
                    assertTrue(isDualBranded)
                }
            }
        }

        @Test
        fun `input is empty with default config, then output data should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                    assertTrue(cardNumberState.validation is Validation.Invalid)
                    assertTrue(expiryDateState.validation is Validation.Invalid)
                    assertTrue(securityCodeState.validation is Validation.Invalid)
                    assertTrue(holderNameState.validation is Validation.Valid)
                    assertTrue(socialSecurityNumberState.validation is Validation.Valid)
                    assertTrue(kcpBirthDateOrTaxNumberState.validation is Validation.Valid)
                    assertTrue(kcpCardPasswordState.validation is Validation.Valid)
                    assertTrue(installmentState.validation is Validation.Valid)
                    assertTrue(addressState.isValid)
                }
            }
        }

        @Test
        fun `input is empty with custom config, then output data should be invalid`() = runTest {
            delegate = createCardDelegate(
                configuration = getCustomCardConfigurationBuilder().build()
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData { /* Empty to trigger an update */ }

                with(expectMostRecentItem()) {
                    assertFalse(isValid)
                    assertTrue(cardNumberState.validation is Validation.Invalid)
                    assertTrue(expiryDateState.validation is Validation.Invalid)
                    assertTrue(securityCodeState.validation is Validation.Valid)
                    assertTrue(holderNameState.validation is Validation.Invalid)
                    assertTrue(socialSecurityNumberState.validation is Validation.Invalid)
                    assertTrue(kcpBirthDateOrTaxNumberState.validation is Validation.Invalid)
                    assertTrue(kcpCardPasswordState.validation is Validation.Invalid)
                    assertTrue(installmentState.validation is Validation.Valid)
                    assertFalse(addressState.isValid)
                }
            }
        }

        @Test
        fun `input data with default config is valid, then output data should be good`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.updateInputData {
                    cardNumber = TEST_CARD_NUMBER
                    securityCode = TEST_SECURITY_CODE
                    expiryDate = TEST_EXPIRY_DATE
                }

                with(expectMostRecentItem()) {
                    assertTrue(isValid)
                    assertEquals(createOutputData(isCardListVisible = false), this)
                }
            }
        }

        @Test
        fun `input data with custom config is valid, then output data should be good`() = runTest {
            val cardBrands = listOf(
                CardListItem(CardBrand(cardType = CardType.VISA), true, Environment.TEST),
                CardListItem(CardBrand(cardType = CardType.MASTERCARD), false, Environment.TEST),
                CardListItem(CardBrand(cardType = CardType.AMERICAN_EXPRESS), false, Environment.TEST)
            )
            val supportedCardBrands = cardBrands.map { it.cardBrand }
            val installmentConfiguration = InstallmentConfiguration(
                InstallmentOptions.DefaultInstallmentOptions(
                    maxInstallments = 3,
                    includeRevolving = true
                )
            )
            val addressConfiguration = AddressConfiguration.FullAddress()
            val addressParams = AddressParams.FullAddress(addressFieldPolicy = AddressFieldPolicyParams.Required)

            delegate = createCardDelegate(
                configuration = CardConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY)
                    .setHideCvc(true)
                    .setHideCvcStoredCard(true)
                    .setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
                    .setInstallmentConfigurations(installmentConfiguration)
                    .setHolderNameRequired(true)
                    .setAddressConfiguration(addressConfiguration)
                    .setKcpAuthVisibility(KCPAuthVisibility.SHOW)
                    .setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                    .setShowStorePaymentField(false)
                    .build()
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {

                val installmentModel = InstallmentModel(
                    textResId = R.string.checkout_card_installments_option_revolving,
                    value = 1,
                    option = InstallmentOption.REVOLVING
                )

                delegate.updateInputData {
                    cardNumber = TEST_CARD_NUMBER
                    securityCode = TEST_SECURITY_CODE
                    expiryDate = TEST_EXPIRY_DATE
                    holderName = "S. Hopper"
                    socialSecurityNumber = "123.123.123-12"
                    kcpBirthDateOrTaxNumber = "9011672845"
                    kcpCardPassword = "12"
                    isStorePaymentSelected = true
                    selectedCardIndex = 0
                    installmentOption = installmentModel
                    address.apply {
                        postalCode = "1011 DJ"
                        street = "Simon Carmiggeltstraat"
                        stateOrProvince = "North Holland"
                        houseNumberOrName = "6"
                        apartmentSuite = "apt"
                        city = "Amsterdam"
                        country = "Netherlands"
                    }
                }

                val countryOptions = AddressFormUtils.initializeCountryOptions(
                    shopperLocale = delegate.componentParams.shopperLocale,
                    addressParams = addressParams,
                    countryList = TestAddressRepository.COUNTRIES
                )

                val expectedCountries = AddressFormUtils.markAddressListItemSelected(
                    list = countryOptions,
                    code = null,
                )

                val expectedAddressOutputData = createAddressOutputData(
                    postalCode = FieldState("1011 DJ", Validation.Valid),
                    street = FieldState("Simon Carmiggeltstraat", Validation.Valid),
                    stateOrProvince = FieldState("North Holland", Validation.Valid),
                    houseNumberOrName = FieldState("6", Validation.Valid),
                    apartmentSuite = FieldState("apt", Validation.Valid),
                    city = FieldState("Amsterdam", Validation.Valid),
                    country = FieldState("Netherlands", Validation.Valid),
                    isOptional = false,
                    countryOptions = expectedCountries,
                    stateOptions = AddressFormUtils.initializeStateOptions(TestAddressRepository.STATES)
                )

                val expectedDetectedCardTypes = detectCardTypeRepository.getDetectedCardTypesLocal(supportedCardBrands)

                val expectedInstallmentOptions = InstallmentUtils.makeInstallmentOptions(
                    installmentConfiguration,
                    expectedDetectedCardTypes.first().cardBrand,
                    true
                )

                val expectedOutputData = createOutputData(
                    cardNumberState = FieldState(TEST_CARD_NUMBER, Validation.Valid),
                    securityCodeState = FieldState(TEST_SECURITY_CODE, Validation.Valid),
                    expiryDateState = FieldState(TEST_EXPIRY_DATE, Validation.Valid),
                    holderNameState = FieldState("S. Hopper", Validation.Valid),
                    socialSecurityNumberState = FieldState("12312312312", Validation.Valid),
                    kcpBirthDateOrTaxNumberState = FieldState("9011672845", Validation.Valid),
                    kcpCardPasswordState = FieldState("12", Validation.Valid),
                    installmentState = FieldState(installmentModel, Validation.Valid),
                    addressState = expectedAddressOutputData,
                    isStoredPaymentMethodEnable = true,
                    cvcUIState = InputFieldUIState.HIDDEN,
                    expiryDateUIState = InputFieldUIState.REQUIRED,
                    holderNameUIState = InputFieldUIState.REQUIRED,
                    showStorePaymentField = false,
                    detectedCardTypes = expectedDetectedCardTypes,
                    isSocialSecurityNumberRequired = true,
                    isKCPAuthRequired = true,
                    addressUIState = AddressFormUIState.FULL_ADDRESS,
                    installmentOptions = expectedInstallmentOptions,
                    kcpBirthDateOrTaxNumberHint = R.string.checkout_kcp_tax_number_hint,
                    cardBrands = cardBrands,
                    isCardListVisible = false
                )

                with(expectMostRecentItem()) {
                    assertTrue(isValid)
                    assertEquals(expectedOutputData, this)
                }
            }
        }
    }

    @Nested
    @DisplayName("when creating component state and")
    inner class CreateComponentStateTest {

        @Test
        fun `component is not initialized, then component state should not be ready`() = runTest {
            delegate.componentStateFlow.test {
                delegate.updateComponentState(createOutputData())

                val componentState = expectMostRecentItem()

                assertFalse(componentState.isReady)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            cardEncrypter.shouldThrowException = true

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(createOutputData())

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `card number in output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    createOutputData(
                        cardNumberState = FieldState(
                            "12345678",
                            Validation.Invalid(R.string.checkout_card_number_not_valid)
                        )
                    )
                )

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `expiry date in output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    createOutputData(
                        expiryDateState = FieldState(
                            ExpiryDate(10, 2020),
                            Validation.Invalid(R.string.checkout_expiry_date_not_valid)
                        )
                    )
                )

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `output data with default config is valid, then component state should be good`() = runTest {
            delegate = createCardDelegate(order = null)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(createOutputData())

                val componentState = expectMostRecentItem()

                assertTrue(componentState.isValid)
                assertEquals(TEST_CARD_NUMBER.takeLast(4), componentState.lastFourDigits)
                assertEquals(TEST_CARD_NUMBER.take(8), componentState.binValue)
                assertEquals(CardBrand(cardType = CardType.VISA), componentState.cardBrand)

                val paymentComponentData = componentState.data
                with(paymentComponentData) {
                    assertFalse(storePaymentMethod)
                    assertNull(shopperReference)
                    assertNull(socialSecurityNumber)
                    assertNull(billingAddress)
                    assertNull(installments)
                    assertNull(amount)
                    assertNull(dateOfBirth)
                    assertNull(deliveryAddress)
                    assertNull(order)
                    assertNull(shopperEmail)
                    assertNull(shopperName)
                    assertNull(telephoneNumber)
                }

                with(requireNotNull(paymentComponentData.paymentMethod)) {
                    assertEquals(TEST_CARD_NUMBER, encryptedCardNumber)
                    assertEquals(TEST_EXPIRY_DATE.expiryMonth.toString(), encryptedExpiryMonth)
                    assertEquals(TEST_EXPIRY_DATE.expiryYear.toString(), encryptedExpiryYear)
                    assertEquals(TEST_SECURITY_CODE, encryptedSecurityCode)
                    assertEquals(PaymentMethodTypes.SCHEME, type)
                    assertNull(holderName)
                    assertNull(taxNumber)
                    assertNull(encryptedPassword)
                    assertNull(fundingSource)
                    assertNull(brand)
                    assertNull(storedPaymentMethodId)
                    assertEquals("2.2.11", threeDS2SdkVersion)
                }
            }
        }

        @Test
        fun `output data with custom config is valid, then component state should be good`() = runTest {
            delegate = createCardDelegate(
                paymentMethod = PaymentMethod(fundingSource = "funding_source_1"),
                configuration = getCustomCardConfigurationBuilder().build(),
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                val addressOutputData = createAddressOutputData(
                    postalCode = FieldState("1011 DJ", Validation.Valid),
                    street = FieldState("Simon Carmiggeltstraat", Validation.Valid),
                    stateOrProvince = FieldState("North Holland", Validation.Valid),
                    houseNumberOrName = FieldState("6", Validation.Valid),
                    apartmentSuite = FieldState("apt", Validation.Valid),
                    city = FieldState("Amsterdam", Validation.Valid),
                    country = FieldState("Netherlands", Validation.Valid),
                    isOptional = false
                )

                val addressUIState = AddressFormUIState.FULL_ADDRESS
                val installmentModel = InstallmentModel(
                    textResId = R.string.checkout_card_installments_option_revolving,
                    value = 1,
                    option = InstallmentOption.REVOLVING
                )

                val detectedCardTypes = listOf(
                    createDetectedCardType(),
                    createDetectedCardType().copy(
                        isSelected = true,
                        cardBrand = CardBrand(cardType = CardType.VISA)
                    )
                )

                delegate.updateComponentState(
                    createOutputData(
                        holderNameState = FieldState("S. Hopper", Validation.Valid),
                        socialSecurityNumberState = FieldState("0108", Validation.Valid),
                        kcpBirthDateOrTaxNumberState = FieldState("3445456", Validation.Valid),
                        kcpCardPasswordState = FieldState("12", Validation.Valid),
                        addressState = addressOutputData,
                        installmentState = FieldState(installmentModel, Validation.Valid),
                        isStoredPaymentMethodEnable = true,
                        cvcUIState = InputFieldUIState.REQUIRED,
                        expiryDateUIState = InputFieldUIState.REQUIRED,
                        detectedCardTypes = detectedCardTypes,
                        isSocialSecurityNumberRequired = false,
                        isKCPAuthRequired = false,
                        addressUIState = addressUIState,
                        installmentOptions = listOf(installmentModel),
                        cardBrands = listOf(
                            CardListItem(CardBrand(cardType = CardType.VISA), false, Environment.TEST),
                            CardListItem(CardBrand(cardType = CardType.MASTERCARD), false, Environment.TEST),
                            CardListItem(CardBrand(cardType = CardType.AMERICAN_EXPRESS), false, Environment.TEST)
                        ),
                    )
                )

                val componentState = expectMostRecentItem()

                val expectedAddress = AddressFormUtils.makeAddressData(addressOutputData, addressUIState)
                val expectedInstallments = InstallmentUtils.makeInstallmentModelObject(installmentModel)

                assertTrue(componentState.isValid)
                assertEquals(TEST_CARD_NUMBER.takeLast(4), componentState.lastFourDigits)
                assertEquals(TEST_CARD_NUMBER.take(8), componentState.binValue)
                assertEquals(CardBrand(cardType = CardType.VISA), componentState.cardBrand)

                val paymentComponentData = componentState.data
                with(paymentComponentData) {
                    assertTrue(storePaymentMethod)
                    assertEquals("shopper_android", shopperReference)
                    assertEquals("0108", socialSecurityNumber)
                    assertEquals(expectedAddress, billingAddress)
                    assertEquals(expectedInstallments, installments)
                    assertEquals(TEST_ORDER, order)
                    assertNull(amount)
                    assertNull(dateOfBirth)
                    assertNull(deliveryAddress)
                    assertNull(shopperEmail)
                    assertNull(shopperName)
                    assertNull(telephoneNumber)
                }

                with(requireNotNull(paymentComponentData.paymentMethod)) {
                    assertEquals(TEST_CARD_NUMBER, encryptedCardNumber)
                    assertEquals(TEST_EXPIRY_DATE.expiryMonth.toString(), encryptedExpiryMonth)
                    assertEquals(TEST_EXPIRY_DATE.expiryYear.toString(), encryptedExpiryYear)
                    assertNull(encryptedSecurityCode)
                    assertEquals("S. Hopper", holderName)
                    assertEquals("3445456", taxNumber)
                    assertEquals("12", encryptedPassword)
                    assertEquals("funding_source_1", fundingSource)
                    assertEquals(PaymentMethodTypes.SCHEME, type)
                    assertEquals(CardType.VISA.txVariant, brand)
                    assertNull(storedPaymentMethodId)
                    assertEquals("2.2.11", threeDS2SdkVersion)
                }
            }
        }

        @Test
        fun `card number is less than 16 digits, then the binValue should be 6 digits`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    createOutputData(cardNumberState = FieldState("12345678901234", Validation.Valid))
                )

                val componentState = expectMostRecentItem()

                assertEquals(BIN_VALUE_LENGTH, componentState.binValue.length)
            }
        }

        @Test
        fun `card number is more than 16 digits, then the binValue should be 8 digits`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    createOutputData(cardNumberState = FieldState("1234567890123456", Validation.Valid))
                )

                val componentState = expectMostRecentItem()

                assertEquals(BIN_VALUE_EXTENDED_LENGTH, componentState.binValue.length)
            }
        }
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).sendAnalyticsEvent()
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSubmitButtonVisible(false)
                    .build()
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSubmitButtonVisible(true)
                    .build()
            )

            assertTrue(delegate.shouldShowSubmitButton())
        }
    }

    @Nested
    inner class SubmitHandlerTest {

        @Test
        fun `when delegate is initialized then submit handler event is initialized`() = runTest {
            val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
            delegate.initialize(coroutineScope)
            verify(submitHandler).initialize(coroutineScope, delegate.componentStateFlow)
        }

        @Test
        fun `when delegate setInteractionBlocked is called then submit handler setInteractionBlocked is called`() =
            runTest {
                delegate.setInteractionBlocked(true)
                verify(submitHandler).setInteractionBlocked(true)
            }

        @Test
        fun `when delegate onSubmit is called then submit handler onSubmit is called`() = runTest {
            delegate.componentStateFlow.test {
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
                delegate.onSubmit()
                verify(submitHandler).onSubmit(expectMostRecentItem())
            }
        }
    }

    private fun createCardDelegate(
        publicKeyRepository: PublicKeyRepository = this.publicKeyRepository,
        addressRepository: AddressRepository = this.addressRepository,
        detectCardTypeRepository: DetectCardTypeRepository = this.detectCardTypeRepository,
        cardValidationMapper: CardValidationMapper = CardValidationMapper(),
        cardEncrypter: BaseCardEncrypter = this.cardEncrypter,
        genericEncrypter: BaseGenericEncrypter = this.genericEncrypter,
        configuration: CardConfiguration = getDefaultCardConfigurationBuilder().build(),
        paymentMethod: PaymentMethod = PaymentMethod(),
        analyticsRepository: AnalyticsRepository = this.analyticsRepository,
        submitHandler: SubmitHandler<CardComponentState> = this.submitHandler,
        order: OrderRequest? = TEST_ORDER,
    ): DefaultCardDelegate {
        return DefaultCardDelegate(
            observerRepository = PaymentObserverRepository(),
            paymentMethod = paymentMethod,
            order = order,
            publicKeyRepository = publicKeyRepository,
            componentParams = CardComponentParamsMapper().mapToParamsDefault(configuration, paymentMethod),
            cardEncrypter = cardEncrypter,
            addressRepository = addressRepository,
            detectCardTypeRepository = detectCardTypeRepository,
            cardValidationMapper = cardValidationMapper,
            genericEncrypter = genericEncrypter,
            analyticsRepository = analyticsRepository,
            submitHandler = submitHandler,
        )
    }

    private fun getDefaultCardConfigurationBuilder(shopperLocale: Locale = Locale.US): CardConfiguration.Builder {
        return CardConfiguration.Builder(shopperLocale, Environment.TEST, TEST_CLIENT_KEY)
            .setSupportedCardTypes(CardType.VISA)
    }

    private fun getCustomCardConfigurationBuilder(): CardConfiguration.Builder {
        return CardConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY)
            .setHideCvc(true)
            .setShopperReference("shopper_android")
            .setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
            .setInstallmentConfigurations(
                InstallmentConfiguration(
                    InstallmentOptions.DefaultInstallmentOptions(
                        maxInstallments = 3,
                        includeRevolving = true
                    )
                )
            )
            .setHolderNameRequired(true)
            .setAddressConfiguration(AddressConfiguration.FullAddress())
            .setKcpAuthVisibility(KCPAuthVisibility.SHOW)
            .setShowStorePaymentField(false)
            .setSupportedCardTypes(CardType.VISA, CardType.MASTERCARD, CardType.AMERICAN_EXPRESS)
    }

    private fun createOutputData(
        cardNumberState: FieldState<String> = FieldState(TEST_CARD_NUMBER, Validation.Valid),
        expiryDateState: FieldState<ExpiryDate> = FieldState(TEST_EXPIRY_DATE, Validation.Valid),
        securityCodeState: FieldState<String> = FieldState(TEST_SECURITY_CODE, Validation.Valid),
        holderNameState: FieldState<String> = FieldState("", Validation.Valid),
        socialSecurityNumberState: FieldState<String> = FieldState("", Validation.Valid),
        kcpBirthDateOrTaxNumberState: FieldState<String> = FieldState("", Validation.Valid),
        kcpCardPasswordState: FieldState<String> = FieldState("", Validation.Valid),
        addressState: AddressOutputData = createAddressOutputData(),
        installmentState: FieldState<InstallmentModel?> = FieldState(null, Validation.Valid),
        isStoredPaymentMethodEnable: Boolean = false,
        cvcUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
        expiryDateUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
        holderNameUIState: InputFieldUIState = InputFieldUIState.HIDDEN,
        showStorePaymentField: Boolean = true,
        detectedCardTypes: List<DetectedCardType> =
            detectCardTypeRepository.getDetectedCardTypesLocal(listOf(CardBrand(cardType = CardType.VISA))),
        isSocialSecurityNumberRequired: Boolean = false,
        isKCPAuthRequired: Boolean = false,
        addressUIState: AddressFormUIState = AddressFormUIState.NONE,
        installmentOptions: List<InstallmentModel> = emptyList(),
        isDualBranded: Boolean = false,
        @StringRes kcpBirthDateOrTaxNumberHint: Int = R.string.checkout_kcp_birth_date_or_tax_number_hint,
        cardBrands: List<CardListItem> = listOf(
            CardListItem(
                CardBrand(cardType = CardType.VISA),
                true,
                Environment.TEST
            )
        ),
        isCardListVisible: Boolean = true
    ): CardOutputData {
        return CardOutputData(
            cardNumberState = cardNumberState,
            expiryDateState = expiryDateState,
            securityCodeState = securityCodeState,
            holderNameState = holderNameState,
            socialSecurityNumberState = socialSecurityNumberState,
            kcpBirthDateOrTaxNumberState = kcpBirthDateOrTaxNumberState,
            kcpCardPasswordState = kcpCardPasswordState,
            addressState = addressState,
            installmentState = installmentState,
            isStoredPaymentMethodEnable = isStoredPaymentMethodEnable,
            cvcUIState = cvcUIState,
            expiryDateUIState = expiryDateUIState,
            holderNameUIState = holderNameUIState,
            showStorePaymentField = showStorePaymentField,
            detectedCardTypes = detectedCardTypes,
            isSocialSecurityNumberRequired = isSocialSecurityNumberRequired,
            isKCPAuthRequired = isKCPAuthRequired,
            addressUIState = addressUIState,
            installmentOptions = installmentOptions,
            cardBrands = cardBrands,
            isDualBranded = isDualBranded,
            kcpBirthDateOrTaxNumberHint = kcpBirthDateOrTaxNumberHint,
            componentMode = ComponentMode.DEFAULT,
            isCardListVisible = isCardListVisible
        )
    }

    private fun createDetectedCardType(
        cardBrand: CardBrand = CardBrand(cardType = CardType.MASTERCARD),
        isReliable: Boolean = true,
        enableLuhnCheck: Boolean = true,
        cvcPolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        isSupported: Boolean = true,
        panLength: Int? = null,
        isSelected: Boolean = false,
    ): DetectedCardType {
        return DetectedCardType(
            cardBrand = cardBrand,
            isReliable = isReliable,
            enableLuhnCheck = enableLuhnCheck,
            cvcPolicy = cvcPolicy,
            expiryDatePolicy = expiryDatePolicy,
            isSupported = isSupported,
            panLength = panLength,
            isSelected = isSelected,
        )
    }

    private fun createAddressOutputData(
        postalCode: FieldState<String> = FieldState("", Validation.Valid),
        street: FieldState<String> = FieldState("", Validation.Valid),
        stateOrProvince: FieldState<String> = FieldState("", Validation.Valid),
        houseNumberOrName: FieldState<String> = FieldState("", Validation.Valid),
        apartmentSuite: FieldState<String> = FieldState("", Validation.Valid),
        city: FieldState<String> = FieldState("", Validation.Valid),
        country: FieldState<String> = FieldState("", Validation.Valid),
        isOptional: Boolean = true,
        countryOptions: List<AddressListItem> = emptyList(),
        stateOptions: List<AddressListItem> = emptyList()
    ): AddressOutputData {
        return AddressOutputData(
            postalCode = postalCode,
            street = street,
            stateOrProvince = stateOrProvince,
            houseNumberOrName = houseNumberOrName,
            apartmentSuite = apartmentSuite,
            city = city,
            country = country,
            isOptional = isOptional,
            countryOptions = countryOptions,
            stateOptions = stateOptions
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CARD_NUMBER = "5555444433331111"
        private val TEST_EXPIRY_DATE = ExpiryDate(3, 2030)
        private const val TEST_SECURITY_CODE = "737"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
    }
}
