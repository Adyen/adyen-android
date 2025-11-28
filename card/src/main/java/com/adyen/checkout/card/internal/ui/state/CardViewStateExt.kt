/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/11/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.selectedBrand
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.helper.runCompileOnly
import com.adyen.checkout.core.common.ui.model.ExpiryDate
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.CardPaymentMethod
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.threeds2.ThreeDS2Service

@Suppress("ReturnCount")
internal fun CardViewState.toPaymentComponentState(
    componentParams: CardComponentParams,
    cardEncryptor: BaseCardEncryptor,
    checkoutAttemptId: String,
    onEncryptionError: (EncryptionException) -> Unit,
): CardPaymentComponentState {
    val publicKey = componentParams.publicKey ?: return invalidCardPaymentComponentState()

    val encryptedCard = encryptCard(
        cardEncryptor = cardEncryptor,
        publicKey = publicKey,
        cardNumber = cardNumber.text,
        expiryDate = expiryDate.text,
        securityCode = securityCode.text,
        onEncryptionError = onEncryptionError,
    ) ?: return invalidCardPaymentComponentState()

    val cardBrand = dualBrandData?.selectedBrand ?: detectedCardBrands.firstOrNull()
    val holderName = if (componentParams.isHolderNameRequired && holderName.text.isNotBlank()) {
        holderName.text
    } else {
        null
    }

    return mapComponentState(encryptedCard, holderName, cardBrand, componentParams, checkoutAttemptId)
}

private fun mapComponentState(
    encryptedCard: EncryptedCard,
    holderName: String?,
    cardBrand: CardBrand?,
    componentParams: CardComponentParams,
    checkoutAttemptId: String,
): CardPaymentComponentState {
    val cardPaymentMethod = CardPaymentMethod(
        type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
        checkoutAttemptId = checkoutAttemptId,
        encryptedCardNumber = encryptedCard.encryptedCardNumber,
        encryptedExpiryMonth = encryptedCard.encryptedExpiryMonth,
        encryptedExpiryYear = encryptedCard.encryptedExpiryYear,
        // TODO - Card. Add isCvcHidden check
        encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
        holderName = holderName,
        threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion },
        brand = cardBrand?.txVariant,
    )

    val paymentComponentData = PaymentComponentData(
        paymentMethod = cardPaymentMethod,
        storePaymentMethod = null,
        shopperReference = componentParams.shopperReference,
        order = null,
        amount = componentParams.amount,
    )

    return CardPaymentComponentState(
        data = paymentComponentData,
        isValid = true,
    )
}

@Suppress("LongParameterList")
private fun encryptCard(
    cardEncryptor: BaseCardEncryptor,
    publicKey: String,
    cardNumber: String,
    expiryDate: String,
    securityCode: String,
    onEncryptionError: (EncryptionException) -> Unit,
): EncryptedCard? {
    val unencryptedCardBuilder = UnencryptedCard.Builder()
    return try {
        unencryptedCardBuilder.setNumber(cardNumber)
        // TODO - Card. Add isCvcHidden check
        val cvc = securityCode
        if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
        if (expiryDate.isNotBlank()) {
            val expiryDate = ExpiryDate.from(expiryDate)
            unencryptedCardBuilder.setExpiryDate(
                expiryMonth = expiryDate.expiryMonth.toString(),
                expiryYear = expiryDate.expiryYear.toString(),
            )
        }

        cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        onEncryptionError(e)
        null
    }
}

private fun invalidCardPaymentComponentState() = CardPaymentComponentState(
    data = PaymentComponentData(null, null, null),
    isValid = false,
)
