/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/2/2021.
 */
package com.adyen.checkout.card

import com.adyen.checkout.card.internal.ui.DefaultCardDelegate.Companion.BIN_VALUE_EXTENDED_LENGTH
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate.Companion.BIN_VALUE_LENGTH
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate.Companion.ENCRYPTION_KEY_FOR_KCP_PASSWORD
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate.Companion.EXTENDED_CARD_NUMBER_LENGTH
import com.adyen.checkout.card.internal.ui.DefaultCardDelegate.Companion.LAST_FOUR_LENGTH
import com.adyen.checkout.card.internal.ui.model.CardDelegateState
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.util.DetectedCardTypesUtils
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.internal.util.runCompileOnly
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.threeds2.ThreeDS2Service

/**
 * Represents the state of [CardComponent].
 */
data class CardComponentState(
    override val data: PaymentComponentData<CardPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
    val cardBrand: CardBrand?,
    val binValue: String,
    val lastFourDigits: String?,
) : PaymentComponentState<CardPaymentMethod>

internal fun CardDelegateState.toComponentState(
    analyticsManager: AnalyticsManager,
): CardComponentState {
    val cardNumber = cardNumberDelegateState.value

    val firstCardBrand = DetectedCardTypesUtils.getSelectedOrFirstDetectedCardType(
        detectedCardTypes = detectedCardTypes,
    )?.cardBrand

    val binValue =
        if (cardNumberDelegateState.validation?.isValid() == true && cardNumber.length >= EXTENDED_CARD_NUMBER_LENGTH) {
            cardNumber.take(BIN_VALUE_EXTENDED_LENGTH)
        } else {
            cardNumber.take(BIN_VALUE_LENGTH)
        }

    // This safe call is needed because _componentStateFlow is null while this is called the first time.
    @Suppress("UNNECESSARY_SAFE_CALL")
    if (_componentStateFlow?.value?.binValue != binValue) {
        onBinValueListener?.invoke(binValue)
    }

    val publicKey = publicKey

    // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
    if (!isValid || publicKey == null) {
        return CardComponentState(
            data = PaymentComponentData(null, null, null),
            isInputValid = outputData.isValid,
            isReady = publicKey != null,
            cardBrand = firstCardBrand,
            binValue = binValue,
            lastFourDigits = null,
        )
    }

    val unencryptedCardBuilder = UnencryptedCard.Builder()

    val encryptedCard: EncryptedCard = try {
        unencryptedCardBuilder.setNumber(cardNumberDelegateState.value)
        if (!isCvcHidden()) {
            val cvc = securityCodeDelegateState.value
            if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
        }
        val expiryDateResult = expiryDateDelegateState.value
        if (expiryDateResult != EMPTY_DATE) {
            unencryptedCardBuilder.setExpiryDate(
                expiryMonth = expiryDateResult.expiryMonth.toString(),
                expiryYear = expiryDateResult.expiryYear.toString(),
            )
        }

        cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        val event = GenericEvents.error(paymentMethod.type.orEmpty(), com.adyen.checkout.components.core.internal.analytics.ErrorEvent.ENCRYPTION)
        analyticsManager.trackEvent(event)

        exceptionChannel.trySend(e)

        return CardComponentState(
            data = PaymentComponentData(null, null, null),
            isInputValid = false,
            isReady = true,
            cardBrand = firstCardBrand,
            binValue = binValue,
            lastFourDigits = null,
        )
    }

    return mapComponentState(
        encryptedCard,
        outputData,
        cardNumber,
        firstCardBrand,
        binValue,
    )
}

private fun mapComponentState(
    encryptedCard: EncryptedCard,
    stateOutputData: CardOutputData,
    cardNumber: String,
    firstCardBrand: com.adyen.checkout.core.CardBrand?,
    binValue: String
): CardComponentState {
    val cardPaymentMethod = CardPaymentMethod(
        type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
        checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
    ).apply {
        encryptedCardNumber = encryptedCard.encryptedCardNumber
        encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth
        encryptedExpiryYear = encryptedCard.encryptedExpiryYear

        if (!isCvcHidden()) {
            encryptedSecurityCode = encryptedCard.encryptedSecurityCode
        }

        if (isHolderNameRequired()) {
            holderName = stateOutputData.holderNameState.value
        }

        if (isKCPAuthRequired()) {
            publicKey?.let { publicKey ->
                encryptedPassword = genericEncryptor.encryptField(
                    ENCRYPTION_KEY_FOR_KCP_PASSWORD,
                    stateOutputData.kcpCardPasswordState.value,
                    publicKey,
                )
            } ?: throw CheckoutException("Encryption failed because public key cannot be found.")
            taxNumber = stateOutputData.kcpBirthDateOrTaxNumberState.value
        }

        brand = getCardBrand(stateOutputData.detectedCardTypes)

        fundingSource = getFundingSource()

        threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion }
    }

    val paymentComponentData = makePaymentComponentData(cardPaymentMethod, stateOutputData)

    val lastFour = cardNumber.takeLast(LAST_FOUR_LENGTH)

    return CardComponentState(
        data = paymentComponentData,
        isInputValid = true,
        isReady = true,
        cardBrand = firstCardBrand,
        binValue = binValue,
        lastFourDigits = lastFour,
    )
}
