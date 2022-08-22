/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/7/2022.
 */

package com.adyen.checkout.giftcard

import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.GiftCardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.exception.EncryptionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class DefaultGiftCardDelegate(
    private val paymentMethod: PaymentMethod,
    private val publicKeyRepository: PublicKeyRepository,
    private val configuration: GiftCardConfiguration,
    private val cardEncrypter: CardEncrypter,
) : GiftCardDelegate {

    private val _outputDataFlow = MutableStateFlow<GiftCardOutputData?>(null)
    override val outputDataFlow: Flow<GiftCardOutputData?> = _outputDataFlow

    private val outputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow<GiftCardComponentState?>(null)
    override val componentStateFlow: Flow<GiftCardComponentState?> = _componentStateFlow

    private val _exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private var publicKey: String? = null

    override fun initialize(coroutineScope: CoroutineScope) {
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
                    val item = _exceptionFlow.tryEmit(ComponentException("Unable to fetch publicKey.", e))
                    if (!item) {
                        Logger.d(TAG, "not emitted")
                    }
                }
            )
        }
    }

    override fun onInputDataChanged(inputData: GiftCardInputData) {
        val outputData = GiftCardOutputData(cardNumber = inputData.cardNumber, pin = inputData.pin)

        _outputDataFlow.tryEmit(outputData)

        createComponentState(outputData)
    }

    @Suppress("ReturnCount")
    override fun createComponentState(outputData: GiftCardOutputData) {
        val paymentComponentData = PaymentComponentData<GiftCardPaymentMethod>()

        val publicKey = validatePublicKey(outputData, paymentComponentData) ?: return

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (isComponentStateInvalid(outputData, paymentComponentData)) return

        val encryptedCard = encryptCard(outputData, paymentComponentData, publicKey) ?: return

        val giftCardPaymentMethod = GiftCardPaymentMethod(
            type = GiftCardPaymentMethod.PAYMENT_METHOD_TYPE,
            encryptedCardNumber = encryptedCard.encryptedCardNumber,
            encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
            brand = paymentMethod.brand,
        )

        paymentComponentData.paymentMethod = giftCardPaymentMethod
        val lastDigits = outputData.giftcardNumberFieldState.value.takeLast(LAST_DIGITS_LENGTH)
        _componentStateFlow.tryEmit(
            GiftCardComponentState(
                paymentComponentData = paymentComponentData,
                isInputValid = true,
                isReady = true,
                lastFourDigits = lastDigits,
            )
        )
    }

    private fun validatePublicKey(
        outputData: GiftCardOutputData,
        paymentComponentData: PaymentComponentData<GiftCardPaymentMethod>,
    ): String? {
        val publicKey = publicKey
        if (publicKey == null) {
            val state = GiftCardComponentState(
                paymentComponentData = paymentComponentData,
                isInputValid = outputData.isValid,
                isReady = false,
                lastFourDigits = null,
            )

            _componentStateFlow.tryEmit(state)
        }
        return publicKey
    }

    private fun isComponentStateInvalid(
        outputData: GiftCardOutputData,
        paymentComponentData: PaymentComponentData<GiftCardPaymentMethod>,
    ): Boolean {
        if (!outputData.isValid) {
            val state = GiftCardComponentState(
                paymentComponentData = paymentComponentData,
                isInputValid = false,
                isReady = true,
                lastFourDigits = null,
            )

            _componentStateFlow.tryEmit(state)

            return true
        }
        return false
    }

    private fun encryptCard(
        outputData: GiftCardOutputData,
        paymentComponentData: PaymentComponentData<GiftCardPaymentMethod>,
        publicKey: String,
    ): EncryptedCard? = try {
        val unencryptedCardBuilder = UnencryptedCard.Builder()
            .setNumber(outputData.giftcardNumberFieldState.value)
            .setCvc(outputData.giftcardPinFieldState.value)

        cardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        _exceptionFlow.tryEmit(e)

        _componentStateFlow.tryEmit(
            GiftCardComponentState(
                paymentComponentData = paymentComponentData,
                isInputValid = false,
                isReady = true,
                lastFourDigits = null,
            )
        )

        null
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val LAST_DIGITS_LENGTH = 4
    }
}
