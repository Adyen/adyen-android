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
        onEncryptionError = onEncryptionError,
    ) ?: return invalidCardPaymentComponentState()

    val cardPaymentMethod = createPaymentMethod(
        encryptedCard = encryptedCard,
        holderName = holderName(componentParams),
        cardBrand = cardBrand(),
        checkoutAttemptId = checkoutAttemptId,
    )

    val paymentComponentData = createPaymentComponentData(cardPaymentMethod, componentParams)

    return createPaymentComponentState(paymentComponentData)
}

@Suppress("LongParameterList")
private fun CardViewState.encryptCard(
    cardEncryptor: BaseCardEncryptor,
    publicKey: String,
    onEncryptionError: (EncryptionException) -> Unit,
): EncryptedCard? {
    val unencryptedCardBuilder = UnencryptedCard.Builder()
    return try {
        unencryptedCardBuilder.setNumber(cardNumber.text)
        // TODO - Card. Add isCvcHidden check
        val cvc = securityCode.text
        if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
        if (expiryDate.text.isNotBlank()) {
            val expiryDate = ExpiryDate.from(expiryDate.text)
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

private fun createPaymentMethod(
    encryptedCard: EncryptedCard,
    holderName: String?,
    cardBrand: CardBrand?,
    checkoutAttemptId: String,
) = CardPaymentMethod(
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

private fun createPaymentComponentData(
    cardPaymentMethod: CardPaymentMethod,
    componentParams: CardComponentParams
) = PaymentComponentData(
    paymentMethod = cardPaymentMethod,
    storePaymentMethod = null,
    shopperReference = componentParams.shopperReference,
    order = null,
    amount = componentParams.amount,
)

private fun createPaymentComponentState(
    paymentComponentData: PaymentComponentData<CardPaymentMethod>,
): CardPaymentComponentState {
    return CardPaymentComponentState(
        data = paymentComponentData,
        isValid = true,
    )
}

private fun invalidCardPaymentComponentState() = CardPaymentComponentState(
    data = PaymentComponentData(null, null, null),
    isValid = false,
)

private fun CardViewState.cardBrand() = dualBrandData?.selectedBrand ?: detectedCardBrands.firstOrNull()

private fun CardViewState.holderName(componentParams: CardComponentParams) =
    if (componentParams.isHolderNameRequired && holderName.text.isNotBlank()) {
        holderName.text
    } else {
        null
    }
