/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/8/2022.
 */

package com.adyen.checkout.card

import androidx.annotation.StringRes
import app.cash.turbine.test
import app.cash.turbine.testIn
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.repository.AddressRepository
import com.adyen.checkout.card.repository.DetectCardTypeRepository
import com.adyen.checkout.card.test.TestAddressRepository
import com.adyen.checkout.card.test.TestDetectCardTypeRepository
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.card.util.AddressFormUtils
import com.adyen.checkout.card.util.DetectedCardTypesUtils
import com.adyen.checkout.card.util.InstallmentUtils
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.test.TestPublicKeyRepository
import com.adyen.checkout.components.ui.ComponentMode
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.GenericEncrypter
import com.adyen.checkout.cse.test.TestCardEncrypter
import com.adyen.checkout.cse.test.TestGenericEncrypter
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
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultCardDelegateTest {

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
                    .setAddressConfiguration(AddressConfiguration.PostalCode)
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
        fun `address configuration is full address, then countries and states should be emitted`() = runTest {
            val countriesFlow = addressRepository.countriesFlow.testIn(this)
            val statesFlow = addressRepository.statesFlow.testIn(this)
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setAddressConfiguration(AddressConfiguration.FullAddress())
                    .build()
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertEquals(TestAddressRepository.COUNTRIES, countriesFlow.awaitItem())
            assertEquals(TestAddressRepository.STATES, statesFlow.awaitItem())

            countriesFlow.cancelAndIgnoreRemainingEvents()
            statesFlow.cancelAndIgnoreRemainingEvents()
        }

        @Test
        fun `detect card type repository returns error, then output data should not have detected cards`() = runTest {
            detectCardTypeRepository.detectionResult = TestDetectCardTypeRepository.TestDetectedCardType.ERROR
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(CardInputData())

                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(detectedCardTypes.isEmpty())
                }
            }
        }

        @Test
        fun `detect card type repository returns supported cards, then output data should contain them`() = runTest {
            val supportedCardTypes = listOf(CardType.VISA, CardType.MASTERCARD, CardType.AMERICAN_EXPRESS)
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardTypes.toTypedArray())
                    .build()
            )
            detectCardTypeRepository.detectionResult =
                TestDetectCardTypeRepository.TestDetectedCardType.DETECTED_LOCALLY

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(CardInputData())

                val expectedDetectedCardTypes =
                    detectCardTypeRepository.getDetectedCardTypesLocal(supportedCardTypes)

                with(requireNotNull(expectMostRecentItem())) {
                    assertEquals(expectedDetectedCardTypes, detectedCardTypes)
                    assertFalse(isDualBranded)
                }
            }
        }

        @Test
        fun `detect card type repository returns unsupported cards, then output data should filter them`() = runTest {
            val supportedCardTypes = listOf(CardType.VISA, CardType.AMERICAN_EXPRESS)
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardTypes.toTypedArray())
                    .build()
            )
            detectCardTypeRepository.detectionResult =
                TestDetectCardTypeRepository.TestDetectedCardType.FETCHED_FROM_NETWORK

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                delegate.onInputDataChanged(CardInputData())

                val expectedDetectedCardTypes =
                    detectCardTypeRepository.getDetectedCardTypesNetwork(supportedCardTypes).filter { it.isSupported }

                with(requireNotNull(expectMostRecentItem())) {
                    assertEquals(expectedDetectedCardTypes, detectedCardTypes)
                    assertFalse(isDualBranded)
                }
            }
        }

        @Test
        fun `detect card type repository returns dual branded cards, then output data should be good`() = runTest {
            val supportedCardTypes = listOf(CardType.BCMC, CardType.MAESTRO)
            delegate = createCardDelegate(
                configuration = getDefaultCardConfigurationBuilder()
                    .setSupportedCardTypes(*supportedCardTypes.toTypedArray())
                    .build()
            )
            detectCardTypeRepository.detectionResult = TestDetectCardTypeRepository.TestDetectedCardType.DUAL_BRANDED

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                val invalidLuhnCardNumber = "192382023091310912"

                val inputData = delegate.inputData.apply {
                    cardNumber = invalidLuhnCardNumber
                    selectedCardIndex = 1
                }
                delegate.onInputDataChanged(inputData)

                val expectedDetectedCardTypes = DetectedCardTypesUtils.filterDetectedCardTypes(
                    detectCardTypeRepository.getDetectedCardTypesDualBranded(supportedCardTypes),
                    inputData.selectedCardIndex
                )

                val selectedCard =
                    requireNotNull(DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(expectedDetectedCardTypes))

                assertFalse(selectedCard.enableLuhnCheck)
                assertEquals(Brand.FieldPolicy.HIDDEN, selectedCard.expiryDatePolicy)
                assertEquals(Brand.FieldPolicy.OPTIONAL, selectedCard.cvcPolicy)

                with(requireNotNull(expectMostRecentItem())) {
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
                delegate.onInputDataChanged(CardInputData())

                with(requireNotNull(expectMostRecentItem())) {
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
                delegate.onInputDataChanged(CardInputData())

                with(requireNotNull(expectMostRecentItem())) {
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
                val inputData = delegate.inputData.apply {
                    cardNumber = TEST_CARD_NUMBER
                    securityCode = TEST_SECURITY_CODE
                    expiryDate = TEST_EXPIRY_DATE
                }

                delegate.onInputDataChanged(inputData)

                with(requireNotNull(expectMostRecentItem())) {
                    assertTrue(isValid)
                    assertEquals(createOutputData(), this)
                }
            }
        }

        @Test
        fun `input data with custom config is valid, then output data should be good`() = runTest {
            val supportedCardTypes = listOf(CardType.VISA, CardType.MASTERCARD, CardType.AMERICAN_EXPRESS)
            val installmentConfiguration = InstallmentConfiguration(
                InstallmentOptions.DefaultInstallmentOptions(
                    maxInstallments = 3,
                    includeRevolving = true
                )
            )
            val addressConfiguration = AddressConfiguration.FullAddress()
            delegate = createCardDelegate(
                configuration = CardConfiguration.Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY)
                    .setHideCvc(true)
                    .setHideCvcStoredCard(true)
                    .setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
                    .setInstallmentConfigurations(installmentConfiguration)
                    .setHolderNameRequired(true)
                    .setAddressConfiguration(addressConfiguration)
                    .setKcpAuthVisibility(KCPAuthVisibility.SHOW)
                    .setSupportedCardTypes(*supportedCardTypes.toTypedArray())
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

                val inputData = delegate.inputData.apply {
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

                delegate.onInputDataChanged(inputData)


                val expectedAddressOutputData = createAddressOutputData(
                    postalCode = FieldState("1011 DJ", Validation.Valid),
                    street = FieldState("Simon Carmiggeltstraat", Validation.Valid),
                    stateOrProvince = FieldState("North Holland", Validation.Valid),
                    houseNumberOrName = FieldState("6", Validation.Valid),
                    apartmentSuite = FieldState("apt", Validation.Valid),
                    city = FieldState("Amsterdam", Validation.Valid),
                    country = FieldState("Netherlands", Validation.Valid),
                )

                val expectedDetectedCardTypes = detectCardTypeRepository.getDetectedCardTypesLocal(supportedCardTypes)

                val expectedInstallmentOptions = InstallmentUtils.makeInstallmentOptions(
                    installmentConfiguration,
                    expectedDetectedCardTypes.first().cardType,
                    true
                )

                val countryOptions = AddressFormUtils.initializeCountryOptions(
                    addressConfiguration = addressConfiguration,
                    countryList = TestAddressRepository.COUNTRIES
                )

                val expectedCountries = AddressFormUtils.markAddressListItemSelected(
                    countryOptions,
                    inputData.address.country
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
                    countryOptions = expectedCountries,
                    stateOptions = AddressFormUtils.initializeStateOptions(TestAddressRepository.STATES),
                    kcpBirthDateOrTaxNumberHint = R.string.checkout_kcp_tax_number_hint,
                    supportedCardTypes = supportedCardTypes,
                )

                with(requireNotNull(expectMostRecentItem())) {
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
                delegate.createComponentState(createOutputData())

                val componentState = requireNotNull(expectMostRecentItem())

                assertFalse(componentState.isReady)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `encryption fails, then component state should be invalid`() = runTest {
            cardEncrypter.shouldThrowException = true

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(createOutputData())

                val componentState = requireNotNull(expectMostRecentItem())

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `card number in output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    createOutputData(
                        cardNumberState = FieldState(
                            "12345678",
                            Validation.Invalid(R.string.checkout_card_number_not_valid)
                        )
                    )
                )

                val componentState = requireNotNull(expectMostRecentItem())

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `expiry date in output data is invalid, then component state should be invalid`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(
                    createOutputData(
                        expiryDateState = FieldState(
                            ExpiryDate(10, 2020),
                            Validation.Invalid(R.string.checkout_expiry_date_not_valid)
                        )
                    )
                )

                val componentState = requireNotNull(expectMostRecentItem())

                assertTrue(componentState.isReady)
                assertFalse(componentState.isInputValid)
                assertNull(componentState.lastFourDigits)
            }
        }

        @Test
        fun `output data with default config is valid, then component state should be good`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.createComponentState(createOutputData())

                val componentState = requireNotNull(expectMostRecentItem())

                assertTrue(componentState.isValid)
                assertEquals(TEST_CARD_NUMBER.takeLast(4), componentState.lastFourDigits)
                assertEquals(TEST_CARD_NUMBER.take(6), componentState.binValue)
                assertEquals(CardType.VISA, componentState.cardType)

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
                    assertNull(threeDS2SdkVersion)
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
                        cardType = CardType.VISA
                    )
                )

                delegate.createComponentState(
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
                        countryOptions = emptyList(),
                        stateOptions = emptyList(),
                        supportedCardTypes = listOf(CardType.VISA, CardType.MASTERCARD, CardType.AMERICAN_EXPRESS),
                    )
                )

                val componentState = requireNotNull(expectMostRecentItem())

                val expectedAddress = AddressFormUtils.makeAddressData(addressOutputData, addressUIState)
                val expectedInstallments = InstallmentUtils.makeInstallmentModelObject(installmentModel)

                assertTrue(componentState.isValid)
                assertEquals(TEST_CARD_NUMBER.takeLast(4), componentState.lastFourDigits)
                assertEquals(TEST_CARD_NUMBER.take(6), componentState.binValue)
                assertEquals(CardType.VISA, componentState.cardType)

                val paymentComponentData = componentState.data
                with(paymentComponentData) {
                    assertTrue(storePaymentMethod)
                    assertEquals("shopper_android", shopperReference)
                    assertEquals("0108", socialSecurityNumber)
                    assertEquals(expectedAddress, billingAddress)
                    assertEquals(expectedInstallments, installments)
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
                    assertNull(encryptedSecurityCode)
                    assertEquals("S. Hopper", holderName)
                    assertEquals("3445456", taxNumber)
                    assertEquals("12", encryptedPassword)
                    assertEquals("funding_source_1", fundingSource)
                    assertEquals(PaymentMethodTypes.SCHEME, type)
                    assertEquals(CardType.VISA.txVariant, brand)
                    assertNull(storedPaymentMethodId)
                    assertNull(threeDS2SdkVersion)
                }
            }
        }
    }

    private fun createCardDelegate(
        publicKeyRepository: PublicKeyRepository = this.publicKeyRepository,
        addressRepository: AddressRepository = this.addressRepository,
        detectCardTypeRepository: DetectCardTypeRepository = this.detectCardTypeRepository,
        cardValidationMapper: CardValidationMapper = CardValidationMapper(),
        cardEncrypter: CardEncrypter = this.cardEncrypter,
        genericEncrypter: GenericEncrypter = this.genericEncrypter,
        configuration: CardConfiguration = getDefaultCardConfigurationBuilder().build(),
        paymentMethod: PaymentMethod = PaymentMethod(),
    ): DefaultCardDelegate {
        return DefaultCardDelegate(
            paymentMethod = paymentMethod,
            publicKeyRepository = publicKeyRepository,
            configuration = configuration,
            cardEncrypter = cardEncrypter,
            addressRepository = addressRepository,
            detectCardTypeRepository = detectCardTypeRepository,
            cardValidationMapper = cardValidationMapper,
            genericEncrypter = genericEncrypter,
        )
    }

    private fun getDefaultCardConfigurationBuilder(): CardConfiguration.Builder {
        return CardConfiguration
            .Builder(Locale.US, Environment.TEST, TEST_CLIENT_KEY)
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
            detectCardTypeRepository.getDetectedCardTypesLocal(listOf(CardType.VISA)),
        isSocialSecurityNumberRequired: Boolean = false,
        isKCPAuthRequired: Boolean = false,
        addressUIState: AddressFormUIState = AddressFormUIState.NONE,
        installmentOptions: List<InstallmentModel> = emptyList(),
        countryOptions: List<AddressListItem> = emptyList(),
        stateOptions: List<AddressListItem> = emptyList(),
        isDualBranded: Boolean = false,
        @StringRes kcpBirthDateOrTaxNumberHint: Int = R.string.checkout_kcp_birth_date_or_tax_number_hint,
        supportedCardTypes: List<CardType> = listOf(CardType.VISA),
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
            countryOptions = countryOptions,
            stateOptions = stateOptions,
            supportedCardTypes = supportedCardTypes,
            isDualBranded = isDualBranded,
            kcpBirthDateOrTaxNumberHint = kcpBirthDateOrTaxNumberHint,
            componentMode = ComponentMode.DEFAULT,
        )
    }

    private fun createDetectedCardType(
        cardType: CardType = CardType.MASTERCARD,
        isReliable: Boolean = true,
        enableLuhnCheck: Boolean = true,
        cvcPolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        isSupported: Boolean = true,
        isSelected: Boolean = false,
    ): DetectedCardType {
        return DetectedCardType(
            cardType = cardType,
            isReliable = isReliable,
            enableLuhnCheck = enableLuhnCheck,
            cvcPolicy = cvcPolicy,
            expiryDatePolicy = expiryDatePolicy,
            isSupported = isSupported,
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
    ): AddressOutputData {
        return AddressOutputData(
            postalCode = postalCode,
            street = street,
            stateOrProvince = stateOrProvince,
            houseNumberOrName = houseNumberOrName,
            apartmentSuite = apartmentSuite,
            city = city,
            country = country,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CARD_NUMBER = "5555444433331111"
        private val TEST_EXPIRY_DATE = ExpiryDate(3, 2030)
        private const val TEST_SECURITY_CODE = "737"
    }
}
