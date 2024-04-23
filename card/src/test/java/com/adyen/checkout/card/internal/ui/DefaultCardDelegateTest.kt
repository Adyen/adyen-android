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
import com.adyen.checkout.card.card
import com.adyen.checkout.card.internal.data.api.DetectCardTypeRepository
import com.adyen.checkout.card.internal.data.api.TestDetectCardTypeRepository
import com.adyen.checkout.card.internal.data.api.TestDetectedCardType
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.AddressFieldPolicyParams
import com.adyen.checkout.card.internal.ui.model.CardComponentParamsMapper
import com.adyen.checkout.card.internal.ui.model.CardListItem
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.ui.model.ExpiryDate
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.ui.model.InstallmentOption
import com.adyen.checkout.card.internal.ui.model.InstallmentOptionParams
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentsParamsMapper
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.card.internal.util.DetectedCardTypesUtils
import com.adyen.checkout.card.internal.util.InstallmentUtils
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.data.api.PublicKeyRepository
import com.adyen.checkout.components.core.internal.test.TestPublicKeyRepository
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.Environment
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import com.adyen.checkout.cse.internal.test.TestCardEncryptor
import com.adyen.checkout.cse.internal.test.TestGenericEncryptor
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.data.api.AddressRepository
import com.adyen.checkout.ui.core.internal.test.TestAddressRepository
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.AddressLookupDelegate
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import com.adyen.checkout.ui.core.internal.util.AddressFormUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class DefaultCardDelegateTest(
    @Mock private val analyticsRepository: AnalyticsRepository,
    @Mock private val submitHandler: SubmitHandler<CardComponentState>,
    @Mock private val addressLookupDelegate: AddressLookupDelegate,
) {

    private lateinit var cardEncryptor: TestCardEncryptor
    private lateinit var genericEncryptor: TestGenericEncryptor
    private lateinit var publicKeyRepository: TestPublicKeyRepository
    private lateinit var addressRepository: TestAddressRepository
    private lateinit var detectCardTypeRepository: TestDetectCardTypeRepository
    private lateinit var delegate: DefaultCardDelegate

    @BeforeEach
    fun before() {
        cardEncryptor = TestCardEncryptor()
        genericEncryptor = TestGenericEncryptor()
        publicKeyRepository = TestPublicKeyRepository()
        addressRepository = TestAddressRepository()
        detectCardTypeRepository = TestDetectCardTypeRepository()

        whenever(addressLookupDelegate.addressLookupSubmitFlow).thenReturn(MutableStateFlow(AddressInputModel()))

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
            val countriesFlow = addressRepository.countriesFlow.test(testScheduler)
            val statesFlow = addressRepository.statesFlow.test(testScheduler)

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assert(countriesFlow.values.isEmpty())
            assert(statesFlow.values.isEmpty())

            countriesFlow.cancel()
            statesFlow.cancel()
        }

        @Test
        fun `address configuration is postal code, then countries and states should not be fetched`() = runTest {
            val countriesFlow = addressRepository.countriesFlow.test(testScheduler)
            val statesFlow = addressRepository.statesFlow.test(testScheduler)
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setAddressConfiguration(AddressConfiguration.PostalCode())
                },
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assert(countriesFlow.values.isEmpty())
            assert(statesFlow.values.isEmpty())

            countriesFlow.cancel()
            statesFlow.cancel()
        }

        @Test
        fun `address repository returns error, then countries should be emitted empty`() = runTest {
            val countriesFlow = addressRepository.countriesFlow.test(testScheduler)
            val statesFlow = addressRepository.statesFlow.test(testScheduler)

            addressRepository.shouldReturnError = true
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setAddressConfiguration(AddressConfiguration.FullAddress())
                },
            )
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            assertTrue(countriesFlow.latestValue.isEmpty())
            assert(statesFlow.values.isEmpty())

            countriesFlow.cancel()
            statesFlow.cancel()
        }

        @Test
        fun `address configuration is full address with default country, then countries and states should be emitted`() =
            runTest {
                val countriesFlow = addressRepository.countriesFlow.test(testScheduler)
                val statesFlow = addressRepository.statesFlow.test(testScheduler)

                delegate = createCardDelegate(
                    configuration = createCheckoutConfiguration {
                        setAddressConfiguration(AddressConfiguration.FullAddress(defaultCountryCode = "NL"))
                    },
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                assertEquals(TestAddressRepository.COUNTRIES, countriesFlow.latestValue)
                assertEquals(TestAddressRepository.STATES, statesFlow.latestValue)

                countriesFlow.cancel()
                statesFlow.cancel()
            }

        @Test
        fun `address configuration is full address without default country, then only countries should be emitted`() =
            runTest {
                val countriesFlow = addressRepository.countriesFlow.test(testScheduler)
                val statesFlow = addressRepository.statesFlow.test(testScheduler)

                delegate = createCardDelegate(
                    configuration = createCheckoutConfiguration(shopperLocale = Locale.CANADA) {
                        setAddressConfiguration(AddressConfiguration.FullAddress())
                    },
                )
                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                assertEquals(TestAddressRepository.COUNTRIES, countriesFlow.latestValue)
                assert(statesFlow.values.isEmpty())

                countriesFlow.cancel()
                statesFlow.cancel()
            }

        @Test
        fun `When the address is changed, addressOutputDataFlow should be notified with the same data`() = runTest {
            val addressConfiguration = AddressConfiguration.FullAddress()
            val addressParams = AddressParams.FullAddress(addressFieldPolicy = AddressFieldPolicyParams.Required)
            val countryOptions = AddressFormUtils.initializeCountryOptions(
                shopperLocale = delegate.componentParams.shopperLocale,
                addressParams = addressParams,
                countryList = TestAddressRepository.COUNTRIES,
            )

            val expectedCountries = AddressFormUtils.markAddressListItemSelected(
                list = countryOptions,
                code = delegate.componentParams.shopperLocale.country,
            )

            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setAddressConfiguration(addressConfiguration)
                },
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
                    country = "Turkey",
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
                    assertEquals(
                        stateOptions,
                        AddressFormUtils.initializeStateOptions(TestAddressRepository.STATES),
                    )
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
                configuration = createCheckoutConfiguration {
                    setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                },
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
                configuration = createCheckoutConfiguration {
                    setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                },
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
                configuration = createCheckoutConfiguration {
                    setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                },
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
                CardBrand(cardType = CardType.AMERICAN_EXPRESS),
            )
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                },
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
                CardBrand(cardType = CardType.AMERICAN_EXPRESS),
            )
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setSupportedCardTypes(*supportedCardTypes.toTypedArray())
                },
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
                CardBrand(cardType = CardType.MAESTRO),
            )
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                },
            )
            detectCardTypeRepository.detectionResult = TestDetectedCardType.DUAL_BRANDED

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                val invalidLuhnCardNumber = "192382023091310912"

                delegate.updateInputData {
                    cardNumber = invalidLuhnCardNumber
                }

                // we need to update selectedCardIndex separate from cardNumber to simulate the actual use case
                delegate.updateInputData {
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
                    assertEquals(InputFieldUIState.HIDDEN, expiryDateUIState)
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
                configuration = getCustomCardConfiguration(),
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
                CardListItem(CardBrand(cardType = CardType.AMERICAN_EXPRESS), false, Environment.TEST),
            )
            val supportedCardBrands = cardBrands.map { it.cardBrand }
            val installmentConfiguration = InstallmentConfiguration(
                InstallmentOptions.DefaultInstallmentOptions(
                    maxInstallments = 3,
                    includeRevolving = true,
                ),
            )
            val expectedInstallmentParams = InstallmentParams(
                InstallmentOptionParams.DefaultInstallmentOptions(
                    values = listOf(2, 3),
                    includeRevolving = true,
                ),
                shopperLocale = Locale.US,
            )

            val addressConfiguration = AddressConfiguration.FullAddress()
            val addressParams = AddressParams.FullAddress(addressFieldPolicy = AddressFieldPolicyParams.Required)

            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setHideCvc(true)
                    setHideCvcStoredCard(true)
                    setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
                    setInstallmentConfigurations(installmentConfiguration)
                    setHolderNameRequired(true)
                    setAddressConfiguration(addressConfiguration)
                    setKcpAuthVisibility(KCPAuthVisibility.SHOW)
                    setSupportedCardTypes(*supportedCardBrands.toTypedArray())
                    setShowStorePaymentField(false)
                },
            )

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.outputDataFlow.test {
                val installmentModel = InstallmentModel(
                    numberOfInstallments = 1,
                    option = InstallmentOption.REVOLVING,
                    amount = null,
                    shopperLocale = Locale.US,
                    showAmount = false,
                )

                delegate.updateInputData {
                    cardNumber = TEST_CARD_NUMBER
                    securityCode = TEST_SECURITY_CODE
                    expiryDate = TEST_EXPIRY_DATE
                    holderName = "S. Hopper"
                    socialSecurityNumber = "123.123.123-12"
                    kcpBirthDateOrTaxNumber = "9011672845"
                    kcpCardPassword = "12"
                    isStorePaymentMethodSwitchChecked = true
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
                    countryList = TestAddressRepository.COUNTRIES,
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
                    stateOptions = AddressFormUtils.initializeStateOptions(TestAddressRepository.STATES),
                )

                val expectedDetectedCardTypes = detectCardTypeRepository.getDetectedCardTypesLocal(supportedCardBrands)

                val expectedInstallmentOptions = InstallmentUtils.makeInstallmentOptions(
                    expectedInstallmentParams,
                    expectedDetectedCardTypes.first().cardBrand,
                    true,
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
                    shouldStorePaymentMethod = true,
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
                    isCardListVisible = false,
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
            cardEncryptor.shouldThrowException = true

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
                            Validation.Invalid(R.string.checkout_card_number_not_valid),
                        ),
                    ),
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
                            Validation.Invalid(R.string.checkout_expiry_date_not_valid),
                        ),
                    ),
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
                    assertEquals(false, storePaymentMethod)
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
                    assertEquals(CardType.VISA.txVariant, brand)
                    assertNull(holderName)
                    assertNull(taxNumber)
                    assertNull(encryptedPassword)
                    assertNull(fundingSource)
                    assertNull(storedPaymentMethodId)
                    assertEquals("2.2.15", threeDS2SdkVersion)
                }
            }
        }

        @Test
        fun `output data with custom config is valid, then component state should be good`() = runTest {
            delegate = createCardDelegate(
                paymentMethod = PaymentMethod(fundingSource = "funding_source_1"),
                configuration = getCustomCardConfiguration(),
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
                    isOptional = false,
                )

                val addressUIState = AddressFormUIState.FULL_ADDRESS
                val installmentModel = InstallmentModel(
                    numberOfInstallments = 1,
                    option = InstallmentOption.REVOLVING,
                    amount = null,
                    shopperLocale = Locale.US,
                    showAmount = false,
                )

                val detectedCardTypes = listOf(
                    createDetectedCardType(),
                    createDetectedCardType(
                        isSelected = true,
                        cardBrand = CardBrand(cardType = CardType.VISA),
                    ),
                )

                delegate.updateComponentState(
                    createOutputData(
                        holderNameState = FieldState("S. Hopper", Validation.Valid),
                        socialSecurityNumberState = FieldState("0108", Validation.Valid),
                        kcpBirthDateOrTaxNumberState = FieldState("3445456", Validation.Valid),
                        kcpCardPasswordState = FieldState("12", Validation.Valid),
                        addressState = addressOutputData,
                        installmentState = FieldState(installmentModel, Validation.Valid),
                        shouldStorePaymentMethod = true,
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
                            CardListItem(CardBrand(cardType = CardType.AMERICAN_EXPRESS), false, Environment.TEST),
                        ),
                    ),
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
                    assertNull(storePaymentMethod)
                    assertEquals("shopper_android", shopperReference)
                    assertEquals("0108", socialSecurityNumber)
                    assertEquals(expectedAddress, billingAddress)
                    assertEquals(expectedInstallments, installments)
                    assertEquals(TEST_ORDER, order)
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
                    assertEquals("2.2.15", threeDS2SdkVersion)
                }
            }
        }

        @Test
        fun `card number is less than 16 digits, then the binValue should be 6 digits`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    createOutputData(cardNumberState = FieldState("12345678901234", Validation.Valid)),
                )

                val componentState = expectMostRecentItem()

                assertEquals(DefaultCardDelegate.BIN_VALUE_LENGTH, componentState.binValue.length)
            }
        }

        @Test
        fun `card number is more than 16 digits, then the binValue should be 8 digits`() = runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateComponentState(
                    createOutputData(cardNumberState = FieldState("1234567890123456", Validation.Valid)),
                )

                val componentState = expectMostRecentItem()

                assertEquals(DefaultCardDelegate.BIN_VALUE_EXTENDED_LENGTH, componentState.binValue.length)
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.card.internal.ui.DefaultCardDelegateTest#shouldStorePaymentMethodSource")
        fun `storePaymentMethod in component state should match store switch visibility and state`(
            isStorePaymentMethodSwitchVisible: Boolean,
            isStorePaymentMethodSwitchChecked: Boolean,
            expectedStorePaymentMethod: Boolean?,
        ) = runTest {
            val configuration = createCheckoutConfiguration {
                setShowStorePaymentField(isStorePaymentMethodSwitchVisible)
            }
            delegate = createCardDelegate(configuration = configuration)
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    cardNumber = TEST_CARD_NUMBER
                    securityCode = TEST_SECURITY_CODE
                    expiryDate = TEST_EXPIRY_DATE
                    this.isStorePaymentMethodSwitchChecked = isStorePaymentMethodSwitchChecked
                }

                val componentState = expectMostRecentItem()
                assertEquals(expectedStorePaymentMethod, componentState.data.storePaymentMethod)
            }
        }

        @ParameterizedTest
        @MethodSource("com.adyen.checkout.card.internal.ui.DefaultCardDelegateTest#amountSource")
        fun `when input data is valid then amount is propagated in component state if set`(
            configurationValue: Amount?,
            expectedComponentStateValue: Amount?,
        ) = runTest {
            if (configurationValue != null) {
                val configuration = createCheckoutConfiguration(amount = configurationValue)
                delegate = createCardDelegate(configuration = configuration)
            }
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    cardNumber = TEST_CARD_NUMBER
                    securityCode = TEST_SECURITY_CODE
                    expiryDate = TEST_EXPIRY_DATE
                }
                assertEquals(expectedComponentStateValue, expectMostRecentItem().data.amount)
            }
        }
    }

    @Test
    fun `when delegate is initialized then analytics event is sent`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        verify(analyticsRepository).setupAnalytics()
    }

    @Nested
    inner class SubmitButtonVisibilityTest {

        @Test
        fun `when submit button is configured to be hidden, then it should not show`() {
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(false)
                },
            )

            assertFalse(delegate.shouldShowSubmitButton())
        }

        @Test
        fun `when submit button is configured to be visible, then it should show`() {
            delegate = createCardDelegate(
                configuration = createCheckoutConfiguration {
                    setSubmitButtonVisible(true)
                },
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

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when component state is valid then PaymentMethodDetails should contain checkoutAttemptId`() = runTest {
            whenever(analyticsRepository.getCheckoutAttemptId()) doReturn TEST_CHECKOUT_ATTEMPT_ID

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.componentStateFlow.test {
                delegate.updateInputData {
                    cardNumber = TEST_CARD_NUMBER
                    securityCode = TEST_SECURITY_CODE
                    expiryDate = TEST_EXPIRY_DATE
                }

                assertEquals(TEST_CHECKOUT_ATTEMPT_ID, expectMostRecentItem().data.paymentMethod?.checkoutAttemptId)
            }
        }
    }

    @Nested
    inner class OnBinValueListenerTest {

        @Test
        fun `when on bin value listener is set, then it should be called`() = runTest {
            val expectedBinValue = "545454"
            val cardNumber = expectedBinValue + "1234567891"

            delegate.setOnBinValueListener { binValue ->
                launch(this.coroutineContext) {
                    assertEquals(expectedBinValue, binValue)
                }
            }

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData { this.cardNumber = cardNumber }
        }

        @Test
        fun `when on bin value listener is called again with the same value, then it should be called only once`() =
            runTest {
                val expectedBinValue = "545454"
                val cardNumber = expectedBinValue + "1234567891"
                var timesCalled = 0

                delegate.setOnBinValueListener { binValue ->
                    timesCalled++

                    launch(this.coroutineContext) {
                        assertEquals(expectedBinValue, binValue)
                        assertEquals(1, timesCalled)
                    }
                }

                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.updateInputData { this.cardNumber = cardNumber }
                delegate.updateInputData { this.cardNumber = cardNumber }
            }
    }

    @Nested
    inner class OnBinLookupListenerTest {

        @Test
        fun `when card number is detected locally, then callback should be called with unreliable result`() = runTest {
            detectCardTypeRepository.detectionResult = TestDetectedCardType.DETECTED_LOCALLY

            delegate.setOnBinLookupListener { data ->
                launch(this.coroutineContext) {
                    with(data.first()) {
                        assertEquals("visa", brand)
                        assertNull(paymentMethodVariant)
                        assertFalse(isReliable)
                    }
                }
            }

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            delegate.updateInputData { cardNumber = "5555444" }
        }

        @Test
        fun `when card number is detected over network, then callback should be called with reliable result`() =
            runTest {
                detectCardTypeRepository.detectionResult = TestDetectedCardType.FETCHED_FROM_NETWORK

                delegate.setOnBinLookupListener { data ->
                    launch(this.coroutineContext) {
                        with(data.first()) {
                            assertEquals("mc", brand)
                            assertEquals("mccredit", paymentMethodVariant)
                            assertTrue(isReliable)
                        }
                    }
                }

                delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

                delegate.updateInputData { cardNumber = "5555444" }
            }

        @Test
        fun `when callback is called multiple times, then it should only trigger if the data changed`() = runTest {
            detectCardTypeRepository.detectionResult = TestDetectedCardType.FETCHED_FROM_NETWORK
            var timesTriggered = 0

            delegate.setOnBinLookupListener {
                timesTriggered++
            }

            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            // Trigger first time
            delegate.updateInputData { cardNumber = "5555444" }
            // Shouldn't trigger
            delegate.updateInputData { cardNumber = "55554444" }
            detectCardTypeRepository.detectionResult = TestDetectedCardType.DETECTED_LOCALLY
            // Trigger second time
            delegate.updateInputData { cardNumber = "555544443" }

            assertEquals(2, timesTriggered)
        }
    }

    @Test
    fun `when startAddressLookup is called view flow should emit AddressLookup`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.startAddressLookup()
        delegate.viewFlow.test {
            assertEquals(CardComponentViewType.AddressLookup, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when view type is AddressLookup and handleBackPress() is called DefaultCardView should be emitted`() =
        runTest {
            delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
            delegate.startAddressLookup()
            assertTrue(delegate.handleBackPress())
            delegate.viewFlow.test {
                assertEquals(CardComponentViewType.DefaultCardView, awaitItem())
                expectNoEvents()
            }
        }

    @Test
    fun `when view type is DefaultCardView and handleBackPress() is called it should return false`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        assertFalse(delegate.handleBackPress())
    }

    @Test
    fun `when delegate is cleared then address lookup delegate is cleared`() = runTest {
        delegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        delegate.onCleared()
        verify(addressLookupDelegate).clear()
    }

    @Suppress("LongParameterList")
    private fun createCardDelegate(
        publicKeyRepository: PublicKeyRepository = this.publicKeyRepository,
        addressRepository: AddressRepository = this.addressRepository,
        detectCardTypeRepository: DetectCardTypeRepository = this.detectCardTypeRepository,
        cardValidationMapper: CardValidationMapper = CardValidationMapper(),
        cardEncryptor: BaseCardEncryptor = this.cardEncryptor,
        genericEncryptor: BaseGenericEncryptor = this.genericEncryptor,
        configuration: CheckoutConfiguration = createCheckoutConfiguration(),
        paymentMethod: PaymentMethod = PaymentMethod(type = PaymentMethodTypes.SCHEME),
        analyticsRepository: AnalyticsRepository = this.analyticsRepository,
        submitHandler: SubmitHandler<CardComponentState> = this.submitHandler,
        order: OrderRequest? = TEST_ORDER,
        addressLookupDelegate: AddressLookupDelegate = this.addressLookupDelegate
    ): DefaultCardDelegate {
        val componentParams = CardComponentParamsMapper(
            commonComponentParamsMapper = CommonComponentParamsMapper(),
            installmentsParamsMapper = InstallmentsParamsMapper(),
        ).mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = Locale.US,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = paymentMethod,
        )

        return DefaultCardDelegate(
            observerRepository = PaymentObserverRepository(),
            paymentMethod = paymentMethod,
            order = order,
            publicKeyRepository = publicKeyRepository,
            componentParams = componentParams,
            cardEncryptor = cardEncryptor,
            addressRepository = addressRepository,
            detectCardTypeRepository = detectCardTypeRepository,
            cardValidationMapper = cardValidationMapper,
            genericEncryptor = genericEncryptor,
            analyticsRepository = analyticsRepository,
            submitHandler = submitHandler,
            addressLookupDelegate = addressLookupDelegate,
        )
    }

    private fun createCheckoutConfiguration(
        shopperLocale: Locale = Locale.US,
        amount: Amount? = null,
        configuration: CardConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        card {
            setSupportedCardTypes(CardType.VISA, CardType.MASTERCARD)
            apply(configuration)
        }
    }

    private fun getCustomCardConfiguration() = createCheckoutConfiguration {
        setHideCvc(true)
        setShopperReference("shopper_android")
        setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
        setInstallmentConfigurations(
            InstallmentConfiguration(
                InstallmentOptions.DefaultInstallmentOptions(
                    maxInstallments = 3,
                    includeRevolving = true,
                ),
            ),
        )
        setHolderNameRequired(true)
        setAddressConfiguration(AddressConfiguration.FullAddress())
        setKcpAuthVisibility(KCPAuthVisibility.SHOW)
        setShowStorePaymentField(false)
        setSupportedCardTypes(CardType.VISA, CardType.MASTERCARD, CardType.AMERICAN_EXPRESS)
    }

    @Suppress("LongParameterList")
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
        shouldStorePaymentMethod: Boolean = false,
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
                Environment.TEST,
            ),
            CardListItem(
                CardBrand(cardType = CardType.MASTERCARD),
                false,
                Environment.TEST,
            ),
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
            shouldStorePaymentMethod = shouldStorePaymentMethod,
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
            isCardListVisible = isCardListVisible,
        )
    }

    @Suppress("LongParameterList")
    private fun createDetectedCardType(
        cardBrand: CardBrand = CardBrand(cardType = CardType.MASTERCARD),
        isReliable: Boolean = true,
        enableLuhnCheck: Boolean = true,
        cvcPolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy: Brand.FieldPolicy = Brand.FieldPolicy.REQUIRED,
        isSupported: Boolean = true,
        panLength: Int? = null,
        paymentMethodVariant: String? = null,
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
            paymentMethodVariant = paymentMethodVariant,
            isSelected = isSelected,
        )
    }

    @Suppress("LongParameterList")
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
            stateOptions = stateOptions,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CARD_NUMBER = "5555444433331111"
        private val TEST_EXPIRY_DATE = ExpiryDate(3, 2030)
        private const val TEST_SECURITY_CODE = "737"
        private val TEST_ORDER = OrderRequest("PSP", "ORDER_DATA")
        private const val TEST_CHECKOUT_ATTEMPT_ID = "TEST_CHECKOUT_ATTEMPT_ID"

        @JvmStatic
        fun shouldStorePaymentMethodSource() = listOf(
            // isStorePaymentMethodSwitchVisible, isStorePaymentMethodSwitchChecked, expectedStorePaymentMethod
            arguments(false, false, null),
            arguments(false, true, null),
            arguments(true, false, false),
            arguments(true, true, true),
        )

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, expectedComponentStateValue
            arguments(Amount("EUR", 100), Amount("EUR", 100)),
            arguments(Amount("USD", 0), Amount("USD", 0)),
            arguments(null, null),
        )
    }
}
