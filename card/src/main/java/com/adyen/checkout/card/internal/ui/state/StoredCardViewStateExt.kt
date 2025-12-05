/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.old.internal.util.runCompileOnly
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.EncryptionException
import com.adyen.checkout.cse.UnencryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.threeds2.ThreeDS2Service

@Suppress("ReturnCount", "LongParameterList")
internal fun StoredCardViewState.toPaymentComponentState(
    componentParams: CardComponentParams,
    cardEncryptor: BaseCardEncryptor,
    checkoutAttemptId: String,
    storedPaymentMethodId: String?,
    onEncryptionFailed: (EncryptionException) -> Unit,
    onPublicKeyNotFound: (RuntimeException) -> Unit,
): CardPaymentComponentState {
    val publicKey = componentParams.publicKey(onPublicKeyNotFound = onPublicKeyNotFound)
        ?: return invalidCardPaymentComponentState()

    val encryptedCard = encryptCard(
        cardEncryptor = cardEncryptor,
        publicKey = publicKey,
        onEncryptionFailed = onEncryptionFailed,
    ) ?: return invalidCardPaymentComponentState()

    val cardPaymentMethod = createPaymentMethod(
        storedCardId = storedPaymentMethodId,
        encryptedCard = encryptedCard,
        checkoutAttemptId = checkoutAttemptId,
    )

    val paymentComponentData = createPaymentComponentData(cardPaymentMethod, componentParams)

    return createPaymentComponentState(paymentComponentData)
}

@Suppress("LongParameterList")
private fun StoredCardViewState.encryptCard(
    cardEncryptor: BaseCardEncryptor,
    publicKey: String,
    onEncryptionFailed: (EncryptionException) -> Unit,
): EncryptedCard? {
    val unencryptedCardBuilder = UnencryptedCard.Builder()
    return try {
        // TODO - Card. Add isCvcHidden check
        val cvc = securityCode.text
        if (cvc.isNotEmpty()) unencryptedCardBuilder.setCvc(cvc)
        cardEncryptor.encryptFields(unencryptedCardBuilder.build(), publicKey)
    } catch (e: EncryptionException) {
        onEncryptionFailed(e)
        null
    }
}

private fun invalidCardPaymentComponentState() = CardPaymentComponentState(
    data = PaymentComponentData(null, null, null),
    isValid = false,
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

private fun CardComponentParams.publicKey(onPublicKeyNotFound: (RuntimeException) -> Unit): String? {
    return publicKey ?: run {
        onPublicKeyNotFound(RuntimeException("Public key is missing."))
        null
    }
}

private fun createPaymentMethod(
    storedCardId: String?,
    encryptedCard: EncryptedCard,
    checkoutAttemptId: String,
) = CardPaymentMethod(
    type = CardPaymentMethod.PAYMENT_METHOD_TYPE,
    checkoutAttemptId = checkoutAttemptId,
    storedPaymentMethodId = storedPaymentMethodId(storedCardId),
    encryptedSecurityCode = encryptedCard.encryptedSecurityCode,
    threeDS2SdkVersion = runCompileOnly { ThreeDS2Service.INSTANCE.sdkVersion },
)

private fun storedPaymentMethodId(storedPaymentMethodId: String?): String {
    return storedPaymentMethodId ?: "ID_NOT_FOUND"
}
