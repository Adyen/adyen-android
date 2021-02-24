/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.card

import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.cse.CardEncrypter
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.exception.EncryptionException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val TAG = LogUtil.getTag()

private val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SCHEME)
private const val BIN_VALUE_LENGTH = 6
private const val LAST_FOUR_LENGTH = 4

@Suppress("TooManyFunctions")
class CardComponent private constructor(
    private val cardDelegate: CardDelegate,
    cardConfiguration: CardConfiguration
) : BasePaymentComponent<CardConfiguration, CardInputData, CardOutputData, CardComponentState>(cardDelegate, cardConfiguration) {

    private var storedPaymentInputData: CardInputData? = null
    private var publicKey = ""

    init {
        viewModelScope.launch {
            publicKey = cardDelegate.fetchPublicKey()
            if (publicKey.isEmpty()) {
                notifyException(ComponentException("Unable to fetch publicKey."))
            }
        }

        if (cardDelegate is NewCardDelegate) {
            cardDelegate.binLookupFlow
                .onEach {
                    Logger.d(TAG, "New binLookupFlow emitted")
                    with(outputData) {
                        this ?: return@with
                        val newOutputData = CardOutputData(
                            cardNumberState,
                            expiryDateState,
                            securityCodeState,
                            holderNameState,
                            isStoredPaymentMethodEnable,
                            isCvcHidden,
                            it
                        )
                        notifyStateChanged(newOutputData)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    constructor(storedCardDelegate: StoredCardDelegate, cardConfiguration: CardConfiguration) : this(
        storedCardDelegate as CardDelegate,
        cardConfiguration
    ) {
        storedPaymentInputData = storedCardDelegate.getStoredCardInputData()

        // TODO: 09/12/2020 move this logic to base component, maybe create the inputdata from the delegate?
        if (!requiresInput()) {
            inputDataChanged(CardInputData())
        }
    }

    constructor(cardDelegate: NewCardDelegate, cardConfiguration: CardConfiguration) : this(
        cardDelegate as CardDelegate,
        cardConfiguration
    )

    override fun requiresInput(): Boolean {
        return cardDelegate.requiresInput()
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> {
        return PAYMENT_METHOD_TYPES
    }

    override fun onInputDataChanged(inputData: CardInputData): CardOutputData {
        Logger.v(TAG, "onInputDataChanged")

        val detectedCardTypes = cardDelegate.detectCardType(inputData.cardNumber, publicKey, viewModelScope)

        return CardOutputData(
            cardDelegate.validateCardNumber(inputData.cardNumber),
            cardDelegate.validateExpiryDate(inputData.expiryDate),
            // TODO: 29/01/2021 move validation logic using detected object
            cardDelegate.validateSecurityCode(inputData.securityCode, detectedCardTypes.firstOrNull()?.cardType),
            cardDelegate.validateHolderName(inputData.holderName),
            inputData.isStorePaymentSelected,
            cardDelegate.isCvcHidden(),
            detectedCardTypes
        )
    }

    @Suppress("ReturnCount")
    override fun createComponentState(): CardComponentState {
        Logger.v(TAG, "createComponentState")

        // TODO: 29/01/2021 pass outputData as non null parameter
        val stateOutputData = outputData ?: throw CheckoutException("Cannot create state with null outputData")

        val cardPaymentMethod = CardPaymentMethod()
        cardPaymentMethod.type = CardPaymentMethod.PAYMENT_METHOD_TYPE

        val unencryptedCardBuilder = UnencryptedCard.Builder()

        val paymentComponentData = PaymentComponentData<CardPaymentMethod>()

        val cardNumber = stateOutputData.cardNumberState.value

        val firstCardType = stateOutputData.detectedCardTypes.firstOrNull()?.cardType

        val binValue: String = getBinValueFromCardNumber(cardNumber)

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!stateOutputData.isValid) {
            return CardComponentState(paymentComponentData, false, firstCardType, binValue, null)
        }

        val encryptedCard: EncryptedCard = try {
            if (!isStoredPaymentMethod()) {
                unencryptedCardBuilder.setNumber(stateOutputData.cardNumberState.value)
            }
            if (!cardDelegate.isCvcHidden()) {
                unencryptedCardBuilder.setCvc(stateOutputData.securityCodeState.value)
            }
            val expiryDateResult = stateOutputData.expiryDateState.value
            if (expiryDateResult.expiryYear != ExpiryDate.EMPTY_VALUE && expiryDateResult.expiryMonth != ExpiryDate.EMPTY_VALUE) {
                unencryptedCardBuilder.setExpiryMonth(expiryDateResult.expiryMonth.toString())
                unencryptedCardBuilder.setExpiryYear(expiryDateResult.expiryYear.toString())
            }

            CardEncrypter.encryptFields(unencryptedCardBuilder.build(), publicKey)
        } catch (e: EncryptionException) {
            notifyException(e)
            return CardComponentState(paymentComponentData, false, firstCardType, binValue, null)
        }

        if (!isStoredPaymentMethod()) {
            cardPaymentMethod.encryptedCardNumber = encryptedCard.encryptedCardNumber
            cardPaymentMethod.encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth
            cardPaymentMethod.encryptedExpiryYear = encryptedCard.encryptedExpiryYear
        } else {
            cardPaymentMethod.storedPaymentMethodId = (mPaymentMethodDelegate as StoredCardDelegate).getId()
        }

        if (!cardDelegate.isCvcHidden()) {
            cardPaymentMethod.encryptedSecurityCode = encryptedCard.encryptedSecurityCode
        }

        if (cardDelegate.isHolderNameRequired()) {
            cardPaymentMethod.holderName = stateOutputData.holderNameState.value
        }

        paymentComponentData.paymentMethod = cardPaymentMethod
        paymentComponentData.setStorePaymentMethod(stateOutputData.isStoredPaymentMethodEnable)
        paymentComponentData.shopperReference = configuration.shopperReference

        val lastFour = getLastFourDigitsFromCardNumber(cardNumber)

        return CardComponentState(paymentComponentData, stateOutputData.isValid, firstCardType, binValue, lastFour)
    }

    fun isStoredPaymentMethod(): Boolean {
        return cardDelegate is StoredCardDelegate
    }

    fun getStoredPaymentInputData(): CardInputData? {
        return storedPaymentInputData
    }

    fun isHolderNameRequire(): Boolean {
        return cardDelegate.isHolderNameRequired()
    }

    fun showStorePaymentField(): Boolean {
        return configuration.isShowStorePaymentFieldEnable
    }

    private fun getBinValueFromCardNumber(cardNumber: String): String {
        return if (cardNumber.length < BIN_VALUE_LENGTH) cardNumber else cardNumber.substring(0..BIN_VALUE_LENGTH)
    }

    private fun getLastFourDigitsFromCardNumber(cardNumber: String): String {
        return cardNumber.takeLast(LAST_FOUR_LENGTH)
    }

    companion object {
        @JvmStatic
        val PROVIDER: StoredPaymentComponentProvider<CardComponent, CardConfiguration> = CardComponentProvider()
    }
}
