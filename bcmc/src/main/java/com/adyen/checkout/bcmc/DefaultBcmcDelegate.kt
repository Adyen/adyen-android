/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2022.
 */

package com.adyen.checkout.bcmc

import com.adyen.checkout.card.CardValidationMapper
import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.util.CardValidationUtils
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.exception.EncryptionException
import com.adyen.threeds2.ThreeDS2Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class DefaultBcmcDelegate(
    private val paymentMethod: PaymentMethod,
    private val publicKeyRepository: PublicKeyRepository,
    private val configuration: BcmcConfiguration,
    private val cardValidationMapper: CardValidationMapper,
    private val cardEncrypter: CardEncrypter,
) : BcmcDelegate {

    private val _outputDataFlow = MutableStateFlow<BcmcOutputData?>(null)
    override val outputDataFlow: Flow<BcmcOutputData?> = _outputDataFlow

    private val outputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<CardPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<CardPaymentMethod>?> = _componentStateFlow

    private val _exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private var publicKey: String? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        fetchPublicKey(coroutineScope)
    }

    private fun fetchPublicKey(coroutineScope: CoroutineScope) {
        Logger.d(TAG, "fetchPublicKey")
        coroutineScope.launch {
            publicKeyRepository.fetchPublicKey(
                environment = configuration.environment,
                clientKey = configuration.clientKey
            ).fold(
                onSuccess = { key ->
                    Logger.d(TAG, "Public key fetched")
                    publicKey = key
                    outputData?.let { createComponentState(it) }
                },
                onFailure = { e ->
                    Logger.e(TAG, "Unable to fetch public key")
                    _exceptionFlow.tryEmit(ComponentException("Unable to fetch publicKey.", e))
                }
            )
        }
    }

    override fun onInputDataChanged(inputData: BcmcInputData) {
        val outputData = BcmcOutputData(
            validateCardNumber(inputData.cardNumber),
            CardValidationUtils.validateExpiryDate(inputData.expiryDate, Brand.FieldPolicy.REQUIRED),
            inputData.isStorePaymentSelected
        )

        _outputDataFlow.tryEmit(outputData)

        createComponentState(outputData)
    }

    private fun validateCardNumber(cardNumber: String): FieldState<String> {
        val validation =
            CardValidationUtils.validateCardNumber(cardNumber, enableLuhnCheck = true, isBrandSupported = true)
        return cardValidationMapper.mapCardNumberValidation(cardNumber, validation)
    }

    @Suppress("ReturnCount")
    override fun createComponentState(outputData: BcmcOutputData) {
        val paymentComponentData = PaymentComponentData<CardPaymentMethod>()

        val publicKey = validatePublicKey(outputData, paymentComponentData) ?: return

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (isComponentStateInvalid(outputData, paymentComponentData)) return

        val encryptedCard = encryptCardData(outputData, paymentComponentData, publicKey) ?: return

        // BCMC payment method is scheme type.
        val cardPaymentMethod = CardPaymentMethod(
            type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
            encryptedCardNumber = encryptedCard.encryptedCardNumber,
            encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth,
            encryptedExpiryYear = encryptedCard.encryptedExpiryYear,
            threeDS2SdkVersion = get3DS2SdkVersion(),
        )
        paymentComponentData.apply {
            paymentMethod = cardPaymentMethod
            storePaymentMethod = outputData.isStoredPaymentMethodEnabled
            shopperReference = configuration.shopperReference
        }

        _componentStateFlow.tryEmit(PaymentComponentState(paymentComponentData, isInputValid = true, isReady = true))
    }

    private fun validatePublicKey(
        outputData: BcmcOutputData,
        paymentComponentData: PaymentComponentData<CardPaymentMethod>,
    ): String? {
        val publicKey = publicKey
        if (publicKey == null) {
            val state = PaymentComponentState(
                data = paymentComponentData,
                isInputValid = outputData.isValid,
                isReady = false,
            )

            _componentStateFlow.tryEmit(state)
        }
        return publicKey
    }

    private fun isComponentStateInvalid(
        outputData: BcmcOutputData,
        paymentComponentData: PaymentComponentData<CardPaymentMethod>,
    ): Boolean {
        if (!outputData.isValid) {
            val state = PaymentComponentState(
                data = paymentComponentData,
                isInputValid = false,
                isReady = true
            )

            _componentStateFlow.tryEmit(state)

            return true
        }
        return false
    }

    private fun encryptCardData(
        outputData: BcmcOutputData,
        paymentComponentData: PaymentComponentData<CardPaymentMethod>,
        publicKey: String,
    ): EncryptedCard? = try {
        val unencryptedCardBuilder = UnencryptedCard.Builder()
            .setNumber(outputData.cardNumberField.value)

        val expiryDateResult = outputData.expiryDateField.value
        if (expiryDateResult != ExpiryDate.EMPTY_DATE) {
            unencryptedCardBuilder.setExpiryMonth(expiryDateResult.expiryMonth.toString())
            unencryptedCardBuilder.setExpiryYear(expiryDateResult.expiryYear.toString())
        }

        cardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        _exceptionFlow.tryEmit(e)

        _componentStateFlow.tryEmit(
            PaymentComponentState(
                data = paymentComponentData,
                isInputValid = false,
                isReady = true
            )
        )

        null
    }

    private fun get3DS2SdkVersion(): String? = try {
        ThreeDS2Service.INSTANCE.sdkVersion
    } catch (e: ClassNotFoundException) {
        Logger.e(TAG, "threeDS2SdkVersion not set because 3DS2 SDK is not present in project.")
        null
    } catch (e: NoClassDefFoundError) {
        Logger.e(TAG, "threeDS2SdkVersion not set because 3DS2 SDK is not present in project.")
        null
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun isCardNumberSupported(cardNumber: String?): Boolean {
        if (cardNumber.isNullOrEmpty()) return false
        return CardType.estimate(cardNumber).contains(BcmcComponent.SUPPORTED_CARD_TYPE)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
