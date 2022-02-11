/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */
package com.adyen.checkout.giftcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.GiftCardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.repository.PublicKeyRepository
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.exception.EncryptionException
import kotlinx.coroutines.launch

private val TAG = LogUtil.getTag()

private const val LAST_FOUR_LENGTH = 4

/**
 * Component should not be instantiated directly. Instead use the PROVIDER object.
 *
 * @param paymentMethodDelegate [GenericPaymentMethodDelegate]
 * @param configuration [GiftCardConfiguration]
 */
class GiftCardComponent(
    savedStateHandle: SavedStateHandle,
    private val paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: GiftCardConfiguration,
    private val publicKeyRepository: PublicKeyRepository
) :
    BasePaymentComponent<GiftCardConfiguration, GiftCardInputData, GiftCardOutputData, GiftCardComponentState>(
        savedStateHandle,
        paymentMethodDelegate,
        configuration
    ) {

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration> = GiftCardComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GIFTCARD)
    }

    private var publicKey: String? = null

    init {
        viewModelScope.launch {
            try {
                publicKey = fetchPublicKey()
                notifyStateChanged()
            } catch (e: CheckoutException) {
                notifyException(ComponentException("Unable to fetch publicKey.", e))
            }
        }
    }

    private suspend fun fetchPublicKey(): String {
        return publicKeyRepository.fetchPublicKey(
            environment = configuration.environment,
            clientKey = configuration.clientKey
        )
    }

    override fun onInputDataChanged(inputData: GiftCardInputData): GiftCardOutputData {
        Logger.v(TAG, "onInputDataChanged")
        return GiftCardOutputData(cardNumber = inputData.cardNumber, pin = inputData.pin)
    }

    @Suppress("ReturnCount")
    override fun createComponentState(): GiftCardComponentState {
        val unencryptedCardBuilder = UnencryptedCard.Builder()
        val outputData = outputData
        val paymentComponentData = PaymentComponentData<GiftCardPaymentMethod>()

        val publicKey = publicKey

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (outputData?.isValid != true || publicKey == null) {
            val isInputValid = outputData?.isValid ?: false
            val isReady = publicKey != null
            return GiftCardComponentState(
                paymentComponentData = paymentComponentData,
                isInputValid = isInputValid,
                isReady = isReady,
                lastFourDigits = null
            )
        }
        val encryptedCard = try {
            unencryptedCardBuilder.setNumber(outputData.giftcardNumberFieldState.value)
            unencryptedCardBuilder.setCvc(outputData.giftcardPinFieldState.value)
            CardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (e: EncryptionException) {
            notifyException(e)
            return GiftCardComponentState(
                paymentComponentData = paymentComponentData,
                isInputValid = false,
                isReady = true,
                lastFourDigits = null
            )
        }

        val giftCardPaymentMethod = GiftCardPaymentMethod().apply {
            type = GiftCardPaymentMethod.PAYMENT_METHOD_TYPE
            encryptedCardNumber = encryptedCard.encryptedCardNumber
            encryptedSecurityCode = encryptedCard.encryptedSecurityCode
            brand = paymentMethodDelegate.paymentMethod.brand
        }
        paymentComponentData.paymentMethod = giftCardPaymentMethod
        val lastFour = outputData.giftcardNumberFieldState.value.takeLast(LAST_FOUR_LENGTH)
        return GiftCardComponentState(
            paymentComponentData = paymentComponentData,
            isInputValid = true,
            isReady = true,
            lastFourDigits = lastFour
        )
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES
}
